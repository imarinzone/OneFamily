package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val GoogleDefaultFontFamily = FontFamily.Default

/**
 * Dynamically scales all typography sizes relative to the phone's screen dimensions (`scale`)
 * using Google default system fonts (`FontFamily.Default`).
 */
fun buildResponsiveTypography(scale: Float = 1f): Typography {
    val s = scale.coerceIn(0.82f, 1.30f)
    return Typography(
        displayLarge = TextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (38f * s).sp,
            lineHeight = (44f * s).sp,
            letterSpacing = (-0.5f * s).sp
        ),
        displayMedium = TextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (30f * s).sp,
            lineHeight = (36f * s).sp,
            letterSpacing = (-0.25f * s).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (24f * s).sp,
            lineHeight = (30f * s).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20f * s).sp,
            lineHeight = (26f * s).sp
        ),
        titleLarge = TextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (18f * s).sp,
            lineHeight = (24f * s).sp
        ),
        titleMedium = TextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (15.5f * s).sp,
            lineHeight = (21f * s).sp,
            letterSpacing = (0.1f * s).sp
        ),
        titleSmall = TextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (13.5f * s).sp,
            lineHeight = (19f * s).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (15.5f * s).sp,
            lineHeight = (22f * s).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (13.5f * s).sp,
            lineHeight = (19f * s).sp
        ),
        bodySmall = TextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12f * s).sp,
            lineHeight = (16f * s).sp
        ),
        labelLarge = TextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (13.5f * s).sp,
            lineHeight = (19f * s).sp
        ),
        labelMedium = TextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12f * s).sp,
            lineHeight = (16f * s).sp
        ),
        labelSmall = TextStyle(
            fontFamily = GoogleDefaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (10.8f * s).sp,
            lineHeight = (14.5f * s).sp
        )
    )
}

val Typography = buildResponsiveTypography(1f)
