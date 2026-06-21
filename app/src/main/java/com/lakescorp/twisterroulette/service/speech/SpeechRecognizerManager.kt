package com.lakescorp.twisterroulette.service.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.lakescorp.twisterroulette.domain.model.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class SpeechState {
    IDLE,
    LISTENING,
    TRIGGER_DETECTED,
    UNSUPPORTED,
    ERROR
}

@Singleton
class SpeechRecognizerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainScope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(SpeechState.IDLE)
    val state: StateFlow<SpeechState> = _state.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var isListeningActive = false
    private var currentTriggerWord = "Twister"
    private var currentLanguage = AppLanguage.ENGLISH
    private var onTriggerDetectedCallback: (() -> Unit)? = null

    // Backoff state for transient recognizer errors.
    private var retryAttempt = 0
    private val maxRetries = 5

    // Flag to prefer offline recognition, falls back to online if unavailable
    private var preferOffline = true

    // Guards against onPartialResults + onResults both firing the trigger in the same cycle.
    private var triggerFiredThisCycle = false

    init {
        checkAvailability()
    }

    private fun checkAvailability() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = SpeechState.UNSUPPORTED
        }
    }

    fun startListening(
        triggerWord: String,
        language: AppLanguage,
        onTriggerDetected: () -> Unit
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = SpeechState.UNSUPPORTED
            return
        }

        currentTriggerWord = triggerWord
        currentLanguage = language
        onTriggerDetectedCallback = onTriggerDetected
        isListeningActive = true
        retryAttempt = 0
        preferOffline = true

        mainScope.launch {
            initAndListen()
        }
    }

    private fun initAndListen() {
        if (!isListeningActive) return

        triggerFiredThisCycle = false

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }

            val locale = when (currentLanguage) {
                AppLanguage.ENGLISH -> Locale.US
                AppLanguage.SPANISH -> Locale("es", "ES")
                AppLanguage.FRENCH -> Locale.FRANCE
                AppLanguage.PORTUGUESE -> Locale("pt", "PT")
                AppLanguage.HINDI -> Locale("hi", "IN")
                AppLanguage.CHINESE -> Locale.SIMPLIFIED_CHINESE
            }

            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                if (preferOffline) {
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                }
            }

            speechRecognizer?.startListening(recognizerIntent)
            _state.value = SpeechState.LISTENING
            _errorMessage.value = null
            Log.d(TAG, "initAndListen: started listening for '$currentTriggerWord' in $currentLanguage")
        } catch (e: Exception) {
            Log.e(TAG, "initAndListen: exception starting recognizer", e)
            _state.value = SpeechState.ERROR
            _errorMessage.value = e.localizedMessage
            retryListening()
        }
    }

    fun stopListening() {
        isListeningActive = false
        mainScope.launch {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            _state.value = SpeechState.IDLE
            Log.d(TAG, "stopListening: stopped")
        }
    }

    private fun retryListening(delayMs: Long = 1000) {
        if (!isListeningActive) return
        Log.d(TAG, "retryListening: retrying in ${delayMs}ms (attempt $retryAttempt)")
        mainScope.launch {
            kotlinx.coroutines.delay(delayMs)
            initAndListen()
        }
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            retryAttempt = 0
            _state.value = SpeechState.LISTENING
            Log.d(TAG, "onReadyForSpeech")
        }

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech: isListeningActive=$isListeningActive triggerFired=$triggerFiredThisCycle")
        }

        override fun onError(error: Int) {
            Log.d(TAG, "onError: code=$error isListeningActive=$isListeningActive")
            if (!isListeningActive) {
                _state.value = SpeechState.IDLE
                return
            }
            when (error) {
                // Permanent failures: do not loop — surface a terminal state.
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS,
                SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> {
                    isListeningActive = false
                    _state.value = SpeechState.UNSUPPORTED
                    Log.w(TAG, "onError: permanent failure, code=$error")
                }
                SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> {
                    if (preferOffline) {
                        Log.d(TAG, "onError: Language unavailable offline. Retrying with online recognition.")
                        preferOffline = false
                        retryListening(delayMs = 300)
                    } else {
                        isListeningActive = false
                        _state.value = SpeechState.UNSUPPORTED
                        Log.w(TAG, "onError: permanent language unavailable failure, code=$error")
                    }
                }
                // Normal in continuous listening: retry promptly with no penalty.
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    retryAttempt = 0
                    retryListening(delayMs = 300)
                }
                // Transient (busy/audio/server/client/network): capped exponential backoff.
                else -> {
                    if (retryAttempt >= maxRetries) {
                        isListeningActive = false
                        _state.value = SpeechState.ERROR
                        Log.e(TAG, "onError: max retries reached, code=$error")
                    } else {
                        val backoff = (500L shl retryAttempt).coerceAtMost(8000L)
                        retryAttempt++
                        retryListening(delayMs = backoff)
                    }
                }
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            Log.d(TAG, "onResults: matches=$matches triggerFired=$triggerFiredThisCycle")
            if (matches != null) {
                checkMatches(matches)
            }
            if (isListeningActive) {
                val delay = if (triggerFiredThisCycle) 1500L else 300L
                retryListening(delayMs = delay)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (matches != null) {
                checkMatches(matches)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun checkMatches(matches: ArrayList<String>) {
        if (triggerFiredThisCycle) return
        val trigger = currentTriggerWord.lowercase().trim()
        for (match in matches) {
            if (match.lowercase().contains(trigger)) {
                triggerFiredThisCycle = true
                _state.value = SpeechState.TRIGGER_DETECTED
                Log.d(TAG, "checkMatches: trigger detected in '$match'")
                onTriggerDetectedCallback?.invoke()
                break
            }
        }
    }

    companion object {
        private const val TAG = "SpeechRecognizerMgr"
    }
}
