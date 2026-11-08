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
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import com.example.ui.components.ScopeSwitcherBar
import com.example.ui.screens.AddAccountDialog
import com.example.ui.screens.AddCategoryBudgetDialog
import com.example.ui.screens.AddTransactionDialog
import com.example.ui.screens.AnalyticsTabScreen
import com.example.ui.screens.BudgetsTabScreen
import com.example.ui.screens.DashboardTabScreen
import com.example.ui.screens.EditOverallBudgetDialog
import com.example.ui.screens.EditTransactionDialog
import com.example.ui.screens.HouseholdTabScreen
import com.example.ui.screens.QuickCategoryPickerDialog
import com.example.ui.screens.ThemeSettingsDialog
import com.example.ui.screens.TransactionsLedgerTabScreen
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

    val activeUid = firebaseUser?.uid ?: localUser?.uid
    val activeName = firebaseUser?.displayName
        ?: firebaseUser?.email?.substringBefore("@")
        ?: localUser?.displayName
        ?: "Member"
    val activeEmail = firebaseUser?.email ?: localUser?.email.orEmpty()

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
    DASHBOARD("Home", Icons.Default.Dashboard),
    TRANSACTIONS("Receipts", Icons.AutoMirrored.Filled.ReceiptLong),
    BUDGETS("Budgets", Icons.Default.AccountBalanceWallet),
    ANALYTICS("Charts", Icons.Default.PieChart),
    HOUSEHOLD("Family", Icons.Default.Groups)
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
    var currentTab by rememberSaveable { mutableStateOf(AxioNavTab.DASHBOARD) }

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

    if (currentTab != AxioNavTab.DASHBOARD) {
        BackHandler {
            currentTab = AxioNavTab.DASHBOARD
        }
    }

    val currency = uiState.userProfile?.currencySymbol ?: "₹"

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(
                        onClick = triggerSmsSync,
                        modifier = Modifier.testTag("top_bar_sms_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "Sync Phone SMS"
                        )
                    }
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.testTag("top_bar_theme_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme Settings"
                        )
                    }
                    IconButton(
                        onClick = {
                            signOut(
                                context = context,
                                credentialManager = credentialManager,
                                onSignOutComplete = onSignOut,
                                scope = coroutineScope
                            )
                        },
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTransactionDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_expense")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        },
        bottomBar = {
            NavigationBar {
                AxioNavTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        alwaysShowLabel = false,
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentTab != AxioNavTab.HOUSEHOLD) {
                ScopeSwitcherBar(
                    selectedScope = uiState.selectedScope,
                    hasHousehold = uiState.household != null,
                    householdName = uiState.household?.name,
                    onScopeSelected = { viewModel.selectScope(it) }
                )
            }

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

            Box(modifier = Modifier.weight(1f)) {
                Crossfade(targetState = currentTab, label = "tab_transition") { targetTab ->
                    when (targetTab) {
                        AxioNavTab.DASHBOARD -> DashboardTabScreen(
                            uiState = uiState,
                            onStepMonth = { viewModel.stepMonth(it) },
                            onSelectPeriod = { viewModel.selectPeriod(it) },
                            onEditMonthlyBudget = { showEditOverallBudgetDialog = true },
                            onScanSmsClick = triggerSmsSync,
                            onAddTransactionClick = { showAddTransactionDialog = true },
                            onAddAccountClick = { showAddAccountDialog = true },
                            onOpenHouseholdTab = { currentTab = AxioNavTab.HOUSEHOLD },
                            onOpenTransactionsTab = { currentTab = AxioNavTab.TRANSACTIONS },
                            onSelectCategoryAndOpenLedger = { cat ->
                                viewModel.selectCategoryFilter(cat)
                                currentTab = AxioNavTab.TRANSACTIONS
                            },
                            onAcceptEmailInvite = { viewModel.acceptEmailInvitation(it) },
                            onEditTransaction = { editingTransaction = it },
                            onQuickChangeCategory = { quickCategoryTransaction = it },
                            onToggleTransactionPrivacy = { viewModel.toggleTransactionPrivacy(it) },
                            onDeleteTransaction = { viewModel.deleteTransaction(it) }
                        )

                        AxioNavTab.TRANSACTIONS -> TransactionsLedgerTabScreen(
                            uiState = uiState,
                            onStepMonth = { viewModel.stepMonth(it) },
                            onSelectPeriod = { viewModel.selectPeriod(it) },
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
                            onEditTransaction = { editingTransaction = it },
                            onQuickChangeCategory = { quickCategoryTransaction = it },
                            onToggleTransactionPrivacy = { viewModel.toggleTransactionPrivacy(it) },
                            onDeleteTransaction = { viewModel.deleteTransaction(it) }
                        )

                        AxioNavTab.BUDGETS -> BudgetsTabScreen(
                            uiState = uiState,
                            onStepMonth = { viewModel.stepMonth(it) },
                            onSelectPeriod = { viewModel.selectPeriod(it) },
                            onEditOverallBudgetClick = { showEditOverallBudgetDialog = true },
                            onAddOrEditCategoryBudgetClick = { catProg ->
                                editingCategoryBudget = true to catProg
                            }
                        )

                        AxioNavTab.ANALYTICS -> AnalyticsTabScreen(
                            uiState = uiState,
                            onStepMonth = { viewModel.stepMonth(it) },
                            onSelectPeriod = { viewModel.selectPeriod(it) }
                        )

                        AxioNavTab.HOUSEHOLD -> HouseholdTabScreen(
                            uiState = uiState,
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
            personalBudget = uiState.userProfile?.personalMonthlyBudget ?: 35000.0,
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
