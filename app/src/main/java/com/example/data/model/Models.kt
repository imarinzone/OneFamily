package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val activeHouseholdId: String = "",
    val personalMonthlyBudget: Double = 0.0,
    val currencySymbol: String = "₹",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ExpenseTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val householdId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = "EXPENSE", // EXPENSE | INCOME
    val category: String = "General",
    val accountName: String = "Cash",
    val paymentMode: String = "UPI", // UPI | BANK | CARD | WALLET | CASH
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val visibility: String = "SHARED", // SHARED | PERSONAL
    val smsSignature: String = "",
    val linkedSmsSignatures: List<String> = emptyList(),
    val referenceNumber: String = "",
    val transactionDateMillis: Long = System.currentTimeMillis(),
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CategoryBudget(
    @DocumentId val id: String = "",
    val userId: String = "",
    val householdId: String = "",
    val category: String = "",
    val monthlyLimit: Double = 0.0,
    val alertThresholdPercent: Long = 80L,
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FinancialAccount(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val bankName: String = "",
    val accountType: String = "BANK", // BANK | CREDIT_CARD | WALLET | CASH | UPI
    val lastFourDigits: String = "",
    val balance: Double = 0.0,
    val creditLimit: Double = 0.0,
    val isSharedVisible: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Household(
    @DocumentId val id: String = "",
    val name: String = "",
    val ownerUid: String = "",
    val joinCode: String = "",
    val memberUids: List<String> = emptyList(),
    val memberNames: Map<String, String> = emptyMap(),
    val memberEmails: Map<String, String> = emptyMap(),
    val invitedEmails: List<String> = emptyList(),
    val monthlyBudgetLimit: Double = 0.0,
    val currencySymbol: String = "₹",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class HouseholdCodeIndex(
    @DocumentId val id: String = "",
    val joinCode: String = "",
    val householdId: String = "",
    val householdName: String = "",
    val ownerUid: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class BudgetScope(val label: String) {
    HOUSEHOLD_SHARED("Household"),
    PERSONAL_ONLY("Personal")
}

enum class FamilyCurrencyOption(
    val symbol: String,
    val code: String,
    val displayName: String
) {
    INR("₹", "INR", "Indian Rupee (₹)"),
    USD("$", "USD", "US Dollar ($)"),
    EUR("€", "EUR", "Euro (€)"),
    GBP("£", "GBP", "British Pound (£)"),
    AED("AED", "AED", "UAE Dirham (AED)"),
    SGD("S$", "SGD", "Singapore Dollar (S$)"),
    JPY("¥", "JPY", "Japanese Yen (¥)"),
    CAD("C$", "CAD", "Canadian Dollar (C$)"),
    AUD("A$", "AUD", "Australian Dollar (A$)")
}

enum class WallpaperPatternStyle(val label: String) {
    WHATSAPP_DOODLES("Finance Doodles"),
    FINANCE_ICONS_AND_EMOJIS("Icons & Emojis"),
    MINIMAL_GEOMETRIC("Minimal Ledger")
}

enum class WallpaperColorAccent(val label: String) {
    THEME_ADAPTIVE("Theme Adaptive"),
    EMERALD_MINT("Emerald Mint"),
    ROYAL_GOLD("Royal Gold"),
    OCEAN_SLATE("Ocean Cyan"),
    WARM_CORAL("Warm Coral")
}

enum class WallpaperDensity(val label: String, val stepMultiplier: Float) {
    COMPACT("Dense", 0.78f),
    BALANCED("Balanced", 1.0f),
    AIRY("Spacious", 1.32f)
}

data class WallpaperSettings(
    val enabled: Boolean = true,
    val style: WallpaperPatternStyle = WallpaperPatternStyle.WHATSAPP_DOODLES,
    val colorAccent: WallpaperColorAccent = WallpaperColorAccent.THEME_ADAPTIVE,
    val density: WallpaperDensity = WallpaperDensity.BALANCED,
    val opacityPercent: Int = 11
)

data class FcmPushSettings(
    val familyTransactionAlertsEnabled: Boolean = true,
    val budgetThresholdAlertsEnabled: Boolean = true,
    val subscribedTopic: String = "household_shared"
)

data class CrashlyticsSettings(
    val crashReportingEnabled: Boolean = true,
    val smsSyncGuardEnabled: Boolean = true,
    val typographyGuardEnabled: Boolean = true
)

data class ExpertFeatureFlagsSettings(
    val expertModeEnabled: Boolean = false,
    val crashAnalyticsFeatureEnabled: Boolean = true,
    val fcmPushDiagnosticsEnabled: Boolean = true,
    val parallelSmsEngineTuningEnabled: Boolean = true,
    val strictDuplicateLinkerEnabled: Boolean = true,
    val rawSmsDebugInspectorEnabled: Boolean = true,
    val readerThreadCount: Int = 6,
    val writerThreadCount: Int = 6,
    val smsBatchSize: Int = 12
)

data class StabilityDiagnosticEntry(
    val id: String,
    val timestampMillis: Long,
    val subsystem: String, // "SMS_AUTO_SYNC" | "RESPONSIVE_TYPOGRAPHY" | "RUNTIME_GUARD" | "DUPLICATE_LINKER"
    val severity: String,  // "INFO" | "WARN" | "NON_FATAL" | "CRASH_PREVENTED"
    val message: String,
    val deviceModel: String
)

object ExpenseCategories {
    val ALL = listOf(
        "Groceries",
        "Food & Dining",
        "Rent & Housing",
        "Bills & Utilities",
        "Fuel & Travel",
        "Shopping",
        "Health & Fitness",
        "Entertainment",
        "Investments",
        "Salary & Income",
        "General"
    )

    val EXPENSE_ONLY = ALL.filter { it != "Salary & Income" }
}
