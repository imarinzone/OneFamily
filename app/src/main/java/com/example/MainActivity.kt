package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.LocalAccountSessionManager
import com.example.data.model.BudgetScope
import com.example.data.model.ExpenseTransaction
import com.example.data.repository.AxioRepository
import com.example.data.sms.SmsInboxReader
import com.example.data.sms.TransactionNotificationHelper
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.screens.AddAccountDialog
import com.example.ui.screens.AddCategoryBudgetDialog
import com.example.ui.screens.AddTransactionDialog
import com.example.ui.screens.AnalyticsTabScreen
import com.example.ui.screens.BudgetsTabScreen
import com.example.ui.screens.EditOverallBudgetDialog
import com.example.ui.screens.EditTransactionDialog
import com.example.ui.screens.HouseholdTabScreen
import com.example.ui.screens.QuickCategoryPickerDialog
import com.example.ui.screens.ThemeSettingsDialog
import com.example.ui.theme.AxioFamilyTheme
import com.example.ui.viewmodel.AxioViewModel
import com.example.ui.viewmodel.CategorySpendProgress
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val pendingNotificationExpenseId = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalAccountSessionManager.ensureInitialized(this)
        TransactionNotificationHelper.ensureChannel(this)
        extractNotificationDeepLink(intent)
        enableEdgeToEdge()
        setContent {
            val deepLinkExpenseId by pendingNotificationExpenseId.collectAsStateWithLifecycle()
            val themeMode by LocalAccountSessionManager.themeMode.collectAsStateWithLifecycle()
            AxioFamilyTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        deepLinkExpenseId = deepLinkExpenseId,
                        onConsumeDeepLinkExpenseId = { pendingNotificationExpenseId.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractNotificationDeepLink(intent)
    }

    private fun extractNotificationDeepLink(intent: Intent?) {
        val expenseId = intent?.getStringExtra(TransactionNotificationHelper.EXTRA_EDIT_EXPENSE_ID)
        if (!expenseId.isNullOrBlank()) {
            pendingNotificationExpenseId.value = expenseId
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(
    auth: FirebaseAuth = Firebase.auth,
    deepLinkExpenseId: String? = null,
    onConsumeDeepLinkExpenseId: () -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        LocalAccountSessionManager.ensureInitialized(context)
    }
    val flowUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val localUser by LocalAccountSessionManager.activeUser.collectAsStateWithLifecycle()
    var directUser by remember { mutableStateOf(auth.currentUser) }
    LaunchedEffect(flowUser) {
        directUser = flowUser
    }
    val firebaseUser = flowUser ?: directUser

    val activeUid = localUser?.uid ?: firebaseUser?.uid
    val activeName = localUser?.displayName
        ?: firebaseUser?.displayName
        ?: firebaseUser?.email?.substringBefore("@")
        ?: "Member"
    val activeEmail = localUser?.email ?: firebaseUser?.email.orEmpty()

    if (activeUid == null) {
        AuthScreen(onAuthSuccess = { directUser = auth.currentUser })
    } else {
        AxioMainShell(
            currentUserId = activeUid,
            currentUserName = activeName,
            currentUserEmail = activeEmail,
            deepLinkExpenseId = deepLinkExpenseId,
            onConsumeDeepLinkExpenseId = onConsumeDeepLinkExpenseId,
            onSignOut = {
                LocalAccountSessionManager.signOut(context)
                directUser = null
            }
        )
    }
}

enum class AxioNavTab(val label: String, val icon: ImageVector) {
    ANALYSIS("Analysis", Icons.Default.PieChart),
    BUDGETS("Budgets", Icons.Default.AccountBalanceWallet),
    ACCOUNT("Account", Icons.Default.Groups)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AxioMainShell(
    currentUserId: String,
    currentUserName: String,
    currentUserEmail: String,
    deepLinkExpenseId: String? = null,
    onConsumeDeepLinkExpenseId: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: AxioViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                AxioViewModel(
                    repository = AxioRepository(app),
                    smsInboxReader = SmsInboxReader(app),
                    currentUserId = currentUserId,
                    currentUserName = currentUserName,
                    currentUserEmail = currentUserEmail
                )
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = AxioNavTab.entries
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val currentTab = tabs[pagerState.currentPage]

    var showAddTransactionDialog by rememberSaveable { mutableStateOf(false) }
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showAddAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showEditOverallBudgetDialog by rememberSaveable { mutableStateOf(false) }
    var editingCategoryBudget by remember { mutableStateOf<Pair<Boolean, CategorySpendProgress?>>(false to null) }
    var editingTransaction by remember { mutableStateOf<ExpenseTransaction?>(null) }
    var quickCategoryTransaction by remember { mutableStateOf<ExpenseTransaction?>(null) }

    // Open EditTransactionDialog automatically when user taps a real-time SMS notification
    LaunchedEffect(deepLinkExpenseId, uiState.personalExpenses, uiState.sharedExpenses) {
        if (!deepLinkExpenseId.isNullOrBlank()) {
            val allTxs = uiState.sharedExpenses + uiState.personalExpenses
            val match = allTxs.firstOrNull { it.id == deepLinkExpenseId }
            if (match != null) {
                editingTransaction = match
                onConsumeDeepLinkExpenseId()
            }
        }
    }

    // Multi-permission launcher for READ_SMS, RECEIVE_SMS, and POST_NOTIFICATIONS
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantMap ->
        val readSmsGranted = grantMap[Manifest.permission.READ_SMS] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        if (readSmsGranted) {
            viewModel.scanDeviceSmsInbox(context = context, silentIfEmpty = false, notifyNewest = true)
        }
    }

    // Auto-scan SMS on first launch if permission is already granted, or request permissions
    LaunchedEffect(currentUserId) {
        val hasReadSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (hasReadSms) {
            viewModel.autoScanSmsOnLaunchIfNeeded(context)
        } else {
            val perms = buildList {
                add(Manifest.permission.READ_SMS)
                add(Manifest.permission.RECEIVE_SMS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            permissionsLauncher.launch(perms.toTypedArray())
        }
    }

    val triggerSmsSync = {
        val hasReadSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (hasReadSms) {
            viewModel.scanDeviceSmsInbox(context = context, silentIfEmpty = false, notifyNewest = true)
        } else {
            val perms = buildList {
                add(Manifest.permission.READ_SMS)
                add(Manifest.permission.RECEIVE_SMS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            permissionsLauncher.launch(perms.toTypedArray())
        }
    }

    if (currentTab != AxioNavTab.ANALYSIS) {
        BackHandler {
            coroutineScope.launch {
                pagerState.animateScrollToPage(AxioNavTab.ANALYSIS.ordinal)
            }
        }
    }

    val currency = uiState.userProfile?.currencySymbol ?: "₹"

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            AccountSharingTopBar(
                userName = currentUserName,
                selectedScope = uiState.selectedScope,
                hasHousehold = uiState.household != null,
                householdName = uiState.household?.name,
                onScopeSelected = { viewModel.selectScope(it) },
                onOpenAccountTab = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(AxioNavTab.ACCOUNT.ordinal)
                    }
                },
                onSmsSyncClick = triggerSmsSync,
                onThemeClick = { showThemeDialog = true },
                onSignOutClick = {
                    signOut(
                        context = context,
                        credentialManager = credentialManager,
                        onSignOutComplete = onSignOut,
                        scope = coroutineScope
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Status / Error Banner
                val bannerMsg = uiState.errorMessage ?: uiState.statusBannerMessage
                AnimatedVisibility(visible = bannerMsg != null) {
                    if (bannerMsg != null) {
                        val isErr = uiState.errorMessage != null
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isErr) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = bannerMsg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isErr) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.dismissBanner() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Gesture-Enabled HorizontalPager across tabs
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { pageIndex ->
                    when (tabs[pageIndex]) {
                        AxioNavTab.ANALYSIS -> AnalyticsTabScreen(
                            uiState = uiState,
                            onStepMonth = { viewModel.stepMonth(it) },
                            onSelectPeriod = { viewModel.selectPeriod(it) },
                            onSelectDateRangePreset = { viewModel.selectDateRangePreset(it) },
                            onSelectCustomDateRange = { s, e -> viewModel.selectCustomDateRange(s, e) },
                            onEditMonthlyBudget = { showEditOverallBudgetDialog = true },
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onSelectType = { viewModel.selectTypeFilter(it) },
                            onSelectCategory = { viewModel.selectCategoryFilter(it) },
                            onSelectPaymentMode = { viewModel.selectPaymentModeFilter(it) },
                            onSelectAccount = { viewModel.selectAccountFilter(it) },
                            onSelectTag = { viewModel.selectTagFilter(it) },
                            onSelectMember = { viewModel.selectMemberFilter(it) },
                            onSelectSortOrder = { viewModel.selectSortOrder(it) },
                            onClearFilters = { viewModel.clearFilters() },
                            onScanSmsClick = triggerSmsSync,
                            onAddTransactionClick = { showAddTransactionDialog = true },
                            onAcceptEmailInvite = { viewModel.acceptEmailInvitation(it) },
                            onEditTransaction = { editingTransaction = it },
                            onQuickChangeCategory = { quickCategoryTransaction = it },
                            onToggleTransactionPrivacy = { viewModel.toggleTransactionPrivacy(it) },
                            onDeleteTransaction = { viewModel.deleteTransaction(it) }
                        )

                        AxioNavTab.BUDGETS -> BudgetsTabScreen(
                            uiState = uiState,
                            onStepMonth = { viewModel.stepMonth(it) },
                            onSelectPeriod = { viewModel.selectPeriod(it) },
                            onSelectDateRangePreset = { viewModel.selectDateRangePreset(it) },
                            onSelectCustomDateRange = { s, e -> viewModel.selectCustomDateRange(s, e) },
                            onEditOverallBudgetClick = { showEditOverallBudgetDialog = true },
                            onAddOrEditCategoryBudgetClick = { catProg ->
                                editingCategoryBudget = true to catProg
                            }
                        )

                        AxioNavTab.ACCOUNT -> HouseholdTabScreen(
                            uiState = uiState,
                            currentUserName = currentUserName,
                            currentUserEmail = currentUserEmail,
                            onScopeSelected = { viewModel.selectScope(it) },
                            onAddAccountClick = { showAddAccountDialog = true },
                            onScanSmsClick = triggerSmsSync,
                            onSignOutClick = {
                                signOut(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onSignOutComplete = onSignOut,
                                    scope = coroutineScope
                                )
                            },
                            onCreateHousehold = { name, limit ->
                                viewModel.createFamilyHousehold(name, limit)
                            },
                            onJoinByCode = { code ->
                                viewModel.joinHouseholdWithCode(code)
                            },
                            onInviteByEmail = { email ->
                                viewModel.inviteFamilyMemberByEmail(email)
                            },
                            onAcceptEmailInvite = { household ->
                                viewModel.acceptEmailInvitation(household)
                            }
                        )
                    }
                }
            }

            // Floating Bottom Navigation Pill Bar
            FloatingBottomNavBar(
                tabs = tabs,
                selectedTab = currentTab,
                onSelectTab = { tab ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(tab.ordinal)
                    }
                },
                onAddClick = { showAddTransactionDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 14.dp)
            )
        }
    }

    // Dialogs & Sheets
    if (showAddTransactionDialog) {
        AddTransactionDialog(
            hasHousehold = uiState.household != null,
            defaultShared = uiState.selectedScope != BudgetScope.PERSONAL_ONLY,
            accounts = uiState.accounts,
            availableCategories = uiState.availableCategories,
            currencySymbol = currency,
            onDismiss = { showAddTransactionDialog = false },
            onSave = { title, amount, type, category, accountName, paymentMode, tagsInput, notes, isShared, txDateMillis ->
                viewModel.addManualTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    accountName = accountName,
                    paymentMode = paymentMode,
                    tagsInput = tagsInput,
                    notes = notes,
                    isShared = isShared,
                    transactionDateMillis = txDateMillis,
                    context = context
                )
                showAddTransactionDialog = false
            }
        )
    }

    if (editingTransaction != null) {
        val txToEdit = editingTransaction!!
        EditTransactionDialog(
            transaction = txToEdit,
            hasHousehold = uiState.household != null,
            availableCategories = uiState.availableCategories,
            currencySymbol = currency,
            onDismiss = { editingTransaction = null },
            onDelete = {
                viewModel.deleteTransaction(txToEdit)
                editingTransaction = null
            },
            onSave = { title, amount, type, category, accountName, paymentMode, tagsInput, notes, txDateMillis, targetShared ->
                viewModel.updateTransaction(
                    original = txToEdit,
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    accountName = accountName,
                    paymentMode = paymentMode,
                    tagsInput = tagsInput,
                    notes = notes,
                    transactionDateMillis = txDateMillis,
                    targetShared = targetShared
                )
                editingTransaction = null
            }
        )
    }

    if (quickCategoryTransaction != null) {
        val txForCat = quickCategoryTransaction!!
        QuickCategoryPickerDialog(
            transaction = txForCat,
            availableCategories = uiState.availableCategories,
            onDismiss = { quickCategoryTransaction = null },
            onSelectCategory = { newCat ->
                viewModel.quickUpdateTransactionCategory(txForCat, newCat)
                quickCategoryTransaction = null
            },
            onOpenFullEditor = {
                quickCategoryTransaction = null
                editingTransaction = txForCat
            }
        )
    }

    if (showThemeDialog) {
        val currentThemeMode by LocalAccountSessionManager.themeMode.collectAsStateWithLifecycle()
        ThemeSettingsDialog(
            currentMode = currentThemeMode,
            onSelectMode = { LocalAccountSessionManager.setThemeMode(context, it) },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (editingCategoryBudget.first) {
        AddCategoryBudgetDialog(
            initialProgress = editingCategoryBudget.second,
            hasHousehold = uiState.household != null,
            defaultShared = uiState.selectedScope != BudgetScope.PERSONAL_ONLY,
            currencySymbol = currency,
            onDismiss = { editingCategoryBudget = false to null },
            onSave = { category, limit, alertPct, isShared ->
                viewModel.saveCategoryBudget(category, limit, alertPct, isShared)
                editingCategoryBudget = false to null
            }
        )
    }

    if (showEditOverallBudgetDialog) {
        EditOverallBudgetDialog(
            personalBudget = uiState.userProfile?.personalMonthlyBudget ?: 0.0,
            householdBudget = uiState.household?.monthlyBudgetLimit,
            currencySymbol = currency,
            onDismiss = { showEditOverallBudgetDialog = false },
            onSave = { pLimit, hLimit, curr ->
                viewModel.updateOverallMonthlyBudget(pLimit, hLimit, curr)
                showEditOverallBudgetDialog = false
            }
        )
    }

    if (showAddAccountDialog) {
        AddAccountDialog(
            currencySymbol = currency,
            onDismiss = { showAddAccountDialog = false },
            onSave = { name, bankName, accountType, lastFour, balance, creditLimit ->
                viewModel.saveFinancialAccount(
                    name = name,
                    bankName = bankName,
                    accountType = accountType,
                    lastFourDigits = lastFour,
                    balance = balance,
                    creditLimit = creditLimit,
                    isSharedVisible = true
                )
                showAddAccountDialog = false
            }
        )
    }
}

@Composable
private fun AccountSharingTopBar(
    userName: String,
    selectedScope: BudgetScope,
    hasHousehold: Boolean,
    householdName: String?,
    onScopeSelected: (BudgetScope) -> Unit,
    onOpenAccountTab: () -> Unit,
    onSmsSyncClick: () -> Unit,
    onThemeClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Unified Account & Sharing Pill Bar
            Surface(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .testTag("top_account_sharing_bar"),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // User Avatar & Name Pill -> Tapping opens Account & Sharing settings tab
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onOpenAccountTab)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("top_account_profile_pill"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Account",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = userName.ifBlank { "Account" },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Quick Sharing Toggle (Household vs Personal)
                    val isHouseholdScope = selectedScope == BudgetScope.HOUSEHOLD_SHARED && hasHousehold
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                if (hasHousehold) {
                                    val next = if (selectedScope == BudgetScope.HOUSEHOLD_SHARED) {
                                        BudgetScope.PERSONAL_ONLY
                                    } else {
                                        BudgetScope.HOUSEHOLD_SHARED
                                    }
                                    onScopeSelected(next)
                                } else {
                                    onOpenAccountTab()
                                }
                            }
                            .testTag("top_sharing_scope_toggle"),
                        shape = RoundedCornerShape(50),
                        color = if (isHouseholdScope) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isHouseholdScope) Icons.Default.Groups else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isHouseholdScope) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isHouseholdScope) {
                                    householdName?.take(10) ?: "Family"
                                } else {
                                    "Personal"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isHouseholdScope) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Right: Action Icon Buttons (No title text)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onSmsSyncClick,
                    modifier = Modifier.testTag("top_bar_sms_sync_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "Sync Phone SMS"
                    )
                }
                IconButton(
                    onClick = onThemeClick,
                    modifier = Modifier.testTag("top_bar_theme_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Theme Settings"
                    )
                }
                IconButton(
                    onClick = onSignOutClick,
                    modifier = Modifier.testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out"
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingBottomNavBar(
    tabs: List<AxioNavTab>,
    selectedTab: AxioNavTab,
    onSelectTab: (AxioNavTab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(50),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            )
            .testTag("floating_bottom_nav_bar"),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "floating_nav_bg"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "floating_nav_fg"
                )

                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .width(56.dp)
                        .clip(RoundedCornerShape(50))
                        .background(bgColor)
                        .clickable { onSelectTab(tab) }
                        .testTag("nav_tab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Integrated Floating Add Receipt Button inside the Floating Pill
            Surface(
                onClick = onAddClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("fab_add_expense"),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
