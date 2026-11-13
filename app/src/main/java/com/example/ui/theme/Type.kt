package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val GoogleDefaultFontFamily = FontFamily.Default

val PlusJakartaSansFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val ReceiptMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.SemiBold),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

private fun guardedTextStyle(
    fontFamily: FontFamily = GoogleDefaultFontFamily,
    fontWeight: FontWeight,
    baseFontSizeSp: Float,
    baseLineHeightSp: Float,
    scale: Float,
    baseLetterSpacingSp: Float = 0f
): TextStyle {
    val computedFontSize = (baseFontSizeSp * scale).let {
        if (it.isFinite() && it > 0f) it.coerceIn(9f, 54f) else baseFontSizeSp
    }
    val minSafeLineHeight = computedFontSize * 1.25f
    val computedLineHeight = (baseLineHeightSp * scale).let {
        if (it.isFinite() && it >= minSafeLineHeight) it else minSafeLineHeight
    }
    val computedLetterSpacing = (baseLetterSpacingSp * scale).let {
        if (it.isFinite()) it.coerceIn(-1.5f, 1.5f) else 0f
    }
    return TextStyle(
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        fontSize = computedFontSize.sp,
        lineHeight = computedLineHeight.sp,
        letterSpacing = computedLetterSpacing.sp
    )
}

/**
 * Dynamically scales M3 Expressive typography relative to screen dimensions (`scale`)
 * and OS accessibility `fontScale`, pairing bundled Plus Jakarta Sans display headers
 * with clean system body/medium headlines and JetBrains Mono receipt figures.
 */
fun buildResponsiveTypography(
    scale: Float = 1f,
    fontScale: Float = 1f
): Typography {
    val safeScale = if (scale.isFinite() && scale > 0f) scale.coerceIn(0.82f, 1.30f) else 1f
    val safeFontScale = if (fontScale.isFinite() && fontScale > 0f) fontScale.coerceIn(0.75f, 2.50f) else 1f
    val effectiveScale = if (safeScale * safeFontScale > 1.55f) {
        (1.55f / safeFontScale).coerceIn(0.82f, 1.22f)
    } else {
        safeScale
    }

    return Typography(
        displayLarge = guardedTextStyle(
            fontFamily = PlusJakartaSansFontFamily,
            fontWeight = FontWeight.Bold,
            baseFontSizeSp = 40f,
            baseLineHeightSp = 50f,
            scale = effectiveScale,
            baseLetterSpacingSp = -0.75f
        ),
        displayMedium = guardedTextStyle(
            fontFamily = PlusJakartaSansFontFamily,
            fontWeight = FontWeight.Bold,
            baseFontSizeSp = 32f,
            baseLineHeightSp = 40f,
            scale = effectiveScale,
            baseLetterSpacingSp = -0.4f
        ),
        displaySmall = guardedTextStyle(
            fontFamily = PlusJakartaSansFontFamily,
            fontWeight = FontWeight.Bold,
            baseFontSizeSp = 27f,
            baseLineHeightSp = 34f,
            scale = effectiveScale,
            baseLetterSpacingSp = -0.2f
        ),
        headlineLarge = guardedTextStyle(
            fontFamily = PlusJakartaSansFontFamily,
            fontWeight = FontWeight.Bold,
            baseFontSizeSp = 25f,
            baseLineHeightSp = 32f,
            scale = effectiveScale,
            baseLetterSpacingSp = -0.2f
        ),
        headlineMedium = guardedTextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.SemiBold,
            baseFontSizeSp = 20f,
            baseLineHeightSp = 26f,
            scale = effectiveScale
        ),
        headlineSmall = guardedTextStyle(
            fontFamily = PlusJakartaSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            baseFontSizeSp = 18.5f,
            baseLineHeightSp = 24f,
            scale = effectiveScale
        ),
        titleLarge = guardedTextStyle(
            fontFamily = PlusJakartaSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            baseFontSizeSp = 18f,
            baseLineHeightSp = 24f,
            scale = effectiveScale
        ),
        titleMedium = guardedTextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.SemiBold,
            baseFontSizeSp = 15.5f,
            baseLineHeightSp = 21f,
            scale = effectiveScale,
            baseLetterSpacingSp = 0.05f
        ),
        titleSmall = guardedTextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Medium,
            baseFontSizeSp = 13.5f,
            baseLineHeightSp = 19f,
            scale = effectiveScale
        ),
        bodyLarge = guardedTextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Normal,
            baseFontSizeSp = 15.5f,
            baseLineHeightSp = 22f,
            scale = effectiveScale
        ),
        bodyMedium = guardedTextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Normal,
            baseFontSizeSp = 13.5f,
            baseLineHeightSp = 19f,
            scale = effectiveScale
        ),
        bodySmall = guardedTextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Normal,
            baseFontSizeSp = 12f,
            baseLineHeightSp = 16f,
            scale = effectiveScale
        ),
        labelLarge = guardedTextStyle(
            fontFamily = PlusJakartaSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            baseFontSizeSp = 13.5f,
            baseLineHeightSp = 19f,
            scale = effectiveScale
        ),
        labelMedium = guardedTextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Medium,
            baseFontSizeSp = 12f,
            baseLineHeightSp = 16f,
            scale = effectiveScale
        ),
        labelSmall = guardedTextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Medium,
            baseFontSizeSp = 10.8f,
            baseLineHeightSp = 14.5f,
            scale = effectiveScale
        )
    )
}

val Typography = buildResponsiveTypography(1f, 1f)
