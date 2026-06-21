#!/usr/bin/env python3
"""
generate_showcase.py — Play Store Screenshot & Feature Graphic Generator for Twister Roulette
=============================================================================================
Creates beautiful 1080×1920 showcase cards (for Phone, 7-inch Tablet, and 10-inch Tablet)
in English, Spanish, and French, plus a 1024×500 Feature Graphic banner.

This script uses real screenshots (either manually placed in raw/ or automatically
imported from Paparazzi Compose previews).
"""

import os
import sys
import math
import shutil
from PIL import Image, ImageDraw, ImageFont, ImageFilter

# ─── Constants ───
OUT_W = 1080
OUT_H = 1920

# Standard crop heights for real device screenshots (Pixel 5 / typical aspect ratios)
STATUS_BAR_RATIO = 0.04
NAV_BAR_RATIO = 0.05

LANGS = ["en-US", "es-ES", "fr-FR", "pt-PT", "hi-IN", "zh-CN"]
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
RAW_DIR = os.path.join(SCRIPT_DIR, "raw")
OUT_DIR = os.path.join(SCRIPT_DIR, "output")
PAPARAZZI_DIR = os.path.join(SCRIPT_DIR, "..", "app", "src", "test", "snapshots", "images")

# Brand colors (Kinetic Neon Theme)
PRIMARY = (124, 46, 255)       # Electric violet (#7C2EFF)
SECONDARY = (46, 255, 255)     # Neon cyan (#2EFFFF)
BG_DARK = (15, 15, 15)         # DarkObsidian (#0F0F0F)
BG_CARD = (30, 30, 30)         # CharcoalGlass (#1E1E1E)
WHITE = (255, 255, 255)        # TextHighContrast (#FFFFFF)
MUTED = (176, 176, 176)        # TextMuted (#B0B0B0)

# ─── Localized Copy Config ───
LANG_COPY = {
    "en-US": {
        "brand": "TWISTER ROULETTE",
        "play": {
            "title": "PLAY",
            "headline": "SPIN THE NEON WHEEL",
            "subtext": "Randomly drawn body parts and colors announced out loud. Works fully offline.",
        },
        "modes": {
            "title": "GAME MODES",
            "headline": "DIVERSE GAME MODES",
            "subtext": "Play Classic, One Color, sequence challenges, or the tense Reducing mode.",
        },
        "settings": {
            "title": "SETTINGS",
            "headline": "TAILOR THE RULES",
            "subtext": "Configure custom colors, voice trigger words, timer interval, and voice pitch.",
        },
        "feature_graphic": {
            "headline": "Twister Roulette",
            "subtext": "The ultimate digital party spinner"
        }
    },
    "es-ES": {
        "brand": "RULETA TWISTER",
        "play": {
            "title": "JUGAR",
            "headline": "GIRA LA RULETA NEÓN",
            "subtext": "Mano o pie y colores aleatorios anunciados con voz en tiempo real.",
        },
        "modes": {
            "title": "MODOS DE JUEGO",
            "headline": "MODOS DE JUEGO VARIADOS",
            "subtext": "Elige entre Clásico, Un Color, Secuencia o el emocionante modo Reductor.",
        },
        "settings": {
            "title": "AJUSTES",
            "headline": "PERSONALIZA LAS REGLAS",
            "subtext": "Configura idiomas, palabras clave de voz, tiempos, efectos y conjuntos de colores.",
        },
        "feature_graphic": {
            "headline": "Ruleta Twister",
            "subtext": "La ruleta digital para tus fiestas"
        }
    },
    "fr-FR": {
        "brand": "TWISTER ROULETTE",
        "play": {
            "title": "JOUER",
            "headline": "TOURNEZ LA ROULETTE NEON",
            "subtext": "Mouvements et couleurs aléatoires annoncés par synthèse vocale en temps réel.",
        },
        "modes": {
            "title": "MODOS DE JEU",
            "headline": "MODOS DE JEU DIVERS",
            "subtext": "Jouez en Classique, Une Couleur, Séquence ou le mode Réducteur intense.",
        },
        "settings": {
            "title": "PARAMÈTRES",
            "headline": "PERSONNALISEZ LES RÈGLES",
            "subtext": "Ajustez les jeux de couleurs, mots de déclenchement, minuteurs et voix.",
        },
        "feature_graphic": {
            "headline": "Twister Roulette",
            "subtext": "La roulette numérique pour vos soirées"
        }
    },
    "pt-PT": {
        "brand": "ROLETA TWISTER",
        "play": {
            "title": "JOGAR",
            "headline": "GIRA A RODA DE NEON",
            "subtext": "Partes do corpo e cores sorteadas aleatoriamente e anunciadas em voz alta. Funciona totalmente offline.",
        },
        "modes": {
            "title": "MODOS DE JOGO",
            "headline": "DIVERSOS MODOS DE JOGO",
            "subtext": "Joga o modo Clássico, Uma Cor, desafios de sequência ou o tenso modo Redutor.",
        },
        "settings": {
            "title": "CONFIGURAÇÕES",
            "headline": "PERSONALIZA AS REGRAS",
            "subtext": "Configura cores personalizadas, palavras de ativação por voz, intervalo do temporizador e o tom da voz.",
        },
        "feature_graphic": {
            "headline": "Roleta Twister",
            "subtext": "A melhor roleta de festa digital"
        }
    },
    "hi-IN": {
        "brand": "ट्विस्टर रूलेट",
        "play": {
            "title": "खेलें",
            "headline": "नियॉन व्हील घुमाएं",
            "subtext": "यादृच्छिक रूप से चुने गए शरीर के अंग और रंग ज़ोर से घोषित किए जाते हैं। पूरी तरह से ऑफ़लाइन काम करता है।",
        },
        "modes": {
            "title": "गेम मोड",
            "headline": "विविध गेम मोड",
            "subtext": "क्लासिक, एक रंग, अनुक्रम चुनौतियाँ, या तनावपूर्ण रिड्यूसिंग मोड खेलें।",
        },
        "settings": {
            "title": "सेटिंग्स",
            "headline": "नियमों को अनुकूलित करें",
            "subtext": "कस्टम रंग, वॉयस ट्रिगर शब्द, टाइमर अंतराल और वॉयस पिच कॉन्फ़िगर करें।",
        },
        "feature_graphic": {
            "headline": "ट्विस्टर रूलेट",
            "subtext": "परम डिजिटल पार्टी स्पिनर"
        }
    },
    "zh-CN": {
        "brand": "扭扭乐轮盘",
        "play": {
            "title": "开始玩",
            "headline": "旋转霓虹轮盘",
            "subtext": "随机抽取身体部位和颜色并大声朗读。完全离线可用。",
        },
        "modes": {
            "title": "游戏模式",
            "headline": "多样游戏模式",
            "subtext": "体验经典模式、单色模式、顺序挑战或紧张的递减模式。",
        },
        "settings": {
            "title": "设置",
            "headline": "量身定制规则",
            "subtext": "配置自定义颜色、语音触发词、定时器间隔及语音音调。",
        },
        "feature_graphic": {
            "headline": "扭扭乐轮盘",
            "subtext": "终极数字派对旋转器"
        }
    }
}

# ─── Font Helpers ───
def _try_fonts(paths, size):
    for p in paths:
        if os.path.exists(p):
            try:
                return ImageFont.truetype(p, size)
            except Exception:
                continue
    return ImageFont.load_default()

def font_sans(size, bold=False, lang="en-US"):
    paparazzi_lang = lang.split("-")[0]
    paths = []
    if paparazzi_lang == "zh":
        paths = [
            f"/usr/share/fonts/opentype/noto/NotoSansCJK-{'Bold' if bold else 'Regular'}.ttc",
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
        ]
    elif paparazzi_lang == "hi":
        paths = [
            f"/usr/share/fonts/truetype/noto/NotoSansDevanagari-{'Bold' if bold else 'Regular'}.ttf",
            "/usr/share/fonts/truetype/noto/NotoSansDevanagari-Regular.ttf"
        ]
    
    paths.extend([
        f"/usr/share/fonts/truetype/noto/NotoSans-{'Bold' if bold else 'Regular'}.ttf",
        f"/usr/share/fonts/truetype/liberation/LiberationSans-{'Bold' if bold else 'Regular'}.ttf",
        f"/usr/share/fonts/truetype/dejavu/DejaVuSans{'-Bold' if bold else ''}.ttf",
    ])
    return _try_fonts(paths, size)

# ─── Standard Drawing Helpers ───
def center_text(draw, text, y, font, fill, width=OUT_W):
    bbox = draw.textbbox((0, 0), text, font=font)
    tw = bbox[2] - bbox[0]
    x = (width - tw) // 2
    draw.text((x, y), text, font=font, fill=fill)

def wrap_text_centered(draw, text, y, font, fill, max_w, line_gap=10, width=OUT_W):
    words = text.split()
    lines, cur = [], ""
    for w in words:
        test = f"{cur} {w}".strip()
        if draw.textbbox((0, 0), test, font=font)[2] <= max_w:
            cur = test
        else:
            if cur:
                lines.append(cur)
            cur = w
    if cur:
        lines.append(cur)

    lh = draw.textbbox((0, 0), "Ag", font=font)[3] - draw.textbbox((0,0), "Ag", font=font)[1] + line_gap
    for i, ln in enumerate(lines):
        center_text(draw, ln, y + i * lh, font, fill, width)
    return len(lines) * lh

def get_wrap_height(draw, text, font, max_w, line_gap=10):
    words = text.split()
    lines, cur = [], ""
    for w in words:
        test = f"{cur} {w}".strip()
        if draw.textbbox((0, 0), test, font=font)[2] <= max_w:
            cur = test
        else:
            if cur:
                lines.append(cur)
            cur = w
    if cur:
        lines.append(cur)
    lh = draw.textbbox((0, 0), "Ag", font=font)[3] - draw.textbbox((0,0), "Ag", font=font)[1] + line_gap
    return len(lines) * lh

def accent_rule(draw, y, length=140, color=None, width=OUT_W):
    color = color or (*PRIMARY, 200)
    cx = width // 2
    draw.line([(cx - length // 2, y), (cx + length // 2, y)], fill=color, width=3)

def dark_gradient(w, h, tint_ratio=0.35):
    img = Image.new("RGB", (w, h), BG_DARK)
    d = ImageDraw.Draw(img)
    for i in range(h):
        t = i / h
        r = int(BG_DARK[0] + (PRIMARY[0] - BG_DARK[0]) * t * tint_ratio)
        g = int(BG_DARK[1] + (PRIMARY[1] - BG_DARK[1]) * t * tint_ratio * 0.3)
        b = int(BG_DARK[2] + (PRIMARY[2] - BG_DARK[2]) * t * tint_ratio * 0.8)
        d.line([(0, i), (w, i)], fill=(r, g, b))
    return img

def rounded_rect_mask(size, radius):
    w, h = size
    # 2x supersampling for ultra-smooth antialiasing
    mask2x = Image.new("L", (w * 2, h * 2), 0)
    ImageDraw.Draw(mask2x).rounded_rectangle([(0, 0), (w * 2 - 1, h * 2 - 1)], radius=radius * 2, fill=255)
    return mask2x.resize((w, h), Image.Resampling.LANCZOS)

def apply_shadow(img, blur=18, offset=(6, 10), color=(0, 0, 0, 160)):
    pad = blur * 2
    sw, sh = img.width + pad + abs(offset[0]), img.height + pad + abs(offset[1])
    shadow = Image.new("RGBA", (sw, sh), (0, 0, 0, 0))
    stamp = Image.new("RGBA", img.size, color)
    mask = img.split()[3]
    ox, oy = pad // 2 + max(0, offset[0]), pad // 2 + max(0, offset[1])
    shadow.paste(stamp, (ox, oy), mask)
    shadow = shadow.filter(ImageFilter.GaussianBlur(blur))
    ix, iy = pad // 2 + max(0, -offset[0]), pad // 2 + max(0, -offset[1])
    shadow.paste(img, (ix, iy), img)
    return shadow, (ix - ox, iy - oy)

# ─── Raw Screenshot Loader with Fallbacks ───
def get_raw_screenshot(screen_type, lang, form_factor):
    """
    Finds the correct raw screenshot.
    1. Looks under raw/<form_factor>/<lang>/<screen_type>.png
    2. Try to find and import localized Paparazzi snapshot (e.g. com.lakescorp.twisterroulette_ScreenshotTest_settings[pt].png)
    3. Fallback to raw/<form_factor>/en-US/<screen_type>.png
    4. Fallback to default/English Paparazzi snapshot
    """
    os.makedirs(os.path.join(RAW_DIR, form_factor, lang), exist_ok=True)
    
    # 1. Check local path
    local_filename = f"{screen_type}.png"
    local_path = os.path.join(RAW_DIR, form_factor, lang, local_filename)
    if os.path.exists(local_path):
        return Image.open(local_path)

    # Map screenshot type to paparazzi snapshot test name
    paparazzi_map = {
        "play": "com.lakescorp.twisterroulette_ScreenshotTest_main_result_red.png",
        "modes": "com.lakescorp.twisterroulette_ScreenshotTest_modes_classic.png",
        "settings": "com.lakescorp.twisterroulette_ScreenshotTest_settings.png"
    }
    paparazzi_name = paparazzi_map.get(screen_type)
    
    # 2. Try to find and import localized Paparazzi snapshot
    if paparazzi_name:
        paparazzi_lang = lang.split("-")[0]  # e.g. "pt" from "pt-PT"
        name_without_ext, ext = os.path.splitext(paparazzi_name)
        localized_paparazzi_name = f"{name_without_ext}[{paparazzi_lang}]{ext}"
        localized_paparazzi_path = os.path.join(PAPARAZZI_DIR, localized_paparazzi_name)
        
        if os.path.exists(localized_paparazzi_path):
            shutil.copy2(localized_paparazzi_path, local_path)
            print(f"  📸 Found Localized Paparazzi screenshot: Imported {localized_paparazzi_name} -> {local_path}")
            return Image.open(local_path)
        
    # 3. Check English fallback in raw/
    en_path = os.path.join(RAW_DIR, form_factor, "en-US", local_filename)
    if os.path.exists(en_path):
        print(f"  ⚠️ Warning: Using English fallback screenshot for {lang}/{screen_type} ({en_path})")
        return Image.open(en_path)
        
    # 4. Fallback to default/English Paparazzi snapshot
    if paparazzi_name:
        paparazzi_path = os.path.join(PAPARAZZI_DIR, paparazzi_name)
        if os.path.exists(paparazzi_path):
            # Cache to en-US first
            en_dir = os.path.join(RAW_DIR, "phone", "en-US")
            os.makedirs(en_dir, exist_ok=True)
            cached_path = os.path.join(en_dir, local_filename)
            shutil.copy2(paparazzi_path, cached_path)
            
            # Also copy to requested path
            shutil.copy2(paparazzi_path, local_path)
            print(f"  📸 Found default Paparazzi screenshot: Imported {paparazzi_name} -> {local_path}")
            return Image.open(local_path)

    # 5. Error if not found anywhere
    print(f"\n❌ ERROR: Raw screenshot not found for '{screen_type}' in locale '{lang}' under:")
    print(f"   - {local_path}")
    print(f"   - {en_path}")
    if paparazzi_name:
        print(f"   - Paparazzi: {os.path.join(PAPARAZZI_DIR, paparazzi_name)}")
    print("\nPlease run './gradlew recordPaparazzi' to generate snapshots first, or manually place screenshots in 'raw/'.\n")
    sys.exit(1)

# ─── Showcase Card Generator (Universal) ───
def make_showcase_card(screen_type, lang, form_factor, out_path):
    """
    Builds a unified 1080×1920 Google Play Store Showcase Card using real screenshots.
    """
    W, H = OUT_W, OUT_H
    
    # Load raw image
    raw_img = get_raw_screenshot(screen_type, lang, form_factor).convert("RGBA")
    
    # Determine sizing parameters
    if form_factor == "phone":
        phone_h_render = 1200
    elif form_factor == "tablet_7":
        phone_h_render = 1200
    else: # tablet_10
        phone_h_render = 1150
        
    # Crop status & nav bars ONLY if it's a real device screenshot.
    # Paparazzi screenshots are exactly 1000px high (461x1000) and do not contain OS system bars.
    # We only crop if height indicates a real full-screen device capture (e.g. height >= 1200).
    if raw_img.height >= 1200:
        crop_top = int(raw_img.height * STATUS_BAR_RATIO)
        crop_bottom = int(raw_img.height * NAV_BAR_RATIO)
        cropped = raw_img.crop((0, crop_top, raw_img.width, raw_img.height - crop_bottom))
    else:
        cropped = raw_img
        
    # Scale to target render height preserving aspect ratio
    scale = phone_h_render / cropped.height
    pw = int(cropped.width * scale)
    ph = phone_h_render
    scaled = cropped.resize((pw, ph), Image.LANCZOS)
    
    # Create mask for rounded corners
    corner_radius = 42 if form_factor == "phone" else 24
    mask = rounded_rect_mask((pw, ph), radius=corner_radius)
    scaled.putalpha(mask)
    
    # Draw premium accent border around screenshot (supersampled)
    border_im2x = Image.new("RGBA", (pw * 2, ph * 2), (0, 0, 0, 0))
    b_draw = ImageDraw.Draw(border_im2x)
    b_draw.rounded_rectangle([(0, 0), (pw * 2 - 1, ph * 2 - 1)], radius=corner_radius * 2, outline=(*PRIMARY, 180), width=4 * 2)
    border_im = border_im2x.resize((pw, ph), Image.Resampling.LANCZOS)
    scaled.paste(border_im, (0, 0), border_im)
    
    # Apply soft drop shadow
    shadowed, (six, siy) = apply_shadow(scaled, blur=22, offset=(0, 14))
    
    # Paste onto canvas
    canvas = dark_gradient(W, H).convert("RGBA")
    draw = ImageDraw.Draw(canvas)
    
    # Text styles
    f_label = font_sans(24, bold=True, lang=lang)
    f_head = font_sans(56, bold=True, lang=lang)
    f_sub = font_sans(32, bold=False, lang=lang)
    
    brand_lbl = LANG_COPY[lang]["brand"]
    headline = LANG_COPY[lang][screen_type]["headline"]
    subtext = LANG_COPY[lang][screen_type]["subtext"]
    
    if form_factor == "phone":
        # ── Phone: Screenshot Top, Text Bottom
        # Paste Mockup centered horizontally
        sx = (W - shadowed.width) // 2
        sy = 100 - siy
        canvas.paste(shadowed, (sx, sy), shadowed)
        
        # Soft vertical fade-out transition
        fade_h = 100
        fade_y = 1250
        fade = Image.new("RGBA", (W, fade_h))
        fd = ImageDraw.Draw(fade)
        for i in range(fade_h):
            a = int(255 * ((i/fade_h) ** 1.6))
            fd.line([(0, i), (W, i)], fill=(*BG_DARK, a))
        canvas.alpha_composite(fade, (0, fade_y))
        
        # Calculate dynamic text height to center it perfectly
        label_h = 24
        gap1, gap2 = 45, 30
        headline_h = get_wrap_height(draw, headline, f_head, W - 100, 16)
        gap3 = 24
        subtext_h = get_wrap_height(draw, subtext, f_sub, W - 120, 12)
        
        total_text_h = label_h + gap1 + gap2 + headline_h + gap3 + subtext_h
        panel_y = 1300
        panel_h = H - panel_y
        
        text_y = panel_y + (panel_h - total_text_h) // 2
        
        # Draw text elements
        center_text(draw, brand_lbl, text_y, f_label, (*SECONDARY, 210))
        text_y += label_h + gap1
        accent_rule(draw, text_y, length=100, color=(*SECONDARY, 140))
        text_y += gap2
        
        y_used = wrap_text_centered(draw, headline, text_y, f_head, WHITE, max_w=W - 100, line_gap=16)
        text_y += y_used + gap3
        accent_rule(draw, text_y, length=60, color=(*SECONDARY, 160))
        text_y += gap3
        
        wrap_text_centered(draw, subtext, text_y, f_sub, MUTED, max_w=W - 120, line_gap=12)
        
    else:
        # ── Tablet (7" and 10"): Text Top, Screenshot Bottom
        # Draw text elements at the top
        text_y = 54
        center_text(draw, brand_lbl, text_y, f_label, (*SECONDARY, 210))
        accent_rule(draw, 102, length=100, color=(*SECONDARY, 140))
        
        y_used = wrap_text_centered(draw, headline, 134, f_head, WHITE, max_w=W - 100, line_gap=16)
        accent_rule(draw, 134 + y_used + 16, length=60, color=(*SECONDARY, 160))
        
        wrap_text_centered(draw, subtext, 134 + y_used + 46, f_sub, MUTED, max_w=W - 120, line_gap=12)
        
        # Paste tablet mockup centered at the bottom
        sx = (W - shadowed.width) // 2
        sy = 480 - siy
        canvas.paste(shadowed, (sx, sy), shadowed)
        
    # Save the card
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    canvas.convert("RGB").save(out_path, "PNG", optimize=True)
    size_kb = os.path.getsize(out_path) // 1024
    print(f"  ✅ Generated card: {out_path} ({size_kb} KB)")

# ─── Feature Graphic Generator ───
def make_feature_graphic(out_path, lang="en-US"):
    """
    Generates a 1024×500 Store Banner (Feature Graphic):
      - Deep rich dark background with vibrant, vector-smooth neon radial glows (Cyan & Violet)
      - Fading Neon Cyan technical dot grid (tiny 3x3 sharp dots)
      - Overlapping double-mockup with anti-aliased borders and shadows
      - Dynamic typography layout matching app styles
    """
    W, H = 1024, 500
    
    # ── Background gradient ──
    canvas = Image.new("RGB", (W, H), (10, 10, 15))
    draw = ImageDraw.Draw(canvas)
    for y in range(H):
        ratio = y / H
        r = int(10 + 6 * ratio)
        g = int(10 + 4 * ratio)
        b = int(12 + 10 * ratio)
        draw.line([(0, y), (W, y)], fill=(r, g, b))
        
    # ── Vector-Smooth Radial Glows (Concentric circle blending at full-res) ──
    glow_overlay = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow_overlay)
    
    # Violet glow (bottom-left)
    cx_v, cy_v, r_v = 180, 420, 360
    for r in range(0, r_v, 2):
        ratio = r / r_v
        opacity = int(85 * (1.0 - ratio) ** 1.8)
        glow_draw.ellipse([cx_v - r, cy_v - r, cx_v + r, cy_v + r], outline=(124, 46, 255, opacity), width=3)
        
    # Cyan glow (right side, behind devices)
    cx_c, cy_c, r_c = 780, 250, 440
    for r in range(0, r_c, 2):
        ratio = r / r_c
        opacity = int(100 * (1.0 - ratio) ** 1.8)
        glow_draw.ellipse([cx_c - r, cy_c - r, cx_c + r, cy_c + r], outline=(46, 255, 255, opacity), width=3)
        
    canvas.paste(glow_overlay, (0, 0), glow_overlay)
    
    # ── Technical Dot Grid ──
    grid_overlay = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    grid_draw = ImageDraw.Draw(grid_overlay)
    for x in range(20, W, 40):
        for y in range(20, H, 40):
            dist = math.sqrt((x - 780)**2 + (y - 250)**2)
            alpha = int(max(0, 255 - dist * 0.4) * 0.08)
            if alpha > 2:
                # Small 3x3 dots for technical look
                grid_draw.ellipse([x - 1, y - 1, x + 1, y + 1], fill=(46, 255, 255, alpha))
    canvas.paste(grid_overlay, (0, 0), grid_overlay)

    # ── Try to load & draw the brand logo ──
    logo_path = os.path.join(SCRIPT_DIR, "logo.png")
    logo_svg = os.path.join(SCRIPT_DIR, "..", "logo.svg")
    
    # Auto-compile logo.svg to logo.png if missing or newer
    if os.path.exists(logo_svg):
        if not os.path.exists(logo_path) or os.path.getmtime(logo_svg) > os.path.getmtime(logo_path):
            import subprocess
            try:
                inkscape_path = None
                for path in ["/snap/bin/inkscape", "inkscape"]:
                    if shutil.which(path):
                        inkscape_path = path
                        break
                if inkscape_path:
                    subprocess.run([inkscape_path, logo_svg, "-o", logo_path], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                    print(f"  🎨 Inkscape: Successfully rasterized logo.svg -> logo.png")
            except Exception as e:
                print(f"  ⚠️ Warning: Could not auto-rasterize logo.svg ({e})")

    has_logo = False
    if os.path.exists(logo_path):
        try:
            logo_img = Image.open(logo_path).convert("RGBA")
            has_logo = True
        except Exception:
            pass

    # ── Load mockups ──
    # Background mockup (Modes screen)
    bg_raw = get_raw_screenshot("modes", lang, "phone").convert("RGBA")
    if bg_raw.height >= 1200:
        crop_top = int(bg_raw.height * STATUS_BAR_RATIO)
        crop_bottom = int(bg_raw.height * NAV_BAR_RATIO)
        bg_cropped = bg_raw.crop((0, crop_top, bg_raw.width, bg_raw.height - crop_bottom))
    else:
        bg_cropped = bg_raw
        
    bg_dev_h = 330
    bg_scale = bg_dev_h / bg_cropped.height
    bg_dev_w = int(bg_cropped.width * bg_scale)
    bg_scaled = bg_cropped.resize((bg_dev_w, bg_dev_h), Image.LANCZOS)
    bg_mask = rounded_rect_mask((bg_dev_w, bg_dev_h), radius=14)
    bg_scaled.putalpha(bg_mask)
    
    # Supersampled background device border
    bg_border2x = Image.new("RGBA", (bg_dev_w * 2, bg_dev_h * 2), (0, 0, 0, 0))
    bg_b_draw = ImageDraw.Draw(bg_border2x)
    bg_b_draw.rounded_rectangle([(0, 0), (bg_dev_w * 2 - 1, bg_dev_h * 2 - 1)], radius=14 * 2, outline=(124, 46, 255, 140), width=2 * 2)
    bg_border = bg_border2x.resize((bg_dev_w, bg_dev_h), Image.Resampling.LANCZOS)
    bg_scaled.paste(bg_border, (0, 0), bg_border)
    bg_shadowed, _ = apply_shadow(bg_scaled, blur=10, offset=(4, 6))
    
    # Foreground mockup (Play screen)
    fg_raw = get_raw_screenshot("play", lang, "phone").convert("RGBA")
    if fg_raw.height >= 1200:
        crop_top = int(fg_raw.height * STATUS_BAR_RATIO)
        crop_bottom = int(fg_raw.height * NAV_BAR_RATIO)
        fg_cropped = fg_raw.crop((0, crop_top, fg_raw.width, fg_raw.height - crop_bottom))
    else:
        fg_cropped = fg_raw
        
    fg_dev_h = 385
    fg_scale = fg_dev_h / fg_cropped.height
    fg_dev_w = int(fg_cropped.width * fg_scale)
    fg_scaled = fg_cropped.resize((fg_dev_w, fg_dev_h), Image.LANCZOS)
    fg_mask = rounded_rect_mask((fg_dev_w, fg_dev_h), radius=16)
    fg_scaled.putalpha(fg_mask)
    
    # Supersampled foreground device border
    fg_border2x = Image.new("RGBA", (fg_dev_w * 2, fg_dev_h * 2), (0, 0, 0, 0))
    fg_b_draw = ImageDraw.Draw(fg_border2x)
    fg_b_draw.rounded_rectangle([(0, 0), (fg_dev_w * 2 - 1, fg_dev_h * 2 - 1)], radius=16 * 2, outline=(46, 255, 255, 200), width=3 * 2)
    fg_border = fg_border2x.resize((fg_dev_w, fg_dev_h), Image.Resampling.LANCZOS)
    fg_scaled.paste(fg_border, (0, 0), fg_border)
    fg_shadowed, _ = apply_shadow(fg_scaled, blur=14, offset=(6, 12))

    # Paste background phone (centered at x = 835, y = 250)
    bg_pad = 20
    bg_ix = bg_pad // 2 + max(0, -4)
    bg_iy = bg_pad // 2 + max(0, -6)
    bg_sx = 835 - bg_dev_w // 2 - bg_ix
    bg_sy = 250 - bg_dev_h // 2 - bg_iy
    canvas.paste(bg_shadowed, (bg_sx, bg_sy), bg_shadowed)

    # Paste foreground phone (centered at x = 710, y = 250)
    fg_pad = 28
    fg_ix = fg_pad // 2 + max(0, -6)
    fg_iy = fg_pad // 2 + max(0, -12)
    fg_sx = 710 - fg_dev_w // 2 - fg_ix
    fg_sy = 250 - fg_dev_h // 2 - fg_iy
    canvas.paste(fg_shadowed, (fg_sx, fg_sy), fg_shadowed)

    # ── Text layout engine (Left Side) ──
    x_text = 80
    logo_sz = 80
    
    headline = LANG_COPY[lang]["feature_graphic"]["headline"]
    subtitle = LANG_COPY[lang]["feature_graphic"]["subtext"]
    
    if lang == "zh-CN":
        headline_lines = [headline]
    else:
        headline_lines = [line.strip() for line in headline.split(" ") if line.strip()]
        
    f_title = font_sans(68, bold=True, lang=lang)
    dummy_draw = ImageDraw.Draw(Image.new("L", (1, 1)))
    lh_title = dummy_draw.textbbox((0, 0), "Ag", font=f_title)[3] - dummy_draw.textbbox((0, 0), "Ag", font=f_title)[1]
    lh_title = int(lh_title * 1.1)
    headline_height = len(headline_lines) * lh_title
    
    f_sub = font_sans(24, bold=False, lang=lang)
    lh_sub = dummy_draw.textbbox((0, 0), "Ag", font=f_sub)[3] - dummy_draw.textbbox((0, 0), "Ag", font=f_sub)[1]
    lh_sub = int(lh_sub * 1.2)
    
    def wrap_text_to_lines(text, font, max_w):
        words = text.split(" ")
        lines, cur = [], ""
        for w in words:
            test = f"{cur} {w}".strip()
            w_test = dummy_draw.textbbox((0, 0), test, font=font)[2]
            if w_test <= max_w:
                cur = test
            else:
                if cur:
                    lines.append(cur)
                cur = w
        if cur:
            lines.append(cur)
        return lines

    max_subtitle_w = 480
    subtitle_lines = wrap_text_to_lines(subtitle, f_sub, max_subtitle_w)
    subtitle_height = len(subtitle_lines) * lh_sub
    
    gap_logo_title = 20
    gap_title_divider = 20
    gap_divider_subtitle = 16
    divider_h = 4
    
    total_text_h = 0
    if has_logo:
        total_text_h += logo_sz + gap_logo_title
    total_text_h += headline_height + gap_title_divider + divider_h + gap_divider_subtitle + subtitle_height
    
    y_start = (H - total_text_h) // 2
    if y_start < 60:
        y_start = 60
        
    curr_y = y_start
    if has_logo:
        logo_scaled = logo_img.resize((logo_sz, logo_sz), Image.LANCZOS)
        canvas.paste(logo_scaled, (x_text, curr_y), logo_scaled)
        curr_y += logo_sz + gap_logo_title
        
    for line in headline_lines:
        draw.text((x_text, curr_y), line, font=f_title, fill=WHITE)
        curr_y += lh_title
        
    curr_y += gap_title_divider
    
    # Draw neon cyan accent line
    draw.line([(x_text, curr_y), (x_text + 120, curr_y)], fill=(46, 255, 255, 220), width=divider_h)
    curr_y += divider_h + gap_divider_subtitle
    
    for line in subtitle_lines:
        draw.text((x_text, curr_y), line, font=f_sub, fill=MUTED)
        curr_y += lh_sub
    
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    canvas.save(out_path, "PNG", optimize=True)
    size_kb = os.path.getsize(out_path) // 1024
    print(f"  🎨 Generated Feature Graphic: {out_path} ({size_kb} KB)")

# ─── Main ───
def main():
    print("=" * 70)
    print("      TWISTER ROULETTE — Play Store Asset Creation Suite")
    print("=" * 70)
    
    # 1. Process showcases for each locale & form factor
    for form_factor in ["phone", "tablet_7", "tablet_10"]:
        print(f"\n🚀 Processing Form Factor: {form_factor.upper()}")
        print("-" * 50)
        
        for lang in LANGS:
            print(f"  Locale: {lang}")
            for screen in ["play", "modes", "settings"]:
                out_file = f"showcase_{screen}.png"
                out_path = os.path.join(OUT_DIR, form_factor, lang, out_file)
                make_showcase_card(screen, lang, form_factor, out_path)
                
    # 2. Process Feature Graphic (English/default, Spanish, French)
    print(f"\n🚀 Generating Feature Graphic Banners")
    print("-" * 50)
    for lang in LANGS:
        out_name = f"feature_graphic_{lang}.png" if lang != "en-US" else "feature_graphic.png"
        make_feature_graphic(os.path.join(OUT_DIR, out_name), lang=lang)
    
    print(f"\n{'='*70}")
    print("  COMPLETED SUCCESSFULLY!")
    print(f"{'='*70}")
    print(f"  All Play Store assets have been written to:")
    print(f"  {OUT_DIR}/")
    print(f"  └─ phone/      ← Upload to Phone Screenshots section")
    print(f"  └─ tablet_7/   ← Upload to 7-inch Tablet Screenshots section")
    print(f"  └─ tablet_10/  ← Upload to 10-inch Tablet Screenshots section")
    print(f"  └─ feature_graphic.png  ← Upload to Feature Graphic section")

if __name__ == "__main__":
    main()
