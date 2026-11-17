package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountLimitPeriod
import com.example.data.model.AccountSectionType
import com.example.data.model.BudgetScope
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FinancialAccount
import com.example.ui.components.BillingCycleCalendarGrid
import com.example.ui.components.CircularTonalIconButton
import com.example.ui.components.CustomDateRangePickerModal
import com.example.ui.components.EditorialScreenHeader
import com.example.ui.components.PerforatedSectionHeader
import com.example.ui.components.ReceiptDashedDividerLine
import com.example.ui.components.ReceiptTicketCard
import com.example.ui.components.ScopeSwitcherBar
import com.example.ui.components.SquircleIconBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.BudgetDangerCoral
import com.example.ui.theme.BudgetSafeGreen
import com.example.ui.theme.BudgetWarnAmber
import com.example.ui.theme.HouseholdBadgeIndigo
import com.example.ui.theme.LocalAppDimens
import com.example.ui.theme.ReceiptMonoFontFamily
import com.example.ui.theme.groupedCardShape
import com.example.ui.theme.rememberAppHapticPerformer
import com.example.ui.viewmodel.AccountSpendSummary
import com.example.ui.viewmodel.DateRangePreset
import com.example.ui.viewmodel.OneFamilyDashboardUiState

/**
 * Accounts Screen (`AccountsTabScreen`):
 * - Editorial M3 Header + Serrated Thermal Paper Receipt Accounts Ledger summary card.
 * - Full Budget filter parity: Household/Personal scope switcher + DateRangePreset filter chips + Custom Range picker + Search/Type filter (including Excluded).
 * - Grouped cleanly into 4 distinct sections:
 *   1. Wallets & Cash
 *   2. Bank Accounts
 *   3. Debit Cards (Linked to Bank)
 *   4. Credit Cards
 * - Transactions stay hidden on the accounts list until the user taps an account to open its dedicated detail & graph page.
 */
@Composable
fun AccountsTabScreen(
    uiState: OneFamilyDashboardUiState,
    onScopeSelected: (BudgetScope) -> Unit = {},
    onSelectDateRangePreset: (DateRangePreset) -> Unit = {},
    onSelectCustomDateRange: (Long, Long) -> Unit = { _, _ -> },
    onSearchQueryChange: (String) -> Unit = {},
    onSelectType: (String) -> Unit = {},
    onClearFilters: () -> Unit = {},
    onSaveAccountSpendLimitInline: (account: FinancialAccount, monthlyLimit: Double) -> Unit = { _, _ -> },
    onSaveAccountSpendLimitWithPeriod: (
        (account: FinancialAccount, monthlyLimit: Double, period: String, startMillis: Long, endMillis: Long) -> Unit
    )? = null,
    onSaveAccountSpendLimitWithBillingCycle: (
        (account: FinancialAccount, monthlyLimit: Double, billingCycleDay: Int) -> Unit
    )? = null,
    onToggleAccountHidden: (AccountSpendSummary, Boolean) -> Unit = { _, _ -> },
    onDeleteOrUntrackAccount: (AccountSpendSummary) -> Unit = {},
    onAddAccountClick: (initialAccountType: String) -> Unit = {},
    onAccountClick: (accountId: String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onEditTransaction: (ExpenseTransaction) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onQuickChangeCategory: (ExpenseTransaction) -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    val currency = uiState.activeCurrencySymbol
    val performHaptic = rememberAppHapticPerformer()

    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    var showCustomRangePicker by rememberSaveable { mutableStateOf(false) }
    var activeInlineAccountId by rememberSaveable { mutableStateOf<String?>(null) }
    var inlineLimitInput by rememberSaveable { mutableStateOf("") }
    var inlineBillingCycleDayInput by rememberSaveable { mutableStateOf("1") }
    var showAllAccountsExpanded by rememberSaveable { mutableStateOf(false) }
    var pendingDeleteAccount by remember { mutableStateOf<AccountSpendSummary?>(null) }

    val hasActiveFilter = uiState.selectedDateRangePreset != DateRangePreset.MONTH ||
        uiState.searchQuery.isNotBlank() ||
        uiState.selectedTypeFilter != "All"

    // Apply optional local Search & Type filter cleanly on each account's transactions
    val filteredAccountSummaries = remember(
        uiState.accountSummaries,
        uiState.searchQuery,
        uiState.selectedTypeFilter
    ) {
        uiState.accountSummaries.map { summary ->
            val filteredTxs = summary.transactions.filter { tx ->
                val matchesQuery = uiState.searchQuery.isBlank() ||
                    tx.title.contains(uiState.searchQuery, ignoreCase = true) ||
                    tx.category.contains(uiState.searchQuery, ignoreCase = true) ||
                    tx.notes.contains(uiState.searchQuery, ignoreCase = true) ||
                    summary.account.name.contains(uiState.searchQuery, ignoreCase = true) ||
                    summary.linkedBankDisplayName.contains(uiState.searchQuery, ignoreCase = true)
                val isIncome = tx.type.equals("INCOME", ignoreCase = true) ||
                    ExpenseCategories.isIncomeCategory(tx.category)
                val matchesType = when {
                    uiState.selectedTypeFilter == "All" -> true
                    uiState.selectedTypeFilter.equals("INCOME", ignoreCase = true) -> isIncome
                    uiState.selectedTypeFilter.equals("EXPENSE", ignoreCase = true) -> !isIncome && !tx.isExcludedFromExpense
                    uiState.selectedTypeFilter.equals("EXCLUDED", ignoreCase = true) -> tx.isExcludedFromExpense
                    else -> true
                }
                matchesQuery && matchesType
            }
            summary.copy(transactions = filteredTxs)
        }
    }

    val visibleAccountSummaries = remember(filteredAccountSummaries) {
        filteredAccountSummaries.filter { !it.isHidden }
    }
    val hiddenAccountCount = remember(filteredAccountSummaries) {
        filteredAccountSummaries.count { it.isHidden }
    }

    val totalSpendAllAccounts = remember(visibleAccountSummaries) {
        visibleAccountSummaries.sumOf { it.spent }
    }
    val totalIncomeAllAccounts = remember(visibleAccountSummaries) {
        visibleAccountSummaries.sumOf { it.income }
    }
    val activeLimitCount = remember(visibleAccountSummaries) {
        visibleAccountSummaries.count { it.limit > 0.0 }
    }
    val overLimitCount = remember(visibleAccountSummaries) {
        visibleAccountSummaries.count { it.isOverLimit }
    }

    val sectionOrder = remember {
        listOf(
            AccountSectionType.WALLET,
            AccountSectionType.BANK,
            AccountSectionType.DEBIT_CARD,
            AccountSectionType.CREDIT_CARD
        )
    }
    val groupedBySection = remember(visibleAccountSummaries) {
        sectionOrder.associateWith { section ->
            visibleAccountSummaries.filter { it.section == section }
        }
    }

    val activateInlineEditor: (AccountSpendSummary) -> Unit = { summary ->
        performHaptic()
        if (activeInlineAccountId == summary.account.id) {
            activeInlineAccountId = null
        } else {
            activeInlineAccountId = summary.account.id
            inlineLimitInput = if (summary.limit > 0.0) {
                if (summary.limit % 1.0 == 0.0) summary.limit.toLong().toString() else summary.limit.toString()
            } else {
                ""
            }
            inlineBillingCycleDayInput = summary.account.billingCycleDay.coerceIn(1, 31).toString()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("accounts_tab_list"),
        contentPadding = PaddingValues(top = 6.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 0. Editorial M3 Header
        item {
            EditorialScreenHeader(
                title = "Accounts",
                countPillText = if (hiddenAccountCount > 0) {
                    "${visibleAccountSummaries.size} shown • $hiddenAccountCount hidden"
                } else {
                    "${visibleAccountSummaries.size} accounts"
                },
                subtitle = uiState.periodLabel,
                modifier = Modifier.padding(bottom = 8.dp),
                actions = {
                    CircularTonalIconButton(
                        icon = Icons.Default.FilterList,
                        contentDescription = "Account Filters",
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
                        modifier = Modifier.testTag("accounts_toggle_filters_button")
                    )
                    CircularTonalIconButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Add Account",
                        onClick = { onAddAccountClick("BANK") },
                        modifier = Modifier.testTag("accounts_add_account_button")
                    )
                }
            )
        }

        // 1. Scope Switcher (same as Home/Budgets when Household feature is enabled)
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

        // 2. Budget-Parity Date Range Filter Chips Strip + Collapsible Search/Type Drawer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding)
                    .padding(bottom = 10.dp)
                    .animateContentSize(
                        spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .testTag("accounts_date_range_row"),
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
                            modifier = Modifier.testTag("accounts_date_range_chip_${preset.name.lowercase()}")
                        )
                    }
                }

                AnimatedVisibility(visible = filtersExpanded || uiState.searchQuery.isNotBlank() || uiState.selectedTypeFilter != "All") {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = onSearchQueryChange,
                                placeholder = { Text("Search account, linked bank, or receipt...") },
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
                                    .testTag("accounts_search_input")
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    "All" to "All",
                                    "EXPENSE" to "Expenses",
                                    "INCOME" to "Income",
                                    "EXCLUDED" to "Excluded"
                                ).forEach { (typeVal, label) ->
                                    FilterChip(
                                        selected = uiState.selectedTypeFilter.equals(typeVal, ignoreCase = true),
                                        onClick = {
                                            performHaptic()
                                            onSelectType(typeVal)
                                        },
                                        label = { Text(label) },
                                        modifier = Modifier.testTag("accounts_type_chip_${typeVal.lowercase()}")
                                    )
                                }
                                if (hasActiveFilter) {
                                    TextButton(
                                        onClick = {
                                            performHaptic()
                                            onClearFilters()
                                        },
                                        modifier = Modifier.testTag("accounts_clear_filters_button")
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

        // 3. Serrated Paper Receipt Summary Card
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = dimens.horizontalPadding)
                    .padding(bottom = 12.dp)
            ) {
                ReceiptTicketCard(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(modifier = Modifier.padding(dimens.cardPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Accounts Ledger",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (overLimitCount > 0) {
                                    BudgetDangerCoral.copy(alpha = 0.16f)
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                }
                            ) {
                                Text(
                                    text = if (overLimitCount > 0) {
                                        "$overLimitCount over limit"
                                    } else {
                                        "$activeLimitCount limits set"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (overLimitCount > 0) {
                                        BudgetDangerCoral
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Period Spend",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = formatCurrency(totalSpendAllAccounts, currency),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 1
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Period Income",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = formatCurrency(totalIncomeAllAccounts, currency),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Four Account Sections: Wallet, Bank, Debit Card Linked to Bank, Credit Card
        sectionOrder.forEach { section ->
            val sectionItems = groupedBySection[section].orEmpty()
            val sectionTag = section.name.lowercase()
            val addAccountDefaultType = when (section) {
                AccountSectionType.WALLET -> "WALLET"
                AccountSectionType.BANK -> "BANK"
                AccountSectionType.DEBIT_CARD -> "DEBIT_CARD"
                AccountSectionType.CREDIT_CARD -> "CREDIT_CARD"
            }

            item(key = "section_header_$sectionTag") {
                PerforatedSectionHeader(
                    title = section.title,
                    countBadge = "${sectionItems.size}",
                    modifier = Modifier
                        .padding(top = 6.dp, bottom = 4.dp)
                        .testTag("section_header_$sectionTag")
                )
            }

            if (sectionItems.isEmpty()) {
                item(key = "section_empty_$sectionTag") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.horizontalPadding, vertical = 4.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "No ${section.title.lowercase()} added yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            FilledTonalButton(
                                onClick = {
                                    performHaptic()
                                    onAddAccountClick(addAccountDefaultType)
                                },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("empty_section_add_$sectionTag")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add")
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = sectionItems,
                    key = { _, item -> "acc_${sectionTag}_${item.account.id}" }
                ) { idx, summary ->
                    val stackShape = groupedCardShape(index = idx, count = sectionItems.size)
                    val safeTag = summary.account.name.lowercase()
                        .replace(Regex("[^a-z0-9]+"), "_")
                        .trim('_')
                        .ifBlank { summary.account.id }
                    val isEditingLimit = activeInlineAccountId == summary.account.id

                    Box(
                        modifier = Modifier.padding(
                            horizontal = dimens.horizontalPadding,
                            vertical = 1.5.dp
                        )
                    ) {
                        AccountCardWithTransactions(
                            summary = summary,
                            currency = currency,
                            periodLabel = uiState.periodLabel,
                            safeTag = safeTag,
                            cardShape = stackShape,
                            isEditingLimit = isEditingLimit,
                            inlineLimitInput = inlineLimitInput,
                            inlineBillingCycleDayInput = inlineBillingCycleDayInput,
                            onAccountClick = {
                                performHaptic()
                                onAccountClick(summary.account.id)
                            },
                            onToggleLimitEditor = { activateInlineEditor(summary) },
                            onInlineLimitInputChange = { inlineLimitInput = sanitizeDecimalInput(it) },
                            onInlineBillingCycleDayChange = { raw ->
                                inlineBillingCycleDayInput = raw.filter { it.isDigit() }.take(2)
                            },
                            onSaveInlineLimit = { parsedLimit ->
                                val parsedCycleDay = (inlineBillingCycleDayInput.trim().toIntOrNull()
                                    ?: summary.account.billingCycleDay)
                                    .coerceIn(1, 31)
                                when {
                                    onSaveAccountSpendLimitWithBillingCycle != null -> {
                                        onSaveAccountSpendLimitWithBillingCycle(
                                            summary.account,
                                            parsedLimit,
                                            parsedCycleDay
                                        )
                                    }
                                    onSaveAccountSpendLimitWithPeriod != null -> {
                                        onSaveAccountSpendLimitWithPeriod(
                                            summary.account.copy(billingCycleDay = parsedCycleDay),
                                            parsedLimit,
                                            AccountLimitPeriod.MONTHLY.name,
                                            0L,
                                            0L
                                        )
                                    }
                                    else -> {
                                        onSaveAccountSpendLimitInline(
                                            summary.account.copy(billingCycleDay = parsedCycleDay),
                                            parsedLimit
                                        )
                                    }
                                }
                                activeInlineAccountId = null
                            },
                            onCancelInlineLimit = { activeInlineAccountId = null },
                            onToggleHidden = {
                                performHaptic()
                                onToggleAccountHidden(summary, !summary.isHidden)
                            },
                            onRequestDelete = {
                                performHaptic()
                                pendingDeleteAccount = summary
                            }
                        )
                    }
                }
            }
        }

        // 5. Show All Accounts Bottom Section (allows unhiding hidden accounts or deleting/untracking any account)
        if (filteredAccountSummaries.isNotEmpty()) {
            item(key = "accounts_show_all_footer") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            performHaptic()
                            showAllAccountsExpanded = !showAllAccountsExpanded
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("accounts_show_all_button")
                    ) {
                        Icon(
                            imageVector = if (showAllAccountsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val hiddenSuffix = if (hiddenAccountCount > 0) " • $hiddenAccountCount hidden" else ""
                        Text(
                            text = if (showAllAccountsExpanded) {
                                "Hide All Accounts (${filteredAccountSummaries.size}$hiddenSuffix)"
                            } else {
                                "Show All Accounts (${filteredAccountSummaries.size}$hiddenSuffix)"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (showAllAccountsExpanded) {
                itemsIndexed(
                    items = filteredAccountSummaries,
                    key = { _, s -> "all_acc_${s.account.id}" }
                ) { idx, summary ->
                    val rowShape = groupedCardShape(index = idx, count = filteredAccountSummaries.size)
                    val safeTag = summary.account.name.lowercase()
                        .replace(Regex("[^a-z0-9]+"), "_")
                        .trim('_')
                        .ifBlank { summary.account.id }
                    Card(
                        shape = rowShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (summary.isHidden) {
                                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = dimens.horizontalPadding,
                                end = dimens.horizontalPadding,
                                bottom = 4.dp
                            )
                            .clip(rowShape)
                            .clickable {
                                performHaptic()
                                onAccountClick(summary.account.id)
                            }
                            .testTag("all_accounts_row_$safeTag")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = summary.account.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (summary.isHidden) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier.testTag("all_accounts_hidden_badge_$safeTag")
                                        ) {
                                            Text(
                                                text = "Hidden",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = buildString {
                                        append(summary.section.title)
                                        if (summary.account.lastFourDigits.isNotBlank()) {
                                            append(" • ••${summary.account.lastFourDigits}")
                                        }
                                        append(" • Spent ${formatCurrency(summary.spent, currency)}")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = {
                                    performHaptic()
                                    onToggleAccountHidden(summary, !summary.isHidden)
                                },
                                modifier = Modifier.testTag("all_accounts_toggle_hide_$safeTag")
                            ) {
                                Icon(
                                    imageVector = if (summary.isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (summary.isHidden) "Show Account" else "Hide Account",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = {
                                    performHaptic()
                                    pendingDeleteAccount = summary
                                },
                                modifier = Modifier.testTag("all_accounts_delete_$safeTag")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete or Untrack Account",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
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

    val deleteTarget = pendingDeleteAccount
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteAccount = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.testTag("delete_account_confirm_dialog"),
            title = {
                Text(
                    text = "Delete / Untrack Account?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Remove ${deleteTarget.account.name} from tracked accounts? Existing transactions stay intact, and you can re-add the account anytime."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        performHaptic()
                        onDeleteOrUntrackAccount(deleteTarget)
                        pendingDeleteAccount = null
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("confirm_delete_account_button")
                ) {
                    Text("Delete / Untrack")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteAccount = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountCardWithTransactions(
    summary: AccountSpendSummary,
    currency: String,
    periodLabel: String,
    safeTag: String,
    cardShape: RoundedCornerShape,
    isEditingLimit: Boolean,
    inlineLimitInput: String,
    inlineBillingCycleDayInput: String,
    onAccountClick: () -> Unit,
    onToggleLimitEditor: () -> Unit,
    onInlineLimitInputChange: (String) -> Unit,
    onInlineBillingCycleDayChange: (String) -> Unit,
    onSaveInlineLimit: (Double) -> Unit,
    onCancelInlineLimit: () -> Unit,
    onToggleHidden: () -> Unit,
    onRequestDelete: () -> Unit
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    val account = summary.account
    val barColor = when {
        summary.isOverLimit -> BudgetDangerCoral
        summary.isNearAlert -> BudgetWarnAmber
        else -> BudgetSafeGreen
    }
    val sectionIcon = when (summary.section) {
        AccountSectionType.WALLET -> Icons.Default.AccountBalanceWallet
        AccountSectionType.BANK -> Icons.Default.AccountBalance
        AccountSectionType.DEBIT_CARD, AccountSectionType.CREDIT_CARD -> Icons.Default.CreditCard
    }
    val iconTint = when (summary.section) {
        AccountSectionType.WALLET -> MaterialTheme.colorScheme.tertiary
        AccountSectionType.BANK -> MaterialTheme.colorScheme.primary
        AccountSectionType.DEBIT_CARD -> MaterialTheme.colorScheme.secondary
        AccountSectionType.CREDIT_CARD -> HouseholdBadgeIndigo
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .animateContentSize(
                spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isEditingLimit) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClickLabel = "Open ${account.name} details") { onAccountClick() }
                    .testTag("account_card_$safeTag")
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            // Top Account Identity & Inline Set Limit CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SquircleIconBadge(
                    icon = sectionIcon,
                    tint = iconTint,
                    containerColor = iconTint.copy(alpha = 0.14f),
                    size = dimens.rowIconBoxSize,
                    iconSize = dimens.rowIconSize,
                    contentDescription = summary.section.title
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val metaParts = buildList {
                        if (account.bankName.isNotBlank() && !account.name.contains(account.bankName, ignoreCase = true)) {
                            add(account.bankName)
                        }
                        if (account.lastFourDigits.isNotBlank()) add("••${account.lastFourDigits}")
                        if (account.balance > 0.0) add("Bal ${formatCurrency(account.balance, currency)}")
                        if (summary.section == AccountSectionType.CREDIT_CARD && account.creditLimit > 0.0) {
                            add("Limit ${formatCurrency(account.creditLimit, currency)}")
                        }
                    }
                    if (metaParts.isNotEmpty()) {
                        Text(
                            text = metaParts.joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = buildString {
                            append("Spent ${formatCurrency(summary.spent, currency)}")
                            if (summary.income > 0.0) {
                                append(" • In ${formatCurrency(summary.income, currency)}")
                            }
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                        color = if (summary.isOverLimit) barColor else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                FilledTonalButton(
                    onClick = onToggleLimitEditor,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("set_account_limit_$safeTag")
                ) {
                    Icon(
                        imageVector = if (summary.limit > 0.0) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (summary.limit > 0.0) formatCurrency(summary.limit, currency) else "Set Limit",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = if (summary.limit > 0.0) ReceiptMonoFontFamily else MaterialTheme.typography.labelMedium.fontFamily
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }
                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = {
                            performHaptic()
                            menuExpanded = true
                        },
                        modifier = Modifier.testTag("account_menu_button_$safeTag")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Account Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (summary.section == AccountSectionType.CREDIT_CARD) {
                                        if (summary.limit > 0.0) "Edit Limit & Billing Cycle" else "Set Limit & Billing Cycle"
                                    } else {
                                        if (summary.limit > 0.0) "Edit Limit & Period" else "Set Spend Limit"
                                    }
                                )
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onToggleLimitEditor()
                            }
                        )
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
                                onToggleHidden()
                            },
                            modifier = Modifier.testTag("account_menu_hide_$safeTag")
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
                                onRequestDelete()
                            },
                            modifier = Modifier.testTag("account_menu_delete_$safeTag")
                        )
                    }
                }
            }

            // Linked Bank Pill for Debit Cards
            if (summary.section == AccountSectionType.DEBIT_CARD && summary.linkedBankDisplayName.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.testTag("debit_linked_bank_$safeTag")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Linked Bank Account",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Linked to ${summary.linkedBankDisplayName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Inline Spend Limit Numeric & Period Editor (no modal opened)
            AnimatedVisibility(visible = isEditingLimit) {
                val isCreditCard = summary.section == AccountSectionType.CREDIT_CARD
                val parsedCycleDay = inlineBillingCycleDayInput.trim().toIntOrNull()?.coerceIn(1, 31)
                    ?: account.billingCycleDay.coerceIn(1, 31)
                val cycleRangePreview = remember(isCreditCard, parsedCycleDay) {
                    if (isCreditCard) {
                        com.example.ui.viewmodel.AccountLimitPeriodUtils.formatBillingCycleRange(
                            billingCycleDay = parsedCycleDay,
                            cycleOffset = 0,
                            includeYear = false
                        )
                    } else {
                        ""
                    }
                }
                val inlineError = if (inlineLimitInput.isNotEmpty()) {
                    validateNonNegativeAmount(inlineLimitInput)
                } else {
                    null
                }
                val commitInlineLimit = {
                    if (inlineError == null) {
                        performHaptic()
                        val parsedLimit = inlineLimitInput.trim().toDoubleOrNull() ?: 0.0
                        onSaveInlineLimit(parsedLimit)
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inlineLimitInput,
                            onValueChange = onInlineLimitInputChange,
                            label = {
                                Text(if (isCreditCard) "Billing Cycle Limit ($currency)" else "Spend Limit ($currency)")
                            },
                            placeholder = { Text("0 to clear") },
                            isError = inlineError != null,
                            supportingText = inlineError?.let { err -> { Text(err) } } ?: if (isCreditCard) {
                                { Text("Linked to Day $parsedCycleDay cycle ($cycleRangePreview)") }
                            } else {
                                null
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { commitInlineLimit() }),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("inline_account_limit_input_$safeTag")
                        )
                        Button(
                            onClick = commitInlineLimit,
                            enabled = inlineError == null,
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("inline_save_account_limit_$safeTag")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save Spend Limit",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (summary.limit > 0.0) {
                            IconButton(
                                onClick = {
                                    performHaptic()
                                    onSaveInlineLimit(0.0)
                                },
                                modifier = Modifier.testTag("inline_clear_account_limit_$safeTag")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Spend Limit",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            IconButton(
                                onClick = onCancelInlineLimit,
                                modifier = Modifier.testTag("inline_cancel_account_limit_$safeTag")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Spend Limit Edit"
                                )
                            }
                        }
                    }

                    if (isCreditCard) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("inline_limit_cycle_link_$safeTag")
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
                                    text = "Limit tracks spend across Billing Cycle Day $parsedCycleDay ($cycleRangePreview)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("inline_billing_cycle_section_$safeTag")
                        ) {
                            BillingCycleCalendarGrid(
                                selectedDay = parsedCycleDay,
                                onSelectDay = { day ->
                                    onInlineBillingCycleDayChange(day.toString())
                                },
                                tagPrefix = "inline_billing_cycle_calendar_$safeTag"
                            )
                        }
                    }
                }
            }

            // Spend Limit Progress Tracker (scoped to account's configured Billing Cycle / Monthly period)
            if (summary.limit > 0.0) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { summary.progressFraction.coerceIn(0f, 1f) },
                    color = barColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_limit_status_$safeTag"),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (summary.section == AccountSectionType.CREDIT_CARD) {
                            "${summary.percentage}% of cycle limit • ${summary.limitPeriodLabel}"
                        } else {
                            "${summary.percentage}% used • ${summary.limitPeriodLabel}"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                        color = barColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    val diff = summary.limit - summary.limitSpent
                    Text(
                        text = if (diff >= 0.0) {
                            "${formatCurrency(diff, currency)} remaining"
                        } else {
                            "Over by ${formatCurrency(-diff, currency)}"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                        color = if (diff >= 0.0) MaterialTheme.colorScheme.onSurfaceVariant else barColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Compact Click-Through Footer (no transactions displayed on Accounts list)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_card_open_detail_hint_$safeTag"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = buildString {
                            val count = summary.transactions.size
                            append(if (count == 1) "1 transaction" else "$count transactions")
                            if (summary.section == AccountSectionType.CREDIT_CARD) {
                                append(" • Cycle Day ${account.billingCycleDay.coerceIn(1, 31)}")
                            } else {
                                append(" in $periodLabel")
                            }
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Graph & Txns",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open ${account.name} details",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        }
    }
}


