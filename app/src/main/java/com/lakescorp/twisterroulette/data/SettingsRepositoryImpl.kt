package com.lakescorp.twisterroulette.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lakescorp.twisterroulette.domain.model.AppLanguage
import com.lakescorp.twisterroulette.domain.model.AppSettings
import com.lakescorp.twisterroulette.domain.model.AppTheme
import com.lakescorp.twisterroulette.domain.model.ChallengeFrequency
import com.lakescorp.twisterroulette.domain.model.ColorSet
import com.lakescorp.twisterroulette.domain.model.GameModeType
import com.lakescorp.twisterroulette.domain.model.TtsPitch
import com.lakescorp.twisterroulette.domain.model.TtsSpeed
import com.lakescorp.twisterroulette.domain.model.TwisterColor
import com.lakescorp.twisterroulette.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "twister_settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val LANGUAGE = stringPreferencesKey("language")
        val COLOR_SET = stringPreferencesKey("color_set")
        val ENABLED_COLORS = stringPreferencesKey("enabled_colors")
        val TIMER_INTERVAL_SECS = intPreferencesKey("timer_interval_secs")
        val TRIGGER_WORD = stringPreferencesKey("trigger_word")
        val TTS_SPEED = stringPreferencesKey("tts_speed")
        val TTS_PITCH = stringPreferencesKey("tts_pitch")
        val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
        val THEME = stringPreferencesKey("theme")
        val GAME_MODE = stringPreferencesKey("game_mode")
        val ONE_COLOR_COLOR = stringPreferencesKey("one_color_color")
        val REDUCING_LOOP = booleanPreferencesKey("reducing_loop")
        val REDUCING_TURNS_PER_DROP = intPreferencesKey("reducing_turns_per_drop")
        val REDUCING_MIN_COLORS = intPreferencesKey("reducing_min_colors")
        val SEQUENCE_LENGTH = intPreferencesKey("sequence_length")
        val CHALLENGE_FREQUENCY = stringPreferencesKey("challenge_frequency")
    }

    override fun getSettings(): Flow<AppSettings> {
        return context.dataStore.data.map { preferences ->
            val languageStr = preferences[PreferencesKeys.LANGUAGE]
            val language = try {
                if (languageStr != null) AppLanguage.valueOf(languageStr) else AppLanguage.ENGLISH
            } catch (e: IllegalArgumentException) {
                AppLanguage.ENGLISH
            }

            val colorSetStr = preferences[PreferencesKeys.COLOR_SET]
            val colorSet = try {
                if (colorSetStr != null) ColorSet.valueOf(colorSetStr) else ColorSet.CLASSIC
            } catch (e: IllegalArgumentException) {
                ColorSet.CLASSIC
            }

            val enabledColorsStr = preferences[PreferencesKeys.ENABLED_COLORS]
            val enabledColors = if (enabledColorsStr != null) {
                enabledColorsStr.split(",")
                    .filter { it.isNotEmpty() }
                    .mapNotNull {
                        try {
                            TwisterColor.valueOf(it)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }
                    .toSet()
            } else {
                colorSet.getDefaultColors()
            }

            val timerIntervalSecs = preferences[PreferencesKeys.TIMER_INTERVAL_SECS] ?: 10
            val triggerWord = preferences[PreferencesKeys.TRIGGER_WORD] ?: "Twister"

            val ttsSpeedStr = preferences[PreferencesKeys.TTS_SPEED]
            val ttsSpeed = try {
                if (ttsSpeedStr != null) TtsSpeed.valueOf(ttsSpeedStr) else TtsSpeed.NORMAL
            } catch (e: IllegalArgumentException) {
                TtsSpeed.NORMAL
            }

            val ttsPitchStr = preferences[PreferencesKeys.TTS_PITCH]
            val ttsPitch = try {
                if (ttsPitchStr != null) TtsPitch.valueOf(ttsPitchStr) else TtsPitch.NORMAL
            } catch (e: IllegalArgumentException) {
                TtsPitch.NORMAL
            }

            val soundEffectsEnabled = preferences[PreferencesKeys.SOUND_EFFECTS_ENABLED] ?: true

            val themeStr = preferences[PreferencesKeys.THEME]
            val theme = try {
                if (themeStr != null) AppTheme.valueOf(themeStr) else AppTheme.SYSTEM
            } catch (e: IllegalArgumentException) {
                AppTheme.SYSTEM
            }

            val gameModeStr = preferences[PreferencesKeys.GAME_MODE]
            val gameMode = try {
                if (gameModeStr != null) GameModeType.valueOf(gameModeStr) else GameModeType.CLASSIC
            } catch (e: IllegalArgumentException) {
                GameModeType.CLASSIC
            }

            val oneColorStr = preferences[PreferencesKeys.ONE_COLOR_COLOR]
            val oneColorColor = try {
                if (oneColorStr != null) TwisterColor.valueOf(oneColorStr) else TwisterColor.RED
            } catch (e: IllegalArgumentException) {
                TwisterColor.RED
            }

            val reducingLoop = preferences[PreferencesKeys.REDUCING_LOOP] ?: false
            val reducingTurnsPerDrop = preferences[PreferencesKeys.REDUCING_TURNS_PER_DROP] ?: 1
            val reducingMinColors = preferences[PreferencesKeys.REDUCING_MIN_COLORS] ?: 2
            val sequenceLength = preferences[PreferencesKeys.SEQUENCE_LENGTH] ?: 2

            val challengeFrequencyStr = preferences[PreferencesKeys.CHALLENGE_FREQUENCY]
            val challengeFrequency = try {
                if (challengeFrequencyStr != null) ChallengeFrequency.valueOf(challengeFrequencyStr) else ChallengeFrequency.INFREQUENT
            } catch (e: IllegalArgumentException) {
                ChallengeFrequency.INFREQUENT
            }

            AppSettings(
                language = language,
                colorSet = colorSet,
                enabledColors = enabledColors,
                timerIntervalSecs = timerIntervalSecs,
                triggerWord = triggerWord,
                ttsSpeed = ttsSpeed,
                ttsPitch = ttsPitch,
                soundEffectsEnabled = soundEffectsEnabled,
                theme = theme,
                gameMode = gameMode,
                oneColorColor = oneColorColor,
                reducingLoop = reducingLoop,
                reducingTurnsPerDrop = reducingTurnsPerDrop,
                reducingMinColors = reducingMinColors,
                sequenceLength = sequenceLength,
                challengeFrequency = challengeFrequency
            )
        }
    }

    override suspend fun saveSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = settings.language.name
            preferences[PreferencesKeys.COLOR_SET] = settings.colorSet.name
            preferences[PreferencesKeys.ENABLED_COLORS] = settings.enabledColors.joinToString(",") { it.name }
            preferences[PreferencesKeys.TIMER_INTERVAL_SECS] = settings.timerIntervalSecs
            preferences[PreferencesKeys.TRIGGER_WORD] = settings.triggerWord
            preferences[PreferencesKeys.TTS_SPEED] = settings.ttsSpeed.name
            preferences[PreferencesKeys.TTS_PITCH] = settings.ttsPitch.name
            preferences[PreferencesKeys.SOUND_EFFECTS_ENABLED] = settings.soundEffectsEnabled
            preferences[PreferencesKeys.THEME] = settings.theme.name
            preferences[PreferencesKeys.GAME_MODE] = settings.gameMode.name
            preferences[PreferencesKeys.ONE_COLOR_COLOR] = settings.oneColorColor.name
            preferences[PreferencesKeys.REDUCING_LOOP] = settings.reducingLoop
            preferences[PreferencesKeys.REDUCING_TURNS_PER_DROP] = settings.reducingTurnsPerDrop
            preferences[PreferencesKeys.REDUCING_MIN_COLORS] = settings.reducingMinColors
            preferences[PreferencesKeys.SEQUENCE_LENGTH] = settings.sequenceLength
            preferences[PreferencesKeys.CHALLENGE_FREQUENCY] = settings.challengeFrequency.name
        }
    }
}
