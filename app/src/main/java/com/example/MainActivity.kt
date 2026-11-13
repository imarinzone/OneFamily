package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.data.repository.OneFamilyRepository
import com.example.data.sms.SmsInboxContentObserver
import com.example.data.sms.SmsInboxReader
import com.example.data.sms.TransactionNotificationHelper
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.components.AppDecoratedBackground
import com.example.ui.components.MonthPeriodSelectorBar
import com.example.ui.screens.AddAccountDialog
import com.example.ui.screens.AddCategoryBudgetDialog
import com.example.ui.screens.AddCustomCategoryDialog
import com.example.ui.screens.AddTransactionDialog
import com.example.ui.screens.AnalyticsTabScreen
import com.example.ui.screens.BudgetsTabScreen
import com.example.ui.screens.EditOverallBudgetDialog
import com.example.ui.screens.EditTransactionDialog
import com.example.ui.screens.ExportTransactionsCsvDialog
import com.example.ui.screens.FirstTimeSmsSyncScreen
import com.example.ui.screens.HomeTabScreen
import com.example.ui.screens.HouseholdTabScreen
import com.example.ui.screens.LinkDuplicateTransactionDialog
import com.example.ui.screens.QuickCategoryPickerDialog
import com.example.ui.theme.LocalAppDimens
import com.example.ui.theme.OneFamilyTheme
import com.example.ui.theme.rememberAppHapticPerformer
import com.example.ui.viewmodel.CategorySpendProgress
import com.example.ui.viewmodel.DateRangePreset
import com.example.ui.viewmodel.OneFamilyViewModel
import com.example.ui.viewmodel.YearMonthPeriod
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
            OneFamilyTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppDecoratedBackground(modifier = Modifier.fillMaxSize()) {
                        AppNavigation(
                            deepLinkExpenseId = deepLinkExpenseId,
                            onConsumeDeepLinkExpenseId = { pendingNotificationExpenseId.value = null }
                        )
                    }
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

    AnimatedContent(
        targetState = activeUid,
        transitionSpec = {
            val spatialSpring = spring<androidx.compose.ui.unit.IntOffset>(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
            val alphaSpring = spring<Float>(stiffness = Spring.StiffnessMedium)
            if (targetState != null && initialState == null) {
                (slideInVertically(spatialSpring) { it / 6 } + fadeIn(alphaSpring))
                    .togetherWith(slideOutVertically(spatialSpring) { -it / 8 } + fadeOut(alphaSpring))
            } else if (targetState == null && initialState != null) {
                (slideInVertically(spatialSpring) { -it / 6 } + fadeIn(alphaSpring))
                    .togetherWith(slideOutVertically(spatialSpring) { it / 6 } + fadeOut(alphaSpring))
            } else {
                fadeIn(alphaSpring).togetherWith(fadeOut(alphaSpring))
            }
        },
        label = "app_auth_shell_transition"
    ) { resolvedUid ->
        if (resolvedUid == null) {
            AuthScreen(onAuthSuccess = { directUser = auth.currentUser })
        } else {
            OneFamilyMainShell(
                currentUserId = resolvedUid,
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
}

enum class OneFamilyNavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    ANALYSIS("Analysis", Icons.Filled.PieChart, Icons.Outlined.PieChart),
    BUDGETS("Budgets", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    ACCOUNT("Settings", Icons.Filled.Settings, Icons.Outlined.Settings);

    val icon: ImageVector
        get() = selectedIcon
}

@Composable
fun OneFamilyMainShell(
    currentUserId: String,
    currentUserName: String,
    currentUserEmail: String,
    deepLinkExpenseId: String? = null,
    onConsumeDeepLinkExpenseId: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val performHaptic = rememberAppHapticPerformer()

    val viewModel: OneFamilyViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                OneFamilyViewModel(
                    repository = OneFamilyRepository(app),
                    smsInboxReader = SmsInboxReader(app),
                    currentUserId = currentUserId,
                    currentUserName = currentUserName,
                    currentUserEmail = currentUserEmail
                )
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val smsSyncProgress by viewModel.smsSyncProgress.collectAsStateWithLifecycle()
    val tabs = OneFamilyNavTab.entries
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val currentTab = tabs[pagerState.currentPage]

    var showSmsSyncScreen by rememberSaveable(currentUserId) {
        mutableStateOf(!LocalAccountSessionManager.hasCompletedInitialSmsSync(context, currentUserId))
    }
    var hasReadSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showAddTransactionDialog by rememberSaveable { mutableStateOf(false) }
    var showAddAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showEditOverallBudgetDialog by rememberSaveable { mutableStateOf(false) }
    var showAddCustomCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var showExportCsvDialog by rememberSaveable { mutableStateOf(false) }
    var pendingCsvExport by remember { mutableStateOf<Triple<String, Int, String>?>(null) }
    var editingCategoryBudget by remember { mutableStateOf<Pair<Boolean, CategorySpendProgress?>>(false to null) }
    var editingTransaction by remember { mutableStateOf<ExpenseTransaction?>(null) }
    var quickCategoryTransaction by remember { mutableStateOf<ExpenseTransaction?>(null) }
    var linkingDuplicateTransaction by remember { mutableStateOf<ExpenseTransaction?>(null) }

    val createCsvDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val payload = pendingCsvExport
        pendingCsvExport = null
        if (uri != null && payload != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(payload.first.toByteArray(Charsets.UTF_8))
                    stream.flush()
                } ?: error("Unable to open destination file stream.")
            }.onSuccess {
                viewModel.notifyCsvExportResult(
                    exportedCount = payload.second,
                    periodLabel = payload.third
                )
            }.onFailure { err ->
                viewModel.notifyCsvExportError(err.localizedMessage ?: "Failed to save CSV file.")
            }
        }
    }

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

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantMap ->
        val readSmsGranted = grantMap[Manifest.permission.READ_SMS] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        hasReadSmsPermission = readSmsGranted
        if (readSmsGranted) {
            showSmsSyncScreen = true
            viewModel.scanDeviceSmsInbox(context = context, silentIfEmpty = false, notifyNewest = true)
        }
    }

    DisposableEffect(context, currentUserId, hasReadSmsPermission) {
        val observer = if (hasReadSmsPermission) {
            runCatching {
                val obs = SmsInboxContentObserver(
                    context = context.applicationContext
                )
                context.contentResolver.registerContentObserver(
                    Telephony.Sms.CONTENT_URI,
                    true,
                    obs
                )
                obs
            }.getOrNull()
        } else {
            null
        }
        onDispose {
            if (observer != null) {
                runCatching { context.contentResolver.unregisterContentObserver(observer) }
            }
        }
    }

    val startOrRequestSmsSync = {
        val hasReadSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        hasReadSmsPermission = hasReadSms
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

    val triggerSmsSync = {
        viewModel.resetSmsSyncProgress()
        showSmsSyncScreen = true
        startOrRequestSmsSync()
    }

    val navigateToTab: (OneFamilyNavTab) -> Unit = { tab ->
        performHaptic()
        coroutineScope.launch {
            pagerState.animateScrollToPage(
                page = tab.ordinal,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    val currency = uiState.activeCurrencySymbol

    AnimatedContent(
        targetState = showSmsSyncScreen,
        transitionSpec = {
            val spatialSpring = spring<androidx.compose.ui.unit.IntOffset>(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
            val alphaSpring = spring<Float>(stiffness = Spring.StiffnessMedium)
            if (targetState) {
                (slideInVertically(spatialSpring) { it / 5 } + fadeIn(alphaSpring))
                    .togetherWith(slideOutVertically(spatialSpring) { -it / 8 } + fadeOut(alphaSpring))
            } else {
                (slideInVertically(spatialSpring) { -it / 8 } + fadeIn(alphaSpring))
                    .togetherWith(slideOutVertically(spatialSpring) { it / 5 } + fadeOut(alphaSpring))
            }
        },
        label = "sms_sync_dashboard_transition"
    ) { isSyncScreenVisible ->
        if (isSyncScreenVisible) {
            FirstTimeSmsSyncScreen(
                syncState = smsSyncProgress,
                onStartSyncClick = startOrRequestSmsSync,
                onContinueToAppClick = {
                    LocalAccountSessionManager.setInitialSmsSyncCompleted(context, currentUserId, true)
                    viewModel.resetSmsSyncProgress()
                    showSmsSyncScreen = false
                },
                onSkipClick = {
                    LocalAccountSessionManager.setInitialSmsSyncCompleted(context, currentUserId, true)
                    viewModel.resetSmsSyncProgress()
                    showSmsSyncScreen = false
                },
                currencySymbol = currency
            )
        } else {
            if (currentTab != OneFamilyNavTab.HOME) {
                BackHandler {
                    navigateToTab(OneFamilyNavTab.HOME)
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWideScreen = maxWidth >= 600.dp

                val topDateSelector: @Composable () -> Unit = {
                    val isDateBarApplicable = currentTab == OneFamilyNavTab.HOME || currentTab == OneFamilyNavTab.ANALYSIS
                    AnimatedVisibility(
                        visible = isDateBarApplicable,
                        enter = slideInVertically(
                            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                        ) { -it } + fadeIn(),
                        exit = slideOutVertically(
                            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                        ) { -it } + fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(vertical = dimens.itemSpacing * 0.35f),
                            color = Color.Transparent
                        ) {
                            MonthPeriodSelectorBar(
                                selectedPeriod = uiState.selectedPeriod,
                                periodLabel = uiState.periodLabel,
                                onStepMonth = { viewModel.stepMonth(it) },
                                onSelectCurrentMonth = { viewModel.selectPeriod(YearMonthPeriod.current()) },
                                onToggleAllTime = {
                                    if (uiState.selectedDateRangePreset == DateRangePreset.ALL_TIME) {
                                        viewModel.selectDateRangePreset(DateRangePreset.MONTH)
                                    } else {
                                        viewModel.selectDateRangePreset(DateRangePreset.ALL_TIME)
                                    }
                                },
                                selectedDateRangePreset = uiState.selectedDateRangePreset,
                                onSelectDateRangePreset = { viewModel.selectDateRangePreset(it) },
                                onSelectMonthPeriod = { viewModel.selectPeriod(it) },
                                customStartDateMillis = uiState.customStartDateMillis,
                                customEndDateMillis = uiState.customEndDateMillis,
                                onSelectCustomDateRange = { s, e -> viewModel.selectCustomDateRange(s, e) },
                                showDateRangeChips = false
                            )
                        }
                    }
                }

                val pagerContent: @Composable (Modifier) -> Unit = { pagerModifier ->
                    HorizontalPager(
                        state = pagerState,
                        modifier = pagerModifier
                    ) { pageIndex ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction)
                                    val absOffset = kotlin.math.abs(pageOffset).coerceIn(0f, 1f)
                                    alpha = 1f - absOffset * 0.16f
                                    val scale = 1f - absOffset * 0.025f
                                    scaleX = scale
                                    scaleY = scale
                                }
                        ) {
                            when (tabs[pageIndex]) {
                                OneFamilyNavTab.HOME -> HomeTabScreen(
                                    uiState = uiState,
                                    onScopeSelected = { viewModel.selectScope(it) },
                                    onSelectDateRangePreset = { viewModel.selectDateRangePreset(it) },
                                    onSelectCustomDateRange = { s, e -> viewModel.selectCustomDateRange(s, e) },
                                    onEditMonthlyBudget = { navigateToTab(OneFamilyNavTab.BUDGETS) },
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
                                    onExportCsvClick = { showExportCsvDialog = true },
                                    onAddTransactionClick = { showAddTransactionDialog = true },
                                    onAcceptEmailInvite = { viewModel.acceptEmailInvitation(it) },
                                    onEditTransaction = { editingTransaction = it },
                                    onQuickChangeCategory = { quickCategoryTransaction = it },
                                    onToggleTransactionPrivacy = { viewModel.toggleTransactionPrivacy(it) },
                                    onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                    onLinkDuplicateTransaction = { linkingDuplicateTransaction = it },
                                    onUndoLastSwipeAction = { viewModel.undoLastSwipeAction() }
                                )

                                OneFamilyNavTab.ANALYSIS -> AnalyticsTabScreen(
                                    uiState = uiState,
                                    onStepMonth = { viewModel.stepMonth(it) },
                                    onSelectPeriod = { viewModel.selectPeriod(it) },
                                    onSelectCategory = { viewModel.selectCategoryFilter(it) },
                                    onEditTransaction = { editingTransaction = it },
                                    onQuickChangeCategory = { quickCategoryTransaction = it },
                                    onToggleTransactionPrivacy = { viewModel.toggleTransactionPrivacy(it) },
                                    onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                    onLinkDuplicateTransaction = { linkingDuplicateTransaction = it }
                                )

                                OneFamilyNavTab.BUDGETS -> BudgetsTabScreen(
                                    uiState = uiState,
                                    onEditOverallBudgetClick = { showEditOverallBudgetDialog = true },
                                    onSaveCategoryBudgetInline = { category, limit ->
                                        viewModel.saveCategoryBudget(
                                            category = category,
                                            monthlyLimit = limit,
                                            alertThresholdPercent = 80,
                                            isShared = uiState.householdFeatureEnabled &&
                                                uiState.selectedScope != BudgetScope.PERSONAL_ONLY
                                        )
                                    },
                                    onAddCustomCategoryClick = { showAddCustomCategoryDialog = true }
                                )

                                OneFamilyNavTab.ACCOUNT -> HouseholdTabScreen(
                                    uiState = uiState,
                                    currentUserName = currentUserName,
                                    currentUserEmail = currentUserEmail,
                                    onUpdateUserName = { newName ->
                                        viewModel.updateUserName(context, newName)
                                    },
                                    onUpdatePreferredCurrency = { newCurrency ->
                                        viewModel.updatePreferredCurrency(newCurrency)
                                    },
                                    onAddAccountClick = { showAddAccountDialog = true },
                                    onScanSmsClick = triggerSmsSync,
                                    onExportCsvClick = { showExportCsvDialog = true },
                                    onSendTestFcmAlertClick = {
                                        viewModel.sendTestFcmPushAlert(context)
                                    },
                                    onRunStabilityCheckClick = {
                                        viewModel.runCrashlyticsStabilityCheck(context)
                                    },
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
                }

                val bannerMsg = uiState.errorMessage ?: uiState.statusBannerMessage
                val isBannerErr = uiState.errorMessage != null

                if (isWideScreen) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        M3DockedNavigationRail(
                            tabs = tabs,
                            selectedTab = currentTab,
                            showAddButton = currentTab == OneFamilyNavTab.HOME,
                            onSelectTab = navigateToTab,
                            onAddClick = {
                                performHaptic()
                                showAddTransactionDialog = true
                            }
                        )
                        Scaffold(
                            modifier = Modifier.weight(1f),
                            contentWindowInsets = WindowInsets.safeDrawing,
                            containerColor = Color.Transparent,
                            topBar = topDateSelector,
                            bottomBar = {
                                ContextualFloatingStatusPill(
                                    message = bannerMsg,
                                    isError = isBannerErr,
                                    canUndo = uiState.canUndoLastAction && !isBannerErr,
                                    onUndo = { viewModel.undoLastSwipeAction() },
                                    onDismiss = { viewModel.dismissBanner() },
                                    modifier = Modifier.navigationBarsPadding()
                                )
                            }
                        ) { innerPadding ->
                            pagerContent(
                                Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                    }
                } else {
                    Scaffold(
                        contentWindowInsets = WindowInsets.safeDrawing,
                        containerColor = Color.Transparent,
                        topBar = topDateSelector,
                        floatingActionButton = {
                            AnimatedVisibility(
                                visible = currentTab == OneFamilyNavTab.HOME,
                                enter = scaleIn(
                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                                ) + fadeIn(),
                                exit = scaleOut(
                                    spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                                ) + fadeOut()
                            ) {
                                ExtendedFloatingActionButton(
                                    onClick = {
                                        performHaptic()
                                        showAddTransactionDialog = true
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "Add Receipt"
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = "Add Receipt",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.testTag("fab_add_expense")
                                )
                            }
                        },
                        bottomBar = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ContextualFloatingStatusPill(
                                    message = bannerMsg,
                                    isError = isBannerErr,
                                    canUndo = uiState.canUndoLastAction && !isBannerErr,
                                    onUndo = { viewModel.undoLastSwipeAction() },
                                    onDismiss = { viewModel.dismissBanner() }
                                )
                                M3DockedNavigationBar(
                                    tabs = tabs,
                                    selectedTab = currentTab,
                                    onSelectTab = navigateToTab
                                )
                            }
                        }
                    ) { innerPadding ->
                        pagerContent(
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    val activeHasHousehold = uiState.householdFeatureEnabled && uiState.household != null

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            hasHousehold = activeHasHousehold,
            defaultShared = activeHasHousehold && uiState.selectedScope != BudgetScope.PERSONAL_ONLY,
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
            hasHousehold = activeHasHousehold,
            availableCategories = uiState.availableCategories,
            currencySymbol = currency,
            onDismiss = { editingTransaction = null },
            onDelete = {
                viewModel.deleteTransaction(txToEdit)
                editingTransaction = null
            },
            onLinkDuplicate = {
                editingTransaction = null
                linkingDuplicateTransaction = txToEdit
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

    if (linkingDuplicateTransaction != null) {
        val sourceTx = linkingDuplicateTransaction!!
        LinkDuplicateTransactionDialog(
            sourceTransaction = sourceTx,
            allTransactions = uiState.allScopedExpenses,
            currencySymbol = currency,
            onDismiss = { linkingDuplicateTransaction = null },
            onConfirmLink = { primaryTx, duplicateTx ->
                viewModel.linkDuplicateTransactions(primaryTx, duplicateTx)
                linkingDuplicateTransaction = null
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

    if (editingCategoryBudget.first) {
        AddCategoryBudgetDialog(
            initialProgress = editingCategoryBudget.second,
            hasHousehold = activeHasHousehold,
            defaultShared = activeHasHousehold && uiState.selectedScope != BudgetScope.PERSONAL_ONLY,
            currencySymbol = currency,
            onDismiss = { editingCategoryBudget = false to null },
            onSave = { category, limit, alertPct, isShared ->
                viewModel.saveCategoryBudget(category, limit, alertPct, isShared)
                editingCategoryBudget = false to null
            }
        )
    }

    if (showAddCustomCategoryDialog) {
        AddCustomCategoryDialog(
            hasHousehold = activeHasHousehold,
            defaultShared = activeHasHousehold && uiState.selectedScope != BudgetScope.PERSONAL_ONLY,
            currencySymbol = currency,
            onDismiss = { showAddCustomCategoryDialog = false },
            onSave = { categoryName, optionalLimit, alertPct, isShared ->
                viewModel.addCustomCategoryWithOptionalBudget(
                    categoryName = categoryName,
                    optionalMonthlyLimit = optionalLimit ?: 0.0,
                    alertThresholdPercent = alertPct,
                    isShared = isShared,
                    context = context
                )
                showAddCustomCategoryDialog = false
            }
        )
    }

    if (showEditOverallBudgetDialog) {
        EditOverallBudgetDialog(
            personalBudget = uiState.userProfile?.personalMonthlyBudget ?: 0.0,
            householdBudget = if (uiState.householdFeatureEnabled) uiState.household?.monthlyBudgetLimit else null,
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

    if (showExportCsvDialog) {
        ExportTransactionsCsvDialog(
            uiState = uiState,
            onDismiss = { showExportCsvDialog = false },
            onSaveCsvToFile = { csvContent, suggestedFileName, count, periodLabel ->
                pendingCsvExport = Triple(csvContent, count, periodLabel)
                showExportCsvDialog = false
                runCatching {
                    createCsvDocumentLauncher.launch(suggestedFileName)
                }.onFailure { err ->
                    pendingCsvExport = null
                    viewModel.notifyCsvExportError(err.localizedMessage ?: "Unable to open file picker.")
                }
            },
            onShareCsv = { csvContent, suggestedFileName, count, periodLabel ->
                showExportCsvDialog = false
                runCatching {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_SUBJECT, suggestedFileName)
                        putExtra(Intent.EXTRA_TEXT, csvContent)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export CSV ($periodLabel)"))
                    viewModel.notifyCsvExportResult(count, periodLabel)
                }.onFailure { err ->
                    viewModel.notifyCsvExportError(err.localizedMessage ?: "Unable to share CSV.")
                }
            }
        )
    }
}

/**
 * Floating contextual Undo / Status pill hovering right above the docked M3 NavigationBar
 * (inspired by Screenshot 2 & Screenshot 3's floating mini contextual bar).
 */
@Composable
private fun ContextualFloatingStatusPill(
    message: String?,
    isError: Boolean,
    canUndo: Boolean,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
        ) { it / 2 } + fadeIn(),
        exit = slideOutVertically(
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
        ) { it / 2 } + fadeOut()
    ) {
        if (message != null) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding, vertical = 6.dp)
                    .testTag("status_banner_surface"),
                shape = RoundedCornerShape(22.dp),
                color = if (isError) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                },
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isError) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        },
                        modifier = Modifier.weight(1f)
                    )
                    if (canUndo) {
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(
                            onClick = {
                                performHaptic()
                                onUndo()
                            },
                            modifier = Modifier.testTag("undo_swipe_action_button")
                        ) {
                            Text(
                                text = "UNDO",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            performHaptic()
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("dismiss_banner_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Dismiss",
                            tint = if (isError) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Rounded 28dp top-corner docked M3 NavigationBar with stadium active indicator pills,
 * Filled selected icons, Outlined unselected icons, and crisp labels.
 */
@Composable
private fun M3DockedNavigationBar(
    tabs: List<OneFamilyNavTab>,
    selectedTab: OneFamilyNavTab,
    onSelectTab: (OneFamilyNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("floating_bottom_nav_bar"),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelectTab(tab) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                )
            }
        }
    }
}

/**
 * Adaptive Material 3 NavigationRail for tablets and unfolded foldables (>= 600.dp).
 */
@Composable
private fun M3DockedNavigationRail(
    tabs: List<OneFamilyNavTab>,
    selectedTab: OneFamilyNavTab,
    showAddButton: Boolean,
    onSelectTab: (OneFamilyNavTab) -> Unit,
    onAddClick: () -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .testTag("floating_bottom_nav_bar"),
        header = {
            AnimatedVisibility(visible = showAddButton) {
                FloatingActionButton(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .testTag("fab_add_expense")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Receipt"
                    )
                }
            }
        }
    ) {
        Spacer(modifier = Modifier.weight(1f))
        tabs.forEach { tab ->
            val isSelected = selectedTab == tab
            NavigationRailItem(
                selected = isSelected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}
