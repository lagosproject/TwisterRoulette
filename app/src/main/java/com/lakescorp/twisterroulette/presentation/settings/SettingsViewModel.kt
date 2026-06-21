package com.lakescorp.twisterroulette.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lakescorp.twisterroulette.domain.model.AppLanguage
import com.lakescorp.twisterroulette.domain.model.AppSettings
import com.lakescorp.twisterroulette.domain.model.AppTheme
import com.lakescorp.twisterroulette.domain.model.ColorSet
import com.lakescorp.twisterroulette.domain.model.TtsPitch
import com.lakescorp.twisterroulette.domain.model.TtsSpeed
import com.lakescorp.twisterroulette.domain.model.TwisterColor
import com.lakescorp.twisterroulette.domain.usecase.GetSettingsUseCase
import com.lakescorp.twisterroulette.domain.usecase.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase
) : ViewModel() {

    val settings: StateFlow<AppSettings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun updateLanguage(language: AppLanguage) {
        updateSettings { it.copy(language = language) }
    }

    fun updateColorSet(colorSet: ColorSet) {
        updateSettings {
            it.copy(
                colorSet = colorSet,
                enabledColors = colorSet.getDefaultColors()
            )
        }
    }

    fun toggleColor(color: TwisterColor): Boolean {
        val currentEnabled = settings.value.enabledColors
        val newEnabled = if (currentEnabled.contains(color)) {
            if (currentEnabled.size <= 2) {
                return false
            }
            currentEnabled - color
        } else {
            currentEnabled + color
        }
        
        updateSettings { 
            it.copy(
                colorSet = ColorSet.CUSTOM,
                enabledColors = newEnabled
            )
        }
        return true
    }

    fun updateTimerInterval(secs: Int) {
        updateSettings { it.copy(timerIntervalSecs = secs) }
    }

    fun updateTriggerWord(word: String) {
        if (word.isNotBlank()) {
            updateSettings { it.copy(triggerWord = word.trim()) }
        }
    }

    fun updateTtsSpeed(speed: TtsSpeed) {
        updateSettings { it.copy(ttsSpeed = speed) }
    }

    fun updateTtsPitch(pitch: TtsPitch) {
        updateSettings { it.copy(ttsPitch = pitch) }
    }

    fun updateSoundEffects(enabled: Boolean) {
        updateSettings { it.copy(soundEffectsEnabled = enabled) }
    }

    fun updateTheme(theme: AppTheme) {
        updateSettings { it.copy(theme = theme) }
    }

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            val updated = transform(settings.value)
            saveSettingsUseCase(updated)
        }
    }
}
