package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.local.LocalAccountDataStore
import com.example.data.local.LocalAccountSessionManager
import com.example.data.model.CategoryBudget
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FinancialAccount
import com.example.data.model.Household
import com.example.data.model.HouseholdCodeIndex
import com.example.data.model.UserProfile
import com.example.data.sms.SmsCandidate
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import com.ritesh.parser.core.TransactionType
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AxioRepository(private val dbProvider: () -> FirebaseFirestore) {

    constructor(db: FirebaseFirestore) : this({ db })

    constructor(context: Context) : this({
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    }) {
        LocalAccountSessionManager.ensureInitialized(context.applicationContext)
    }

    private val db: FirebaseFirestore
        get() = dbProvider()

    private val currentFirebaseUser
        get() = runCatching { Firebase.auth.currentUser }.getOrNull()

    private fun isLocalMode(userIdHint: String? = null): Boolean {
        if (userIdHint != null && userIdHint.startsWith("local_user_")) return true
        if (LocalAccountSessionManager.getCurrentUser() != null) return true
        return currentFirebaseUser == null
    }

    private fun requireLocalUserId(): String {
        return LocalAccountSessionManager.getCurrentUser()?.uid
            ?: throw IllegalStateException("No active local or Google user session.")
    }

    private fun requireUserId(): String {
        return currentFirebaseUser?.uid
            ?: LocalAccountSessionManager.getCurrentUser()?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- USER PROFILE ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        if (isLocalMode(userId)) {
            return LocalAccountDataStore.observeUserProfile(userId)
        }
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

    suspend fun getUserProfileOnce(): UserProfile? {
        if (isLocalMode()) {
            val uid = LocalAccountSessionManager.getCurrentUser()?.uid ?: return null
            return LocalAccountDataStore.getUserProfileOnce(uid)
        }
        val uid = currentFirebaseUser?.uid ?: return null
        return runCatching {
            val snap = db.collection("users").document(uid).get().await()
            if (snap.exists()) snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) else null
        }.getOrNull()
    }

    suspend fun getHouseholdOnce(householdId: String): Household? {
        if (householdId.isBlank()) return null
        if (isLocalMode() || householdId.startsWith("local_")) {
            return LocalAccountDataStore.getHouseholdOnce(householdId)
        }
        return runCatching {
            val snap = db.collection("households").document(householdId).get().await()
            if (snap.exists()) snap.toObject(Household::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) else null
        }.getOrNull()
    }

    suspend fun ensureUserProfile(
        displayName: String,
        email: String,
        photoUrl: String = ""
    ): Result<UserProfile> = runCatching {
        if (isLocalMode()) {
            val uid = requireLocalUserId()
            return@runCatching LocalAccountDataStore.ensureUserProfile(uid, displayName, email, photoUrl)
        }
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
                    "personalMonthlyBudget" to 0.0,
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
                    personalMonthlyBudget = 0.0,
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
        if (isLocalMode()) {
            val uid = requireLocalUserId()
            LocalAccountDataStore.updatePersonalBudgetSettings(uid, personalMonthlyBudget, currencySymbol)
            return@runCatching
        }
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

    suspend fun updateUserDisplayName(
        newDisplayName: String,
        household: Household?
    ): Result<Unit> = runCatching {
        val cleanName = newDisplayName.trim().ifBlank {
            throw IllegalArgumentException("Name cannot be empty.")
        }.take(100)

        if (isLocalMode()) {
            val uid = requireLocalUserId()
            LocalAccountDataStore.updateUserDisplayName(uid, cleanName, household)
            return@runCatching
        }

        val uid = requireUserId()
        val userDocRef = db.collection("users").document(uid)
        try {
            userDocRef.update(
                mapOf(
                    "displayName" to cleanName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (household != null && household.id.isNotBlank()) {
                val updatedNames = household.memberNames.toMutableMap().apply {
                    put(uid, cleanName)
                }
                db.collection("households").document(household.id).update(
                    mapOf(
                        "memberNames" to updatedNames,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userDocRef.path)
            throw e
        }
    }

    // --- HOUSEHOLD CREATION, LINKING & INVITES ---

    fun observeHousehold(householdId: String): Flow<Household?> {
        if (isLocalMode() || householdId.startsWith("local_")) {
            return LocalAccountDataStore.observeHousehold(householdId)
        }
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
        if (isLocalMode()) {
            return LocalAccountDataStore.observeEmailInvitedHouseholds(userEmail)
        }
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
        if (isLocalMode()) {
            val uid = requireLocalUserId()
            return@runCatching LocalAccountDataStore.createHousehold(
                uid = uid,
                name = name,
                monthlyBudgetLimit = monthlyBudgetLimit,
                currencySymbol = currencySymbol,
                userDisplayName = userDisplayName,
                userEmail = userEmail
            )
        }
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
                "monthlyBudgetLimit" to monthlyBudgetLimit.coerceAtLeast(0.0),
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
        if (isLocalMode()) {
            val uid = requireLocalUserId()
            return@runCatching LocalAccountDataStore.joinHouseholdByCode(
                uid = uid,
                rawJoinCode = rawJoinCode,
                userDisplayName = userDisplayName,
                userEmail = userEmail
            )
        }
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
        if (isLocalMode() || householdId.startsWith("local_")) {
            val uid = requireLocalUserId()
            return@runCatching LocalAccountDataStore.joinHouseholdById(
                uid = uid,
                householdId = householdId,
                userDisplayName = userDisplayName,
                userEmail = userEmail
            )
        }
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
        if (isLocalMode() || household.id.startsWith("local_")) {
            LocalAccountDataStore.inviteMemberByEmail(household, inviteeEmail)
            return@runCatching
        }
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
        if (isLocalMode() || householdId.startsWith("local_")) {
            LocalAccountDataStore.updateHouseholdSettings(householdId, name, monthlyBudgetLimit)
            return@runCatching
        }
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
        if (isLocalMode(userId)) {
            return LocalAccountDataStore.observePersonalExpenses(userId)
        }
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
        if (isLocalMode(userId) || householdId.startsWith("local_")) {
            return LocalAccountDataStore.observeSharedExpenses(householdId)
        }
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
        transactionDateMillis: Long = System.currentTimeMillis(),
        customDocId: String? = null
    ): Result<String> = runCatching {
        if (isLocalMode()) {
            val uid = requireLocalUserId()
            return@runCatching LocalAccountDataStore.addExpense(
                uid = uid,
                userName = userName,
                household = household,
                title = title,
                amount = amount,
                type = type,
                category = category,
                accountName = accountName,
                paymentMode = paymentMode,
                tags = tags,
                notes = notes,
                isShared = isShared,
                smsSignature = smsSignature,
                referenceNumber = referenceNumber,
                transactionDateMillis = transactionDateMillis,
                customDocId = customDocId
            )
        }
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

        val docRef = if (!customDocId.isNullOrBlank()) {
            collectionRef.document(customDocId)
        } else {
            collectionRef.document()
        }

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

    /**
     * Batch-imports all newly discovered SMS transactions in chronological order (`timestamp`).
     * Defaults to Shared Household when a household is linked, as configured by the user.
     */
    suspend fun batchImportSmsCandidates(
        candidates: List<SmsCandidate>,
        userName: String,
        household: Household?,
        shareByDefault: Boolean = true
    ): Result<Int> = runCatching {
        if (isLocalMode()) {
            val uid = requireLocalUserId()
            return@runCatching LocalAccountDataStore.batchImportSmsCandidates(
                uid = uid,
                candidates = candidates,
                userName = userName,
                household = household,
                shareByDefault = shareByDefault
            )
        }
        val uid = requireUserId()
        if (candidates.isEmpty()) return@runCatching 0

        val effectiveShared = shareByDefault && household != null
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

        // Query existing SMS signatures from personal & shared collections so we never overwrite existing/edited SMS receipts
        val existingServerSignatures = mutableSetOf<String>()
        runCatching {
            val pSnap = db.collection("users").document(uid).collection("personal_expenses").get().await()
            pSnap.documents.forEach { doc ->
                doc.getString("smsSignature")?.takeIf { it.isNotBlank() }?.let { existingServerSignatures.add(it) }
                existingServerSignatures.add(doc.id.removePrefix("sms_"))
            }
        }
        if (effectiveShared) {
            runCatching {
                val sSnap = db.collection("households").document(targetHouseholdId).collection("shared_expenses")
                    .whereArrayContains("memberUids", uid)
                    .get()
                    .await()
                sSnap.documents.forEach { doc ->
                    doc.getString("smsSignature")?.takeIf { it.isNotBlank() }?.let { existingServerSignatures.add(it) }
                    existingServerSignatures.add(doc.id.removePrefix("sms_"))
                }
            }
        }

        // Sort oldest to newest so transactions are added chronologically by SMS timestamp
        val sortedCandidates = candidates
            .filterNot { it.id in existingServerSignatures }
            .sortedBy { it.parsed.timestamp }
        if (sortedCandidates.isEmpty()) return@runCatching 0

        var importedCount = 0
        val accountsToUpsert = mutableMapOf<String, SmsCandidate>()

        for (chunk in sortedCandidates.chunked(350)) {
            val batch = db.batch()
            for (candidate in chunk) {
                val parsed = candidate.parsed
                val isIncome = parsed.type == TransactionType.INCOME
                val category = candidate.suggestedCategory.ifBlank { "General" }.take(60)
                val merchantTitle = (parsed.merchant?.takeIf { it.isNotBlank() }
                    ?: "${parsed.bankName} ${parsed.type.name.lowercase().replaceFirstChar { it.uppercase() }}").take(150)

                val docId = "sms_${candidate.id}"
                val docRef = collectionRef.document(docId)
                val tags = listOf("sms-auto", parsed.bankName.lowercase().replace(" ", "-").take(35))
                    .filter { it.isNotBlank() }
                    .distinct()

                val paymentMode = if (candidate.suggestedPaymentMode in setOf("UPI", "BANK", "CARD", "WALLET", "CASH")) {
                    candidate.suggestedPaymentMode
                } else {
                    "UPI"
                }

                LocalAccountDataStore.cacheSmsMessage(
                    signature = candidate.id,
                    sender = parsed.sender,
                    body = parsed.smsBody,
                    timestamp = parsed.timestamp
                )

                val payload = mapOf(
                    "userId" to uid,
                    "userName" to userName.ifBlank { "Me" }.take(100),
                    "householdId" to targetHouseholdId,
                    "title" to merchantTitle,
                    "amount" to parsed.amount.toDouble().coerceAtLeast(0.01),
                    "type" to if (isIncome) "INCOME" else "EXPENSE",
                    "category" to category,
                    "accountName" to candidate.suggestedAccountName.ifBlank { parsed.bankName }.take(100),
                    "paymentMode" to paymentMode,
                    "tags" to tags,
                    "notes" to "[${parsed.sender}] ${parsed.smsBody}".take(500),
                    "visibility" to if (effectiveShared) "SHARED" else "PERSONAL",
                    "smsSignature" to candidate.id.take(200),
                    "referenceNumber" to parsed.reference.orEmpty().take(100),
                    "transactionDateMillis" to parsed.timestamp.coerceAtLeast(1L),
                    "memberUids" to memberUids,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(docRef, payload)
                importedCount++

                if (parsed.balance != null || parsed.creditLimit != null || parsed.accountNumber != null) {
                    accountsToUpsert[candidate.suggestedAccountName] = candidate
                }
            }
            try {
                batch.commit().await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.WRITE, collectionRef.path)
                throw e
            }
        }

        // Upsert discovered accounts with latest balance/limit
        for ((_, candidate) in accountsToUpsert) {
            val parsed = candidate.parsed
            val accType = when {
                parsed.isFromCard -> "CREDIT_CARD"
                candidate.suggestedPaymentMode == "WALLET" -> "WALLET"
                else -> "BANK"
            }
            runCatching {
                upsertAccount(
                    name = candidate.suggestedAccountName,
                    bankName = parsed.bankName,
                    accountType = accType,
                    lastFourDigits = parsed.accountNumber.orEmpty(),
                    balance = parsed.balance?.toDouble() ?: 0.0,
                    creditLimit = parsed.creditLimit?.toDouble() ?: 0.0,
                    isSharedVisible = effectiveShared
                )
            }
        }

        importedCount
    }

    /**
     * Updates an existing transaction's details (merchant, amount, type, category, account,
     * paymentMode, tags, notes, date, and shared/personal visibility).
     */
    suspend fun updateExpense(
        existing: ExpenseTransaction,
        household: Household?,
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountName: String,
        paymentMode: String,
        tags: List<String>,
        notes: String,
        transactionDateMillis: Long,
        isShared: Boolean
    ): Result<Unit> = runCatching {
        if (isLocalMode(existing.userId)) {
            val uid = requireLocalUserId()
            LocalAccountDataStore.updateExpense(
                uid = uid,
                existing = existing,
                household = household,
                title = title,
                amount = amount,
                type = type,
                category = category,
                accountName = accountName,
                paymentMode = paymentMode,
                tags = tags,
                notes = notes,
                transactionDateMillis = transactionDateMillis,
                isShared = isShared
            )
            return@runCatching
        }
        val uid = requireUserId()
        val wantShared = isShared && household != null
        val wasShared = existing.visibility == "SHARED" && existing.householdId.isNotBlank()

        val cleanTags = tags.map { it.trim().removePrefix("#") }
            .filter { it.isNotBlank() }
            .distinct()
            .take(10)
            .map { it.take(40) }

        val validMode = if (paymentMode in setOf("UPI", "BANK", "CARD", "WALLET", "CASH")) paymentMode else "UPI"
        val validType = if (type == "INCOME") "INCOME" else "EXPENSE"

        // If moving between PERSONAL and SHARED collections, create in target and delete from source
        if (wantShared != wasShared) {
            addExpense(
                userName = existing.userName.ifBlank { "Me" },
                household = household,
                title = title,
                amount = amount,
                type = validType,
                category = category,
                accountName = accountName,
                paymentMode = validMode,
                tags = cleanTags,
                notes = notes,
                isShared = wantShared,
                smsSignature = existing.smsSignature,
                referenceNumber = existing.referenceNumber,
                transactionDateMillis = transactionDateMillis
            ).getOrThrow()
            deleteExpense(existing).getOrThrow()
            return@runCatching
        }

        val docRef = if (wasShared) {
            db.collection("households").document(existing.householdId)
                .collection("shared_expenses").document(existing.id)
        } else {
            db.collection("users").document(uid)
                .collection("personal_expenses").document(existing.id)
        }

        val updateMap = mapOf(
            "title" to title.trim().ifBlank { category }.take(150),
            "amount" to amount.coerceAtLeast(0.01),
            "type" to validType,
            "category" to category.trim().ifBlank { "General" }.take(60),
            "accountName" to accountName.trim().ifBlank { "Cash" }.take(100),
            "paymentMode" to validMode,
            "tags" to cleanTags,
            "notes" to notes.trim().take(500),
            "transactionDateMillis" to transactionDateMillis.coerceAtLeast(1L),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.update(updateMap).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    /**
     * Fast category update by document ID & scope — used by both quick category chips
     * and the real-time Android Notification action buttons.
     */
    suspend fun updateExpenseCategoryById(
        expenseId: String,
        householdId: String,
        visibility: String,
        newCategory: String
    ): Result<Unit> = runCatching {
        if (isLocalMode() || householdId.startsWith("local_")) {
            LocalAccountDataStore.updateExpenseCategoryById(expenseId, newCategory)
            return@runCatching
        }
        val uid = requireUserId()
        val docRef = if (visibility == "SHARED" && householdId.isNotBlank()) {
            db.collection("households").document(householdId)
                .collection("shared_expenses").document(expenseId)
        } else {
            db.collection("users").document(uid)
                .collection("personal_expenses").document(expenseId)
        }
        try {
            docRef.update(
                mapOf(
                    "category" to newCategory.trim().ifBlank { "General" }.take(60),
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

    suspend fun toggleExpensePrivacy(
        expense: ExpenseTransaction,
        household: Household
    ): Result<Unit> = runCatching {
        if (isLocalMode(expense.userId)) {
            val uid = requireLocalUserId()
            LocalAccountDataStore.toggleExpensePrivacy(uid, expense, household)
            return@runCatching
        }
        requireUserId()
        val movingToShared = expense.visibility == "PERSONAL"

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

        deleteExpense(expense).getOrThrow()
    }

    suspend fun deleteExpense(expense: ExpenseTransaction): Result<Unit> = runCatching {
        if (isLocalMode(expense.userId)) {
            LocalAccountDataStore.deleteExpense(expense.id)
            return@runCatching
        }
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
        if (isLocalMode(userId)) {
            return LocalAccountDataStore.observePersonalCategoryBudgets(userId)
        }
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
        if (isLocalMode(userId) || householdId.startsWith("local_")) {
            return LocalAccountDataStore.observeSharedCategoryBudgets(householdId)
        }
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
        if (isLocalMode()) {
            val uid = requireLocalUserId()
            return@runCatching LocalAccountDataStore.saveCategoryBudget(
                uid = uid,
                category = category,
                monthlyLimit = monthlyLimit,
                alertThresholdPercent = alertThresholdPercent,
                isShared = isShared,
                household = household
            )
        }
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
            "monthlyLimit" to monthlyLimit.coerceAtLeast(0.0),
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
        if (isLocalMode(budget.userId)) {
            LocalAccountDataStore.deleteCategoryBudget(budget.id)
            return@runCatching
        }
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
        if (isLocalMode(userId)) {
            return LocalAccountDataStore.observeAccounts(userId)
        }
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
        if (isLocalMode()) {
            val uid = requireLocalUserId()
            return@runCatching LocalAccountDataStore.upsertAccount(
                uid = uid,
                name = name,
                bankName = bankName,
                accountType = accountType,
                lastFourDigits = lastFourDigits,
                balance = balance,
                creditLimit = creditLimit,
                isSharedVisible = isSharedVisible
            )
        }
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
        if (isLocalMode()) {
            LocalAccountDataStore.deleteAccount(accountId)
            return@runCatching
        }
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
