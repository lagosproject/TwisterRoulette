package com.lakescorp.twisterroulette.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.lakescorp.twisterroulette.domain.model.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = DeepViolet,
    secondary = NeonBlue,
    tertiary = CharcoalGlass,
    background = DarkObsidian,
    surface = CharcoalGlass,
    onPrimary = TextHighContrast,
    onSecondary = DarkObsidian,
    onTertiary = TextHighContrast,
    onBackground = TextHighContrast,
    onSurface = TextHighContrast
)

private val LightColorScheme = lightColorScheme(
    primary = DeepViolet,
    secondary = LightTeal,
    tertiary = Color(0xFFF0F0F5),
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEEEEF5),
    onPrimary = TextHighContrast,
    onSecondary = TextHighContrast,
    onTertiary = DarkObsidian,
    onBackground = DarkObsidian,
    onSurface = DarkObsidian,
    onSurfaceVariant = DarkObsidian
)

@Composable
fun TwisterRouletteTheme(
    theme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (theme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            activity.window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
