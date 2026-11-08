package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

private val DarkColorScheme = darkColorScheme(
    primary = CoralPrimaryDark,
    onPrimary = OnCoralDark,
    primaryContainer = CoralContainerDark,
    onPrimaryContainer = OnCoralContainerDark,
    secondary = MarigoldSecondaryDark,
    onSecondary = OnMarigoldDark,
    secondaryContainer = MarigoldContainerDark,
    onSecondaryContainer = OnMarigoldContainerDark,
    tertiary = BerryTertiaryDark,
    onTertiary = OnBerryDark,
    tertiaryContainer = BerryContainerDark,
    onTertiaryContainer = OnBerryContainerDark,
    background = CreamBackgroundDark,
    onBackground = OnCreamSurfaceDark,
    surface = PaperSurfaceDark,
    onSurface = OnCreamSurfaceDark,
    surfaceVariant = WarmSurfaceVariantDark,
    onSurfaceVariant = OnWarmSurfaceVariantDark,
    outline = Color(0xFF5A4E66),
    outlineVariant = Color(0xFF433A4D)
)

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimaryLight,
    onPrimary = OnCoralLight,
    primaryContainer = CoralContainerLight,
    onPrimaryContainer = OnCoralContainerLight,
    secondary = MarigoldSecondaryLight,
    onSecondary = OnMarigoldLight,
    secondaryContainer = MarigoldContainerLight,
    onSecondaryContainer = OnMarigoldContainerLight,
    tertiary = BerryTertiaryLight,
    onTertiary = OnBerryLight,
    tertiaryContainer = BerryContainerLight,
    onTertiaryContainer = OnBerryContainerLight,
    background = CreamBackgroundLight,
    onBackground = OnCreamSurfaceLight,
    surface = PaperSurfaceLight,
    onSurface = OnCreamSurfaceLight,
    surfaceVariant = WarmSurfaceVariantLight,
    onSurfaceVariant = OnWarmSurfaceVariantLight,
    outline = Color(0xFFD5C8B8),
    outlineVariant = Color(0xFFEAE2D3)
)

@Composable
fun AxioFamilyTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    },
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
