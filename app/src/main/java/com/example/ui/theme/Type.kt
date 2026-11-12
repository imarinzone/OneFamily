package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val GoogleDefaultFontFamily = FontFamily.Default

private fun guardedTextStyle(
    fontWeight: FontWeight,
    baseFontSizeSp: Float,
    baseLineHeightSp: Float,
    scale: Float,
    baseLetterSpacingSp: Float = 0f
): TextStyle {
    val computedFontSize = (baseFontSizeSp * scale).let {
        if (it.isFinite() && it > 0f) it.coerceIn(9f, 52f) else baseFontSizeSp
    }
    val minSafeLineHeight = computedFontSize * 1.25f
    val computedLineHeight = (baseLineHeightSp * scale).let {
        if (it.isFinite() && it >= minSafeLineHeight) it else minSafeLineHeight
    }
    val computedLetterSpacing = (baseLetterSpacingSp * scale).let {
        if (it.isFinite()) it.coerceIn(-1.5f, 1.5f) else 0f
    }
    return TextStyle(
        fontFamily = GoogleDefaultFontFamily,
        fontWeight = fontWeight,
        fontSize = computedFontSize.sp,
        lineHeight = computedLineHeight.sp,
        letterSpacing = computedLetterSpacing.sp
    )
}

/**
 * Dynamically scales all typography sizes relative to the phone's screen dimensions (`scale`)
 * and OS accessibility `fontScale` using Google default system fonts (`FontFamily.Default`).
 * Always enforces finite, bounded scale values and safe line heights unconditionally.
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
            fontWeight = FontWeight.Bold,
            baseFontSizeSp = 38f,
            baseLineHeightSp = 48f,
            scale = effectiveScale,
            baseLetterSpacingSp = -0.5f
        ),
        displayMedium = guardedTextStyle(
            fontWeight = FontWeight.Bold,
            baseFontSizeSp = 30f,
            baseLineHeightSp = 38f,
            scale = effectiveScale,
            baseLetterSpacingSp = -0.25f
        ),
        headlineLarge = guardedTextStyle(
            fontWeight = FontWeight.Bold,
            baseFontSizeSp = 24f,
            baseLineHeightSp = 31f,
            scale = effectiveScale
        ),
        headlineMedium = guardedTextStyle(
            fontWeight = FontWeight.SemiBold,
            baseFontSizeSp = 20f,
            baseLineHeightSp = 26f,
            scale = effectiveScale
        ),
        titleLarge = guardedTextStyle(
            fontWeight = FontWeight.SemiBold,
            baseFontSizeSp = 18f,
            baseLineHeightSp = 24f,
            scale = effectiveScale
        ),
        titleMedium = guardedTextStyle(
            fontWeight = FontWeight.SemiBold,
            baseFontSizeSp = 15.5f,
            baseLineHeightSp = 21f,
            scale = effectiveScale,
            baseLetterSpacingSp = 0.1f
        ),
        titleSmall = guardedTextStyle(
            fontWeight = FontWeight.Medium,
            baseFontSizeSp = 13.5f,
            baseLineHeightSp = 19f,
            scale = effectiveScale
        ),
        bodyLarge = guardedTextStyle(
            fontWeight = FontWeight.Normal,
            baseFontSizeSp = 15.5f,
            baseLineHeightSp = 22f,
            scale = effectiveScale
        ),
        bodyMedium = guardedTextStyle(
            fontWeight = FontWeight.Normal,
            baseFontSizeSp = 13.5f,
            baseLineHeightSp = 19f,
            scale = effectiveScale
        ),
        bodySmall = guardedTextStyle(
            fontWeight = FontWeight.Normal,
            baseFontSizeSp = 12f,
            baseLineHeightSp = 16f,
            scale = effectiveScale
        ),
        labelLarge = guardedTextStyle(
            fontWeight = FontWeight.SemiBold,
            baseFontSizeSp = 13.5f,
            baseLineHeightSp = 19f,
            scale = effectiveScale
        ),
        labelMedium = guardedTextStyle(
            fontWeight = FontWeight.Medium,
            baseFontSizeSp = 12f,
            baseLineHeightSp = 16f,
            scale = effectiveScale
        ),
        labelSmall = guardedTextStyle(
            fontWeight = FontWeight.Medium,
            baseFontSizeSp = 10.8f,
            baseLineHeightSp = 14.5f,
            scale = effectiveScale
        )
    )
}

val Typography = buildResponsiveTypography(1f, 1f)
