# Contributing to Twister Roulette

First off, thank you for taking the time to contribute! Contributions from the community help make this project better for everyone.

---

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

---

## How Can I Contribute?

### 1. Reporting Bugs
- Search existing issues to see if the bug has already been reported.
- If not, open a new issue using the **Bug Report** template. Provide detailed steps to reproduce, device specifications, and any relevant logs or screenshots.

### 2. Suggesting Enhancements
- Open a new issue using the **Feature Request** template.
- Explain the user benefit, how the feature should behave, and any potential UI layouts.

### 3. Submitting Code Changes
We follow a standard Git-Flow-like branching strategy:

1. **Fork the repository** on GitHub.
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/your-username/TwisterRoulette.git
   ```
3. **Create a branch** for your work. Use a descriptive prefix:
   - For new features: `feature/your-feature-name`
   - For bug fixes: `bugfix/issue-number-description`
   - For documentation updates: `docs/short-description`
4. **Develop and test** your changes (see the testing guidelines below).
5. **Commit your changes**. Write clear, concise commit messages. Keep them imperative (e.g., "Add Spanish voice announcements").
6. **Push to your fork** and **submit a Pull Request (PR)** to our `main` branch.

---

## Development & Testing Guidelines

### Coding Style
- Follow the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Keep composables modular, clean, and verify they work in both Light and Dark themes.

### Running Tests Locally
Before submitting a PR, ensure all tests compile and pass successfully on your local machine:

1. **Unit & Instrumentation Tests**:
   ```bash
   ./gradlew test
   ```
2. **Snapshot Tests (Paparazzi)**:
   We use Paparazzi for UI regression and snapshot testing.
   - To record/update snapshots (if you changed UI components):
     ```bash
     ./gradlew recordPaparazzi
     ```
   - To verify snapshots against reference images:
     ```bash
     ./gradlew verifyPaparazzi
     ```

---

## Pull Request Checklist

Before submitting your PR, please verify:
- [ ] Your code compiles without errors or warnings.
- [ ] All local tests (`./gradlew test verifyPaparazzi`) pass successfully.
- [ ] You have check-run for any leaked API keys, tokens, or local credentials.
- [ ] You have updated the documentation (`README.md`, inline comments) if applicable.
- [ ] The PR template is fully filled out.
