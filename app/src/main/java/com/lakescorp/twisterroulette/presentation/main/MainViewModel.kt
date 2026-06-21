package com.lakescorp.twisterroulette.presentation.main

import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lakescorp.twisterroulette.R
import com.lakescorp.twisterroulette.domain.model.AppLanguage
import com.lakescorp.twisterroulette.domain.model.AppSettings
import com.lakescorp.twisterroulette.domain.model.GameResult
import com.lakescorp.twisterroulette.domain.model.Turn
import com.lakescorp.twisterroulette.domain.model.TurnVerb
import com.lakescorp.twisterroulette.domain.gamemode.GameMode
import com.lakescorp.twisterroulette.domain.gamemode.GameModeFactory
import com.lakescorp.twisterroulette.domain.usecase.GetSettingsUseCase
import com.lakescorp.twisterroulette.domain.usecase.SaveSettingsUseCase
import com.lakescorp.twisterroulette.service.speech.SpeechRecognizerManager
import com.lakescorp.twisterroulette.service.speech.SpeechState
import com.lakescorp.twisterroulette.service.tts.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

enum class PlayMode {
    MANUAL, TIMER, VOICE
}

/** A single past spin result, tagged with its turn number for the history list. */
data class HistoryEntry(
    val result: GameResult,
    val turn: Int
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val ttsManager: TtsManager,
    private val speechRecognizerManager: SpeechRecognizerManager
) : ViewModel() {

    val settings: StateFlow<AppSettings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    private val _currentTurn = MutableStateFlow<Turn?>(null)
    val currentTurn: StateFlow<Turn?> = _currentTurn.asStateFlow()

    // Transient message shown briefly on screen (e.g. "Red is out!" in Reducing mode).
    private val _modeMessage = MutableStateFlow<String?>(null)
    val modeMessage: StateFlow<String?> = _modeMessage.asStateFlow()

    // Movement history, newest first. Index 0 is the current move; the UI shows
    // the rest as the non-scrollable "Recent Moves" list.
    private var turnCounter = 0
    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    private val _activeMode = MutableStateFlow(PlayMode.MANUAL)
    val activeMode: StateFlow<PlayMode> = _activeMode.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerProgress = MutableStateFlow(1f)
    val timerProgress: StateFlow<Float> = _timerProgress.asStateFlow()

    private var timerJob: Job? = null
    private var toneGenerator: ToneGenerator? = null

    val speechState: StateFlow<SpeechState> = speechRecognizerManager.state
    private val _showMicrophoneRationale = MutableStateFlow(false)
    val showMicrophoneRationale: StateFlow<Boolean> = _showMicrophoneRationale.asStateFlow()

    // Active game-mode engine; rebuilt (and reset) whenever the mode or its
    // relevant settings change.
    private var gameMode: GameMode = GameModeFactory.create(AppSettings())
    private var gameModeSignature: String? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: RuntimeException) {
            // ToneGenerator throws when audio hardware is busy; degrade gracefully
            // (sound effects simply won't play) but record it for diagnostics.
            Log.w(TAG, "ToneGenerator unavailable; sound effects disabled", e)
        }

        // Rebuild the game-mode engine when its inputs change (mode, fixed color,
        // loop flag, or the global palette that several modes draw from).
        viewModelScope.launch {
            settings.collect { s ->
                val signature = listOf(
                    s.gameMode,
                    s.oneColorColor,
                    s.reducingLoop,
                    s.reducingTurnsPerDrop,
                    s.enabledColors.map { it.name }.sorted().joinToString(",")
                ).joinToString("|")
                if (signature != gameModeSignature) {
                    gameModeSignature = signature
                    gameMode = GameModeFactory.create(s)
                }
            }
        }
    }

    fun setPlayMode(mode: PlayMode) {
        if (_activeMode.value == mode) return
        cleanupActiveMode()
        _activeMode.value = mode
    }

    fun cleanupActiveMode() {
        stopTimer()
        speechRecognizerManager.stopListening()
        _showMicrophoneRationale.value = false
    }

    fun spin() {
        val currentSettings = settings.value
        val turn = gameMode.nextTurn()
        _currentTurn.value = turn

        // Only real moves advance the turn counter / history (challenges don't).
        turn.primaryMove?.let { move ->
            turnCounter++
            _history.value = (listOf(HistoryEntry(move, turnCounter)) + _history.value).take(MAX_HISTORY)
        }

        if (currentSettings.soundEffectsEnabled) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
        }

        val localized = getLocalizedContext(context, currentSettings.language)
        // Surface a transient on-screen message when a color is dropped (Reducing mode).
        _modeMessage.value = turn.droppedColor?.let {
            localized.getString(R.string.mode_color_dropped, localized.getString(it.nameResId))
        }

        announceTurn(turn, currentSettings, localized)
    }

    fun clearModeMessage() {
        _modeMessage.value = null
    }

    private fun announceTurn(turn: Turn, currentSettings: AppSettings, localized: Context) {
        val spoken = when {
            turn.challengeResId != null -> localized.getString(turn.challengeResId)
            turn.moves.isNotEmpty() -> {
                val movesText = turn.moves.joinToString(", ") { move ->
                    val part = localized.getString(move.bodyPart.nameResId)
                    val color = localized.getString(move.color.nameResId)
                    if (turn.verb == TurnVerb.LIFT) {
                        localized.getString(R.string.tts_lift_template, part, color)
                    } else {
                        localized.getString(R.string.tts_announcement_template, part, color)
                    }
                }
                val droppedNote = turn.droppedColor?.let {
                    ". " + localized.getString(R.string.mode_color_dropped, localized.getString(it.nameResId))
                } ?: ""
                movesText + droppedNote
            }
            else -> return
        }

        ttsManager.speak(
            text = spoken,
            language = currentSettings.language,
            speed = currentSettings.ttsSpeed,
            pitch = currentSettings.ttsPitch
        )
    }

    private fun getLocalizedContext(context: Context, language: AppLanguage): Context {
        val locale = when (language) {
            AppLanguage.ENGLISH -> Locale.US
            AppLanguage.SPANISH -> Locale("es", "ES")
            AppLanguage.FRENCH -> Locale.FRANCE
            AppLanguage.PORTUGUESE -> Locale("pt", "PT")
            AppLanguage.HINDI -> Locale("hi", "IN")
            AppLanguage.CHINESE -> Locale.SIMPLIFIED_CHINESE
        }
        // Operate on a copy: the Configuration returned by resources is shared
        // mutable state, and mutating it in place can leak this transient locale
        // into the app's global resource configuration.
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            saveSettingsUseCase(settings.value.copy(language = language))
        }
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        
        val tickIntervalMs = 50L

        timerJob = viewModelScope.launch {
            // Read interval at the start of each countdown cycle so interval
            // changes are picked up without disrupting an in-flight countdown.
            var totalTimeMs = settings.value.timerIntervalSecs * 1000L
            var elapsed = 0L
            var beepedThisCycle = false
            while (_isTimerRunning.value) {
                delay(tickIntervalMs)
                elapsed += tickIntervalMs
                val remainingProgress = 1f - (elapsed.toFloat() / totalTimeMs.toFloat())
                _timerProgress.value = remainingProgress.coerceIn(0f, 1f)

                // Fire the final-second warning tone exactly once per cycle.
                if (settings.value.soundEffectsEnabled && !beepedThisCycle && totalTimeMs - elapsed in 0L..1000L) {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 80)
                    beepedThisCycle = true
                }

                if (elapsed >= totalTimeMs) {
                    spin()
                    elapsed = 0L
                    beepedThisCycle = false
                    totalTimeMs = settings.value.timerIntervalSecs * 1000L
                    _timerProgress.value = 1f
                }
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun stopTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
        _timerProgress.value = 1f
    }

    fun startVoiceListening() {
        val word = settings.value.triggerWord
        val lang = settings.value.language
        speechRecognizerManager.startListening(word, lang) {
            viewModelScope.launch {
                spin()
            }
        }
    }

    fun stopVoiceListening() {
        speechRecognizerManager.stopListening()
    }

    fun setMicrophoneRationaleVisible(visible: Boolean) {
        _showMicrophoneRationale.value = visible
    }

    override fun onCleared() {
        super.onCleared()
        cleanupActiveMode()
        // TtsManager is an app-scoped singleton; only stop the current utterance.
        // Destroying it here would leave a dead engine for any recreated ViewModel.
        ttsManager.stop()
        toneGenerator?.release()
    }

    companion object {
        private const val TAG = "MainViewModel"
        private const val MAX_HISTORY = 30
    }
}
