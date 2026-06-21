# Stitch Reference — Twister Roulette

Design references for the app live in **Google Stitch** (accessed via the Stitch MCP server:
`list_projects`, `list_screens`, `get_screen`, `edit_screens`, `generate_variants`,
`update_design_system`, …). A fresh chat can re-list everything live with those tools.

## Project
- **Title:** `Neon Twister Spinner`
- **Resource name / ID:** `projects/6036994918970854459`
- **Device type:** Mobile (390×... frames)

## Design System — "Kinetic Neon"
- **Primary (Electric Violet):** `#7C2EFF`
- **Secondary (Neon Cyan):** `#2EFFFF`
- **Tertiary:** `#B42EFF`
- **Base surface (near-black):** `#0F0F0F`
- **Charcoal "glass" container:** `#1E1E1E`
- **Headline font:** Plus Jakarta Sans (ExtraBold/Black for game-state headlines)
- **Body / label font:** Inter
- **Shape:** large radii — 24dp standard, 32dp for primary game cards; pill buttons
- **Style:** dark-first, glassmorphism + "neon glow" colored shadows
- **Game color palette (inviolable, max saturation):**
  Red `#FF2E2E`, Blue `#2E8BFF`, Yellow `#FFD02E`, Green `#2EFF5C`,
  Purple `#B42EFF`, Orange `#FF7E2E`, Black `#1E1E1E`

> Note: these tokens are mirrored in the app at
> `app/src/main/java/com/lakescorp/twisterroulette/presentation/theme/Color.kt`.

## Screens (IDs under `projects/6036994918970854459/screens/`)
| Screen | ID |
|--------|----|
| App Icon | `6c0a65e89bd44d99834bdf618ba11743` |
| Main — Voice Mode | `685606c10a844a699c272bdfc7995f22` |
| Main — Voice Mode (V2, revised) | `ec1899c773374f6aa98435483e7e4f2f` |
| Main — Timer Mode | `ed8b6cf4af104e8fa536cc762fa491e1` |
| Main — Timer Mode (V2, revised) | `b9a12b4d1a9d4fa6bf917cec836f878f` |
| Main — Manual + History | `056a3e88e65c429583b38514ee1d0a80` |
| Manual Mode — With History | `f7e80b5c88454a9694bd7ce9a0ce5164` |
| Main — Manual Mode (V2, revised) | `d7df094cd9294058b1052571d1456673` |
| Timer Mode — With History | `e8ef29ee7032465b9816ef54b7521600` |
| Voice Mode — With History | `6b687fb41bbf40e7bf3d585885942f70` |
| Main — Result (Red) | `ac37b8d58c4f4d3291a9872cc0983db3` |
| Settings Screen | `8521c3ac5b3b4ffbae3f12f55dbacea0` |
| Permission Rationale Dialog | `a49ecf319fef4945a478abe7db01fa8d` |

The "(V2, revised)" screens reflect the agreed product decisions (see below); the originals
are kept for history and can be deleted in the Stitch UI.

## Product decisions already applied to the design / app
- **No** language selector on main (moved into Settings as a flag picker); **no** app title on main.
- **No** "two-color" mode, **no** multiplayer/players, **no** "Save Changes" button (auto-save),
  **no** "Twister Pro" branding; color validation = **min 2 colors**.
- Mode selector at the top with **icon + text**; spin happens by **tapping the result card**
  (no reload FAB).
- **Bottom navigation tabs:** Play / Modos / Settings (content swaps in place, not pushed screens).
- **Movement history:** non-scrollable "Recent Moves" list, newest first, per-row `Turn #N`.
- **Timer mode:** countdown is the **card border**; Play/Stop appear as a tap overlay on the card.
- **Voice mode:** mic on/off is a tap overlay; card dim=off / neon + soundwave ripples=listening;
  listening status sits below the card. Single trigger word ("Twister").
- **Game modes implemented:** Classic, One Color (fixed color, max 2 players),
  Reducing (drops a color every N turns, announces the loss, optional loop),
  Reverse (lift instead of place), Challenge (~1/3 turns a dare), Sequence (2–3 moves).

## App icon
The Stitch-generated icon (glowing hand + foot + cyan spinner arrow on a violet gradient,
screen `6c0a65e89bd44d99834bdf618ba11743`) is the chosen direction but **not yet wired** into
the app (the manifest still points at the system placeholder `@android:drawable/sym_def_app_icon`).
