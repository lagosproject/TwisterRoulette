# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-06-18

### Added
- Initial project structure with Hilt dependency injection, Jetpack Compose, and DataStore preferences.
- 3 Play Modes: Manual, Timer (with circular countdown progress bar), and Voice Mode.
- On-device continuous offline Speech Recognition using Google Speech engine.
- On-device Text-to-Speech (TTS) integration with locale switcher supporting English, Spanish, and French.
- Color customization system (Classic, Extended, Dark, and Custom configurations).
- Dark-first electric violet and neon blue user interface design matching the Android Material 3 guidelines.
- Repository health templates including security rules, issue templates, PR checklists, and CI/CD pipelines.

### Changed
- Refactored language selection controls to prevent text wrapping on long French/Spanish translations.

### Fixed
- Fixed segmented play-mode selector label wrapping in Spanish translation.
