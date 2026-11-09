package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

/**
 * Relative screen dimensions computed dynamically from each phone's actual screen width and height.
 */
@Immutable
data class AppDimens(
    val screenWidth: Dp,
    val screenHeight: Dp,
    val widthScale: Float,
    val heightScale: Float,
    val horizontalPadding: Dp,
    val cardPadding: Dp,
    val itemSpacing: Dp,
    val cardCornerRadius: Dp,
    val smallCornerRadius: Dp,
    val rowIconBoxSize: Dp,
    val rowIconSize: Dp,
    val pulseRingSize: Dp,
    val donutChartSize: Dp,
    val barChartHeight: Dp,
    val navPillHeight: Dp,
    val navItemWidth: Dp,
    val bottomListPadding: Dp
)

val LocalAppDimens = staticCompositionLocalOf {
    AppDimens(
        screenWidth = 390.dp,
        screenHeight = 844.dp,
        widthScale = 1f,
        heightScale = 1f,
        horizontalPadding = 16.dp,
        cardPadding = 18.dp,
        itemSpacing = 12.dp,
        cardCornerRadius = 22.dp,
        smallCornerRadius = 14.dp,
        rowIconBoxSize = 44.dp,
        rowIconSize = 22.dp,
        pulseRingSize = 88.dp,
        donutChartSize = 128.dp,
        barChartHeight = 148.dp,
        navPillHeight = 48.dp,
        navItemWidth = 56.dp,
        bottomListPadding = 120.dp
    )
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
    background = Color(0xFF000000),
    onBackground = OnCreamSurfaceDark,
    surface = PaperSurfaceDark,
    onSurface = OnCreamSurfaceDark,
    surfaceVariant = WarmSurfaceVariantDark,
    onSurfaceVariant = OnWarmSurfaceVariantDark,
    surfaceTint = Color.Transparent,
    surfaceDim = Color(0xFF000000),
    surfaceBright = Color(0xFF1C1C1C),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF070707),
    surfaceContainer = Color(0xFF0A0A0A),
    surfaceContainerHigh = Color(0xFF121212),
    surfaceContainerHighest = Color(0xFF1A1A1A),
    outline = Color(0xFF2E2E2E),
    outlineVariant = Color(0xFF1F1F1F)
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
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.coerceAtLeast(320)
    val screenHeightDp = configuration.screenHeightDp.coerceAtLeast(480)

    val widthScale = (screenWidthDp / 390f).coerceIn(0.82f, 1.30f)
    val heightScale = (screenHeightDp / 844f).coerceIn(0.82f, 1.30f)

    val appDimens = remember(screenWidthDp, screenHeightDp) {
        val sw = screenWidthDp.dp
        val sh = screenHeightDp.dp
        AppDimens(
            screenWidth = sw,
            screenHeight = sh,
            widthScale = widthScale,
            heightScale = heightScale,
            horizontalPadding = (sw * 0.041f).coerceIn(12.dp, 28.dp),
            cardPadding = (sw * 0.045f).coerceIn(14.dp, 26.dp),
            itemSpacing = (sh * 0.014f).coerceIn(8.dp, 18.dp),
            cardCornerRadius = (sw * 0.056f).coerceIn(18.dp, 30.dp),
            smallCornerRadius = (sw * 0.036f).coerceIn(12.dp, 20.dp),
            rowIconBoxSize = (sw * 0.112f).coerceIn(40.dp, 54.dp),
            rowIconSize = (sw * 0.056f).coerceIn(20.dp, 28.dp),
            pulseRingSize = (sw * 0.225f).coerceIn(76.dp, 116.dp),
            donutChartSize = (sw * 0.32f).coerceIn(106.dp, 164.dp),
            barChartHeight = (sh * 0.175f).coerceIn(124.dp, 196.dp),
            navPillHeight = (sh * 0.058f).coerceAtLeast(48.dp),
            navItemWidth = (sw * 0.145f).coerceAtLeast(48.dp),
            bottomListPadding = (sh * 0.14f).coerceIn(96.dp, 150.dp)
        )
    }

    val responsiveTypography = remember(widthScale) {
        buildResponsiveTypography(widthScale)
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalAppDimens provides appDimens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = responsiveTypography,
            content = content
        )
    }
}
