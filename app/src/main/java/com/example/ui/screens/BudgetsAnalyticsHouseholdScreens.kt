package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LocalAccountSessionManager
import com.example.data.model.BudgetScope
import com.example.data.model.ExpenseTransaction
import com.example.data.model.Household
import com.example.ui.components.AccountsCarouselStrip
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.CustomDateRangePickerModal
import com.example.ui.components.ExpenseTransactionItemCard
import com.example.ui.components.MonthPeriodSelectorBar
import com.example.ui.components.MonthlyBarGraph
import com.example.ui.components.MonthlyBudgetPulseCard
import com.example.ui.components.ReceiptDashedDividerLine
import com.example.ui.components.ReceiptTicketCard
import com.example.ui.components.ScopeSwitcherBar
import com.example.ui.components.ThemeModeSelectorBar
import com.example.ui.components.categoryIcon
import com.example.ui.components.formatCurrency
import com.example.ui.theme.BudgetDangerCoral
import com.example.ui.theme.BudgetSafeGreen
import com.example.ui.theme.BudgetWarnAmber
import com.example.ui.theme.HouseholdBadgeIndigo
import com.example.ui.theme.categoryAccentColor
import com.example.ui.viewmodel.AxioDashboardUiState
import com.example.ui.viewmodel.CategorySpendProgress
import com.example.ui.viewmodel.DateRangePreset
import com.example.ui.viewmodel.YearMonthPeriod

@Composable
fun BudgetsTabScreen(
    uiState: AxioDashboardUiState,
    onStepMonth: (Int) -> Unit,
    onSelectPeriod: (YearMonthPeriod?) -> Unit,
    onSelectDateRangePreset: (DateRangePreset) -> Unit = {},
    onSelectCustomDateRange: (Long, Long) -> Unit = { _, _ -> },
    onEditOverallBudgetClick: () -> Unit,
    onAddOrEditCategoryBudgetClick: (CategorySpendProgress?) -> Unit
) {
    val currency = uiState.userProfile?.currencySymbol ?: "₹"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("budgets_tab_list"),
        contentPadding = PaddingValues(0.dp, 8.dp, 0.dp, 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            MonthPeriodSelectorBar(
                selectedPeriod = uiState.selectedPeriod,
                periodLabel = uiState.periodLabel,
                onStepMonth = onStepMonth,
                onSelectCurrentMonth = { onSelectPeriod(YearMonthPeriod.current()) },
                onToggleAllTime = {
                    if (uiState.selectedDateRangePreset == DateRangePreset.ALL_TIME) {
                        onSelectDateRangePreset(DateRangePreset.MONTH)
                    } else {
                        onSelectDateRangePreset(DateRangePreset.ALL_TIME)
                    }
                },
                selectedDateRangePreset = uiState.selectedDateRangePreset,
                onSelectDateRangePreset = onSelectDateRangePreset,
                onSelectMonthPeriod = { onSelectPeriod(it) },
                customStartDateMillis = uiState.customStartDateMillis,
                customEndDateMillis = uiState.customEndDateMillis,
                onSelectCustomDateRange = onSelectCustomDateRange
            )
        }

        // Receipt Ticket Summary for Monthly Budgets (Single-Line Household & Personal Pills)
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ReceiptTicketCard(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Budgets",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = onEditOverallBudgetClick,
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("configure_overall_budget_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Monthly Budget",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        ReceiptDashedDividerLine()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Household",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        maxLines = 1
                                    )
                                    val hhLimit = uiState.household?.monthlyBudgetLimit ?: 0.0
                                    Text(
                                        text = if (hhLimit > 0.0) {
                                            formatCurrency(hhLimit, currency)
                                        } else {
                                            "Not set"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        maxLines = 1
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Personal",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 1
                                    )
                                    val pLimit = uiState.userProfile?.personalMonthlyBudget ?: 0.0
                                    Text(
                                        text = if (pLimit > 0.0) {
                                            formatCurrency(pLimit, currency)
                                        } else {
                                            "Not set"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { onAddOrEditCategoryBudgetClick(null) },
                    modifier = Modifier.testTag("add_category_budget_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Set Category Limit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        items(
            items = uiState.categoryProgressList,
            key = { it.category }
        ) { item ->
            val catColor = categoryAccentColor(item.category)
            val barColor = when {
                item.isOverBudget -> BudgetDangerCoral
                item.isNearAlert -> BudgetWarnAmber
                else -> BudgetSafeGreen
            }
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onAddOrEditCategoryBudgetClick(item) },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        // Single-line Category Row: Icon + Name + Amount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(catColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = categoryIcon(item.category),
                                    contentDescription = item.category,
                                    tint = catColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = formatCurrency(item.spent, currency),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (item.limit > 0 && item.isOverBudget) barColor else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (item.limit > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { item.progressFraction.coerceIn(0f, 1f) },
                                color = barColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Unified Analysis Dashboard & Receipts Screen:
 * Combines all features of the Analysis screen, Dashboard pulse card, and Receipts Ledger
 * into one cohesive, gesture-enabled screen.
 */
@Composable
fun AnalyticsTabScreen(
    uiState: AxioDashboardUiState,
    onStepMonth: (Int) -> Unit,
    onSelectPeriod: (YearMonthPeriod?) -> Unit,
    onSelectDateRangePreset: (DateRangePreset) -> Unit = {},
    onSelectCustomDateRange: (Long, Long) -> Unit = { _, _ -> },
    onEditMonthlyBudget: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSelectType: (String) -> Unit = {},
    onSelectCategory: (String) -> Unit = {},
    onSelectPaymentMode: (String) -> Unit = {},
    onSelectAccount: (String) -> Unit = {},
    onSelectTag: (String) -> Unit = {},
    onSelectMember: (String) -> Unit = {},
    onSelectSortOrder: (String) -> Unit = {},
    onClearFilters: () -> Unit = {},
    onScanSmsClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit = {},
    onAcceptEmailInvite: (Household) -> Unit = {},
    onSelectCategoryAndOpenLedger: (String) -> Unit = {},
    onEditTransaction: (ExpenseTransaction) -> Unit = {},
    onQuickChangeCategory: (ExpenseTransaction) -> Unit = {},
    onToggleTransactionPrivacy: (ExpenseTransaction) -> Unit = {},
    onDeleteTransaction: (ExpenseTransaction) -> Unit = {}
) {
    val currency = uiState.userProfile?.currencySymbol ?: "₹"
    val categoryTotals = uiState.categoryProgressList
        .filter { it.spent > 0 }
        .map { it.category to it.spent }

    var moreBreakdownsExpanded by rememberSaveable { mutableStateOf(false) }
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    var showDrawerCustomRangePicker by rememberSaveable { mutableStateOf(false) }

    val activePieCategory = uiState.selectedCategoryFilter.takeIf { it != "All" }

    val hasActiveFilter = uiState.searchQuery.isNotBlank() ||
        uiState.selectedTypeFilter != "All" ||
        uiState.selectedCategoryFilter != "All" ||
        uiState.selectedPaymentModeFilter != "All" ||
        uiState.selectedAccountFilter != "All" ||
        uiState.selectedTagFilter != "All" ||
        uiState.selectedMemberFilter != "All" ||
        uiState.selectedSortOrder != "NEWEST"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analytics_tab_list"),
        contentPadding = PaddingValues(0.dp, 8.dp, 0.dp, 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Smoothly Scrollable Month & Custom Date Range Bar
        item {
            MonthPeriodSelectorBar(
                selectedPeriod = uiState.selectedPeriod,
                periodLabel = uiState.periodLabel,
                onStepMonth = onStepMonth,
                onSelectCurrentMonth = { onSelectPeriod(YearMonthPeriod.current()) },
                onToggleAllTime = {
                    if (uiState.selectedDateRangePreset == DateRangePreset.ALL_TIME) {
                        onSelectDateRangePreset(DateRangePreset.MONTH)
                    } else {
                        onSelectDateRangePreset(DateRangePreset.ALL_TIME)
                    }
                },
                selectedDateRangePreset = uiState.selectedDateRangePreset,
                onSelectDateRangePreset = onSelectDateRangePreset,
                onSelectMonthPeriod = { onSelectPeriod(it) },
                customStartDateMillis = uiState.customStartDateMillis,
                customEndDateMillis = uiState.customEndDateMillis,
                onSelectCustomDateRange = onSelectCustomDateRange
            )
        }

        // 2. Pending Email Invites Banner (Merged from Dashboard)
        if (uiState.pendingEmailInvites.isNotEmpty()) {
            item {
                val invite = uiState.pendingEmailInvites.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailUnread,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(26.dp)
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
                            onClick = { onAcceptEmailInvite(invite) },
                            modifier = Modifier.testTag("accept_invite_button")
                        ) {
                            Text("Join")
                        }
                    }
                }
            }
        }

        // 3. Hero Monthly Spending & Budget Pulse Card (Merged from Dashboard)
        item {
            MonthlyBudgetPulseCard(
                totalSpent = uiState.totalMonthlySpent,
                totalIncome = uiState.totalMonthlyIncome,
                monthlyBudget = uiState.activeMonthlyBudget,
                dailySafeSpend = uiState.dailySafeSpend,
                remainingDays = uiState.remainingDaysInMonth,
                currencySymbol = currency,
                periodLabel = uiState.periodLabel,
                household = uiState.household,
                onEditBudgetClick = onEditMonthlyBudget
            )
        }

        // 4. Receipt Ticket Card: 6-Month Bar Graph + Interactive Category Pie Chart
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ReceiptTicketCard(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Monthly Trend",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        MonthlyBarGraph(
                            bars = uiState.monthlyTrendBars,
                            currencySymbol = currency,
                            onSelectMonth = { onSelectPeriod(it) },
                            onStepMonth = onStepMonth
                        )

                        if (categoryTotals.isNotEmpty()) {
                            ReceiptDashedDividerLine()

                            Text(
                                text = "Categories",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            CategoryDonutChart(
                                categoryTotals = categoryTotals,
                                currencySymbol = currency,
                                selectedCategory = activePieCategory,
                                onCategoryClick = { clickedCategory ->
                                    if (activePieCategory != null && activePieCategory.equals(clickedCategory, ignoreCase = true)) {
                                        onSelectCategory("All")
                                    } else {
                                        onSelectCategory(clickedCategory)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 5. Progressive Disclosure: Expandable Top Places & Member Split Card
        if (uiState.topMerchants.isNotEmpty() || uiState.memberSpendMap.isNotEmpty()) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { moreBreakdownsExpanded = !moreBreakdownsExpanded }
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Top Places & Split",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = if (moreBreakdownsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle Details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            AnimatedVisibility(visible = moreBreakdownsExpanded) {
                                Column(
                                    modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ReceiptDashedDividerLine()

                                    if (uiState.memberSpendMap.isNotEmpty()) {
                                        val total = uiState.memberSpendMap.values.sum().coerceAtLeast(1.0)
                                        uiState.memberSpendMap.entries.sortedByDescending { it.value }.forEach { (member, spent) ->
                                            val ratio = (spent / total).toFloat().coerceIn(0f, 1f)
                                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(text = member, style = MaterialTheme.typography.titleSmall)
                                                    Text(
                                                        text = formatCurrency(spent, currency),
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = HouseholdBadgeIndigo,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = { ratio },
                                                    color = HouseholdBadgeIndigo,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                        .clip(RoundedCornerShape(50))
                                                )
                                            }
                                        }
                                    }

                                    if (uiState.topMerchants.isNotEmpty()) {
                                        uiState.topMerchants.take(5).forEach { (merchant, total) ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = merchant,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = formatCurrency(total, currency),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Search & Filter Drawer (Merged from Ledger Dashboard)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search...") },
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
                            .weight(1f)
                            .testTag("transaction_search_input")
                    )

                    IconButton(
                        onClick = { filtersExpanded = !filtersExpanded },
                        modifier = Modifier.testTag("toggle_filters_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = if (filtersExpanded || hasActiveFilter) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                // Active Category Pill when filtered (e.g., after clicking a Pie Chart slice)
                if (uiState.selectedCategoryFilter != "All") {
                    val catAccent = categoryAccentColor(uiState.selectedCategoryFilter)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = catAccent.copy(alpha = 0.16f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelectCategory("All") }
                            .testTag("active_category_filter_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = categoryIcon(uiState.selectedCategoryFilter),
                                contentDescription = null,
                                tint = catAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.selectedCategoryFilter,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = catAccent
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Category Filter",
                                tint = catAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Expandable Filter Drawer
                AnimatedVisibility(visible = filtersExpanded || hasActiveFilter) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Date Range Row inside Filter Drawer
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DateRangePreset.entries.forEach { preset ->
                                    FilterChip(
                                        selected = uiState.selectedDateRangePreset == preset,
                                        onClick = {
                                            if (preset == DateRangePreset.CUSTOM) {
                                                showDrawerCustomRangePicker = true
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
                                        }
                                    )
                                }
                            }

                            // Type & Sort Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("All" to "All", "EXPENSE" to "Expenses", "INCOME" to "Income").forEach { (typeVal, label) ->
                                    FilterChip(
                                        selected = uiState.selectedTypeFilter.equals(typeVal, ignoreCase = true),
                                        onClick = { onSelectType(typeVal) },
                                        label = { Text(label) }
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
                                        onClick = { onSelectSortOrder(sortKey) },
                                        label = { Text(sortLabel) }
                                    )
                                }
                            }

                            // Category Filter Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("All") + uiState.availableCategories.forEach { cat ->
                                    FilterChip(
                                        selected = uiState.selectedCategoryFilter.equals(cat, ignoreCase = true),
                                        onClick = { onSelectCategory(cat) },
                                        label = { Text(cat) }
                                    )
                                }
                            }

                            // Payment Mode Filter Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("All", "UPI", "BANK", "CARD", "WALLET", "CASH").forEach { mode ->
                                    FilterChip(
                                        selected = uiState.selectedPaymentModeFilter.equals(mode, ignoreCase = true),
                                        onClick = { onSelectPaymentMode(mode) },
                                        label = { Text(if (mode == "All") "All Modes" else mode) }
                                    )
                                }
                            }

                            if (hasActiveFilter) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = onClearFilters,
                                        modifier = Modifier.testTag("clear_filters_button")
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

        // 7. Full Filtered Transactions List (with Swipe Right = Share, Swipe Left = Delete, Tap = Edit)
        if (uiState.filteredExpenses.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (uiState.allScopedExpenses.isNotEmpty()) {
                                Button(
                                    onClick = { onSelectDateRangePreset(DateRangePreset.ALL_TIME) },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.testTag("empty_state_all_time_button")
                                ) {
                                    Text("All Time")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onScanSmsClick,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.testTag("empty_state_sms_parser_button")
                                ) {
                                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sync")
                                }
                            }
                            Button(
                                onClick = onAddTransactionClick,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("empty_state_add_expense_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add")
                            }
                        }
                    }
                }
            }
        } else {
            items(
                items = uiState.filteredExpenses,
                key = { "${it.visibility}_${it.id}" }
            ) { expense ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ExpenseTransactionItemCard(
                        expense = expense,
                        currencySymbol = currency,
                        canTogglePrivacy = uiState.household != null,
                        onTogglePrivacy = { onToggleTransactionPrivacy(expense) },
                        onDelete = { onDeleteTransaction(expense) },
                        onEditTransaction = { onEditTransaction(expense) },
                        onQuickChangeCategory = { onQuickChangeCategory(expense) },
                        onTagClick = { clickedTag -> onSelectTag(clickedTag) }
                    )
                }
            }
        }
    }

    if (showDrawerCustomRangePicker) {
        CustomDateRangePickerModal(
            initialStartMillis = uiState.customStartDateMillis,
            initialEndMillis = uiState.customEndDateMillis,
            onDismiss = { showDrawerCustomRangePicker = false },
            onApplyRange = { start, end ->
                onSelectCustomDateRange(start, end)
            }
        )
    }
}

/**
 * Unified Account, Financial Accounts & Family Sharing Screen:
 * Consolidates user account info, sharing scope settings, bank/card accounts,
 * household join code & member invitations, and theme preferences.
 */
@Composable
fun HouseholdTabScreen(
    uiState: AxioDashboardUiState,
    currentUserName: String = "",
    currentUserEmail: String = "",
    onScopeSelected: (BudgetScope) -> Unit = {},
    onAddAccountClick: () -> Unit = {},
    onScanSmsClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onCreateHousehold: (String, Double) -> Unit,
    onJoinByCode: (String) -> Unit,
    onInviteByEmail: (String) -> Unit,
    onAcceptEmailInvite: (Household) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentThemeMode by LocalAccountSessionManager.themeMode.collectAsStateWithLifecycle()
    val currency = uiState.userProfile?.currencySymbol ?: "₹"

    var newHouseholdName by remember { mutableStateOf("Our Family Home") }
    var newHouseholdLimit by remember { mutableStateOf("") }
    var joinCodeInput by remember { mutableStateOf("") }
    var inviteEmailInput by remember { mutableStateOf("") }

    val displayName = currentUserName.ifBlank { uiState.userProfile?.displayName ?: "Member" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("household_tab_list"),
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Account & Sharing Overview Bar Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_sharing_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onScanSmsClick,
                                modifier = Modifier.testTag("scan_sms_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sms,
                                    contentDescription = "Sync SMS",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = onSignOutClick
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign Out",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Active Sharing Scope Switcher inside Account Bar
                    ScopeSwitcherBar(
                        selectedScope = uiState.selectedScope,
                        hasHousehold = uiState.household != null,
                        householdName = uiState.household?.name,
                        onScopeSelected = onScopeSelected
                    )
                }
            }
        }

        // 2. Financial Accounts Card (Bank, Credit Card, UPI, Cash)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Accounts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AccountsCarouselStrip(
                        accounts = uiState.accounts,
                        currencySymbol = currency,
                        onAddAccountClick = onAddAccountClick
                    )
                }
            }
        }

        // 3. Material 3 Theme & Appearance Card (System / Light / Dark AMOLED)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    ThemeModeSelectorBar(
                        currentMode = currentThemeMode,
                        onSelectMode = { LocalAccountSessionManager.setThemeMode(context, it) }
                    )
                }
            }
        }

        val household = uiState.household

        if (household == null) {
            if (uiState.pendingEmailInvites.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            uiState.pendingEmailInvites.forEach { inv ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = inv.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(onClick = { onAcceptEmailInvite(inv) }) {
                                        Text("Join")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Create Household Receipt Ticket
            item {
                ReceiptTicketCard(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Create Household",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        ReceiptDashedDividerLine()
                        OutlinedTextField(
                            value = newHouseholdName,
                            onValueChange = { newHouseholdName = it },
                            label = { Text("Household Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_household_name_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newHouseholdLimit,
                            onValueChange = { newHouseholdLimit = it },
                            label = { Text("Monthly Budget ($currency)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_household_limit_input")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val limit = newHouseholdLimit.toDoubleOrNull() ?: 0.0
                                onCreateHousehold(newHouseholdName, limit)
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_household_submit_button")
                        ) {
                            Text("Create")
                        }
                    }
                }
            }

            // Join Existing Household by 6-Character Code
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Join with Code",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = joinCodeInput,
                            onValueChange = { joinCodeInput = it.uppercase().take(8) },
                            label = { Text("Code") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("join_household_code_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (joinCodeInput.isNotBlank()) {
                                    onJoinByCode(joinCodeInput)
                                }
                            },
                            enabled = joinCodeInput.trim().length >= 4,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("join_household_submit_button")
                        ) {
                            Text("Join")
                        }
                    }
                }
            }
        } else {
            // Active Household Sharing Settings & Join Code Ticket
            item {
                ReceiptTicketCard(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = household.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (household.monthlyBudgetLimit > 0.0) {
                                Text(
                                    text = formatCurrency(household.monthlyBudgetLimit, currency),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        ReceiptDashedDividerLine()

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = household.joinCode,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(household.joinCode))
                                    },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.testTag("copy_join_code_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Invite Family Member by Email Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = inviteEmailInput,
                                onValueChange = { inviteEmailInput = it },
                                label = { Text("Email") },
                                leadingIcon = {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("invite_member_email_input")
                            )
                            Button(
                                onClick = {
                                    if (inviteEmailInput.contains("@")) {
                                        onInviteByEmail(inviteEmailInput)
                                        inviteEmailInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("send_email_invite_button")
                            ) {
                                Text("Invite")
                            }
                        }
                    }
                }
            }

            // Linked Household Members Roster (Single-line rows)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Members",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        household.memberUids.forEach { uid ->
                            val name = household.memberNames[uid] ?: "Member"
                            val memberSharedSpend = uiState.sharedExpenses
                                .filter { it.userId == uid && it.type == "EXPENSE" }
                                .sumOf { it.amount }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(HouseholdBadgeIndigo.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = name.take(1).uppercase(),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = HouseholdBadgeIndigo,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formatCurrency(memberSharedSpend, currency),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
