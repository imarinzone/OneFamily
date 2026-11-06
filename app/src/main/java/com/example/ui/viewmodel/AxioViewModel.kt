package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BudgetScope
import com.example.data.model.CategoryBudget
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FinancialAccount
import com.example.data.model.Household
import com.example.data.model.UserProfile
import com.example.data.repository.AxioRepository
import com.example.data.sms.SmsCandidate
import com.example.data.sms.SmsInboxReader
import com.google.firebase.firestore.FirebaseFirestoreException
import com.ritesh.parser.core.TransactionType
import com.ritesh.parser.core.bank.BankParserFactory
import java.util.Calendar
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
    val selectedScope: BudgetScope = BudgetScope.COMBINED,
    val searchQuery: String = "",
    val selectedCategoryFilter: String = "All",
    val selectedAccountFilter: String = "All",
    val selectedTagFilter: String = "All",
    val selectedMemberFilter: String = "All",
    val activeExpenses: List<ExpenseTransaction> = emptyList(),
    val filteredExpenses: List<ExpenseTransaction> = emptyList(),
    val personalExpenses: List<ExpenseTransaction> = emptyList(),
    val sharedExpenses: List<ExpenseTransaction> = emptyList(),
    val accounts: List<FinancialAccount> = emptyList(),
    val categoryProgressList: List<CategorySpendProgress> = emptyList(),
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

    private val _selectedScope = MutableStateFlow(BudgetScope.COMBINED)
    private val _searchQuery = MutableStateFlow("")
    private val _categoryFilter = MutableStateFlow("All")
    private val _accountFilter = MutableStateFlow("All")
    private val _tagFilter = MutableStateFlow("All")
    private val _memberFilter = MutableStateFlow("All")
    private val _smsCandidates = MutableStateFlow<List<SmsCandidate>>(emptyList())
    private val _statusBanner = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)

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
        val query: String,
        val category: String,
        val account: String,
        val tag: String,
        val member: String,
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

    private val filterStateFlow = combine(
        combine(_selectedScope, _searchQuery, _categoryFilter) { s, q, c -> Triple(s, q, c) },
        combine(_accountFilter, _tagFilter, _memberFilter) { a, t, m -> Triple(a, t, m) },
        combine(_smsCandidates, _statusBanner, _errorMessage) { cand, ban, err -> Triple(cand, ban, err) }
    ) { (scope, query, cat), (acc, tag, mem), (cand, ban, err) ->
        FilterStateBundle(scope, query, cat, acc, tag, mem, cand, ban, err)
    }

    val uiState: StateFlow<AxioDashboardUiState> = combine(coreDataFlow, filterStateFlow) { core, filter ->
        val effectiveScope = if (core.household == null && filter.scope == BudgetScope.HOUSEHOLD_SHARED) {
            BudgetScope.PERSONAL_ONLY
        } else {
            filter.scope
        }

        val scopedTransactions = when (effectiveScope) {
            BudgetScope.HOUSEHOLD_SHARED -> core.sharedExpenses
            BudgetScope.PERSONAL_ONLY -> core.personalExpenses
            BudgetScope.COMBINED -> (core.sharedExpenses + core.personalExpenses)
                .distinctBy { "${it.visibility}_${it.id}" }
                .sortedByDescending { it.transactionDateMillis }
        }

        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = cal.get(Calendar.DAY_OF_MONTH)
        val remainingDays = (maxDay - today + 1).coerceAtLeast(1)

        val currentMonthTransactions = scopedTransactions.filter { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.transactionDateMillis }
            txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }

        val monthlyExpensesOnly = currentMonthTransactions.filter { it.type == "EXPENSE" }
        val monthlyIncomeOnly = currentMonthTransactions.filter { it.type == "INCOME" }

        val totalSpent = monthlyExpensesOnly.sumOf { it.amount }
        val totalIncome = monthlyIncomeOnly.sumOf { it.amount }

        val personalCap = core.profile?.personalMonthlyBudget ?: 35000.0
        val householdCap = core.household?.monthlyBudgetLimit ?: 75000.0

        val activeMonthlyBudget = when (effectiveScope) {
            BudgetScope.HOUSEHOLD_SHARED -> householdCap
            BudgetScope.PERSONAL_ONLY -> personalCap
            BudgetScope.COMBINED -> if (core.household != null) householdCap + personalCap else personalCap
        }

        val remainingBudget = (activeMonthlyBudget - totalSpent).coerceAtLeast(0.0)
        val dailySafeSpend = remainingBudget / remainingDays

        val activeBudgets = when (effectiveScope) {
            BudgetScope.HOUSEHOLD_SHARED -> core.sharedBudgets
            BudgetScope.PERSONAL_ONLY -> core.personalBudgets
            BudgetScope.COMBINED -> (core.sharedBudgets + core.personalBudgets).distinctBy { it.category }
        }

        val categoriesInPlay = (activeBudgets.map { it.category } + monthlyExpensesOnly.map { it.category }).distinct()
        val categoryProgressList = categoriesInPlay.map { category ->
            val catExpenses = monthlyExpensesOnly.filter { it.category.equals(category, ignoreCase = true) }
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

        val tagSpendMap = mutableMapOf<String, Double>()
        monthlyExpensesOnly.forEach { tx ->
            tx.tags.forEach { tag ->
                tagSpendMap[tag] = (tagSpendMap[tag] ?: 0.0) + tx.amount
            }
        }
        val allTagsWithSpend = tagSpendMap.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }

        val topMerchants = monthlyExpensesOnly
            .groupBy { it.title }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
            .take(8)
            .map { it.key to it.value }

        val memberSpendMap = monthlyExpensesOnly
            .groupBy { it.userName.ifBlank { "Member" } }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val filtered = scopedTransactions.filter { tx ->
            val matchesQuery = filter.query.isBlank() ||
                tx.title.contains(filter.query, ignoreCase = true) ||
                tx.notes.contains(filter.query, ignoreCase = true) ||
                tx.category.contains(filter.query, ignoreCase = true) ||
                tx.accountName.contains(filter.query, ignoreCase = true) ||
                tx.tags.any { it.contains(filter.query, ignoreCase = true) }

            val matchesCat = filter.category == "All" || tx.category.equals(filter.category, ignoreCase = true)
            val matchesAcc = filter.account == "All" || tx.accountName.equals(filter.account, ignoreCase = true)
            val matchesTag = filter.tag == "All" || tx.tags.any { it.equals(filter.tag, ignoreCase = true) }
            val matchesMem = filter.member == "All" || tx.userName.equals(filter.member, ignoreCase = true)

            matchesQuery && matchesCat && matchesAcc && matchesTag && matchesMem
        }

        AxioDashboardUiState(
            isLoading = false,
            userProfile = core.profile,
            household = core.household,
            pendingEmailInvites = core.invites,
            selectedScope = effectiveScope,
            searchQuery = filter.query,
            selectedCategoryFilter = filter.category,
            selectedAccountFilter = filter.account,
            selectedTagFilter = filter.tag,
            selectedMemberFilter = filter.member,
            activeExpenses = scopedTransactions,
            filteredExpenses = filtered,
            personalExpenses = core.personalExpenses,
            sharedExpenses = core.sharedExpenses,
            accounts = core.accounts,
            categoryProgressList = categoryProgressList,
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

    fun selectScope(scope: BudgetScope) {
        _selectedScope.value = scope
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategoryFilter(category: String) {
        _categoryFilter.value = category
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

    fun clearFilters() {
        _searchQuery.value = ""
        _categoryFilter.value = "All"
        _accountFilter.value = "All"
        _tagFilter.value = "All"
        _memberFilter.value = "All"
    }

    fun dismissBanner() {
        _statusBanner.value = null
        _errorMessage.value = null
    }

    // --- SMS INBOX SYNC & CASHIRO BANK PARSER ACTIONS ---

    fun scanDeviceSmsInbox() {
        viewModelScope.launch {
            val state = uiState.value
            val existingSignatures = (state.personalExpenses + state.sharedExpenses)
                .mapNotNull { it.smsSignature.takeIf { sig -> sig.isNotBlank() } }
                .toSet()

            val discovered = smsInboxReader.scanDeviceSmsInbox(existingSignatures)
            if (discovered.isNotEmpty()) {
                _smsCandidates.value = discovered
                _statusBanner.value = "Cashiro SMS Parser detected ${discovered.size} bank/UPI transactions."
            } else {
                _statusBanner.value = "No new bank SMS found in device inbox. Try the Cashiro Bank SMS Tester to parse sample or pasted bank alerts!"
            }
        }
    }

    fun parseCustomOrTemplateSms(
        sender: String,
        body: String,
        defaultShared: Boolean = true
    ) {
        val candidate = smsInboxReader.parseRawSms(
            sender = sender,
            body = body,
            defaultShared = defaultShared
        )
        if (candidate != null) {
            val currentList = _smsCandidates.value.toMutableList()
            if (currentList.none { it.id == candidate.id }) {
                currentList.add(0, candidate)
                _smsCandidates.value = currentList
            }
            _statusBanner.value = "Parsed ${candidate.parsed.bankName} alert: ₹${candidate.parsed.amount} at ${candidate.parsed.merchant ?: candidate.suggestedCategory}"
        } else {
            _errorMessage.value = "SMS did not match a transactional debit/credit pattern (OTPs and promos are ignored)."
        }
    }

    fun loadSampleBankSmsBatch() {
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
                defaultShared = sample.defaultShared
            )
        }.filterNot { it.id in existingSignatures }

        _smsCandidates.value = parsedBatch
        _statusBanner.value = "Loaded ${parsedBatch.size} bank & UPI SMS alerts via Cashiro BankParserFactory for review."
    }

    fun importSmsCandidate(
        candidate: SmsCandidate,
        asShared: Boolean,
        customCategory: String? = null,
        customTags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val state = uiState.value
            val parsed = candidate.parsed
            val isIncome = parsed.type == TransactionType.INCOME
            val category = customCategory ?: candidate.suggestedCategory
            val merchantTitle = parsed.merchant?.takeIf { it.isNotBlank() } ?: "${parsed.bankName} ${parsed.type.name.lowercase().replaceFirstChar { it.uppercase() }}"

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
                isShared = asShared && state.household != null,
                smsSignature = candidate.id,
                referenceNumber = parsed.reference.orEmpty(),
                transactionDateMillis = parsed.timestamp
            )

            // Also update or create the FinancialAccount if balance or card limit was extracted
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
                    isSharedVisible = asShared
                )
            }

            result.onSuccess {
                _smsCandidates.value = _smsCandidates.value.filterNot { it.id == candidate.id }
                _statusBanner.value = "Saved '$merchantTitle' (${if (asShared && state.household != null) "Household Shared" else "Personal"})."
            }.onFailure { err ->
                _errorMessage.value = err.localizedMessage ?: "Failed to import SMS transaction."
            }
        }
    }

    fun importAllSmsCandidates(forceShared: Boolean? = null) {
        val list = _smsCandidates.value
        list.forEach { candidate ->
            importSmsCandidate(
                candidate = candidate,
                asShared = forceShared ?: candidate.isSharedByDefault
            )
        }
    }

    fun dismissSmsCandidate(candidateId: String) {
        _smsCandidates.value = _smsCandidates.value.filterNot { it.id == candidateId }
    }

    // --- MANUAL TRANSACTION LOGGING & PRIVACY TOGGLE ---

    fun addManualTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountName: String,
        paymentMode: String,
        tagsInput: String,
        notes: String,
        isShared: Boolean
    ) {
        viewModelScope.launch {
            val state = uiState.value
            val tags = tagsInput.split(",", " ")
                .map { it.trim().removePrefix("#") }
                .filter { it.isNotBlank() }

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
                isShared = isShared
            )
            result.onSuccess {
                _statusBanner.value = "Added $title to ${if (isShared && state.household != null) "Shared Household" else "Personal"} ledger."
            }.onFailure { err ->
                _errorMessage.value = err.localizedMessage ?: "Failed to add transaction."
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
            val household = uiState.value.household
            repository.saveCategoryBudget(
                category = category,
                monthlyLimit = monthlyLimit,
                alertThresholdPercent = alertThresholdPercent,
                isShared = isShared,
                household = household
            ).onSuccess {
                _statusBanner.value = "Updated $category budget limit."
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
            _statusBanner.value = "Monthly budget limits saved."
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
                // Seed default household category budgets so the family starts with structured caps
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
                _selectedScope.value = BudgetScope.COMBINED
                _statusBanner.value = "Created household '${created.name}' • Join Code: ${created.joinCode}"
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
                _selectedScope.value = BudgetScope.COMBINED
                _statusBanner.value = "Joined family household! Real-time budget sync is now active."
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
                _selectedScope.value = BudgetScope.COMBINED
                _statusBanner.value = "Joined '${household.name}'! Real-time sync is active."
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
                    _statusBanner.value = "Invited $email to '${household.name}'."
                }
                .onFailure { err ->
                    _errorMessage.value = err.localizedMessage ?: "Failed to send email invite."
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
