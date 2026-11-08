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
