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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.WallpaperColorAccent
import com.example.data.model.WallpaperDensity
import com.example.data.model.WallpaperPatternStyle
import com.example.ui.components.AccountsCarouselStrip
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.CustomDateRangePickerModal
import com.example.ui.components.ExpenseTransactionItemCard
import com.example.ui.components.FinanceWallpaperPreviewBanner
import com.example.ui.components.MonthlyBarGraph
import com.example.ui.components.MonthlyBudgetPulseCard
import com.example.ui.components.ReceiptDashedDividerLine
import com.example.ui.components.ReceiptTicketCard
import com.example.ui.components.ScopeSwitcherBar
import com.example.ui.components.ThemeModeSelectorBar
import com.example.ui.components.categoryIcon
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatShortDate
import com.example.ui.theme.BudgetDangerCoral
import com.example.ui.theme.BudgetSafeGreen
import com.example.ui.theme.BudgetWarnAmber
import com.example.ui.theme.HouseholdBadgeIndigo
import com.example.ui.theme.LocalAppDimens
import com.example.ui.theme.categoryAccentColor
import com.example.ui.viewmodel.OneFamilyDashboardUiState
import com.example.ui.viewmodel.CategorySpendProgress
import com.example.ui.viewmodel.DateRangePreset
import com.example.ui.viewmodel.YearMonthPeriod
import kotlin.math.roundToInt

/**
 * Home Screen (`HomeTabScreen`):
 * - Displays the Personal & Family section switcher (`ScopeSwitcherBar`) ONLY on the Home screen.
 * - Displays the Monthly Budget Pulse Card, search & filter drawer, and full interactive transaction list.
 */
@Composable
fun HomeTabScreen(
    uiState: OneFamilyDashboardUiState,
    onScopeSelected: (BudgetScope) -> Unit,
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
    onEditTransaction: (ExpenseTransaction) -> Unit = {},
    onQuickChangeCategory: (ExpenseTransaction) -> Unit = {},
    onToggleTransactionPrivacy: (ExpenseTransaction) -> Unit = {},
    onLinkDuplicateTransaction: (ExpenseTransaction) -> Unit = {},
    onDeleteTransaction: (ExpenseTransaction) -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    val currency = uiState.userProfile?.currencySymbol ?: "₹"

    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    var showDrawerCustomRangePicker by rememberSaveable { mutableStateOf(false) }

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
            .testTag("home_tab_list"),
        contentPadding = PaddingValues(0.dp, dimens.itemSpacing * 0.5f, 0.dp, dimens.bottomListPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
    ) {
        // 1. Personal & Family Section Switcher (Shown ONLY on the Home Screen)
        item {
            ScopeSwitcherBar(
                selectedScope = uiState.selectedScope,
                hasHousehold = uiState.household != null,
                householdName = uiState.household?.name,
                onScopeSelected = onScopeSelected
            )
        }

        // 2. Pending Email Invites Banner
        if (uiState.pendingEmailInvites.isNotEmpty()) {
            item {
                val invite = uiState.pendingEmailInvites.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding),
                    shape = RoundedCornerShape(dimens.cardCornerRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(dimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailUnread,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(dimens.rowIconSize)
                        )
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.75f))
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

        // 3. Hero Monthly Spending & Budget Pulse Card
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

        // 4. Search & Expandable Filter Drawer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding)
                    .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.65f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.5f)
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
                            modifier = Modifier.padding(
                                horizontal = dimens.horizontalPadding * 0.85f,
                                vertical = dimens.itemSpacing * 0.6f
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = categoryIcon(uiState.selectedCategoryFilter),
                                contentDescription = null,
                                tint = catAccent,
                                modifier = Modifier.size(dimens.rowIconSize * 0.72f)
                            )
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                            Text(
                                text = uiState.selectedCategoryFilter,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = catAccent
                            )
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Category Filter",
                                tint = catAccent,
                                modifier = Modifier.size(dimens.rowIconSize * 0.72f)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = filtersExpanded || hasActiveFilter) {
                    Card(
                        shape = RoundedCornerShape(dimens.cardCornerRadius),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(dimens.cardPadding * 0.7f),
                            verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.4f)
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
                                        },
                                        modifier = Modifier.testTag(
                                            if (preset == DateRangePreset.ALL_TIME) {
                                                "all_time_period_chip"
                                            } else {
                                                "date_range_chip_${preset.name.lowercase()}"
                                            }
                                        )
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.4f)
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

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.4f)
                            ) {
                                (listOf("All") + uiState.availableCategories).forEach { cat ->
                                    FilterChip(
                                        selected = uiState.selectedCategoryFilter.equals(cat, ignoreCase = true),
                                        onClick = { onSelectCategory(cat) },
                                        label = { Text(cat) }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.4f)
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

        // 5. Filtered Transactions List
        if (uiState.filteredExpenses.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding),
                    shape = RoundedCornerShape(dimens.cardCornerRadius),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(dimens.rowIconSize * 1.4f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.6f)) {
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
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(dimens.rowIconSize * 0.72f))
                                    Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                                    Text("Sync")
                                }
                            }
                            Button(
                                onClick = onAddTransactionClick,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("empty_state_add_expense_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(dimens.rowIconSize * 0.72f))
                                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
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
                Box(modifier = Modifier.padding(horizontal = dimens.horizontalPadding)) {
                    ExpenseTransactionItemCard(
                        expense = expense,
                        currencySymbol = currency,
                        canTogglePrivacy = uiState.household != null,
                        onTogglePrivacy = { onToggleTransactionPrivacy(expense) },
                        onLinkDuplicate = { onLinkDuplicateTransaction(expense) },
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
 * Analysis Tab (`AnalyticsTabScreen`):
 * - Displays 6-Month Monthly Trend Bar Graph and Interactive Category Donut Chart.
 * - Shows all categories, and when any category is tapped (either in the donut chart or in the category list),
 *   expands/displays ALL transactions for that category with full details.
 * - Tapping any transaction opens the full `EditTransactionDialog` so the user can edit the entire transaction
 *   (merchant, amount, timestamp, category, account, payment mode, tags, notes, shared/personal) and view the raw SMS.
 */
@Composable
fun AnalyticsTabScreen(
    uiState: OneFamilyDashboardUiState,
    onStepMonth: (Int) -> Unit,
    onSelectPeriod: (YearMonthPeriod?) -> Unit,
    onSelectCategory: (String) -> Unit = {},
    onEditTransaction: (ExpenseTransaction) -> Unit = {},
    onQuickChangeCategory: (ExpenseTransaction) -> Unit = {},
    onToggleTransactionPrivacy: (ExpenseTransaction) -> Unit = {},
    onLinkDuplicateTransaction: (ExpenseTransaction) -> Unit = {},
    onDeleteTransaction: (ExpenseTransaction) -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    val currency = uiState.userProfile?.currencySymbol ?: "₹"
    val categoryTotals = uiState.categoryProgressList
        .filter { it.spent > 0 }
        .map { it.category to it.spent }

    var selectedAnalysisCategory by rememberSaveable {
        mutableStateOf<String?>(uiState.selectedCategoryFilter.takeIf { it != "All" })
    }
    var moreBreakdownsExpanded by rememberSaveable { mutableStateOf(false) }

    // Keep local selection synced if external filter changes
    LaunchedEffect(uiState.selectedCategoryFilter) {
        if (uiState.selectedCategoryFilter != "All") {
            selectedAnalysisCategory = uiState.selectedCategoryFilter
        }
    }

    val toggleCategorySelection: (String) -> Unit = { clickedCategory ->
        if (selectedAnalysisCategory != null && selectedAnalysisCategory.equals(clickedCategory, ignoreCase = true)) {
            selectedAnalysisCategory = null
            onSelectCategory("All")
        } else {
            selectedAnalysisCategory = clickedCategory
            onSelectCategory(clickedCategory)
        }
    }

    // All transactions in the current period grouped by category
    val allPeriodTransactions = uiState.activeExpenses

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analytics_tab_list"),
        contentPadding = PaddingValues(0.dp, dimens.itemSpacing * 0.5f, 0.dp, dimens.bottomListPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
    ) {
        // 1. Receipt Ticket Card: 6-Month Bar Graph + Interactive Category Pie Chart
        item {
            Box(modifier = Modifier.padding(horizontal = dimens.horizontalPadding)) {
                ReceiptTicketCard(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(modifier = Modifier.padding(dimens.cardPadding)) {
                        Text(
                            text = "Monthly Trend",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(dimens.itemSpacing * 0.65f))
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
                                selectedCategory = selectedAnalysisCategory,
                                onCategoryClick = toggleCategorySelection
                            )
                        }
                    }
                }
            }
        }

        // 2. Interactive Category Breakdown Cards: Tap any category to view & edit all its transactions
        items(
            items = uiState.categoryProgressList.filter { it.spent > 0 },
            key = { "analysis_cat_${it.category}" }
        ) { catItem ->
            val isExpanded = selectedAnalysisCategory != null &&
                selectedAnalysisCategory.equals(catItem.category, ignoreCase = true)
            val catColor = categoryAccentColor(catItem.category)
            val categoryTransactions = remember(allPeriodTransactions, catItem.category) {
                allPeriodTransactions
                    .filter { it.category.equals(catItem.category, ignoreCase = true) }
                    .sortedByDescending { it.transactionDateMillis }
            }

            Box(modifier = Modifier.padding(horizontal = dimens.horizontalPadding)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimens.cardCornerRadius))
                        .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy))
                        .testTag("analysis_category_card_${catItem.category.lowercase().replace(" ", "_")}"),
                    shape = RoundedCornerShape(dimens.cardCornerRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Category Header Row (Tap to expand/collapse all transactions in this category)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { toggleCategorySelection(catItem.category) }
                                .padding(
                                    horizontal = dimens.horizontalPadding,
                                    vertical = dimens.itemSpacing * 1.1f
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(dimens.rowIconBoxSize)
                                    .clip(RoundedCornerShape(dimens.smallCornerRadius))
                                    .background(catColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = categoryIcon(catItem.category),
                                    contentDescription = catItem.category,
                                    tint = catColor,
                                    modifier = Modifier.size(dimens.rowIconSize)
                                )
                            }
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.85f))
                            Text(
                                text = "${catItem.category} (${categoryTransactions.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
                            Text(
                                text = formatCurrency(catItem.spent, currency),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Category Transactions",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Expanded Transactions List for this Category (Tap any transaction to Edit Full Transaction)
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = dimens.horizontalPadding * 0.75f,
                                        end = dimens.horizontalPadding * 0.75f,
                                        bottom = dimens.itemSpacing
                                    ),
                                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.7f)
                            ) {
                                ReceiptDashedDividerLine()
                                categoryTransactions.forEach { tx ->
                                    Surface(
                                        shape = RoundedCornerShape(dimens.smallCornerRadius),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(dimens.smallCornerRadius))
                                            .clickable { onEditTransaction(tx) }
                                            .testTag("analysis_tx_row_${tx.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    horizontal = dimens.horizontalPadding * 0.85f,
                                                    vertical = dimens.itemSpacing * 0.85f
                                                ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = tx.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${formatShortDate(tx.transactionDateMillis)} • ${tx.accountName}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = formatCurrency(tx.amount, currency),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.25f))
                                                IconButton(
                                                    onClick = { onLinkDuplicateTransaction(tx) },
                                                    modifier = Modifier
                                                        .size(dimens.rowIconBoxSize * 0.72f)
                                                        .testTag("analysis_link_tx_button_${tx.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Link,
                                                        contentDescription = "Link Duplicate Transaction",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(dimens.rowIconSize * 0.75f)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { onEditTransaction(tx) },
                                                    modifier = Modifier
                                                        .size(dimens.rowIconBoxSize * 0.75f)
                                                        .testTag("analysis_edit_tx_button_${tx.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit Transaction",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(dimens.rowIconSize * 0.8f)
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
        }

        // 3. Expandable Top Places & Member Split Card
        if (uiState.topMerchants.isNotEmpty() || uiState.memberSpendMap.isNotEmpty()) {
            item {
                Box(modifier = Modifier.padding(horizontal = dimens.horizontalPadding)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                        shape = RoundedCornerShape(dimens.cardCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { moreBreakdownsExpanded = !moreBreakdownsExpanded }
                                    .padding(dimens.cardPadding),
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
                                    modifier = Modifier.padding(
                                        start = dimens.cardPadding,
                                        end = dimens.cardPadding,
                                        bottom = dimens.cardPadding
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
                                ) {
                                    ReceiptDashedDividerLine()

                                    if (uiState.memberSpendMap.isNotEmpty()) {
                                        val total = uiState.memberSpendMap.values.sum().coerceAtLeast(1.0)
                                        uiState.memberSpendMap.entries.sortedByDescending { it.value }.forEach { (member, spent) ->
                                            val ratio = (spent / total).toFloat().coerceIn(0f, 1f)
                                            Column(modifier = Modifier.padding(vertical = dimens.itemSpacing * 0.3f)) {
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
                                                Spacer(modifier = Modifier.height(dimens.itemSpacing * 0.3f))
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
                                                    .padding(vertical = dimens.itemSpacing * 0.3f),
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
                                                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
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
    }
}

@Composable
fun BudgetsTabScreen(
    uiState: OneFamilyDashboardUiState,
    onEditOverallBudgetClick: () -> Unit,
    onAddOrEditCategoryBudgetClick: (CategorySpendProgress?) -> Unit
) {
    val dimens = LocalAppDimens.current
    val currency = uiState.userProfile?.currencySymbol ?: "₹"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("budgets_tab_list"),
        contentPadding = PaddingValues(0.dp, dimens.itemSpacing * 0.5f, 0.dp, dimens.bottomListPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
    ) {
        // Receipt Ticket Summary for Monthly Budgets (Single-Line Household & Personal Pills)
        item {
            Box(modifier = Modifier.padding(horizontal = dimens.horizontalPadding)) {
                ReceiptTicketCard(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(modifier = Modifier.padding(dimens.cardPadding)) {
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
                                    .size(dimens.rowIconBoxSize * 0.8f)
                                    .testTag("configure_overall_budget_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Monthly Budget",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(dimens.rowIconSize * 0.8f)
                                )
                            }
                        }

                        ReceiptDashedDividerLine()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.6f)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(dimens.smallCornerRadius),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = dimens.horizontalPadding * 0.75f,
                                        vertical = dimens.itemSpacing
                                    ),
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
                                shape = RoundedCornerShape(dimens.smallCornerRadius),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = dimens.horizontalPadding * 0.75f,
                                        vertical = dimens.itemSpacing
                                    ),
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
                    .padding(horizontal = dimens.horizontalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Category Budgets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap any category to set or edit its monthly limit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = { onAddOrEditCategoryBudgetClick(null) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("add_category_budget_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Set Category Limit",
                        modifier = Modifier.size(dimens.rowIconSize * 0.75f)
                    )
                    Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.3f))
                    Text("Custom Category")
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
            val safeTag = item.category.lowercase().replace(Regex("[^a-z0-9]+"), "_")
            Box(modifier = Modifier.padding(horizontal = dimens.horizontalPadding)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimens.cardCornerRadius))
                        .clickable { onAddOrEditCategoryBudgetClick(item) }
                        .testTag("category_budget_card_$safeTag"),
                    shape = RoundedCornerShape(dimens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = dimens.horizontalPadding,
                            vertical = dimens.itemSpacing * 1.1f
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(dimens.rowIconBoxSize)
                                    .clip(RoundedCornerShape(dimens.smallCornerRadius))
                                    .background(catColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = categoryIcon(item.category),
                                    contentDescription = item.category,
                                    tint = catColor,
                                    modifier = Modifier.size(dimens.rowIconSize)
                                )
                            }
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.85f))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (item.limit > 0.0) {
                                        "Spent ${formatCurrency(item.spent, currency)} of ${formatCurrency(item.limit, currency)}"
                                    } else {
                                        "Spent ${formatCurrency(item.spent, currency)} • Budget not set"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (item.limit > 0.0 && item.isOverBudget) {
                                        barColor
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
                            OutlinedButton(
                                onClick = { onAddOrEditCategoryBudgetClick(item) },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("set_category_budget_$safeTag")
                            ) {
                                Icon(
                                    imageVector = if (item.limit > 0.0) Icons.Default.Edit else Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(dimens.rowIconSize * 0.7f)
                                )
                                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.25f))
                                Text(
                                    text = if (item.limit > 0.0) formatCurrency(item.limit, currency) else "Set Budget",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (item.limit > 0.0) {
                            Spacer(modifier = Modifier.height(dimens.itemSpacing * 0.8f))
                            LinearProgressIndicator(
                                progress = { item.progressFraction.coerceIn(0f, 1f) },
                                color = barColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                            Spacer(modifier = Modifier.height(dimens.itemSpacing * 0.35f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.percentage}% used",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = barColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                                val diff = item.limit - item.spent
                                Text(
                                    text = if (diff >= 0.0) {
                                        "${formatCurrency(diff, currency)} remaining"
                                    } else {
                                        "Over by ${formatCurrency(-diff, currency)}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (diff >= 0.0) MaterialTheme.colorScheme.onSurfaceVariant else barColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Unified Account, Settings & Family Sharing Screen (`HouseholdTabScreen`):
 * - Edit User Name section (`edit_user_name_input` & `save_user_name_button`)
 * - SMS Sync section with updated sync icon (`Icons.Default.Sync`, `scan_sms_button`)
 * - Material 3 Theme selector (`System`, `Light`, `Dark` Full AMOLED `#000000`)
 * - Financial Accounts (`AccountsCarouselStrip`)
 * - Household creation, join code, email invitations, and members roster
 * - Dedicated Logout section (`settings_logout_button`)
 */
@Composable
fun HouseholdTabScreen(
    uiState: OneFamilyDashboardUiState,
    currentUserName: String = "",
    currentUserEmail: String = "",
    onUpdateUserName: (String) -> Unit = {},
    onAddAccountClick: () -> Unit = {},
    onScanSmsClick: () -> Unit = {},
    onSendTestFcmAlertClick: () -> Unit = {},
    onRunStabilityCheckClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onCreateHousehold: (String, Double) -> Unit,
    onJoinByCode: (String) -> Unit,
    onInviteByEmail: (String) -> Unit,
    onAcceptEmailInvite: (Household) -> Unit
) {
    val dimens = LocalAppDimens.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentThemeMode by LocalAccountSessionManager.themeMode.collectAsStateWithLifecycle()
    val wallpaperSettings by LocalAccountSessionManager.wallpaperSettings.collectAsStateWithLifecycle()
    val fcmPushSettings by LocalAccountSessionManager.fcmPushSettings.collectAsStateWithLifecycle()
    val crashlyticsSettings by LocalAccountSessionManager.crashlyticsSettings.collectAsStateWithLifecycle()
    val stabilityDiagnostics by LocalAccountSessionManager.stabilityDiagnostics.collectAsStateWithLifecycle()
    val currency = uiState.userProfile?.currencySymbol ?: "₹"

    val displayName = uiState.userProfile?.displayName?.takeIf { it.isNotBlank() }
        ?: currentUserName.ifBlank { "Member" }
    var editableUserName by remember(displayName) { mutableStateOf(displayName) }

    var newHouseholdName by remember { mutableStateOf("Our Family Home") }
    var newHouseholdLimit by remember { mutableStateOf("") }
    var joinCodeInput by remember { mutableStateOf("") }
    var inviteEmailInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("household_tab_list"),
        contentPadding = PaddingValues(
            dimens.horizontalPadding,
            dimens.itemSpacing * 0.5f,
            dimens.horizontalPadding,
            dimens.bottomListPadding
        ),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 1.15f)
    ) {
        // 1. User Profile & Edit Name Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_sharing_card"),
                shape = RoundedCornerShape(dimens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(dimens.rowIconBoxSize)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(dimens.rowIconSize)
                            )
                        }
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.75f))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (currentUserEmail.isNotBlank()) {
                                Text(
                                    text = currentUserEmail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Edit User Name Row with Validation
                    val userNameError = validateDisplayName(editableUserName)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.6f)
                    ) {
                        OutlinedTextField(
                            value = editableUserName,
                            onValueChange = { editableUserName = it.take(60) },
                            label = { Text("Your Name") },
                            isError = userNameError != null,
                            supportingText = userNameError?.let { err -> { Text(err) } },
                            singleLine = true,
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_user_name_input")
                        )
                        Button(
                            onClick = {
                                if (validateDisplayName(editableUserName) == null) {
                                    onUpdateUserName(editableUserName.trim())
                                }
                            },
                            enabled = userNameError == null && editableUserName.trim() != displayName,
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            modifier = Modifier
                                .height(dimens.navPillHeight)
                                .testTag("save_user_name_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save Name",
                                modifier = Modifier.size(dimens.rowIconSize * 0.8f)
                            )
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.3f))
                            Text("Save")
                        }
                    }
                }
            }
        }

        // 2. SMS Sync Section (Updated Sync Icon)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.cardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(dimens.rowIconBoxSize * 0.85f)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(dimens.rowIconSize * 0.85f)
                            )
                        }
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.75f))
                        Text(
                            text = "SMS Sync",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onScanSmsClick,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("scan_sms_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync SMS",
                            modifier = Modifier.size(dimens.rowIconSize * 0.75f)
                        )
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                        Text("Sync Now")
                    }
                }
            }
        }

        // 3. Material 3 Theme & WhatsApp-Style Finance Wallpaper Customization Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_and_wallpaper_card"),
                shape = RoundedCornerShape(dimens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.85f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(dimens.rowIconBoxSize * 0.85f)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(dimens.rowIconSize * 0.85f)
                            )
                        }
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.75f))
                        Column {
                            Text(
                                text = "Theme & Finance Wallpaper",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Adapts automatically to Light & AMOLED Dark modes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    ThemeModeSelectorBar(
                        currentMode = currentThemeMode,
                        onSelectMode = { LocalAccountSessionManager.setThemeMode(context, it) }
                    )

                    ReceiptDashedDividerLine()

                    // Live Preview Banner
                    FinanceWallpaperPreviewBanner(settings = wallpaperSettings)

                    // On / Off Toggle for WhatsApp-Style Finance Background
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Finance Doodle & Emoji Background",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "WhatsApp-style tiled finance icons & emojis",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = wallpaperSettings.enabled,
                            onCheckedChange = { enabled ->
                                LocalAccountSessionManager.updateWallpaperSettings(
                                    context = context,
                                    newSettings = wallpaperSettings.copy(enabled = enabled)
                                )
                            },
                            modifier = Modifier.testTag("wallpaper_enable_switch")
                        )
                    }

                    AnimatedVisibility(visible = wallpaperSettings.enabled) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.7f)
                        ) {
                            // Pattern Style Selector
                            Text(
                                text = "Pattern Style",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.45f)
                            ) {
                                WallpaperPatternStyle.entries.forEach { style ->
                                    FilterChip(
                                        selected = wallpaperSettings.style == style,
                                        onClick = {
                                            LocalAccountSessionManager.updateWallpaperSettings(
                                                context = context,
                                                newSettings = wallpaperSettings.copy(style = style)
                                            )
                                        },
                                        label = { Text(style.label) },
                                        modifier = Modifier.testTag("wallpaper_style_chip_${style.name.lowercase()}")
                                    )
                                }
                            }

                            // Color Accent Selector
                            Text(
                                text = "Color Accent",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.45f)
                            ) {
                                WallpaperColorAccent.entries.forEach { accent ->
                                    FilterChip(
                                        selected = wallpaperSettings.colorAccent == accent,
                                        onClick = {
                                            LocalAccountSessionManager.updateWallpaperSettings(
                                                context = context,
                                                newSettings = wallpaperSettings.copy(colorAccent = accent)
                                            )
                                        },
                                        label = { Text(accent.label) },
                                        modifier = Modifier.testTag("wallpaper_accent_chip_${accent.name.lowercase()}")
                                    )
                                }
                            }

                            // Pattern Density Selector
                            Text(
                                text = "Pattern Density",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.45f)
                            ) {
                                WallpaperDensity.entries.forEach { density ->
                                    FilterChip(
                                        selected = wallpaperSettings.density == density,
                                        onClick = {
                                            LocalAccountSessionManager.updateWallpaperSettings(
                                                context = context,
                                                newSettings = wallpaperSettings.copy(density = density)
                                            )
                                        },
                                        label = { Text(density.label) },
                                        modifier = Modifier.testTag("wallpaper_density_chip_${density.name.lowercase()}")
                                    )
                                }
                            }

                            // Pattern Intensity / Opacity Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pattern Intensity",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${wallpaperSettings.opacityPercent}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = wallpaperSettings.opacityPercent.toFloat(),
                                onValueChange = { newVal ->
                                    LocalAccountSessionManager.updateWallpaperSettings(
                                        context = context,
                                        newSettings = wallpaperSettings.copy(opacityPercent = newVal.roundToInt().coerceIn(4, 28))
                                    )
                                },
                                valueRange = 4f..28f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("wallpaper_opacity_slider")
                            )
                        }
                    }
                }
            }
        }

        // 3B. Firebase Cloud Messaging (FCM) Family & Budget Push Alerts Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fcm_push_settings_card"),
                shape = RoundedCornerShape(dimens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.75f)
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
                                    .size(dimens.rowIconBoxSize * 0.85f)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(dimens.rowIconSize * 0.85f)
                                )
                            }
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.75f))
                            Column {
                                Text(
                                    text = "Firebase Cloud Messaging (FCM)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val topicId = uiState.household?.id?.ifBlank { "default" } ?: "personal"
                                Text(
                                    text = "Topic: household_$topicId",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Family Member Transaction Alerts",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Instant push notification when a family member logs a shared expense",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = fcmPushSettings.familyTransactionAlertsEnabled,
                            onCheckedChange = { enabled ->
                                LocalAccountSessionManager.updateFcmPushSettings(
                                    context = context,
                                    newSettings = fcmPushSettings.copy(familyTransactionAlertsEnabled = enabled)
                                )
                            },
                            modifier = Modifier.testTag("fcm_family_tx_alerts_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Category Budget Threshold Alerts",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Instant push notification when a shared category budget nears its threshold",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = fcmPushSettings.budgetThresholdAlertsEnabled,
                            onCheckedChange = { enabled ->
                                LocalAccountSessionManager.updateFcmPushSettings(
                                    context = context,
                                    newSettings = fcmPushSettings.copy(budgetThresholdAlertsEnabled = enabled)
                                )
                            },
                            modifier = Modifier.testTag("fcm_budget_threshold_alerts_switch")
                        )
                    }

                    OutlinedButton(
                        onClick = onSendTestFcmAlertClick,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fcm_test_push_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(dimens.rowIconSize * 0.75f)
                        )
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                        Text("Send Test FCM Budget Push Alert")
                    }
                }
            }
        }

        // 3C. Firebase Crashlytics & Real-Time Stability Guard Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("crashlytics_settings_card"),
                shape = RoundedCornerShape(dimens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.75f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(dimens.rowIconBoxSize * 0.85f)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(dimens.rowIconSize * 0.85f)
                            )
                        }
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.75f))
                        Column {
                            Text(
                                text = "Firebase Crashlytics & Stability Guard",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Real-time crash protection for SMS auto-sync & responsive typography",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Real-Time Crashlytics Reporting",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Track, prioritize, and capture uncaught & non-fatal exceptions",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = crashlyticsSettings.crashReportingEnabled,
                            onCheckedChange = { enabled ->
                                LocalAccountSessionManager.updateCrashlyticsSettings(
                                    context = context,
                                    newSettings = crashlyticsSettings.copy(crashReportingEnabled = enabled)
                                )
                            },
                            modifier = Modifier.testTag("crashlytics_enabled_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SMS Auto-Sync Stability Guardrails",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Paced IO batching & cursor isolation on OEM messaging providers",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = crashlyticsSettings.smsSyncGuardEnabled,
                            onCheckedChange = { enabled ->
                                LocalAccountSessionManager.updateCrashlyticsSettings(
                                    context = context,
                                    newSettings = crashlyticsSettings.copy(smsSyncGuardEnabled = enabled)
                                )
                            },
                            modifier = Modifier.testTag("crashlytics_sms_guard_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Responsive Typography Guardrails",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Clamps extreme font/screen scales across foldables & small devices",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = crashlyticsSettings.typographyGuardEnabled,
                            onCheckedChange = { enabled ->
                                LocalAccountSessionManager.updateCrashlyticsSettings(
                                    context = context,
                                    newSettings = crashlyticsSettings.copy(typographyGuardEnabled = enabled)
                                )
                            },
                            modifier = Modifier.testTag("crashlytics_typography_guard_switch")
                        )
                    }

                    if (stabilityDiagnostics.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("crashlytics_diagnostics_list")
                        ) {
                            Column(
                                modifier = Modifier.padding(dimens.cardPadding * 0.75f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Recent Stability Breadcrumbs (${stabilityDiagnostics.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                stabilityDiagnostics.take(4).forEach { entry ->
                                    Text(
                                        text = "• [${entry.subsystem}] ${entry.message}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.5f)
                    ) {
                        OutlinedButton(
                            onClick = onRunStabilityCheckClick,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("crashlytics_run_check_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                modifier = Modifier.size(dimens.rowIconSize * 0.72f)
                            )
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.3f))
                            Text("Verify Stability")
                        }
                        if (stabilityDiagnostics.isNotEmpty()) {
                            TextButton(
                                onClick = { LocalAccountSessionManager.clearStabilityDiagnostics(context) },
                                modifier = Modifier.testTag("crashlytics_clear_logs_button")
                            ) {
                                Text("Clear")
                            }
                        }
                    }
                }
            }
        }

        // 4. Financial Accounts Card (Bank, Credit Card, UPI, Cash)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(dimens.rowIconBoxSize * 0.85f)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(dimens.rowIconSize * 0.85f)
                            )
                        }
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.75f))
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

        // 5. Family Household Sharing Settings
        val household = uiState.household

        if (household == null) {
            if (uiState.pendingEmailInvites.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimens.cardCornerRadius),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(dimens.cardPadding)) {
                            uiState.pendingEmailInvites.forEach { inv ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = dimens.itemSpacing * 0.35f),
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

            item {
                ReceiptTicketCard(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(modifier = Modifier.padding(dimens.cardPadding)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.6f))
                            Text(
                                text = "Create Household",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        ReceiptDashedDividerLine()
                        val hhNameError = validateDisplayName(newHouseholdName)
                        val hhLimitError = validateNonNegativeAmount(newHouseholdLimit)
                        OutlinedTextField(
                            value = newHouseholdName,
                            onValueChange = { newHouseholdName = it.take(60) },
                            label = { Text("Household Name") },
                            isError = hhNameError != null,
                            supportingText = hhNameError?.let { err -> { Text(err) } },
                            singleLine = true,
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_household_name_input")
                        )
                        Spacer(modifier = Modifier.height(dimens.itemSpacing * 0.8f))
                        OutlinedTextField(
                            value = newHouseholdLimit,
                            onValueChange = { newHouseholdLimit = sanitizeDecimalInput(it) },
                            label = { Text("Monthly Budget ($currency)") },
                            placeholder = { Text("Optional") },
                            isError = hhLimitError != null,
                            supportingText = hhLimitError?.let { err -> { Text(err) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_household_limit_input")
                        )
                        Spacer(modifier = Modifier.height(dimens.itemSpacing))
                        Button(
                            onClick = {
                                if (hhNameError == null && hhLimitError == null) {
                                    val limit = newHouseholdLimit.trim().toDoubleOrNull() ?: 0.0
                                    onCreateHousehold(newHouseholdName.trim(), limit)
                                }
                            },
                            enabled = hhNameError == null && hhLimitError == null,
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_household_submit_button")
                        ) {
                            Text("Create")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(dimens.cardPadding)) {
                        Text(
                            text = "Join with Code",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(dimens.itemSpacing))
                        val joinCodeError = if (joinCodeInput.isNotEmpty()) {
                            validateHouseholdJoinCode(joinCodeInput)
                        } else {
                            null
                        }
                        OutlinedTextField(
                            value = joinCodeInput,
                            onValueChange = {
                                joinCodeInput = it.filter { ch -> ch.isLetterOrDigit() }.uppercase().take(6)
                            },
                            label = { Text("6-Character Invite Code") },
                            isError = joinCodeError != null,
                            supportingText = joinCodeError?.let { err -> { Text(err) } },
                            singleLine = true,
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("join_household_code_input")
                        )
                        Spacer(modifier = Modifier.height(dimens.itemSpacing))
                        Button(
                            onClick = {
                                if (validateHouseholdJoinCode(joinCodeInput) == null) {
                                    onJoinByCode(joinCodeInput.trim())
                                }
                            },
                            enabled = validateHouseholdJoinCode(joinCodeInput) == null,
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
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
            item {
                ReceiptTicketCard(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(modifier = Modifier.padding(dimens.cardPadding)) {
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
                                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
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
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = dimens.horizontalPadding,
                                        vertical = dimens.itemSpacing * 0.85f
                                    ),
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
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(dimens.rowIconSize * 0.72f))
                                    Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                                    Text("Copy")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(dimens.itemSpacing))

                        val inviteEmailError = if (inviteEmailInput.isNotEmpty()) {
                            validateEmailAddress(inviteEmailInput)
                        } else {
                            null
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.6f)
                        ) {
                            OutlinedTextField(
                                value = inviteEmailInput,
                                onValueChange = { inviteEmailInput = it.trim().take(120) },
                                label = { Text("Email Address") },
                                isError = inviteEmailError != null,
                                supportingText = inviteEmailError?.let { err -> { Text(err) } },
                                leadingIcon = {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(dimens.smallCornerRadius),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("invite_member_email_input")
                            )
                            Button(
                                onClick = {
                                    if (validateEmailAddress(inviteEmailInput) == null) {
                                        onInviteByEmail(inviteEmailInput.trim())
                                        inviteEmailInput = ""
                                    }
                                },
                                enabled = validateEmailAddress(inviteEmailInput) == null,
                                shape = RoundedCornerShape(dimens.smallCornerRadius),
                                modifier = Modifier
                                    .height(dimens.navPillHeight)
                                    .testTag("send_email_invite_button")
                            ) {
                                Text("Invite")
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(dimens.cardPadding)) {
                        Text(
                            text = "Members",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(dimens.itemSpacing * 0.8f))
                        household.memberUids.forEach { uid ->
                            val name = household.memberNames[uid] ?: "Member"
                            val memberSharedSpend = uiState.sharedExpenses
                                .filter { it.userId == uid && it.type == "EXPENSE" }
                                .sumOf { it.amount }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = dimens.itemSpacing * 0.6f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(dimens.rowIconBoxSize * 0.9f)
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
                                    Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.75f))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
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

        // 6. Dedicated Logout Section in Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.cardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(dimens.rowIconBoxSize * 0.85f)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(dimens.rowIconSize * 0.85f)
                            )
                        }
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.75f))
                        Text(
                            text = "Logout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onSignOutClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            modifier = Modifier.size(dimens.rowIconSize * 0.75f)
                        )
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                        Text("Logout")
                    }
                }
            }
        }
    }
}
