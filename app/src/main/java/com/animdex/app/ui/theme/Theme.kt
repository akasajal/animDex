package com.animdex.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AnimDexDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = DarkSurface,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = EmeraldAccent,
    onSecondary = DarkSurface,
    background = DarkSurface,
    onBackground = TextPrimaryDark,
    surface = DarkSurfaceVariant,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = CardBorder
)

@Composable
fun AnimDexTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AnimDexDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
