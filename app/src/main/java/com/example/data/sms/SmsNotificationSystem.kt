package com.example.data.sms

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.LocalAccountSessionManager
import com.example.data.model.ExpenseCategories
import com.example.data.repository.OneFamilyRepository
import com.example.ui.theme.categoryAccentColorArgb
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.ritesh.parser.core.TransactionType
import java.text.NumberFormat
import java.util.Collections
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object TransactionNotificationHelper {
    const val CHANNEL_ID = "onefamily_realtime_transactions"
    const val CHANNEL_ID_FCM_FAMILY = "onefamily_fcm_family_push"
    const val CHANNEL_ID_FCM_BUDGET = "onefamily_fcm_budget_push"
    const val EXTRA_EDIT_EXPENSE_ID = "extra_edit_expense_id"
    const val EXTRA_EDIT_HOUSEHOLD_ID = "extra_edit_household_id"
    const val EXTRA_EDIT_VISIBILITY = "extra_edit_visibility"
    const val EXTRA_NEW_CATEGORY = "extra_new_category"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val extraTitle = "extra_tx_title"
    const val extraAmount = "extra_tx_amount"

    private val incomingSmsMutex = Mutex()
    private val notifiedSignatures: MutableSet<String> =
        Collections.synchronizedSet(mutableSetOf<String>())
    private val notifiedBodyTimestamps = java.util.concurrent.ConcurrentHashMap<String, Long>()
    private val notifiedBudgetKeys: MutableSet<String> =
        Collections.synchronizedSet(mutableSetOf<String>())

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val smsChannel = NotificationChannel(
                CHANNEL_ID,
                "Real-Time Expense & Category Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant alerts for new SMS transactions with quick category editing"
                enableVibration(true)
            }
            val fcmFamilyChannel = NotificationChannel(
                CHANNEL_ID_FCM_FAMILY,
                "FCM Family Transaction Push Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant FCM push alerts when a family member logs a shared transaction"
                enableVibration(true)
            }
            val fcmBudgetChannel = NotificationChannel(
                CHANNEL_ID_FCM_BUDGET,
                "FCM Budget Threshold Push Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant FCM push alerts when a category or household budget nears or exceeds its threshold"
                enableVibration(true)
            }
            manager?.createNotificationChannel(smsChannel)
            manager?.createNotificationChannel(fcmFamilyChannel)
            manager?.createNotificationChannel(fcmBudgetChannel)
        }
    }

    @DrawableRes
    fun categoryNotificationIconRes(category: String): Int {
        val normalized = ExpenseCategories.normalizeCategory(category).lowercase()
        return when {
            normalized == "unknown" -> R.drawable.ic_cat_unknown
            normalized.contains("grocer") -> R.drawable.ic_cat_groceries
            normalized.contains("food") || normalized.contains("dining") -> R.drawable.ic_cat_food
            normalized.contains("rent") || normalized.contains("housing") -> R.drawable.ic_cat_housing
            normalized.contains("bill") || normalized.contains("util") -> R.drawable.ic_cat_utilities
            normalized.contains("fuel") || normalized.contains("travel") -> R.drawable.ic_cat_travel
            normalized.contains("shop") -> R.drawable.ic_cat_shopping
            normalized.contains("health") || normalized.contains("fitness") -> R.drawable.ic_cat_health
            normalized.contains("entertain") -> R.drawable.ic_cat_entertainment
            normalized.contains("invest") -> R.drawable.ic_cat_investments
            normalized.contains("salary") || normalized.contains("income") -> R.drawable.ic_cat_income
            else -> R.drawable.ic_cat_custom
        }
    }

    fun buildCategoryLargeIconBitmap(context: Context, category: String): Bitmap? {
        return runCatching {
            val sizePx = (48f * context.resources.displayMetrics.density).toInt().coerceIn(48, 192)
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val accentColor = categoryAccentColorArgb(category)

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                style = Paint.Style.FILL
            }
            val radius = sizePx / 2f
            canvas.drawCircle(radius, radius, radius, bgPaint)

            val rawDrawable = ContextCompat.getDrawable(context, categoryNotificationIconRes(category))
            if (rawDrawable != null) {
                val wrapped = DrawableCompat.wrap(rawDrawable.mutate())
                DrawableCompat.setTint(wrapped, android.graphics.Color.WHITE)
                val pad = (sizePx * 0.24f).toInt()
                wrapped.setBounds(pad, pad, sizePx - pad, sizePx - pad)
                wrapped.draw(canvas)
            }
            bitmap
        }.getOrNull()
    }

    private fun sanitizeSummaryField(raw: String, fallback: String): String {
        val singleLine = raw
            .lineSequence()
            .firstOrNull()
            .orEmpty()
            .replace(Regex("\\s+"), " ")
            .trim()
        return singleLine.ifBlank { fallback }.take(60)
    }

    fun showNewTransactionNotification(
        context: Context,
        expenseId: String,
        householdId: String,
        visibility: String,
        title: String,
        amount: Double,
        currentCategory: String,
        currencySymbol: String = "₹",
        paymentSummary: String = "",
        transactionType: String = "EXPENSE"
    ) {
        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val notificationId = expenseId.hashCode()
        val cleanCategory = ExpenseCategories.normalizeCategory(currentCategory)
        val cleanMerchant = sanitizeSummaryField(title, cleanCategory)
        val formattedAmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
            maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        }.format(amount)
        val amountDisplay = if (transactionType.equals("INCOME", ignoreCase = true)) {
            "+$currencySymbol$formattedAmt"
        } else {
            "$currencySymbol$formattedAmt"
        }

        // Tap notification -> Open MainActivity directly into the Edit Transaction / Category dialog
        val openEditIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_EDIT_EXPENSE_ID, expenseId)
            putExtra(EXTRA_EDIT_HOUSEHOLD_ID, householdId)
            putExtra(EXTRA_EDIT_VISIBILITY, visibility)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openEditIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val quickCategories = listOf(
            "Food & Dining",
            "Groceries",
            "Shopping",
            "Bills & Utilities",
            "Fuel & Travel"
        ).filterNot { it.equals(cleanCategory, ignoreCase = true) }.take(3)

        val scopeShort = if (visibility == "SHARED" && householdId.isNotBlank()) "Household" else "Personal"
        val cleanPayment = sanitizeSummaryField(paymentSummary, "")
        val collapsedSummary = buildString {
            append(cleanCategory)
            if (cleanPayment.isNotBlank()) {
                append(" • ")
                append(cleanPayment)
            }
            append(" • ")
            append(scopeShort)
        }
        val expandedSummary = buildString {
            append("Merchant: ").append(cleanMerchant)
            append("\nCategory: ").append(cleanCategory)
            append(" • Amount: ").append(amountDisplay)
            if (cleanPayment.isNotBlank()) {
                append("\nAccount: ").append(cleanPayment).append(" (").append(scopeShort).append(")")
            } else {
                append(" (").append(scopeShort).append(")")
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(categoryNotificationIconRes(cleanCategory))
            .setLargeIcon(buildCategoryLargeIconBitmap(context, cleanCategory))
            .setColor(categoryAccentColorArgb(cleanCategory))
            .setContentTitle("$cleanMerchant • $amountDisplay")
            .setContentText(collapsedSummary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedSummary))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        quickCategories.forEachIndexed { index, targetCategory ->
            val actionIntent = Intent(context, NotificationCategoryActionReceiver::class.java).apply {
                putExtra(EXTRA_EDIT_EXPENSE_ID, expenseId)
                putExtra(EXTRA_EDIT_HOUSEHOLD_ID, householdId)
                putExtra(EXTRA_EDIT_VISIBILITY, visibility)
                putExtra(EXTRA_NEW_CATEGORY, targetCategory)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(extraTitle, cleanMerchant)
                putExtra(extraAmount, amountDisplay)
            }
            val actionPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + index + 1,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, targetCategory, actionPendingIntent)
        }

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }

    /**
     * Firebase Cloud Messaging (FCM) Push Alert when a family member logs a new shared transaction.
     */
    fun showFamilyTransactionPushNotification(
        context: Context,
        expenseId: String,
        householdId: String,
        householdName: String,
        memberName: String,
        title: String,
        amount: Double,
        category: String,
        currencySymbol: String = "₹"
    ) {
        LocalAccountSessionManager.ensureInitialized(context.applicationContext)
        val fcmSettings = LocalAccountSessionManager.fcmPushSettings.value
        if (!fcmSettings.familyTransactionAlertsEnabled) return

        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val cleanCategory = ExpenseCategories.normalizeCategory(category)
        val cleanMerchant = sanitizeSummaryField(title, cleanCategory)
        val formattedAmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
            maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        }.format(amount)

        val openEditIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_EDIT_EXPENSE_ID, expenseId)
            putExtra(EXTRA_EDIT_HOUSEHOLD_ID, householdId)
            putExtra(EXTRA_EDIT_VISIBILITY, "SHARED")
        }
        val notificationId = ("fcm_family_$expenseId").hashCode()
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openEditIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cleanHousehold = sanitizeSummaryField(householdName, "Family Home")
        val cleanMember = sanitizeSummaryField(memberName, "Family Member")
        val summaryLine = "$cleanCategory • $cleanMember ($cleanHousehold)"
        val expandedSummary = "Merchant: $cleanMerchant\nCategory: $cleanCategory • Amount: $currencySymbol$formattedAmt\nBy: $cleanMember ($cleanHousehold)"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_FCM_FAMILY)
            .setSmallIcon(categoryNotificationIconRes(cleanCategory))
            .setLargeIcon(buildCategoryLargeIconBitmap(context, cleanCategory))
            .setColor(categoryAccentColorArgb(cleanCategory))
            .setContentTitle("$cleanMerchant • $currencySymbol$formattedAmt")
            .setContentText(summaryLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedSummary))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }

    /**
     * Firebase Cloud Messaging (FCM) Push Alert when a shared category budget or household monthly
     * budget nears or exceeds its configured alert threshold percentage.
     */
    fun showBudgetThresholdPushNotification(
        context: Context,
        categoryOrScopeName: String,
        spentAmount: Double,
        limitAmount: Double,
        thresholdPercent: Int,
        currencySymbol: String = "₹",
        forceNotify: Boolean = false
    ) {
        LocalAccountSessionManager.ensureInitialized(context.applicationContext)
        val fcmSettings = LocalAccountSessionManager.fcmPushSettings.value
        if (!fcmSettings.budgetThresholdAlertsEnabled && !forceNotify) return
        if (limitAmount <= 0.0) return

        val usedPercent = ((spentAmount / limitAmount) * 100.0).toInt()
        if (usedPercent < thresholdPercent && !forceNotify) return

        val bucket = if (usedPercent >= 100) "100" else thresholdPercent.toString()
        val dedupeKey = "${categoryOrScopeName.lowercase()}_$bucket"
        if (!forceNotify && !notifiedBudgetKeys.add(dedupeKey)) return

        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val numFmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
            maximumFractionDigits = 0
        }
        val spentStr = "$currencySymbol${numFmt.format(spentAmount)}"
        val limitStr = "$currencySymbol${numFmt.format(limitAmount)}"
        val headline = if (usedPercent >= 100) {
            "$categoryOrScopeName • Budget Exceeded ($usedPercent%)"
        } else {
            "$categoryOrScopeName • $usedPercent% Budget Used"
        }
        val bodyText = "$spentStr of $limitStr monthly limit"

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val notificationId = ("fcm_budget_$dedupeKey").hashCode()
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_FCM_BUDGET)
            .setSmallIcon(categoryNotificationIconRes(categoryOrScopeName))
            .setLargeIcon(buildCategoryLargeIconBitmap(context, categoryOrScopeName))
            .setColor(categoryAccentColorArgb(categoryOrScopeName))
            .setContentTitle(headline)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }

    /**
     * Shared processor used by both `IncomingSmsBroadcastReceiver` (`SMS_RECEIVED`) and
     * `SmsInboxContentObserver` (`content://sms`) to deduplicate, save, and notify when a new
     * bank/UPI SMS arrives.
     */
    suspend fun processIncomingSmsCandidateAndNotify(
        context: Context,
        candidate: SmsCandidate
    ) = incomingSmsMutex.withLock {
        val appContext = context.applicationContext
        LocalAccountSessionManager.ensureInitialized(appContext)
        val firebaseUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val localUser = LocalAccountSessionManager.getCurrentUser(appContext)
        val activeUid = firebaseUser?.uid ?: localUser?.uid
        if (activeUid.isNullOrBlank()) return@withLock

        val contentKey = SmsInboxReader.buildSmsContentKey(
            body = candidate.parsed.smsBody,
            timestamp = candidate.parsed.timestamp
        )
        val adjacentKeys = SmsInboxReader.buildSmsAdjacentContentKeys(
            body = candidate.parsed.smsBody,
            timestamp = candidate.parsed.timestamp
        )
        val scopedSig = "$activeUid|${candidate.id}"
        val scopedContentKey = "$activeUid|$contentKey"
        val scopedAdjacentKeys = adjacentKeys.map { "$activeUid|$it" }

        val normalizedBody = SmsInboxReader.normalizeSmsBody(candidate.parsed.smsBody)
        val bodyIdentityKey = if (normalizedBody.length >= 15) {
            "$activeUid|${candidate.parsed.type.name}|${String.format(Locale.US, "%.2f", candidate.parsed.amount.toDouble())}|$normalizedBody"
        } else {
            ""
        }

        if (scopedSig in notifiedSignatures ||
            scopedContentKey in notifiedSignatures ||
            scopedAdjacentKeys.any { it in notifiedSignatures }
        ) return@withLock

        if (bodyIdentityKey.isNotEmpty()) {
            val prevTs = notifiedBodyTimestamps[bodyIdentityKey]
            if (prevTs != null && kotlin.math.abs(candidate.parsed.timestamp - prevTs) <= SmsInboxReader.SLIDING_DEDUP_WINDOW_MS) {
                return@withLock
            }
        }

        try {
            val repo = OneFamilyRepository(appContext)
            val profile = repo.getUserProfileOnce()
            val household = profile?.activeHouseholdId?.takeIf { it.isNotBlank() }?.let {
                repo.getHouseholdOnce(it)
            }

            // Check if this SMS (or its adjacent content key / linked signature) already exists in the ledger
            val existingSignatures = repo.getAllExistingSmsSignatures(household?.id)
            if (candidate.id in existingSignatures ||
                contentKey in existingSignatures ||
                adjacentKeys.any { it in existingSignatures }
            ) {
                notifiedSignatures.add(scopedSig)
                notifiedSignatures.add(scopedContentKey)
                notifiedSignatures.addAll(scopedAdjacentKeys)
                if (bodyIdentityKey.isNotEmpty()) {
                    notifiedBodyTimestamps[bodyIdentityKey] = candidate.parsed.timestamp
                }
                return@withLock
            }

            notifiedSignatures.add(scopedSig)
            notifiedSignatures.add(scopedContentKey)
            notifiedSignatures.addAll(scopedAdjacentKeys)
            if (bodyIdentityKey.isNotEmpty()) {
                notifiedBodyTimestamps[bodyIdentityKey] = candidate.parsed.timestamp
            }

            val parsed = candidate.parsed
            val isIncome = parsed.type == TransactionType.INCOME
            val merchantTitle = parsed.merchant?.takeIf { it.isNotBlank() }
                ?: "${parsed.bankName} ${parsed.type.name.lowercase().replaceFirstChar { it.uppercase() }}"
            val effectiveShared = household != null
            val docId = "sms_${candidate.id}"
            val memberDisplayName = firebaseUser?.displayName ?: localUser?.displayName ?: profile?.displayName ?: "Member"
            val currencySymbol = profile?.currencySymbol ?: "₹"

            val savedResult = repo.addExpense(
                userName = memberDisplayName,
                household = household,
                title = merchantTitle,
                amount = parsed.amount.toDouble(),
                type = if (isIncome) "INCOME" else "EXPENSE",
                category = candidate.suggestedCategory,
                accountName = candidate.suggestedAccountName,
                paymentMode = candidate.suggestedPaymentMode,
                tags = listOf("sms-auto", parsed.bankName.lowercase().replace(" ", "-")),
                notes = "[${parsed.sender}] ${parsed.smsBody}".take(500),
                isShared = effectiveShared,
                smsSignature = candidate.id,
                referenceNumber = parsed.reference.orEmpty(),
                transactionDateMillis = parsed.timestamp,
                customDocId = docId
            )

            val expenseIdToNotify = savedResult.getOrDefault(docId)
            val paymentSummary = buildString {
                if (candidate.suggestedAccountName.isNotBlank()) append(candidate.suggestedAccountName)
                if (candidate.suggestedPaymentMode.isNotBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append(candidate.suggestedPaymentMode)
                }
            }
            showNewTransactionNotification(
                context = appContext,
                expenseId = expenseIdToNotify,
                householdId = household?.id.orEmpty(),
                visibility = if (effectiveShared) "SHARED" else "PERSONAL",
                title = merchantTitle,
                amount = parsed.amount.toDouble(),
                currentCategory = candidate.suggestedCategory,
                currencySymbol = currencySymbol,
                paymentSummary = paymentSummary,
                transactionType = if (isIncome) "INCOME" else "EXPENSE"
            )

            if (household != null) {
                showFamilyTransactionPushNotification(
                    context = appContext,
                    expenseId = expenseIdToNotify,
                    householdId = household.id,
                    householdName = household.name,
                    memberName = memberDisplayName,
                    title = merchantTitle,
                    amount = parsed.amount.toDouble(),
                    category = candidate.suggestedCategory,
                    currencySymbol = currencySymbol
                )
            }
        } catch (e: Exception) {
            LocalAccountSessionManager.recordDiagnostic(
                context = appContext,
                subsystem = "SMS_REALTIME_SYNC",
                severity = "NON_FATAL",
                message = "Guarded incoming SMS processing: ${e.javaClass.simpleName}: ${e.localizedMessage.orEmpty()}"
            )
        }
    }
}

/**
 * Handles clicks on the quick category action buttons inside the Android notification
 * and updates the transaction category in real time.
 */
class NotificationCategoryActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val expenseId = intent.getStringExtra(TransactionNotificationHelper.EXTRA_EDIT_EXPENSE_ID).orEmpty()
        val householdId = intent.getStringExtra(TransactionNotificationHelper.EXTRA_EDIT_HOUSEHOLD_ID).orEmpty()
        val visibility = intent.getStringExtra(TransactionNotificationHelper.EXTRA_EDIT_VISIBILITY).orEmpty()
        val newCategory = intent.getStringExtra(TransactionNotificationHelper.EXTRA_NEW_CATEGORY).orEmpty()
        val notificationId = intent.getIntExtra(TransactionNotificationHelper.EXTRA_NOTIFICATION_ID, 0)
        val title = intent.getStringExtra(TransactionNotificationHelper.extraTitle).orEmpty()
        val amountText = intent.getStringExtra(TransactionNotificationHelper.extraAmount).orEmpty()

        if (expenseId.isBlank() || newCategory.isBlank()) return
        LocalAccountSessionManager.ensureInitialized(context.applicationContext)
        val hasSession = runCatching { Firebase.auth.currentUser }.getOrNull() != null ||
            LocalAccountSessionManager.getCurrentUser(context) != null
        if (!hasSession) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repo = OneFamilyRepository(context.applicationContext)
                repo.updateExpenseCategoryById(
                    expenseId = expenseId,
                    householdId = householdId,
                    visibility = visibility,
                    newCategory = newCategory
                )

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                ) {
                    val normalizedNew = ExpenseCategories.normalizeCategory(newCategory)
                    val updatedBuilder = NotificationCompat.Builder(context, TransactionNotificationHelper.CHANNEL_ID)
                        .setSmallIcon(TransactionNotificationHelper.categoryNotificationIconRes(normalizedNew))
                        .setLargeIcon(TransactionNotificationHelper.buildCategoryLargeIconBitmap(context, normalizedNew))
                        .setColor(categoryAccentColorArgb(normalizedNew))
                        .setContentTitle("$title • $amountText")
                        .setContentText("Category: $normalizedNew ✓")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setTimeoutAfter(4000L)
                    runCatching {
                        NotificationManagerCompat.from(context).notify(notificationId, updatedBuilder.build())
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

/**
 * Listens for real-time incoming SMS messages (`android.provider.Telephony.SMS_RECEIVED`),
 * parses bank/UPI alerts, saves them, and posts an interactive notification.
 */
class IncomingSmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val appContext = context.applicationContext
        LocalAccountSessionManager.ensureInitialized(appContext)
        val firebaseUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val localUser = LocalAccountSessionManager.getCurrentUser(appContext)
        if (firebaseUser == null && localUser == null) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages.firstOrNull()?.displayOriginatingAddress.orEmpty()
        val fullBody = messages.joinToString(separator = "") { it.displayMessageBody.orEmpty() }
        val timestamp = messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()

        if (sender.isBlank() || fullBody.isBlank()) return

        val reader = SmsInboxReader(appContext)
        val candidate = reader.parseRawSms(
            sender = sender,
            body = fullBody,
            timestamp = timestamp,
            defaultShared = true
        ) ?: return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                TransactionNotificationHelper.processIncomingSmsCandidateAndNotify(
                    context = appContext,
                    candidate = candidate
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}

/**
 * Observes `content://sms` for newly inserted SMS messages while the app is active so that even
 * on OEMs where the default messaging app intercepts `SMS_RECEIVED`, newly received bank/UPI
 * SMS messages are immediately parsed, saved, and notified.
 */
class SmsInboxContentObserver(
    private val context: Context
) : ContentObserver(Handler(Looper.getMainLooper())) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val observerMutex = Mutex()
    private var lastProcessedSmsDateMillis: Long = System.currentTimeMillis()

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        val hasReadSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasReadSms) return

        scope.launch {
            observerMutex.withLock {
                runCatching {
                    val projection = arrayOf(
                        Telephony.Sms.ADDRESS,
                        Telephony.Sms.BODY,
                        Telephony.Sms.DATE,
                        Telephony.Sms.TYPE
                    )
                    context.contentResolver.query(
                        Telephony.Sms.Inbox.CONTENT_URI,
                        projection,
                        "${Telephony.Sms.DATE} > ?",
                        arrayOf(lastProcessedSmsDateMillis.toString()),
                        "${Telephony.Sms.DATE} DESC LIMIT 3"
                    )?.use { cursor ->
                        val addrIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                        val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                        val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
                        if (addrIdx < 0 || bodyIdx < 0 || dateIdx < 0) return@use

                        val reader = SmsInboxReader(context.applicationContext)
                        var newestSeen = lastProcessedSmsDateMillis
                        while (cursor.moveToNext()) {
                            val sender = cursor.getString(addrIdx).orEmpty()
                            val body = cursor.getString(bodyIdx).orEmpty()
                            val dateMillis = cursor.getLong(dateIdx)
                            if (dateMillis > newestSeen) {
                                newestSeen = dateMillis
                            }
                            if (sender.isNotBlank() && body.isNotBlank()) {
                                val candidate = reader.parseRawSms(
                                    sender = sender,
                                    body = body,
                                    timestamp = dateMillis,
                                    defaultShared = true
                                )
                                if (candidate != null) {
                                    TransactionNotificationHelper.processIncomingSmsCandidateAndNotify(
                                        context = context.applicationContext,
                                        candidate = candidate
                                    )
                                }
                            }
                        }
                        lastProcessedSmsDateMillis = newestSeen
                    }
                }
            }
        }
    }
}
