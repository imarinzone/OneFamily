package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.data.model.Household
import com.example.ui.components.AccountsCarouselStrip
import com.example.ui.components.ExpenseTransactionItemCard
import com.example.ui.components.MonthlyBudgetPulseCard
import com.example.ui.components.formatCurrency
import com.example.ui.theme.BudgetDangerCoral
import com.example.ui.theme.BudgetSafeGreen
import com.example.ui.theme.BudgetWarnAmber
import com.example.ui.viewmodel.AxioDashboardUiState

@Composable
fun DashboardTabScreen(
    uiState: AxioDashboardUiState,
    onEditMonthlyBudget: () -> Unit,
    onScanSmsClick: () -> Unit,
    onOpenSmsTesterClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onAddAccountClick: () -> Unit,
    onOpenHouseholdTab: () -> Unit,
    onOpenTransactionsTab: () -> Unit,
    onAcceptEmailInvite: (Household) -> Unit,
    onToggleTransactionPrivacy: (ExpenseTransaction) -> Unit,
    onDeleteTransaction: (ExpenseTransaction) -> Unit
) {
    val currency = uiState.userProfile?.currencySymbol ?: "₹"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_lazy_column"),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pending Email Invites Banner
        if (uiState.pendingEmailInvites.isNotEmpty()) {
            item {
                val invite = uiState.pendingEmailInvites.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
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
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Household Invitation",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Join '${invite.name}' to sync shared family budgets in real time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onAcceptEmailInvite(invite) },
                            modifier = Modifier.testTag("accept_invite_button")
                        ) {
                            Text("Accept")
                        }
                    }
                }
            }
        }

        // Main Monthly Budget Pulse Card
        item {
            MonthlyBudgetPulseCard(
                totalSpent = uiState.totalMonthlySpent,
                totalIncome = uiState.totalMonthlyIncome,
                monthlyBudget = uiState.activeMonthlyBudget,
                dailySafeSpend = uiState.dailySafeSpend,
                remainingDays = uiState.remainingDaysInMonth,
                currencySymbol = currency,
                scopeLabel = uiState.selectedScope.label,
                household = uiState.household,
                onEditBudgetClick = onEditMonthlyBudget
            )
        }

        // Quick Action Row: Cashiro SMS Sync, SMS Tester, Household Link
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onScanSmsClick,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("scan_sms_button")
                ) {
                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sync SMS", maxLines = 1)
                }

                OutlinedButton(
                    onClick = onOpenSmsTesterClick,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_bank_sms_button")
                ) {
                    Text(
                        text = if (uiState.smsCandidates.isNotEmpty()) "Review SMS (${uiState.smsCandidates.size})" else "Bank SMS Parser",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Family Household Prompt if not yet linked
        if (uiState.household == null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Share Budget with Family",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Create or join a household with a 6-digit code or email invite for real-time shared tracking.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = onOpenHouseholdTab,
                            modifier = Modifier.testTag("setup_household_cta")
                        ) {
                            Text("Link Now")
                        }
                    }
                }
            }
        }

        // Accounts, Cards & Wallets Strip
        item {
            AccountsCarouselStrip(
                accounts = uiState.accounts,
                currencySymbol = currency,
                onAddAccountClick = onAddAccountClick
            )
        }

        // Category Budget Watchlist (Top 3 categories or alerts)
        val topCategoryBudgets = uiState.categoryProgressList.filter { it.limit > 0 }.take(3)
        if (topCategoryBudgets.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Category Budget Watchlist",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        topCategoryBudgets.forEach { catProgress ->
                            val barColor = when {
                                catProgress.isOverBudget -> BudgetDangerCoral
                                catProgress.isNearAlert -> BudgetWarnAmber
                                else -> BudgetSafeGreen
                            }
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = catProgress.category,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        if (catProgress.isOverBudget || catProgress.isNearAlert) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.WarningAmber,
                                                contentDescription = "Budget Alert",
                                                tint = barColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${formatCurrency(catProgress.spent, currency)} / ${formatCurrency(catProgress.limit, currency)} (${catProgress.percentage}%)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = barColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { catProgress.progressFraction.coerceIn(0f, 1f) },
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

        // Recent Transactions Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions (${uiState.activeExpenses.size})",
                    style = MaterialTheme.typography.titleMedium
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
                    onAddTransactionClick = onAddTransactionClick,
                    onOpenSmsTesterClick = onOpenSmsTesterClick
                )
            }
        } else {
            items(
                items = uiState.activeExpenses.take(8),
                key = { "${it.visibility}_${it.id}" }
            ) { expense ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ExpenseTransactionItemCard(
                        expense = expense,
                        currencySymbol = currency,
                        canTogglePrivacy = uiState.household != null,
                        onTogglePrivacy = { onToggleTransactionPrivacy(expense) },
                        onDelete = { onDeleteTransaction(expense) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionsLedgerTabScreen(
    uiState: AxioDashboardUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onSelectTag: (String) -> Unit,
    onSelectMember: (String) -> Unit,
    onClearFilters: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onOpenSmsTesterClick: () -> Unit,
    onToggleTransactionPrivacy: (ExpenseTransaction) -> Unit,
    onDeleteTransaction: (ExpenseTransaction) -> Unit
) {
    val currency = uiState.userProfile?.currencySymbol ?: "₹"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("transactions_ledger_list"),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search merchant, #tag, note, or bank...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf("All") + ExpenseCategories.ALL
                    categories.forEach { cat ->
                        FilterChip(
                            selected = uiState.selectedCategoryFilter.equals(cat, ignoreCase = true),
                            onClick = { onSelectCategory(cat) },
                            label = { Text(cat) }
                        )
                    }
                }

                // Household Member & #Tag Filter Strip
                val members = uiState.memberSpendMap.keys.toList()
                val tags = uiState.allTagsWithSpend.map { it.first }
                if (members.size > 1 || tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (members.size > 1) {
                            FilterChip(
                                selected = uiState.selectedMemberFilter == "All",
                                onClick = { onSelectMember("All") },
                                label = { Text("All Members") }
                            )
                            members.forEach { member ->
                                FilterChip(
                                    selected = uiState.selectedMemberFilter.equals(member, ignoreCase = true),
                                    onClick = { onSelectMember(member) },
                                    label = { Text(member) }
                                )
                            }
                        }
                        tags.forEach { tag ->
                            FilterChip(
                                selected = uiState.selectedTagFilter.equals(tag, ignoreCase = true),
                                onClick = {
                                    if (uiState.selectedTagFilter.equals(tag, ignoreCase = true)) {
                                        onSelectTag("All")
                                    } else {
                                        onSelectTag(tag)
                                    }
                                },
                                label = { Text("#$tag") }
                            )
                        }
                    }
                }

                if (uiState.searchQuery.isNotBlank() ||
                    uiState.selectedCategoryFilter != "All" ||
                    uiState.selectedTagFilter != "All" ||
                    uiState.selectedMemberFilter != "All"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Showing ${uiState.filteredExpenses.size} matching transactions",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onClearFilters) {
                            Text("Reset Filters")
                        }
                    }
                }
            }
        }

        if (uiState.filteredExpenses.isEmpty()) {
            item {
                EmptyLedgerPlaceholder(
                    onAddTransactionClick = onAddTransactionClick,
                    onOpenSmsTesterClick = onOpenSmsTesterClick
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
                        onTagClick = { clickedTag -> onSelectTag(clickedTag) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLedgerPlaceholder(
    onAddTransactionClick: () -> Unit,
    onOpenSmsTesterClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No Transactions Logged Yet",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Import bank, card & UPI alerts automatically with the Cashiro Bank SMS Parser or log a cash/card expense manually.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onOpenSmsTesterClick,
                    modifier = Modifier.testTag("empty_state_sms_parser_button")
                ) {
                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Bank SMS")
                }
                Button(
                    onClick = onAddTransactionClick,
                    modifier = Modifier.testTag("empty_state_add_expense_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Expense")
                }
            }
        }
    }
}
