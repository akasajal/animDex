package com.animdex.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    DARK("Dark"),
    LIGHT("Light")
}

// Accent palettes
object AccentColors {
    val Rose = Color(0xFFC67A9E)
    val Lavender = Color(0xFF9D8DF1)
    val Sage = Color(0xFF7BC47F)
    val Sky = Color(0xFF61C0BF)
    val Amber = Color(0xFFF2C14E)
    val Slate = Color(0xFF6E7FA8)

    val all = mapOf(
        "Rose" to Rose,
        "Lavender" to Lavender,
        "Sage" to Sage,
        "Sky" to Sky,
        "Amber" to Amber,
        "Slate" to Slate
    )

    fun fromName(name: String): Color {
        return all[name] ?: Rose
    }

    fun nameOf(color: Color): String {
        return all.entries.firstOrNull { it.value == color }?.key ?: "Rose"
    }

    // High-contrast text color against the accent background (WCAG contrast compliant)
    fun onColorFor(accent: Color): Color {
        val luminance = 0.2126f * accent.red + 0.7152f * accent.green + 0.0722f * accent.blue
        return if (luminance > 0.48f) Color(0xFF121212) else Color.White
    }
}

val LocalAccentColor = staticCompositionLocalOf { AccentColors.Rose }

private fun darkScheme(accent: Color): ColorScheme {
    val onAccent = AccentColors.onColorFor(accent)
    return darkColorScheme(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accent.copy(alpha = 0.28f),
        onPrimaryContainer = Color(0xFFF5F5F5),
        background = Color(0xFF121212),
        onBackground = Color(0xFFE8E0E5),
        surface = Color(0xFF1E1E1E),
        onSurface = Color(0xFFE8E0E5),
        surfaceVariant = Color(0xFF2A2A2A),
        onSurfaceVariant = Color(0xFFB8B0B8),
        outline = Color(0xFF444444),
        outlineVariant = Color(0xFF333333),
        secondary = accent.copy(alpha = 0.85f),
        onSecondary = onAccent,
        tertiary = accent.copy(alpha = 0.6f),
        error = Color(0xFFCF6679),
    )
}

private fun lightScheme(accent: Color): ColorScheme {
    val onAccent = AccentColors.onColorFor(accent)
    return lightColorScheme(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accent.copy(alpha = 0.20f),
        onPrimaryContainer = Color(0xFF1A1A1A),
        background = Color(0xFFFAF8FB),
        onBackground = Color(0xFF1A1A1A),
        surface = Color.White,
        onSurface = Color(0xFF1A1A1A),
        surfaceVariant = Color(0xFFF0ECF1),
        onSurfaceVariant = Color(0xFF5A5A5A),
        outline = Color(0xFFD0C8D0),
        outlineVariant = Color(0xFFE4DFE4),
        secondary = accent.copy(alpha = 0.85f),
        onSecondary = onAccent,
        tertiary = accent.copy(alpha = 0.6f),
        error = Color(0xFFB00020),
    )
}

@Composable
fun AnimDexTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accent: Color = AccentColors.Rose,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) darkScheme(accent) else lightScheme(accent)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    CompositionLocalProvider(LocalAccentColor provides accent) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
