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

class OneFamilyRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createHousehold_and_joinByCode_syncsSharedExpensesAcrossFamilyMembers() = runBlocking {
        val aliceUid = signInTestUser("alice_family@test.com")
        val repo = OneFamilyRepository(firestore)

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
        val repo = OneFamilyRepository(firestore)

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
        val repo = OneFamilyRepository(firestore)
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

    @Test
    fun smsDeduplication_and_linkDuplicate_preservesLinkedSignaturesAcrossPrivacyMoves() = runBlocking {
        val aliceUid = signInTestUser("alice_sms_dedup@test.com")
        val repo = OneFamilyRepository(firestore)

        val household = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfile("Alice", "alice_sms_dedup@test.com").getOrThrow()
            repo.createHousehold(
                name = "Dedup Family",
                monthlyBudgetLimit = 60000.0,
                currencySymbol = "₹",
                userDisplayName = "Alice",
                userEmail = "alice_sms_dedup@test.com"
            ).getOrThrow()
        }

        // Cross-bucket boundary timestamps: 599,500ms (bucket 0/1) vs 601,000ms (bucket 1/2)
        val base10MinBoundary = 1_730_000_400_000L // exact multiple of 600_000L (10m) and 300_000L (5m)
        val tsBeforeBoundary = base10MinBoundary - 500L
        val tsAfterBoundary = base10MinBoundary + 1_000L

        val smsBody = "Rs. 1,420.00 debited from A/c XX4821 on 05-11-26 to VPA swiggy@icici (UPI Ref 776655443322)."
        val parsedA = BankParserFactory.parseSms(smsBody, "VM-HDFCBK", tsBeforeBoundary)!!
        val candidateA = com.example.data.sms.SmsCandidate(
            id = parsedA.generateTransactionId(),
            parsed = parsedA,
            suggestedCategory = BankParserFactory.inferCategory(parsedA),
            suggestedAccountName = BankParserFactory.formatAccountDisplayName(parsedA),
            suggestedPaymentMode = BankParserFactory.inferPaymentMode(parsedA),
            isSharedByDefault = true
        )
        val parsedB = BankParserFactory.parseSms(smsBody, "AD-HDFCBK", tsAfterBoundary)!!
        val candidateBCrossBoundaryDup = com.example.data.sms.SmsCandidate(
            id = parsedB.generateTransactionId(),
            parsed = parsedB,
            suggestedCategory = BankParserFactory.inferCategory(parsedB),
            suggestedAccountName = BankParserFactory.formatAccountDisplayName(parsedB),
            suggestedPaymentMode = BankParserFactory.inferPaymentMode(parsedB),
            isSharedByDefault = true
        )

        val importedCount = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.batchImportSmsCandidates(
                candidates = listOf(candidateA, candidateBCrossBoundaryDup),
                userName = "Alice",
                household = household,
                shareByDefault = true
            ).getOrThrow()
        }
        assertEquals("Cross-boundary identical SMS should import exactly once", 1, importedCount)

        // Separate UPI app alert with different wording -> imported so user can link it, then preserved across Personal <-> Shared move
        val upiBody = "You paid Rs. 1,420.00 to Swiggy via UPI Ref 776655443322 from HDFC Bank XX4821."
        val parsedUpi = BankParserFactory.parseSms(upiBody, "AX-GPAY", tsAfterBoundary + 8_000L)!!
        val upiCandidate = com.example.data.sms.SmsCandidate(
            id = parsedUpi.generateTransactionId(),
            parsed = parsedUpi,
            suggestedCategory = BankParserFactory.inferCategory(parsedUpi),
            suggestedAccountName = BankParserFactory.formatAccountDisplayName(parsedUpi),
            suggestedPaymentMode = BankParserFactory.inferPaymentMode(parsedUpi),
            isSharedByDefault = true
        )
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.batchImportSmsCandidates(
                candidates = listOf(upiCandidate),
                userName = "Alice",
                household = household,
                shareByDefault = true
            ).getOrThrow()
        }

        val sharedBeforeLink = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeSharedExpenses(household.id, aliceUid).first { it.size == 2 }
        }
        val primary = sharedBeforeLink.first { it.smsSignature == candidateA.id }
        val secondary = sharedBeforeLink.first { it.smsSignature == upiCandidate.id }

        val merged = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.linkDuplicateTransaction(primary, secondary).getOrThrow()
        }
        assertTrue(merged.linkedSmsSignatures.contains(upiCandidate.id))

        // Move SHARED -> PERSONAL atomically and verify linkedSmsSignatures persists and blocks re-import
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.toggleExpensePrivacy(merged, household).getOrThrow()
        }
        val personalList = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observePersonalExpenses(aliceUid).first { list -> list.any { it.id == merged.id } }
        }
        val movedPersonal = personalList.first { it.id == merged.id }
        assertTrue(movedPersonal.linkedSmsSignatures.contains(upiCandidate.id))

        val reimportAfterMove = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.batchImportSmsCandidates(
                candidates = listOf(upiCandidate, candidateBCrossBoundaryDup),
                userName = "Alice",
                household = household,
                shareByDefault = true
            ).getOrThrow()
        }
        assertEquals(0, reimportAfterMove)
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}

