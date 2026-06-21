# Twister Roulette Android App

[![CI Build](https://github.com/your-username/TwisterRoulette/actions/workflows/ci.yml/badge.svg)](https://github.com/your-username/TwisterRoulette/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg)](https://kotlinlang.org/)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20%28API%2026%2B%29-blue.svg)](https://developer.android.com)

Twister Roulette is a native Android application designed to replace the classic physical Twister roulette spinner. It randomly spins a body part and a color, announces the result in real-time, and operates fully offline. Built with Jetpack Compose, it features a premium, dark-first electric neon aesthetic optimized for single-handed use during active party games.

---

## Table of Contents

- [About the Project](#about-the-project)
- [Features](#features)
- [Tech Stack & Architecture](#tech-stack--architecture)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Prerequisites & Installation](#prerequisites--installation)
  - [Building from Android Studio](#building-from-android-studio)
  - [Building from Command Line (CLI)](#building-from-command-line-cli)
- [Usage & Examples](#usage--examples)
- [Troubleshooting Voice Mode](#troubleshooting-voice-mode)
- [Contributing](#contributing)
- [License](#license)

---

## About the Project

Traditional physical spinner wheels can get lost, damaged, or become awkward to spin while playing. **Twister Roulette** solves this by providing a highly legible, loud, and automatic solution. Players can place their phone across the room and let it announce commands, or speak to the app directly using continuous voice commands.

---

## Features

1. **Roulette Logic:** Selects a body part (`Left Hand`, `Right Hand`, `Left Foot`, `Right Foot`) and a color (Classic, Extended, Dark Edition, or Custom Sets) with clean scale+fade card animations.
2. **Offline Text-To-Speech (TTS):** Announces the results out loud in the selected app language (`English`, `Spanish`, `French`), independent of the device's system locale.
3. **Play Modes:**
   - **Manual Mode:** Spin on demand via the floating action button.
   - **Timer Mode:** Periodically spins and announces results at a selected interval (5s to 60s) with a circular countdown ring.
   - **Voice Mode:** Continuously listens for the trigger word `"Twister"` (or custom word) on-device using offline Google Speech Recognition.
4. **Color Customization System:** Allows toggling specific active colors, validating that at least 2 colors remain enabled.
5. **Modern Dynamic Design:** Styled with a premium Material3 dark-first electric violet/neon blue palette, drop shadows, and visual feedback micro-animations.

---

## Tech Stack & Architecture

- **Language:** Kotlin (100% Native)
- **Minimum SDK:** API 26 (Android 8.0)
- **Target SDK:** API 35 (Android 14/15)
- **Architecture:** Clean Architecture + MVVM (Model-View-ViewModel)
- **DI Framework:** Hilt
- **UI Toolkit:** Jetpack Compose with Material3
- **Local Persistence:** DataStore Preferences
- **TTS Engine:** On-device Android `TextToSpeech`
- **Speech Engine:** On-device Android `SpeechRecognizer` (`EXTRA_PREFER_OFFLINE = true`)
- **UI Testing & Snapshots:** Paparazzi

---

## Project Structure

```
app/
├── data/
│   └── SettingsRepositoryImpl.kt  # Reads/Writes AppSettings via DataStore
├── domain/
│   ├── model/                     # BodyPart, TwisterColor, ColorSet, AppSettings
│   ├── repository/                # SettingsRepository interface
│   └── usecase/                   # SpinUseCase, GetSettingsUseCase, SaveSettingsUseCase
├── service/
│   ├── tts/TtsManager.kt          # Handles Locale, Pitch, Audio Focus ducking, and speak commands
│   └── speech/SpeechRecognizer.kt # Continuous offline trigger word voice listening
├── presentation/
│   ├── main/                      # MainViewModel and MainScreen dashboard views
│   ├── settings/                  # SettingsViewModel and SettingsScreen configuration views
│   └── theme/                     # Color, Typography, and Theme system
├── TwisterApplication.kt          # Hilt Application entry point
└── di/AppModule.kt                # Hilt singleton providers
```

---

## Configuration

The application uses an external directory to load release keystore credentials safely outside the code repository.

1. Copy `.env.example` to create your local `.env` file (if you have local env setups).
2. For signing release APKs locally or on CI, configure the path to your signing keystore by setting the environment variable:
   ```bash
   export TWISTER_KEYSTORE_DIR="/path/to/your/keystores"
   ```
3. Inside this directory, ensure you have:
   - `upload-keystore.jks` (Your Keystore file)
   - `upload-credentials.txt` (A Java properties file containing the `alias`, `password`, and `passwordAlias`)

---

## Prerequisites & Installation

### System Requirements
- **Java JDK:** Version 21
- **Android SDK:** API 35 Platform Tools installed

### Building from Android Studio

1. Open Android Studio.
2. Select **File > Open** and choose the `TwisterRoulette` root directory.
3. Allow Gradle to sync and compile the project dependencies.
4. Select a connected physical device or emulator (API 26+).
5. Click **Run** (`Shift + F10`) to deploy.

### Building from Command Line (CLI)

Use the Gradle wrapper to build the debug APK:

```bash
# Set execute permissions for gradlew
chmod +x gradlew

# Run debug build compilation
./gradlew assembleDebug
```

The compiled package will be available at: `app/build/outputs/apk/debug/app-debug.apk`.

---

## Usage & Examples

### Triggering a Spin (Kotlin Use Case Example)
The `SpinUseCase` randomly selects an active color and body part based on user settings:

```kotlin
class SpinUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): SpinResult {
        val settings = repository.getSettings().first()
        val activeColors = settings.activeColors
        val selectedColor = activeColors.random()
        val selectedBodyPart = BodyPart.values().random()
        
        return SpinResult(
            color = selectedColor,
            bodyPart = selectedBodyPart
        )
    }
}
```

### Voice Mode Listener State Flow
To start/stop listening based on UI interactions, the continuous speech service toggles the listening state and publishes events:

```kotlin
// Start voice command listening
speechManager.startListening(
    triggerWord = "Twister",
    onResult = { result ->
        if (result.containsTrigger) {
            viewModel.spin()
        }
    },
    onError = { error ->
        viewModel.handleVoiceError(error)
    }
)
```

---

## Troubleshooting Voice Mode

Voice mode depends on the device's default offline voice recognition software (such as Google Speech Services).

1. Ensure the **RECORD_AUDIO** permission is granted.
2. Keep the device's default offline language packages up-to-date (via **Google App > Settings > Voice > Offline speech recognition**).
3. If voice recognition is completely unavailable on your emulator, check that the Google Play Services Speech engine is installed.

---

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, development workflow, and process for submitting pull requests.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
