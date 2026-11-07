package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import com.example.data.model.BudgetScope
import com.example.data.repository.AxioRepository
import com.example.data.sms.SmsInboxReader
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.components.ScopeSwitcherBar
import com.example.ui.screens.AddAccountDialog
import com.example.ui.screens.AddCategoryBudgetDialog
import com.example.ui.screens.AddTransactionDialog
import com.example.ui.screens.AnalyticsTabScreen
import com.example.ui.screens.BudgetsTabScreen
import com.example.ui.screens.CashiroSmsSyncDialog
import com.example.ui.screens.DashboardTabScreen
import com.example.ui.screens.EditOverallBudgetDialog
import com.example.ui.screens.HouseholdTabScreen
import com.example.ui.screens.TransactionsLedgerTabScreen
import com.example.ui.theme.AxioFamilyTheme
import com.example.ui.viewmodel.AxioViewModel
import com.example.ui.viewmodel.CategorySpendProgress
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AxioFamilyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
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
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val flowUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var directUser by remember { mutableStateOf(auth.currentUser) }
    LaunchedEffect(flowUser) {
        directUser = flowUser
    }
    val user = flowUser ?: directUser

    if (user == null) {
        AuthScreen(onAuthSuccess = { directUser = auth.currentUser })
    } else {
        AxioMainShell(
            currentUserId = user.uid,
            currentUserName = user.displayName ?: user.email?.substringBefore("@") ?: "Family Member",
            currentUserEmail = user.email.orEmpty(),
            onSignOut = { directUser = null }
        )
    }
}

enum class AxioNavTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Pulse", Icons.Default.Dashboard),
    TRANSACTIONS("Ledger", Icons.AutoMirrored.Filled.ReceiptLong),
    BUDGETS("Budgets", Icons.Default.AccountBalanceWallet),
    ANALYTICS("Analytics", Icons.Default.PieChart),
    HOUSEHOLD("Family", Icons.Default.Groups)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AxioMainShell(
    currentUserId: String,
    currentUserName: String,
    currentUserEmail: String,
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
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                AxioViewModel(
                    repository = AxioRepository(db),
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
    var showSmsParserDialog by rememberSaveable { mutableStateOf(false) }
    var showAddAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showEditOverallBudgetDialog by rememberSaveable { mutableStateOf(false) }
    var editingCategoryBudget by remember { mutableStateOf<Pair<Boolean, CategorySpendProgress?>>(false to null) }

    // Runtime permission launcher for READ_SMS
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanDeviceSmsInbox()
            showSmsParserDialog = true
        } else {
            // Even if denied or on an emulator without SIM, open the Cashiro Bank SMS Parser dialog
            viewModel.loadSampleBankSmsBatch()
            showSmsParserDialog = true
        }
    }

    val triggerSmsSync = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            viewModel.scanDeviceSmsInbox()
            showSmsParserDialog = true
        } else {
            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
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
                title = {
                    Column {
                        Text(
                            text = "Axio Family",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (uiState.household != null) {
                                "${uiState.household!!.name} • Code: ${uiState.household!!.joinCode}"
                            } else {
                                "Personal Expense & Household Budget Tracker"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSmsParserDialog = true },
                        modifier = Modifier.testTag("top_bar_sms_parser_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "Cashiro Bank SMS Parser"
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
            ExtendedFloatingActionButton(
                onClick = { showAddTransactionDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Expense") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_expense")
            )
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
                        label = { Text(tab.label) },
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
            // Persistent Scope Switcher Bar across Dashboard, Ledger, Budgets & Analytics
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
                                    contentDescription = "Dismiss notification",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    AxioNavTab.DASHBOARD -> DashboardTabScreen(
                        uiState = uiState,
                        onEditMonthlyBudget = { showEditOverallBudgetDialog = true },
                        onScanSmsClick = triggerSmsSync,
                        onOpenSmsTesterClick = {
                            if (uiState.smsCandidates.isEmpty()) {
                                viewModel.loadSampleBankSmsBatch()
                            }
                            showSmsParserDialog = true
                        },
                        onAddTransactionClick = { showAddTransactionDialog = true },
                        onAddAccountClick = { showAddAccountDialog = true },
                        onOpenHouseholdTab = { currentTab = AxioNavTab.HOUSEHOLD },
                        onOpenTransactionsTab = { currentTab = AxioNavTab.TRANSACTIONS },
                        onAcceptEmailInvite = { viewModel.acceptEmailInvitation(it) },
                        onToggleTransactionPrivacy = { viewModel.toggleTransactionPrivacy(it) },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) }
                    )

                    AxioNavTab.TRANSACTIONS -> TransactionsLedgerTabScreen(
                        uiState = uiState,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onSelectCategory = { viewModel.selectCategoryFilter(it) },
                        onSelectTag = { viewModel.selectTagFilter(it) },
                        onSelectMember = { viewModel.selectMemberFilter(it) },
                        onClearFilters = { viewModel.clearFilters() },
                        onAddTransactionClick = { showAddTransactionDialog = true },
                        onOpenSmsTesterClick = {
                            if (uiState.smsCandidates.isEmpty()) {
                                viewModel.loadSampleBankSmsBatch()
                            }
                            showSmsParserDialog = true
                        },
                        onToggleTransactionPrivacy = { viewModel.toggleTransactionPrivacy(it) },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) }
                    )

                    AxioNavTab.BUDGETS -> BudgetsTabScreen(
                        uiState = uiState,
                        onEditOverallBudgetClick = { showEditOverallBudgetDialog = true },
                        onAddOrEditCategoryBudgetClick = { catProg ->
                            editingCategoryBudget = true to catProg
                        }
                    )

                    AxioNavTab.ANALYTICS -> AnalyticsTabScreen(
                        uiState = uiState
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

    // Dialogs & Sheets
    if (showAddTransactionDialog) {
        AddTransactionDialog(
            hasHousehold = uiState.household != null,
            defaultShared = uiState.selectedScope != BudgetScope.PERSONAL_ONLY,
            accounts = uiState.accounts,
            currencySymbol = currency,
            onDismiss = { showAddTransactionDialog = false },
            onSave = { title, amount, type, category, accountName, paymentMode, tagsInput, notes, isShared ->
                viewModel.addManualTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    accountName = accountName,
                    paymentMode = paymentMode,
                    tagsInput = tagsInput,
                    notes = notes,
                    isShared = isShared
                )
                showAddTransactionDialog = false
            }
        )
    }

    if (showSmsParserDialog) {
        CashiroSmsSyncDialog(
            candidates = uiState.smsCandidates,
            hasHousehold = uiState.household != null,
            currencySymbol = currency,
            onScanInbox = triggerSmsSync,
            onLoadSampleBatch = { viewModel.loadSampleBankSmsBatch() },
            onParseCustomSms = { sender, body, defShared ->
                viewModel.parseCustomOrTemplateSms(sender, body, defShared)
            },
            onImportCandidate = { cand, asShared ->
                viewModel.importSmsCandidate(cand, asShared)
            },
            onImportAll = { forceShared ->
                viewModel.importAllSmsCandidates(forceShared)
            },
            onDismissCandidate = { id ->
                viewModel.dismissSmsCandidate(id)
            },
            onDismiss = { showSmsParserDialog = false }
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
