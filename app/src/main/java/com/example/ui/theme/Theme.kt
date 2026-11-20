package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    NIGHT,
    DAY,
    SYSTEM
}

private val CinemaDarkScheme = darkColorScheme(
    primary = AmberGold,
    onPrimary = Color(0xFF1A1200),
    primaryContainer = Color(0xFF3E2B07),
    onPrimaryContainer = AmberSoft,
    secondary = TealSignal,
    onSecondary = Color(0xFF00201D),
    secondaryContainer = Color(0xFF0E3A36),
    onSecondaryContainer = Color(0xFFA7F3EA),
    tertiary = CrimsonPulse,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4B1519),
    onTertiaryContainer = Color(0xFFFFD9DA),
    background = CinemaObsidian,
    onBackground = TextPrimaryDark,
    surface = CinemaSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CinemaSurfaceElevatedDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLow = CinemaSurfaceDark,
    surfaceContainer = CinemaSurfaceElevatedDark,
    surfaceContainerHigh = CinemaSurfaceHighestDark,
    outline = OutlineDark,
    outlineVariant = Color(0xFF21283B)
)

private val CinemaAmoledScheme = CinemaDarkScheme.copy(
    background = CinemaAmoledBlack,
    surface = Color(0xFF0B0D12),
    surfaceContainerLow = Color(0xFF0D1017),
    surfaceContainer = Color(0xFF141822),
    surfaceContainerHigh = Color(0xFF1D2230)
)

private val CinemaLightScheme = lightColorScheme(
    primary = AmberGoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECC8),
    onPrimaryContainer = Color(0xFF382300),
    secondary = Color(0xFF00796B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8F5EE),
    onSecondaryContainer = Color(0xFF002D27),
    tertiary = CrimsonPulse,
    onTertiary = Color.White,
    background = CinemaDayBackground,
    onBackground = CinemaDayInk,
    surface = CinemaDaySurface,
    onSurface = CinemaDayInk,
    surfaceVariant = CinemaDaySurfaceElevated,
    onSurfaceVariant = CinemaDayInkMuted,
    surfaceContainerLow = CinemaDaySurface,
    surfaceContainer = CinemaDaySurfaceElevated,
    surfaceContainerHigh = Color(0xFFE3E0D6),
    outline = OutlineLight,
    outlineVariant = Color(0xFFE6E3DA)
)

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

    val colorScheme = when {
        useDark && amoledNightMode -> CinemaAmoledScheme
        useDark -> CinemaDarkScheme
        else -> CinemaLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KinoPulseTypography,
        content = content
    )
}
