package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.text.font.FontFamily
import androidx.test.core.app.ApplicationProvider
import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import com.example.data.export.CsvTransactionExporter
import com.example.data.local.LocalAccountDataStore
import com.example.data.local.LocalAccountSessionManager
import com.example.data.model.CrashlyticsSettings
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.data.model.ExpertFeatureFlagsSettings
import com.example.data.model.FamilyCurrencyOption
import com.example.data.model.FcmPushSettings
import com.example.data.model.Household
import com.example.data.model.UserProfile
import com.example.data.model.WallpaperColorAccent
import com.example.data.model.WallpaperDensity
import com.example.data.model.WallpaperPatternStyle
import com.example.data.model.WallpaperSettings
import com.example.data.repository.OneFamilyRepository
import com.example.data.sms.RawSmsRow
import com.example.data.sms.SmsInboxReader
import com.example.data.sms.TransactionNotificationHelper
import com.example.ui.components.MonthlyBarGraph
import com.example.ui.components.categoryIcon
import com.example.ui.components.formatCompactCurrency
import com.example.ui.components.formatCurrency
import com.example.ui.screens.ExportTransactionsCsvDialog
import com.example.ui.screens.FirstTimeSmsSyncScreen
import com.example.ui.screens.HomeTabScreen
import com.example.ui.screens.HouseholdTabScreen
import com.example.ui.screens.mergeSelectedDatePreservingTime
import com.example.ui.screens.mergeSelectedTimePreservingDate
import com.example.ui.screens.sanitizeDecimalInput
import com.example.ui.screens.validateCategoryName
import com.example.ui.screens.validateCurrencySymbol
import com.example.ui.screens.validateDisplayName
import com.example.ui.screens.validateEmailAddress
import com.example.ui.screens.validateHouseholdJoinCode
import com.example.ui.screens.validateNonNegativeAmount
import com.example.ui.screens.validatePositiveAmount
import com.example.ui.theme.AppPrimaryColorOption
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.KiwiPrimaryDark
import com.example.ui.theme.KiwiPrimaryLight
import com.example.ui.theme.OneFamilyTheme
import com.example.ui.theme.buildResponsiveTypography
import com.example.ui.theme.resolveAppColorScheme
import com.example.ui.viewmodel.DateRangeFilterUtils
import com.example.ui.viewmodel.DateRangePreset
import com.example.ui.viewmodel.MonthlySpendBar
import com.example.ui.viewmodel.OneFamilyDashboardUiState
import com.example.ui.viewmodel.OneFamilyViewModel
import com.example.ui.viewmodel.SmsReadPreviewItem
import com.example.ui.viewmodel.SmsSyncProgressState
import com.example.ui.viewmodel.YearMonthPeriod
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import android.os.Looper
import java.util.Calendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("One Family", appName)
    }

    @Test
    fun `test user login, exact timestamp persistence, and full transaction editing`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val loginResult = LocalAccountSessionManager.signInWithUsernamePassword(
            context = context,
            rawUsername = "test",
            rawPassword = "pass"
        )
        assertTrue(loginResult.isSuccess)
        val user = loginResult.getOrNull()
        assertNotNull(user)
        assertEquals("test", user?.username)

        val repo = OneFamilyRepository(context)
        val profile = repo.ensureUserProfile(user!!.displayName, user.email).getOrNull()
        assertNotNull(profile)
        val household = repo.getHouseholdOnce(profile!!.activeHouseholdId)
        assertNotNull(household)

        // Verify unassigned budgets default to 0.0 (not hardcoded numbers)
        assertEquals(0.0, household!!.monthlyBudgetLimit, 0.001)
        val sharedBudgets = repo.observeSharedCategoryBudgets(household.id, user.uid).first()
        assertTrue(sharedBudgets.isEmpty())

        // Create an exact timestamp with specific hours, minutes, and seconds
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 15, 14, 37, 42)
            set(Calendar.MILLISECOND, 123)
        }
        val exactTimestamp = cal.timeInMillis

        val txId = repo.addExpense(
            userName = user.displayName,
            household = household,
            title = "Morning Coffee",
            amount = 180.0,
            type = "EXPENSE",
            category = "Food & Dining",
            accountName = "Cash",
            paymentMode = "UPI",
            tags = listOf("coffee"),
            notes = "Test receipt",
            isShared = true,
            transactionDateMillis = exactTimestamp
        ).getOrNull()
        assertNotNull(txId)

        val sharedExpenses = repo.observeSharedExpenses(household.id, user.uid).first()
        val savedTx = sharedExpenses.firstOrNull { it.id == txId }
        assertNotNull(savedTx)
        assertEquals("Morning Coffee", savedTx!!.title)
        assertEquals(180.0, savedTx.amount, 0.001)
        // Verify exact timestamp (including time of day) is stored and retrieved
        assertEquals(exactTimestamp, savedTx.transactionDateMillis)

        // Verify full transaction editing updates title, amount, category, and updated timestamp
        val updatedTime = mergeSelectedTimePreservingDate(
            hourOfDay = 19,
            minute = 45,
            currentTimestampMillis = exactTimestamp
        )
        val updateResult = repo.updateExpense(
            existing = savedTx,
            household = household,
            title = "Evening Dinner",
            amount = 540.0,
            type = savedTx.type,
            category = "Groceries",
            accountName = savedTx.accountName,
            paymentMode = savedTx.paymentMode,
            tags = savedTx.tags,
            notes = savedTx.notes,
            transactionDateMillis = updatedTime,
            isShared = true
        )
        assertTrue(updateResult.isSuccess)

        val afterUpdate = repo.observeSharedExpenses(household.id, user.uid).first()
            .firstOrNull { it.id == txId }
        assertNotNull(afterUpdate)
        assertEquals("Evening Dinner", afterUpdate!!.title)
        assertEquals(540.0, afterUpdate.amount, 0.001)
        assertEquals("Groceries", afterUpdate.category)
        assertEquals(updatedTime, afterUpdate.transactionDateMillis)

        val checkCal = Calendar.getInstance().apply { timeInMillis = afterUpdate.transactionDateMillis }
        assertEquals(19, checkCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(45, checkCal.get(Calendar.MINUTE))
        assertEquals(15, checkCal.get(Calendar.DAY_OF_MONTH))

        LocalAccountSessionManager.signOut(context)
    }

    @Test
    fun `test date and time picker helpers preserve time and date components`() {
        val initialCal = Calendar.getInstance().apply {
            set(2026, Calendar.MAY, 10, 9, 25, 33)
        }
        val initialMillis = initialCal.timeInMillis

        // Pick a new UTC date (e.g. May 22, 2026) while preserving 09:25:33
        val utcPickedCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.MAY, 22, 0, 0, 0)
        }
        val mergedDateMillis = mergeSelectedDatePreservingTime(
            utcDateMillis = utcPickedCal.timeInMillis,
            currentTimestampMillis = initialMillis
        )
        val mergedCal = Calendar.getInstance().apply { timeInMillis = mergedDateMillis }

        assertEquals(2026, mergedCal.get(Calendar.YEAR))
        assertEquals(Calendar.MAY, mergedCal.get(Calendar.MONTH))
        assertEquals(22, mergedCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, mergedCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(25, mergedCal.get(Calendar.MINUTE))
        assertEquals(33, mergedCal.get(Calendar.SECOND))
    }

    @Test
    fun `test SMS parsing stores actual timestamp and caches raw SMS for viewing during edit`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalAccountSessionManager.signInWithUsernamePassword(context, "test", "pass")
        val repo = OneFamilyRepository(context)
        val user = LocalAccountSessionManager.getCurrentUser(context)!!
        val profile = repo.ensureUserProfile(user.displayName, user.email).getOrNull()!!
        val household = repo.getHouseholdOnce(profile.activeHouseholdId)

        val smsTimestamp = 1762954235000L // Exact epoch timestamp with seconds
        val rawBody = "Rs. 649.50 debited from A/c XX4321 on 12-11-26 at Swiggy via UPI Ref 987654321."
        val smsReader = SmsInboxReader(context)
        val candidate = smsReader.parseRawSms(
            sender = "VM-HDFCBK",
            body = rawBody,
            timestamp = smsTimestamp
        )
        assertNotNull(candidate)
        assertEquals(649.50, candidate!!.parsed.amount.toDouble(), 0.01)
        assertEquals(smsTimestamp, candidate.parsed.timestamp)

        // Import the SMS candidate and verify the raw SMS body is accessible when editing the transaction
        val importedCount = repo.batchImportSmsCandidates(
            candidates = listOf(candidate),
            userName = user.displayName,
            household = household,
            shareByDefault = true
        ).getOrNull()
        assertEquals(1, importedCount)

        val allShared = repo.observeSharedExpenses(household!!.id, user.uid).first()
        val importedTx = allShared.firstOrNull { it.smsSignature == candidate.id }
        assertNotNull(importedTx)
        assertEquals(smsTimestamp, importedTx!!.transactionDateMillis)

        // Retrieve raw SMS for EditTransactionDialog
        val smsRecord = LocalAccountDataStore.getSmsMessageForTransaction(importedTx)
        assertNotNull(smsRecord)
        assertTrue(smsRecord!!.body.contains("Rs. 649.50 debited from A/c XX4321"))
        assertEquals("VM-HDFCBK", smsRecord.sender)

        LocalAccountSessionManager.signOut(context)
    }

    @Test
    fun `test editing user display name updates session, user profile, and household members`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalAccountSessionManager.signInWithUsernamePassword(context, "test", "pass")
        val repo = OneFamilyRepository(context)
        val initialSession = LocalAccountSessionManager.getCurrentUser(context)!!
        val profile = repo.ensureUserProfile(initialSession.displayName, initialSession.email).getOrNull()!!
        val household = repo.getHouseholdOnce(profile.activeHouseholdId)!!

        // Update user display name
        val updatedSessionResult = LocalAccountSessionManager.updateDisplayName(context, "Aarav Sharma")
        assertNotNull(updatedSessionResult)
        assertEquals("Aarav Sharma", LocalAccountSessionManager.getCurrentUser(context)?.displayName)

        val repoUpdate = repo.updateUserDisplayName("Aarav Sharma", household)
        assertTrue(repoUpdate.isSuccess)

        val updatedProfile = repo.observeUserProfile(initialSession.uid).first()
        assertEquals("Aarav Sharma", updatedProfile?.displayName)

        val updatedHousehold = repo.getHouseholdOnce(household.id)
        assertEquals("Aarav Sharma", updatedHousehold?.memberNames?.get(initialSession.uid))

        // Restore default display name for subsequent tests
        LocalAccountSessionManager.updateDisplayName(context, "Mock User")
        repo.updateUserDisplayName("Mock User", household)
        LocalAccountSessionManager.signOut(context)
    }

    @Test
    fun `test responsive Google default typography and AMOLED theme preferences`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Verify Google default font family (FontFamily.Default) and relative scaling
        val smallScreenTypography = buildResponsiveTypography(0.90f)
        val largeScreenTypography = buildResponsiveTypography(1.15f)
        assertEquals(FontFamily.Default, smallScreenTypography.bodyLarge.fontFamily)
        assertEquals(FontFamily.Default, largeScreenTypography.headlineMedium.fontFamily)
        assertTrue(largeScreenTypography.bodyLarge.fontSize.value > smallScreenTypography.bodyLarge.fontSize.value)

        // Verify Theme mode persistence
        LocalAccountSessionManager.setThemeMode(context, AppThemeMode.DARK)
        assertEquals(AppThemeMode.DARK, LocalAccountSessionManager.themeMode.value)
        LocalAccountSessionManager.setThemeMode(context, AppThemeMode.SYSTEM)
        assertEquals(AppThemeMode.SYSTEM, LocalAccountSessionManager.themeMode.value)
    }

    @Test
    fun `test budget and form input validation helpers`() {
        // Decimal sanitization
        assertEquals("1250.50", sanitizeDecimalInput("₹1,250.50abc"))
        assertEquals("45.12", sanitizeDecimalInput("45.12.99"))

        // Positive & non-negative amount validation
        assertNotNull(validatePositiveAmount(""))
        assertNotNull(validatePositiveAmount("0"))
        assertNotNull(validatePositiveAmount("-50"))
        assertNull(validatePositiveAmount("1500.75"))

        assertNull(validateNonNegativeAmount(""))
        assertNull(validateNonNegativeAmount("0"))
        assertNull(validateNonNegativeAmount("25000"))

        // Category, currency, name, email, and join code validation
        assertNotNull(validateCategoryName("A"))
        assertNull(validateCategoryName("Groceries"))
        assertNotNull(validateCurrencySymbol(""))
        assertNull(validateCurrencySymbol("₹"))
        assertNotNull(validateDisplayName("X"))
        assertNull(validateDisplayName("Priya Patel"))
        assertNotNull(validateEmailAddress("not-an-email"))
        assertNull(validateEmailAddress("member@family.org"))
        assertNotNull(validateHouseholdJoinCode("ABC"))
        assertNull(validateHouseholdJoinCode("FAM123"))
    }

    @Test
    fun `test setting and clearing category-based budgets and first-time SMS sync state`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalAccountSessionManager.signInWithUsernamePassword(context, "test", "pass")
        val user = LocalAccountSessionManager.getCurrentUser(context)!!
        val repo = OneFamilyRepository(context)
        val profile = repo.ensureUserProfile(user.displayName, user.email).getOrNull()!!
        val household = repo.getHouseholdOnce(profile.activeHouseholdId)!!

        // Verify first-time SMS sync flag persistence
        LocalAccountSessionManager.setInitialSmsSyncCompleted(context, user.uid, false)
        assertFalse(LocalAccountSessionManager.hasCompletedInitialSmsSync(context, user.uid))
        LocalAccountSessionManager.setInitialSmsSyncCompleted(context, user.uid, true)
        assertTrue(LocalAccountSessionManager.hasCompletedInitialSmsSync(context, user.uid))

        // Set a category budget for Groceries and Food & Dining
        val saveGroceries = repo.saveCategoryBudget(
            household = household,
            category = "Groceries",
            monthlyLimit = 12000.0,
            alertThresholdPercent = 80,
            isShared = true
        )
        assertTrue(saveGroceries.isSuccess)

        val saveDining = repo.saveCategoryBudget(
            household = household,
            category = "Food & Dining",
            monthlyLimit = 5000.0,
            alertThresholdPercent = 75,
            isShared = true
        )
        assertTrue(saveDining.isSuccess)

        val budgets = repo.observeSharedCategoryBudgets(household.id, user.uid).first()
        val groceriesBudget = budgets.firstOrNull { it.category == "Groceries" }
        val diningBudget = budgets.firstOrNull { it.category == "Food & Dining" }
        assertNotNull(groceriesBudget)
        assertEquals(12000.0, groceriesBudget!!.monthlyLimit, 0.01)
        assertNotNull(diningBudget)
        assertEquals(5000.0, diningBudget!!.monthlyLimit, 0.01)

        // Clear limit for Food & Dining by passing 0.0
        repo.saveCategoryBudget(
            household = household,
            category = "Food & Dining",
            monthlyLimit = 0.0,
            alertThresholdPercent = 80,
            isShared = true
        )
        val updatedBudgets = repo.observeSharedCategoryBudgets(household.id, user.uid).first()
        val clearedDining = updatedBudgets.firstOrNull { it.category == "Food & Dining" }
        assertNull(clearedDining)

        // Also clear Groceries so shared state is clean for other tests
        repo.saveCategoryBudget(
            household = household,
            category = "Groceries",
            monthlyLimit = 0.0,
            alertThresholdPercent = 80,
            isShared = true
        )

        // Test incoming SMS notification pipeline deduplication & saving
        val smsReader = SmsInboxReader(context)
        val incomingCandidate = smsReader.parseRawSms(
            sender = "AX-ICICIB",
            body = "Acct XX9012 debited with Rs 420.00 on 15-11-26 at Amazon Pay. UPI:654321987.",
            timestamp = System.currentTimeMillis()
        )
        assertNotNull(incomingCandidate)
        TransactionNotificationHelper.processIncomingSmsCandidateAndNotify(
            context = context,
            candidate = incomingCandidate!!
        )
        val afterFirstIncoming = repo.observeSharedExpenses(household.id, user.uid).first()
            .filter { it.smsSignature == incomingCandidate.id }
        assertEquals(1, afterFirstIncoming.size)

        // Second time with the exact same SMS should be deduplicated
        TransactionNotificationHelper.processIncomingSmsCandidateAndNotify(
            context = context,
            candidate = incomingCandidate
        )
        val afterDuplicateIncoming = repo.observeSharedExpenses(household.id, user.uid).first()
            .filter { it.smsSignature == incomingCandidate.id }
        assertEquals(1, afterDuplicateIncoming.size)

        LocalAccountSessionManager.signOut(context)
    }

    @Test
    fun `test test user has full mock data while real user has zero mock data and strict isolation`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Verify Test User has full mock data (sample transactions, accounts, raw SMS) for demoing
        val testLogin = LocalAccountSessionManager.signInWithUsernamePassword(
            context = context,
            rawUsername = "test",
            rawPassword = "pass"
        )
        assertTrue(testLogin.isSuccess)
        val testUser = testLogin.getOrNull()!!
        assertEquals("Mock User", testUser.displayName)

        val repo = OneFamilyRepository(context)
        val testProfile = repo.ensureUserProfile(testUser.displayName, testUser.email).getOrNull()!!
        val testHousehold = repo.getHouseholdOnce(testProfile.activeHouseholdId)!!

        // Verify accounts were seeded for Test User
        val testAccounts = repo.observeAccounts(testUser.uid).first()
        assertTrue(testAccounts.size >= 3)
        assertTrue(testAccounts.any { it.name.contains("HDFC") })
        assertTrue(testAccounts.any { it.name.contains("Axis") })

        // Verify realistic mock transactions across past months were seeded for Test User
        val testSharedExpenses = repo.observeSharedExpenses(testHousehold.id, testUser.uid).first()
        val mockTxs = testSharedExpenses.filter { it.userId == testUser.uid }
        assertTrue("Test user should have sample mock transactions for demoing", mockTxs.size >= 10)
        assertTrue(mockTxs.any { it.title.contains("Salary") || it.type == "INCOME" })
        assertTrue(mockTxs.any { it.title.contains("Rent") || it.title.contains("Groceries") })

        // Verify each mock transaction has its raw SMS body cached
        val sampleMockTx = mockTxs.first()
        val mockSms = LocalAccountDataStore.getSmsMessageForTransaction(sampleMockTx)
        assertNotNull(mockSms)
        assertTrue(mockSms!!.body.isNotBlank())

        LocalAccountSessionManager.signOut(context)

        // 2. Verify Real User has strictly ZERO mock data and complete isolation from Test User
        val realRegister = LocalAccountSessionManager.registerUsernamePassword(
            context = context,
            rawUsername = "alex",
            rawPassword = "password123",
            rawDisplayName = "Alex Mercer"
        )
        assertTrue(realRegister.isSuccess)
        val realUser = realRegister.getOrNull()!!

        val realProfile = repo.ensureUserProfile(realUser.displayName, realUser.email).getOrNull()!!
        // Real user starts without an active household until created/joined
        assertEquals("", realProfile.activeHouseholdId)

        // Verify Real User has zero mock transactions and zero accounts inherited from Test User
        val realPersonalExpenses = repo.observePersonalExpenses(realUser.uid).first()
        assertTrue("Real user must have zero mock personal expenses", realPersonalExpenses.isEmpty())

        val realAccounts = repo.observeAccounts(realUser.uid).first()
        assertTrue("Real user must have zero accounts inherited from test user", realAccounts.isEmpty())

        // Real user adds a manual transaction
        val manualTxId = repo.addExpense(
            userName = realUser.displayName,
            household = null,
            title = "Real User Dinner",
            amount = 850.0,
            type = "EXPENSE",
            category = "Food & Dining",
            accountName = "Cash",
            paymentMode = "CASH",
            tags = listOf("real"),
            notes = "Alex's cash receipt",
            isShared = false
        ).getOrThrow()

        val afterManual = repo.observePersonalExpenses(realUser.uid).first()
        assertEquals(1, afterManual.size)
        assertEquals("Real User Dinner", afterManual.first().title)
        assertEquals(realUser.uid, afterManual.first().userId)

        LocalAccountSessionManager.signOut(context)
    }

    @Test
    fun `test linking duplicate SMS transactions merges signatures, deletes duplicate, and prevents re-import`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalAccountSessionManager.signInWithUsernamePassword(context, "test", "pass")
        val user = LocalAccountSessionManager.getCurrentUser(context)!!
        val repo = OneFamilyRepository(context)
        val profile = repo.ensureUserProfile(user.displayName, user.email).getOrNull()!!
        val household = repo.getHouseholdOnce(profile.activeHouseholdId)!!

        val smsReader = SmsInboxReader(context)
        val ts = System.currentTimeMillis()

        // Primary bank SMS alert
        val bankSms = smsReader.parseRawSms(
            sender = "VM-HDFCBK",
            body = "Rs. 1,850.00 debited from A/c XX4821 on 18-11-26 to VPA bluebottle@okaxis (UPI Ref 8811223344).",
            timestamp = ts
        )!!
        // Duplicate UPI app SMS alert for the exact same purchase
        val upiDuplicateSms = smsReader.parseRawSms(
            sender = "AX-GPAY",
            body = "You paid Rs. 1,850.00 to Blue Bottle Cafe via UPI Ref 8811223344 from HDFC Bank XX4821.",
            timestamp = ts + 25_000L
        )!!

        repo.batchImportSmsCandidates(
            candidates = listOf(bankSms, upiDuplicateSms),
            userName = user.displayName,
            household = household,
            shareByDefault = true
        )

        val beforeLink = repo.observeSharedExpenses(household.id, user.uid).first()
        val primaryTx = beforeLink.firstOrNull { it.smsSignature == bankSms.id }
        val duplicateTx = beforeLink.firstOrNull { it.smsSignature == upiDuplicateSms.id }
        assertNotNull(primaryTx)
        assertNotNull(duplicateTx)

        // Link duplicateTx into primaryTx
        val linkResult = repo.linkDuplicateTransaction(primaryTx!!, duplicateTx!!)
        assertTrue(linkResult.isSuccess)
        val mergedTx = linkResult.getOrNull()!!
        assertTrue(mergedTx.linkedSmsSignatures.contains(upiDuplicateSms.id))

        // Verify duplicate transaction row was removed from the ledger while primary remains
        val afterLink = repo.observeSharedExpenses(household.id, user.uid).first()
        assertNull(afterLink.firstOrNull { it.id == duplicateTx.id })
        val persistedPrimary = afterLink.firstOrNull { it.id == primaryTx.id }
        assertNotNull(persistedPrimary)
        assertTrue(persistedPrimary!!.linkedSmsSignatures.contains(upiDuplicateSms.id))

        // Verify both raw SMS alerts are returned when viewing/editing the linked transaction
        val allLinkedSms = LocalAccountDataStore.getLinkedSmsMessagesForTransaction(persistedPrimary)
        assertEquals(2, allLinkedSms.size)
        assertTrue(allLinkedSms.any { it.sender == "VM-HDFCBK" })
        assertTrue(allLinkedSms.any { it.sender == "AX-GPAY" })

        // Verify re-importing the duplicate SMS candidate is skipped because its signature is linked
        val reimportCount = repo.batchImportSmsCandidates(
            candidates = listOf(upiDuplicateSms),
            userName = user.displayName,
            household = household,
            shareByDefault = true
        ).getOrNull()
        assertEquals(0, reimportCount)

        LocalAccountSessionManager.signOut(context)
    }

    @Test
    fun `test WhatsApp-style finance wallpaper settings, FCM push settings, and Crashlytics stability guardrails`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalAccountSessionManager.ensureInitialized(context)

        // 1. Verify WhatsApp-style finance wallpaper customization & persistence
        val customWallpaper = WallpaperSettings(
            enabled = true,
            style = WallpaperPatternStyle.WHATSAPP_DOODLES,
            colorAccent = WallpaperColorAccent.EMERALD_MINT,
            density = WallpaperDensity.COMPACT,
            opacityPercent = 16
        )
        LocalAccountSessionManager.updateWallpaperSettings(context, customWallpaper)
        val loadedWallpaper = LocalAccountSessionManager.wallpaperSettings.value
        assertTrue(loadedWallpaper.enabled)
        assertEquals(WallpaperPatternStyle.WHATSAPP_DOODLES, loadedWallpaper.style)
        assertEquals(WallpaperColorAccent.EMERALD_MINT, loadedWallpaper.colorAccent)
        assertEquals(WallpaperDensity.COMPACT, loadedWallpaper.density)
        assertEquals(16, loadedWallpaper.opacityPercent)

        // Toggle wallpaper off and back on
        LocalAccountSessionManager.updateWallpaperSettings(context, customWallpaper.copy(enabled = false))
        assertFalse(LocalAccountSessionManager.wallpaperSettings.value.enabled)
        LocalAccountSessionManager.updateWallpaperSettings(context, WallpaperSettings())
        assertTrue(LocalAccountSessionManager.wallpaperSettings.value.enabled)

        // 2. Verify Firebase Cloud Messaging (FCM) push alert settings & dispatch
        val customFcm = FcmPushSettings(
            familyTransactionAlertsEnabled = true,
            budgetThresholdAlertsEnabled = true,
            subscribedTopic = "household_test_family"
        )
        LocalAccountSessionManager.updateFcmPushSettings(context, customFcm)
        assertEquals("household_test_family", LocalAccountSessionManager.fcmPushSettings.value.subscribedTopic)

        // Trigger FCM family transaction & budget threshold push notifications safely
        TransactionNotificationHelper.showFamilyTransactionPushNotification(
            context = context,
            expenseId = "test_fcm_tx_1",
            householdId = "local_household_test",
            householdName = "Family Household",
            memberName = "Priya Sharma",
            title = "Nature's Basket Groceries",
            amount = 2450.0,
            category = "Groceries",
            currencySymbol = "₹"
        )
        TransactionNotificationHelper.showBudgetThresholdPushNotification(
            context = context,
            categoryOrScopeName = "Groceries",
            spentAmount = 9200.0,
            limitAmount = 10000.0,
            thresholdPercent = 80,
            currencySymbol = "₹"
        )

        // 3. Verify Firebase Crashlytics reporting & diagnostic breadcrumbs (bug guards are always-on in code, not toggleable)
        val customCrashlytics = CrashlyticsSettings(
            crashReportingEnabled = true
        )
        LocalAccountSessionManager.updateCrashlyticsSettings(context, customCrashlytics)
        assertTrue(LocalAccountSessionManager.crashlyticsSettings.value.crashReportingEnabled)

        LocalAccountSessionManager.clearStabilityDiagnostics(context)
        assertTrue(LocalAccountSessionManager.stabilityDiagnostics.value.isEmpty())

        LocalAccountSessionManager.recordDiagnostic(
            context = context,
            subsystem = "SMS_AUTO_SYNC",
            severity = "INFO",
            message = "Verified SMS batch guardrails"
        )
        val diagnostics = LocalAccountSessionManager.stabilityDiagnostics.value
        assertEquals(1, diagnostics.size)
        assertEquals("SMS_AUTO_SYNC", diagnostics.first().subsystem)
    }

    @Test
    fun `test family preferred currency selection updates UserProfile, Household, activeCurrencySymbol, and all transaction formatting`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalAccountSessionManager.signInWithUsernamePassword(context, "test", "pass")
        val user = LocalAccountSessionManager.getCurrentUser(context)!!
        val repo = OneFamilyRepository(context)
        val profile = repo.ensureUserProfile(user.displayName, user.email).getOrNull()!!
        val household = repo.getHouseholdOnce(profile.activeHouseholdId)!!

        // Verify all preset FamilyCurrencyOptions have non-blank symbols and codes
        assertTrue(FamilyCurrencyOption.entries.size >= 8)
        assertEquals("$", FamilyCurrencyOption.USD.symbol)
        assertEquals("€", FamilyCurrencyOption.EUR.symbol)
        assertEquals("£", FamilyCurrencyOption.GBP.symbol)
        assertEquals("AED", FamilyCurrencyOption.AED.symbol)

        // Switch preferred currency to USD ($) and verify both UserProfile and Household update
        val usdResult = repo.updatePreferredCurrency("$", household)
        assertTrue(usdResult.isSuccess)
        assertEquals("$", repo.observeUserProfile(user.uid).first()?.currencySymbol)
        assertEquals("$", repo.getHouseholdOnce(household.id)?.currencySymbol)

        // Verify OneFamilyDashboardUiState.activeCurrencySymbol reflects the household's preferred currency
        val uiStateWithUsd = OneFamilyDashboardUiState(
            userProfile = repo.observeUserProfile(user.uid).first(),
            household = repo.getHouseholdOnce(household.id)
        )
        assertEquals("$", uiStateWithUsd.activeCurrencySymbol)
        assertEquals("$2,450", formatCurrency(2450.0, uiStateWithUsd.activeCurrencySymbol))
        assertEquals("$2.5k", formatCompactCurrency(2450.0, uiStateWithUsd.activeCurrencySymbol))

        // Switch preferred currency to EUR (€) and verify formatting
        repo.updatePreferredCurrency("€", household)
        val uiStateWithEur = OneFamilyDashboardUiState(
            userProfile = repo.observeUserProfile(user.uid).first(),
            household = repo.getHouseholdOnce(household.id)
        )
        assertEquals("€", uiStateWithEur.activeCurrencySymbol)
        assertEquals("€1,200.75", formatCurrency(1200.75, uiStateWithEur.activeCurrencySymbol))
        assertEquals("€1,200.5", formatCurrency(1200.50, uiStateWithEur.activeCurrencySymbol))

        // Switch preferred currency to AED and then restore back to ₹
        repo.updatePreferredCurrency("AED", household)
        assertEquals("AED", repo.getHouseholdOnce(household.id)?.currencySymbol)
        assertEquals("AED500", formatCurrency(500.0, "AED"))

        repo.updatePreferredCurrency("₹", household)
        assertEquals("₹", repo.getHouseholdOnce(household.id)?.currencySymbol)
        LocalAccountSessionManager.signOut(context)
    }

    @Test
    fun `test undo features for unwanted swipes on the home screen (swipe-left delete, swipe-right link duplicate, and privacy toggle)`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalAccountSessionManager.signInWithUsernamePassword(context, "test", "pass")
        val user = LocalAccountSessionManager.getCurrentUser(context)!!
        val repo = OneFamilyRepository(context)
        val smsReader = SmsInboxReader(context)
        val profile = repo.ensureUserProfile(user.displayName, user.email).getOrNull()!!
        val household = repo.getHouseholdOnce(profile.activeHouseholdId)!!

        val viewModel = OneFamilyViewModel(
            repository = repo,
            smsInboxReader = smsReader,
            currentUserId = user.uid,
            currentUserName = user.displayName,
            currentUserEmail = user.email
        )
        composeTestRule.waitForIdle()
        shadowOf(Looper.getMainLooper()).idle()

        // 1. Test Unwanted Swipe-Left Delete & Undo Restoration
        val now = System.currentTimeMillis()
        val swipeDeleteTxId = repo.addExpense(
            userName = user.displayName,
            household = household,
            title = "Accidental Swipe Groceries",
            amount = 1499.0,
            type = "EXPENSE",
            category = "Groceries",
            accountName = "HDFC Bank",
            paymentMode = "UPI",
            tags = listOf("swipe-test"),
            notes = "[VM-HDFCBK] Rs. 1499.00 debited from HDFC Bank A/c XX4821",
            isShared = true,
            smsSignature = "sms_sig_undo_delete_1",
            transactionDateMillis = now
        ).getOrNull()!!

        val txBeforeDelete = repo.observeSharedExpenses(household.id, user.uid).first()
            .first { it.id == swipeDeleteTxId }

        // Trigger swipe-left delete via ViewModel
        viewModel.deleteTransaction(txBeforeDelete)
        composeTestRule.waitForIdle()
        shadowOf(Looper.getMainLooper()).idle()

        // Verify transaction was deleted and undo state is active
        assertNull(
            repo.observeSharedExpenses(household.id, user.uid).first()
                .firstOrNull { it.id == swipeDeleteTxId }
        )
        assertNotNull(viewModel.lastUndoableAction.value)
        assertEquals("Deleted 'Accidental Swipe Groceries'.", viewModel.statusBanner.value)
        assertTrue(viewModel.uiState.first { it.canUndoLastAction }.canUndoLastAction)

        // Trigger Undo for the unwanted swipe-left delete
        viewModel.undoLastSwipeAction()
        composeTestRule.waitForIdle()
        shadowOf(Looper.getMainLooper()).idle()

        val restoredAfterDelete = repo.observeSharedExpenses(household.id, user.uid).first()
            .firstOrNull { it.id == swipeDeleteTxId }
        assertNotNull("Deleted transaction should be restored after undoLastSwipeAction()", restoredAfterDelete)
        assertEquals("Accidental Swipe Groceries", restoredAfterDelete!!.title)
        assertEquals(1499.0, restoredAfterDelete.amount, 0.001)
        assertEquals("sms_sig_undo_delete_1", restoredAfterDelete.smsSignature)
        assertNull(viewModel.lastUndoableAction.value)
        assertEquals("Restored 'Accidental Swipe Groceries'.", viewModel.statusBanner.value)

        // 2. Test Unwanted Swipe-Right Link Duplicate & Undo Restoration
        val duplicateTxId = repo.addExpense(
            userName = user.displayName,
            household = household,
            title = "Duplicate UPI Alert Groceries",
            amount = 1499.0,
            type = "EXPENSE",
            category = "Groceries",
            accountName = "GPay",
            paymentMode = "UPI",
            tags = listOf("gpay"),
            notes = "[AX-GPAY] You paid Rs. 1499.00 via Google Pay UPI to Groceries",
            isShared = true,
            smsSignature = "sms_sig_undo_dup_2",
            transactionDateMillis = now + 5_000L
        ).getOrNull()!!

        val dupTxBeforeLink = repo.observeSharedExpenses(household.id, user.uid).first()
            .first { it.id == duplicateTxId }

        viewModel.linkDuplicateTransactions(restoredAfterDelete, dupTxBeforeLink)
        composeTestRule.waitForIdle()
        shadowOf(Looper.getMainLooper()).idle()

        // Verify duplicate was merged & removed, and undo state is active
        assertNull(
            repo.observeSharedExpenses(household.id, user.uid).first()
                .firstOrNull { it.id == duplicateTxId }
        )
        assertNotNull(viewModel.lastUndoableAction.value)

        // Trigger Undo for the unwanted swipe-right link duplicate
        viewModel.undoLastSwipeAction()
        composeTestRule.waitForIdle()
        shadowOf(Looper.getMainLooper()).idle()

        val expensesAfterUnlink = repo.observeSharedExpenses(household.id, user.uid).first()
        val restoredPrimary = expensesAfterUnlink.firstOrNull { it.id == swipeDeleteTxId }
        val restoredDuplicate = expensesAfterUnlink.firstOrNull { it.id == duplicateTxId }
        assertNotNull("Primary transaction should be restored after undoing duplicate link", restoredPrimary)
        assertNotNull("Duplicate transaction should be restored after undoing duplicate link", restoredDuplicate)
        assertTrue(restoredPrimary!!.linkedSmsSignatures.isEmpty())
        assertEquals("Duplicate UPI Alert Groceries", restoredDuplicate!!.title)
        assertNull(viewModel.lastUndoableAction.value)

        // 3. Test Unwanted Privacy Scope Toggle & Undo Restoration
        viewModel.toggleTransactionPrivacy(restoredPrimary)
        composeTestRule.waitForIdle()
        shadowOf(Looper.getMainLooper()).idle()
        assertNotNull(viewModel.lastUndoableAction.value)
        assertNull(
            repo.observeSharedExpenses(household.id, user.uid).first()
                .firstOrNull { it.id == swipeDeleteTxId }
        )

        viewModel.undoLastSwipeAction()
        composeTestRule.waitForIdle()
        shadowOf(Looper.getMainLooper()).idle()
        val restoredSharedScope = repo.observeSharedExpenses(household.id, user.uid).first()
            .firstOrNull { it.id == swipeDeleteTxId }
        assertNotNull("Transaction should be restored back to SHARED scope after undoing privacy toggle", restoredSharedScope)
        assertEquals("SHARED", restoredSharedScope!!.visibility)

        // Clean up test transactions
        repo.deleteExpense(restoredSharedScope)
        repo.deleteExpense(restoredDuplicate)
        LocalAccountSessionManager.signOut(context)
    }

    @Test
    fun `test HomeTabScreen UI swipe undo bar and preferred currency display`() {
        val sampleTx = ExpenseTransaction(
            id = "ui_test_tx_1",
            userId = "local_user_test",
            userName = "Mock User",
            householdId = "local_household_test",
            visibility = "SHARED",
            title = "Whole Foods Market",
            amount = 325.0,
            type = "EXPENSE",
            category = "Groceries",
            accountName = "Chase Card",
            paymentMode = "CARD",
            transactionDateMillis = System.currentTimeMillis()
        )

        var currentUiState by mutableStateOf(
            OneFamilyDashboardUiState(
                isLoading = false,
                userProfile = UserProfile(uid = "local_user_test", displayName = "Mock User", currencySymbol = "$"),
                household = Household(id = "local_household_test", name = "Family Home", currencySymbol = "$"),
                activeExpenses = listOf(sampleTx),
                allScopedExpenses = listOf(sampleTx),
                filteredExpenses = listOf(sampleTx),
                totalMonthlySpent = 325.0,
                activeMonthlyBudget = 2000.0
            )
        )

        composeTestRule.setContent {
            OneFamilyTheme {
                HomeTabScreen(
                    uiState = currentUiState,
                    onScopeSelected = {},
                    onDeleteTransaction = { deleted ->
                        currentUiState = currentUiState.copy(
                            filteredExpenses = currentUiState.filteredExpenses.filterNot { it.id == deleted.id },
                            statusBannerMessage = "Deleted '${deleted.title}'.",
                            canUndoLastAction = true
                        )
                    },
                    onUndoLastSwipeAction = {
                        currentUiState = currentUiState.copy(
                            filteredExpenses = listOf(sampleTx),
                            statusBannerMessage = "Restored '${sampleTx.title}'.",
                            canUndoLastAction = false
                        )
                    }
                )
            }
        }

        // Verify transaction displays with the selected family currency ($)
        composeTestRule.onNodeWithText("Whole Foods Market").assertIsDisplayed()
        composeTestRule.onNodeWithText("-$325").assertIsDisplayed()

        // Simulate an unwanted swipe-left delete on the Home screen
        currentUiState = currentUiState.copy(
            filteredExpenses = emptyList(),
            statusBannerMessage = "Deleted 'Whole Foods Market'.",
            canUndoLastAction = true
        )
        composeTestRule.waitForIdle()

        // Verify the Home Undo Swipe bar and button appear
        composeTestRule.onNodeWithTag("home_undo_swipe_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_undo_swipe_button").assertIsDisplayed()

        // Tap Undo Swipe button and verify the transaction is restored on the Home screen
        composeTestRule.onNodeWithTag("home_undo_swipe_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Whole Foods Market").assertIsDisplayed()
        composeTestRule.onNodeWithText("-$325").assertIsDisplayed()

        // Change family preferred currency to € and verify the Home screen updates immediately
        currentUiState = currentUiState.copy(
            userProfile = currentUiState.userProfile?.copy(currencySymbol = "€"),
            household = currentUiState.household?.copy(currencySymbol = "€")
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("-€325").assertIsDisplayed()
    }

    @Test
    fun `test parallel SMS reading and transaction adding with 6 reader threads and 6 writer threads in batches without race conditions`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalAccountSessionManager.signInWithUsernamePassword(context, "test", "pass")
        val user = LocalAccountSessionManager.getCurrentUser(context)!!
        val repo = OneFamilyRepository(context)
        val smsReader = SmsInboxReader(context)
        val profile = repo.ensureUserProfile(user.displayName, user.email).getOrNull()!!
        val household = repo.getHouseholdOnce(profile.activeHouseholdId)!!

        val viewModel = OneFamilyViewModel(
            repository = repo,
            smsInboxReader = smsReader,
            currentUserId = user.uid,
            currentUserName = user.displayName,
            currentUserEmail = user.email
        )
        composeTestRule.waitForIdle()
        shadowOf(Looper.getMainLooper()).idle()

        // Align baseTime to the start of a 5-minute bucket (+10s) so +1.5s never crosses a 300_000ms bucket boundary
        val baseTime = ((System.currentTimeMillis() - 7_200_000L) / 300_000L) * 300_000L + 10_000L
        // Create 40 unique SMS alerts + 10 cross-batch duplicate SMS alerts (same SMS body + 1.5s offset)
        // and include out-of-order timestamps with Avl Bal to verify newest balance wins under parallel writes
        val uniqueRows = (1..40).map { idx ->
            val ts = baseTime + (idx * 300_000L)
            val avlBal = 50_000.0 - (idx * 100.0) // newest (idx=40) has Avl Bal Rs. 46000.0
            RawSmsRow(
                sender = "VM-HDFCBK",
                body = "Rs. ${200 + idx}.00 debited from HDFC Bank A/c XX9182 on 18-11-26 to VPA merchant$idx@okaxis (UPI Ref 99001122$idx). Avl Bal: Rs. $avlBal",
                timestamp = ts
            )
        }
        // Add 10 cross-batch duplicate SMS messages (within 1.5 seconds of first 10 unique rows)
        val duplicateRows = (1..10).map { idx ->
            val ts = baseTime + (idx * 300_000L) + 1_500L
            val avlBal = 50_000.0 - (idx * 100.0)
            RawSmsRow(
                sender = "AD-HDFCBK",
                body = "Rs. ${200 + idx}.00 debited from HDFC Bank A/c XX9182 on 18-11-26 to VPA merchant$idx@okaxis (UPI Ref 99001122$idx). Avl Bal: Rs. $avlBal",
                timestamp = ts
            )
        }
        // Shuffle rows so older and newer timestamps are distributed across different parallel batches
        val allRawRows = (uniqueRows + duplicateRows).shuffled()

        val (parsedCandidates, savedCount) = viewModel.runParallelSmsBatchPipeline(
            rawRowsOverride = allRawRows,
            existingSignatures = emptySet(),
            userName = user.displayName,
            household = household,
            readerThreads = 6,
            writerThreads = 6,
            batchSize = 8,
            perBatchDelayMs = 0L
        )
        val finalSyncState = viewModel.smsSyncProgress.value

        // Verify at least 5 threads each were used for parallel reading & parallel writing
        assertTrue(finalSyncState.readerThreadCount >= 5)
        assertTrue(finalSyncState.writerThreadCount >= 5)
        assertEquals(6, finalSyncState.readerThreadCount)
        assertEquals(6, finalSyncState.writerThreadCount)
        assertEquals(50, finalSyncState.totalSmsCount)
        assertEquals(50, finalSyncState.readSmsCount)
        assertEquals(0, finalSyncState.remainingSmsCount)
        // Exactly 40 unique transactions should be matched and saved (0 duplicates from race conditions)
        assertEquals(40, parsedCandidates.size)
        assertEquals(40, savedCount)
        assertEquals(40, finalSyncState.matchedTransactionsCount)
        assertEquals(40, finalSyncState.savedTransactionsCount)
        assertTrue(finalSyncState.recentReadTransactions.isNotEmpty())
        assertTrue(finalSyncState.currentReadingTransactionTitle.isNotBlank())

        // Verify all 40 transactions exist in repository with zero duplicates
        val allShared = repo.observeSharedExpenses(household.id, user.uid).first()
        val parallelImported = allShared.filter { it.accountName.contains("9182") }
        assertEquals(40, parallelImported.size)

        // Verify monotonic timestamp guard preserved the newest account balance (46,000.0) despite shuffled parallel batches
        val accounts = repo.observeAccounts(user.uid).first()
        val hdfc9182 = accounts.firstOrNull { it.name.contains("9182") }
        assertNotNull(hdfc9182)
        assertEquals(46_000.0, hdfc9182!!.balance, 0.01)

        LocalAccountSessionManager.signOut(context)
    }

    @Test
    fun `test FirstTimeSmsSyncScreen shows animation ONLY while actively scanning and hides thread counts`() {
        var currentSyncState by mutableStateOf(
            SmsSyncProgressState(
                isSyncing = false,
                isCompleted = false
            )
        )

        composeTestRule.setContent {
            OneFamilyTheme {
                FirstTimeSmsSyncScreen(
                    syncState = currentSyncState,
                    onStartSyncClick = {},
                    onContinueToAppClick = {},
                    onSkipClick = {},
                    currencySymbol = "₹"
                )
            }
        }
        composeTestRule.waitForIdle()

        // 1. Before scanning starts: animation MUST NOT be shown, Start button is shown, and no thread counts appear
        composeTestRule.onNodeWithTag("first_time_sms_sync_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("start_first_time_sms_sync_button").assertIsDisplayed()
        assertTrue(
            "Expected scanning animation to be hidden before scanning starts",
            composeTestRule.onAllNodesWithTag("sms_transaction_reading_animation").fetchSemanticsNodes().isEmpty()
        )
        assertTrue(composeTestRule.onAllNodesWithText("6R × 6W", substring = true).fetchSemanticsNodes().isEmpty())

        // 2. During active scanning (`isSyncing = true`): animation IS displayed below header badge
        currentSyncState = SmsSyncProgressState(
            isSyncing = true,
            isCompleted = false,
            totalSmsCount = 120,
            readSmsCount = 72,
            matchedTransactionsCount = 24,
            savedTransactionsCount = 20,
            readerThreadCount = 6,
            writerThreadCount = 6,
            processedBatchesCount = 6,
            totalBatchesCount = 10,
            currentReadingTransactionTitle = "Blue Tokai Coffee",
            currentReadingBank = "HDFC Bank ••4821",
            currentReadingAmount = 480.0,
            currentReadingCategory = "Food & Dining",
            recentReadTransactions = listOf(
                SmsReadPreviewItem(
                    id = "preview_1",
                    merchant = "Blue Tokai Coffee",
                    bankName = "HDFC Bank ••4821",
                    category = "Food & Dining",
                    amount = 480.0,
                    isIncome = false
                )
            )
        )
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("sms_parallel_threads_badge").assertIsDisplayed()
        assertTrue(composeTestRule.onAllNodesWithText("6R", substring = true).fetchSemanticsNodes().isEmpty())
        composeTestRule.onNodeWithTag("sms_sync_percentage_text").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("sms_sync_read_count").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("sms_transaction_reading_animation").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("sms_live_transaction_ticker").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sms_scanner_viewport_canvas").assertExists()
        composeTestRule.onNodeWithText("Blue Tokai Coffee").assertIsDisplayed()
        composeTestRule.onNodeWithText("₹480").assertIsDisplayed()

        val badgeBounds = composeTestRule.onNodeWithTag("sms_parallel_threads_badge")
            .fetchSemanticsNode().boundsInRoot
        val animBounds = composeTestRule.onNodeWithTag("sms_transaction_reading_animation")
            .fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected scanning animation card (${animBounds.top}) to be placed below header badge (${badgeBounds.bottom})",
            animBounds.top > badgeBounds.bottom
        )

        // 3. After scanning completes (`isSyncing = false, isCompleted = true`): animation is hidden again
        currentSyncState = currentSyncState.copy(isSyncing = false, isCompleted = true, readSmsCount = 120)
        composeTestRule.waitForIdle()
        assertTrue(
            "Expected scanning animation to be hidden once scanning finishes",
            composeTestRule.onAllNodesWithTag("sms_transaction_reading_animation").fetchSemanticsNodes().isEmpty()
        )
        composeTestRule.onNodeWithTag("finish_sms_sync_continue_button").assertIsDisplayed()
    }

    @Test
    fun `test feature-flagged expert settings, CSV export button, and removal of thread-count and bug-guard toggles on HouseholdTabScreen`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalAccountSessionManager.ensureInitialized(context)
        // Reset expert mode to disabled initially
        LocalAccountSessionManager.setExpertFeatureFlags(
            context,
            ExpertFeatureFlagsSettings(expertModeEnabled = false)
        )

        val uiState = OneFamilyDashboardUiState(
            isLoading = false,
            userProfile = UserProfile(uid = "local_user_test", displayName = "Test User", currencySymbol = "₹"),
            household = Household(id = "local_household_test", name = "Family Home", currencySymbol = "₹")
        )

        composeTestRule.setContent {
            OneFamilyTheme {
                HouseholdTabScreen(
                    uiState = uiState,
                    currentUserName = "Test User",
                    currentUserEmail = "test@onefamily.app",
                    onUpdateUserName = {},
                    onUpdatePreferredCurrency = {},
                    onAddAccountClick = {},
                    onScanSmsClick = {},
                    onSignOutClick = {},
                    onCreateHousehold = { _, _ -> },
                    onJoinByCode = {},
                    onInviteByEmail = {},
                    onAcceptEmailInvite = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Verify Export CSV card & button are displayed in Settings
        composeTestRule.onNodeWithTag("household_tab_list")
            .performScrollToNode(hasTestTag("settings_export_csv_button"))
        composeTestRule.onNodeWithTag("settings_export_csv_button").assertIsDisplayed()

        // 1. Scroll to ThemeModeSelectorBar and verify it uses color-only swatches (no "System", "Light", "Dark" text labels)
        composeTestRule.onNodeWithTag("household_tab_list")
            .performScrollToNode(hasTestTag("theme_color_swatch_bar"))
        composeTestRule.onNodeWithTag("theme_color_swatch_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_mode_system").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_mode_light").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_mode_dark").assertIsDisplayed()
        assertTrue(composeTestRule.onAllNodesWithText("System").fetchSemanticsNodes().isEmpty())

        // Tap Dark and System color swatches to verify theme mode switches cleanly
        composeTestRule.onNodeWithTag("theme_mode_dark").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppThemeMode.DARK, LocalAccountSessionManager.themeMode.value)
        composeTestRule.onNodeWithTag("theme_mode_system").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppThemeMode.SYSTEM, LocalAccountSessionManager.themeMode.value)

        // 2. Scroll to Advanced Settings card and verify Crashlytics and FCM Push cards are hidden when OFF
        composeTestRule.onNodeWithTag("household_tab_list")
            .performScrollToNode(hasTestTag("expert_feature_flag_card"))
        composeTestRule.onNodeWithTag("expert_feature_flag_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Advanced Settings").assertIsDisplayed()
        assertTrue(composeTestRule.onAllNodesWithTag("crashlytics_settings_card").fetchSemanticsNodes().isEmpty())
        assertTrue(composeTestRule.onAllNodesWithTag("fcm_push_settings_card").fetchSemanticsNodes().isEmpty())

        // 3. Enable the Master Expert Feature Flag switch
        composeTestRule.onNodeWithTag("expert_feature_flag_switch").performClick()
        composeTestRule.waitForIdle()
        assertTrue(LocalAccountSessionManager.expertFeatureFlags.value.expertModeEnabled)

        // 4. Verify Expert sub-flag chips and valid diagnostic cards exist, while ALL thread-count and bug-guard toggles are absent
        composeTestRule.onNodeWithTag("expert_subflags_row").assertExists()
        composeTestRule.onNodeWithTag("household_tab_list")
            .performScrollToNode(hasTestTag("fcm_push_settings_card"))
        composeTestRule.onNodeWithTag("fcm_push_settings_card").assertExists()

        composeTestRule.onNodeWithTag("household_tab_list")
            .performScrollToNode(hasTestTag("crashlytics_settings_card"))
        composeTestRule.onNodeWithTag("crashlytics_settings_card").assertExists()

        // Ensure user cannot modify parsing/writing thread counts or disable bug guards in UI
        assertTrue(composeTestRule.onAllNodesWithTag("expert_parallel_sms_card").fetchSemanticsNodes().isEmpty())
        assertTrue(composeTestRule.onAllNodesWithTag("expert_reader_threads_chip_6").fetchSemanticsNodes().isEmpty())
        assertTrue(composeTestRule.onAllNodesWithTag("expert_writer_threads_chip_6").fetchSemanticsNodes().isEmpty())
        assertTrue(composeTestRule.onAllNodesWithTag("expert_strict_duplicate_switch").fetchSemanticsNodes().isEmpty())
        assertTrue(composeTestRule.onAllNodesWithTag("crashlytics_sms_guard_switch").fetchSemanticsNodes().isEmpty())
        assertTrue(composeTestRule.onAllNodesWithTag("crashlytics_typography_guard_switch").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `test default theme color is kiwi green and supports material 3 dynamic and custom primary colors`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalAccountSessionManager.ensureInitialized(context)

        // Reset to default Kiwi Green
        LocalAccountSessionManager.setPrimaryColorOption(context, AppPrimaryColorOption.KIWI_GREEN)
        assertEquals(AppPrimaryColorOption.KIWI_GREEN, LocalAccountSessionManager.primaryColorOption.value)

        // Verify Kiwi Green color schemes resolve to KiwiPrimaryLight and KiwiPrimaryDark
        val lightKiwiScheme = resolveAppColorScheme(
            context = context,
            darkTheme = false,
            primaryColorOption = AppPrimaryColorOption.KIWI_GREEN,
            customPrimaryColorArgb = 0xFF65A30DL
        )
        val darkKiwiScheme = resolveAppColorScheme(
            context = context,
            darkTheme = true,
            primaryColorOption = AppPrimaryColorOption.KIWI_GREEN,
            customPrimaryColorArgb = 0xFF65A30DL
        )
        assertEquals(KiwiPrimaryLight, lightKiwiScheme.primary)
        assertEquals(KiwiPrimaryDark, darkKiwiScheme.primary)

        // Verify custom primary color ARGB resolves to exact chosen primary color
        val customPurpleArgb = 0xFF9333EAL
        LocalAccountSessionManager.setCustomPrimaryColor(context, customPurpleArgb)
        assertEquals(AppPrimaryColorOption.CUSTOM_COLOR, LocalAccountSessionManager.primaryColorOption.value)
        assertEquals(customPurpleArgb, LocalAccountSessionManager.customPrimaryColorArgb.value)

        val customLightScheme = resolveAppColorScheme(
            context = context,
            darkTheme = false,
            primaryColorOption = AppPrimaryColorOption.CUSTOM_COLOR,
            customPrimaryColorArgb = customPurpleArgb
        )
        assertEquals(androidx.compose.ui.graphics.Color(customPurpleArgb), customLightScheme.primary)

        // Verify Material 3 System Dynamic Color option can be selected via PrimaryColorSelectorBar
        val uiState = OneFamilyDashboardUiState(
            isLoading = false,
            userProfile = UserProfile(uid = "local_user_test", displayName = "Mock User", currencySymbol = "₹"),
            household = Household(id = "local_household_test", name = "Family Home", currencySymbol = "₹")
        )
        composeTestRule.setContent {
            OneFamilyTheme {
                HouseholdTabScreen(
                    uiState = uiState,
                    currentUserName = "Mock User",
                    currentUserEmail = "test@onefamily.local",
                    onUpdateUserName = {},
                    onUpdatePreferredCurrency = {},
                    onAddAccountClick = {},
                    onScanSmsClick = {},
                    onSignOutClick = {},
                    onCreateHousehold = { _, _ -> },
                    onJoinByCode = {},
                    onInviteByEmail = {},
                    onAcceptEmailInvite = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("household_tab_list")
            .performScrollToNode(hasTestTag("primary_color_swatch_bar"))
        composeTestRule.onNodeWithTag("primary_color_swatch_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("primary_color_kiwi_green").assertIsDisplayed()
        composeTestRule.onNodeWithTag("primary_color_system_dynamic").assertIsDisplayed()

        // Tap Material 3 System Dynamic swatch
        composeTestRule.onNodeWithTag("primary_color_system_dynamic").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppPrimaryColorOption.SYSTEM_DYNAMIC, LocalAccountSessionManager.primaryColorOption.value)

        // Tap Kiwi Green swatch to restore default
        composeTestRule.onNodeWithTag("primary_color_kiwi_green").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppPrimaryColorOption.KIWI_GREEN, LocalAccountSessionManager.primaryColorOption.value)
    }

    @Test
    fun `test overall budget trend line shows below budget and above budget status`() {
        val currentPeriod = YearMonthPeriod(2026, 11)
        val prevPeriod = currentPeriod.previousMonth()
        val sampleBars = listOf(
            MonthlySpendBar(period = prevPeriod, label = "Oct", expenseTotal = 32000.0, incomeTotal = 0.0, isSelected = false),
            MonthlySpendBar(period = currentPeriod, label = "Nov", expenseTotal = 42000.0, incomeTotal = 0.0, isSelected = true)
        )

        var activeOverallBudget by mutableStateOf(50000.0)

        composeTestRule.setContent {
            OneFamilyTheme {
                MonthlyBarGraph(
                    bars = sampleBars,
                    currencySymbol = "₹",
                    overallBudget = activeOverallBudget,
                    onSelectMonth = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // 1. When overall budget (₹50,000) > actual spend (₹42,000), trend line and "Below Budget" status pill are displayed
        composeTestRule.onNodeWithTag("overall_budget_trend_header").assertIsDisplayed()
        composeTestRule.onNodeWithTag("overall_budget_status_pill").assertIsDisplayed()
        composeTestRule.onNodeWithTag("overall_budget_trend_canvas").assertIsDisplayed()
        composeTestRule.onNodeWithText("▼ ₹8.0k Below Budget").assertIsDisplayed()

        // 2. When overall budget (₹35,000) < actual spend (₹42,000), status pill updates to "Above Budget"
        activeOverallBudget = 35000.0
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("▲ ₹7.0k Above Budget").assertIsDisplayed()

        // 3. When overall budget is 0.0 (not set), trend header and canvas are hidden
        activeOverallBudget = 0.0
        composeTestRule.waitForIdle()
        assertTrue(composeTestRule.onAllNodesWithTag("overall_budget_trend_header").fetchSemanticsNodes().isEmpty())
        assertTrue(composeTestRule.onAllNodesWithTag("overall_budget_trend_canvas").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `test categories use Unknown instead of General, eliminate duplicate defaults, and map unique icons and notification icons`() {
        // 1. Unknown replaces General; zero duplicates in ExpenseCategories.ALL
        assertTrue(ExpenseCategories.ALL.contains("Unknown"))
        assertFalse(ExpenseCategories.ALL.any { it.equals("General", ignoreCase = true) })
        assertEquals(ExpenseCategories.ALL.size, ExpenseCategories.ALL.map { it.lowercase() }.distinct().size)

        // 2. Legacy and duplicate category aliases collapse cleanly
        assertEquals("Unknown", ExpenseCategories.normalizeCategory(""))
        assertEquals("Unknown", ExpenseCategories.normalizeCategory("General"))
        assertEquals("Salary & Income", ExpenseCategories.normalizeCategory("Salary"))
        assertEquals("Bills & Utilities", ExpenseCategories.normalizeCategory("Utilities"))
        assertEquals("Fuel & Travel", ExpenseCategories.normalizeCategory("Transportation"))
        assertEquals("Health & Fitness", ExpenseCategories.normalizeCategory("Health & Medical"))

        // 3. Every standard category has its own distinct Material icon & distinct notification drawable icon
        val composeIcons = ExpenseCategories.ALL.map { categoryIcon(it) }.toSet()
        assertEquals(
            "Expected every standard category in ExpenseCategories.ALL to have a unique Compose icon",
            ExpenseCategories.ALL.size,
            composeIcons.size
        )
        val notifIconResIds = ExpenseCategories.ALL.map {
            TransactionNotificationHelper.categoryNotificationIconRes(it)
        }.toSet()
        assertEquals(
            "Expected every standard category in ExpenseCategories.ALL to have a unique notification icon drawable",
            ExpenseCategories.ALL.size,
            notifIconResIds.size
        )
    }

    @Test
    fun `test notifications display category icon and concise merchant category amount summary without raw SMS body`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val nm = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancelAll()

        TransactionNotificationHelper.showNewTransactionNotification(
            context = app,
            expenseId = "notif_tx_99",
            householdId = "local_household_test",
            visibility = "SHARED",
            title = "Swiggy Instamart\nRs. 645.00 debited from A/c XX4821 Avl Bal Rs. 24000",
            amount = 645.0,
            currentCategory = "Groceries",
            currencySymbol = "₹",
            paymentSummary = "UPI • HDFC Bank ••4821",
            transactionType = "EXPENSE"
        )

        val activeNotifs = shadowOf(nm).allNotifications
        assertEquals(1, activeNotifs.size)
        val posted = activeNotifs.first()

        // Verify category-specific small icon & large icon bitmap
        assertEquals(
            R.drawable.ic_cat_groceries,
            posted.smallIcon.resId
        )
        assertNotNull("Expected large category icon bitmap on transaction notification", posted.getLargeIcon())

        val titleText = posted.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val contentText = posted.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = posted.extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()

        // Must contain Merchant, Amount, and Category summary — and NEVER raw multi-line SMS text
        assertTrue(titleText.contains("Swiggy Instamart"))
        assertTrue(titleText.contains("₹645"))
        assertTrue(contentText.contains("Groceries"))
        assertTrue(bigText.contains("Merchant: Swiggy Instamart"))
        assertTrue(bigText.contains("Category: Groceries"))
        assertTrue(bigText.contains("Amount: ₹645"))
        assertFalse(bigText.contains("debited from", ignoreCase = true))
        assertFalse(bigText.contains("Avl Bal", ignoreCase = true))
    }

    @Test
    fun `test Current FY, Past FY, Past Year filters and CSV export with period selection`() {
        fun utcMillis(year: Int, monthOneBased: Int, day: Int): Long {
            return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, monthOneBased - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        // Reference date: 15 Nov 2026 -> Current FY is Apr 1, 2026 .. Mar 31, 2027; Past FY is Apr 1, 2025 .. Mar 31, 2026
        val refNov2026 = utcMillis(2026, 11, 15)
        val txCurrentFyMay2026 = ExpenseTransaction(
            id = "tx_cfy_1",
            title = "Apollo Pharmacy, Koramangala",
            amount = 1250.50,
            type = "EXPENSE",
            category = "Health & Fitness",
            accountName = "HDFC Bank ••4821",
            paymentMode = "UPI",
            userName = "Aarav",
            householdId = "hh_1",
            visibility = "SHARED",
            transactionDateMillis = utcMillis(2026, 5, 10)
        )
        val txPastFyFeb2026 = ExpenseTransaction(
            id = "tx_pfy_1",
            title = "BESCOM Electricity",
            amount = 3400.0,
            type = "EXPENSE",
            category = "Bills & Utilities",
            accountName = "ICICI Bank ••1104",
            paymentMode = "BANK",
            userName = "Priya",
            householdId = "hh_1",
            visibility = "SHARED",
            transactionDateMillis = utcMillis(2026, 2, 18)
        )
        val txPastFyAug2025 = ExpenseTransaction(
            id = "tx_pfy_2",
            title = "BigBasket Superstore",
            amount = 2800.0,
            type = "EXPENSE",
            category = "Groceries",
            accountName = "HDFC Bank ••4821",
            paymentMode = "CARD",
            userName = "Aarav",
            householdId = "hh_1",
            visibility = "SHARED",
            transactionDateMillis = utcMillis(2025, 8, 22)
        )
        val allSampleTxs = listOf(txCurrentFyMay2026, txPastFyFeb2026, txPastFyAug2025)

        val currentFyFiltered = DateRangeFilterUtils.filterTransactionsByPreset(
            transactions = allSampleTxs,
            preset = DateRangePreset.CURRENT_FY,
            selectedPeriod = YearMonthPeriod(2026, 10),
            customRange = null,
            referenceMillis = refNov2026
        )
        assertEquals(listOf("tx_cfy_1"), currentFyFiltered.map { it.id })

        val pastFyFiltered = DateRangeFilterUtils.filterTransactionsByPreset(
            transactions = allSampleTxs,
            preset = DateRangePreset.PAST_FY,
            selectedPeriod = YearMonthPeriod(2026, 10),
            customRange = null,
            referenceMillis = refNov2026
        )
        assertEquals(listOf("tx_pfy_1", "tx_pfy_2"), pastFyFiltered.map { it.id })

        val thisYearFiltered = DateRangeFilterUtils.filterTransactionsByPreset(
            transactions = allSampleTxs,
            preset = DateRangePreset.THIS_YEAR,
            selectedPeriod = YearMonthPeriod(2026, 10),
            customRange = null,
            referenceMillis = refNov2026
        )
        assertEquals(listOf("tx_cfy_1", "tx_pfy_1"), thisYearFiltered.map { it.id })

        val pastYearFiltered = DateRangeFilterUtils.filterTransactionsByPreset(
            transactions = allSampleTxs,
            preset = DateRangePreset.PAST_YEAR,
            selectedPeriod = YearMonthPeriod(2026, 10),
            customRange = null,
            referenceMillis = refNov2026
        )
        assertEquals(listOf("tx_pfy_2"), pastYearFiltered.map { it.id })

        // Verify RFC-4180 CSV generation escapes commas in merchant names and outputs normalized categories
        val csvOutput = CsvTransactionExporter.buildTransactionsCsv(
            transactions = currentFyFiltered,
            currencySymbol = "₹"
        )
        val lines = csvOutput.trim().lines()
        assertEquals(2, lines.size)
        assertTrue(lines[0].startsWith("Date,Time,Merchant,Category,Type,Amount"))
        assertTrue(lines[1].contains("\"Apollo Pharmacy, Koramangala\""))
        assertTrue(lines[1].contains("Health & Fitness"))
        assertTrue(lines[1].contains("1250.50"))

        // Verify ExportTransactionsCsvDialog UI allows selecting Current FY and triggers Save CSV callback
        var exportedCsvPayload: Triple<String, String, Int>? = null
        val dialogUiState = OneFamilyDashboardUiState(
            isLoading = false,
            selectedDateRangePreset = DateRangePreset.ALL_TIME,
            allScopedExpenses = allSampleTxs,
            sharedExpenses = allSampleTxs
        )

        composeTestRule.setContent {
            OneFamilyTheme {
                ExportTransactionsCsvDialog(
                    uiState = dialogUiState,
                    onDismiss = {},
                    onSaveCsvToFile = { csv, fileName, count, _ ->
                        exportedCsvPayload = Triple(csv, fileName, count)
                    }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("export_csv_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("export_preset_chip_current_fy").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("confirm_export_csv_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertNotNull(exportedCsvPayload)
        assertTrue(exportedCsvPayload!!.second.endsWith(".csv"))
        assertTrue(exportedCsvPayload!!.first.contains("Date,Time,Merchant,Category"))
    }

    @Test
    fun `test cross-bucket boundary SMS duplicate guard, typography scale guard, and batch sync guard are permanently active when all feature flags are disabled`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalAccountSessionManager.signInWithUsernamePassword(context, "test", "pass")
        val user = LocalAccountSessionManager.getCurrentUser(context)!!

        // Explicitly disable ALL optional feature flags and crash reporting to prove zero bug fixes are behind flags
        LocalAccountSessionManager.updateExpertFeatureFlags(
            context,
            ExpertFeatureFlagsSettings(
                expertModeEnabled = false,
                crashAnalyticsFeatureEnabled = false,
                fcmPushDiagnosticsEnabled = false
            )
        )
        LocalAccountSessionManager.updateCrashlyticsSettings(
            context,
            CrashlyticsSettings(crashReportingEnabled = false)
        )
        assertFalse(LocalAccountSessionManager.expertFeatureFlags.value.expertModeEnabled)
        assertFalse(LocalAccountSessionManager.crashlyticsSettings.value.crashReportingEnabled)

        // 1. Typography Scale Guard: NaN, Infinity, negative, and extreme scales never crash and maintain >= 1.25x lineHeight
        val nanTypography = buildResponsiveTypography(scale = Float.NaN, fontScale = Float.NaN)
        val infTypography = buildResponsiveTypography(scale = Float.POSITIVE_INFINITY, fontScale = 4.0f)
        val negTypography = buildResponsiveTypography(scale = -3.0f, fontScale = -1.0f)
        val extremeTypography = buildResponsiveTypography(scale = 1.30f, fontScale = 2.0f)

        listOf(nanTypography, infTypography, negTypography, extremeTypography).forEach { typo ->
            val styles = listOf(
                typo.displayLarge, typo.headlineLarge, typo.headlineMedium, typo.headlineSmall,
                typo.titleLarge, typo.titleMedium, typo.titleSmall,
                typo.bodyLarge, typo.bodyMedium, typo.bodySmall,
                typo.labelLarge, typo.labelMedium, typo.labelSmall
            )
            styles.forEach { style ->
                assertTrue("fontSize must be finite and positive", style.fontSize.value.isFinite() && style.fontSize.value > 0f)
                assertTrue("lineHeight must be finite and positive", style.lineHeight.value.isFinite() && style.lineHeight.value > 0f)
                assertTrue(
                    "lineHeight (${style.lineHeight.value}) must be >= 1.25x fontSize (${style.fontSize.value})",
                    style.lineHeight.value + 0.01f >= style.fontSize.value * 1.25f
                )
            }
        }
        // Combined multiplier is clamped to <= 1.55x base 16sp bodyLarge (<= 24.8sp)
        assertTrue(extremeTypography.bodyLarge.fontSize.value <= 16f * 1.55f + 0.05f)

        // 2. Cross-Bucket Boundary Duplicate SMS Guard (straddling BOTH 5-minute 300_000ms and 10-minute 600_000ms boundaries)
        val repo = OneFamilyRepository(context)
        val smsReader = SmsInboxReader(context)
        val profile = repo.ensureUserProfile(user.displayName, user.email).getOrNull()!!
        val household = repo.getHouseholdOnce(profile.activeHouseholdId)!!

        // Exact 10-minute (and 5-minute) epoch boundary: 299.5s vs 301.0s / 599.5s vs 601.0s
        val exact10MinBoundary = ((System.currentTimeMillis() - 3_600_000L) / 600_000L) * 600_000L
        val tsJustBeforeBoundary = exact10MinBoundary - 500L   // e.g. 09:59.500 (bucket N-1)
        val tsJustAfterBoundary = exact10MinBoundary + 1_000L  // e.g. 10:00.001 (bucket N)
        val duplicateBody = "Rs. 2,345.00 debited from HDFC Bank A/c XX7744 on 18-11-26 to VPA zepto@icici (UPI Ref 554433221199). Avl Bal: Rs. 31200.00"

        val parsedCrossBoundary = smsReader.parseSmsBatchesInParallel(
            rawRows = listOf(
                RawSmsRow(sender = "VM-HDFCBK", body = duplicateBody, timestamp = tsJustBeforeBoundary),
                RawSmsRow(sender = "AD-HDFCBK", body = duplicateBody, timestamp = tsJustAfterBoundary)
            ),
            existingSignatures = emptySet()
        )
        assertEquals(
            "Cross-boundary identical SMS rows (09:59.5 vs 10:01.0) must deduplicate to 1 candidate",
            1,
            parsedCrossBoundary.size
        )

        // Also verify repository batchImportSmsCandidates rejects a second candidate across the 5m/10m bucket boundary
        val candBefore = smsReader.parseRawSms("VM-HDFCBK", duplicateBody, tsJustBeforeBoundary)!!
        val candAfter = smsReader.parseRawSms("AD-HDFCBK", duplicateBody, tsJustAfterBoundary)!!
        assertTrue("Raw modulo bucket signatures differ across boundary", candBefore.id != candAfter.id)

        val firstImport = repo.batchImportSmsCandidates(
            candidates = listOf(candBefore),
            userName = user.displayName,
            household = household,
            shareByDefault = true
        ).getOrThrow()
        assertEquals(1, firstImport)

        val secondCrossBoundaryImport = repo.batchImportSmsCandidates(
            candidates = listOf(candAfter),
            userName = user.displayName,
            household = household,
            shareByDefault = true
        ).getOrThrow()
        assertEquals(
            "Second identical SMS straddling 10-minute boundary must be blocked without any feature flag",
            0,
            secondCrossBoundaryImport
        )

        // Verify LocalAccountDataStore.deduplicateSmsExpenses also collapses legacy cross-boundary duplicates
        val legacyDupA = ExpenseTransaction(
            id = "legacy_dup_a",
            userId = user.uid,
            userName = user.displayName,
            householdId = household.id,
            title = "Zepto",
            amount = 2345.0,
            type = "EXPENSE",
            category = "Groceries",
            accountName = "HDFC Bank ••7744",
            paymentMode = "UPI",
            notes = "[VM-HDFCBK] $duplicateBody",
            visibility = "SHARED",
            smsSignature = candBefore.id,
            referenceNumber = "554433221199",
            transactionDateMillis = tsJustBeforeBoundary
        )
        val legacyDupB = legacyDupA.copy(
            id = "legacy_dup_b",
            notes = "[AD-HDFCBK] $duplicateBody",
            smsSignature = candAfter.id,
            transactionDateMillis = tsJustAfterBoundary
        )
        val collapsed = LocalAccountDataStore.deduplicateSmsExpenses(listOf(legacyDupA, legacyDupB))
        assertEquals("Legacy cross-boundary SMS duplicates must collapse to 1 item", 1, collapsed.size)

        // 3. Verify constant thread counts and absence of thread badges in ViewModel sync status message
        assertEquals(6, SmsInboxReader.DEFAULT_PARALLEL_READER_THREADS)
        assertEquals(6, SmsInboxReader.DEFAULT_PARALLEL_WRITER_THREADS)
        assertEquals(5, SmsInboxReader.MIN_PARALLEL_THREADS)

        val viewModel = OneFamilyViewModel(
            repository = repo,
            smsInboxReader = smsReader,
            currentUserId = user.uid,
            currentUserName = user.displayName,
            currentUserEmail = user.email
        )
        composeTestRule.waitForIdle()
        shadowOf(Looper.getMainLooper()).idle()

        viewModel.runParallelSmsBatchPipeline(
            rawRowsOverride = listOf(
                RawSmsRow(
                    sender = "VM-HDFCBK",
                    body = "Rs. 610.00 debited from HDFC Bank A/c XX7744 on 18-11-26 to VPA metro@sbi (UPI Ref 887766554433).",
                    timestamp = exact10MinBoundary + 120_000L
                )
            ),
            existingSignatures = emptySet(),
            userName = user.displayName,
            household = household
        )
        val syncStatusMsg = viewModel.smsSyncProgress.value.statusMessage
        assertFalse("Status message must never expose thread counts or lightning badge", syncStatusMsg.contains("⚡"))
        assertFalse("Status message must never mention threads", syncStatusMsg.contains("thread", ignoreCase = true))

        LocalAccountSessionManager.signOut(context)
    }
}




