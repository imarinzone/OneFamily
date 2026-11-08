package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Warm Coral, Sunny Marigold, Mint & Berry Pop on Cream Palette
val CoralPrimaryLight = Color(0xFFFF5A5F)
val OnCoralLight = Color(0xFFFFFFFF)
val CoralContainerLight = Color(0xFFFFE2E3)
val OnCoralContainerLight = Color(0xFF4A080C)

val MarigoldSecondaryLight = Color(0xFFF59E0B)
val OnMarigoldLight = Color(0xFFFFFFFF)
val MarigoldContainerLight = Color(0xFFFFF0C2)
val OnMarigoldContainerLight = Color(0xFF3D2400)

val BerryTertiaryLight = Color(0xFF8338EC)
val OnBerryLight = Color(0xFFFFFFFF)
val BerryContainerLight = Color(0xFFEDE2FF)
val OnBerryContainerLight = Color(0xFF2B0063)

val CreamBackgroundLight = Color(0xFFFFFDF9)
val PaperSurfaceLight = Color(0xFFFFFFFF)
val WarmSurfaceVariantLight = Color(0xFFF9F3E8)
val OnCreamSurfaceLight = Color(0xFF231F20)
val OnWarmSurfaceVariantLight = Color(0xFF6E655F)

// Dark Theme Tokens (Cozy Warm Plum-Charcoal with Punchy Pop Accents)
val CoralPrimaryDark = Color(0xFFFF7A7E)
val OnCoralDark = Color(0xFF4A080C)
val CoralContainerDark = Color(0xFF7A181D)
val OnCoralContainerDark = Color(0xFFFFE2E3)

val MarigoldSecondaryDark = Color(0xFFFFC44D)
val OnMarigoldDark = Color(0xFF3D2400)
val MarigoldContainerDark = Color(0xFF5E3A00)
val OnMarigoldContainerDark = Color(0xFFFFF0C2)

val BerryTertiaryDark = Color(0xFFB98CFF)
val OnBerryDark = Color(0xFF2B0063)
val BerryContainerDark = Color(0xFF491296)
val OnBerryContainerDark = Color(0xFFEDE2FF)

val CreamBackgroundDark = Color(0xFF19151D)
val PaperSurfaceDark = Color(0xFF241F29)
val WarmSurfaceVariantDark = Color(0xFF322B38)
val OnCreamSurfaceDark = Color(0xFFFFFDF9)
val OnWarmSurfaceVariantDark = Color(0xFFC9BFC9)

// Punchy Semantic & Receipt Accents
val BudgetSafeGreen = Color(0xFF06D6A0)
val BudgetWarnAmber = Color(0xFFFFB703)
val BudgetDangerCoral = Color(0xFFFF5A5F)
val HouseholdBadgeIndigo = Color(0xFF8338EC)
val PersonalBadgeTeal = Color(0xFFFF5A5F)

val ReceiptPaperColor = Color(0xFFFFFDF6)
val ReceiptPaperBorder = Color(0xFFEAE2D3)
val ReceiptInkColor = Color(0xFF231F20)
val ReceiptMutedInk = Color(0xFF756B63)

// Punchy Category Color Helper
fun categoryAccentColor(category: String): Color {
    val lower = category.lowercase()
    return when {
        lower.contains("grocer") -> Color(0xFF06D6A0) // Fresh Mint
        lower.contains("food") || lower.contains("dining") -> Color(0xFFFF5A5F) // Warm Coral
        lower.contains("rent") || lower.contains("housing") -> Color(0xFF8338EC) // Berry Pop
        lower.contains("bill") || lower.contains("util") -> Color(0xFFFFB703) // Sunny Marigold
        lower.contains("fuel") || lower.contains("travel") -> Color(0xFF118AB2) // Ocean Pop
        lower.contains("shop") -> Color(0xFFEC4899) // Pink Pop
        lower.contains("health") -> Color(0xFF10B981) // Emerald Mint
        lower.contains("entertain") -> Color(0xFFF97316) // Tangerine
        lower.contains("invest") || lower.contains("salary") || lower.contains("income") -> Color(0xFF06D6A0)
        else -> Color(0xFF8338EC)
    }
}
