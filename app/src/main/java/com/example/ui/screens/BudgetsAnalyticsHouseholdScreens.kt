package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Window
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LocalAccountSessionManager
import com.example.data.model.AccountSectionType
import com.example.data.model.BillReminder
import com.example.data.model.BudgetScope
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FamilyCurrencyOption
import com.example.data.model.Household
import com.example.data.model.NotificationSubCategory
import com.example.data.model.WallpaperColorAccent
import com.example.data.model.WallpaperDensity
import com.example.data.model.WallpaperPatternStyle
import com.example.ui.viewmodel.AccountLimitPeriodUtils
import com.example.ui.components.AccountsCarouselStrip
import com.example.ui.components.AxioMonthlySpendBarStrip
import com.example.ui.components.CategoryBadgeGlyph
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.CategorySubTabItem
import com.example.ui.components.CircularSpendBudgetRingGauge
import com.example.ui.components.CircularTonalIconButton
import com.example.ui.components.CustomDateRangePickerModal
import com.example.ui.components.ExpenseTransactionItemCard
import com.example.ui.components.ExpressiveCutoutAppearanceHeroCard
import com.example.ui.components.FinanceWallpaperPreviewBanner
import com.example.ui.components.M3CheckmarkSwitch
import com.example.ui.components.MasterFeatureToggleBanner
import com.example.ui.components.MerchantSubTabItem
import com.example.ui.components.MonthlyBarGraph
import com.example.ui.components.MonthlyBudgetPulseCard
import com.example.ui.components.PerforatedSectionHeader
import com.example.ui.components.PrimaryColorSelectorBar
import com.example.ui.components.ReceiptDashedDividerLine
import com.example.ui.components.ReceiptTicketCard
import com.example.ui.components.ScopeSwitcherBar
import com.example.ui.components.SquircleIconBadge
import com.example.ui.components.ThemeModeSelectorBar
import com.example.ui.components.TransactionCategoriesSubTabContent
import com.example.ui.components.TransactionMerchantsSubTabContent
import com.example.ui.components.TransactionPageSubTab
import com.example.ui.components.TransactionPageSubTabBar
import com.example.ui.components.categoryIcon
import com.example.ui.components.categoryWheelLongPressGesture
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatShortDate
import com.example.ui.components.rememberCustomCategoryEmojis
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.BudgetDangerCoral
import com.example.ui.theme.BudgetSafeGreen
import com.example.ui.theme.BudgetWarnAmber
import com.example.ui.theme.HouseholdBadgeIndigo
import com.example.ui.theme.LocalAppDimens
import com.example.ui.theme.ReceiptMonoFontFamily
import com.example.ui.theme.categoryAccentColor
import com.example.ui.theme.groupedCardShape
import com.example.ui.theme.rememberAppHapticPerformer
import com.example.ui.viewmodel.CategorySpendProgress
import com.example.ui.viewmodel.DateRangePreset
import com.example.ui.viewmodel.MonthlySpendBar
import com.example.ui.viewmodel.OneFamilyDashboardUiState
import com.example.ui.viewmodel.SmsSyncPeriodOption
import com.example.ui.viewmodel.YearMonthPeriod
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
private fun buildCategorySubTabItems(uiState: OneFamilyDashboardUiState): List<CategorySubTabItem> {
    return remember(uiState.activeExpenses, uiState.categoryProgressList) {
        val expenseTxs = uiState.activeExpenses.filter {
            !it.type.equals("INCOME", ignoreCase = true) &&
                !ExpenseCategories.isIncomeCategory(it.category) &&
                !it.isExcludedFromExpense
        }
        val countByCategory = expenseTxs.groupBy {
            ExpenseCategories.normalizeCategory(it.category).lowercase()
        }.mapValues { it.value.size }

        val fromProgress = uiState.categoryProgressList
            .filter { it.spent > 0.0 || it.limit > 0.0 }
            .map { prog ->
                val normKey = ExpenseCategories.normalizeCategory(prog.category).lowercase()
                CategorySubTabItem(
                    category = prog.category,
                    spent = prog.spent,
                    spendCount = countByCategory[normKey] ?: if (prog.spent > 0.0) 1 else 0,
                    budgetLimit = prog.limit
                )
            }

        if (fromProgress.isNotEmpty()) {
            fromProgress.sortedByDescending { it.spent }
        } else {
            expenseTxs.groupBy { ExpenseCategories.normalizeCategory(it.category) }
                .map { (cat, list) ->
                    CategorySubTabItem(
                        category = cat,
                        spent = list.sumOf { it.amount },
                        spendCount = list.size,
                        budgetLimit = 0.0
                    )
                }
                .sortedByDescending { it.spent }
        }
    }
}

@Composable
private fun buildIncomeCategorySubTabItems(uiState: OneFamilyDashboardUiState): List<CategorySubTabItem> {
    return remember(uiState.activeExpenses, uiState.incomeCategoryProgressList) {
        val incomeTxs = uiState.activeExpenses.filter {
            (it.type.equals("INCOME", ignoreCase = true) ||
                ExpenseCategories.isIncomeCategory(it.category)) &&
                !it.isExcludedFromExpense
        }
        val countByCategory = incomeTxs.groupBy {
            ExpenseCategories.normalizeCategory(it.category).lowercase()
        }.mapValues { it.value.size }

        val fromProgress = uiState.incomeCategoryProgressList
            .filter { it.spent > 0.0 }
            .map { prog ->
                val normKey = ExpenseCategories.normalizeCategory(prog.category).lowercase()
                CategorySubTabItem(
                    category = prog.category,
                    spent = prog.spent,
                    spendCount = countByCategory[normKey] ?: 1,
                    budgetLimit = 0.0
                )
            }

        if (fromProgress.isNotEmpty()) {
            fromProgress.sortedByDescending { it.spent }
        } else {
            incomeTxs.groupBy { ExpenseCategories.normalizeCategory(it.category) }
                .map { (cat, list) ->
                    CategorySubTabItem(
                        category = cat,
                        spent = list.sumOf { it.amount },
                        spendCount = list.size,
                        budgetLimit = 0.0
                    )
                }
                .sortedByDescending { it.spent }
        }
    }
}

@Composable
private fun buildMerchantSubTabItems(uiState: OneFamilyDashboardUiState): List<MerchantSubTabItem> {
    return remember(uiState.activeExpenses) {
        val expenseTxs = uiState.activeExpenses.filter { !it.isExcludedFromExpense }
        expenseTxs
            .groupBy {
                val clean = it.title.trim().ifBlank { "Unknown Merchant" }
                com.example.ui.viewmodel.MerchantCategoryUpdatePrompt.normalizeMerchantKey(clean)
            }
            .values
            .map { list ->
                val displayName = list.first().title.trim().ifBlank { "Unknown Merchant" }
                MerchantSubTabItem(
                    merchantName = displayName,
                    totalAmount = list.sumOf { it.amount },
                    transactionCount = list.size,
                    primaryCategory = list.firstOrNull()?.category ?: "Shopping"
                )
            }
            .sortedByDescending { it.totalAmount }
    }
}

private fun computeFilteredMonthlyBarsForDisplay(
    uiState: OneFamilyDashboardUiState
): Pair<List<MonthlySpendBar>, Boolean> {
    val hasNonDateFilter = uiState.searchQuery.isNotBlank() ||
        uiState.selectedTypeFilter != "All" ||
        uiState.selectedCategoryFilter != "All" ||
        uiState.selectedPaymentModeFilter != "All" ||
        uiState.selectedAccountFilter != "All" ||
        uiState.selectedTagFilter != "All" ||
        uiState.selectedMerchantFilter != "All" ||
        uiState.selectedMemberFilter != "All"

    if (!hasNonDateFilter) {
        return uiState.monthlyTrendBars to false
    }

    val bars = if (uiState.filteredMonthlyTrendBars.isNotEmpty()) {
        uiState.filteredMonthlyTrendBars
    } else if (uiState.monthlyTrendBars.isNotEmpty() && uiState.allScopedExpenses.isNotEmpty()) {
        val cleanTagFilter = uiState.selectedTagFilter.trim().removePrefix("#")
        val cleanMerchantKey = com.example.ui.viewmodel.MerchantCategoryUpdatePrompt.normalizeMerchantKey(
            uiState.selectedMerchantFilter
        )
        uiState.monthlyTrendBars.map { bar ->
            val monthTxs = uiState.allScopedExpenses.filter { tx ->
                if (YearMonthPeriod.fromMillis(tx.transactionDateMillis) != bar.period) return@filter false
                val isInc = tx.type.equals("INCOME", ignoreCase = true) ||
                    ExpenseCategories.isIncomeCategory(tx.category)
                val matchesQuery = uiState.searchQuery.isBlank() ||
                    tx.title.contains(uiState.searchQuery, ignoreCase = true) ||
                    tx.notes.contains(uiState.searchQuery, ignoreCase = true) ||
                    tx.category.contains(uiState.searchQuery, ignoreCase = true) ||
                    tx.accountName.contains(uiState.searchQuery, ignoreCase = true)
                val matchesType = when {
                    uiState.selectedTypeFilter == "All" -> true
                    uiState.selectedTypeFilter.equals("EXCLUDED", ignoreCase = true) -> tx.isExcludedFromExpense
                    uiState.selectedTypeFilter.equals("INCOME", ignoreCase = true) -> isInc && !tx.isExcludedFromExpense
                    uiState.selectedTypeFilter.equals("EXPENSE", ignoreCase = true) -> !isInc && !tx.isExcludedFromExpense
                    else -> tx.type.equals(uiState.selectedTypeFilter, ignoreCase = true)
                }
                val matchesCat = uiState.selectedCategoryFilter == "All" ||
                    ExpenseCategories.normalizeCategory(tx.category).equals(
                        ExpenseCategories.normalizeCategory(uiState.selectedCategoryFilter),
                        ignoreCase = true
                    )
                val matchesMode = uiState.selectedPaymentModeFilter == "All" ||
                    tx.paymentMode.equals(uiState.selectedPaymentModeFilter, ignoreCase = true)
                val matchesAcc = uiState.selectedAccountFilter == "All" ||
                    tx.accountName.equals(uiState.selectedAccountFilter, ignoreCase = true)
                val matchesTag = uiState.selectedTagFilter == "All" ||
                    tx.tags.any { it.trim().removePrefix("#").equals(cleanTagFilter, ignoreCase = true) } ||
                    tx.paymentMode.equals(cleanTagFilter, ignoreCase = true)
                val matchesMerchant = uiState.selectedMerchantFilter == "All" ||
                    com.example.ui.viewmodel.MerchantCategoryUpdatePrompt.normalizeMerchantKey(tx.title) == cleanMerchantKey
                val matchesMem = uiState.selectedMemberFilter == "All" ||
                    tx.userName.equals(uiState.selectedMemberFilter, ignoreCase = true)
                matchesQuery && matchesType && matchesCat && matchesMode && matchesAcc && matchesTag && matchesMerchant && matchesMem
            }
            val expSum = if (uiState.selectedTypeFilter.equals("EXCLUDED", ignoreCase = true)) {
                monthTxs.filter { it.isExcludedFromExpense }.sumOf { it.amount }
            } else {
                monthTxs.filter {
                    !(it.type.equals("INCOME", ignoreCase = true) || ExpenseCategories.isIncomeCategory(it.category)) &&
                        !it.isExcludedFromExpense
                }.sumOf { it.amount }
            }
            val incSum = if (uiState.selectedTypeFilter.equals("EXCLUDED", ignoreCase = true)) {
                monthTxs.filter {
                    (it.type.equals("INCOME", ignoreCase = true) || ExpenseCategories.isIncomeCategory(it.category)) &&
                        it.isExcludedFromExpense
                }.sumOf { it.amount }
            } else {
                monthTxs.filter {
                    (it.type.equals("INCOME", ignoreCase = true) || ExpenseCategories.isIncomeCategory(it.category)) &&
                        !it.isExcludedFromExpense
                }.sumOf { it.amount }
            }
            bar.copy(expenseTotal = expSum, incomeTotal = incSum)
        }
    } else {
        uiState.monthlyTrendBars
    }

    val useIncomeBars = uiState.selectedTypeFilter.equals("INCOME", ignoreCase = true) ||
        (uiState.selectedCategoryFilter != "All" && ExpenseCategories.isIncomeCategory(uiState.selectedCategoryFilter)) ||
        (uiState.selectedMerchantFilter != "All" && bars.all { it.expenseTotal == 0.0 } && bars.any { it.incomeTotal > 0.0 })

    return bars to useIncomeBars
}

@Composable
private fun ActiveDrilldownFilterPillsRow(
    selectedCategory: String,
    selectedTag: String,
    selectedMerchant: String,
    customEmojis: Map<String, String>,
    onClearCategory: () -> Unit,
    onClearTag: () -> Unit,
    onClearMerchant: () -> Unit,
    categoryPillTag: String = "active_category_filter_pill",
    tagPillTag: String = "active_tag_filter_pill",
    merchantPillTag: String = "active_merchant_filter_pill"
) {
    val performHaptic = rememberAppHapticPerformer()
    if (selectedCategory == "All" && selectedTag == "All" && selectedMerchant == "All") return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedCategory != "All") {
            val catAccent = categoryAccentColor(selectedCategory)
            Surface(
                shape = RoundedCornerShape(50),
                color = catAccent.copy(alpha = 0.16f),
                modifier = Modifier
                    .heightIn(min = 42.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        performHaptic()
                        onClearCategory()
                    }
                    .testTag(categoryPillTag)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryBadgeGlyph(
                        category = selectedCategory,
                        tint = catAccent,
                        iconSize = 18.dp,
                        customEmoji = customEmojis[selectedCategory.trim().lowercase()]
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedCategory,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = catAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Category Filter",
                        tint = catAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (selectedTag != "All") {
            val cleanTag = selectedTag.trim().removePrefix("#")
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF8AB4F8).copy(alpha = 0.22f),
                modifier = Modifier
                    .heightIn(min = 42.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        performHaptic()
                        onClearTag()
                    }
                    .testTag(tagPillTag)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#$cleanTag",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8AB4F8),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Tag Filter",
                        tint = Color(0xFF8AB4F8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (selectedMerchant != "All") {
            Surface(
                shape = RoundedCornerShape(50),
                color = BudgetSafeGreen.copy(alpha = 0.18f),
                modifier = Modifier
                    .heightIn(min = 42.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        performHaptic()
                        onClearMerchant()
                    }
                    .testTag(merchantPillTag)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedMerchant,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = BudgetSafeGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Merchant Filter",
                        tint = BudgetSafeGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineCategoryBudgetDialog(
    category: String,
    currentLimit: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSaveLimit: (Double) -> Unit
) {
    var inputText by rememberSaveable(category) {
        mutableStateOf(if (currentLimit > 0.0) currentLimit.roundToInt().toString() else "")
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Budget for $category", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Set a monthly spend limit for $category ($currencySymbol).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Monthly Limit ($currencySymbol)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inline_category_budget_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = inputText.toDoubleOrNull() ?: 0.0
                    if (parsed > 0.0) {
                        onSaveLimit(parsed)
                    }
                    onDismiss()
                },
                modifier = Modifier.testTag("inline_category_budget_save_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Home Screen (`HomeTabScreen`):
 * - Personal & Family section switcher (`ScopeSwitcherBar`) when Household mode is enabled.
 * - Serrated Thermal Paper Receipt Budget Pulse Card.
 * - Monthly Trend Card (`MonthlyBarGraph`) moved onto the Home page.
 * - Bill Reminders Card (`home_bill_reminders_card`) surfacing Credit Card billing cycle reminders,
 *   recurring bill/utility reminders, and user-added bill reminders.
 * - Active Set Limits Card (`home_active_limits_card`) surfacing all configured limits:
 *   Overall Monthly Budget, Account/Credit Card Spend Limits (with billing cycle), and Category Budgets.
 */
@Composable
fun HomeTabScreen(
    uiState: OneFamilyDashboardUiState,
    onScopeSelected: (BudgetScope) -> Unit,
    onStepMonth: (Int) -> Unit = {},
    onSelectPeriod: (YearMonthPeriod?) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSelectDateRangePreset: (DateRangePreset) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSelectCustomDateRange: (Long, Long) -> Unit = { _, _ -> },
    onEditMonthlyBudget: () -> Unit = {},
    onAccountClick: (String) -> Unit = {},
    onNavigateToAccounts: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSearchQueryChange: (String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSelectType: (String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSelectCategory: (String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSelectPaymentMode: (String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSelectAccount: (String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSelectTag: (String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSelectMerchant: (String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSelectMember: (String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSelectSortOrder: (String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onClearFilters: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onScanSmsClick: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onExportCsvClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit = {},
    onAcceptEmailInvite: (Household) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onEditTransaction: (ExpenseTransaction) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onQuickChangeCategory: (ExpenseTransaction) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onToggleTransactionPrivacy: (ExpenseTransaction) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onToggleExcludeTransaction: (ExpenseTransaction) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onLinkDuplicateTransaction: (ExpenseTransaction) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onDeleteTransaction: (ExpenseTransaction) -> Unit = {},
    onUndoLastSwipeAction: () -> Unit = {},
    onOpenAllTransactionsPage: () -> Unit = {},
    onSaveCategoryBudgetInline: (String, Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val dimens = LocalAppDimens.current
    val currency = uiState.activeCurrencySymbol
    val performHaptic = rememberAppHapticPerformer()
    val customEmojis = rememberCustomCategoryEmojis()

    val savedBillReminders by LocalAccountSessionManager.billReminders.collectAsStateWithLifecycle()
    var showAddBillReminderDialog by rememberSaveable { mutableStateOf(false) }
    var editingBudgetForCategory by remember { mutableStateOf<Pair<String, Double>?>(null) }

    // 1. Credit Card Billing Cycle Reminders (linked to credit card limits & billing cycle day)
    val creditCardReminders = remember(uiState.accountSummaries) {
        uiState.accountSummaries.filter { summary ->
            !summary.account.isHidden && (
                summary.section == AccountSectionType.CREDIT_CARD ||
                    summary.account.billingCycleDay > 1
                )
        }
    }

    // 2. Recurring Bills & Utilities / Rent reminders auto-derived from expenses (excluding duplicates in savedBillReminders)
    val autoDetectedBillExpenses = remember(uiState.allScopedExpenses, savedBillReminders) {
        val customTitles = savedBillReminders.map { it.title.trim().lowercase() }.toSet()
        uiState.allScopedExpenses
            .filter { tx ->
                !tx.isExcludedFromExpense &&
                    !tx.type.equals("INCOME", ignoreCase = true) && (
                    tx.category.equals("Bills & Utilities", ignoreCase = true) ||
                        tx.category.equals("Rent & Housing", ignoreCase = true)
                    ) &&
                    tx.title.trim().lowercase() !in customTitles
            }
            .sortedByDescending { it.transactionDateMillis }
            .distinctBy { it.title.trim().lowercase() }
            .take(4)
    }

    // 3. Configured Set Limits (Overall Monthly Budget + Account/Card Spend Limits + Category Budgets)
    val activeAccountLimits = remember(uiState.accountSummaries) {
        uiState.accountSummaries.filter { !it.account.isHidden && it.limit > 0.0 }
    }
    val activeCategoryLimits = remember(uiState.categoryProgressList) {
        uiState.categoryProgressList.filter { it.limit > 0.0 }
    }
    val totalActiveLimitsCount = (if (uiState.activeMonthlyBudget > 0.0) 1 else 0) +
        activeAccountLimits.size +
        activeCategoryLimits.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_tab_list"),
        contentPadding = PaddingValues(top = 6.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 1. Personal & Family Section Switcher (Shown ONLY on Home when Household feature is enabled in Settings)
        if (uiState.householdFeatureEnabled) {
            item {
                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                    ScopeSwitcherBar(
                        selectedScope = uiState.selectedScope,
                        hasHousehold = uiState.household != null,
                        householdName = uiState.household?.name,
                        onScopeSelected = onScopeSelected
                    )
                }
            }
        }

        // 2. Pending Email Invites Banner (Shown ONLY when Household feature is enabled)
        if (uiState.householdFeatureEnabled && uiState.pendingEmailInvites.isNotEmpty()) {
            item {
                val invite = uiState.pendingEmailInvites.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(dimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SquircleIconBadge(
                            icon = Icons.Default.MarkEmailUnread,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                            size = 42.dp,
                            iconSize = 20.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = invite.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                performHaptic()
                                onAcceptEmailInvite(invite)
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("accept_invite_button")
                        ) {
                            Text("Join")
                        }
                    }
                }
            }
        }

        // 3. Calm Rhythm-Inspired Editorial Hero Card
        item {
            Box(modifier = Modifier.padding(bottom = 12.dp)) {
                MonthlyBudgetPulseCard(
                    totalSpent = uiState.totalMonthlySpent,
                    totalIncome = uiState.totalMonthlyIncome,
                    monthlyBudget = uiState.activeMonthlyBudget,
                    dailySafeSpend = uiState.dailySafeSpend,
                    remainingDays = uiState.remainingDaysInMonth,
                    currencySymbol = currency,
                    periodLabel = uiState.periodLabel,
                    household = uiState.household,
                    monthlyTrendBars = uiState.monthlyTrendBars,
                    transactionsCount = uiState.filteredExpenses.size,
                    onOpenTransactionsClick = onOpenAllTransactionsPage,
                    onQuickAddClick = onAddTransactionClick,
                    onEditBudgetClick = onEditMonthlyBudget
                )
            }
        }

        // 4. Quick Undo Bar on Home Screen when a transaction was just swiped
        if (uiState.canUndoLastAction) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding)
                        .padding(bottom = 10.dp)
                        .testTag("home_undo_swipe_bar"),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.statusBannerMessage ?: "Swipe action applied.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        TextButton(
                            onClick = {
                                performHaptic()
                                onUndoLastSwipeAction()
                            },
                            modifier = Modifier.testTag("home_undo_swipe_button")
                        ) {
                            Text(
                                text = "Undo Swipe",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // 5. Clean Monthly Trend Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding)
                    .padding(bottom = 12.dp)
                    .testTag("home_monthly_trend_card"),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(modifier = Modifier.padding(dimens.cardPadding)) {
                    Text(
                        text = "Monthly Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MonthlyBarGraph(
                        bars = uiState.monthlyTrendBars,
                        currencySymbol = currency,
                        overallBudget = uiState.activeMonthlyBudget,
                        onSelectMonth = { onSelectPeriod(it) },
                        onStepMonth = onStepMonth
                    )
                }
            }
        }

        // 6. Clean Bill Reminders Section
        item {
            val totalRemindersCount = creditCardReminders.size + savedBillReminders.size + autoDetectedBillExpenses.size
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding)
                    .padding(bottom = 12.dp)
                    .testTag("home_bill_reminders_card"),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bill Reminders",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        FilledTonalButton(
                            onClick = {
                                performHaptic()
                                showAddBillReminderDialog = true
                            },
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("home_add_bill_reminder_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Bill Reminder",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }

                    if (totalRemindersCount == 0) {
                        Text(
                            text = "No upcoming bills",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        val todayDay = remember {
                            java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
                        }
                        var renderedBillRows = 0

                        // A. Credit Card Billing Cycle Reminders
                        creditCardReminders.forEach { summary ->
                            if (renderedBillRows > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                            }
                            renderedBillRows++
                            val acc = summary.account
                            val cycleDay = acc.billingCycleDay.coerceIn(1, 31)
                            val cycleRange = remember(cycleDay) {
                                AccountLimitPeriodUtils.formatBillingCycleRange(cycleDay)
                            }
                            val safeAccId = acc.id.ifBlank { acc.name }.lowercase().replace(Regex("[^a-z0-9]+"), "_")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        performHaptic()
                                        onAccountClick(acc.id)
                                    }
                                    .padding(vertical = 8.dp)
                                    .testTag("home_bill_reminder_item_$safeAccId"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Cycle Day $cycleDay · $cycleRange",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formatCurrency(summary.spent, currency),
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.isOverLimit) BudgetDangerCoral else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // B. User-Added Custom Bill Reminders
                        savedBillReminders.forEach { reminder ->
                            if (renderedBillRows > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                            }
                            renderedBillRows++
                            val safeRemId = reminder.id.lowercase().replace(Regex("[^a-z0-9]+"), "_")
                            val daysDiff = if (reminder.dueDayOfMonth >= todayDay) {
                                reminder.dueDayOfMonth - todayDay
                            } else {
                                (30 - todayDay) + reminder.dueDayOfMonth
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("home_bill_reminder_item_$safeRemId"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = reminder.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (daysDiff == 0) {
                                            "Due today · Day ${reminder.dueDayOfMonth}"
                                        } else {
                                            "Due Day ${reminder.dueDayOfMonth} · in ${daysDiff}d"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (reminder.amount > 0.0) {
                                        Text(
                                            text = formatCurrency(reminder.amount, currency),
                                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            performHaptic()
                                            LocalAccountSessionManager.deleteBillReminder(context, reminder.id)
                                        },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .size(48.dp)
                                            .testTag("home_delete_bill_reminder_$safeRemId")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Bill Reminder",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // C. Auto-Detected Recurring Bills & Utilities
                        autoDetectedBillExpenses.forEach { billTx ->
                            if (renderedBillRows > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                            }
                            renderedBillRows++
                            val billDay = remember(billTx.transactionDateMillis) {
                                java.util.Calendar.getInstance().apply {
                                    timeInMillis = billTx.transactionDateMillis
                                }.get(java.util.Calendar.DAY_OF_MONTH)
                            }
                            val safeBillId = billTx.id.lowercase().replace(Regex("[^a-z0-9]+"), "_")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        performHaptic()
                                        onOpenAllTransactionsPage()
                                    }
                                    .padding(vertical = 8.dp)
                                    .testTag("home_bill_reminder_item_$safeBillId"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = billTx.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${billTx.category} · Day $billDay",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formatCurrency(billTx.amount, currency),
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Clean Active Limits Section
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding)
                    .padding(bottom = 12.dp)
                    .testTag("home_active_limits_card"),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Limits",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            TextButton(
                                onClick = {
                                    performHaptic()
                                    onEditMonthlyBudget()
                                },
                                modifier = Modifier.testTag("home_manage_limits_button")
                            ) {
                                Text("Budgets", fontWeight = FontWeight.SemiBold)
                            }
                            TextButton(
                                onClick = {
                                    performHaptic()
                                    onNavigateToAccounts()
                                },
                                modifier = Modifier.testTag("home_manage_account_limits_button")
                            ) {
                                Text("Cards", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    if (totalActiveLimitsCount == 0) {
                        Text(
                            text = "No limits set yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        var renderedLimitRows = 0

                        // A. Overall Monthly Budget Limit
                        if (uiState.activeMonthlyBudget > 0.0) {
                            renderedLimitRows++
                            val ratio = (uiState.totalMonthlySpent / uiState.activeMonthlyBudget).toFloat().coerceIn(0f, 1f)
                            val pct = ((uiState.totalMonthlySpent / uiState.activeMonthlyBudget) * 100.0).roundToInt()
                            val barColor = when {
                                uiState.totalMonthlySpent > uiState.activeMonthlyBudget -> BudgetDangerCoral
                                pct >= 80 -> BudgetWarnAmber
                                else -> BudgetSafeGreen
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        performHaptic()
                                        onEditMonthlyBudget()
                                    }
                                    .padding(vertical = 6.dp)
                                    .testTag("home_limit_item_overall_budget"),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                @OptIn(ExperimentalLayoutApi::class)
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Monthly Budget",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${formatCurrency(uiState.totalMonthlySpent, currency)} / ${formatCurrency(uiState.activeMonthlyBudget, currency)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                        fontWeight = FontWeight.Bold,
                                        color = barColor,
                                        maxLines = 1
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    color = barColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                            }
                        }

                        // B. Account & Credit Card Spend Limits
                        activeAccountLimits.forEach { summary ->
                            if (renderedLimitRows > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                            }
                            renderedLimitRows++
                            val acc = summary.account
                            val safeAccId = acc.id.ifBlank { acc.name }.lowercase().replace(Regex("[^a-z0-9]+"), "_")
                            val ratio = (summary.spent / summary.limit.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
                            val barColor = when {
                                summary.isOverLimit -> BudgetDangerCoral
                                summary.isNearAlert -> BudgetWarnAmber
                                else -> BudgetSafeGreen
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        performHaptic()
                                        onAccountClick(acc.id)
                                    }
                                    .padding(vertical = 6.dp)
                                    .testTag("home_limit_item_account_$safeAccId"),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                @OptIn(ExperimentalLayoutApi::class)
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${formatCurrency(summary.spent, currency)} / ${formatCurrency(summary.limit, currency)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                        fontWeight = FontWeight.Bold,
                                        color = barColor,
                                        maxLines = 1
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    color = barColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                            }
                        }

                        // C. Category Budget Limits
                        activeCategoryLimits.forEach { catItem ->
                            if (renderedLimitRows > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                            }
                            renderedLimitRows++
                            val safeCatId = catItem.category.lowercase().replace(Regex("[^a-z0-9]+"), "_")
                            val ratio = (catItem.spent / catItem.limit.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
                            val pct = ((catItem.spent / catItem.limit.coerceAtLeast(1.0)) * 100.0).roundToInt()
                            val barColor = when {
                                catItem.spent > catItem.limit -> BudgetDangerCoral
                                pct >= catItem.alertThresholdPercent -> BudgetWarnAmber
                                else -> BudgetSafeGreen
                            }
                            val catColor = categoryAccentColor(catItem.category)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        performHaptic()
                                        editingBudgetForCategory = catItem.category to catItem.limit
                                    }
                                    .padding(vertical = 6.dp)
                                    .testTag("home_limit_item_category_$safeCatId"),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                @OptIn(ExperimentalLayoutApi::class)
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CategoryBadgeGlyph(
                                            category = catItem.category,
                                            tint = catColor,
                                            iconSize = 16.dp,
                                            customEmoji = customEmojis[catItem.category.trim().lowercase()]
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = catItem.category,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "${formatCurrency(catItem.spent, currency)} / ${formatCurrency(catItem.limit, currency)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                        fontWeight = FontWeight.Bold,
                                        color = barColor,
                                        maxLines = 1
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    color = barColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddBillReminderDialog) {
        var billTitle by rememberSaveable { mutableStateOf("") }
        var billAmount by rememberSaveable { mutableStateOf("") }
        var billDueDay by rememberSaveable { mutableStateOf("5") }
        AlertDialog(
            onDismissRequest = { showAddBillReminderDialog = false },
            title = { Text("Add Bill Reminder", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = billTitle,
                        onValueChange = { billTitle = it.take(60) },
                        label = { Text("Bill Name (e.g. Electricity, Rent, Broadband)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bill_reminder_title_input")
                    )
                    OutlinedTextField(
                        value = billAmount,
                        onValueChange = { billAmount = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Amount ($currency)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bill_reminder_amount_input")
                    )
                    OutlinedTextField(
                        value = billDueDay,
                        onValueChange = { billDueDay = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("Due Day of Month (1 - 31)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bill_reminder_due_day_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val titleClean = billTitle.trim()
                        if (titleClean.isNotBlank()) {
                            LocalAccountSessionManager.addOrUpdateBillReminder(
                                context = context,
                                reminder = BillReminder(
                                    title = titleClean,
                                    amount = billAmount.toDoubleOrNull() ?: 0.0,
                                    dueDayOfMonth = (billDueDay.toIntOrNull() ?: 1).coerceIn(1, 31)
                                )
                            )
                            showAddBillReminderDialog = false
                        }
                    },
                    modifier = Modifier.testTag("bill_reminder_save_button")
                ) {
                    Text("Save Reminder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBillReminderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (editingBudgetForCategory != null) {
        val (catName, currentLim) = editingBudgetForCategory!!
        InlineCategoryBudgetDialog(
            category = catName,
            currentLimit = currentLim,
            currencySymbol = currency,
            onDismiss = { editingBudgetForCategory = null },
            onSaveLimit = { newLimit ->
                onSaveCategoryBudgetInline(catName, newLimit)
            }
        )
    }
}

/**
 * Dedicated All Transactions Page (`AllTransactionsScreen`):
 * Matches the reference screenshots:
 * - Header (`All transactions` + period dropdown + Accounts, Filter, Export, Add buttons).
 * - Horizontally scrollable Monthly Spend Bar Strip (`May`, `Jun`, `Jul`, `Aug`, `Sep`, `Oct`, `Nov`).
 * - 3 Sub-Tabs (`Transactions` | `Categories` | `Merchants`) with glowing green indicator.
 * - `Transactions` sub-tab: Circular Spend/Budget Ring Gauge + clickable `Income | Budget` columns + clickable `#Tag`, Category, Merchant, and Direction badges on every transaction.
 * - `Categories` sub-tab: Multi-Segment Donut Chart + clickable percentage legend + `Spends | Budget` summary + Category Cards with `"N Spends"` and `"Set budget >"` / `"of ₹X >"`.
 * - `Merchants` sub-tab: Merchant Cards with green storefront icon, Merchant Name, `"N Spends"`, and total spend.
 */
@Composable
fun AllTransactionsScreen(
    uiState: OneFamilyDashboardUiState,
    onBackClick: () -> Unit = {},
    onScopeSelected: (BudgetScope) -> Unit = {},
    onSelectPeriod: (YearMonthPeriod?) -> Unit = {},
    onSelectDateRangePreset: (DateRangePreset) -> Unit = {},
    onSelectCustomDateRange: (Long, Long) -> Unit = { _, _ -> },
    onSearchQueryChange: (String) -> Unit = {},
    onSelectType: (String) -> Unit = {},
    onSelectCategory: (String) -> Unit = {},
    onSelectPaymentMode: (String) -> Unit = {},
    onSelectAccount: (String) -> Unit = {},
    onSelectTag: (String) -> Unit = {},
    onSelectMerchant: (String) -> Unit = {},
    onSelectSortOrder: (String) -> Unit = {},
    onClearFilters: () -> Unit = {},
    onExportCsvClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit = {},
    onEditMonthlyBudget: () -> Unit = {},
    onAddCustomCategoryClick: () -> Unit = {},
    onSaveCategoryBudgetInline: (String, Double) -> Unit = { _, _ -> },
    onEditTransaction: (ExpenseTransaction) -> Unit = {},
    onQuickChangeCategory: (ExpenseTransaction) -> Unit = {},
    onToggleTransactionPrivacy: (ExpenseTransaction) -> Unit = {},
    onToggleExcludeTransaction: (ExpenseTransaction) -> Unit = {},
    onLinkDuplicateTransaction: (ExpenseTransaction) -> Unit = {},
    onDeleteTransaction: (ExpenseTransaction) -> Unit = {},
    onUndoLastSwipeAction: () -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    val currency = uiState.activeCurrencySymbol
    val performHaptic = rememberAppHapticPerformer()

    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    var monthStripVisible by rememberSaveable { mutableStateOf(true) }
    var showCustomRangePicker by rememberSaveable { mutableStateOf(false) }
    var selectedSubTab by rememberSaveable { mutableStateOf(TransactionPageSubTab.TRANSACTIONS) }
    var editingBudgetForCategory by remember { mutableStateOf<Pair<String, Double>?>(null) }

    val hasActiveFilter = uiState.searchQuery.isNotBlank() ||
        uiState.selectedDateRangePreset != DateRangePreset.MONTH ||
        uiState.selectedTypeFilter != "All" ||
        uiState.selectedCategoryFilter != "All" ||
        uiState.selectedPaymentModeFilter != "All" ||
        uiState.selectedAccountFilter != "All" ||
        uiState.selectedTagFilter != "All" ||
        uiState.selectedMerchantFilter != "All" ||
        uiState.selectedMemberFilter != "All" ||
        uiState.selectedSortOrder != "NEWEST"

    val customEmojis = rememberCustomCategoryEmojis()
    val categoryItems = buildCategorySubTabItems(uiState)
    val incomeCategoryItems = buildIncomeCategorySubTabItems(uiState)
    val merchantItems = buildMerchantSubTabItems(uiState)
    val (displayMonthlyBars, useIncomeMonthlyBars) = remember(
        uiState.monthlyTrendBars,
        uiState.filteredMonthlyTrendBars,
        uiState.allScopedExpenses,
        uiState.searchQuery,
        uiState.selectedTypeFilter,
        uiState.selectedCategoryFilter,
        uiState.selectedPaymentModeFilter,
        uiState.selectedAccountFilter,
        uiState.selectedTagFilter,
        uiState.selectedMerchantFilter,
        uiState.selectedMemberFilter
    ) {
        computeFilteredMonthlyBarsForDisplay(uiState)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("all_transactions_screen_list"),
        contentPadding = PaddingValues(top = 6.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 0. Clean Top Navigation Bar (Back + Bar Chart Toggle + Search/Filter/Export/Add; no duplicate page title or month/year)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularTonalIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            onClick = {
                                performHaptic()
                                onBackClick()
                            },
                            modifier = Modifier.testTag("all_transactions_back_button")
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .heightIn(min = 42.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    performHaptic()
                                    monthStripVisible = !monthStripVisible
                                }
                                .testTag("all_transactions_period_dropdown_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${uiState.filteredExpenses.size} items",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = if (monthStripVisible) {
                                        Icons.Default.ExpandLess
                                    } else {
                                        Icons.Default.ExpandMore
                                    },
                                    contentDescription = "Toggle Monthly Spend Bars",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CircularTonalIconButton(
                            icon = Icons.Default.Search,
                            contentDescription = "Search Transactions",
                            onClick = { filtersExpanded = !filtersExpanded },
                            containerColor = if (uiState.searchQuery.isNotBlank()) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            contentColor = if (uiState.searchQuery.isNotBlank()) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.testTag("all_transactions_toggle_search_button")
                        )
                        CircularTonalIconButton(
                            icon = Icons.Default.FilterList,
                            contentDescription = "Toggle Filters",
                            onClick = { filtersExpanded = !filtersExpanded },
                            containerColor = if (filtersExpanded || hasActiveFilter) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            contentColor = if (filtersExpanded || hasActiveFilter) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.testTag("all_transactions_toggle_filters_button")
                        )
                        CircularTonalIconButton(
                            icon = Icons.Default.Download,
                            contentDescription = "Export CSV",
                            onClick = onExportCsvClick,
                            modifier = Modifier.testTag("all_transactions_export_csv_button")
                        )
                        CircularTonalIconButton(
                            icon = Icons.Default.Add,
                            contentDescription = "Add Transaction",
                            onClick = onAddTransactionClick,
                            modifier = Modifier.testTag("all_transactions_add_button")
                        )
                    }
                }

                // 0B. Horizontally Scrollable Monthly Spend Bar Strip (updates amounts when category/merchant selected)
                AnimatedVisibility(visible = monthStripVisible && displayMonthlyBars.isNotEmpty()) {
                    AxioMonthlySpendBarStrip(
                        bars = displayMonthlyBars,
                        currencySymbol = currency,
                        onSelectMonth = { onSelectPeriod(it) },
                        useIncomeBars = useIncomeMonthlyBars
                    )
                }

                // 0C. 3 Sub-Tabs directly matching reference screenshot: Transactions | Categories | Merchants
                TransactionPageSubTabBar(
                    selectedTab = selectedSubTab,
                    onSelectTab = { selectedSubTab = it },
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        // 1. Optional Household Scope Switcher
        if (uiState.householdFeatureEnabled) {
            item {
                Box(modifier = Modifier.padding(bottom = 6.dp)) {
                    ScopeSwitcherBar(
                        selectedScope = uiState.selectedScope,
                        hasHousehold = uiState.household != null,
                        householdName = uiState.household?.name,
                        onScopeSelected = onScopeSelected
                    )
                }
            }
        }

        // 2. Active Drilldown Pills + Collapsible Search & Filter Drawer (kept compact until toggled)
        if (uiState.selectedCategoryFilter != "All" ||
            uiState.selectedTagFilter != "All" ||
            uiState.selectedMerchantFilter != "All" ||
            filtersExpanded ||
            uiState.searchQuery.isNotBlank()
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding)
                        .padding(top = 4.dp, bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActiveDrilldownFilterPillsRow(
                        selectedCategory = uiState.selectedCategoryFilter,
                        selectedTag = uiState.selectedTagFilter,
                        selectedMerchant = uiState.selectedMerchantFilter,
                        customEmojis = customEmojis,
                        onClearCategory = { onSelectCategory("All") },
                        onClearTag = { onSelectTag("All") },
                        onClearMerchant = { onSelectMerchant("All") },
                        categoryPillTag = "all_tx_active_category_pill",
                        tagPillTag = "all_tx_active_tag_pill",
                        merchantPillTag = "all_tx_active_merchant_pill"
                    )

                    AnimatedVisibility(visible = filtersExpanded || uiState.searchQuery.isNotBlank()) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = onSearchQueryChange,
                                    placeholder = { Text("Search merchant, category, account, or tag...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    trailingIcon = {
                                        if (uiState.searchQuery.isNotBlank()) {
                                            IconButton(onClick = { onSearchQueryChange("") }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("all_transactions_search_input")
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf(
                                        "All" to "All (${uiState.activeExpenses.size})",
                                        "EXPENSE" to "Expenses",
                                        "INCOME" to "Income",
                                        "EXCLUDED" to if (uiState.excludedTransactionsCount > 0) {
                                            "Excluded (${uiState.excludedTransactionsCount})"
                                        } else {
                                            "Excluded"
                                        }
                                    ).forEach { (typeVal, label) ->
                                        FilterChip(
                                            selected = uiState.selectedTypeFilter.equals(typeVal, ignoreCase = true),
                                            onClick = {
                                                performHaptic()
                                                onSelectType(typeVal)
                                            },
                                            leadingIcon = if (typeVal == "EXCLUDED") {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Default.VisibilityOff,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            } else {
                                                null
                                            },
                                            label = { Text(label) },
                                            modifier = Modifier.testTag("all_tx_type_chip_${typeVal.lowercase()}")
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DateRangePreset.entries.forEach { preset ->
                                        FilterChip(
                                            selected = uiState.selectedDateRangePreset == preset,
                                            onClick = {
                                                performHaptic()
                                                if (preset == DateRangePreset.CUSTOM) {
                                                    showCustomRangePicker = true
                                                } else {
                                                    onSelectDateRangePreset(preset)
                                                }
                                            },
                                            label = {
                                                Text(
                                                    if (preset == DateRangePreset.CUSTOM &&
                                                        uiState.selectedDateRangePreset == DateRangePreset.CUSTOM
                                                    ) {
                                                        uiState.periodLabel
                                                    } else {
                                                        preset.label
                                                    }
                                                )
                                            },
                                            modifier = Modifier.testTag("all_tx_date_chip_${preset.name.lowercase()}")
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    (listOf("All") + uiState.availableCategories).forEach { cat ->
                                        FilterChip(
                                            selected = uiState.selectedCategoryFilter.equals(cat, ignoreCase = true),
                                            onClick = {
                                                performHaptic()
                                                onSelectCategory(cat)
                                            },
                                            label = { Text(cat) }
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("All", "UPI", "BANK", "CARD", "WALLET", "CASH").forEach { mode ->
                                        FilterChip(
                                            selected = uiState.selectedPaymentModeFilter.equals(mode, ignoreCase = true),
                                            onClick = {
                                                performHaptic()
                                                onSelectPaymentMode(mode)
                                            },
                                            label = { Text(if (mode == "All") "All Modes" else mode) }
                                        )
                                    }
                                    listOf(
                                        "NEWEST" to "Newest",
                                        "OLDEST" to "Oldest",
                                        "HIGHEST" to "Highest $currency",
                                        "LOWEST" to "Lowest $currency"
                                    ).forEach { (sortKey, sortLabel) ->
                                        FilterChip(
                                            selected = uiState.selectedSortOrder == sortKey,
                                            onClick = {
                                                performHaptic()
                                                onSelectSortOrder(sortKey)
                                            },
                                            label = { Text(sortLabel) }
                                        )
                                    }
                                }

                                if (hasActiveFilter) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                performHaptic()
                                                onClearFilters()
                                            },
                                            modifier = Modifier.testTag("all_tx_clear_filters_button")
                                        ) {
                                            Text("Reset")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Undo Bar if applicable
        if (uiState.canUndoLastAction) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding)
                        .padding(bottom = 8.dp)
                        .testTag("all_tx_undo_swipe_bar"),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.statusBannerMessage ?: "Action applied.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onUndoLastSwipeAction) {
                            Text("Undo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Sub-Tab Content: Transactions | Categories | Merchants
        when (selectedSubTab) {
            TransactionPageSubTab.CATEGORIES -> {
                item {
                    TransactionCategoriesSubTabContent(
                        categories = categoryItems,
                        totalSpent = uiState.totalMonthlySpent,
                        monthlyBudget = uiState.activeMonthlyBudget,
                        currencySymbol = currency,
                        selectedCategory = uiState.selectedCategoryFilter,
                        onCategoryClick = { clickedCat ->
                            val nextCat = if (uiState.selectedCategoryFilter.equals(clickedCat, ignoreCase = true)) {
                                "All"
                            } else {
                                clickedCat
                            }
                            onSelectCategory(nextCat)
                        },
                        onSetCategoryBudgetClick = { cat, curLimit ->
                            editingBudgetForCategory = cat to curLimit
                        },
                        onEditMonthlyBudgetClick = onEditMonthlyBudget,
                        onAddCustomCategoryClick = onAddCustomCategoryClick,
                        incomeCategories = incomeCategoryItems,
                        totalIncome = uiState.totalMonthlyIncome
                    )
                }

                if (uiState.selectedCategoryFilter != "All") {
                    item {
                        PerforatedSectionHeader(
                            title = "${uiState.selectedCategoryFilter} Transactions",
                            countBadge = "${uiState.filteredExpenses.size}",
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                    }
                    if (uiState.filteredExpenses.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = dimens.horizontalPadding, vertical = 6.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "No transactions in ${uiState.selectedCategoryFilter}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    FilledTonalButton(
                                        onClick = { onSelectCategory("All") },
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("Show All Categories")
                                    }
                                }
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = uiState.filteredExpenses,
                            key = { _, tx -> "cat_subtab_${tx.visibility}_${tx.id}" }
                        ) { idx, expense ->
                            Box(
                                modifier = Modifier.padding(
                                    horizontal = dimens.horizontalPadding,
                                    vertical = 1.5.dp
                                )
                            ) {
                                ExpenseTransactionItemCard(
                                    expense = expense,
                                    currencySymbol = currency,
                                    canTogglePrivacy = uiState.householdFeatureEnabled && uiState.household != null,
                                    onTogglePrivacy = { onToggleTransactionPrivacy(expense) },
                                    onToggleExcludeTransaction = { onToggleExcludeTransaction(expense) },
                                    onLinkDuplicate = { onLinkDuplicateTransaction(expense) },
                                    onDelete = { onDeleteTransaction(expense) },
                                    onEditTransaction = { onEditTransaction(expense) },
                                    onQuickChangeCategory = { onQuickChangeCategory(expense) },
                                    availableCategories = uiState.availableCategories,
                                    onTagClick = { clickedTag -> onSelectTag(clickedTag) },
                                    shape = groupedCardShape(index = idx, count = uiState.filteredExpenses.size),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            }
                        }
                    }
                }
            }

            TransactionPageSubTab.MERCHANTS -> {
                item {
                    TransactionMerchantsSubTabContent(
                        merchants = merchantItems,
                        currencySymbol = currency,
                        selectedMerchant = uiState.selectedMerchantFilter,
                        onMerchantClick = { clickedMerchant ->
                            val nextMerchant = if (uiState.selectedMerchantFilter.equals(clickedMerchant, ignoreCase = true)) {
                                "All"
                            } else {
                                clickedMerchant
                            }
                            onSelectMerchant(nextMerchant)
                        }
                    )
                }

                if (uiState.selectedMerchantFilter != "All") {
                    item {
                        PerforatedSectionHeader(
                            title = "${uiState.selectedMerchantFilter} Transactions",
                            countBadge = "${uiState.filteredExpenses.size}",
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                    }
                    if (uiState.filteredExpenses.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = dimens.horizontalPadding, vertical = 6.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "No transactions for ${uiState.selectedMerchantFilter}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    FilledTonalButton(
                                        onClick = { onSelectMerchant("All") },
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("Show All Merchants")
                                    }
                                }
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = uiState.filteredExpenses,
                            key = { _, tx -> "merchant_subtab_${tx.visibility}_${tx.id}" }
                        ) { idx, expense ->
                            Box(
                                modifier = Modifier.padding(
                                    horizontal = dimens.horizontalPadding,
                                    vertical = 1.5.dp
                                )
                            ) {
                                ExpenseTransactionItemCard(
                                    expense = expense,
                                    currencySymbol = currency,
                                    canTogglePrivacy = uiState.householdFeatureEnabled && uiState.household != null,
                                    onTogglePrivacy = { onToggleTransactionPrivacy(expense) },
                                    onToggleExcludeTransaction = { onToggleExcludeTransaction(expense) },
                                    onLinkDuplicate = { onLinkDuplicateTransaction(expense) },
                                    onDelete = { onDeleteTransaction(expense) },
                                    onEditTransaction = { onEditTransaction(expense) },
                                    onQuickChangeCategory = { onQuickChangeCategory(expense) },
                                    availableCategories = uiState.availableCategories,
                                    onTagClick = { clickedTag -> onSelectTag(clickedTag) },
                                    shape = groupedCardShape(index = idx, count = uiState.filteredExpenses.size),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            }
                        }
                    }
                }
            }

            TransactionPageSubTab.TRANSACTIONS -> {
                // Circular Spend / Budget Ring Gauge + Clickable Income | Budget Summary
                item {
                    CircularSpendBudgetRingGauge(
                        totalSpent = uiState.totalMonthlySpent,
                        totalIncome = uiState.totalMonthlyIncome,
                        monthlyBudget = uiState.activeMonthlyBudget,
                        currencySymbol = currency,
                        selectedTypeFilter = uiState.selectedTypeFilter,
                        onSpendRingClick = {
                            if (uiState.selectedTypeFilter.equals("EXPENSE", ignoreCase = true)) {
                                onSelectType("All")
                            } else {
                                onSelectType("EXPENSE")
                            }
                        },
                        onIncomeClick = {
                            if (uiState.selectedTypeFilter.equals("INCOME", ignoreCase = true)) {
                                onSelectType("All")
                            } else {
                                onSelectType("INCOME")
                            }
                        },
                        onBudgetClick = onEditMonthlyBudget
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.horizontalPadding)
                            .padding(bottom = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = uiState.selectedTypeFilter.equals("All", ignoreCase = true),
                            onClick = {
                                performHaptic()
                                onSelectType("All")
                            },
                            label = { Text("All (${uiState.activeExpenses.size})") },
                            modifier = Modifier.testTag("quick_type_filter_chip_all")
                        )
                        FilterChip(
                            selected = uiState.selectedTypeFilter.equals("EXPENSE", ignoreCase = true),
                            onClick = {
                                performHaptic()
                                onSelectType("EXPENSE")
                            },
                            label = { Text("Expenses") },
                            modifier = Modifier.testTag("quick_type_filter_chip_expense")
                        )
                        FilterChip(
                            selected = uiState.selectedTypeFilter.equals("INCOME", ignoreCase = true),
                            onClick = {
                                performHaptic()
                                onSelectType("INCOME")
                            },
                            label = { Text("Income") },
                            modifier = Modifier.testTag("quick_type_filter_chip_income")
                        )
                        if (uiState.excludedTransactionsCount > 0 ||
                            uiState.selectedTypeFilter.equals("EXCLUDED", ignoreCase = true)
                        ) {
                            FilterChip(
                                selected = uiState.selectedTypeFilter.equals("EXCLUDED", ignoreCase = true),
                                onClick = {
                                    performHaptic()
                                    if (uiState.selectedTypeFilter.equals("EXCLUDED", ignoreCase = true)) {
                                        onSelectType("All")
                                    } else {
                                        onSelectType("EXCLUDED")
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text("Excluded (${uiState.excludedTransactionsCount})") },
                                modifier = Modifier.testTag("quick_excluded_filter_chip")
                            )
                        }
                    }
                }

                item {
                    PerforatedSectionHeader(
                        title = if (uiState.selectedTypeFilter.equals("EXCLUDED", ignoreCase = true)) {
                            "Excluded Transactions"
                        } else {
                            "Receipts Ledger"
                        },
                        countBadge = "${uiState.filteredExpenses.size}",
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                if (uiState.filteredExpenses.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimens.horizontalPadding, vertical = 6.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (uiState.selectedTypeFilter.equals("EXCLUDED", ignoreCase = true)) {
                                        "No excluded transactions"
                                    } else {
                                        "No matching transactions"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (hasActiveFilter) {
                                    FilledTonalButton(
                                        onClick = onClearFilters,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("Reset Filters")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    itemsIndexed(
                        items = uiState.filteredExpenses,
                        key = { _, tx -> "all_page_${tx.visibility}_${tx.id}" }
                    ) { idx, expense ->
                        Box(
                            modifier = Modifier.padding(
                                horizontal = dimens.horizontalPadding,
                                vertical = 1.5.dp
                            )
                        ) {
                            ExpenseTransactionItemCard(
                                expense = expense,
                                currencySymbol = currency,
                                canTogglePrivacy = uiState.householdFeatureEnabled && uiState.household != null,
                                onTogglePrivacy = { onToggleTransactionPrivacy(expense) },
                                onToggleExcludeTransaction = { onToggleExcludeTransaction(expense) },
                                onLinkDuplicate = { onLinkDuplicateTransaction(expense) },
                                onDelete = { onDeleteTransaction(expense) },
                                onEditTransaction = { onEditTransaction(expense) },
                                onQuickChangeCategory = { onQuickChangeCategory(expense) },
                                availableCategories = uiState.availableCategories,
                                onTagClick = { clickedTag -> onSelectTag(clickedTag) },
                                shape = groupedCardShape(index = idx, count = uiState.filteredExpenses.size),
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        }
                    }
                }
            }
        }
    }

    if (editingBudgetForCategory != null) {
        val (catName, currentLim) = editingBudgetForCategory!!
        InlineCategoryBudgetDialog(
            category = catName,
            currentLimit = currentLim,
            currencySymbol = currency,
            onDismiss = { editingBudgetForCategory = null },
            onSaveLimit = { newLimit ->
                onSaveCategoryBudgetInline(catName, newLimit)
            }
        )
    }

    if (showCustomRangePicker) {
        CustomDateRangePickerModal(
            initialStartMillis = uiState.customStartDateMillis,
            initialEndMillis = uiState.customEndDateMillis,
            onDismiss = { showCustomRangePicker = false },
            onApplyRange = { start, end ->
                onSelectCustomDateRange(start, end)
            }
        )
    }
}

/**
 * Unified Settings, Appearance & Family Household Screen (`HouseholdTabScreen`):
 * - Editorial "Settings" Header + Asymmetric Top-Right Cutout Appearance Hero Card (Screenshot 6).
 * - Connected 3-Card Visual Style Stack: App Theme (`Light | Auto | Dark`), AMOLED Mode, Haptic Feedback.
 * - Full-Width Stadium Master Toggle Banner for Family Household (Screenshot 1).
 * - Connected 6-Card Account, Currency, Accounts, SMS Sync, CSV Export & Diagnostics Stack (Screenshots 3 & 7).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdTabScreen(
    uiState: OneFamilyDashboardUiState,
    currentUserName: String = "",
    currentUserEmail: String = "",
    onUpdateUserName: (String) -> Unit = {},
    onUpdatePreferredCurrency: (String) -> Unit = {},
    onAddAccountClick: () -> Unit = {},
    onScanSmsClick: () -> Unit = {},
    smsSyncPeriodOption: SmsSyncPeriodOption = SmsSyncPeriodOption.ALL_TIME,
    smsSyncCustomRange: Pair<Long, Long>? = null,
    onSelectSmsSyncPeriod: (SmsSyncPeriodOption) -> Unit = {},
    onSelectSmsSyncCustomRange: (Long, Long) -> Unit = { _, _ -> },
    onExportCsvClick: () -> Unit = {},
    onSendTestFcmAlertClick: () -> Unit = {},
    onRunStabilityCheckClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    isDeletingAccount: Boolean = false,
    onDeleteAccountAndDataClick: () -> Unit = {},
    onCreateHousehold: (String, Double) -> Unit,
    onJoinByCode: (String) -> Unit,
    onInviteByEmail: (String) -> Unit,
    onAcceptEmailInvite: (Household) -> Unit,
    initialCategory: SettingsCategory? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val performHaptic = rememberAppHapticPerformer()
    val systemDark = isSystemInDarkTheme()

    val currentThemeMode by LocalAccountSessionManager.themeMode.collectAsStateWithLifecycle()
    val currentPrimaryOption by LocalAccountSessionManager.primaryColorOption.collectAsStateWithLifecycle()
    val customPrimaryColorArgb by LocalAccountSessionManager.customPrimaryColorArgb.collectAsStateWithLifecycle()
    val amoledDarkEnabled by LocalAccountSessionManager.amoledDarkEnabled.collectAsStateWithLifecycle()
    val hapticFeedbackEnabled by LocalAccountSessionManager.hapticFeedbackEnabled.collectAsStateWithLifecycle()
    val wallpaperSettings by LocalAccountSessionManager.wallpaperSettings.collectAsStateWithLifecycle()
    val fcmPushSettings by LocalAccountSessionManager.fcmPushSettings.collectAsStateWithLifecycle()
    val crashlyticsSettings by LocalAccountSessionManager.crashlyticsSettings.collectAsStateWithLifecycle()
    val expertFeatureFlags by LocalAccountSessionManager.expertFeatureFlags.collectAsStateWithLifecycle()
    val householdFeatureEnabled by LocalAccountSessionManager.householdFeatureEnabled.collectAsStateWithLifecycle()
    val currency = uiState.activeCurrencySymbol

    val isDarkActive = when (currentThemeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> systemDark
    }

    val displayName = uiState.userProfile?.displayName?.takeIf { it.isNotBlank() }
        ?: currentUserName.ifBlank { "Member" }

    var selectedCategory by rememberSaveable(initialCategory) { mutableStateOf(initialCategory) }
    var showDeleteAccountConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showSmsCustomRangePicker by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = selectedCategory != null) {
        selectedCategory = null
    }

    if (showSmsCustomRangePicker) {
        CustomDateRangePickerModal(
            initialStartMillis = smsSyncCustomRange?.first,
            initialEndMillis = smsSyncCustomRange?.second,
            onDismiss = { showSmsCustomRangePicker = false },
            onApplyRange = { startMillis, endMillis ->
                onSelectSmsSyncCustomRange(startMillis, endMillis)
                showSmsCustomRangePicker = false
            }
        )
    }

    if (showDeleteAccountConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeletingAccount) {
                    showDeleteAccountConfirmDialog = false
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Permanent Deletion Warning",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Account & All Data?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This permanently deletes all your personal expenses, budgets, financial accounts, and profile from Firebase and this device, removes your household membership (or deletes the household if you are the sole member), and deletes your account. Data is permanently erased and cannot be recovered.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        performHaptic()
                        showDeleteAccountConfirmDialog = false
                        onDeleteAccountAndDataClick()
                    },
                    enabled = !isDeletingAccount,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_delete_account_confirm_button")
                ) {
                    Text("Delete Forever", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        performHaptic()
                        showDeleteAccountConfirmDialog = false
                    },
                    enabled = !isDeletingAccount,
                    modifier = Modifier.testTag("confirm_delete_account_cancel_button")
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.testTag("confirm_delete_account_dialog")
        )
    }

    AnimatedContent(
        targetState = selectedCategory,
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it / 4 } + fadeOut())
            } else {
                (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { it / 4 } + fadeOut())
            }
        },
        label = "settings_category_navigation"
    ) { activeCategory ->
        when (activeCategory) {
            null -> SettingsCategoryListPage(
                displayName = displayName,
                currentUserEmail = currentUserEmail,
                isDarkActive = isDarkActive,
                householdFeatureEnabled = householdFeatureEnabled,
                activeHouseholdName = uiState.household?.name,
                smsSyncPeriodOption = smsSyncPeriodOption,
                smsSyncCustomRange = smsSyncCustomRange,
                currency = currency,
                expertModeEnabled = expertFeatureFlags.expertModeEnabled,
                onSelectCategory = { selectedCategory = it }
            )

            SettingsCategory.ACCOUNT -> AccountSettingsPage(
                displayName = displayName,
                currentUserEmail = currentUserEmail,
                onUpdateUserName = onUpdateUserName,
                onSignOutClick = onSignOutClick,
                isDeletingAccount = isDeletingAccount,
                onRequestDeleteAccount = { showDeleteAccountConfirmDialog = true },
                onBackClick = { selectedCategory = null }
            )

            SettingsCategory.APPEARANCE -> AppearanceSettingsPage(
                isDarkActive = isDarkActive,
                currentThemeMode = currentThemeMode,
                currentPrimaryOption = currentPrimaryOption,
                customPrimaryColorArgb = customPrimaryColorArgb,
                amoledDarkEnabled = amoledDarkEnabled,
                hapticFeedbackEnabled = hapticFeedbackEnabled,
                wallpaperSettings = wallpaperSettings,
                onBackClick = { selectedCategory = null }
            )

            SettingsCategory.FAMILY -> FamilyHouseholdSettingsPage(
                uiState = uiState,
                currency = currency,
                householdFeatureEnabled = householdFeatureEnabled,
                clipboardManager = clipboardManager,
                onCreateHousehold = onCreateHousehold,
                onJoinByCode = onJoinByCode,
                onInviteByEmail = onInviteByEmail,
                onAcceptEmailInvite = onAcceptEmailInvite,
                onBackClick = { selectedCategory = null }
            )

            SettingsCategory.NOTIFICATIONS -> NotificationsSettingsPage(
                fcmPushSettings = fcmPushSettings,
                onBackClick = { selectedCategory = null }
            )

            SettingsCategory.SMS_SYNC -> SmsSyncSettingsPage(
                smsSyncPeriodOption = smsSyncPeriodOption,
                smsSyncCustomRange = smsSyncCustomRange,
                onSelectSmsSyncPeriod = onSelectSmsSyncPeriod,
                onOpenCustomRangePicker = { showSmsCustomRangePicker = true },
                onScanSmsClick = onScanSmsClick,
                onBackClick = { selectedCategory = null }
            )

            SettingsCategory.CURRENCY_DATA -> CurrencyAndExportSettingsPage(
                currency = currency,
                onUpdatePreferredCurrency = onUpdatePreferredCurrency,
                onExportCsvClick = onExportCsvClick,
                onBackClick = { selectedCategory = null }
            )

            SettingsCategory.ADVANCED -> AdvancedSettingsPage(
                expertFeatureFlags = expertFeatureFlags,
                crashlyticsSettings = crashlyticsSettings,
                onBackClick = { selectedCategory = null }
            )
        }
    }
}
