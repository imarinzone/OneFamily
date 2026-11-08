package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BudgetScope
import com.example.data.model.CategoryBudget
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FinancialAccount
import com.example.data.model.Household
import com.example.data.model.UserProfile
import com.example.data.repository.AxioRepository
import com.example.data.sms.SmsCandidate
import com.example.data.sms.SmsInboxReader
import com.example.data.sms.TransactionNotificationHelper
import com.google.firebase.firestore.FirebaseFirestoreException
import com.ritesh.parser.core.TransactionType
import com.ritesh.parser.core.bank.BankParserFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class YearMonthPeriod(
    val year: Int,
    val month: Int // 0..11 (Calendar.MONTH)
) {
    fun formatDisplay(): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun formatShortMonth(): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
    }

    fun previousMonth(): YearMonthPeriod {
        return if (month == 0) {
            YearMonthPeriod(year - 1, 11)
        } else {
            YearMonthPeriod(year, month - 1)
        }
    }

    fun nextMonth(): YearMonthPeriod {
        return if (month == 11) {
            YearMonthPeriod(year + 1, 0)
        } else {
            YearMonthPeriod(year, month + 1)
        }
    }

    companion object {
        fun current(): YearMonthPeriod {
            val cal = Calendar.getInstance()
            return YearMonthPeriod(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH)
            )
        }

        fun fromMillis(millis: Long): YearMonthPeriod {
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            return YearMonthPeriod(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH)
            )
        }
    }
}

data class MonthlySpendBar(
    val period: YearMonthPeriod,
    val label: String,
    val expenseTotal: Double,
    val incomeTotal: Double,
    val isSelected: Boolean
)

data class CategorySpendProgress(
    val category: String,
    val spent: Double,
    val limit: Double,
    val alertThresholdPercent: Int,
    val budgetDoc: CategoryBudget?,
    val memberBreakdown: Map<String, Double>
) {
    val progressFraction: Float
        get() = if (limit > 0) (spent / limit).toFloat().coerceAtLeast(0f) else 0f

    val percentage: Int
        get() = if (limit > 0) ((spent / limit) * 100.0).toInt() else 0

    val isOverBudget: Boolean
        get() = limit > 0 && spent >= limit

    val isNearAlert: Boolean
        get() = limit > 0 && percentage >= alertThresholdPercent && !isOverBudget
}

data class AxioDashboardUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile? = null,
    val household: Household? = null,
    val pendingEmailInvites: List<Household> = emptyList(),
    val selectedScope: BudgetScope = BudgetScope.HOUSEHOLD_SHARED,
    val selectedPeriod: YearMonthPeriod? = YearMonthPeriod.current(), // null = All Time
    val periodLabel: String = YearMonthPeriod.current().formatDisplay(),
    val searchQuery: String = "",
    val selectedTypeFilter: String = "All",
    val selectedCategoryFilter: String = "All",
    val selectedPaymentModeFilter: String = "All",
    val selectedAccountFilter: String = "All",
    val selectedTagFilter: String = "All",
    val selectedMemberFilter: String = "All",
    val selectedSortOrder: String = "NEWEST",
    val availableCategories: List<String> = ExpenseCategories.ALL,
    val activeExpenses: List<ExpenseTransaction> = emptyList(), // Scoped & period-filtered
    val allScopedExpenses: List<ExpenseTransaction> = emptyList(), // Scoped across all time
    val filteredExpenses: List<ExpenseTransaction> = emptyList(), // Ledger filtered & sorted
    val personalExpenses: List<ExpenseTransaction> = emptyList(),
    val sharedExpenses: List<ExpenseTransaction> = emptyList(),
    val accounts: List<FinancialAccount> = emptyList(),
    val categoryProgressList: List<CategorySpendProgress> = emptyList(),
    val monthlyTrendBars: List<MonthlySpendBar> = emptyList(),
    val totalMonthlySpent: Double = 0.0,
    val totalMonthlyIncome: Double = 0.0,
    val activeMonthlyBudget: Double = 75000.0,
    val dailySafeSpend: Double = 0.0,
    val remainingDaysInMonth: Int = 1,
    val allTagsWithSpend: List<Pair<String, Double>> = emptyList(),
    val topMerchants: List<Pair<String, Double>> = emptyList(),
    val memberSpendMap: Map<String, Double> = emptyMap(),
    val smsCandidates: List<SmsCandidate> = emptyList(),
    val statusBannerMessage: String? = null,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class AxioViewModel(
    private val repository: AxioRepository,
    private val smsInboxReader: SmsInboxReader,
    private val currentUserId: String,
    private val currentUserName: String,
    private val currentUserEmail: String
) : ViewModel() {

    private val _selectedScope = MutableStateFlow(BudgetScope.HOUSEHOLD_SHARED)
    private val _selectedPeriod = MutableStateFlow<YearMonthPeriod?>(YearMonthPeriod.current())
    private val _searchQuery = MutableStateFlow("")
    private val _typeFilter = MutableStateFlow("All")
    private val _categoryFilter = MutableStateFlow("All")
    private val _paymentModeFilter = MutableStateFlow("All")
    private val _accountFilter = MutableStateFlow("All")
    private val _tagFilter = MutableStateFlow("All")
    private val _memberFilter = MutableStateFlow("All")
    private val _sortOrder = MutableStateFlow("NEWEST")
    private val _customCategories = MutableStateFlow<List<String>>(emptyList())
    private val _smsCandidates = MutableStateFlow<List<SmsCandidate>>(emptyList())
    private val _statusBanner = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private var hasAutoScannedSmsThisSession = false

    val smsCandidates: StateFlow<List<SmsCandidate>> = _smsCandidates.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureUserProfile(
                displayName = currentUserName,
                email = currentUserEmail
            )
        }
    }

    private val userProfileFlow = repository.observeUserProfile(currentUserId)
        .catch { e -> handleStreamError(e); emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private val activeHouseholdIdFlow = userProfileFlow
        .map { it?.activeHouseholdId.orEmpty() }
        .distinctUntilChanged()

    private val householdFlow = activeHouseholdIdFlow
        .flatMapLatest { hid ->
            if (hid.isBlank()) flowOf(null) else repository.observeHousehold(hid)
        }
        .catch { e -> handleStreamError(e); emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private val pendingInvitesFlow = if (currentUserEmail.isNotBlank()) {
        repository.observeEmailInvitedHouseholds(currentUserEmail)
            .catch { emit(emptyList()) }
    } else {
        flowOf(emptyList())
    }

    private val personalExpensesFlow = repository.observePersonalExpenses(currentUserId)
        .catch { e -> handleStreamError(e); emit(emptyList()) }

    private val sharedExpensesFlow = activeHouseholdIdFlow
        .flatMapLatest { hid ->
            if (hid.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeSharedExpenses(hid, currentUserId)
            }
        }
        .catch { e -> handleStreamError(e); emit(emptyList()) }

    private val personalBudgetsFlow = repository.observePersonalCategoryBudgets(currentUserId)
        .catch { e -> handleStreamError(e); emit(emptyList()) }

    private val sharedBudgetsFlow = activeHouseholdIdFlow
        .flatMapLatest { hid ->
            if (hid.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeSharedCategoryBudgets(hid, currentUserId)
            }
        }
        .catch { e -> handleStreamError(e); emit(emptyList()) }

    private val accountsFlow = repository.observeAccounts(currentUserId)
        .catch { e -> handleStreamError(e); emit(emptyList()) }

    private data class CoreDataBundle(
        val profile: UserProfile?,
        val household: Household?,
        val invites: List<Household>,
        val personalExpenses: List<ExpenseTransaction>,
        val sharedExpenses: List<ExpenseTransaction>,
        val personalBudgets: List<CategoryBudget>,
        val sharedBudgets: List<CategoryBudget>,
        val accounts: List<FinancialAccount>
    )

    private data class FilterStateBundle(
        val scope: BudgetScope,
        val period: YearMonthPeriod?,
        val query: String,
        val type: String,
        val category: String,
        val paymentMode: String,
        val account: String,
        val tag: String,
        val member: String,
        val sortOrder: String,
        val customCategories: List<String>,
        val candidates: List<SmsCandidate>,
        val banner: String?,
        val error: String?
    )

    private val coreDataFlow = combine(
        combine(userProfileFlow, householdFlow, pendingInvitesFlow) { p, h, i -> Triple(p, h, i) },
        combine(personalExpensesFlow, sharedExpensesFlow) { pe, se -> pe to se },
        combine(personalBudgetsFlow, sharedBudgetsFlow, accountsFlow) { pb, sb, acc -> Triple(pb, sb, acc) }
    ) { (profile, household, invites), (personalExp, sharedExp), (personalBud, sharedBud, accounts) ->
        CoreDataBundle(
            profile = profile,
            household = household,
            invites = invites.filter { currentUserId !in it.memberUids },
            personalExpenses = personalExp,
            sharedExpenses = sharedExp,
            personalBudgets = personalBud,
            sharedBudgets = sharedBud,
            accounts = accounts
        )
    }

    private val filterPrimaryFlow = combine(
        combine(_selectedScope, _selectedPeriod, _searchQuery) { s, p, q -> Triple(s, p, q) },
        combine(_typeFilter, _categoryFilter, _paymentModeFilter) { ty, c, pm -> Triple(ty, c, pm) },
        combine(_accountFilter, _tagFilter, _memberFilter) { a, t, m -> Triple(a, t, m) }
    ) { (scope, period, query), (type, cat, pm), (acc, tag, mem) ->
        arrayOf(scope, period, query, type, cat, pm, acc, tag, mem)
    }

    private val filterSecondaryFlow = combine(
        _sortOrder,
        _customCategories,
        _smsCandidates,
        _statusBanner,
        _errorMessage
    ) { sort, customCats, cand, ban, err ->
        arrayOf(sort, customCats, cand, ban, err)
    }

    private val filterStateFlow = combine(filterPrimaryFlow, filterSecondaryFlow) { p, s ->
        @Suppress("UNCHECKED_CAST")
        FilterStateBundle(
            scope = p[0] as BudgetScope,
            period = p[1] as YearMonthPeriod?,
            query = p[2] as String,
            type = p[3] as String,
            category = p[4] as String,
            paymentMode = p[5] as String,
            account = p[6] as String,
            tag = p[7] as String,
            member = p[8] as String,
            sortOrder = s[0] as String,
            customCategories = s[1] as List<String>,
            candidates = s[2] as List<SmsCandidate>,
            banner = s[3] as String?,
            error = s[4] as String?
        )
    }

    val uiState: StateFlow<AxioDashboardUiState> = combine(coreDataFlow, filterStateFlow) { core, filter ->
        val effectiveScope = filter.scope

        val allScopedTransactions = when (effectiveScope) {
            BudgetScope.HOUSEHOLD_SHARED -> {
                if (core.household != null) {
                    core.sharedExpenses
                } else {
                    core.personalExpenses
                }
            }
            BudgetScope.PERSONAL_ONLY -> core.personalExpenses
        }

        val selectedPeriod = filter.period
        val periodLabel = selectedPeriod?.formatDisplay() ?: "All Time"

        val cal = Calendar.getInstance()
        val nowPeriod = YearMonthPeriod.current()
        val remainingDays = if (selectedPeriod == null || selectedPeriod == nowPeriod) {
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val today = cal.get(Calendar.DAY_OF_MONTH)
            (maxDay - today + 1).coerceAtLeast(1)
        } else {
            val targetCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedPeriod.year)
                set(Calendar.MONTH, selectedPeriod.month)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            targetCal.getActualMaximum(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        }

        val periodTransactions = if (selectedPeriod == null) {
            allScopedTransactions
        } else {
            allScopedTransactions.filter { tx ->
                val txPeriod = YearMonthPeriod.fromMillis(tx.transactionDateMillis)
                txPeriod == selectedPeriod
            }
        }

        val periodExpensesOnly = periodTransactions.filter { it.type == "EXPENSE" }
        val periodIncomeOnly = periodTransactions.filter { it.type == "INCOME" }

        val totalSpent = periodExpensesOnly.sumOf { it.amount }
        val totalIncome = periodIncomeOnly.sumOf { it.amount }

        val personalCap = core.profile?.personalMonthlyBudget ?: 35000.0
        val householdCap = core.household?.monthlyBudgetLimit ?: 75000.0

        val activeMonthlyBudget = when (effectiveScope) {
            BudgetScope.HOUSEHOLD_SHARED -> if (core.household != null) householdCap else personalCap
            BudgetScope.PERSONAL_ONLY -> personalCap
        }

        val remainingBudget = (activeMonthlyBudget - totalSpent).coerceAtLeast(0.0)
        val dailySafeSpend = remainingBudget / remainingDays

        val activeBudgets = when (effectiveScope) {
            BudgetScope.HOUSEHOLD_SHARED -> if (core.household != null) core.sharedBudgets else core.personalBudgets
            BudgetScope.PERSONAL_ONLY -> core.personalBudgets
        }

        val allAvailableCategories = (
            ExpenseCategories.ALL +
                filter.customCategories +
                activeBudgets.map { it.category } +
                allScopedTransactions.map { it.category }
            )
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }

        val categoriesInPlay = (activeBudgets.map { it.category } + periodExpensesOnly.map { it.category })
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }

        val categoryProgressList = categoriesInPlay.map { category ->
            val catExpenses = periodExpensesOnly.filter { it.category.equals(category, ignoreCase = true) }
            val catSpent = catExpenses.sumOf { it.amount }
            val budgetDoc = activeBudgets.firstOrNull { it.category.equals(category, ignoreCase = true) }
            val limit = budgetDoc?.monthlyLimit ?: 0.0
            val alertPct = budgetDoc?.alertThresholdPercent?.toInt() ?: 80
            val memberBreakdown = catExpenses.groupBy { it.userName.ifBlank { "Member" } }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
            CategorySpendProgress(
                category = category,
                spent = catSpent,
                limit = limit,
                alertThresholdPercent = alertPct,
                budgetDoc = budgetDoc,
                memberBreakdown = memberBreakdown
            )
        }.sortedByDescending { it.spent }

        // Build 6-Month Spending Bar Graph points ending at anchor month
        val anchorPeriod = selectedPeriod
            ?: allScopedTransactions.maxByOrNull { it.transactionDateMillis }
                ?.let { YearMonthPeriod.fromMillis(it.transactionDateMillis) }
            ?: nowPeriod

        val sixMonthsList = mutableListOf<YearMonthPeriod>()
        var cursor = anchorPeriod
        repeat(6) {
            sixMonthsList.add(0, cursor)
            cursor = cursor.previousMonth()
        }

        val monthlyTrendBars = sixMonthsList.map { ym ->
            val monthTxs = allScopedTransactions.filter {
                YearMonthPeriod.fromMillis(it.transactionDateMillis) == ym
            }
            val expSum = monthTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val incSum = monthTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
            MonthlySpendBar(
                period = ym,
                label = ym.formatShortMonth(),
                expenseTotal = expSum,
                incomeTotal = incSum,
                isSelected = selectedPeriod == ym
            )
        }

        val tagSpendMap = mutableMapOf<String, Double>()
        periodExpensesOnly.forEach { tx ->
            tx.tags.forEach { tag ->
                tagSpendMap[tag] = (tagSpendMap[tag] ?: 0.0) + tx.amount
            }
        }
        val allTagsWithSpend = tagSpendMap.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }

        val topMerchants = periodExpensesOnly
            .groupBy { it.title }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
            .take(8)
            .map { it.key to it.value }

        val memberSpendMap = periodExpensesOnly
            .groupBy { it.userName.ifBlank { "Member" } }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val filteredUnsorted = periodTransactions.filter { tx ->
            val matchesQuery = filter.query.isBlank() ||
                tx.title.contains(filter.query, ignoreCase = true) ||
                tx.notes.contains(filter.query, ignoreCase = true) ||
                tx.category.contains(filter.query, ignoreCase = true) ||
                tx.accountName.contains(filter.query, ignoreCase = true) ||
                tx.paymentMode.contains(filter.query, ignoreCase = true) ||
                tx.tags.any { it.contains(filter.query, ignoreCase = true) }

            val matchesType = filter.type == "All" || tx.type.equals(filter.type, ignoreCase = true)
            val matchesCat = filter.category == "All" || tx.category.equals(filter.category, ignoreCase = true)
            val matchesMode = filter.paymentMode == "All" || tx.paymentMode.equals(filter.paymentMode, ignoreCase = true)
            val matchesAcc = filter.account == "All" || tx.accountName.equals(filter.account, ignoreCase = true)
            val matchesTag = filter.tag == "All" || tx.tags.any { it.equals(filter.tag, ignoreCase = true) }
            val matchesMem = filter.member == "All" || tx.userName.equals(filter.member, ignoreCase = true)

            matchesQuery && matchesType && matchesCat && matchesMode && matchesAcc && matchesTag && matchesMem
        }

        val filtered = when (filter.sortOrder) {
            "OLDEST" -> filteredUnsorted.sortedBy { it.transactionDateMillis }
            "HIGHEST" -> filteredUnsorted.sortedByDescending { it.amount }
            "LOWEST" -> filteredUnsorted.sortedBy { it.amount }
            else -> filteredUnsorted.sortedByDescending { it.transactionDateMillis }
        }

        AxioDashboardUiState(
            isLoading = false,
            userProfile = core.profile,
            household = core.household,
            pendingEmailInvites = core.invites,
            selectedScope = effectiveScope,
            selectedPeriod = selectedPeriod,
            periodLabel = periodLabel,
            searchQuery = filter.query,
            selectedTypeFilter = filter.type,
            selectedCategoryFilter = filter.category,
            selectedPaymentModeFilter = filter.paymentMode,
            selectedAccountFilter = filter.account,
            selectedTagFilter = filter.tag,
            selectedMemberFilter = filter.member,
            selectedSortOrder = filter.sortOrder,
            availableCategories = allAvailableCategories,
            activeExpenses = periodTransactions,
            allScopedExpenses = allScopedTransactions,
            filteredExpenses = filtered,
            personalExpenses = core.personalExpenses,
            sharedExpenses = core.sharedExpenses,
            accounts = core.accounts,
            categoryProgressList = categoryProgressList,
            monthlyTrendBars = monthlyTrendBars,
            totalMonthlySpent = totalSpent,
            totalMonthlyIncome = totalIncome,
            activeMonthlyBudget = activeMonthlyBudget,
            dailySafeSpend = dailySafeSpend,
            remainingDaysInMonth = remainingDays,
            allTagsWithSpend = allTagsWithSpend,
            topMerchants = topMerchants,
            memberSpendMap = memberSpendMap,
            smsCandidates = filter.candidates,
            statusBannerMessage = filter.banner,
            errorMessage = filter.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = AxioDashboardUiState()
    )

    // --- PERIOD & FILTER CONTROLS ---

    fun selectScope(scope: BudgetScope) {
        _selectedScope.value = scope
    }

    fun selectPeriod(period: YearMonthPeriod?) {
        _selectedPeriod.value = period
    }

    fun stepMonth(delta: Int) {
        val current = _selectedPeriod.value ?: YearMonthPeriod.current()
        _selectedPeriod.value = if (delta < 0) current.previousMonth() else current.nextMonth()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectTypeFilter(type: String) {
        _typeFilter.value = type
    }

    fun selectCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun selectPaymentModeFilter(mode: String) {
        _paymentModeFilter.value = mode
    }

    fun selectAccountFilter(account: String) {
        _accountFilter.value = account
    }

    fun selectTagFilter(tag: String) {
        _tagFilter.value = tag
    }

    fun selectMemberFilter(member: String) {
        _memberFilter.value = member
    }

    fun selectSortOrder(sortOrder: String) {
        _sortOrder.value = sortOrder
    }

    fun addCustomCategory(categoryName: String) {
        val cleaned = categoryName.trim()
        if (cleaned.isNotBlank()) {
            val existing = _customCategories.value
            if (existing.none { it.equals(cleaned, ignoreCase = true) }) {
                _customCategories.value = existing + cleaned
            }
        }
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _typeFilter.value = "All"
        _categoryFilter.value = "All"
        _paymentModeFilter.value = "All"
        _accountFilter.value = "All"
        _tagFilter.value = "All"
        _memberFilter.value = "All"
        _sortOrder.value = "NEWEST"
    }

    fun dismissBanner() {
        _statusBanner.value = null
        _errorMessage.value = null
    }

    // --- AUTOMATIC FULL SMS INBOX SYNC & REAL-TIME NOTIFICATION ---

    fun autoScanSmsOnLaunchIfNeeded(context: Context) {
        if (hasAutoScannedSmsThisSession) return
        hasAutoScannedSmsThisSession = true
        scanDeviceSmsInbox(context = context, silentIfEmpty = true, notifyNewest = false)
    }

    fun scanDeviceSmsInbox(
        context: Context? = null,
        silentIfEmpty: Boolean = false,
        notifyNewest: Boolean = true
    ) {
        viewModelScope.launch {
            val state = uiState.value
            val existingSignatures = (state.personalExpenses + state.sharedExpenses)
                .mapNotNull { it.smsSignature.takeIf { sig -> sig.isNotBlank() } }
                .toSet()

            val discovered = smsInboxReader.scanDeviceSmsInbox(existingSignatures)
            if (discovered.isNotEmpty()) {
                val userName = currentUserName.ifBlank { state.userProfile?.displayName ?: "Me" }
                val household = state.household ?: repository.getUserProfileOnce()
                    ?.activeHouseholdId
                    ?.takeIf { it.isNotBlank() }
                    ?.let { repository.getHouseholdOnce(it) }

                val importResult = repository.batchImportSmsCandidates(
                    candidates = discovered,
                    userName = userName,
                    household = household,
                    shareByDefault = true
                )

                importResult.onSuccess { importedCount ->
                    _smsCandidates.value = emptyList()
                    val currentPeriod = _selectedPeriod.value
                    val hasCurrentMonthTx = discovered.any {
                        YearMonthPeriod.fromMillis(it.parsed.timestamp) == currentPeriod
                    }
                    if (currentPeriod != null && !hasCurrentMonthTx && state.activeExpenses.isEmpty()) {
                        val newestTxPeriod = discovered.maxByOrNull { it.parsed.timestamp }
                            ?.let { YearMonthPeriod.fromMillis(it.parsed.timestamp) }
                        if (newestTxPeriod != null) {
                            _selectedPeriod.value = newestTxPeriod
                        }
                    }

                    _statusBanner.value = "Imported $importedCount SMS transactions chronologically (${if (household != null) "Shared Household" else "Personal"})."

                    if (notifyNewest && context != null) {
                        val newest = discovered.maxByOrNull { it.parsed.timestamp }
                        if (newest != null) {
                            val expenseId = "sms_${newest.id}"
                            val isShared = household != null
                            val merchantTitle = newest.parsed.merchant?.takeIf { it.isNotBlank() }
                                ?: "${newest.parsed.bankName} ${newest.parsed.type.name.lowercase().replaceFirstChar { it.uppercase() }}"
                            TransactionNotificationHelper.showNewTransactionNotification(
                                context = context,
                                expenseId = expenseId,
                                visibility = if (isShared) "SHARED" else "PERSONAL",
                                householdId = household?.id.orEmpty(),
                                title = merchantTitle,
                                amount = newest.parsed.amount.toDouble(),
                                currentCategory = newest.suggestedCategory
                            )
                        }
                    }
                }.onFailure { err ->
                    _errorMessage.value = err.localizedMessage ?: "Failed to import SMS transactions."
                }
            } else if (!silentIfEmpty) {
                val totalExistingSms = (state.personalExpenses + state.sharedExpenses)
                    .count { it.smsSignature.isNotBlank() }
                _statusBanner.value = if (totalExistingSms > 0) {
                    "All $totalExistingSms bank SMS transactions from your phone are already synced."
                } else {
                    "No new bank/UPI SMS found in inbox."
                }
            }
        }
    }

    fun parseCustomOrTemplateSms(
        sender: String,
        body: String,
        defaultShared: Boolean = true,
        context: Context? = null
    ) {
        val candidate = smsInboxReader.parseRawSms(
            sender = sender,
            body = body,
            defaultShared = defaultShared
        )
        if (candidate != null) {
            importSmsCandidate(
                candidate = candidate,
                asShared = defaultShared,
                context = context,
                triggerNotification = true
            )
        } else {
            _errorMessage.value = "SMS did not match a bank/UPI debit or credit alert."
        }
    }

    fun loadSampleBankSmsBatch(context: Context? = null) {
        viewModelScope.launch {
            val state = uiState.value
            val existingSignatures = (state.personalExpenses + state.sharedExpenses)
                .mapNotNull { it.smsSignature.takeIf { sig -> sig.isNotBlank() } }
                .toSet()

            val now = System.currentTimeMillis()
            val parsedBatch = BankParserFactory.SAMPLE_BANK_SMS_TEMPLATES.mapIndexedNotNull { index, sample ->
                smsInboxReader.parseRawSms(
                    sender = sample.sender,
                    body = sample.body,
                    timestamp = now - (index * 3_600_000L),
                    defaultShared = true
                )
            }.filterNot { it.id in existingSignatures }

            if (parsedBatch.isNotEmpty()) {
                val userName = currentUserName.ifBlank { state.userProfile?.displayName ?: "Me" }
                repository.batchImportSmsCandidates(
                    candidates = parsedBatch,
                    userName = userName,
                    household = state.household,
                    shareByDefault = true
                ).onSuccess { count ->
                    _statusBanner.value = "Added $count sample bank SMS transactions chronologically."
                    if (context != null) {
                        val newest = parsedBatch.maxByOrNull { it.parsed.timestamp }
                        if (newest != null) {
                            val isShared = state.household != null
                            TransactionNotificationHelper.showNewTransactionNotification(
                                context = context,
                                expenseId = "sms_${newest.id}",
                                visibility = if (isShared) "SHARED" else "PERSONAL",
                                householdId = state.household?.id.orEmpty(),
                                title = newest.parsed.merchant ?: newest.suggestedCategory,
                                amount = newest.parsed.amount.toDouble(),
                                currentCategory = newest.suggestedCategory
                            )
                        }
                    }
                }
            } else {
                _statusBanner.value = "Sample bank SMS transactions are already in your ledger."
            }
        }
    }

    fun importSmsCandidate(
        candidate: SmsCandidate,
        asShared: Boolean,
        customCategory: String? = null,
        customTags: List<String> = emptyList(),
        context: Context? = null,
        triggerNotification: Boolean = false
    ) {
        viewModelScope.launch {
            val state = uiState.value
            val parsed = candidate.parsed
            val isIncome = parsed.type == TransactionType.INCOME
            val category = customCategory ?: candidate.suggestedCategory
            val merchantTitle = parsed.merchant?.takeIf { it.isNotBlank() }
                ?: "${parsed.bankName} ${parsed.type.name.lowercase().replaceFirstChar { it.uppercase() }}"
            val shareFlag = asShared && state.household != null

            val result = repository.addExpense(
                userName = currentUserName.ifBlank { state.userProfile?.displayName ?: "Me" },
                household = state.household,
                title = merchantTitle,
                amount = parsed.amount.toDouble(),
                type = if (isIncome) "INCOME" else "EXPENSE",
                category = category,
                accountName = candidate.suggestedAccountName,
                paymentMode = candidate.suggestedPaymentMode,
                tags = (customTags + listOf("sms-auto", parsed.bankName.lowercase().replace(" ", "-"))).distinct(),
                notes = "Auto-parsed from ${parsed.sender}${parsed.reference?.let { " • Ref: $it" }.orEmpty()}",
                isShared = shareFlag,
                smsSignature = candidate.id,
                referenceNumber = parsed.reference.orEmpty(),
                transactionDateMillis = parsed.timestamp
            )

            if (parsed.balance != null || parsed.creditLimit != null || parsed.accountNumber != null) {
                val accType = when {
                    parsed.isFromCard -> "CREDIT_CARD"
                    candidate.suggestedPaymentMode == "WALLET" -> "WALLET"
                    else -> "BANK"
                }
                repository.upsertAccount(
                    name = candidate.suggestedAccountName,
                    bankName = parsed.bankName,
                    accountType = accType,
                    lastFourDigits = parsed.accountNumber.orEmpty(),
                    balance = parsed.balance?.toDouble() ?: 0.0,
                    creditLimit = parsed.creditLimit?.toDouble() ?: 0.0,
                    isSharedVisible = shareFlag
                )
            }

            result.onSuccess { savedExpenseId ->
                _smsCandidates.value = _smsCandidates.value.filterNot { it.id == candidate.id }
                _statusBanner.value = "Added '$merchantTitle' (${if (shareFlag) "Shared Household" else "Personal"})."
                if (triggerNotification && context != null) {
                    TransactionNotificationHelper.showNewTransactionNotification(
                        context = context,
                        expenseId = savedExpenseId,
                        visibility = if (shareFlag) "SHARED" else "PERSONAL",
                        householdId = if (shareFlag) state.household?.id.orEmpty() else "",
                        title = merchantTitle,
                        amount = parsed.amount.toDouble(),
                        currentCategory = category
                    )
                }
            }.onFailure { err ->
                _errorMessage.value = err.localizedMessage ?: "Failed to import SMS transaction."
            }
        }
    }

    fun importAllSmsCandidates(forceShared: Boolean? = null) {
        viewModelScope.launch {
            val list = _smsCandidates.value
            if (list.isEmpty()) return@launch
            val state = uiState.value
            val userName = currentUserName.ifBlank { state.userProfile?.displayName ?: "Me" }
            repository.batchImportSmsCandidates(
                candidates = list,
                userName = userName,
                household = state.household,
                shareByDefault = forceShared ?: true
            ).onSuccess { count ->
                _smsCandidates.value = emptyList()
                _statusBanner.value = "Imported $count SMS transactions."
            }
        }
    }

    fun dismissSmsCandidate(candidateId: String) {
        _smsCandidates.value = _smsCandidates.value.filterNot { it.id == candidateId }
    }

    // --- MANUAL TRANSACTION LOGGING, FULL EDITING & CATEGORY EDITING ---

    fun addManualTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountName: String,
        paymentMode: String,
        tagsInput: String,
        notes: String,
        isShared: Boolean,
        transactionDateMillis: Long = System.currentTimeMillis(),
        context: Context? = null
    ) {
        viewModelScope.launch {
            addCustomCategory(category)
            val state = uiState.value
            val tags = tagsInput.split(",", " ")
                .map { it.trim().removePrefix("#") }
                .filter { it.isNotBlank() }
            val shareFlag = isShared && state.household != null

            val result = repository.addExpense(
                userName = currentUserName.ifBlank { state.userProfile?.displayName ?: "Me" },
                household = state.household,
                title = title,
                amount = amount,
                type = type,
                category = category,
                accountName = accountName,
                paymentMode = paymentMode,
                tags = tags,
                notes = notes,
                isShared = shareFlag,
                transactionDateMillis = transactionDateMillis
            )
            result.onSuccess { savedExpenseId ->
                _statusBanner.value = "Added $title."
                if (context != null) {
                    TransactionNotificationHelper.showNewTransactionNotification(
                        context = context,
                        expenseId = savedExpenseId,
                        visibility = if (shareFlag) "SHARED" else "PERSONAL",
                        householdId = if (shareFlag) state.household?.id.orEmpty() else "",
                        title = title.ifBlank { category },
                        amount = amount,
                        currentCategory = category
                    )
                }
            }.onFailure { err ->
                _errorMessage.value = err.localizedMessage ?: "Failed to add transaction."
            }
        }
    }

    fun updateTransaction(
        original: ExpenseTransaction,
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountName: String,
        paymentMode: String,
        tagsInput: String,
        notes: String,
        transactionDateMillis: Long,
        targetShared: Boolean
    ) {
        viewModelScope.launch {
            addCustomCategory(category)
            val tags = tagsInput.split(",", " ")
                .map { it.trim().removePrefix("#") }
                .filter { it.isNotBlank() }

            repository.updateExpense(
                existing = original,
                household = uiState.value.household,
                title = title.ifBlank { category },
                amount = amount,
                type = type,
                category = category,
                accountName = accountName,
                paymentMode = paymentMode,
                tags = tags,
                notes = notes,
                transactionDateMillis = transactionDateMillis,
                isShared = targetShared
            ).onSuccess {
                _statusBanner.value = "Updated '${title.ifBlank { category }}'."
            }.onFailure { err ->
                _errorMessage.value = err.localizedMessage ?: "Failed to update transaction."
            }
        }
    }

    fun quickUpdateTransactionCategory(
        expense: ExpenseTransaction,
        newCategory: String
    ) {
        viewModelScope.launch {
            addCustomCategory(newCategory)
            repository.updateExpenseCategoryById(
                expenseId = expense.id,
                visibility = expense.visibility,
                householdId = expense.householdId,
                newCategory = newCategory
            ).onSuccess {
                _statusBanner.value = "Category updated to $newCategory."
            }.onFailure { err ->
                _errorMessage.value = err.localizedMessage ?: "Could not update category."
            }
        }
    }

    fun toggleTransactionPrivacy(expense: ExpenseTransaction) {
        val household = uiState.value.household
        if (household == null) {
            _errorMessage.value = "Create or join a Family Household first to share expenses."
            return
        }
        viewModelScope.launch {
            repository.toggleExpensePrivacy(expense, household)
                .onSuccess {
                    val newScope = if (expense.visibility == "PERSONAL") "Household Shared" else "Personal"
                    _statusBanner.value = "Moved '${expense.title}' to $newScope."
                }
                .onFailure { err ->
                    _errorMessage.value = err.localizedMessage ?: "Could not change privacy scope."
                }
        }
    }

    fun deleteTransaction(expense: ExpenseTransaction) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
                .onSuccess { _statusBanner.value = "Deleted '${expense.title}'." }
                .onFailure { err -> _errorMessage.value = err.localizedMessage ?: "Failed to delete transaction." }
        }
    }

    // --- BUDGETS & ACCOUNTS ---

    fun saveCategoryBudget(
        category: String,
        monthlyLimit: Double,
        alertThresholdPercent: Int,
        isShared: Boolean
    ) {
        viewModelScope.launch {
            addCustomCategory(category)
            val household = uiState.value.household
            repository.saveCategoryBudget(
                category = category,
                monthlyLimit = monthlyLimit,
                alertThresholdPercent = alertThresholdPercent,
                isShared = isShared,
                household = household
            ).onSuccess {
                _statusBanner.value = "Updated $category budget."
            }.onFailure { err ->
                _errorMessage.value = err.localizedMessage ?: "Failed to save budget."
            }
        }
    }

    fun updateOverallMonthlyBudget(
        personalLimit: Double,
        householdLimit: Double?,
        currencySymbol: String
    ) {
        viewModelScope.launch {
            repository.updatePersonalBudgetSettings(personalLimit, currencySymbol)
            val household = uiState.value.household
            if (household != null && householdLimit != null && householdLimit > 0) {
                repository.updateHouseholdSettings(household.id, household.name, householdLimit)
            }
            _statusBanner.value = "Budget limits updated."
        }
    }

    fun saveFinancialAccount(
        name: String,
        bankName: String,
        accountType: String,
        lastFourDigits: String,
        balance: Double,
        creditLimit: Double,
        isSharedVisible: Boolean
    ) {
        viewModelScope.launch {
            repository.upsertAccount(
                name = name,
                bankName = bankName,
                accountType = accountType,
                lastFourDigits = lastFourDigits,
                balance = balance,
                creditLimit = creditLimit,
                isSharedVisible = isSharedVisible
            ).onSuccess {
                _statusBanner.value = "Saved account $name."
            }.onFailure { err ->
                _errorMessage.value = err.localizedMessage ?: "Failed to save account."
            }
        }
    }

    // --- HOUSEHOLD COLLABORATION ACTIONS ---

    fun createFamilyHousehold(name: String, monthlyLimit: Double) {
        viewModelScope.launch {
            val currency = uiState.value.userProfile?.currencySymbol ?: "₹"
            repository.createHousehold(
                name = name,
                monthlyBudgetLimit = monthlyLimit,
                currencySymbol = currency,
                userDisplayName = currentUserName,
                userEmail = currentUserEmail
            ).onSuccess { created ->
                val defaultCaps = listOf(
                    "Groceries" to (monthlyLimit * 0.20),
                    "Rent & Housing" to (monthlyLimit * 0.35),
                    "Food & Dining" to (monthlyLimit * 0.12),
                    "Bills & Utilities" to (monthlyLimit * 0.10),
                    "Fuel & Travel" to (monthlyLimit * 0.10)
                )
                defaultCaps.forEach { (cat, cap) ->
                    repository.saveCategoryBudget(
                        category = cat,
                        monthlyLimit = cap,
                        alertThresholdPercent = 80,
                        isShared = true,
                        household = created
                    )
                }
                _selectedScope.value = BudgetScope.HOUSEHOLD_SHARED
                _statusBanner.value = "Created '${created.name}' • Code: ${created.joinCode}"
            }.onFailure { err ->
                _errorMessage.value = err.localizedMessage ?: "Failed to create household."
            }
        }
    }

    fun joinHouseholdWithCode(joinCode: String) {
        viewModelScope.launch {
            repository.joinHouseholdByCode(
                rawJoinCode = joinCode,
                userDisplayName = currentUserName,
                userEmail = currentUserEmail
            ).onSuccess {
                _selectedScope.value = BudgetScope.HOUSEHOLD_SHARED
                _statusBanner.value = "Joined household!"
            }.onFailure { err ->
                _errorMessage.value = err.localizedMessage ?: "Could not join household with code $joinCode."
            }
        }
    }

    fun acceptEmailInvitation(household: Household) {
        viewModelScope.launch {
            repository.joinHouseholdById(
                householdId = household.id,
                userDisplayName = currentUserName,
                userEmail = currentUserEmail
            ).onSuccess {
                _selectedScope.value = BudgetScope.HOUSEHOLD_SHARED
                _statusBanner.value = "Joined '${household.name}'!"
            }.onFailure { err ->
                _errorMessage.value = err.localizedMessage ?: "Failed to accept invitation."
            }
        }
    }

    fun inviteFamilyMemberByEmail(email: String) {
        val household = uiState.value.household ?: return
        viewModelScope.launch {
            repository.inviteMemberByEmail(household, email)
                .onSuccess {
                    _statusBanner.value = "Invited $email."
                }
                .onFailure { err ->
                    _errorMessage.value = err.localizedMessage ?: "Failed to send invite."
                }
        }
    }

    private fun handleStreamError(error: Throwable) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        _errorMessage.value = rootError.localizedMessage ?: "Sync error occurred."
    }
}
