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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpenseTransaction
import com.example.data.model.Household
import com.example.ui.components.AccountsCarouselStrip
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.ExpenseTransactionItemCard
import com.example.ui.components.MonthPeriodSelectorBar
import com.example.ui.components.MonthlyBarGraph
import com.example.ui.components.MonthlyBudgetPulseCard
import com.example.ui.viewmodel.AxioDashboardUiState
import com.example.ui.viewmodel.YearMonthPeriod

@Composable
fun DashboardTabScreen(
    uiState: AxioDashboardUiState,
    onStepMonth: (Int) -> Unit,
    onSelectPeriod: (YearMonthPeriod?) -> Unit,
    onEditMonthlyBudget: () -> Unit,
    onScanSmsClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onAddAccountClick: () -> Unit,
    onOpenHouseholdTab: () -> Unit,
    onOpenTransactionsTab: () -> Unit,
    onSelectCategoryAndOpenLedger: (String) -> Unit,
    onAcceptEmailInvite: (Household) -> Unit,
    onEditTransaction: (ExpenseTransaction) -> Unit,
    onQuickChangeCategory: (ExpenseTransaction) -> Unit,
    onToggleTransactionPrivacy: (ExpenseTransaction) -> Unit,
    onDeleteTransaction: (ExpenseTransaction) -> Unit
) {
    val currency = uiState.userProfile?.currencySymbol ?: "₹"
    val categoryTotals = uiState.categoryProgressList
        .filter { it.spent > 0 }
        .map { it.category to it.spent }

    var chartsExpanded by rememberSaveable { mutableStateOf(false) }
    var accountsExpanded by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_lazy_column"),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month / Year & All-Time Selector Bar
        item {
            MonthPeriodSelectorBar(
                selectedPeriod = uiState.selectedPeriod,
                periodLabel = uiState.periodLabel,
                onStepMonth = onStepMonth,
                onSelectCurrentMonth = { onSelectPeriod(YearMonthPeriod.current()) },
                onToggleAllTime = {
                    if (uiState.selectedPeriod == null) {
                        onSelectPeriod(YearMonthPeriod.current())
                    } else {
                        onSelectPeriod(null)
                    }
                }
            )
        }

        // Pending Email Invites Banner
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

        // Hero Monthly Budget Ring Card
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

        // Expandable Card 1: Monthly Graph & Category Pie Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy))
                    .testTag("dashboard_charts_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { chartsExpanded = !chartsExpanded }
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PieChart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Charts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            imageVector = if (chartsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Charts",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = chartsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, end = 18.dp, bottom = 18.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            MonthlyBarGraph(
                                bars = uiState.monthlyTrendBars,
                                currencySymbol = currency,
                                onSelectMonth = { period -> onSelectPeriod(period) }
                            )

                            if (categoryTotals.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                Spacer(modifier = Modifier.height(10.dp))

                                CategoryDonutChart(
                                    categoryTotals = categoryTotals,
                                    currencySymbol = currency,
                                    onCategoryClick = { cat -> onSelectCategoryAndOpenLedger(cat) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Expandable Card 2: Accounts & Phone SMS Sync
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { accountsExpanded = !accountsExpanded }
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Accounts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
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
                            Icon(
                                imageVector = if (accountsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Accounts",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    AnimatedVisibility(visible = accountsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, end = 18.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                            AccountsCarouselStrip(
                                accounts = uiState.accounts,
                                currencySymbol = currency,
                                onAddAccountClick = onAddAccountClick
                            )

                            if (uiState.household == null) {
                                OutlinedButton(
                                    onClick = onOpenHouseholdTab,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("setup_household_cta")
                                ) {
                                    Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Link Family")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onOpenTransactionsTab,
                    modifier = Modifier.testTag("view_all_transactions_button")
                ) {
                    Text("See All")
                }
            }
        }

        if (uiState.activeExpenses.isEmpty()) {
            item {
                EmptyLedgerPlaceholder(
                    hasAnyTransactionsInOtherMonths = uiState.allScopedExpenses.isNotEmpty(),
                    onShowAllTimeClick = { onSelectPeriod(null) },
                    onScanSmsClick = onScanSmsClick,
                    onAddTransactionClick = onAddTransactionClick
                )
            }
        } else {
            items(
                items = uiState.activeExpenses.take(12),
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
                        onQuickChangeCategory = { onQuickChangeCategory(expense) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionsLedgerTabScreen(
    uiState: AxioDashboardUiState,
    onStepMonth: (Int) -> Unit,
    onSelectPeriod: (YearMonthPeriod?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectType: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onSelectPaymentMode: (String) -> Unit,
    onSelectAccount: (String) -> Unit,
    onSelectTag: (String) -> Unit,
    onSelectMember: (String) -> Unit,
    onSelectSortOrder: (String) -> Unit,
    onClearFilters: () -> Unit,
    onScanSmsClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onEditTransaction: (ExpenseTransaction) -> Unit,
    onQuickChangeCategory: (ExpenseTransaction) -> Unit,
    onToggleTransactionPrivacy: (ExpenseTransaction) -> Unit,
    onDeleteTransaction: (ExpenseTransaction) -> Unit
) {
    val currency = uiState.userProfile?.currencySymbol ?: "₹"
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }

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
            .testTag("transactions_ledger_list"),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Month / Year & All-Time Selector
        item {
            MonthPeriodSelectorBar(
                selectedPeriod = uiState.selectedPeriod,
                periodLabel = uiState.periodLabel,
                onStepMonth = onStepMonth,
                onSelectCurrentMonth = { onSelectPeriod(YearMonthPeriod.current()) },
                onToggleAllTime = {
                    if (uiState.selectedPeriod == null) {
                        onSelectPeriod(YearMonthPeriod.current())
                    } else {
                        onSelectPeriod(null)
                    }
                }
            )
        }

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
                        onClick = { filtersExpanded = !filtersExpanded }
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

                // Expandable Filter Drawer (Progressive Disclosure)
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

                            // Category Filter Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val categories = listOf("All") + uiState.availableCategories
                                categories.forEach { cat ->
                                    FilterChip(
                                        selected = uiState.selectedCategoryFilter.equals(cat, ignoreCase = true),
                                        onClick = { onSelectCategory(cat) },
                                        label = { Text(cat) }
                                    )
                                }
                            }

                            // Payment Mode Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("All", "UPI", "CARD", "BANK", "CASH").forEach { mode ->
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
                                    TextButton(onClick = onClearFilters) {
                                        Text("Reset")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.filteredExpenses.isEmpty()) {
            item {
                EmptyLedgerPlaceholder(
                    hasAnyTransactionsInOtherMonths = uiState.allScopedExpenses.isNotEmpty(),
                    onShowAllTimeClick = { onSelectPeriod(null) },
                    onScanSmsClick = onScanSmsClick,
                    onAddTransactionClick = onAddTransactionClick
                )
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
}

@Composable
private fun EmptyLedgerPlaceholder(
    hasAnyTransactionsInOtherMonths: Boolean,
    onShowAllTimeClick: () -> Unit,
    onScanSmsClick: () -> Unit,
    onAddTransactionClick: () -> Unit
) {
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
                if (hasAnyTransactionsInOtherMonths) {
                    Button(
                        onClick = onShowAllTimeClick,
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
