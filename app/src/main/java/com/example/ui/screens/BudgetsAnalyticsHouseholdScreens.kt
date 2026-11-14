package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Window
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LocalAccountSessionManager
import com.example.data.model.BudgetScope
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FamilyCurrencyOption
import com.example.data.model.Household
import com.example.data.model.WallpaperColorAccent
import com.example.data.model.WallpaperDensity
import com.example.data.model.WallpaperPatternStyle
import com.example.ui.components.AccountsCarouselStrip
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.CircularTonalIconButton
import com.example.ui.components.CustomDateRangePickerModal
import com.example.ui.components.EditorialScreenHeader
import com.example.ui.components.ExpenseTransactionItemCard
import com.example.ui.components.ExpressiveCutoutAppearanceHeroCard
import com.example.ui.components.FinanceWallpaperPreviewBanner
import com.example.ui.components.M3CheckmarkSwitch
import com.example.ui.components.MasterFeatureToggleBanner
import com.example.ui.components.MonthlyBarGraph
import com.example.ui.components.MonthlyBudgetPulseCard
import com.example.ui.components.PerforatedSectionHeader
import com.example.ui.components.PrimaryColorSelectorBar
import com.example.ui.components.ReceiptDashedDividerLine
import com.example.ui.components.ReceiptTicketCard
import com.example.ui.components.ScopeSwitcherBar
import com.example.ui.components.SquircleIconBadge
import com.example.ui.components.ThemeModeSelectorBar
import com.example.ui.components.categoryIcon
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatShortDate
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
import com.example.ui.viewmodel.OneFamilyDashboardUiState
import com.example.ui.viewmodel.YearMonthPeriod
import kotlin.math.roundToInt

/**
 * Home Screen (`HomeTabScreen`):
 * - Oversized M3 Expressive Editorial Header with circular tonal Filter & CSV actions.
 * - Personal & Family section switcher (`ScopeSwitcherBar`) when Household mode is enabled.
 * - Serrated Thermal Paper Receipt Budget Pulse Card + M3 pill search & filter drawer.
 * - Prominent "All Transactions" navigation card to open the dedicated `AllTransactionsScreen` page,
 *   plus a compact Recent Receipts preview stack.
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
    @Suppress("UNUSED_PARAMETER") onSelectAccount: (String) -> Unit = {},
    onSelectTag: (String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSelectMember: (String) -> Unit = {},
    onSelectSortOrder: (String) -> Unit = {},
    onClearFilters: () -> Unit = {},
    onScanSmsClick: () -> Unit = {},
    onExportCsvClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit = {},
    onAcceptEmailInvite: (Household) -> Unit = {},
    onEditTransaction: (ExpenseTransaction) -> Unit = {},
    onQuickChangeCategory: (ExpenseTransaction) -> Unit = {},
    onToggleTransactionPrivacy: (ExpenseTransaction) -> Unit = {},
    onToggleExcludeTransaction: (ExpenseTransaction) -> Unit = {},
    onLinkDuplicateTransaction: (ExpenseTransaction) -> Unit = {},
    onDeleteTransaction: (ExpenseTransaction) -> Unit = {},
    onUndoLastSwipeAction: () -> Unit = {},
    onOpenAllTransactionsPage: () -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    val currency = uiState.activeCurrencySymbol
    val performHaptic = rememberAppHapticPerformer()

    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    var showDrawerCustomRangePicker by rememberSaveable { mutableStateOf(false) }

    val hasActiveFilter = uiState.searchQuery.isNotBlank() ||
        uiState.selectedDateRangePreset != DateRangePreset.MONTH ||
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
        contentPadding = PaddingValues(top = 6.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 0. Editorial M3 Header with Circular Tonal Action Pills
        item {
            EditorialScreenHeader(
                title = "One Family",
                countPillText = "${uiState.filteredExpenses.size} receipts",
                subtitle = uiState.periodLabel,
                modifier = Modifier.padding(bottom = 6.dp),
                actions = {
                    CircularTonalIconButton(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = "All Transactions Page",
                        onClick = {
                            performHaptic()
                            onOpenAllTransactionsPage()
                        },
                        modifier = Modifier.testTag("home_open_transactions_page_button")
                    )
                    CircularTonalIconButton(
                        icon = Icons.Default.Search,
                        contentDescription = "Search Receipts",
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
                        modifier = Modifier.testTag("toggle_search_button")
                    )
                    CircularTonalIconButton(
                        icon = Icons.Default.FilterList,
                        contentDescription = "Filters",
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
                        modifier = Modifier.testTag("toggle_filters_button")
                    )
                    CircularTonalIconButton(
                        icon = Icons.Default.Download,
                        contentDescription = "Export CSV",
                        onClick = onExportCsvClick,
                        modifier = Modifier.testTag("home_export_csv_button")
                    )
                }
            )
        }

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

        // 3. Hero Serrated Paper Receipt Budget Pulse Card
        item {
            Box(modifier = Modifier.padding(bottom = 8.dp)) {
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
                    onEditBudgetClick = onEditMonthlyBudget
                )
            }
        }

        // 3B. Dedicated "All Transactions" Navigation Card + Quick Type Filter Strip (including Excluded)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable {
                            performHaptic()
                            onOpenAllTransactionsPage()
                        }
                        .testTag("open_all_transactions_page_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            SquircleIconBadge(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                                size = 40.dp,
                                iconSize = 20.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "All Transactions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = buildString {
                                        append("${uiState.activeExpenses.size} in ${uiState.periodLabel}")
                                        if (uiState.excludedTransactionsCount > 0) {
                                            append(" • ${uiState.excludedTransactionsCount} excluded")
                                        }
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open All Transactions Page",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Quick Type Filter Strip (All / Expenses / Income / Excluded)
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
                                if (typeVal == "EXCLUDED" && uiState.selectedTypeFilter.equals("EXCLUDED", ignoreCase = true)) {
                                    onSelectType("All")
                                } else {
                                    onSelectType(typeVal)
                                }
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
                            modifier = Modifier.testTag(
                                if (typeVal == "EXCLUDED") {
                                    "quick_excluded_filter_chip"
                                } else {
                                    "quick_type_filter_chip_${typeVal.lowercase()}"
                                }
                            )
                        )
                    }
                }
            }
        }

        // 4. Expandable M3 Search & Filter Drawer
        if (uiState.selectedCategoryFilter != "All" || filtersExpanded || hasActiveFilter) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding)
                        .padding(bottom = 8.dp)
                        .animateContentSize(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.selectedCategoryFilter != "All") {
                        val catAccent = categoryAccentColor(uiState.selectedCategoryFilter)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = catAccent.copy(alpha = 0.16f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    performHaptic()
                                    onSelectCategory("All")
                                }
                                .testTag("active_category_filter_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
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

                    AnimatedVisibility(visible = filtersExpanded || hasActiveFilter) {
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
                                    placeholder = { Text("Search merchant, category, or tag...") },
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
                                        .testTag("transaction_search_input")
                                )
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                            modifier = Modifier.testTag("type_filter_chip_${typeVal.lowercase()}")
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
                                            leadingIcon = if (cat != "All") {
                                                {
                                                    Icon(
                                                        imageVector = categoryIcon(cat),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            } else {
                                                null
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
        }

        // 4B. Quick Undo Bar on Home Screen when a transaction was just swiped
        if (uiState.canUndoLastAction) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding)
                        .padding(bottom = 8.dp)
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

        // 5. Perforated Receipt Section Header
        item {
            PerforatedSectionHeader(
                title = "Recent Receipts",
                countBadge = "${uiState.filteredExpenses.size}",
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // 6. Connected 24dp/6dp Recent Transaction Preview Stack (or Tactile Empty Receipt State)
        if (uiState.filteredExpenses.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 4.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            SquircleIconBadge(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                tint = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            Text(
                                text = "No receipts in period",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (uiState.allScopedExpenses.isNotEmpty()) {
                                FilledTonalButton(
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
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
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
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add")
                            }
                        }
                    }
                }
            }
        } else {
            val recentPreview = uiState.filteredExpenses.take(5)
            itemsIndexed(
                items = recentPreview,
                key = { _, tx -> "${tx.visibility}_${tx.id}" }
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
                        onTagClick = { clickedTag -> onSelectTag(clickedTag) },
                        shape = groupedCardShape(index = idx, count = recentPreview.size),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FilledTonalButton(
                        onClick = {
                            performHaptic()
                            onOpenAllTransactionsPage()
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("view_all_transactions_footer_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open All Transactions (${uiState.filteredExpenses.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
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
 * Dedicated All Transactions Page (`AllTransactionsScreen`):
 * - Houses the full list of receipts/transactions in its own dedicated screen.
 * - Includes Back navigation, full Search & Filter controls, and an "Excluded" filter chip
 *   to inspect transactions excluded from expenses.
 * - Supports long-pressing any transaction's category icon to open the 360° Circular Scroll Wheel category editor.
 */
@Composable
fun AllTransactionsScreen(
    uiState: OneFamilyDashboardUiState,
    onBackClick: () -> Unit = {},
    onScopeSelected: (BudgetScope) -> Unit = {},
    onSelectDateRangePreset: (DateRangePreset) -> Unit = {},
    onSelectCustomDateRange: (Long, Long) -> Unit = { _, _ -> },
    onSearchQueryChange: (String) -> Unit = {},
    onSelectType: (String) -> Unit = {},
    onSelectCategory: (String) -> Unit = {},
    onSelectPaymentMode: (String) -> Unit = {},
    onSelectTag: (String) -> Unit = {},
    onSelectSortOrder: (String) -> Unit = {},
    onClearFilters: () -> Unit = {},
    onExportCsvClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit = {},
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
    var showCustomRangePicker by rememberSaveable { mutableStateOf(false) }

    val hasActiveFilter = uiState.searchQuery.isNotBlank() ||
        uiState.selectedDateRangePreset != DateRangePreset.MONTH ||
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
            .testTag("all_transactions_screen_list"),
        contentPadding = PaddingValues(top = 6.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 0. Editorial Header with Back Button + Actions
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilledTonalButton(
                        onClick = {
                            performHaptic()
                            onBackClick()
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("all_transactions_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Back", fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                EditorialScreenHeader(
                    title = "All Transactions",
                    countPillText = "${uiState.filteredExpenses.size} receipts",
                    subtitle = uiState.periodLabel,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        // 1. Optional Household Scope Switcher
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

        // 2. Always-Visible Type Filter Strip (All, Expenses, Income, Excluded) + Search Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding)
                    .padding(bottom = 8.dp),
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

                // Active Category Filter Pill
                if (uiState.selectedCategoryFilter != "All") {
                    val catAccent = categoryAccentColor(uiState.selectedCategoryFilter)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = catAccent.copy(alpha = 0.16f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                performHaptic()
                                onSelectCategory("All")
                            }
                            .testTag("all_tx_active_category_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
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

                AnimatedVisibility(visible = filtersExpanded) {
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

        // 4. Full Filtered Transactions Stack
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
                                "No excluded transactions in ${uiState.periodLabel}"
                            } else {
                                "No matching transactions in ${uiState.periodLabel}"
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
                        onTagClick = { clickedTag -> onSelectTag(clickedTag) },
                        shape = groupedCardShape(index = idx, count = uiState.filteredExpenses.size),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
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

/**
 * Analysis Tab (`AnalyticsTabScreen`):
 * - Editorial M3 Header + Serrated Receipt Ticket Card with Bezier-Node 12-Month Trend & Donut Chart.
 * - Clicking any category (either in the Donut Chart or in Category Breakdown) filters the Analysis page
 *   so ONLY that category's transactions are shown.
 * - Long-pressing any transaction's category icon opens the 360° Circular Scroll Wheel category editor.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnalyticsTabScreen(
    uiState: OneFamilyDashboardUiState,
    onStepMonth: (Int) -> Unit,
    onSelectPeriod: (YearMonthPeriod?) -> Unit,
    onSelectCategory: (String) -> Unit = {},
    onEditTransaction: (ExpenseTransaction) -> Unit = {},
    onQuickChangeCategory: (ExpenseTransaction) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onToggleTransactionPrivacy: (ExpenseTransaction) -> Unit = {},
    onLinkDuplicateTransaction: (ExpenseTransaction) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onDeleteTransaction: (ExpenseTransaction) -> Unit = {},
    onOpenAllTransactionsPage: () -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    val currency = uiState.activeCurrencySymbol
    val performHaptic = rememberAppHapticPerformer()
    val activeCategoryItems = remember(uiState.categoryProgressList) {
        uiState.categoryProgressList.filter { it.spent > 0 }
    }
    val categoryTotals = remember(activeCategoryItems) {
        activeCategoryItems.map { it.category to it.spent }
    }

    var selectedAnalysisCategory by rememberSaveable {
        mutableStateOf<String?>(uiState.selectedCategoryFilter.takeIf { it != "All" })
    }
    var moreBreakdownsExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.selectedCategoryFilter) {
        selectedAnalysisCategory = uiState.selectedCategoryFilter.takeIf { it != "All" }
    }

    val toggleCategorySelection: (String) -> Unit = { clickedCategory ->
        performHaptic()
        if (selectedAnalysisCategory != null && selectedAnalysisCategory.equals(clickedCategory, ignoreCase = true)) {
            selectedAnalysisCategory = null
            onSelectCategory("All")
        } else {
            selectedAnalysisCategory = clickedCategory
            onSelectCategory(clickedCategory)
        }
    }

    val allPeriodTransactions = uiState.activeExpenses
    val filteredAnalysisTransactions = remember(allPeriodTransactions, selectedAnalysisCategory) {
        val base = if (selectedAnalysisCategory == null) {
            allPeriodTransactions
        } else {
            allPeriodTransactions.filter { tx ->
                tx.category.equals(selectedAnalysisCategory, ignoreCase = true) ||
                    ExpenseCategories.normalizeCategory(tx.category).equals(selectedAnalysisCategory, ignoreCase = true)
            }
        }
        base.sortedByDescending { it.transactionDateMillis }
    }

    val displayedCategoryItems = remember(activeCategoryItems, selectedAnalysisCategory) {
        if (selectedAnalysisCategory == null) {
            activeCategoryItems
        } else {
            activeCategoryItems.filter { item ->
                item.category.equals(selectedAnalysisCategory, ignoreCase = true) ||
                    ExpenseCategories.normalizeCategory(item.category).equals(selectedAnalysisCategory, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analytics_tab_list"),
        contentPadding = PaddingValues(top = 6.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 0. Editorial M3 Header
        item {
            EditorialScreenHeader(
                title = "Analysis",
                countPillText = "${filteredAnalysisTransactions.size} items",
                subtitle = uiState.periodLabel,
                modifier = Modifier.padding(bottom = 10.dp),
                actions = {
                    CircularTonalIconButton(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = "All Transactions",
                        onClick = {
                            performHaptic()
                            onOpenAllTransactionsPage()
                        },
                        modifier = Modifier.testTag("analytics_open_all_transactions_button")
                    )
                }
            )
        }

        // 0B. Active Category Filter Banner (when a category is clicked on Analysis page)
        if (selectedAnalysisCategory != null) {
            item {
                val activeCat = selectedAnalysisCategory!!
                val catColor = categoryAccentColor(activeCat)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = catColor.copy(alpha = 0.16f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding)
                        .padding(bottom = 10.dp)
                        .testTag("analytics_active_category_filter_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            SquircleIconBadge(
                                icon = categoryIcon(activeCat),
                                tint = catColor,
                                containerColor = catColor.copy(alpha = 0.22f),
                                size = 34.dp,
                                iconSize = 18.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Showing only $activeCat",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${filteredAnalysisTransactions.size} transactions in ${uiState.periodLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        FilledTonalButton(
                            onClick = {
                                performHaptic()
                                selectedAnalysisCategory = null
                                onSelectCategory("All")
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("analytics_clear_category_filter_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Category Filter",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Show All")
                        }
                    }
                }
            }
        }

        // 1. Serrated Receipt Ticket Card: Scrollable 12-Month Bezier Graph + Category Donut
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Monthly Trend",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Scroll freely • Tap a month to inspect receipts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.testTag("analytics_selected_period_pill")
                            ) {
                                Text(
                                    text = uiState.periodLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        MonthlyBarGraph(
                            bars = uiState.monthlyTrendBars,
                            currencySymbol = currency,
                            overallBudget = uiState.activeMonthlyBudget,
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

        // 1B. Selected Month / Selected Category Transactions Card
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = dimens.horizontalPadding)
                    .padding(bottom = 12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analytics_selected_month_transactions_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                text = if (selectedAnalysisCategory != null) {
                                    "$selectedAnalysisCategory • ${uiState.periodLabel}"
                                } else {
                                    "Transactions • ${uiState.periodLabel}"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("analytics_selected_month_transactions_header")
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Text(
                                    text = "${filteredAnalysisTransactions.size} items",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (filteredAnalysisTransactions.isEmpty()) {
                            Text(
                                text = if (selectedAnalysisCategory != null) {
                                    "No $selectedAnalysisCategory transactions recorded in ${uiState.periodLabel}."
                                } else {
                                    "No transactions recorded in ${uiState.periodLabel}."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            val visibleSlice = if (selectedAnalysisCategory != null) {
                                filteredAnalysisTransactions
                            } else {
                                filteredAnalysisTransactions.take(15)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                visibleSlice.forEachIndexed { idx, tx ->
                                    val isInc = tx.type.equals("INCOME", ignoreCase = true) ||
                                        ExpenseCategories.normalizeCategory(tx.category)
                                            .equals("Salary & Income", ignoreCase = true)
                                    val catColor = categoryAccentColor(tx.category)
                                    val rowShape = groupedCardShape(
                                        index = idx,
                                        count = visibleSlice.size,
                                        outerCorner = 18.dp,
                                        innerCorner = 6.dp
                                    )
                                    Surface(
                                        shape = rowShape,
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(rowShape)
                                            .clickable {
                                                performHaptic()
                                                onEditTransaction(tx)
                                            }
                                            .testTag("analytics_month_tx_row_${tx.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(14.dp))
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
                                                        .testTag("analytics_month_tx_category_icon_${tx.id}")
                                                ) {
                                                    SquircleIconBadge(
                                                        icon = categoryIcon(tx.category),
                                                        tint = catColor,
                                                        containerColor = catColor.copy(alpha = 0.16f),
                                                        size = 38.dp,
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
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = "${tx.category} • ${formatShortDate(tx.transactionDateMillis)}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${if (isInc) "+" else "-"}${formatCurrency(tx.amount, currency)}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                                fontWeight = FontWeight.Bold,
                                                color = if (isInc) BudgetSafeGreen else MaterialTheme.colorScheme.onSurface
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

        // 2. Connected Category Breakdown Accordion Stack (filtered to selected category when clicked)
        if (displayedCategoryItems.isNotEmpty()) {
            item {
                PerforatedSectionHeader(
                    title = if (selectedAnalysisCategory != null) {
                        "Category Breakdown • $selectedAnalysisCategory"
                    } else {
                        "Category Breakdown"
                    },
                    countBadge = "${displayedCategoryItems.size}",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            itemsIndexed(
                items = displayedCategoryItems,
                key = { _, item -> "analysis_cat_${item.category}" }
            ) { idx, catItem ->
                val isExpanded = selectedAnalysisCategory != null &&
                    selectedAnalysisCategory.equals(catItem.category, ignoreCase = true)
                val catColor = categoryAccentColor(catItem.category)
                val categoryTransactions = remember(allPeriodTransactions, catItem.category) {
                    allPeriodTransactions
                        .filter {
                            it.category.equals(catItem.category, ignoreCase = true) ||
                                ExpenseCategories.normalizeCategory(it.category).equals(catItem.category, ignoreCase = true)
                        }
                        .sortedByDescending { it.transactionDateMillis }
                }
                val stackShape = groupedCardShape(index = idx, count = displayedCategoryItems.size)
                val safeCatTag = catItem.category.lowercase().replace(" ", "_")

                Box(
                    modifier = Modifier.padding(
                        horizontal = dimens.horizontalPadding,
                        vertical = 1.5.dp
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(stackShape)
                            .animateContentSize(
                                spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                            )
                            .testTag("analysis_category_card_$safeCatTag"),
                        shape = stackShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExpanded) {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { toggleCategorySelection(catItem.category) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                                    .testTag("category_breakdown_row_$safeCatTag"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SquircleIconBadge(
                                    icon = categoryIcon(catItem.category),
                                    tint = catColor,
                                    containerColor = catColor.copy(alpha = 0.16f),
                                    size = dimens.rowIconBoxSize,
                                    iconSize = dimens.rowIconSize,
                                    contentDescription = catItem.category
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "${catItem.category} (${categoryTransactions.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formatCurrency(catItem.spent, currency),
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle Category Transactions",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ReceiptDashedDividerLine()
                                    categoryTransactions.forEach { tx ->
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
                                                .testTag("analysis_tx_row_${tx.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
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
                                                        .testTag("analysis_tx_category_icon_${tx.id}")
                                                ) {
                                                    SquircleIconBadge(
                                                        icon = categoryIcon(tx.category),
                                                        tint = catColor,
                                                        containerColor = catColor.copy(alpha = 0.14f),
                                                        size = 34.dp,
                                                        iconSize = 16.dp,
                                                        contentDescription = tx.category
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
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
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = formatCurrency(tx.amount, currency),
                                                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            performHaptic()
                                                            onLinkDuplicateTransaction(tx)
                                                        },
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .testTag("analysis_link_tx_button_${tx.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Link,
                                                            contentDescription = "Link Duplicate Transaction",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            performHaptic()
                                                            onEditTransaction(tx)
                                                        },
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .testTag("analysis_edit_tx_button_${tx.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Edit Transaction",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(18.dp)
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


        // 3. Expandable Top Places (& Member Split when Household Mode is enabled)
        val showMemberSplit = uiState.householdFeatureEnabled && uiState.memberSpendMap.isNotEmpty()
        if (uiState.topMerchants.isNotEmpty() || showMemberSplit) {
            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = dimens.horizontalPadding)
                        .padding(top = 12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        performHaptic()
                                        moreBreakdownsExpanded = !moreBreakdownsExpanded
                                    }
                                    .padding(dimens.cardPadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (showMemberSplit) "Top Places & Split" else "Top Places",
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
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ReceiptDashedDividerLine()

                                    if (showMemberSplit) {
                                        val total = uiState.memberSpendMap.values.sum().coerceAtLeast(1.0)
                                        uiState.memberSpendMap.entries.sortedByDescending { it.value }.forEach { (member, spent) ->
                                            val ratio = (spent / total).toFloat().coerceIn(0f, 1f)
                                            Column(modifier = Modifier.padding(vertical = 3.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(text = member, style = MaterialTheme.typography.titleSmall)
                                                    Text(
                                                        text = formatCurrency(spent, currency),
                                                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = ReceiptMonoFontFamily),
                                                        color = HouseholdBadgeIndigo,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = { ratio },
                                                    color = HouseholdBadgeIndigo,
                                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
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
                                                    .padding(vertical = 3.dp),
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
                                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
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

/**
 * Budgets Screen (`BudgetsTabScreen`):
 * - Editorial M3 Header + Serrated Monthly Budget Receipt Ticket Card.
 * - Connected 24dp/6dp Category Envelope Stack with spring-animated inline numeric budget editor.
 */
@Composable
fun BudgetsTabScreen(
    uiState: OneFamilyDashboardUiState,
    onEditOverallBudgetClick: () -> Unit,
    onSaveCategoryBudgetInline: (category: String, monthlyLimit: Double) -> Unit = { _, _ -> },
    onAddCustomCategoryClick: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onAddOrEditCategoryBudgetClick: (CategorySpendProgress?) -> Unit = {},
    onScopeSelected: (BudgetScope) -> Unit = {},
    onSelectDateRangePreset: (DateRangePreset) -> Unit = {},
    onSelectCustomDateRange: (Long, Long) -> Unit = { _, _ -> }
) {
    val dimens = LocalAppDimens.current
    val currency = uiState.activeCurrencySymbol
    val performHaptic = rememberAppHapticPerformer()
    var activeInlineCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var inlineBudgetInput by rememberSaveable { mutableStateOf("") }
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    var showBudgetCustomRangePicker by rememberSaveable { mutableStateOf(false) }

    val activateInlineEditor: (CategorySpendProgress) -> Unit = { item ->
        performHaptic()
        if (activeInlineCategory != null && activeInlineCategory.equals(item.category, ignoreCase = true)) {
            activeInlineCategory = null
        } else {
            activeInlineCategory = item.category
            inlineBudgetInput = if (item.limit > 0.0) {
                if (item.limit % 1.0 == 0.0) item.limit.toLong().toString() else item.limit.toString()
            } else {
                ""
            }
        }
    }

    val activeBudgetCount = remember(uiState.categoryProgressList) {
        uiState.categoryProgressList.count { it.limit > 0.0 }
    }
    val hasActiveFilter = uiState.selectedDateRangePreset != DateRangePreset.MONTH

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("budgets_tab_list"),
        contentPadding = PaddingValues(top = 6.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 0. Editorial M3 Header
        item {
            EditorialScreenHeader(
                title = "Budgets",
                countPillText = "$activeBudgetCount active",
                subtitle = uiState.periodLabel,
                modifier = Modifier.padding(bottom = 10.dp),
                actions = {
                    CircularTonalIconButton(
                        icon = Icons.Default.FilterList,
                        contentDescription = "Budget Filters",
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
                        modifier = Modifier.testTag("budgets_toggle_filters_button")
                    )
                    CircularTonalIconButton(
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit Monthly Budget",
                        onClick = onEditOverallBudgetClick,
                        modifier = Modifier.testTag("configure_overall_budget_button")
                    )
                }
            )
        }

        // 0B. Scope & DateRange Filters (matching AccountsTabScreen & HomeTabScreen)
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

        if (filtersExpanded || hasActiveFilter) {
            item {
                AnimatedVisibility(visible = filtersExpanded || hasActiveFilter) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.horizontalPadding)
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
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
                                                showBudgetCustomRangePicker = true
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
                                        modifier = Modifier.testTag("budgets_date_range_chip_${preset.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. Serrated Receipt Ticket Summary for Monthly Budgets (No duplicate trend graph)
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = dimens.horizontalPadding)
                    .padding(bottom = 14.dp)
            ) {
                ReceiptTicketCard(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(modifier = Modifier.padding(dimens.cardPadding)) {
                        Text(
                            text = "Monthly Caps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        ReceiptDashedDividerLine()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (uiState.householdFeatureEnabled) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable {
                                            performHaptic()
                                            onEditOverallBudgetClick()
                                        }
                                        .testTag("budgets_household_overall_pill"),
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                            text = if (hhLimit > 0.0) formatCurrency(hhLimit, currency) else "Not set",
                                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable {
                                        performHaptic()
                                        onEditOverallBudgetClick()
                                    }
                                    .testTag("budgets_personal_overall_pill"),
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                        text = if (pLimit > 0.0) formatCurrency(pLimit, currency) else "Not set",
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
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

        // 2. Category Budgets Header + Custom Category Button
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                PerforatedSectionHeader(
                    title = "Category Budgets",
                    countBadge = "${uiState.categoryProgressList.size}"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap any category envelope to edit its limit inline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = {
                            performHaptic()
                            onAddCustomCategoryClick()
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("add_category_budget_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Custom Category",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Custom Category")
                    }
                }
            }
        }

        // 3. Connected 24dp/6dp Category Envelope Stack
        itemsIndexed(
            items = uiState.categoryProgressList,
            key = { _, item -> item.category }
        ) { idx, item ->
            val catColor = categoryAccentColor(item.category)
            val barColor = when {
                item.isOverBudget -> BudgetDangerCoral
                item.isNearAlert -> BudgetWarnAmber
                else -> BudgetSafeGreen
            }
            val safeTag = item.category.lowercase().replace(Regex("[^a-z0-9]+"), "_")
            val isEditingInline = activeInlineCategory != null &&
                activeInlineCategory.equals(item.category, ignoreCase = true)
            val stackShape = groupedCardShape(index = idx, count = uiState.categoryProgressList.size)

            Box(
                modifier = Modifier.padding(
                    horizontal = dimens.horizontalPadding,
                    vertical = 1.5.dp
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(stackShape)
                        .animateContentSize(
                            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                        )
                        .clickable { activateInlineEditor(item) }
                        .testTag("category_budget_card_$safeTag"),
                    shape = stackShape,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEditingInline) {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SquircleIconBadge(
                                icon = categoryIcon(item.category),
                                tint = catColor,
                                containerColor = catColor.copy(alpha = 0.16f),
                                size = dimens.rowIconBoxSize,
                                iconSize = dimens.rowIconSize,
                                contentDescription = item.category
                            )
                            Spacer(modifier = Modifier.width(14.dp))
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
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = { activateInlineEditor(item) },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("set_category_budget_$safeTag")
                            ) {
                                Icon(
                                    imageVector = if (item.limit > 0.0) Icons.Default.Edit else Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (item.limit > 0.0) formatCurrency(item.limit, currency) else "Set Budget",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = if (item.limit > 0.0) ReceiptMonoFontFamily else MaterialTheme.typography.labelMedium.fontFamily
                                    ),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Inline Numeric Budget Editor (no modal/new screen opened)
                        AnimatedVisibility(visible = isEditingInline) {
                            val inlineError = if (inlineBudgetInput.isNotEmpty()) {
                                validateNonNegativeAmount(inlineBudgetInput)
                            } else {
                                null
                            }
                            val commitInlineBudget = {
                                if (inlineError == null) {
                                    performHaptic()
                                    val parsedLimit = inlineBudgetInput.trim().toDoubleOrNull() ?: 0.0
                                    onSaveCategoryBudgetInline(item.category, parsedLimit)
                                    activeInlineCategory = null
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = inlineBudgetInput,
                                        onValueChange = { inlineBudgetInput = sanitizeDecimalInput(it) },
                                        label = { Text("Monthly Limit ($currency)") },
                                        placeholder = { Text("0 to clear") },
                                        isError = inlineError != null,
                                        supportingText = inlineError?.let { err -> { Text(err) } },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Decimal,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(onDone = { commitInlineBudget() }),
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("inline_category_budget_input_$safeTag")
                                    )
                                    Button(
                                        onClick = commitInlineBudget,
                                        enabled = inlineError == null,
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("inline_save_category_budget_$safeTag")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Save Category Budget",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    if (item.limit > 0.0) {
                                        IconButton(
                                            onClick = {
                                                performHaptic()
                                                onSaveCategoryBudgetInline(item.category, 0.0)
                                                activeInlineCategory = null
                                            },
                                            modifier = Modifier.testTag("inline_clear_category_budget_$safeTag")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear Budget Limit",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    } else {
                                        IconButton(
                                            onClick = { activeInlineCategory = null },
                                            modifier = Modifier.testTag("inline_cancel_category_budget_$safeTag")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Cancel Inline Edit"
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (item.limit > 0.0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { item.progressFraction.coerceIn(0f, 1f) },
                                color = barColor,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.percentage}% used",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
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
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
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

    if (showBudgetCustomRangePicker) {
        CustomDateRangePickerModal(
            initialStartMillis = uiState.customStartDateMillis,
            initialEndMillis = uiState.customEndDateMillis,
            onDismiss = { showBudgetCustomRangePicker = false },
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
    onExportCsvClick: () -> Unit = {},
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
    val stabilityDiagnostics by LocalAccountSessionManager.stabilityDiagnostics.collectAsStateWithLifecycle()
    val householdFeatureEnabled by LocalAccountSessionManager.householdFeatureEnabled.collectAsStateWithLifecycle()
    val currency = uiState.activeCurrencySymbol

    val isDarkActive = when (currentThemeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> systemDark
    }

    val displayName = uiState.userProfile?.displayName?.takeIf { it.isNotBlank() }
        ?: currentUserName.ifBlank { "Member" }
    var editableUserName by remember(displayName) { mutableStateOf(displayName) }
    var customCurrencyInput by remember(currency) { mutableStateOf(currency) }

    var newHouseholdName by remember { mutableStateOf("Our Family Home") }
    var newHouseholdLimit by remember { mutableStateOf("") }
    var joinCodeInput by remember { mutableStateOf("") }
    var inviteEmailInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("household_tab_list"),
        contentPadding = PaddingValues(top = 6.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 0. Editorial M3 Settings Header
        item {
            EditorialScreenHeader(
                title = "Settings",
                countPillText = displayName,
                subtitle = currentUserEmail.takeIf { it.isNotBlank() },
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        // 1. Asymmetric Top-Right Cutout Appearance Hero Card (Screenshot 6)
        item {
            ExpressiveCutoutAppearanceHeroCard(
                isDarkActive = isDarkActive,
                amoledEnabled = amoledDarkEnabled,
                onToggleQuickTheme = {
                    val next = if (isDarkActive) AppThemeMode.LIGHT else AppThemeMode.DARK
                    LocalAccountSessionManager.setThemeMode(context, next)
                },
                modifier = Modifier
                    .padding(horizontal = dimens.horizontalPadding)
                    .padding(bottom = 12.dp)
            )
        }

        // 2. Visual Style Section Header + Connected 3-Card Stack
        item {
            PerforatedSectionHeader(
                title = "Visual Style",
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Visual Style Card 1 of 3: App Theme, Primary Palette & Finance Wallpaper
        item {
            val topVisualShape = groupedCardShape(index = 0, count = 3)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                    .testTag("theme_and_wallpaper_card"),
                shape = topVisualShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Outlined.Palette,
                                tint = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            Column {
                                Text(
                                    text = "App Theme & Wallpaper",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Choose your preferred paper & ink style",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        M3CheckmarkSwitch(
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

                    // M3 Checkmark SingleChoiceSegmentedButtonRow (Light | Auto | Dark)
                    val segmentedModes = listOf(
                        AppThemeMode.LIGHT to "Light",
                        AppThemeMode.SYSTEM to "Auto",
                        AppThemeMode.DARK to "Dark"
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        segmentedModes.forEachIndexed { idx, (mode, labelText) ->
                            val selected = currentThemeMode == mode
                            SegmentedButton(
                                selected = selected,
                                onClick = {
                                    performHaptic()
                                    LocalAccountSessionManager.setThemeMode(context, mode)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = segmentedModes.size),
                                icon = {
                                    SegmentedButtonDefaults.Icon(active = selected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("appearance_segment_${labelText.lowercase()}")
                            ) {
                                Text(
                                    text = labelText,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Pure Color Swatch Theme Selector
                    ThemeModeSelectorBar(
                        currentMode = currentThemeMode,
                        onSelectMode = {
                            performHaptic()
                            LocalAccountSessionManager.setThemeMode(context, it)
                        }
                    )

                    // Primary Theme Color Selector
                    PrimaryColorSelectorBar(
                        selectedOption = currentPrimaryOption,
                        customPrimaryColorArgb = customPrimaryColorArgb,
                        onSelectOption = {
                            performHaptic()
                            LocalAccountSessionManager.setPrimaryColorOption(context, it)
                        },
                        onSelectCustomColorArgb = {
                            performHaptic()
                            LocalAccountSessionManager.setCustomPrimaryColor(context, it)
                        }
                    )

                    ReceiptDashedDividerLine()

                    FinanceWallpaperPreviewBanner(settings = wallpaperSettings)

                    AnimatedVisibility(visible = wallpaperSettings.enabled) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                WallpaperPatternStyle.entries.forEach { style ->
                                    val symbol = when (style) {
                                        WallpaperPatternStyle.WHATSAPP_DOODLES -> "💳 🧾"
                                        WallpaperPatternStyle.FINANCE_ICONS_AND_EMOJIS -> "💰 📊"
                                        WallpaperPatternStyle.MINIMAL_GEOMETRIC -> "❖ ◇"
                                    }
                                    FilterChip(
                                        selected = wallpaperSettings.style == style,
                                        onClick = {
                                            performHaptic()
                                            LocalAccountSessionManager.updateWallpaperSettings(
                                                context = context,
                                                newSettings = wallpaperSettings.copy(style = style)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = symbol,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        modifier = Modifier.testTag("wallpaper_style_chip_${style.name.lowercase()}")
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                WallpaperColorAccent.entries.forEach { accent ->
                                    val isSelected = wallpaperSettings.colorAccent == accent
                                    val swatchColor = when (accent) {
                                        WallpaperColorAccent.THEME_ADAPTIVE -> MaterialTheme.colorScheme.primary
                                        WallpaperColorAccent.EMERALD_MINT -> Color(0xFF10B981)
                                        WallpaperColorAccent.ROYAL_GOLD -> Color(0xFFF59E0B)
                                        WallpaperColorAccent.OCEAN_SLATE -> Color(0xFF0EA5E9)
                                        WallpaperColorAccent.WARM_CORAL -> Color(0xFFFF5A5F)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(swatchColor.copy(alpha = if (isSelected) 1f else 0.72f))
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                performHaptic()
                                                LocalAccountSessionManager.updateWallpaperSettings(
                                                    context = context,
                                                    newSettings = wallpaperSettings.copy(colorAccent = accent)
                                                )
                                            }
                                            .testTag("wallpaper_accent_chip_${accent.name.lowercase()}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = accent.label,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                WallpaperDensity.entries.forEach { density ->
                                    val densityIcon = when (density) {
                                        WallpaperDensity.COMPACT -> Icons.Default.Apps
                                        WallpaperDensity.BALANCED -> Icons.Default.GridOn
                                        WallpaperDensity.AIRY -> Icons.Default.Window
                                    }
                                    FilterChip(
                                        selected = wallpaperSettings.density == density,
                                        onClick = {
                                            performHaptic()
                                            LocalAccountSessionManager.updateWallpaperSettings(
                                                context = context,
                                                newSettings = wallpaperSettings.copy(density = density)
                                            )
                                        },
                                        label = {
                                            Icon(
                                                imageVector = densityIcon,
                                                contentDescription = density.label,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        modifier = Modifier.testTag("wallpaper_density_chip_${density.name.lowercase()}")
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
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
                                        .weight(1f)
                                        .testTag("wallpaper_opacity_slider")
                                )
                                Text(
                                    text = "${wallpaperSettings.opacityPercent}%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Visual Style Card 2 of 3: AMOLED Mode (Screenshot 6)
        item {
            val midVisualShape = groupedCardShape(index = 1, count = 3)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                    .testTag("amoled_mode_card"),
                shape = midVisualShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        SquircleIconBadge(
                            icon = Icons.Outlined.DarkMode,
                            tint = MaterialTheme.colorScheme.secondary,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                        Column {
                            Text(
                                text = "AMOLED Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pure black background for OLED screens",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    M3CheckmarkSwitch(
                        checked = amoledDarkEnabled,
                        onCheckedChange = { enabled ->
                            LocalAccountSessionManager.setAmoledDarkEnabled(context, enabled)
                        },
                        modifier = Modifier.testTag("amoled_mode_switch")
                    )
                }
            }
        }

        // Visual Style Card 3 of 3: Haptic Feedback (Screenshots 6 & 7)
        item {
            val botVisualShape = groupedCardShape(index = 2, count = 3)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                    .padding(bottom = 12.dp)
                    .testTag("haptic_feedback_card"),
                shape = botVisualShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        SquircleIconBadge(
                            icon = Icons.Outlined.Vibration,
                            tint = MaterialTheme.colorScheme.tertiary,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                        Column {
                            Text(
                                text = "Haptic Feedback",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Subtle vibrations on gestures & switches",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    M3CheckmarkSwitch(
                        checked = hapticFeedbackEnabled,
                        onCheckedChange = { enabled ->
                            LocalAccountSessionManager.setHapticFeedbackEnabled(context, enabled)
                        },
                        modifier = Modifier.testTag("haptic_feedback_switch")
                    )
                }
            }
        }

        // 3. Family Household Master Toggle Banner & Collaboration Cards
        item {
            PerforatedSectionHeader(
                title = "Family Collaboration",
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        item {
            MasterFeatureToggleBanner(
                title = "Family Household",
                subtitle = "Enable shared family ledger, invites & budgets",
                checked = householdFeatureEnabled,
                onCheckedChange = { enabled ->
                    LocalAccountSessionManager.setHouseholdFeatureEnabled(context, enabled)
                },
                leadingIcon = Icons.Outlined.Groups,
                switchTestTag = "household_feature_enable_switch",
                modifier = Modifier
                    .padding(horizontal = dimens.horizontalPadding)
                    .padding(bottom = 10.dp)
                    .testTag("household_feature_settings_card")
            )
        }

        if (householdFeatureEnabled) {
            val household = uiState.household

            if (household == null) {
                if (uiState.pendingEmailInvites.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimens.horizontalPadding, vertical = 4.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(dimens.cardPadding)) {
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
                                        Button(
                                            onClick = { onAcceptEmailInvite(inv) },
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text("Join")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = dimens.horizontalPadding)
                            .padding(bottom = 8.dp)
                    ) {
                        ReceiptTicketCard(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(dimens.cardPadding)
                                    .testTag("household_create_card")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SquircleIconBadge(
                                        icon = Icons.Default.Groups,
                                        tint = MaterialTheme.colorScheme.primary,
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        size = 40.dp,
                                        iconSize = 20.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
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
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("create_household_name_input")
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newHouseholdLimit,
                                    onValueChange = { newHouseholdLimit = sanitizeDecimalInput(it) },
                                    label = { Text("Monthly Budget ($currency)") },
                                    placeholder = { Text("Optional") },
                                    isError = hhLimitError != null,
                                    supportingText = hhLimitError?.let { err -> { Text(err) } },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("create_household_limit_input")
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (hhNameError == null && hhLimitError == null) {
                                            performHaptic()
                                            val limit = newHouseholdLimit.trim().toDoubleOrNull() ?: 0.0
                                            onCreateHousehold(newHouseholdName.trim(), limit)
                                        }
                                    },
                                    enabled = hhNameError == null && hhLimitError == null,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("create_household_submit_button")
                                ) {
                                    Text("Create")
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.horizontalPadding)
                            .padding(bottom = 12.dp)
                            .testTag("household_join_code_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(dimens.cardPadding)) {
                            Text(
                                text = "Join with Code",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
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
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("join_household_code_input")
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (validateHouseholdJoinCode(joinCodeInput) == null) {
                                        performHaptic()
                                        onJoinByCode(joinCodeInput.trim())
                                    }
                                },
                                enabled = validateHouseholdJoinCode(joinCodeInput) == null,
                                shape = RoundedCornerShape(50),
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
                    Box(
                        modifier = Modifier
                            .padding(horizontal = dimens.horizontalPadding)
                            .padding(bottom = 8.dp)
                    ) {
                        ReceiptTicketCard(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(dimens.cardPadding)
                                    .testTag("household_active_details_card")
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
                                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                ReceiptDashedDividerLine()

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
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
                                            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                        OutlinedButton(
                                            onClick = {
                                                performHaptic()
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

                                Spacer(modifier = Modifier.height(10.dp))

                                val inviteEmailError = if (inviteEmailInput.isNotEmpty()) {
                                    validateEmailAddress(inviteEmailInput)
                                } else {
                                    null
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("invite_member_email_input")
                                    )
                                    Button(
                                        onClick = {
                                            if (validateEmailAddress(inviteEmailInput) == null) {
                                                performHaptic()
                                                onInviteByEmail(inviteEmailInput.trim())
                                                inviteEmailInput = ""
                                            }
                                        },
                                        enabled = validateEmailAddress(inviteEmailInput) == null,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .height(54.dp)
                                            .testTag("send_email_invite_button")
                                    ) {
                                        Text("Invite")
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.horizontalPadding)
                            .padding(bottom = 12.dp)
                            .testTag("household_members_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(dimens.cardPadding)) {
                            Text(
                                text = "Members",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            household.memberUids.forEach { uid ->
                                val name = household.memberNames[uid] ?: "Member"
                                val memberSharedSpend = uiState.sharedExpenses
                                    .filter { it.userId == uid && !it.type.equals("INCOME", ignoreCase = true) }
                                    .sumOf { it.amount }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
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
                                                .clip(RoundedCornerShape(14.dp))
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
                                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Account, Currency, SMS, CSV & Diagnostics Connected 6-Card Stack
        item {
            PerforatedSectionHeader(
                title = "Account & Data",
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Account Stack Card 1 of 6: Profile & User Info
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                    .testTag("account_sharing_card"),
                shape = groupedCardShape(index = 0, count = 6),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SquircleIconBadge(
                            icon = Icons.Outlined.Person,
                            tint = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentDescription = "Profile"
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Profile & User Info",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentUserEmail.isNotBlank()) "$displayName • $currentUserEmail" else displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    val userNameError = validateDisplayName(editableUserName)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editableUserName,
                            onValueChange = { editableUserName = it.take(60) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            isError = userNameError != null,
                            supportingText = userNameError?.let { err -> { Text(err) } },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_user_name_input")
                        )
                        Button(
                            onClick = {
                                if (validateDisplayName(editableUserName) == null) {
                                    performHaptic()
                                    onUpdateUserName(editableUserName.trim())
                                }
                            },
                            enabled = userNameError == null && editableUserName.trim() != displayName,
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("save_user_name_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save Name",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Account Stack Card 2 of 6: Preferred Currency
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                    .testTag("family_currency_settings_card"),
                shape = groupedCardShape(index = 1, count = 6),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Default.CurrencyExchange,
                                tint = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentDescription = "Currency"
                            )
                            Text(
                                text = "Preferred Currency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.testTag("active_currency_badge")
                        ) {
                            Text(
                                text = currency,
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FamilyCurrencyOption.entries.forEach { option ->
                            val isSelected = currency == option.symbol
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    performHaptic()
                                    customCurrencyInput = option.symbol
                                    onUpdatePreferredCurrency(option.symbol)
                                },
                                label = {
                                    Text(
                                        text = option.symbol,
                                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = ReceiptMonoFontFamily),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                },
                                modifier = Modifier.testTag("currency_chip_${option.code.lowercase()}")
                            )
                        }
                    }

                    val currencyError = validateCurrencySymbol(customCurrencyInput)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customCurrencyInput,
                            onValueChange = { customCurrencyInput = it.take(6) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CurrencyExchange,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            placeholder = { Text("₹ $ € £") },
                            isError = currencyError != null,
                            supportingText = currencyError?.let { err -> { Text(err) } },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_currency_input")
                        )
                        Button(
                            onClick = {
                                if (validateCurrencySymbol(customCurrencyInput) == null) {
                                    performHaptic()
                                    onUpdatePreferredCurrency(customCurrencyInput.trim())
                                }
                            },
                            enabled = currencyError == null && customCurrencyInput.trim() != currency,
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("save_custom_currency_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Apply Currency",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Account Stack Card 3 of 6: Financial Accounts
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp),
                shape = groupedCardShape(index = 2, count = 6),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SquircleIconBadge(
                            icon = Icons.Outlined.AccountBalance,
                            tint = MaterialTheme.colorScheme.tertiary,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Accounts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Bank accounts, credit cards, UPI & cash",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    AccountsCarouselStrip(
                        accounts = uiState.accounts,
                        currencySymbol = currency,
                        onAddAccountClick = onAddAccountClick
                    )
                }
            }
        }

        // Account Stack Card 4 of 6: SMS Inbox Sync
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp),
                shape = groupedCardShape(index = 3, count = 6),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        SquircleIconBadge(
                            icon = Icons.Default.Sync,
                            tint = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentDescription = "SMS Sync"
                        )
                        Column {
                            Text(
                                text = "SMS Inbox Sync",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Auto-detect Bank & UPI SMS receipts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalButton(
                        onClick = {
                            performHaptic()
                            onScanSmsClick()
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("scan_sms_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync SMS",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Account Stack Card 5 of 6: Export Transactions (CSV)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                    .testTag("export_csv_settings_card"),
                shape = groupedCardShape(index = 4, count = 6),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        SquircleIconBadge(
                            icon = Icons.Outlined.Download,
                            tint = MaterialTheme.colorScheme.secondary,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentDescription = "Export CSV"
                        )
                        Column {
                            Text(
                                text = "Export Transactions (CSV)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Download by FY, year, month, or date range",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalButton(
                        onClick = {
                            performHaptic()
                            onExportCsvClick()
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("settings_export_csv_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export CSV",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CSV")
                    }
                }
            }
        }

        // Account Stack Card 6 of 6: Advanced Settings & Diagnostics
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                    .padding(bottom = 14.dp)
                    .animateContentSize(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                    .testTag("expert_feature_flag_card"),
                shape = groupedCardShape(index = 5, count = 6),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Outlined.Tune,
                                tint = MaterialTheme.colorScheme.tertiary,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentDescription = "Advanced Settings"
                            )
                            Column {
                                Text(
                                    text = "Advanced Settings",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Crashlytics & FCM Push Alerts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        M3CheckmarkSwitch(
                            checked = expertFeatureFlags.expertModeEnabled,
                            onCheckedChange = { enabled ->
                                LocalAccountSessionManager.updateExpertFeatureFlags(
                                    context = context,
                                    newSettings = expertFeatureFlags.copy(expertModeEnabled = enabled)
                                )
                            },
                            modifier = Modifier.testTag("expert_feature_flag_switch")
                        )
                    }

                    AnimatedVisibility(visible = expertFeatureFlags.expertModeEnabled) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ReceiptDashedDividerLine()

                            Text(
                                text = "Experimental Feature Flags",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("expert_subflags_row"),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = expertFeatureFlags.crashAnalyticsFeatureEnabled,
                                    onClick = {
                                        performHaptic()
                                        LocalAccountSessionManager.updateExpertFeatureFlags(
                                            context = context,
                                            newSettings = expertFeatureFlags.copy(
                                                crashAnalyticsFeatureEnabled = !expertFeatureFlags.crashAnalyticsFeatureEnabled
                                            )
                                        )
                                    },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = "Crash Analytics",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text("Crashlytics", style = MaterialTheme.typography.labelSmall)
                                        }
                                    },
                                    modifier = Modifier.testTag("expert_flag_crashlytics_chip")
                                )

                                FilterChip(
                                    selected = expertFeatureFlags.fcmPushDiagnosticsEnabled,
                                    onClick = {
                                        performHaptic()
                                        LocalAccountSessionManager.updateExpertFeatureFlags(
                                            context = context,
                                            newSettings = expertFeatureFlags.copy(
                                                fcmPushDiagnosticsEnabled = !expertFeatureFlags.fcmPushDiagnosticsEnabled
                                            )
                                        )
                                    },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = "FCM Push",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text("FCM", style = MaterialTheme.typography.labelSmall)
                                        }
                                    },
                                    modifier = Modifier.testTag("expert_flag_fcm_chip")
                                )
                            }

                            AnimatedVisibility(visible = expertFeatureFlags.fcmPushDiagnosticsEnabled) {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("fcm_push_settings_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "FCM Push Notifications",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        if (householdFeatureEnabled) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Groups,
                                                        contentDescription = "Family Push Alerts",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        text = "Family Transaction Alerts",
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                                M3CheckmarkSwitch(
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
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                                    contentDescription = "Budget Threshold Push Alerts",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "Budget Threshold Alerts",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            M3CheckmarkSwitch(
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
                                                contentDescription = "Test Push Alert",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Send Test Push Alert",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            AnimatedVisibility(visible = expertFeatureFlags.crashAnalyticsFeatureEnabled) {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("crashlytics_settings_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = "Crashlytics Reporting",
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Crash Analytics & Diagnostics",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.BugReport,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "Crashlytics Crash Reporting",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            M3CheckmarkSwitch(
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

                                        if (stabilityDiagnostics.isNotEmpty()) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "Stability Diagnostics Log",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("crashlytics_diagnostics_list")
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        stabilityDiagnostics.take(3).forEach { entry ->
                                                            Text(
                                                                text = "⚡ [${entry.subsystem}] ${entry.message}",
                                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                                    contentDescription = "Verify Stability",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Run Stability Diagnostic Check",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            if (stabilityDiagnostics.isNotEmpty()) {
                                                IconButton(
                                                    onClick = { LocalAccountSessionManager.clearStabilityDiagnostics(context) },
                                                    modifier = Modifier.testTag("crashlytics_clear_logs_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = "Clear Logs"
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

        // 5. Dedicated Sign Out Card in Settings
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        SquircleIconBadge(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            tint = MaterialTheme.colorScheme.error,
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Logout",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sign out of this account on device",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            performHaptic()
                            onSignOutClick()
                        },
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
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Logout")
                    }
                }
            }
        }
    }
}
