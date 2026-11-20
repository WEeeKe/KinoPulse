package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode {
    NIGHT,
    DAY,
    SYSTEM
}

@Immutable
data class KinoPalette(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceGlass: Color,
    val border: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val onAccent: Color,
    val ctaBackground: Color,
    val ctaContent: Color,
    val ratingGold: Color,
    val onlineGreen: Color
)

private val DarkKinoPalette = KinoPalette(
    isDark = true,
    background = CarbonBackground,
    surface = CarbonSurface,
    surfaceElevated = CarbonSurfaceElevated,
    surfaceGlass = Color(0xE614141A),
    border = CarbonBorder,
    borderSubtle = CarbonBorderSubtle,
    textPrimary = InkPrimaryDark,
    textSecondary = InkSecondaryDark,
    textTertiary = InkTertiaryDark,
    accent = CinemaAccentRed,
    onAccent = Color.White,
    ctaBackground = Color(0xFFF5F5F7),
    ctaContent = Color(0xFF0A0A0D),
    ratingGold = RatingChampagne,
    onlineGreen = SignalEmerald
)

private val AmoledKinoPalette = DarkKinoPalette.copy(
    background = TrueBlackAmoled,
    surface = Color(0xFF0C0C10),
    surfaceElevated = Color(0xFF15151B),
    surfaceGlass = Color(0xEC0C0C10),
    border = Color(0xFF22222B),
    borderSubtle = Color(0xFF16161D)
)

private val LightKinoPalette = KinoPalette(
    isDark = false,
    background = AlabasterBackground,
    surface = AlabasterSurface,
    surfaceElevated = AlabasterSurfaceElevated,
    surfaceGlass = Color(0xEBF8F8FA),
    border = AlabasterBorder,
    borderSubtle = AlabasterBorderSubtle,
    textPrimary = InkPrimaryLight,
    textSecondary = InkSecondaryLight,
    textTertiary = InkTertiaryLight,
    accent = CinemaAccentRedLight,
    onAccent = Color.White,
    ctaBackground = Color(0xFF111115),
    ctaContent = Color.White,
    ratingGold = RatingChampagneLight,
    onlineGreen = SignalEmeraldLight
)

val LocalKinoPalette = staticCompositionLocalOf { DarkKinoPalette }

private fun KinoPalette.toMaterialColorScheme() = if (isDark) {
    darkColorScheme(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = surfaceElevated,
        onPrimaryContainer = textPrimary,
        secondary = onlineGreen,
        onSecondary = Color(0xFF00210B),
        secondaryContainer = surfaceElevated,
        onSecondaryContainer = textPrimary,
        tertiary = ratingGold,
        onTertiary = Color.Black,
        background = background,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceElevated,
        onSurfaceVariant = textSecondary,
        surfaceContainerLow = surface,
        surfaceContainer = surfaceElevated,
        surfaceContainerHigh = surfaceElevated,
        outline = border,
        outlineVariant = borderSubtle
    )
} else {
    lightColorScheme(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = surfaceElevated,
        onPrimaryContainer = textPrimary,
        secondary = onlineGreen,
        onSecondary = Color.White,
        secondaryContainer = surfaceElevated,
        onSecondaryContainer = textPrimary,
        tertiary = ratingGold,
        onTertiary = Color.White,
        background = background,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceElevated,
        onSurfaceVariant = textSecondary,
        surfaceContainerLow = surface,
        surfaceContainer = surfaceElevated,
        surfaceContainerHigh = surfaceElevated,
        outline = border,
        outlineVariant = borderSubtle
    )
}

@Composable
fun KinoPulseTheme(
    themeMode: ThemeMode = ThemeMode.NIGHT,
    amoledNightMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        ThemeMode.NIGHT -> true
        ThemeMode.DAY -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val palette = when {
        useDark && amoledNightMode -> AmoledKinoPalette
        useDark -> DarkKinoPalette
        else -> LightKinoPalette
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !useDark
                controller.isAppearanceLightNavigationBars = !useDark
            }
        }
    }

    CompositionLocalProvider(LocalKinoPalette provides palette) {
        MaterialTheme(
            colorScheme = palette.toMaterialColorScheme(),
            typography = KinoPulseTypography,
            content = content
        )
    }
}
