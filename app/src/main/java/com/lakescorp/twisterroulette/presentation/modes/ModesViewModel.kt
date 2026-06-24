package com.lakescorp.twisterroulette.presentation.modes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lakescorp.twisterroulette.domain.model.AppSettings
import com.lakescorp.twisterroulette.domain.model.ChallengeFrequency
import com.lakescorp.twisterroulette.domain.model.GameModeType
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
class ModesViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase
) : ViewModel() {

    val settings: StateFlow<AppSettings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun selectMode(mode: GameModeType) = update { it.copy(gameMode = mode) }

    fun setOneColor(color: TwisterColor) = update { it.copy(oneColorColor = color) }

    fun setReducingLoop(enabled: Boolean) = update { it.copy(reducingLoop = enabled) }

    fun setReducingTurnsPerDrop(turns: Int) = update { it.copy(reducingTurnsPerDrop = turns) }

    fun setReducingMinColors(minColors: Int) = update { it.copy(reducingMinColors = minColors) }

    fun setSequenceLength(length: Int) = update { it.copy(sequenceLength = length) }

    fun setChallengeFrequency(frequency: ChallengeFrequency) = update { it.copy(challengeFrequency = frequency) }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { saveSettingsUseCase(transform(settings.value)) }
    }
}
