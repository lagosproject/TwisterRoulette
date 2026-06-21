# Play Store Showcase Image Automation

Automated pipeline to generate premium, high-fidelity App Store / Play Store showcase cards and feature graphics for **Twister Roulette**.

## Design Philosophy
Following the app's **Kinetic Neon Theme**, the pipeline renders:
- **Colors**: Deep obsidian (`#0F0F0F`) backgrounds, with electric violet (`#7C2EFF`) and neon cyan (`#2EFFFF`) typography, borders, and overlays.
- **Card Framing**: Rounded corners (matching standard device displays) framed with electric-violet borders and soft drop shadows.
- **Independence**: The script requires no active emulator or device connection. If raw screenshots are missing under `raw/`, it dynamically draws pixel-perfect mockup screens using custom vector drawing routines for all icons (Check, Mic, Dice, settings cog, play/pause switches) and hand/foot gameplay symbols.
- **Localization Resilience**: Custom pixel flag renderers for the language selector (UK 🇬🇧, Spain 🇪🇸, France 🇫🇷) ensure precise lookups and clean rendering on any headless server without requiring colored emoji font support.

---

## Folder Structure

```
screenshots_automation/
├── generate_showcase.py    # Main generation engine
├── .gitignore             # Ignores intermediate raw screenshots and final outputs
├── README.md              # Documentation
├── raw/                   # [Auto-generated / Optional Input] Raw screen captures
│   ├── phone/
│   ├── tablet_7/
│   └── tablet_10/
└── output/                # Final Play Store-ready assets
    ├── phone/             # 1080×1920 Phone showcase cards
    │   ├── en-US/         # English
    │   ├── es-ES/         # Spanish
    │   └── fr-FR/         # French
    ├── tablet_7/          # 1080×1920 7-inch tablet showcase cards (text top, device bottom)
    ├── tablet_10/         # 1080×1920 10-inch tablet showcase cards (text top, device bottom)
    ├── feature_graphic.png # English 1024×500 Banner Graphic
    ├── feature_graphic_es-ES.png
    └── feature_graphic_fr-FR.png
```

---

## Locales & Screens Supported

### 3 Supported Locales
- 🇬🇧 **English (`en-US`)**
- 🇪🇸 **Spanish (`es-ES`)**
- 🇫🇷 **French (`fr-FR`)**

### 3 Showcased Screens
1. **Play Tab (Manual / Timer)**: Showcases the circular neon countdown circle, large body-part/color challenge card (e.g. Left Foot on Red), mode-selector segmented buttons, movement history lists, and the floating spin button.
2. **Game Modes Tab**: Details options (Classic, One Color, Reducing, Challenge, Sequence), focusing on the active *Reducing* card with expanded turns-per-drop slider, loop switch, and neon outline.
3. **Settings Tab**: Demonstrates control options including language selection flags, custom color set selectors (Classic/Extended/Dark/Custom), individual color chip options, trigger-word input, TTS sliders, and sound effects switches.

---

## How to Run

1. Ensure `Pillow` is installed:
   ```bash
   pip install pillow
   ```
2. Run the generator script:
   ```bash
   python3 screenshots_automation/generate_showcase.py
   ```

*(Optional)*: If you want to use real device screenshots, simply paste raw PNGs into `raw/<form_factor>/<locale>/<screen_type>.png` (e.g., `play.png`, `modes.png`, `settings.png`) and re-run the script. It will automatically detect them, crop the system status/navigation bars, and compile them into showcase templates.
