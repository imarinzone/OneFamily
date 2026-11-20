package com.example.data.sms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LocalAccountSessionManager
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.data.repository.OneFamilyRepository
import com.example.ui.components.NotificationQuickCategoryWheelOverlay
import com.example.ui.screens.MerchantCategoryBatchUpdateDialog
import com.example.ui.theme.OneFamilyTheme
import com.example.ui.viewmodel.MerchantCategoryUpdatePrompt
import kotlinx.coroutines.launch

/**
 * Translucent quick-overlay Activity launched directly from the transaction notification's
 * "Category Wheel" action button. Displays the compact half-circle scroll wheel over any app
 * so the user can assign a category in one hand without opening the full app.
 */
class NotificationCategoryWheelActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LocalAccountSessionManager.ensureInitialized(applicationContext)
        com.example.data.local.LocalAccountDataStore.ensureInitialized(applicationContext)

        val expenseId = intent.getStringExtra(TransactionNotificationHelper.EXTRA_EDIT_EXPENSE_ID).orEmpty()
        val householdId = intent.getStringExtra(TransactionNotificationHelper.EXTRA_EDIT_HOUSEHOLD_ID).orEmpty()
        val visibility = intent.getStringExtra(TransactionNotificationHelper.EXTRA_EDIT_VISIBILITY).orEmpty()
        val initialCategoryExtra = intent.getStringExtra(TransactionNotificationHelper.EXTRA_EDIT_CURRENT_CATEGORY)
            .orEmpty()
            .ifBlank { "Unknown" }
        val notificationId = intent.getIntExtra(
            TransactionNotificationHelper.EXTRA_NOTIFICATION_ID,
            expenseId.hashCode()
        )
        val extraTitle = intent.getStringExtra(TransactionNotificationHelper.extraTitle).orEmpty()
        val extraAmount = intent.getStringExtra(TransactionNotificationHelper.extraAmount).orEmpty()

        if (expenseId.isBlank()) {
            finish()
            return
        }

        val repo = OneFamilyRepository(applicationContext)

        setContent {
            val customCategories by LocalAccountSessionManager.customCategories.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()

            var loadedTx by remember { mutableStateOf<ExpenseTransaction?>(null) }
            var currentCategory by remember { mutableStateOf(ExpenseCategories.normalizeCategory(initialCategoryExtra)) }
            var merchantTitle by remember { mutableStateOf(extraTitle) }
            var batchPrompt by remember { mutableStateOf<MerchantCategoryUpdatePrompt?>(null) }
            var isUpdating by remember { mutableStateOf(false) }

            LaunchedEffect(expenseId, householdId, visibility) {
                val tx = repo.getExpenseOnce(expenseId, householdId, visibility)
                if (tx != null) {
                    loadedTx = tx
                    currentCategory = ExpenseCategories.normalizeCategory(tx.category)
                    if (merchantTitle.isBlank()) {
                        merchantTitle = tx.title
                    }
                }
            }

            val isIncomeTx = remember(loadedTx, currentCategory) {
                loadedTx?.type?.equals("INCOME", ignoreCase = true)
                    ?: ExpenseCategories.isIncomeCategory(currentCategory)
            }

            val allCategories = remember(customCategories, currentCategory, isIncomeTx) {
                (ExpenseCategories.ALL + customCategories + listOf(currentCategory))
                    .map { ExpenseCategories.normalizeCategory(it) }
                    .filter { it.isNotBlank() }
                    .distinctBy { it.lowercase() }
                    .filter { cat ->
                        if (isIncomeTx) {
                            ExpenseCategories.isIncomeCategory(cat)
                        } else {
                            !ExpenseCategories.isIncomeCategory(cat)
                        }
                    }
                    .ifEmpty {
                        if (isIncomeTx) ExpenseCategories.INCOME_CATEGORIES else ExpenseCategories.EXPENSE_ONLY
                    }
            }

            OneFamilyTheme {
                if (batchPrompt == null) {
                    NotificationQuickCategoryWheelOverlay(
                        transactionTitle = merchantTitle.ifBlank { "Transaction" },
                        amountDisplay = extraAmount,
                        initialCategory = currentCategory,
                        availableCategories = allCategories,
                        onSelectCategory = { chosen ->
                            if (isUpdating) return@NotificationQuickCategoryWheelOverlay
                            isUpdating = true
                            val normalizedChosen = ExpenseCategories.normalizeCategory(chosen)
                            scope.launch {
                                repo.updateExpenseCategoryById(
                                    expenseId = expenseId,
                                    householdId = householdId,
                                    visibility = visibility,
                                    newCategory = normalizedChosen
                                )
                                TransactionNotificationHelper.postUpdatedCategoryNotification(
                                    context = applicationContext,
                                    notificationId = notificationId,
                                    title = merchantTitle,
                                    amountText = extraAmount,
                                    newCategory = normalizedChosen,
                                    expenseId = expenseId,
                                    householdId = householdId,
                                    visibility = visibility
                                )

                                val resolvedTx = loadedTx ?: ExpenseTransaction(
                                    id = expenseId,
                                    householdId = householdId,
                                    title = merchantTitle,
                                    category = currentCategory,
                                    visibility = visibility.ifBlank { "PERSONAL" },
                                    transactionDateMillis = System.currentTimeMillis()
                                )
                                val matches = if (!normalizedChosen.equals(currentCategory, ignoreCase = true)) {
                                    val allRecent = repo.getRecentExpensesOnce(householdId)
                                    MerchantCategoryUpdatePrompt.findMatchingOlderTransactions(
                                        allTransactions = allRecent,
                                        updatedExpense = resolvedTx,
                                        merchantTitle = merchantTitle.ifBlank { resolvedTx.title },
                                        newCategory = normalizedChosen
                                    )
                                } else {
                                    emptyList()
                                }

                                if (matches.isNotEmpty()) {
                                    batchPrompt = MerchantCategoryUpdatePrompt(
                                        merchantTitle = merchantTitle.ifBlank { resolvedTx.title },
                                        newCategory = normalizedChosen,
                                        matchingTransactions = matches
                                    )
                                } else {
                                    finish()
                                }
                            }
                        },
                        onDismiss = { finish() }
                    )
                } else {
                    MerchantCategoryBatchUpdateDialog(
                        prompt = batchPrompt!!,
                        currencySymbol = "₹",
                        onDismiss = {
                            batchPrompt = null
                            finish()
                        },
                        onConfirm = { selectedIds ->
                            val prompt = batchPrompt ?: return@MerchantCategoryBatchUpdateDialog
                            val chosen = prompt.matchingTransactions.filter { it.id in selectedIds }
                            scope.launch {
                                if (chosen.isNotEmpty()) {
                                    repo.batchUpdateExpenseCategories(chosen, prompt.newCategory)
                                }
                                batchPrompt = null
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }
}
