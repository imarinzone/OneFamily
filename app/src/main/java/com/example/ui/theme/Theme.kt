package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AxioEmeraldPrimaryDark,
    onPrimary = AxioOnEmeraldDark,
    primaryContainer = AxioEmeraldContainerDark,
    onPrimaryContainer = AxioOnEmeraldContainerDark,
    secondary = AxioSlateSecondaryDark,
    onSecondary = AxioOnSlateDark,
    secondaryContainer = AxioSlateContainerDark,
    onSecondaryContainer = AxioOnSlateContainerDark,
    tertiary = AxioIndigoTertiaryDark,
    onTertiary = AxioOnIndigoDark,
    tertiaryContainer = AxioIndigoContainerDark,
    onTertiaryContainer = AxioOnIndigoContainerDark,
    background = AxioBackgroundDark,
    onBackground = AxioOnSurfaceDark,
    surface = AxioSurfaceDark,
    onSurface = AxioOnSurfaceDark,
    surfaceVariant = AxioSurfaceVariantDark,
    onSurfaceVariant = AxioOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = AxioEmeraldPrimaryLight,
    onPrimary = AxioOnEmeraldLight,
    primaryContainer = AxioEmeraldContainerLight,
    onPrimaryContainer = AxioOnEmeraldContainerLight,
    secondary = AxioSlateSecondaryLight,
    onSecondary = AxioOnSlateLight,
    secondaryContainer = AxioSlateContainerLight,
    onSecondaryContainer = AxioOnSlateContainerLight,
    tertiary = AxioIndigoTertiaryLight,
    onTertiary = AxioOnIndigoLight,
    tertiaryContainer = AxioIndigoContainerLight,
    onTertiaryContainer = AxioOnIndigoContainerLight,
    background = AxioBackgroundLight,
    onBackground = AxioOnSurfaceLight,
    surface = AxioSurfaceLight,
    onSurface = AxioOnSurfaceLight,
    surfaceVariant = AxioSurfaceVariantLight,
    onSurfaceVariant = AxioOnSurfaceVariantLight
)

@Composable
fun AxioFamilyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
