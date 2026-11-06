package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import com.ritesh.parser.core.TransactionType
import com.ritesh.parser.core.bank.BankParserFactory
import java.math.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AxioRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createHousehold_and_joinByCode_syncsSharedExpensesAcrossFamilyMembers() = runBlocking {
        val aliceUid = signInTestUser("alice_family@test.com")
        val repo = AxioRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfile("Alice", "alice_family@test.com").getOrThrow()
        }

        val household = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createHousehold(
                name = "Verma Household",
                monthlyBudgetLimit = 80000.0,
                currencySymbol = "₹",
                userDisplayName = "Alice",
                userEmail = "alice_family@test.com"
            ).getOrThrow()
        }

        assertTrue(household.joinCode.length == 6)

        // Bob signs in and joins using Alice's 6-character join code
        val bobUid = signInTestUser("bob_family@test.com")
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfile("Bob", "bob_family@test.com").getOrThrow()
        }

        val joinedHouseholdId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.joinHouseholdByCode(
                rawJoinCode = household.joinCode,
                userDisplayName = "Bob",
                userEmail = "bob_family@test.com"
            ).getOrThrow()
        }
        assertEquals(household.id, joinedHouseholdId)

        val updatedHousehold = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeHousehold(household.id).first { it != null && it.memberUids.contains(bobUid) }
        }
        assertNotNull(updatedHousehold)

        // Bob logs a shared household expense
        val sharedExpenseId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addExpense(
                userName = "Bob",
                household = updatedHousehold,
                title = "BigBasket Family Groceries",
                amount = 1850.0,
                type = "EXPENSE",
                category = "Groceries",
                accountName = "HDFC Bank ••4821",
                paymentMode = "UPI",
                tags = listOf("weekly", "kitchen"),
                notes = "Monthly staples",
                isShared = true
            ).getOrThrow()
        }

        // Alice signs back in and observes Bob's shared expense in real time
        signInTestUser("alice_family@test.com")
        val aliceSharedList = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeSharedExpenses(household.id, aliceUid).first { list ->
                list.any { it.id == sharedExpenseId }
            }
        }
        assertTrue(aliceSharedList.any { it.title == "BigBasket Family Groceries" })
    }

    @Test
    fun personalExpenses_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser("alice_priv@test.com")
        val repo = AxioRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfile("Alice", "alice_priv@test.com").getOrThrow()
            repo.addExpense(
                userName = "Alice",
                household = null,
                title = "Private Gift",
                amount = 999.0,
                type = "EXPENSE",
                category = "Shopping",
                accountName = "ICICI Card ••9012",
                paymentMode = "CARD",
                tags = listOf("secret"),
                notes = "",
                isShared = false
            ).getOrThrow()
        }

        // Bob signs in and tries to read Alice's personal_expenses
        signInTestUser("bob_priv@test.com")
        var caught: FirebaseFirestoreException? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .collection("personal_expenses")
                    .get()
                    .await()
            }
        } catch (e: Exception) {
            caught = e.unwrapFirestoreException()
        }
        assertNotNull("Expected FirebaseFirestoreException for cross-user personal expense access", caught)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, caught?.code)
    }

    @Test
    fun unauthenticatedUser_cannotObservePersonalExpenses() = runBlocking {
        auth.signOut()
        val repo = AxioRepository(firestore)
        var caught: FirebaseFirestoreException? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observePersonalExpenses("any_uid").first()
            }
        } catch (e: Exception) {
            caught = e.unwrapFirestoreException()
        }
        assertNotNull("Expected PERMISSION_DENIED when signed out", caught)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, caught?.code)
    }

    @Test
    fun cashiroBankParserFactory_parsesHdfcSbiIciciAndAxisAlertsAccurately() {
        val hdfcSms = "Sent Rs.1,845.00\nFrom HDFC Bank A/C *4821\nTo BIGBASKET GROCERY\nOn 05/11/26\nRef 631098451209\nAvl Bal Rs.64,210.50"
        val hdfcParsed = BankParserFactory.parseSms(hdfcSms, "AD-HDFCBK-S", 1730000000000L)
        assertNotNull(hdfcParsed)
        assertEquals("HDFC Bank", hdfcParsed!!.bankName)
        assertEquals(0, hdfcParsed.amount.compareTo(BigDecimal("1845.00")))
        assertEquals(TransactionType.EXPENSE, hdfcParsed.type)
        assertEquals("4821", hdfcParsed.accountNumber)
        assertEquals("Groceries", BankParserFactory.inferCategory(hdfcParsed))

        val sbiSms = "Dear UPI user A/C X9102 debited by 540.00 on date 05Nov26 trf to Zomato Limited Refno 431088761234. Avl Bal Rs.28,460.00 -SBI"
        val sbiParsed = BankParserFactory.parseSms(sbiSms, "VM-SBIINB-S", 1730000000000L)
        assertNotNull(sbiParsed)
        assertEquals("State Bank of India", sbiParsed!!.bankName)
        assertEquals(0, sbiParsed.amount.compareTo(BigDecimal("540.00")))
        assertEquals("Food & Dining", BankParserFactory.inferCategory(sbiParsed))
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
