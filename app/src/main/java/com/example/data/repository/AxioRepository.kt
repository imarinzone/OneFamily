package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CategoryBudget
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FinancialAccount
import com.example.data.model.Household
import com.example.data.model.HouseholdCodeIndex
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AxioRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- USER PROFILE ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                logAndRethrow(error, OperationType.GET, path)
            }
    }

    suspend fun ensureUserProfile(
        displayName: String,
        email: String,
        photoUrl: String = ""
    ): Result<UserProfile> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                existing.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: UserProfile(id = uid, uid = uid, displayName = displayName, email = email)
            } else {
                val payload = mapOf(
                    "uid" to uid,
                    "email" to email.take(200),
                    "displayName" to displayName.ifBlank { "Family Member" }.take(100),
                    "photoUrl" to photoUrl.take(500),
                    "activeHouseholdId" to "",
                    "personalMonthlyBudget" to 35000.0,
                    "currencySymbol" to "₹",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                UserProfile(
                    id = uid,
                    uid = uid,
                    email = email,
                    displayName = displayName.ifBlank { "Family Member" },
                    photoUrl = photoUrl,
                    personalMonthlyBudget = 35000.0,
                    currencySymbol = "₹"
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updatePersonalBudgetSettings(
        personalMonthlyBudget: Double,
        currencySymbol: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            docRef.update(
                mapOf(
                    "personalMonthlyBudget" to personalMonthlyBudget.coerceAtLeast(0.0),
                    "currencySymbol" to currencySymbol.ifBlank { "₹" }.take(10),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- HOUSEHOLD CREATION, LINKING & INVITES ---

    fun observeHousehold(householdId: String): Flow<Household?> {
        val path = "households/$householdId"
        return db.collection("households").document(householdId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(Household::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                logAndRethrow(error, OperationType.GET, path)
            }
    }

    fun observeEmailInvitedHouseholds(userEmail: String): Flow<List<Household>> {
        val normalizedEmail = userEmail.trim().lowercase()
        val path = "households"
        return db.collection("households")
            .whereArrayContains("invitedEmails", normalizedEmail)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Household::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                logAndRethrow(error, OperationType.LIST, path)
            }
    }

    suspend fun createHousehold(
        name: String,
        monthlyBudgetLimit: Double,
        currencySymbol: String = "₹",
        userDisplayName: String,
        userEmail: String
    ): Result<Household> = runCatching {
        val uid = requireUserId()
        val joinCode = generateJoinCode()
        val householdRef = db.collection("households").document()
        val codeRef = db.collection("household_codes").document(joinCode)
        val userRef = db.collection("users").document(uid)

        val cleanName = name.trim().ifBlank { "Our Family Budget" }.take(100)
        val cleanUserName = userDisplayName.trim().ifBlank { "Member" }.take(100)
        val cleanEmail = userEmail.trim().lowercase().take(200)

        try {
            val householdPayload = mapOf(
                "name" to cleanName,
                "ownerUid" to uid,
                "joinCode" to joinCode,
                "memberUids" to listOf(uid),
                "memberNames" to mapOf(uid to cleanUserName),
                "memberEmails" to mapOf(uid to cleanEmail),
                "invitedEmails" to emptyList<String>(),
                "monthlyBudgetLimit" to monthlyBudgetLimit.coerceAtLeast(1000.0),
                "currencySymbol" to currencySymbol.ifBlank { "₹" }.take(10),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            householdRef.set(householdPayload).await()

            val codePayload = mapOf(
                "joinCode" to joinCode,
                "householdId" to householdRef.id,
                "householdName" to cleanName,
                "ownerUid" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            codeRef.set(codePayload).await()

            userRef.update(
                mapOf(
                    "activeHouseholdId" to householdRef.id,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Household(
                id = householdRef.id,
                name = cleanName,
                ownerUid = uid,
                joinCode = joinCode,
                memberUids = listOf(uid),
                memberNames = mapOf(uid to cleanUserName),
                memberEmails = mapOf(uid to cleanEmail),
                monthlyBudgetLimit = monthlyBudgetLimit
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, householdRef.path)
            throw e
        }
    }

    suspend fun joinHouseholdByCode(
        rawJoinCode: String,
        userDisplayName: String,
        userEmail: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val code = rawJoinCode.trim().uppercase()
        require(code.length in 4..12) { "Please enter a valid 6-character join code." }

        val codeRef = db.collection("household_codes").document(code)
        val codeSnap = try {
            codeRef.get().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, codeRef.path)
            throw e
        }

        if (!codeSnap.exists()) {
            throw IllegalArgumentException("No household found with code $code.")
        }

        val index = codeSnap.toObject(HouseholdCodeIndex::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?: throw IllegalArgumentException("Invalid household code record.")

        joinHouseholdById(
            householdId = index.householdId,
            userDisplayName = userDisplayName,
            userEmail = userEmail
        ).getOrThrow()
    }

    suspend fun joinHouseholdById(
        householdId: String,
        userDisplayName: String,
        userEmail: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val householdRef = db.collection("households").document(householdId)
        try {
            val snap = householdRef.get().await()
            if (!snap.exists()) {
                throw IllegalArgumentException("Household no longer exists.")
            }
            val current = snap.toObject(Household::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: throw IllegalStateException("Could not parse household.")

            val updatedMembers = (current.memberUids + uid).distinct().take(20)
            val updatedNames = current.memberNames.toMutableMap().apply {
                put(uid, userDisplayName.ifBlank { "Family Member" }.take(100))
            }
            val normalizedEmail = userEmail.trim().lowercase()
            val updatedEmails = current.memberEmails.toMutableMap().apply {
                put(uid, normalizedEmail.take(200))
            }
            val updatedInvites = current.invitedEmails.filterNot { it.equals(normalizedEmail, ignoreCase = true) }

            if (uid !in current.memberUids) {
                householdRef.update(
                    mapOf(
                        "memberUids" to updatedMembers,
                        "memberNames" to updatedNames,
                        "memberEmails" to updatedEmails,
                        "invitedEmails" to updatedInvites,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            db.collection("users").document(uid).update(
                mapOf(
                    "activeHouseholdId" to householdId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            householdId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, householdRef.path)
            throw e
        }
    }

    suspend fun inviteMemberByEmail(
        household: Household,
        inviteeEmail: String
    ): Result<Unit> = runCatching {
        requireUserId()
        val normalized = inviteeEmail.trim().lowercase()
        require(normalized.contains("@") && normalized.length <= 200) { "Enter a valid email address." }

        val updatedInvites = (household.invitedEmails + normalized).distinct().take(20)
        val docRef = db.collection("households").document(household.id)
        try {
            docRef.update(
                mapOf(
                    "invitedEmails" to updatedInvites,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateHouseholdSettings(
        householdId: String,
        name: String,
        monthlyBudgetLimit: Double
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("households").document(householdId)
        try {
            docRef.update(
                mapOf(
                    "name" to name.trim().ifBlank { "Family Budget" }.take(100),
                    "monthlyBudgetLimit" to monthlyBudgetLimit.coerceAtLeast(0.0),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- EXPENSES (PERSONAL & HOUSEHOLD SHARED) ---

    fun observePersonalExpenses(userId: String): Flow<List<ExpenseTransaction>> {
        val path = "users/$userId/personal_expenses"
        return db.collection("users").document(userId).collection("personal_expenses")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ExpenseTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.transactionDateMillis }
            }
            .catch { error ->
                logAndRethrow(error, OperationType.LIST, path)
            }
    }

    fun observeSharedExpenses(householdId: String, userId: String): Flow<List<ExpenseTransaction>> {
        val path = "households/$householdId/shared_expenses"
        return db.collection("households").document(householdId).collection("shared_expenses")
            .whereArrayContains("memberUids", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ExpenseTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.transactionDateMillis }
            }
            .catch { error ->
                logAndRethrow(error, OperationType.LIST, path)
            }
    }

    suspend fun addExpense(
        userName: String,
        household: Household?,
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountName: String,
        paymentMode: String,
        tags: List<String>,
        notes: String,
        isShared: Boolean,
        smsSignature: String = "",
        referenceNumber: String = "",
        transactionDateMillis: Long = System.currentTimeMillis()
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val effectiveShared = isShared && household != null
        val targetHouseholdId = if (effectiveShared) household!!.id else ""
        val memberUids = if (effectiveShared) {
            (household!!.memberUids + uid).distinct().take(20)
        } else {
            listOf(uid)
        }

        val collectionRef = if (effectiveShared) {
            db.collection("households").document(targetHouseholdId).collection("shared_expenses")
        } else {
            db.collection("users").document(uid).collection("personal_expenses")
        }

        val docRef = collectionRef.document()
        val cleanTags = tags.map { it.trim().removePrefix("#") }
            .filter { it.isNotBlank() }
            .distinct()
            .take(10)
            .map { it.take(40) }

        val payload = mapOf(
            "userId" to uid,
            "userName" to userName.ifBlank { "Me" }.take(100),
            "householdId" to targetHouseholdId,
            "title" to title.trim().ifBlank { category }.take(150),
            "amount" to amount.coerceAtLeast(0.01),
            "type" to if (type == "INCOME") "INCOME" else "EXPENSE",
            "category" to category.ifBlank { "General" }.take(60),
            "accountName" to accountName.ifBlank { "Cash" }.take(100),
            "paymentMode" to if (paymentMode in setOf("UPI", "BANK", "CARD", "WALLET", "CASH")) paymentMode else "UPI",
            "tags" to cleanTags,
            "notes" to notes.trim().take(500),
            "visibility" to if (effectiveShared) "SHARED" else "PERSONAL",
            "smsSignature" to smsSignature.take(200),
            "referenceNumber" to referenceNumber.take(100),
            "transactionDateMillis" to transactionDateMillis,
            "memberUids" to memberUids,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun toggleExpensePrivacy(
        expense: ExpenseTransaction,
        household: Household
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val movingToShared = expense.visibility == "PERSONAL"

        // 1. Create in destination collection
        addExpense(
            userName = expense.userName,
            household = household,
            title = expense.title,
            amount = expense.amount,
            type = expense.type,
            category = expense.category,
            accountName = expense.accountName,
            paymentMode = expense.paymentMode,
            tags = expense.tags,
            notes = expense.notes,
            isShared = movingToShared,
            smsSignature = expense.smsSignature,
            referenceNumber = expense.referenceNumber,
            transactionDateMillis = expense.transactionDateMillis
        ).getOrThrow()

        // 2. Delete from source collection
        deleteExpense(expense).getOrThrow()
    }

    suspend fun deleteExpense(expense: ExpenseTransaction): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(expense.id.isNotBlank()) { "Expense ID must not be blank" }
        val docRef = if (expense.visibility == "SHARED" && expense.householdId.isNotBlank()) {
            db.collection("households").document(expense.householdId)
                .collection("shared_expenses").document(expense.id)
        } else {
            db.collection("users").document(uid)
                .collection("personal_expenses").document(expense.id)
        }
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- CATEGORY BUDGETS (PERSONAL & HOUSEHOLD SHARED) ---

    fun observePersonalCategoryBudgets(userId: String): Flow<List<CategoryBudget>> {
        val path = "users/$userId/personal_budgets"
        return db.collection("users").document(userId).collection("personal_budgets")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(CategoryBudget::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                logAndRethrow(error, OperationType.LIST, path)
            }
    }

    fun observeSharedCategoryBudgets(householdId: String, userId: String): Flow<List<CategoryBudget>> {
        val path = "households/$householdId/category_budgets"
        return db.collection("households").document(householdId).collection("category_budgets")
            .whereArrayContains("memberUids", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(CategoryBudget::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                logAndRethrow(error, OperationType.LIST, path)
            }
    }

    suspend fun saveCategoryBudget(
        category: String,
        monthlyLimit: Double,
        alertThresholdPercent: Int = 80,
        isShared: Boolean,
        household: Household?
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val effectiveShared = isShared && household != null
        val sanitizedId = category.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifBlank { "general" }

        val docRef = if (effectiveShared) {
            db.collection("households").document(household!!.id)
                .collection("category_budgets").document(sanitizedId)
        } else {
            db.collection("users").document(uid)
                .collection("personal_budgets").document(sanitizedId)
        }

        val memberUids = if (effectiveShared) {
            (household!!.memberUids + uid).distinct().take(20)
        } else {
            listOf(uid)
        }

        val payload = mapOf(
            "userId" to uid,
            "householdId" to if (effectiveShared) household!!.id else "",
            "category" to category.take(60),
            "monthlyLimit" to monthlyLimit.coerceAtLeast(1.0),
            "alertThresholdPercent" to alertThresholdPercent.coerceIn(1, 100),
            "memberUids" to memberUids,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCategoryBudget(budget: CategoryBudget, isShared: Boolean): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = if (isShared && budget.householdId.isNotBlank()) {
            db.collection("households").document(budget.householdId)
                .collection("category_budgets").document(budget.id)
        } else {
            db.collection("users").document(uid)
                .collection("personal_budgets").document(budget.id)
        }
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- FINANCIAL ACCOUNTS (BANK, CARD, UPI, WALLET, CASH) ---

    fun observeAccounts(userId: String): Flow<List<FinancialAccount>> {
        val path = "users/$userId/accounts"
        return db.collection("users").document(userId).collection("accounts")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FinancialAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                logAndRethrow(error, OperationType.LIST, path)
            }
    }

    suspend fun upsertAccount(
        name: String,
        bankName: String,
        accountType: String,
        lastFourDigits: String,
        balance: Double,
        creditLimit: Double = 0.0,
        isSharedVisible: Boolean = true
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docKey = "${bankName}_${accountType}_${lastFourDigits}"
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(64)
            .ifBlank { UUID.randomUUID().toString().take(16) }

        val docRef = db.collection("users").document(uid).collection("accounts").document(docKey)
        val payload = mapOf(
            "userId" to uid,
            "name" to name.trim().ifBlank { bankName }.take(100),
            "bankName" to bankName.trim().ifBlank { "Bank" }.take(100),
            "accountType" to if (accountType in setOf("BANK", "CREDIT_CARD", "WALLET", "CASH", "UPI")) accountType else "BANK",
            "lastFourDigits" to lastFourDigits.trim().take(10),
            "balance" to balance,
            "creditLimit" to creditLimit.coerceAtLeast(0.0),
            "isSharedVisible" to isSharedVisible,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteAccount(accountId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("accounts").document(accountId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    private fun generateJoinCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    private fun logAndRethrow(error: Throwable, op: OperationType, path: String): Nothing {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception) ?: RuntimeException(error)
        handleFirestoreError(exceptionToLog, op, path)
        throw error
    }
}
