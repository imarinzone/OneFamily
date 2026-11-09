package com.example

import android.content.Context
import androidx.compose.ui.text.font.FontFamily
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.LocalAccountDataStore
import com.example.data.local.LocalAccountSessionManager
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
}
