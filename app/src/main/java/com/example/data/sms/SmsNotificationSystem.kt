package com.example.data.sms

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
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
import com.example.data.local.LocalAccountDataStore
import com.example.data.local.LocalAccountSessionManager
import com.example.data.model.ExpenseCategories
import com.example.data.model.NotificationSubCategory
import com.example.data.repository.OneFamilyRepository
import com.example.ui.theme.categoryAccentColorArgb
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.ritesh.parser.core.TransactionType
import java.text.NumberFormat
import java.util.Calendar
import java.util.Collections
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class SpendSummaryPeriod(
    val label: String,
    val subCategory: NotificationSubCategory
) {
    DAILY("Daily Spend Summary", NotificationSubCategory.DAILY_SUMMARY),
    WEEKLY("Weekly Spend Summary", NotificationSubCategory.WEEKLY_SUMMARY),
    MONTHLY("Monthly Spend Summary", NotificationSubCategory.MONTHLY_SUMMARY)
}

object TransactionNotificationHelper {
    const val CHANNEL_GROUP_ID = "onefamily_alerts_group"
    const val CHANNEL_ID = "onefamily_realtime_transactions"
    const val CHANNEL_ID_SMS_INCOME = "onefamily_sms_income_alerts"
    const val CHANNEL_ID_FCM_FAMILY = "onefamily_fcm_family_push"
    const val CHANNEL_ID_FCM_BUDGET = "onefamily_fcm_budget_push"
    const val CHANNEL_ID_CREDIT_LIMIT = "onefamily_credit_card_limit_alerts"
    const val CHANNEL_ID_SPEND_SUMMARY = "onefamily_spend_summary_alerts"
    const val ACTION_OPEN_EDIT_TRANSACTION = "com.example.action.OPEN_EDIT_TRANSACTION"
    const val EXTRA_EDIT_EXPENSE_ID = "extra_edit_expense_id"
    const val EXTRA_EDIT_HOUSEHOLD_ID = "extra_edit_household_id"
    const val EXTRA_EDIT_VISIBILITY = "extra_edit_visibility"
    const val EXTRA_EDIT_CURRENT_CATEGORY = "extra_edit_current_category"
    const val EXTRA_NEW_CATEGORY = "extra_new_category"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val extraTitle = "extra_tx_title"
    const val extraAmount = "extra_tx_amount"

    private const val PREFS_LIMIT_ALERTS = "onefamily_limit_alert_history_prefs"
    private const val KEY_NOTIFIED_LIMIT_ALERTS = "notified_limit_alert_keys"

    private val incomingSmsMutex = Mutex()
    private val limitAlertsLock = Any()
    private val notifiedSignatures: MutableSet<String> =
        Collections.synchronizedSet(mutableSetOf<String>())
    private val notifiedBodyTimestamps = java.util.concurrent.ConcurrentHashMap<String, Long>()

    private fun limitAlertPrefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_LIMIT_ALERTS, Context.MODE_PRIVATE)

    private fun resolveActiveAlertUid(context: Context): String {
        val appContext = context.applicationContext
        val localUid = LocalAccountSessionManager.getCurrentUser(appContext)?.uid
        val firebaseUid = runCatching { Firebase.auth.currentUser?.uid }.getOrNull()
        return (localUid ?: firebaseUid).orEmpty().ifBlank { "default" }
    }

    private fun currentYearMonthStamp(): String {
        val cal = Calendar.getInstance()
        return String.format(
            Locale.US,
            "%04d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1
        )
    }

    private fun isLimitAlertAlreadySent(context: Context, key: String): Boolean = synchronized(limitAlertsLock) {
        val existing = limitAlertPrefs(context).getStringSet(KEY_NOTIFIED_LIMIT_ALERTS, emptySet()) ?: emptySet()
        key in existing
    }

    private fun markLimitAlertsSent(context: Context, vararg keys: String) = synchronized(limitAlertsLock) {
        val prefs = limitAlertPrefs(context)
        val updated = (prefs.getStringSet(KEY_NOTIFIED_LIMIT_ALERTS, emptySet()) ?: emptySet()).toMutableSet()
        var changed = false
        for (k in keys) {
            if (k.isNotBlank() && updated.add(k)) {
                changed = true
            }
        }
        if (changed) {
            prefs.edit().putStringSet(KEY_NOTIFIED_LIMIT_ALERTS, updated).commit()
        }
    }

    fun clearLimitNotificationHistoryForUser(context: Context, uid: String) = synchronized(limitAlertsLock) {
        val prefs = limitAlertPrefs(context)
        if (uid.isBlank()) {
            prefs.edit().remove(KEY_NOTIFIED_LIMIT_ALERTS).commit()
            return@synchronized
        }
        val existing = prefs.getStringSet(KEY_NOTIFIED_LIMIT_ALERTS, emptySet()) ?: emptySet()
        val token = "|$uid|"
        val remaining = existing.filterNot { it.contains(token) }.toSet()
        if (remaining.size != existing.size) {
            prefs.edit().putStringSet(KEY_NOTIFIED_LIMIT_ALERTS, remaining).commit()
        }
    }

    fun clearAllLimitNotificationHistory(context: Context) = synchronized(limitAlertsLock) {
        limitAlertPrefs(context).edit().remove(KEY_NOTIFIED_LIMIT_ALERTS).commit()
    }

    fun createOpenEditTransactionPendingIntent(
        context: Context,
        expenseId: String,
        householdId: String = "",
        visibility: String = "",
        requestCode: Int = expenseId.hashCode()
    ): PendingIntent {
        val openEditIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_EDIT_TRANSACTION
            data = Uri.parse("onefamily://edit-transaction/${Uri.encode(expenseId)}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_EDIT_EXPENSE_ID, expenseId)
            putExtra(EXTRA_EDIT_HOUSEHOLD_ID, householdId)
            putExtra(EXTRA_EDIT_VISIBILITY, visibility)
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            openEditIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun clearNotifiedStateForUser(uid: String) {
        if (uid.isBlank()) {
            notifiedSignatures.clear()
            notifiedBodyTimestamps.clear()
            return
        }
        val prefix = "$uid|"
        synchronized(notifiedSignatures) {
            notifiedSignatures.removeAll { it.startsWith(prefix) }
        }
        notifiedBodyTimestamps.keys.removeIf { it.startsWith(prefix) }
    }

    fun markSignatureNotified(uid: String, candidate: SmsCandidate) {
        if (uid.isBlank()) return
        val contentKey = SmsInboxReader.buildSmsContentKey(
            body = candidate.parsed.smsBody,
            timestamp = candidate.parsed.timestamp
        )
        val adjacentKeys = SmsInboxReader.buildSmsAdjacentContentKeys(
            body = candidate.parsed.smsBody,
            timestamp = candidate.parsed.timestamp
        )
        notifiedSignatures.add("$uid|${candidate.id}")
        notifiedSignatures.add("$uid|$contentKey")
        adjacentKeys.forEach { notifiedSignatures.add("$uid|$it") }
        val normalizedBody = SmsInboxReader.normalizeSmsBody(candidate.parsed.smsBody)
        if (normalizedBody.length >= 15) {
            val bodyIdentityKey = "$uid|${candidate.parsed.type.name}|${String.format(Locale.US, "%.2f", candidate.parsed.amount.toDouble())}|$normalizedBody"
            notifiedBodyTimestamps[bodyIdentityKey] = candidate.parsed.timestamp
        }
    }

    fun isCandidateAlreadyNotified(uid: String, candidate: SmsCandidate): Boolean {
        if (uid.isBlank()) return false
        val contentKey = SmsInboxReader.buildSmsContentKey(
            body = candidate.parsed.smsBody,
            timestamp = candidate.parsed.timestamp
        )
        val adjacentKeys = SmsInboxReader.buildSmsAdjacentContentKeys(
            body = candidate.parsed.smsBody,
            timestamp = candidate.parsed.timestamp
        )
        if ("$uid|${candidate.id}" in notifiedSignatures ||
            "$uid|$contentKey" in notifiedSignatures ||
            adjacentKeys.any { "$uid|$it" in notifiedSignatures }
        ) {
            return true
        }
        val normalizedBody = SmsInboxReader.normalizeSmsBody(candidate.parsed.smsBody)
        if (normalizedBody.length >= 15) {
            val bodyIdentityKey = "$uid|${candidate.parsed.type.name}|${String.format(Locale.US, "%.2f", candidate.parsed.amount.toDouble())}|$normalizedBody"
            val prevTs = notifiedBodyTimestamps[bodyIdentityKey]
            if (prevTs != null && kotlin.math.abs(candidate.parsed.timestamp - prevTs) <= SmsInboxReader.SLIDING_DEDUP_WINDOW_MS) {
                return true
            }
        }
        return false
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannelGroup(
                NotificationChannelGroup(CHANNEL_GROUP_ID, "Alerts")
            )
            val smsExpenseChannel = NotificationChannel(
                CHANNEL_ID,
                "Expenses",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                group = CHANNEL_GROUP_ID
                description = "Bank & UPI debit alerts with quick category editing"
                enableVibration(true)
            }
            val smsIncomeChannel = NotificationChannel(
                CHANNEL_ID_SMS_INCOME,
                "Income",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                group = CHANNEL_GROUP_ID
                description = "Salary, cashback & credit SMS alerts"
                enableVibration(true)
            }
            val fcmFamilyChannel = NotificationChannel(
                CHANNEL_ID_FCM_FAMILY,
                "Family Sync",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                group = CHANNEL_GROUP_ID
                description = "Shared transactions logged by family members"
                enableVibration(true)
            }
            val fcmBudgetChannel = NotificationChannel(
                CHANNEL_ID_FCM_BUDGET,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                group = CHANNEL_GROUP_ID
                description = "Category or monthly budget spend limit warnings"
                enableVibration(true)
            }
            val creditLimitChannel = NotificationChannel(
                CHANNEL_ID_CREDIT_LIMIT,
                "Card Limits",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                group = CHANNEL_GROUP_ID
                description = "Credit card spend limit threshold warnings"
                enableVibration(true)
            }
            val spendSummaryChannel = NotificationChannel(
                CHANNEL_ID_SPEND_SUMMARY,
                "Spend Summaries",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                group = CHANNEL_GROUP_ID
                description = "Daily, weekly, and monthly total spend summary notifications"
                enableVibration(true)
            }
            manager?.createNotificationChannels(
                listOf(
                    smsExpenseChannel,
                    smsIncomeChannel,
                    fcmFamilyChannel,
                    fcmBudgetChannel,
                    creditLimitChannel,
                    spendSummaryChannel
                )
            )
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
            normalized.contains("cashback") || normalized.contains("cash back") -> R.drawable.ic_cat_cashback
            normalized == "income" -> R.drawable.ic_cat_income
            normalized.contains("salary") || normalized.contains("income") -> R.drawable.ic_cat_salary
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
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
            .replace(Regex("\\s+"), " ")
            .trim()
        return singleLine.ifBlank { fallback }.take(48)
    }

    /**
     * Cleans a raw transaction title for notification display so trailing SMS sentences
     * (e.g. "Not you? Call 1800...", "If not done by you...", "SMS BLOCK...", "Avl Bal...")
     * or raw debit/credit alert sentences never appear in the notification title.
     */
    fun cleanNotificationMerchant(
        rawTitle: String,
        cleanCategory: String,
        transactionType: String = "EXPENSE"
    ): String {
        val defaultFallback = when {
            !cleanCategory.equals("Unknown", ignoreCase = true) && cleanCategory.isNotBlank() -> cleanCategory
            transactionType.equals("INCOME", ignoreCase = true) -> "Income"
            else -> "Expense"
        }

        var candidate = rawTitle
            .lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
            .replace(Regex("""^\[[^\]]+]\s*"""), "") // strip "[VM-HDFCBK] " prefix if present
            .replace(Regex("""\s+"""), " ")
            .trim()

        if (candidate.isBlank()) return defaultFallback

        // Strip trailing disclaimer/balance/helpline sentences or clauses
        val trailingBoilerplateRegex = Regex(
            """(?:[.;!|]|\s+-\s+|\s+)\s*(?:not\s+you\b|if\s+not\b|if\s+this\s+was\s+not\b|if\s+you\s+(?:have\s+)?not\b|sms\s+block\b|to\s+(?:block|dispute|report|check|know|view|opt|stop|avoid)\b|for\s+(?:dispute|any\s+query|assistance)\b|call\s+(?:1800|1860|022|080|\+?91|[0-9]{7,})\b|(?:please\s+)?do\s+not\s+share\b|never\s+share\b|don'?t\s+share\b|avl\.?\s*bal\b|available\s+bal\b|avail\.?\s*(?:bal|limit)\b|clear\s+bal\b|ref(?:\s*no)?[:\s]+[0-9a-zA-Z]+).*$""",
            RegexOption.IGNORE_CASE
        )
        candidate = candidate.replace(trailingBoilerplateRegex, "").trim().trim('.', ',', ';', '-', ':').trim()

        // Strip inline raw debit/credit clauses if someone passed a raw SMS line
        val rawAlertClauseRegex = Regex(
            """(?:\s+(?:rs\.?|inr|₹)\s*[0-9,]+.*$|\b(?:rs\.?|inr|₹)\s*[0-9,]+(?:\.[0-9]{1,2})?\s+(?:debited|credited|spent|paid|withdrawn).*$)""",
            RegexOption.IGNORE_CASE
        )
        candidate = candidate.replace(rawAlertClauseRegex, "").trim().trim('.', ',', ';', '-').trim()

        val lower = candidate.lowercase()
        val garbagePhrases = listOf(
            "not you", "if not", "sms block", "block upi", "report fraud",
            "to dispute", "to block", "to report", "do not share", "never share",
            "call 1800", "call 1860", "helpline", "customer care", "debited from",
            "credited to", "your a/c", "your acct", "avl bal", "available bal"
        )
        if (candidate.length < 2 ||
            garbagePhrases.any { lower.contains(it) } ||
            lower.startsWith("block ") ||
            lower.startsWith("dispute ") ||
            lower.startsWith("report ") ||
            lower.startsWith("call ") ||
            Regex("""^[0-9xX*.\-\s]+$""").matches(candidate) ||
            Regex("""[0-9]{7,}""").containsMatchIn(candidate)
        ) {
            return defaultFallback
        }

        return candidate.take(38).trim()
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
        LocalAccountSessionManager.ensureInitialized(context.applicationContext)
        val cleanCategory = ExpenseCategories.normalizeCategory(currentCategory)
        val isIncome = transactionType.equals("INCOME", ignoreCase = true) ||
            ExpenseCategories.isIncomeCategory(cleanCategory)
        val notifSettings = LocalAccountSessionManager.fcmPushSettings.value
        if (isIncome && !notifSettings.incomeAlertsEnabled) return
        if (!isIncome && !notifSettings.expenseAlertsEnabled) return

        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val notificationId = expenseId.hashCode()
        val cleanMerchant = cleanNotificationMerchant(title, cleanCategory, transactionType)
        val formattedAmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
            maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        }.format(amount)
        val amountDisplay = if (isIncome) {
            "+$currencySymbol$formattedAmt"
        } else {
            "$currencySymbol$formattedAmt"
        }
        val targetChannelId = if (isIncome) CHANNEL_ID_SMS_INCOME else CHANNEL_ID

        // Tap notification -> Open MainActivity directly into the Edit Transaction dialog
        val contentPendingIntent = createOpenEditTransactionPendingIntent(
            context = context,
            expenseId = expenseId,
            householdId = householdId,
            visibility = visibility,
            requestCode = notificationId
        )

        // Category Wheel action (first of max 3 actions so it's always visible on Android notifications)
        val openWheelIntent = Intent(context, NotificationCategoryWheelActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
            putExtra(EXTRA_EDIT_EXPENSE_ID, expenseId)
            putExtra(EXTRA_EDIT_HOUSEHOLD_ID, householdId)
            putExtra(EXTRA_EDIT_VISIBILITY, visibility)
            putExtra(EXTRA_EDIT_CURRENT_CATEGORY, cleanCategory)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(extraTitle, cleanMerchant)
            putExtra(extraAmount, amountDisplay)
        }
        val wheelPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 99,
            openWheelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val quickCandidates = if (isIncome) {
            listOf("Salary & Income", "Cashback")
        } else {
            listOf("Food & Dining", "Groceries", "Shopping", "Bills & Utilities", "Fuel & Travel")
        }
        val quickCategories = quickCandidates
            .filterNot { it.equals(cleanCategory, ignoreCase = true) }
            .take(2)

        val cleanPayment = sanitizeSummaryField(paymentSummary, "")
        val collapsedSummary = buildString {
            append(cleanCategory)
            if (cleanPayment.isNotBlank()) {
                append(" • ")
                append(cleanPayment)
            }
        }
        val expandedSummary = buildString {
            append("Merchant: ").append(cleanMerchant)
            append("\nCategory: ").append(cleanCategory)
            append(" • Amount: ").append(amountDisplay)
            if (cleanPayment.isNotBlank()) {
                append("\nAccount: ").append(cleanPayment)
            }
        }

        val builder = NotificationCompat.Builder(context, targetChannelId)
            .setSmallIcon(categoryNotificationIconRes(cleanCategory))
            .setLargeIcon(buildCategoryLargeIconBitmap(context, cleanCategory))
            .setColor(categoryAccentColorArgb(cleanCategory))
            .setContentTitle("$amountDisplay • $cleanMerchant")
            .setContentText(collapsedSummary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedSummary))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "Category Wheel", wheelPendingIntent)

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

    fun postUpdatedCategoryNotification(
        context: Context,
        notificationId: Int,
        title: String,
        amountText: String,
        newCategory: String,
        expenseId: String = "",
        householdId: String = "",
        visibility: String = ""
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ensureChannel(context)
        val normalizedNew = ExpenseCategories.normalizeCategory(newCategory)
        val targetChannel = if (ExpenseCategories.isIncomeCategory(normalizedNew)) {
            CHANNEL_ID_SMS_INCOME
        } else {
            CHANNEL_ID
        }
        val updatedTitle = when {
            amountText.isNotBlank() && title.isNotBlank() -> "$amountText • $title"
            amountText.isNotBlank() -> amountText
            else -> title
        }
        val updatedBuilder = NotificationCompat.Builder(context, targetChannel)
            .setSmallIcon(categoryNotificationIconRes(normalizedNew))
            .setLargeIcon(buildCategoryLargeIconBitmap(context, normalizedNew))
            .setColor(categoryAccentColorArgb(normalizedNew))
            .setContentTitle(updatedTitle)
            .setContentText("Category: $normalizedNew ✓")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setTimeoutAfter(4000L)
        if (expenseId.isNotBlank()) {
            updatedBuilder.setContentIntent(
                createOpenEditTransactionPendingIntent(
                    context = context,
                    expenseId = expenseId,
                    householdId = householdId,
                    visibility = visibility,
                    requestCode = notificationId
                )
            )
        }
        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, updatedBuilder.build())
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
        val cleanMerchant = cleanNotificationMerchant(title, cleanCategory, "EXPENSE")
        val formattedAmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
            maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        }.format(amount)

        val notificationId = ("fcm_family_$expenseId").hashCode()
        val contentPendingIntent = createOpenEditTransactionPendingIntent(
            context = context,
            expenseId = expenseId,
            householdId = householdId,
            visibility = "SHARED",
            requestCode = notificationId
        )

        val cleanHousehold = sanitizeSummaryField(householdName, "Family Home")
        val cleanMember = sanitizeSummaryField(memberName, "Family Member")
        val summaryLine = "$cleanCategory • $cleanMember"
        val expandedSummary = "Merchant: $cleanMerchant\nCategory: $cleanCategory • Amount: $currencySymbol$formattedAmt\nBy: $cleanMember ($cleanHousehold)"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_FCM_FAMILY)
            .setSmallIcon(categoryNotificationIconRes(cleanCategory))
            .setLargeIcon(buildCategoryLargeIconBitmap(context, cleanCategory))
            .setColor(categoryAccentColorArgb(cleanCategory))
            .setContentTitle("$currencySymbol$formattedAmt • $cleanMerchant")
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

        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val uid = resolveActiveAlertUid(context)
        val periodStamp = currentYearMonthStamp()
        val limitStamp = kotlin.math.round(limitAmount).toLong()
        val baseKey = "budget|$uid|${categoryOrScopeName.trim().lowercase()}|$periodStamp|$limitStamp"
        val isCrossed = usedPercent >= 100
        val stage = if (isCrossed) "CROSSED" else "WARNING"
        val dedupeKey = "$baseKey|$stage"
        if (!forceNotify && isLimitAlertAlreadySent(context, dedupeKey)) return

        val numFmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
            maximumFractionDigits = 0
        }
        val spentStr = "$currencySymbol${numFmt.format(spentAmount)}"
        val limitStr = "$currencySymbol${numFmt.format(limitAmount)}"
        val headline = if (isCrossed) {
            "$spentStr / $limitStr • $categoryOrScopeName Exceeded ($usedPercent%)"
        } else {
            "$spentStr / $limitStr • $categoryOrScopeName ($usedPercent%)"
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
            if (!forceNotify) {
                if (isCrossed) {
                    markLimitAlertsSent(context, dedupeKey, "$baseKey|WARNING")
                } else {
                    markLimitAlertsSent(context, dedupeKey)
                }
            }
        }
    }

    /**
     * Fires a Credit Card Spend Limit notification ONCE per limit warning stage and ONCE per
     * limit crossing stage for the active billing period/limit (persisted across app restarts).
     */
    fun showCreditCardLimitNotification(
        context: Context,
        accountId: String,
        accountName: String,
        spentAmount: Double,
        limitAmount: Double,
        periodLabel: String,
        thresholdPercent: Int,
        currencySymbol: String = "₹",
        forceNotify: Boolean = false,
        periodWindowKey: String = ""
    ) {
        LocalAccountSessionManager.ensureInitialized(context.applicationContext)
        val fcmSettings = LocalAccountSessionManager.fcmPushSettings.value
        if (!fcmSettings.creditCardLimitAlertsEnabled && !forceNotify) return
        if (limitAmount <= 0.0) return

        val effectiveThreshold = thresholdPercent.coerceIn(1, 100)
        val usedPercent = ((spentAmount / limitAmount) * 100.0).toInt()
        if (usedPercent < effectiveThreshold && !forceNotify) return

        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val uid = resolveActiveAlertUid(context)
        val resolvedPeriodKey = periodWindowKey.trim().ifBlank {
            "${periodLabel.trim().lowercase()}_${currentYearMonthStamp()}"
        }
        val limitStamp = kotlin.math.round(limitAmount).toLong()
        val baseKey = "cc_limit|$uid|${accountId.ifBlank { accountName }.trim().lowercase()}|$resolvedPeriodKey|$limitStamp"
        val isCrossed = usedPercent >= 100
        val stage = if (isCrossed) "CROSSED" else "WARNING"
        val dedupeKey = "$baseKey|$stage"
        if (!forceNotify && isLimitAlertAlreadySent(context, dedupeKey)) return

        val numFmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
            maximumFractionDigits = 0
        }
        val spentStr = "$currencySymbol${numFmt.format(spentAmount)}"
        val limitStr = "$currencySymbol${numFmt.format(limitAmount)}"
        val cleanName = sanitizeSummaryField(accountName, "Credit Card")
        val headline = if (isCrossed) {
            "$spentStr / $limitStr • $cleanName Limit Exceeded ($usedPercent%)"
        } else {
            "$spentStr / $limitStr • $cleanName ($usedPercent%)"
        }
        val bodyText = "$periodLabel limit ($effectiveThreshold% alert) • $spentStr of $limitStr"

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val notificationId = ("cc_limit_$dedupeKey").hashCode()
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_CREDIT_LIMIT)
            .setSmallIcon(R.drawable.ic_cat_utilities)
            .setLargeIcon(buildCategoryLargeIconBitmap(context, "Bills & Utilities"))
            .setColor(categoryAccentColorArgb("Bills & Utilities"))
            .setContentTitle(headline)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            if (!forceNotify) {
                if (isCrossed) {
                    markLimitAlertsSent(context, dedupeKey, "$baseKey|WARNING")
                } else {
                    markLimitAlertsSent(context, dedupeKey)
                }
            }
        }
    }

    /**
     * Fires a Daily, Weekly, or Monthly Spend Summary notification showing total spend,
     * number of transactions, and top category. Title always begins with the formatted amount.
     */
    fun showSpendSummaryNotification(
        context: Context,
        period: SpendSummaryPeriod,
        spentAmount: Double,
        transactionCount: Int,
        topCategory: String? = null,
        currencySymbol: String = "₹",
        forceNotify: Boolean = false
    ) {
        LocalAccountSessionManager.ensureInitialized(context.applicationContext)
        val fcmSettings = LocalAccountSessionManager.fcmPushSettings.value
        if (!fcmSettings.isSubCategoryEnabled(period.subCategory) && !forceNotify) return

        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val numFmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
            maximumFractionDigits = if (spentAmount % 1.0 == 0.0) 0 else 2
        }
        val spentStr = "$currencySymbol${numFmt.format(spentAmount.coerceAtLeast(0.0))}"
        val headline = "$spentStr • ${period.label}"
        val cleanTopCat = topCategory?.takeIf { it.isNotBlank() }?.let { ExpenseCategories.normalizeCategory(it) }
        val bodyText = if (transactionCount > 0) {
            buildString {
                append("$transactionCount expense")
                if (transactionCount != 1) append("s")
                if (!cleanTopCat.isNullOrBlank()) {
                    append(" • Top: $cleanTopCat")
                }
                append(" • Total spent: $spentStr")
            }
        } else {
            "No expenses logged in this period • Total spent: $spentStr"
        }

        val iconCat = cleanTopCat ?: "Bills & Utilities"
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val notificationId = ("spend_summary_${period.name.lowercase()}").hashCode()
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_SPEND_SUMMARY)
            .setSmallIcon(categoryNotificationIconRes(iconCat))
            .setLargeIcon(buildCategoryLargeIconBitmap(context, iconCat))
            .setColor(categoryAccentColorArgb(iconCat))
            .setContentTitle(headline)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }

    /**
     * Computes the user's total non-excluded expense spend for [period] (Daily, Weekly, or Monthly)
     * relative to [referenceMillis] and posts the corresponding Spend Summary notification.
     */
    fun evaluateAndSendSpendSummary(
        context: Context,
        period: SpendSummaryPeriod,
        referenceMillis: Long = System.currentTimeMillis(),
        forceNotify: Boolean = false
    ) {
        val appContext = context.applicationContext
        LocalAccountSessionManager.ensureInitialized(appContext)
        LocalAccountDataStore.ensureInitialized(appContext)

        val fcmSettings = LocalAccountSessionManager.fcmPushSettings.value
        if (!fcmSettings.isSubCategoryEnabled(period.subCategory) && !forceNotify) return

        val activeUid = LocalAccountSessionManager.getCurrentUser(appContext)?.uid
            ?: runCatching { Firebase.auth.currentUser?.uid }.getOrNull()
            ?: LocalAccountSessionManager.DEFAULT_TEST_UID
        val profile = LocalAccountDataStore.getUserProfileOnce(activeUid)
        val householdId = profile?.activeHouseholdId.orEmpty()
        val currencySymbol = profile?.currencySymbol?.takeIf { it.isNotBlank() } ?: "₹"

        val (startMillis, endMillis) = computeSummaryWindowMillis(period, referenceMillis)
        val allExpenses = LocalAccountDataStore.getExpensesForUserOrHouseholdOnce(activeUid, householdId)
        val matchingExpenses = allExpenses.filter { tx ->
            !tx.isExcludedFromExpense &&
                tx.type.equals("EXPENSE", ignoreCase = true) &&
                !ExpenseCategories.isIncomeCategory(tx.category) &&
                tx.transactionDateMillis in startMillis..endMillis
        }

        val totalSpent = matchingExpenses.sumOf { it.amount }
        val topCategory = matchingExpenses
            .groupBy { ExpenseCategories.normalizeCategory(it.category) }
            .maxByOrNull { (_, list) -> list.sumOf { it.amount } }
            ?.key

        showSpendSummaryNotification(
            context = appContext,
            period = period,
            spentAmount = totalSpent,
            transactionCount = matchingExpenses.size,
            topCategory = topCategory,
            currencySymbol = currencySymbol,
            forceNotify = forceNotify
        )
    }

    fun computeSummaryWindowMillis(
        period: SpendSummaryPeriod,
        referenceMillis: Long = System.currentTimeMillis()
    ): Pair<Long, Long> {
        val endCal = Calendar.getInstance().apply {
            timeInMillis = referenceMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val startCal = Calendar.getInstance().apply {
            timeInMillis = referenceMillis
            when (period) {
                SpendSummaryPeriod.DAILY -> {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                SpendSummaryPeriod.WEEKLY -> {
                    add(Calendar.DAY_OF_YEAR, -6)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                SpendSummaryPeriod.MONTHLY -> {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }
        }
        return startCal.timeInMillis to endCal.timeInMillis
    }

    /**
     * Shared processor used by both `IncomingSmsBroadcastReceiver` (`SMS_RECEIVED`) and
     * `SmsInboxContentObserver` (`content://sms`) to deduplicate, save, and notify immediately
     * when a new bank/UPI SMS arrives, without blocking on slow background network calls.
     */
    suspend fun processIncomingSmsCandidateAndNotify(
        context: Context,
        candidate: SmsCandidate
    ) = incomingSmsMutex.withLock {
        val appContext = context.applicationContext
        if (com.ritesh.parser.core.bank.BankParser.isMandateOrBillReminderOrNonTransaction(candidate.parsed.smsBody)) {
            return@withLock
        }
        LocalAccountSessionManager.ensureInitialized(appContext)
        LocalAccountDataStore.ensureInitialized(appContext)

        val localUser = LocalAccountSessionManager.getCurrentUser(appContext)
        val firebaseUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val activeUid = localUser?.uid ?: firebaseUser?.uid
        if (activeUid.isNullOrBlank()) return@withLock
        val isFirebaseBacked = firebaseUser != null &&
            !activeUid.startsWith("local_user_") &&
            firebaseUser.uid == activeUid

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

        val docId = "sms_${candidate.id}"
        val alreadyPendingCloudSync = isFirebaseBacked && LocalAccountDataStore.isPendingCloudSync(activeUid, docId)

        if (!alreadyPendingCloudSync) {
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
        }

        try {
            // 1. Check existing signatures before saving/notifying
            val localExistingSignatures = LocalAccountDataStore.getAllExistingSmsSignatures(activeUid)
            val alreadyInLocalStore = candidate.id in localExistingSignatures ||
                contentKey in localExistingSignatures ||
                adjacentKeys.any { it in localExistingSignatures }

            if (!isFirebaseBacked && alreadyInLocalStore) {
                markSignatureNotified(activeUid, candidate)
                return@withLock
            }

            // 2. Resolve profile & household from fast local cache first
            val cachedProfile = LocalAccountDataStore.getUserProfileOnce(activeUid)
            val cachedHousehold = cachedProfile?.activeHouseholdId?.takeIf { it.isNotBlank() }?.let {
                LocalAccountDataStore.getHouseholdOnce(it)
            }

            val parsed = candidate.parsed
            val isIncome = parsed.type == TransactionType.INCOME
            val merchantTitle = parsed.merchant?.takeIf { it.isNotBlank() }
                ?: "${parsed.bankName} ${parsed.type.name.lowercase().replaceFirstChar { it.uppercase() }}"
            val effectiveShared = cachedHousehold != null
            val memberDisplayName = firebaseUser?.displayName?.takeIf { it.isNotBlank() }
                ?: localUser?.displayName?.takeIf { it.isNotBlank() }
                ?: cachedProfile?.displayName?.takeIf { it.isNotBlank() }
                ?: "Member"
            val currencySymbol = cachedProfile?.currencySymbol?.takeIf { it.isNotBlank() } ?: "₹"
            val txTags = listOf("sms-auto", parsed.bankName.lowercase().replace(" ", "-"))
            val txNotes = "[${parsed.sender}] ${parsed.smsBody}".take(500)

            // 3. Persist immediately to local store so the transaction is never lost even if process is killed
            LocalAccountDataStore.cacheSmsMessage(
                signature = candidate.id,
                sender = parsed.sender,
                body = parsed.smsBody,
                timestamp = parsed.timestamp,
                persistImmediately = true
            )
            val localSavedId = if (!alreadyInLocalStore) {
                runCatching {
                    LocalAccountDataStore.addExpense(
                        uid = activeUid,
                        userName = memberDisplayName,
                        household = cachedHousehold,
                        title = merchantTitle,
                        amount = parsed.amount.toDouble(),
                        type = if (isIncome) "INCOME" else "EXPENSE",
                        category = candidate.suggestedCategory,
                        accountName = candidate.suggestedAccountName,
                        paymentMode = candidate.suggestedPaymentMode,
                        tags = txTags,
                        notes = txNotes,
                        isShared = effectiveShared,
                        smsSignature = candidate.id,
                        referenceNumber = parsed.reference.orEmpty(),
                        transactionDateMillis = parsed.timestamp,
                        customDocId = docId
                    )
                }.getOrDefault(docId)
            } else {
                docId
            }

            // Upsert discovered account locally when balance / limit / account digits are parsed
            if (parsed.balance != null || parsed.creditLimit != null || parsed.accountNumber != null) {
                val accType = when {
                    parsed.isFromCard -> "CREDIT_CARD"
                    candidate.suggestedPaymentMode == "WALLET" -> "WALLET"
                    else -> "BANK"
                }
                runCatching {
                    LocalAccountDataStore.upsertAccount(
                        uid = activeUid,
                        name = candidate.suggestedAccountName,
                        bankName = parsed.bankName,
                        accountType = accType,
                        lastFourDigits = parsed.accountNumber.orEmpty(),
                        balance = parsed.balance?.toDouble() ?: 0.0,
                        creditLimit = parsed.creditLimit?.toDouble() ?: 0.0,
                        isSharedVisible = effectiveShared,
                        asOfTimestampMillis = parsed.timestamp,
                        updateBalance = parsed.balance != null,
                        updateCreditLimit = parsed.creditLimit != null
                    )
                }
            }

            // For Firebase-backed sessions, mark pending cloud sync BEFORE attempting Firestore write
            // so even if Android kills the receiver or the device is offline, ViewModel reconciles it.
            if (isFirebaseBacked) {
                LocalAccountDataStore.markPendingCloudSync(activeUid, localSavedId)
            }

            // 4. Sync to Firestore FIRST (enqueuing immediately into Firestore's offline persistence cache
            // so active UI listeners show the new transaction right away), then post notification.
            var resolvedVisibility = if (effectiveShared) "SHARED" else "PERSONAL"
            var resolvedHouseholdId = if (effectiveShared) cachedHousehold?.id.orEmpty() else ""
            var resolvedHousehold = cachedHousehold

            if (isFirebaseBacked) {
                val repo = OneFamilyRepository(appContext)
                val remoteHousehold = kotlinx.coroutines.withTimeoutOrNull(2_000L) {
                    runCatching {
                        val remoteProfile = repo.getUserProfileOnce() ?: cachedProfile
                        remoteProfile?.activeHouseholdId?.takeIf { it.isNotBlank() }?.let {
                            repo.getHouseholdOnce(it)
                        }
                    }.getOrNull()
                } ?: cachedHousehold
                val remoteShared = remoteHousehold != null
                resolvedHousehold = remoteHousehold
                resolvedVisibility = if (remoteShared) "SHARED" else "PERSONAL"
                resolvedHouseholdId = if (remoteShared) remoteHousehold?.id.orEmpty() else ""

                // Enqueue Firestore write immediately with a generous timeout so offline cache updates right away
                kotlinx.coroutines.withTimeoutOrNull(12_000L) {
                    val saveResult = repo.addExpense(
                        userName = memberDisplayName,
                        household = remoteHousehold,
                        title = merchantTitle,
                        amount = parsed.amount.toDouble(),
                        type = if (isIncome) "INCOME" else "EXPENSE",
                        category = candidate.suggestedCategory,
                        accountName = candidate.suggestedAccountName,
                        paymentMode = candidate.suggestedPaymentMode,
                        tags = txTags,
                        notes = txNotes,
                        isShared = remoteShared,
                        smsSignature = candidate.id,
                        referenceNumber = parsed.reference.orEmpty(),
                        transactionDateMillis = parsed.timestamp,
                        customDocId = localSavedId,
                        skipExistingSignatureScan = true
                    )
                    saveResult.onSuccess {
                        LocalAccountDataStore.clearPendingCloudSync(activeUid, localSavedId)
                        if (parsed.balance != null || parsed.creditLimit != null || parsed.accountNumber != null) {
                            val accType = when {
                                parsed.isFromCard -> "CREDIT_CARD"
                                candidate.suggestedPaymentMode == "WALLET" -> "WALLET"
                                else -> "BANK"
                            }
                            runCatching {
                                repo.upsertAccount(
                                    name = candidate.suggestedAccountName,
                                    bankName = parsed.bankName,
                                    accountType = accType,
                                    lastFourDigits = parsed.accountNumber.orEmpty(),
                                    balance = parsed.balance?.toDouble() ?: 0.0,
                                    creditLimit = parsed.creditLimit?.toDouble() ?: 0.0,
                                    isSharedVisible = remoteShared,
                                    asOfTimestampMillis = parsed.timestamp,
                                    updateBalance = parsed.balance != null,
                                    updateCreditLimit = parsed.creditLimit != null
                                )
                            }
                        }
                    }.onFailure { err ->
                        LocalAccountSessionManager.recordDiagnostic(
                            context = appContext,
                            subsystem = "SMS_REALTIME_SYNC",
                            severity = "NON_FATAL",
                            message = "Firestore real-time SMS write deferred (${err.javaClass.simpleName}): ${err.localizedMessage.orEmpty()}"
                        )
                    }
                }
            }

            if (alreadyInLocalStore && !alreadyPendingCloudSync) {
                markSignatureNotified(activeUid, candidate)
                return@withLock
            }

            val paymentSummary = buildString {
                if (candidate.suggestedAccountName.isNotBlank()) append(candidate.suggestedAccountName)
                if (candidate.suggestedPaymentMode.isNotBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append(candidate.suggestedPaymentMode)
                }
            }

            // 5. Post the real-time notification pointing directly to the saved transaction ID
            showNewTransactionNotification(
                context = appContext,
                expenseId = localSavedId,
                householdId = resolvedHouseholdId,
                visibility = resolvedVisibility,
                title = merchantTitle,
                amount = parsed.amount.toDouble(),
                currentCategory = candidate.suggestedCategory,
                currencySymbol = currencySymbol,
                paymentSummary = paymentSummary,
                transactionType = if (isIncome) "INCOME" else "EXPENSE"
            )

            markSignatureNotified(activeUid, candidate)

            if (resolvedHousehold != null) {
                showFamilyTransactionPushNotification(
                    context = appContext,
                    expenseId = localSavedId,
                    householdId = resolvedHousehold.id,
                    householdName = resolvedHousehold.name,
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

    /**
     * Silently updates the matching bank/card account balance from a daily/enquiry balance SMS
     * (such as HDFC daily balance alerts) without creating a transaction or posting a notification.
     */
    suspend fun processIncomingBalanceUpdate(
        context: Context,
        balanceUpdate: com.ritesh.parser.core.ParsedBalanceUpdate
    ) = incomingSmsMutex.withLock {
        val appContext = context.applicationContext
        try {
            LocalAccountSessionManager.ensureInitialized(appContext)
            LocalAccountDataStore.ensureInitialized(appContext)

            val firebaseUser = runCatching { Firebase.auth.currentUser }.getOrNull()
            val localUser = LocalAccountSessionManager.getCurrentUser(appContext)
            val isFirebaseBacked = firebaseUser != null
            val activeUid = firebaseUser?.uid ?: localUser?.uid ?: return@withLock

            val cachedProfile = LocalAccountDataStore.getUserProfileOnce(activeUid)
            val cachedHousehold = cachedProfile?.activeHouseholdId
                ?.takeIf { it.isNotBlank() }
                ?.let { LocalAccountDataStore.getHouseholdOnce(it) }
            val effectiveShared = cachedHousehold != null

            runCatching {
                LocalAccountDataStore.applyAccountBalanceUpdates(
                    uid = activeUid,
                    updates = listOf(balanceUpdate),
                    isSharedVisible = effectiveShared
                )
            }

            if (isFirebaseBacked) {
                val repo = OneFamilyRepository(appContext)
                kotlinx.coroutines.withTimeoutOrNull(8_000L) {
                    repo.applyAccountBalanceUpdates(
                        updates = listOf(balanceUpdate),
                        isSharedVisible = effectiveShared
                    )
                }
            }
        } catch (e: Exception) {
            LocalAccountSessionManager.recordDiagnostic(
                context = appContext,
                subsystem = "SMS_BALANCE_SYNC",
                severity = "NON_FATAL",
                message = "Guarded incoming balance SMS processing: ${e.javaClass.simpleName}: ${e.localizedMessage.orEmpty()}"
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
        LocalAccountDataStore.ensureInitialized(context.applicationContext)
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

                TransactionNotificationHelper.postUpdatedCategoryNotification(
                    context = context,
                    notificationId = notificationId,
                    title = title,
                    amountText = amountText,
                    newCategory = newCategory,
                    expenseId = expenseId,
                    householdId = householdId,
                    visibility = visibility
                )
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
        LocalAccountDataStore.ensureInitialized(appContext)
        val firebaseUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val localUser = LocalAccountSessionManager.getCurrentUser(appContext)
        if (firebaseUser == null && localUser == null) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Group PDU segments by originating address so multi-part or multiple SMS in a single intent are all processed
        val groupedBySender = messages.groupBy { msg ->
            msg.displayOriginatingAddress?.takeIf { it.isNotBlank() }
                ?: msg.originatingAddress.orEmpty()
        }

        val reader = SmsInboxReader(appContext)
        val candidates = mutableListOf<SmsCandidate>()
        val balanceUpdates = mutableListOf<com.ritesh.parser.core.ParsedBalanceUpdate>()
        for ((sender, parts) in groupedBySender) {
            val fullBody = parts.joinToString(separator = "") { part ->
                part.displayMessageBody ?: part.messageBody.orEmpty()
            }
            val timestamp = parts.firstOrNull()?.timestampMillis?.takeIf { it > 0L }
                ?: System.currentTimeMillis()
            if (sender.isBlank() || fullBody.isBlank()) continue
            val txCandidate = reader.parseRawSms(
                sender = sender,
                body = fullBody,
                timestamp = timestamp,
                defaultShared = true
            )
            if (txCandidate != null) {
                candidates.add(txCandidate)
            } else {
                val balanceUpdate = reader.parseBalanceSms(
                    sender = sender,
                    body = fullBody,
                    timestamp = timestamp
                )
                if (balanceUpdate != null) {
                    balanceUpdates.add(balanceUpdate)
                }
            }
        }
        if (candidates.isEmpty() && balanceUpdates.isEmpty()) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                for (candidate in candidates) {
                    TransactionNotificationHelper.processIncomingSmsCandidateAndNotify(
                        context = appContext,
                        candidate = candidate
                    )
                }
                for (balanceUpdate in balanceUpdates) {
                    TransactionNotificationHelper.processIncomingBalanceUpdate(
                        context = appContext,
                        balanceUpdate = balanceUpdate
                    )
                }
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
    private var lastProcessedSmsDateMillis: Long = System.currentTimeMillis() - 60_000L

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        val hasReadSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasReadSms) return

        scope.launch {
            observerMutex.withLock {
                LocalAccountSessionManager.ensureInitialized(context.applicationContext)
                LocalAccountDataStore.ensureInitialized(context.applicationContext)
                runCatching {
                    val projection = arrayOf(
                        Telephony.Sms.ADDRESS,
                        Telephony.Sms.BODY,
                        Telephony.Sms.DATE,
                        Telephony.Sms.TYPE
                    )
                    // Use a 5-minute lookback window to tolerate carrier SMSC timestamp skew,
                    // and avoid `LIMIT` inside sortOrder which fails on Android 10+ TelephonyProvider.
                    val lookbackFloorMillis = (lastProcessedSmsDateMillis - 300_000L).coerceAtLeast(0L)
                    val queryUri = Telephony.Sms.Inbox.CONTENT_URI
                    context.contentResolver.query(
                        queryUri,
                        projection,
                        "${Telephony.Sms.DATE} >= ?",
                        arrayOf(lookbackFloorMillis.toString()),
                        "${Telephony.Sms.DATE} ASC"
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
                                } else {
                                    val balanceUpdate = reader.parseBalanceSms(
                                        sender = sender,
                                        body = body,
                                        timestamp = dateMillis
                                    )
                                    if (balanceUpdate != null) {
                                        TransactionNotificationHelper.processIncomingBalanceUpdate(
                                            context = context.applicationContext,
                                            balanceUpdate = balanceUpdate
                                        )
                                    }
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

/**
 * Schedules daily, weekly, and monthly spend summary notifications at the user's configured
 * time (`summaryNotificationHour` : `summaryNotificationMinute`).
 */
object SpendSummaryAlarmScheduler {
    const val ACTION_SPEND_SUMMARY_ALARM = "com.example.action.SPEND_SUMMARY_ALARM"
    const val EXTRA_SUMMARY_PERIOD = "extra_summary_period"
    private const val REQUEST_CODE_DAILY = 4101
    private const val REQUEST_CODE_WEEKLY = 4102
    private const val REQUEST_CODE_MONTHLY = 4103

    fun computeNextTriggerMillis(
        hourOfDay: Int,
        minute: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hourOfDay.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= nowMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return cal.timeInMillis
    }

    fun computeNextWeeklyTriggerMillis(
        dayOfWeek: Int,
        hourOfDay: Int,
        minute: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val targetDow = dayOfWeek.coerceIn(Calendar.SUNDAY, Calendar.SATURDAY)
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hourOfDay.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentDow = cal.get(Calendar.DAY_OF_WEEK)
        var daysUntil = (targetDow - currentDow + 7) % 7
        if (daysUntil == 0 && cal.timeInMillis <= nowMillis) {
            daysUntil = 7
        }
        cal.add(Calendar.DAY_OF_YEAR, daysUntil)
        return cal.timeInMillis
    }

    fun computeNextEndOfMonthTriggerMillis(
        hourOfDay: Int,
        minute: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, hourOfDay.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= nowMillis) {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.add(Calendar.MONTH, 1)
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        return cal.timeInMillis
    }

    fun computeTriggerMillisForPeriod(
        period: SpendSummaryPeriod,
        settings: com.example.data.model.FcmPushSettings,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val (hour, minute) = settings.getSummaryTime(period.subCategory)
        return when (period) {
            SpendSummaryPeriod.DAILY -> computeNextTriggerMillis(
                hourOfDay = hour,
                minute = minute,
                nowMillis = nowMillis
            )
            SpendSummaryPeriod.WEEKLY -> computeNextWeeklyTriggerMillis(
                dayOfWeek = settings.weeklySummaryDayOfWeek,
                hourOfDay = hour,
                minute = minute,
                nowMillis = nowMillis
            )
            SpendSummaryPeriod.MONTHLY -> computeNextEndOfMonthTriggerMillis(
                hourOfDay = hour,
                minute = minute,
                nowMillis = nowMillis
            )
        }
    }

    fun scheduleNextSummaryAlarms(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        val appContext = context.applicationContext
        LocalAccountSessionManager.ensureInitialized(appContext)
        val settings = LocalAccountSessionManager.fcmPushSettings.value
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        scheduleOrCancelPeriod(
            context = appContext,
            alarmManager = alarmManager,
            period = SpendSummaryPeriod.DAILY,
            requestCode = REQUEST_CODE_DAILY,
            enabled = settings.dailySummaryEnabled,
            triggerAtMillis = computeTriggerMillisForPeriod(SpendSummaryPeriod.DAILY, settings, nowMillis)
        )
        scheduleOrCancelPeriod(
            context = appContext,
            alarmManager = alarmManager,
            period = SpendSummaryPeriod.WEEKLY,
            requestCode = REQUEST_CODE_WEEKLY,
            enabled = settings.weeklySummaryEnabled,
            triggerAtMillis = computeTriggerMillisForPeriod(SpendSummaryPeriod.WEEKLY, settings, nowMillis)
        )
        scheduleOrCancelPeriod(
            context = appContext,
            alarmManager = alarmManager,
            period = SpendSummaryPeriod.MONTHLY,
            requestCode = REQUEST_CODE_MONTHLY,
            enabled = settings.monthlySummaryEnabled,
            triggerAtMillis = computeTriggerMillisForPeriod(SpendSummaryPeriod.MONTHLY, settings, nowMillis)
        )
    }

    private fun scheduleOrCancelPeriod(
        context: Context,
        alarmManager: AlarmManager,
        period: SpendSummaryPeriod,
        requestCode: Int,
        enabled: Boolean,
        triggerAtMillis: Long
    ) {
        val intent = Intent(context, SpendSummaryBroadcastReceiver::class.java).apply {
            action = ACTION_SPEND_SUMMARY_ALARM
            putExtra(EXTRA_SUMMARY_PERIOD, period.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (!enabled) {
            runCatching { alarmManager.cancel(pendingIntent) }
            return
        }
        runCatching {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }
}

/**
 * BroadcastReceiver that fires when a scheduled Spend Summary alarm triggers, posts the
 * corresponding Daily/Weekly/Monthly spend summary notification, and schedules the next alarm.
 */
class SpendSummaryBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        LocalAccountSessionManager.ensureInitialized(appContext)
        LocalAccountDataStore.ensureInitialized(appContext)

        val periodName = intent.getStringExtra(SpendSummaryAlarmScheduler.EXTRA_SUMMARY_PERIOD).orEmpty()
        val period = SpendSummaryPeriod.entries.firstOrNull { it.name.equals(periodName, ignoreCase = true) }

        if (period != null) {
            TransactionNotificationHelper.evaluateAndSendSpendSummary(
                context = appContext,
                period = period,
                forceNotify = false
            )
        } else {
            val settings = LocalAccountSessionManager.fcmPushSettings.value
            if (settings.dailySummaryEnabled) {
                TransactionNotificationHelper.evaluateAndSendSpendSummary(appContext, SpendSummaryPeriod.DAILY)
            }
            if (settings.weeklySummaryEnabled) {
                TransactionNotificationHelper.evaluateAndSendSpendSummary(appContext, SpendSummaryPeriod.WEEKLY)
            }
            if (settings.monthlySummaryEnabled) {
                TransactionNotificationHelper.evaluateAndSendSpendSummary(appContext, SpendSummaryPeriod.MONTHLY)
            }
        }

        SpendSummaryAlarmScheduler.scheduleNextSummaryAlarms(appContext)
    }
}

