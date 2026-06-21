package com.lakescorp.twisterroulette.domain.model

enum class AppLanguage {
    ENGLISH, SPANISH, FRENCH, PORTUGUESE, HINDI, CHINESE
}

enum class TtsSpeed {
    SLOW, NORMAL, FAST;
    
    fun toSpeechRate(): Float = when (this) {
        SLOW -> 0.7f
        NORMAL -> 1.0f
        FAST -> 1.4f
    }
}

enum class TtsPitch {
    LOW, NORMAL, HIGH;
    
    fun toPitchValue(): Float = when (this) {
        LOW -> 0.75f
        NORMAL -> 1.0f
        HIGH -> 1.3f
    }
}

enum class AppTheme {
    DARK, LIGHT, SYSTEM
}

data class AppSettings(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val colorSet: ColorSet = ColorSet.CLASSIC,
    val enabledColors: Set<TwisterColor> = ColorSet.CLASSIC.getDefaultColors(),
    val timerIntervalSecs: Int = 10,
    val triggerWord: String = "Twister",
    val ttsSpeed: TtsSpeed = TtsSpeed.NORMAL,
    val ttsPitch: TtsPitch = TtsPitch.NORMAL,
    val soundEffectsEnabled: Boolean = true,
    val theme: AppTheme = AppTheme.SYSTEM,
    val gameMode: GameModeType = GameModeType.CLASSIC,
    val oneColorColor: TwisterColor = TwisterColor.RED,
    val reducingLoop: Boolean = false,
    val reducingTurnsPerDrop: Int = 1,
    val reducingMinColors: Int = 2,
    val sequenceLength: Int = 2
)
