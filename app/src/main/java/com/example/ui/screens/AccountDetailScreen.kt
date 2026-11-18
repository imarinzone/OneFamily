package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountSectionType
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FinancialAccount
import com.example.ui.components.BillingCycleCalendarDialog
import com.example.ui.components.BillingCycleCalendarGrid
import com.example.ui.components.CircularTonalIconButton
import com.example.ui.components.ExpenseTransactionItemCard
import com.example.ui.components.PerforatedSectionHeader
import com.example.ui.components.formatCompactCurrency
import com.example.ui.components.formatCurrency
import com.example.ui.theme.BudgetDangerCoral
import com.example.ui.theme.BudgetSafeGreen
import com.example.ui.theme.BudgetWarnAmber
import com.example.ui.theme.LocalAppDimens
import com.example.ui.theme.ReceiptMonoFontFamily
import com.example.ui.theme.groupedCardShape
import com.example.ui.theme.rememberAppHapticPerformer
import com.example.ui.viewmodel.AccountLimitPeriodUtils
import com.example.ui.viewmodel.AccountSpendSummary
import com.example.ui.viewmodel.YearMonthPeriod

enum class AccountDetailPeriodMode {
    MONTHLY,
    BILLING_CYCLE
}

private data class AccountGraphBar(
    val key: String,
    val label: String,
    val spent: Double,
    val income: Double,
    val isSelected: Boolean,
    val onClick: () -> Unit
)

/**
 * Dedicated Account Detail Screen opened when a user clicks an account card on AccountsTabScreen.
 * Features:
 * - Period mode switcher: Calendar Monthly OR Credit Card / Account Billing Cycle (based on billingCycleDay 1..31)
 * - 6-period interactive Spend Trend Bar Chart + Daily Spend Sparkline Bars inside the active period
 * - Calendar View picker dialog (1..31) to customize the monthly billing cycle date
 * - All transactions for that account filtered to the selected Month or Billing Cycle period
 */
@Composable
fun AccountDetailScreen(
    summary: AccountSpendSummary,
    currencySymbol: String,
    availableCategories: List<String> = ExpenseCategories.ALL,
    referenceMillis: Long = System.currentTimeMillis(),
    onBackClick: () -> Unit = {},
    onSaveSpendLimitAndCycle: (account: FinancialAccount, monthlyLimit: Double, billingCycleDay: Int) -> Unit = { _, _, _ -> },
    onToggleAccountHidden: (AccountSpendSummary, Boolean) -> Unit = { _, _ -> },
    onDeleteOrUntrackAccount: (AccountSpendSummary) -> Unit = {},
    onEditTransaction: (ExpenseTransaction) -> Unit = {},
    onQuickChangeCategory: (ExpenseTransaction) -> Unit = {},
    onToggleExcludeTransaction: (ExpenseTransaction) -> Unit = {},
    onDeleteTransaction: (ExpenseTransaction) -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    val account = summary.account
    val isCreditCard = summary.section == AccountSectionType.CREDIT_CARD
    val hasBillingCycleMode = isCreditCard || account.billingCycleDay > 1

    var periodMode by rememberSaveable(account.id) {
        mutableStateOf(
            if (isCreditCard || account.billingCycleDay > 1) {
                AccountDetailPeriodMode.BILLING_CYCLE
            } else {
                AccountDetailPeriodMode.MONTHLY
            }
        )
    }
    var selectedMonthOffset by rememberSaveable(account.id) { mutableIntStateOf(0) }
    var selectedCycleOffset by rememberSaveable(account.id) { mutableIntStateOf(0) }
    var selectedTypeFilter by rememberSaveable(account.id) { mutableStateOf("All") }

    var showCycleCalendarDialog by remember { mutableStateOf(false) }
    var showLimitEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val historyTransactions = remember(summary.allTransactions, summary.transactions) {
        (if (summary.allTransactions.isNotEmpty()) summary.allTransactions else summary.transactions)
            .sortedByDescending { it.transactionDateMillis }
    }

    val currentMonthPeriod = remember(referenceMillis) { YearMonthPeriod.fromMillis(referenceMillis) }
    fun shiftMonthPeriod(base: YearMonthPeriod, offset: Int): YearMonthPeriod {
        var cur = base
        if (offset < 0) {
            repeat(-offset) { cur = cur.previousMonth() }
        } else if (offset > 0) {
            repeat(offset) { cur = cur.nextMonth() }
        }
        return cur
    }

    val activePeriodBounds = remember(
        periodMode,
        selectedMonthOffset,
        selectedCycleOffset,
        account.billingCycleDay,
        referenceMillis
    ) {
        if (periodMode == AccountDetailPeriodMode.BILLING_CYCLE) {
            AccountLimitPeriodUtils.computeBillingCycleBoundsForOffset(
                billingCycleDay = account.billingCycleDay,
                cycleOffset = selectedCycleOffset,
                referenceMillis = referenceMillis
            )
        } else {
            val targetYm = shiftMonthPeriod(currentMonthPeriod, selectedMonthOffset)
            com.example.ui.viewmodel.DateRangeFilterUtils.computeBoundsMillis(
                preset = com.example.ui.viewmodel.DateRangePreset.MONTH,
                selectedPeriod = targetYm,
                referenceMillis = referenceMillis
            ) ?: (0L to Long.MAX_VALUE)
        }
    }

    val activePeriodTitle = remember(
        periodMode,
        selectedMonthOffset,
        selectedCycleOffset,
        account.billingCycleDay,
        referenceMillis
    ) {
        if (periodMode == AccountDetailPeriodMode.BILLING_CYCLE) {
            AccountLimitPeriodUtils.formatBillingCycleRange(
                billingCycleDay = account.billingCycleDay,
                cycleOffset = selectedCycleOffset,
                referenceMillis = referenceMillis,
                includeYear = true
            )
        } else {
            shiftMonthPeriod(currentMonthPeriod, selectedMonthOffset).formatDisplay()
        }
    }

    fun isIncomeTx(tx: ExpenseTransaction): Boolean =
        tx.type.equals("INCOME", ignoreCase = true) || ExpenseCategories.isIncomeCategory(tx.category)

    val periodTransactions = remember(historyTransactions, activePeriodBounds) {
        historyTransactions.filter { tx ->
            tx.transactionDateMillis in activePeriodBounds.first..activePeriodBounds.second
        }
    }

    val filteredPeriodTransactions = remember(periodTransactions, selectedTypeFilter) {
        when (selectedTypeFilter.uppercase()) {
            "EXPENSE" -> periodTransactions.filter { !isIncomeTx(it) && !it.isExcludedFromExpense }
            "INCOME" -> periodTransactions.filter { isIncomeTx(it) && !it.isExcludedFromExpense }
            "EXCLUDED" -> periodTransactions.filter { it.isExcludedFromExpense }
            else -> periodTransactions
        }
    }

    val periodSpent = remember(periodTransactions) {
        periodTransactions.filter { !isIncomeTx(it) && !it.isExcludedFromExpense }.sumOf { it.amount }
    }
    val periodIncome = remember(periodTransactions) {
        periodTransactions.filter { isIncomeTx(it) && !it.isExcludedFromExpense }.sumOf { it.amount }
    }
    val excludedCount = remember(periodTransactions) {
        periodTransactions.count { it.isExcludedFromExpense }
    }
    val currentCycleBounds = remember(account.billingCycleDay, referenceMillis) {
        AccountLimitPeriodUtils.computeBillingCycleBoundsForOffset(
            billingCycleDay = account.billingCycleDay,
            cycleOffset = 0,
            referenceMillis = referenceMillis
        )
    }
    val currentCycleSpent = remember(historyTransactions, currentCycleBounds) {
        historyTransactions
            .filter { tx ->
                tx.transactionDateMillis in currentCycleBounds.first..currentCycleBounds.second &&
                    !isIncomeTx(tx) &&
                    !tx.isExcludedFromExpense
            }
            .sumOf { it.amount }
    }
    val currentCycleRangeTitle = remember(account.billingCycleDay, referenceMillis) {
        AccountLimitPeriodUtils.formatBillingCycleRange(
            billingCycleDay = account.billingCycleDay,
            cycleOffset = 0,
            referenceMillis = referenceMillis,
            includeYear = false
        )
    }

    // Build 6 consecutive period bars for the top interactive graph
    val graphBars = remember(
        historyTransactions,
        periodMode,
        selectedMonthOffset,
        selectedCycleOffset,
        account.billingCycleDay,
        referenceMillis
    ) {
        val anchorOffset = if (periodMode == AccountDetailPeriodMode.BILLING_CYCLE) {
            maxOf(0, selectedCycleOffset)
        } else {
            maxOf(0, selectedMonthOffset)
        }
        val windowOffsets = ((anchorOffset - 5)..anchorOffset).toList()
        windowOffsets.map { off ->
            if (periodMode == AccountDetailPeriodMode.BILLING_CYCLE) {
                val bounds = AccountLimitPeriodUtils.computeBillingCycleBoundsForOffset(
                    billingCycleDay = account.billingCycleDay,
                    cycleOffset = off,
                    referenceMillis = referenceMillis
                )
                val txsInPeriod = historyTransactions.filter { it.transactionDateMillis in bounds.first..bounds.second }
                AccountGraphBar(
                    key = "cycle_$off",
                    label = AccountLimitPeriodUtils.formatBillingCycleShortStart(
                        billingCycleDay = account.billingCycleDay,
                        cycleOffset = off,
                        referenceMillis = referenceMillis
                    ),
                    spent = txsInPeriod.filter { !isIncomeTx(it) && !it.isExcludedFromExpense }.sumOf { it.amount },
                    income = txsInPeriod.filter { isIncomeTx(it) && !it.isExcludedFromExpense }.sumOf { it.amount },
                    isSelected = off == selectedCycleOffset,
                    onClick = { selectedCycleOffset = off }
                )
            } else {
                val ym = shiftMonthPeriod(currentMonthPeriod, off)
                val bounds = com.example.ui.viewmodel.DateRangeFilterUtils.computeBoundsMillis(
                    preset = com.example.ui.viewmodel.DateRangePreset.MONTH,
                    selectedPeriod = ym,
                    referenceMillis = referenceMillis
                ) ?: (0L to Long.MAX_VALUE)
                val txsInPeriod = historyTransactions.filter { it.transactionDateMillis in bounds.first..bounds.second }
                AccountGraphBar(
                    key = "month_${ym.year}_${ym.month}",
                    label = ym.formatShortMonth(),
                    spent = txsInPeriod.filter { !isIncomeTx(it) && !it.isExcludedFromExpense }.sumOf { it.amount },
                    income = txsInPeriod.filter { isIncomeTx(it) && !it.isExcludedFromExpense }.sumOf { it.amount },
                    isSelected = off == selectedMonthOffset,
                    onClick = { selectedMonthOffset = off }
                )
            }
        }
    }

    // Daily spend buckets inside the active period for the inline Canvas bar chart
    val dailySpendValues = remember(periodTransactions, activePeriodBounds) {
        val spanDays = (((activePeriodBounds.second - activePeriodBounds.first) / 86_400_000L).toInt() + 1)
            .coerceIn(1, 31)
        val buckets = DoubleArray(spanDays)
        periodTransactions
            .filter { !isIncomeTx(it) && !it.isExcludedFromExpense }
            .forEach { tx ->
                val dayIndex = ((tx.transactionDateMillis - activePeriodBounds.first) / 86_400_000L)
                    .toInt()
                    .coerceIn(0, spanDays - 1)
                buckets[dayIndex] += tx.amount
            }
        buckets.toList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("account_detail_screen"),
        contentPadding = PaddingValues(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Top App Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularTonalIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Accounts",
                    onClick = onBackClick,
                    modifier = Modifier.testTag("account_detail_back_button")
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("account_detail_title")
                    )
                    val subtitle = buildList {
                        add(summary.section.title)
                        if (account.lastFourDigits.isNotBlank()) add("••${account.lastFourDigits}")
                        if (summary.linkedBankDisplayName.isNotBlank()) add("Linked ${summary.linkedBankDisplayName}")
                    }.joinToString(" • ")
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (isCreditCard || hasBillingCycleMode) {
                    CircularTonalIconButton(
                        icon = Icons.Default.CalendarMonth,
                        contentDescription = "Set Billing Cycle Date",
                        onClick = { showCycleCalendarDialog = true },
                        modifier = Modifier.testTag("account_detail_cycle_calendar_icon_button")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = {
                            performHaptic()
                            menuExpanded = true
                        },
                        modifier = Modifier.testTag("account_detail_menu_button")
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Account Actions")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isCreditCard) "Edit Limit & Billing Cycle" else "Edit Spend Limit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                showLimitEditDialog = true
                            }
                        )
                        if (isCreditCard) {
                            DropdownMenuItem(
                                text = { Text("Change Cycle Date (Day ${account.billingCycleDay})") },
                                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    showCycleCalendarDialog = true
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(if (summary.isHidden) "Show Account" else "Hide Account") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (summary.isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleAccountHidden(summary, !summary.isHidden)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete / Untrack", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        // 2. Period Mode Switcher (Monthly vs Account Cycle) + Period Stepper + Calendar Cycle Trigger
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasBillingCycleMode) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_detail_period_mode_switcher")
                    ) {
                        SegmentedButton(
                            selected = periodMode == AccountDetailPeriodMode.BILLING_CYCLE,
                            onClick = {
                                performHaptic()
                                periodMode = AccountDetailPeriodMode.BILLING_CYCLE
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            modifier = Modifier.testTag("account_detail_mode_billing_cycle")
                        ) {
                            Text(
                                text = "Billing Cycle (Day ${account.billingCycleDay})",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        SegmentedButton(
                            selected = periodMode == AccountDetailPeriodMode.MONTHLY,
                            onClick = {
                                performHaptic()
                                periodMode = AccountDetailPeriodMode.MONTHLY
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            modifier = Modifier.testTag("account_detail_mode_monthly")
                        ) {
                            Text(
                                text = "Monthly",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                performHaptic()
                                if (periodMode == AccountDetailPeriodMode.BILLING_CYCLE) {
                                    selectedCycleOffset -= 1
                                } else {
                                    selectedMonthOffset -= 1
                                }
                            },
                            modifier = Modifier.testTag("account_detail_prev_period_button")
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Period")
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = activePeriodTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("account_detail_period_label")
                            )
                            Text(
                                text = if (periodMode == AccountDetailPeriodMode.BILLING_CYCLE) {
                                    "Monthly Cycle • Starts Day ${account.billingCycleDay}"
                                } else {
                                    "Calendar Month View"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                performHaptic()
                                if (periodMode == AccountDetailPeriodMode.BILLING_CYCLE) {
                                    selectedCycleOffset += 1
                                } else {
                                    selectedMonthOffset += 1
                                }
                            },
                            modifier = Modifier.testTag("account_detail_next_period_button")
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Period")
                        }
                    }
                }

                if (isCreditCard) {
                    FilledTonalButton(
                        onClick = {
                            performHaptic()
                            showCycleCalendarDialog = true
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_detail_set_cycle_date_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cycle Date: Day ${account.billingCycleDay} • Change",
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 3. Interactive Account Graph Card (6-Period Trend Strip + Daily Activity Bars + Limit Tracker)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding)
                    .testTag("account_detail_graph_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Account Spend Graph",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (periodMode == AccountDetailPeriodMode.BILLING_CYCLE) {
                                    "Last 6 billing cycles • tap bar to switch"
                                } else {
                                    "Last 6 months • tap bar to switch"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = {
                                performHaptic()
                                showLimitEditDialog = true
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("account_detail_edit_limit_button")
                        ) {
                            Icon(
                                imageVector = if (summary.limit > 0.0) Icons.Default.Edit else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (summary.limit > 0.0) {
                                    formatCurrency(summary.limit, currencySymbol)
                                } else if (isCreditCard) {
                                    "Set Cycle Limit"
                                } else {
                                    "Set Limit"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    // 6-Period Interactive Bar Strip
                    val barScrollState = rememberScrollState()
                    val density = LocalDensity.current
                    val maxBarSpend = remember(graphBars) {
                        (graphBars.maxOfOrNull { it.spent } ?: 1.0).coerceAtLeast(1.0)
                    }
                    LaunchedEffect(graphBars) {
                        val selectedIdx = graphBars.indexOfFirst { it.isSelected }.takeIf { it >= 0 } ?: (graphBars.size - 1)
                        val scrollPx = with(density) { ((selectedIdx - 2).coerceAtLeast(0) * 60).dp.roundToPx() }
                        barScrollState.scrollTo(scrollPx)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(barScrollState)
                            .testTag("account_detail_period_bar_strip"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        graphBars.forEach { bar ->
                            val frac = (bar.spent / maxBarSpend).toFloat().coerceIn(0.08f, 1f)
                            val barHeight = (72.dp * frac).coerceAtLeast(10.dp)
                            val compactAmount = if (bar.spent > 0.0) {
                                formatCompactCurrency(bar.spent, currencySymbol)
                            } else {
                                "${currencySymbol}0"
                            }
                            Column(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(126.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (bar.isSelected) {
                                            MaterialTheme.colorScheme.surfaceContainerHigh
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                    .clickable {
                                        performHaptic()
                                        bar.onClick()
                                    }
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                                    .testTag("account_graph_bar_${bar.key}"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text(
                                    text = compactAmount,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = if (bar.isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (bar.isSelected) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            if (bar.isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = bar.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (bar.isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (bar.isSelected) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Daily Spend Curve/Bars for the Selected Period
                    val maxDaily = remember(dailySpendValues) {
                        (dailySpendValues.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
                    }
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Daily Activity in $activePeriodTitle",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("account_detail_daily_canvas")
                        ) {
                            val count = dailySpendValues.size.coerceAtLeast(1)
                            val slotWidth = size.width / count.toFloat()
                            val barW = (slotWidth * 0.58f).coerceAtLeast(3f)
                            dailySpendValues.forEachIndexed { idx, amt ->
                                val ratio = (amt / maxDaily).toFloat().coerceIn(0f, 1f)
                                val h = if (amt > 0.0) (size.height * ratio).coerceAtLeast(6f) else 4f
                                val x = idx * slotWidth + (slotWidth - barW) / 2f
                                val y = size.height - h
                                drawRoundRect(
                                    color = if (amt > 0.0) primaryColor else trackColor,
                                    topLeft = Offset(x, y),
                                    size = Size(barW, h),
                                    cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                                )
                            }
                        }
                    }

                    // Period Summary Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricTile(
                            label = "Spent",
                            value = formatCurrency(periodSpent, currencySymbol),
                            valueColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            label = "Income",
                            value = formatCurrency(periodIncome, currencySymbol),
                            valueColor = BudgetSafeGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            label = "Txns",
                            value = "${periodTransactions.size}",
                            valueColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(0.75f)
                        )
                    }

                    if (summary.limit > 0.0) {
                        val effectiveLimitSpent = if (isCreditCard && periodMode == AccountDetailPeriodMode.MONTHLY) {
                            currentCycleSpent
                        } else {
                            periodSpent
                        }
                        val pct = ((effectiveLimitSpent / summary.limit) * 100.0).toInt()
                        val barColor = when {
                            effectiveLimitSpent >= summary.limit -> BudgetDangerCoral
                            pct >= summary.alertThresholdPercent -> BudgetWarnAmber
                            else -> BudgetSafeGreen
                        }
                        LinearProgressIndicator(
                            progress = { (effectiveLimitSpent / summary.limit).toFloat().coerceIn(0f, 1f) },
                            color = barColor,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )
                        val diff = summary.limit - effectiveLimitSpent
                        val limitScopeSuffix = if (isCreditCard) {
                            val cycleRange = if (periodMode == AccountDetailPeriodMode.BILLING_CYCLE) {
                                activePeriodTitle
                            } else {
                                currentCycleRangeTitle
                            }
                            " • Cycle Day ${account.billingCycleDay} ($cycleRange)"
                        } else {
                            ""
                        }
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("account_detail_limit_status"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$pct% of ${formatCurrency(summary.limit, currencySymbol)} limit$limitScopeSuffix",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                color = barColor,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (diff >= 0.0) {
                                    "${formatCurrency(diff, currencySymbol)} left"
                                } else {
                                    "Over by ${formatCurrency(-diff, currencySymbol)}"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                color = if (diff >= 0.0) MaterialTheme.colorScheme.onSurfaceVariant else barColor,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 4. Filter Chips + Transactions List Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "All" to "All (${periodTransactions.size})",
                        "EXPENSE" to "Expenses",
                        "INCOME" to "Income",
                        "EXCLUDED" to if (excludedCount > 0) "Excluded ($excludedCount)" else "Excluded"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedTypeFilter.equals(key, ignoreCase = true),
                            onClick = {
                                performHaptic()
                                selectedTypeFilter = key
                            },
                            label = { Text(label) },
                            modifier = Modifier.testTag("account_detail_type_chip_${key.lowercase()}")
                        )
                    }
                }
                PerforatedSectionHeader(
                    title = "Account Transactions",
                    countBadge = "${filteredPeriodTransactions.size}"
                )
            }
        }

        // 5. All Transactions for That Account in Active Period
        if (filteredPeriodTransactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding)
                        .testTag("account_detail_empty_transactions")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No transactions in $activePeriodTitle",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Switch between Monthly and Billing Cycle or use the arrows above to browse earlier activity.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            itemsIndexed(
                items = filteredPeriodTransactions,
                key = { _, tx -> "acc_detail_tx_${tx.id}" }
            ) { idx, tx ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = dimens.horizontalPadding)
                        .testTag("account_detail_tx_row_${tx.id}")
                ) {
                    ExpenseTransactionItemCard(
                        expense = tx,
                        currencySymbol = currencySymbol,
                        onToggleExcludeTransaction = { onToggleExcludeTransaction(tx) },
                        onDelete = { onDeleteTransaction(tx) },
                        onEditTransaction = { onEditTransaction(tx) },
                        onQuickChangeCategory = { onQuickChangeCategory(tx) },
                        availableCategories = availableCategories,
                        shape = groupedCardShape(index = idx, count = filteredPeriodTransactions.size),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                }
            }
        }
    }

    if (showCycleCalendarDialog) {
        BillingCycleCalendarDialog(
            initialDay = account.billingCycleDay,
            accountName = account.name,
            referenceMillis = referenceMillis,
            onDismiss = { showCycleCalendarDialog = false },
            onConfirmDay = { newDay ->
                onSaveSpendLimitAndCycle(account, summary.limit, newDay)
                selectedCycleOffset = 0
                periodMode = AccountDetailPeriodMode.BILLING_CYCLE
                showCycleCalendarDialog = false
            }
        )
    }

    if (showLimitEditDialog) {
        var limitText by rememberSaveable(account.id, showLimitEditDialog) {
            mutableStateOf(
                if (summary.limit > 0.0) {
                    if (summary.limit % 1.0 == 0.0) summary.limit.toLong().toString() else summary.limit.toString()
                } else {
                    ""
                }
            )
        }
        var pickedCycleDay by rememberSaveable(account.id, showLimitEditDialog) {
            mutableIntStateOf(account.billingCycleDay.coerceIn(1, 31))
        }
        val cycleRangePreview = remember(pickedCycleDay, referenceMillis) {
            AccountLimitPeriodUtils.formatBillingCycleRange(
                billingCycleDay = pickedCycleDay,
                cycleOffset = 0,
                referenceMillis = referenceMillis,
                includeYear = false
            )
        }
        AlertDialog(
            onDismissRequest = { showLimitEditDialog = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.testTag("account_detail_limit_dialog"),
            title = {
                Column {
                    Text(
                        text = if (isCreditCard) "Credit Card Limit & Billing Cycle" else "Account Spend Limit",
                        fontWeight = FontWeight.Bold
                    )
                    if (isCreditCard) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { raw -> limitText = raw.filter { it.isDigit() || it == '.' } },
                        label = {
                            Text(if (isCreditCard) "Billing Cycle Limit ($currencySymbol)" else "Limit ($currencySymbol)")
                        },
                        placeholder = { Text("0 to clear") },
                        supportingText = if (isCreditCard) {
                            { Text("Linked to Day $pickedCycleDay cycle ($cycleRangePreview)") }
                        } else {
                            null
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val parsed = limitText.trim().toDoubleOrNull() ?: 0.0
                                onSaveSpendLimitAndCycle(account, parsed, pickedCycleDay)
                                if (isCreditCard && pickedCycleDay != account.billingCycleDay) {
                                    selectedCycleOffset = 0
                                }
                                showLimitEditDialog = false
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_detail_limit_input")
                    )
                    if (isCreditCard) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("account_detail_limit_cycle_link_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = "Billing Cycle Linked to Limit",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Limit tracks spend across Billing Cycle Day $pickedCycleDay ($cycleRangePreview)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        BillingCycleCalendarGrid(
                            selectedDay = pickedCycleDay,
                            onSelectDay = { pickedCycleDay = it },
                            referenceMillis = referenceMillis,
                            tagPrefix = "limit_dialog_billing_cycle_calendar"
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        performHaptic()
                        val parsed = limitText.trim().toDoubleOrNull() ?: 0.0
                        onSaveSpendLimitAndCycle(account, parsed, pickedCycleDay)
                        if (isCreditCard && pickedCycleDay != account.billingCycleDay) {
                            selectedCycleOffset = 0
                        }
                        showLimitEditDialog = false
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("account_detail_save_limit_button")
                ) {
                    Text(if (isCreditCard) "Save Limit & Cycle" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text("Delete / Untrack Account?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Remove ${account.name} from tracked accounts? Existing transactions stay intact.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        performHaptic()
                        onDeleteOrUntrackAccount(summary)
                        showDeleteDialog = false
                        onBackClick()
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Delete / Untrack")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
