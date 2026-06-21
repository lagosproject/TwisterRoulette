package com.lakescorp.twisterroulette.service.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.lakescorp.twisterroulette.domain.model.AppLanguage
import com.lakescorp.twisterroulette.domain.model.TtsPitch
import com.lakescorp.twisterroulette.domain.model.TtsSpeed
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        initializeTts()
    }

    private fun initializeTts() {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { ttsInstance ->
                val result = ttsInstance.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    _isReady.value = false
                    _error.value = "Default language US not supported"
                } else {
                    ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}

                        override fun onDone(utteranceId: String?) {
                            abandonFocus()
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            abandonFocus()
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            abandonFocus()
                        }

                        override fun onStop(utteranceId: String?, interrupted: Boolean) {
                            abandonFocus()
                        }
                    })
                    _isReady.value = true
                    _error.value = null
                }
            }
        } else {
            _isReady.value = false
            _error.value = "Failed to initialize TTS engine"
        }
    }

    fun speak(
        text: String,
        language: AppLanguage,
        speed: TtsSpeed,
        pitch: TtsPitch
    ) {
        // Self-heal: if the engine was previously released, re-initialize it.
        // It becomes usable asynchronously via onInit(); this call is dropped.
        val ttsInstance = tts ?: run {
            initializeTts()
            return
        }
        if (!_isReady.value) {
            _error.value = "TTS not ready"
            return
        }

        val locale = when (language) {
            AppLanguage.ENGLISH -> Locale.US
            AppLanguage.SPANISH -> Locale("es", "ES")
            AppLanguage.FRENCH -> Locale.FRANCE
            AppLanguage.PORTUGUESE -> Locale("pt", "PT")
            AppLanguage.HINDI -> Locale("hi", "IN")
            AppLanguage.CHINESE -> Locale.SIMPLIFIED_CHINESE
        }

        ttsInstance.language = locale
        ttsInstance.setSpeechRate(speed.toSpeechRate())
        ttsInstance.setPitch(pitch.toPitchValue())

        requestFocus()
        val params = android.os.Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "twister_spin")
        }
        ttsInstance.speak(text, TextToSpeech.QUEUE_FLUSH, params, "twister_spin")
    }

    @Suppress("DEPRECATION")
    private fun requestFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener {}
                .build()
            
            focusRequest?.let {
                audioManager.requestAudioFocus(it)
            }
        } else {
            audioManager.requestAudioFocus(
                {},
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
        } else {
            audioManager.abandonAudioFocus(null)
        }
    }

    /**
     * Stops any in-progress utterance and releases audio focus WITHOUT destroying
     * the engine. Safe to call from a screen/ViewModel teardown, since this is an
     * application-scoped singleton that must survive across ViewModel recreation.
     */
    fun stop() {
        tts?.stop()
        abandonFocus()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isReady.value = false
    }
}
