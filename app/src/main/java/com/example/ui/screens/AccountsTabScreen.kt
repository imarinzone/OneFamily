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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.AccountSectionType
import com.example.data.model.BudgetScope
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FinancialAccount
import com.example.ui.components.CircularTonalIconButton
import com.example.ui.components.CustomDateRangePickerModal
import com.example.ui.components.EditorialScreenHeader
import com.example.ui.components.PerforatedSectionHeader
import com.example.ui.components.ReceiptDashedDividerLine
import com.example.ui.components.ReceiptTicketCard
import com.example.ui.components.ScopeSwitcherBar
import com.example.ui.components.SquircleIconBadge
import com.example.ui.components.categoryIcon
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatShortDate
import com.example.ui.theme.BudgetDangerCoral
import com.example.ui.theme.BudgetSafeGreen
import com.example.ui.theme.BudgetWarnAmber
import com.example.ui.theme.HouseholdBadgeIndigo
import com.example.ui.theme.LocalAppDimens
import com.example.ui.theme.ReceiptMonoFontFamily
import com.example.ui.theme.categoryAccentColor
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
 * - Each account features inline monthly spend limit tracking & full list of transactions for that account.
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
    onAddAccountClick: (initialAccountType: String) -> Unit = {},
    onEditTransaction: (ExpenseTransaction) -> Unit = {},
    onQuickChangeCategory: (ExpenseTransaction) -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    val currency = uiState.activeCurrencySymbol
    val performHaptic = rememberAppHapticPerformer()

    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    var showCustomRangePicker by rememberSaveable { mutableStateOf(false) }
    var activeInlineAccountId by rememberSaveable { mutableStateOf<String?>(null) }
    var inlineLimitInput by rememberSaveable { mutableStateOf("") }
    val expandedTransactionsMap = remember { mutableStateMapOf<String, Boolean>() }

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
                    ExpenseCategories.normalizeCategory(tx.category).equals("Salary & Income", ignoreCase = true)
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

    val totalSpendAllAccounts = remember(filteredAccountSummaries) {
        filteredAccountSummaries.sumOf { it.spent }
    }
    val totalIncomeAllAccounts = remember(filteredAccountSummaries) {
        filteredAccountSummaries.sumOf { it.income }
    }
    val activeLimitCount = remember(filteredAccountSummaries) {
        filteredAccountSummaries.count { it.limit > 0.0 }
    }
    val overLimitCount = remember(filteredAccountSummaries) {
        filteredAccountSummaries.count { it.isOverLimit }
    }

    val sectionOrder = remember {
        listOf(
            AccountSectionType.WALLET,
            AccountSectionType.BANK,
            AccountSectionType.DEBIT_CARD,
            AccountSectionType.CREDIT_CARD
        )
    }
    val groupedBySection = remember(filteredAccountSummaries) {
        sectionOrder.associateWith { section ->
            filteredAccountSummaries.filter { it.section == section }
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
                countPillText = "${filteredAccountSummaries.size} accounts",
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
                    val isTransactionsExpanded = expandedTransactionsMap[summary.account.id]
                        ?: summary.transactions.isNotEmpty()

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
                            isTransactionsExpanded = isTransactionsExpanded,
                            onToggleLimitEditor = { activateInlineEditor(summary) },
                            onInlineLimitInputChange = { inlineLimitInput = sanitizeDecimalInput(it) },
                            onSaveInlineLimit = { parsedLimit ->
                                onSaveAccountSpendLimitInline(summary.account, parsedLimit)
                                activeInlineAccountId = null
                            },
                            onCancelInlineLimit = { activeInlineAccountId = null },
                            onToggleTransactionsExpanded = {
                                performHaptic()
                                expandedTransactionsMap[summary.account.id] = !isTransactionsExpanded
                            },
                            onEditTransaction = onEditTransaction,
                            onQuickChangeCategory = onQuickChangeCategory
                        )
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
    isTransactionsExpanded: Boolean,
    onToggleLimitEditor: () -> Unit,
    onInlineLimitInputChange: (String) -> Unit,
    onSaveInlineLimit: (Double) -> Unit,
    onCancelInlineLimit: () -> Unit,
    onToggleTransactionsExpanded: () -> Unit,
    onEditTransaction: (ExpenseTransaction) -> Unit,
    onQuickChangeCategory: (ExpenseTransaction) -> Unit
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
            )
            .testTag("account_card_$safeTag"),
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
                Spacer(modifier = Modifier.width(8.dp))
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

            // Inline Spend Limit Numeric Editor (no modal opened)
            AnimatedVisibility(visible = isEditingLimit) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inlineLimitInput,
                        onValueChange = onInlineLimitInputChange,
                        label = { Text("Spend Limit ($currency)") },
                        placeholder = { Text("0 to clear") },
                        isError = inlineError != null,
                        supportingText = inlineError?.let { err -> { Text(err) } },
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
            }

            // Spend Limit Progress Tracker (similar to Budgets)
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
                        text = "${summary.percentage}% used",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                        color = barColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    val diff = summary.limit - summary.spent
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

            // Per-Account Transactions Header & List
            ReceiptDashedDividerLine()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = summary.transactions.isNotEmpty()) {
                        onToggleTransactionsExpanded()
                    }
                    .padding(vertical = 4.dp)
                    .testTag("toggle_account_transactions_$safeTag"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (summary.transactions.isEmpty()) {
                            "No receipts in $periodLabel"
                        } else {
                            "${summary.transactions.size} transactions in $periodLabel"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (summary.transactions.isNotEmpty()) {
                    Icon(
                        imageVector = if (isTransactionsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Account Transactions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = isTransactionsExpanded && summary.transactions.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    summary.transactions.forEach { tx ->
                        val catColor = categoryAccentColor(tx.category)
                        val isIncome = tx.type.equals("INCOME", ignoreCase = true) ||
                            ExpenseCategories.normalizeCategory(tx.category).equals("Salary & Income", ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    performHaptic()
                                    onEditTransaction(tx)
                                }
                                .testTag("account_tx_row_${tx.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .combinedClickable(
                                            onClick = {
                                                performHaptic()
                                                onEditTransaction(tx)
                                            },
                                            onLongClick = {
                                                performHaptic()
                                                onQuickChangeCategory(tx)
                                            }
                                        )
                                        .testTag("account_tx_category_icon_${tx.id}")
                                ) {
                                    SquircleIconBadge(
                                        icon = categoryIcon(tx.category),
                                        tint = catColor,
                                        containerColor = catColor.copy(alpha = 0.14f),
                                        size = 34.dp,
                                        iconSize = 18.dp,
                                        contentDescription = tx.category
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = tx.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (tx.isExcludedFromExpense) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Excluded",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.testTag("account_tx_excluded_badge_${tx.id}")
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${formatShortDate(tx.transactionDateMillis)} • ${tx.category} • ${tx.paymentMode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isIncome) {
                                        "+${formatCurrency(tx.amount, currency)}"
                                    } else {
                                        "-${formatCurrency(tx.amount, currency)}"
                                    },
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isIncome) BudgetSafeGreen else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

