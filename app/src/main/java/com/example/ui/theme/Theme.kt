package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LocalAccountSessionManager

enum class AppThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Returns Material Design 3 Expressive connected card stack shape:
 * - Single card: `outerCorner` on all 4 corners
 * - Top card: `outerCorner` on top, `innerCorner` on bottom
 * - Middle card: `innerCorner` on all 4 corners
 * - Bottom card: `innerCorner` on top, `outerCorner` on bottom
 */
fun groupedCardShape(
    index: Int,
    count: Int,
    outerCorner: Dp = 24.dp,
    innerCorner: Dp = 6.dp
): RoundedCornerShape {
    return when {
        count <= 1 -> RoundedCornerShape(outerCorner)
        index <= 0 -> RoundedCornerShape(
            topStart = outerCorner,
            topEnd = outerCorner,
            bottomStart = innerCorner,
            bottomEnd = innerCorner
        )
        index >= count - 1 -> RoundedCornerShape(
            topStart = innerCorner,
            topEnd = innerCorner,
            bottomStart = outerCorner,
            bottomEnd = outerCorner
        )
        else -> RoundedCornerShape(innerCorner)
    }
}

/**
 * Returns a haptic tick callback that respects the user's Haptic Feedback setting.
 */
@Composable
fun rememberAppHapticPerformer(): () -> Unit {
    val haptic = LocalHapticFeedback.current
    val enabled by LocalAccountSessionManager.hapticFeedbackEnabled.collectAsStateWithLifecycle()
    return remember(haptic, enabled) {
        {
            if (enabled) {
                runCatching {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        }
    }
}

enum class AppPrimaryColorOption(
    val lightPrimary: Color,
    val lightContainer: Color,
    val onLightContainer: Color,
    val darkPrimary: Color,
    val darkContainer: Color,
    val onDarkContainer: Color
) {
    KIWI_GREEN(
        lightPrimary = KiwiPrimaryLight,
        lightContainer = KiwiContainerLight,
        onLightContainer = OnKiwiContainerLight,
        darkPrimary = KiwiPrimaryDark,
        darkContainer = KiwiContainerDark,
        onDarkContainer = OnKiwiContainerDark
    ),
    SYSTEM_DYNAMIC(
        lightPrimary = KiwiPrimaryLight,
        lightContainer = KiwiContainerLight,
        onLightContainer = OnKiwiContainerLight,
        darkPrimary = KiwiPrimaryDark,
        darkContainer = KiwiContainerDark,
        onDarkContainer = OnKiwiContainerDark
    ),
    CORAL_POP(
        lightPrimary = CoralPrimaryLight,
        lightContainer = CoralContainerLight,
        onLightContainer = OnCoralContainerLight,
        darkPrimary = CoralPrimaryDark,
        darkContainer = CoralContainerDark,
        onDarkContainer = OnCoralContainerDark
    ),
    OCEAN_BLUE(
        lightPrimary = Color(0xFF0284C7),
        lightContainer = Color(0xFFE0F2FE),
        onLightContainer = Color(0xFF082F49),
        darkPrimary = Color(0xFF38BDF8),
        darkContainer = Color(0xFF082F49),
        onDarkContainer = Color(0xFFE0F2FE)
    ),
    ROYAL_BERRY(
        lightPrimary = Color(0xFF7C3AED),
        lightContainer = Color(0xFFEDE4FF),
        onLightContainer = Color(0xFF28005C),
        darkPrimary = Color(0xFFC4B5FD),
        darkContainer = Color(0xFF3E2270),
        onDarkContainer = Color(0xFFEDE4FF)
    ),
    EMERALD_TEAL(
        lightPrimary = Color(0xFF059669),
        lightContainer = Color(0xFFD1FAE5),
        onLightContainer = Color(0xFF064E3B),
        darkPrimary = Color(0xFF34D399),
        darkContainer = Color(0xFF064E3B),
        onDarkContainer = Color(0xFFD1FAE5)
    ),
    SUNNY_AMBER(
        lightPrimary = Color(0xFFD97706),
        lightContainer = Color(0xFFFEF3C7),
        onLightContainer = Color(0xFF451A03),
        darkPrimary = Color(0xFFFBBF24),
        darkContainer = Color(0xFF451A03),
        onDarkContainer = Color(0xFFFEF3C7)
    ),
    ROSE_PINK(
        lightPrimary = Color(0xFFDB2777),
        lightContainer = Color(0xFFFCE7F3),
        onLightContainer = Color(0xFF500724),
        darkPrimary = Color(0xFFF472B6),
        darkContainer = Color(0xFF500724),
        onDarkContainer = Color(0xFFFCE7F3)
    ),
    CUSTOM_COLOR(
        lightPrimary = KiwiPrimaryLight,
        lightContainer = KiwiContainerLight,
        onLightContainer = OnKiwiContainerLight,
        darkPrimary = KiwiPrimaryDark,
        darkContainer = KiwiContainerDark,
        onDarkContainer = OnKiwiContainerDark
    )
}

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
        cardCornerRadius = 24.dp,
        smallCornerRadius = 16.dp,
        rowIconBoxSize = 46.dp,
        rowIconSize = 22.dp,
        pulseRingSize = 88.dp,
        donutChartSize = 128.dp,
        barChartHeight = 152.dp,
        navPillHeight = 48.dp,
        navItemWidth = 64.dp,
        bottomListPadding = 96.dp
    )
}

private val BaseTonalDarkColorScheme = darkColorScheme(
    primary = KiwiPrimaryDark,
    onPrimary = OnKiwiDark,
    primaryContainer = KiwiContainerDark,
    onPrimaryContainer = OnKiwiContainerDark,
    secondary = MarigoldSecondaryDark,
    onSecondary = OnMarigoldDark,
    secondaryContainer = MarigoldContainerDark,
    onSecondaryContainer = OnMarigoldContainerDark,
    tertiary = BerryTertiaryDark,
    onTertiary = OnBerryDark,
    tertiaryContainer = BerryContainerDark,
    onTertiaryContainer = OnBerryContainerDark,
    background = TonalBackgroundDark,
    onBackground = OnCreamSurfaceDark,
    surface = TonalSurfaceDark,
    onSurface = OnCreamSurfaceDark,
    surfaceVariant = TonalSurfaceVariantDark,
    onSurfaceVariant = OnWarmSurfaceVariantDark,
    surfaceTint = KiwiPrimaryDark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    outline = Color(0xFF7B7F75),
    outlineVariant = Color(0xFF3A3E36)
)

private val BaseAmoledDarkColorScheme = BaseTonalDarkColorScheme.copy(
    background = CreamBackgroundDark,
    surface = PaperSurfaceDark,
    surfaceVariant = WarmSurfaceVariantDark,
    surfaceTint = Color.Transparent,
    surfaceDim = Color(0xFF000000),
    surfaceBright = Color(0xFF222520),
    surfaceContainerLowest = AmoledSurfaceContainerLowest,
    surfaceContainerLow = AmoledSurfaceContainerLow,
    surfaceContainer = AmoledSurfaceContainer,
    surfaceContainerHigh = AmoledSurfaceContainerHigh,
    surfaceContainerHighest = AmoledSurfaceContainerHighest,
    outline = Color(0xFF565A51),
    outlineVariant = Color(0xFF262923)
)

private val BaseLightColorScheme = lightColorScheme(
    primary = KiwiPrimaryLight,
    onPrimary = OnKiwiLight,
    primaryContainer = KiwiContainerLight,
    onPrimaryContainer = OnKiwiContainerLight,
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
    surfaceTint = KiwiPrimaryLight,
    surfaceDim = SurfaceDimLight,
    surfaceBright = SurfaceBrightLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    outline = Color(0xFF817D73),
    outlineVariant = Color(0xFFDAD3C5)
)

private fun blendColors(from: Color, to: Color, ratio: Float): Color {
    val r = ratio.coerceIn(0f, 1f)
    return Color(
        red = from.red * (1f - r) + to.red * r,
        green = from.green * (1f - r) + to.green * r,
        blue = from.blue * (1f - r) + to.blue * r,
        alpha = 1f
    )
}

fun resolveAppColorScheme(
    context: android.content.Context,
    darkTheme: Boolean,
    primaryColorOption: AppPrimaryColorOption,
    customPrimaryColorArgb: Long,
    amoledDark: Boolean = false
): ColorScheme {
    val baseDark = if (amoledDark) BaseAmoledDarkColorScheme else BaseTonalDarkColorScheme
    if (primaryColorOption == AppPrimaryColorOption.SYSTEM_DYNAMIC &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    ) {
        return runCatching {
            if (darkTheme) {
                val dynDark = dynamicDarkColorScheme(context)
                if (amoledDark) {
                    dynDark.copy(
                        background = Color(0xFF000000),
                        surface = PaperSurfaceDark,
                        surfaceVariant = WarmSurfaceVariantDark,
                        surfaceContainerLowest = AmoledSurfaceContainerLowest,
                        surfaceContainerLow = AmoledSurfaceContainerLow,
                        surfaceContainer = AmoledSurfaceContainer,
                        surfaceContainerHigh = AmoledSurfaceContainerHigh,
                        surfaceContainerHighest = AmoledSurfaceContainerHighest,
                        surfaceTint = Color.Transparent
                    )
                } else {
                    dynDark
                }
            } else {
                dynamicLightColorScheme(context)
            }
        }.getOrElse {
            if (darkTheme) baseDark else BaseLightColorScheme
        }
    }

    if (primaryColorOption == AppPrimaryColorOption.CUSTOM_COLOR) {
        val baseCustom = Color(customPrimaryColorArgb.toInt()).copy(alpha = 1f)
        return if (darkTheme) {
            val darkPrimary = if (baseCustom.luminance() < 0.25f) {
                blendColors(baseCustom, Color.White, 0.35f)
            } else {
                baseCustom
            }
            val darkContainer = blendColors(baseCustom, Color.Black, 0.70f)
            val onDarkContainer = blendColors(baseCustom, Color.White, 0.82f)
            baseDark.copy(
                primary = darkPrimary,
                onPrimary = if (darkPrimary.luminance() > 0.45f) Color.Black else Color.White,
                primaryContainer = darkContainer,
                onPrimaryContainer = onDarkContainer,
                secondaryContainer = blendColors(darkPrimary, baseDark.surfaceContainerHigh, 0.72f),
                onSecondaryContainer = onDarkContainer
            )
        } else {
            val lightPrimary = if (baseCustom.luminance() > 0.65f) {
                blendColors(baseCustom, Color.Black, 0.28f)
            } else {
                baseCustom
            }
            val lightContainer = blendColors(baseCustom, Color.White, 0.82f)
            val onLightContainer = blendColors(baseCustom, Color.Black, 0.72f)
            BaseLightColorScheme.copy(
                primary = lightPrimary,
                onPrimary = if (lightPrimary.luminance() > 0.55f) Color.Black else Color.White,
                primaryContainer = lightContainer,
                onPrimaryContainer = onLightContainer,
                secondaryContainer = blendColors(lightPrimary, BaseLightColorScheme.surfaceContainerHigh, 0.76f),
                onSecondaryContainer = onLightContainer
            )
        }
    }

    return if (darkTheme) {
        baseDark.copy(
            primary = primaryColorOption.darkPrimary,
            onPrimary = if (primaryColorOption.darkPrimary.luminance() > 0.45f) Color.Black else Color.White,
            primaryContainer = primaryColorOption.darkContainer,
            onPrimaryContainer = primaryColorOption.onDarkContainer
        )
    } else {
        BaseLightColorScheme.copy(
            primary = primaryColorOption.lightPrimary,
            onPrimary = if (primaryColorOption.lightPrimary.luminance() > 0.55f) Color.Black else Color.White,
            primaryContainer = primaryColorOption.lightContainer,
            onPrimaryContainer = primaryColorOption.onLightContainer
        )
    }
}

@Composable
fun OneFamilyTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    },
    primaryColorOption: AppPrimaryColorOption? = null,
    customPrimaryColorArgb: Long? = null,
    amoledDark: Boolean? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val observedPrimaryOption by LocalAccountSessionManager.primaryColorOption.collectAsStateWithLifecycle()
    val observedCustomArgb by LocalAccountSessionManager.customPrimaryColorArgb.collectAsStateWithLifecycle()
    val observedAmoledDark by LocalAccountSessionManager.amoledDarkEnabled.collectAsStateWithLifecycle()
    val effectivePrimaryOption = primaryColorOption ?: observedPrimaryOption
    val effectiveCustomArgb = customPrimaryColorArgb ?: observedCustomArgb
    val effectiveAmoledDark = amoledDark ?: observedAmoledDark

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.coerceAtLeast(320)
    val screenHeightDp = configuration.screenHeightDp.coerceAtLeast(480)

    val rawWidthScale = screenWidthDp / 390f
    val rawHeightScale = screenHeightDp / 844f
    val fontScale = configuration.fontScale.let { if (it.isFinite() && it > 0f) it else 1f }
    val widthScale = if (rawWidthScale.isFinite() && rawWidthScale > 0f) rawWidthScale.coerceIn(0.82f, 1.30f) else 1f
    val heightScale = if (rawHeightScale.isFinite() && rawHeightScale > 0f) rawHeightScale.coerceIn(0.82f, 1.30f) else 1f

    val appDimens = remember(screenWidthDp, screenHeightDp) {
        val sw = screenWidthDp.dp
        val sh = screenHeightDp.dp
        AppDimens(
            screenWidth = sw,
            screenHeight = sh,
            widthScale = widthScale,
            heightScale = heightScale,
            horizontalPadding = (sw * 0.044f).coerceIn(14.dp, 28.dp),
            cardPadding = (sw * 0.046f).coerceIn(16.dp, 26.dp),
            itemSpacing = (sh * 0.014f).coerceIn(8.dp, 18.dp),
            cardCornerRadius = (sw * 0.060f).coerceIn(20.dp, 30.dp),
            smallCornerRadius = (sw * 0.040f).coerceIn(14.dp, 20.dp),
            rowIconBoxSize = (sw * 0.116f).coerceIn(44.dp, 54.dp),
            rowIconSize = (sw * 0.056f).coerceIn(20.dp, 28.dp),
            pulseRingSize = (sw * 0.225f).coerceIn(76.dp, 116.dp),
            donutChartSize = (sw * 0.32f).coerceIn(106.dp, 164.dp),
            barChartHeight = (sh * 0.18f).coerceIn(128.dp, 196.dp),
            navPillHeight = (sh * 0.058f).coerceAtLeast(48.dp),
            navItemWidth = (sw * 0.155f).coerceAtLeast(52.dp),
            bottomListPadding = (sh * 0.12f).coerceIn(88.dp, 140.dp)
        )
    }

    val responsiveTypography = remember(widthScale, fontScale) {
        runCatching { buildResponsiveTypography(scale = widthScale, fontScale = fontScale) }
            .getOrElse { Typography }
    }

    val colorScheme = remember(context, darkTheme, effectivePrimaryOption, effectiveCustomArgb, effectiveAmoledDark) {
        resolveAppColorScheme(
            context = context,
            darkTheme = darkTheme,
            primaryColorOption = effectivePrimaryOption,
            customPrimaryColorArgb = effectiveCustomArgb,
            amoledDark = effectiveAmoledDark
        )
    }

    CompositionLocalProvider(LocalAppDimens provides appDimens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = responsiveTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
