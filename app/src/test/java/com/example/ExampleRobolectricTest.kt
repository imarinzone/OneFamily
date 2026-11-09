package com.example

import android.content.Context
import androidx.compose.ui.text.font.FontFamily
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.LocalAccountDataStore
import com.example.data.local.LocalAccountSessionManager
import com.example.data.model.CrashlyticsSettings
import com.example.data.model.FcmPushSettings
import com.example.data.model.WallpaperColorAccent
import com.example.data.model.WallpaperDensity
import com.example.data.model.WallpaperPatternStyle
import com.example.data.model.WallpaperSettings
import com.example.data.repository.AxioRepository
import com.example.data.sms.SmsInboxReader
import com.example.data.sms.TransactionNotificationHelper
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
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.buildResponsiveTypography
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

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

        val repo = AxioRepository(context)
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
        val repo = AxioRepository(context)
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
        val repo = AxioRepository(context)
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
        val repo = AxioRepository(context)
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
    fun `test mock user login seeds mock data across last 3 months, financial accounts, and raw SMS cache`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val loginResult = LocalAccountSessionManager.signInWithUsernamePassword(
            context = context,
            rawUsername = "test",
            rawPassword = "pass"
        )
        assertTrue(loginResult.isSuccess)
        val user = loginResult.getOrNull()
        assertNotNull(user)
        assertEquals("Mock User", user?.displayName)

        val repo = AxioRepository(context)
        val profile = repo.ensureUserProfile(user!!.displayName, user.email).getOrNull()
        assertNotNull(profile)
        val household = repo.getHouseholdOnce(profile!!.activeHouseholdId)
        assertNotNull(household)

        // Verify shared mock transactions are populated across multiple distinct months
        val sharedExpenses = repo.observeSharedExpenses(household!!.id, user.uid).first()
        val mockTxs = sharedExpenses.filter { it.id.startsWith("mock_tx_") }
        assertTrue(mockTxs.size >= 15)

        // Verify distinct months represented in mock transactions
        val distinctMonths = mockTxs.map { tx ->
            Calendar.getInstance().apply { timeInMillis = tx.transactionDateMillis }.get(Calendar.MONTH)
        }.distinct()
        assertTrue("Expected at least 3 distinct months in mock data, found ${distinctMonths.size}", distinctMonths.size >= 3)

        // Verify categories span Groceries, Rent & Housing, Food & Dining, Utilities & Bills, Transport & Fuel
        val categories = mockTxs.map { it.category }.distinct()
        assertTrue(categories.contains("Groceries"))
        assertTrue(categories.contains("Rent & Housing"))
        assertTrue(categories.contains("Food & Dining"))

        // Verify raw SMS bodies are retrievable for mock transactions
        val groceryTx = mockTxs.first { it.category == "Groceries" }
        val rawSms = LocalAccountDataStore.getSmsMessageForTransaction(groceryTx)
        assertNotNull(rawSms)
        assertTrue(rawSms!!.body.contains("Nature's Basket") || rawSms.body.contains("HDFC Bank"))

        // Verify financial accounts were seeded
        val accounts = repo.observeAccounts(user.uid).first()
        assertTrue(accounts.size >= 3)
        assertTrue(accounts.any { it.name.contains("HDFC") })
        assertTrue(accounts.any { it.name.contains("Axis") })

        LocalAccountSessionManager.signOut(context)
    }

    @Test
    fun `test linking duplicate SMS transactions merges signatures, deletes duplicate, and prevents re-import`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalAccountSessionManager.signInWithUsernamePassword(context, "test", "pass")
        val user = LocalAccountSessionManager.getCurrentUser(context)!!
        val repo = AxioRepository(context)
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

        // 3. Verify Firebase Crashlytics stability guardrails & diagnostic breadcrumbs
        val customCrashlytics = CrashlyticsSettings(
            crashReportingEnabled = true,
            smsSyncGuardEnabled = true,
            typographyGuardEnabled = true
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
}
