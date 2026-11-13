package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// M3 Expressive + Thermal Paper Receipt Palette (Default Kiwi Green + Pastel Tonal Containers)
val KiwiPrimaryLight = Color(0xFF65A30D)
val OnKiwiLight = Color(0xFFFFFFFF)
val KiwiContainerLight = Color(0xFFE4F7BE)
val OnKiwiContainerLight = Color(0xFF1A2E05)

val KiwiPrimaryDark = Color(0xFFA3E635)
val OnKiwiDark = Color(0xFF142600)
val KiwiContainerDark = Color(0xFF28420C)
val OnKiwiContainerDark = Color(0xFFE4F7BE)

val CoralPrimaryLight = Color(0xFFFF5A5F)
val OnCoralLight = Color(0xFFFFFFFF)
val CoralContainerLight = Color(0xFFFFE2E3)
val OnCoralContainerLight = Color(0xFF4A080C)

val MarigoldSecondaryLight = Color(0xFFD97706)
val OnMarigoldLight = Color(0xFFFFFFFF)
val MarigoldContainerLight = Color(0xFFFDE9B9)
val OnMarigoldContainerLight = Color(0xFF3A1E00)

val BerryTertiaryLight = Color(0xFF7C3AED)
val OnBerryLight = Color(0xFFFFFFFF)
val BerryContainerLight = Color(0xFFEDE4FF)
val OnBerryContainerLight = Color(0xFF28005C)

// Light Mode: Tinted Archival Thermal Paper Canvas & 5-Tier M3 Surface Containers
val CreamBackgroundLight = Color(0xFFFAF8F2)
val PaperSurfaceLight = Color(0xFFFAF8F2)
val WarmSurfaceVariantLight = Color(0xFFECE7DB)
val OnCreamSurfaceLight = Color(0xFF1B1C18)
val OnWarmSurfaceVariantLight = Color(0xFF525048)

val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF4F0E6)
val SurfaceContainerLight = Color(0xFFEEE9DE)
val SurfaceContainerHighLight = Color(0xFFE8E2D6)
val SurfaceContainerHighestLight = Color(0xFFE1DBCE)
val SurfaceDimLight = Color(0xFFDCD6C9)
val SurfaceBrightLight = Color(0xFFFFFDF8)

// Dark Mode: Rich M3 Tonal Charcoal Paper & Pure Black AMOLED Tokens
val CoralPrimaryDark = Color(0xFFFF7A7E)
val OnCoralDark = Color(0xFF420408)
val CoralContainerDark = Color(0xFF5C1519)
val OnCoralContainerDark = Color(0xFFFFD9DA)

val MarigoldSecondaryDark = Color(0xFFFBBF24)
val OnMarigoldDark = Color(0xFF3A1E00)
val MarigoldContainerDark = Color(0xFF4B2D05)
val OnMarigoldContainerDark = Color(0xFFFDE9B9)

val BerryTertiaryDark = Color(0xFFC4B5FD)
val OnBerryDark = Color(0xFF28005C)
val BerryContainerDark = Color(0xFF3E2270)
val OnBerryContainerDark = Color(0xFFEDE4FF)

// Tonal Dark Mode (Default Dark — rich elevation hierarchy for connected cards & docked nav)
val TonalBackgroundDark = Color(0xFF11130F)
val TonalSurfaceDark = Color(0xFF11130F)
val TonalSurfaceVariantDark = Color(0xFF363932)
val SurfaceContainerLowestDark = Color(0xFF0C0E0A)
val SurfaceContainerLowDark = Color(0xFF191C17)
val SurfaceContainerDark = Color(0xFF1E211C)
val SurfaceContainerHighDark = Color(0xFF282B25)
val SurfaceContainerHighestDark = Color(0xFF333730)
val SurfaceDimDark = Color(0xFF11130F)
val SurfaceBrightDark = Color(0xFF383B34)

// AMOLED Pure Black Dark Mode (Activated when AMOLED Mode switch is ON)
val CreamBackgroundDark = Color(0xFF000000)
val PaperSurfaceDark = Color(0xFF080908)
val WarmSurfaceVariantDark = Color(0xFF1B1D19)
val OnCreamSurfaceDark = Color(0xFFE5E6DF)
val OnWarmSurfaceVariantDark = Color(0xFFC2C4BB)

val AmoledSurfaceContainerLowest = Color(0xFF000000)
val AmoledSurfaceContainerLow = Color(0xFF0E100D)
val AmoledSurfaceContainer = Color(0xFF141613)
val AmoledSurfaceContainerHigh = Color(0xFF1D201B)
val AmoledSurfaceContainerHighest = Color(0xFF262A24)

// Punchy Semantic & Receipt Accents
val BudgetSafeGreen = Color(0xFF10B981)
val BudgetWarnAmber = Color(0xFFF59E0B)
val BudgetDangerCoral = Color(0xFFEF4444)
val HouseholdBadgeIndigo = Color(0xFF7C3AED)
val PersonalBadgeTeal = Color(0xFF0D9488)

val ReceiptPaperColor = Color(0xFFFFFDF7)
val ReceiptPaperBorder = Color(0xFFE6DEC9)
val ReceiptInkColor = Color(0xFF1D1B18)
val ReceiptMutedInk = Color(0xFF6D665C)

// Distinct Category Accent Color Helper
fun categoryAccentColorArgb(category: String): Int {
    val lower = category.trim().lowercase()
    return when {
        lower.isBlank() || lower == "unknown" || lower == "general" || lower == "uncategorized" -> 0xFF64748B.toInt()
        lower.contains("grocer") -> 0xFF06D6A0.toInt()
        lower.contains("food") || lower.contains("dining") -> 0xFFFF5A5F.toInt()
        lower.contains("rent") || lower.contains("housing") -> 0xFF8338EC.toInt()
        lower.contains("bill") || lower.contains("util") -> 0xFFFFB703.toInt()
        lower.contains("fuel") || lower.contains("travel") || lower.contains("transport") -> 0xFF118AB2.toInt()
        lower.contains("shop") -> 0xFFEC4899.toInt()
        lower.contains("health") || lower.contains("medical") || lower.contains("fitness") -> 0xFF10B981.toInt()
        lower.contains("entertain") -> 0xFFF97316.toInt()
        lower.contains("invest") -> 0xFF2563EB.toInt()
        lower.contains("salary") || lower.contains("income") -> 0xFF16A34A.toInt()
        else -> 0xFF0EA5E9.toInt()
    }
}

fun categoryAccentColor(category: String): Color {
    return Color(categoryAccentColorArgb(category))
}
