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
    const val EXTRA_EDIT_EXPENSE_ID = "extra_edit_expense_id"
    const val EXTRA_EDIT_HOUSEHOLD_ID = "extra_edit_household_id"
    const val EXTRA_EDIT_VISIBILITY = "extra_edit_visibility"
    const val EXTRA_EDIT_CURRENT_CATEGORY = "extra_edit_current_category"
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
    private val notifiedCreditLimitKeys: MutableSet<String> =
        Collections.synchronizedSet(mutableSetOf<String>())

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
            normalized.contains("cashback") || normalized.contains("salary") || normalized.contains("income") -> R.drawable.ic_cat_income
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
            listOf("Income", "Cashback", "Salary & Income")
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
        newCategory: String
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
        }
    }

    /**
     * Fires a Credit Card Spend Limit notification when spend in the configured period reaches
     * the customizable threshold percentage (default 80%).
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
        forceNotify: Boolean = false
    ) {
        LocalAccountSessionManager.ensureInitialized(context.applicationContext)
        val fcmSettings = LocalAccountSessionManager.fcmPushSettings.value
        if (!fcmSettings.creditCardLimitAlertsEnabled && !forceNotify) return
        if (limitAmount <= 0.0) return

        val effectiveThreshold = thresholdPercent.coerceIn(1, 100)
        val usedPercent = ((spentAmount / limitAmount) * 100.0).toInt()
        if (usedPercent < effectiveThreshold && !forceNotify) return

        val bucket = if (usedPercent >= 100) "100" else effectiveThreshold.toString()
        val dedupeKey = "${accountId.ifBlank { accountName }.lowercase()}_${periodLabel.lowercase()}_$bucket"
        if (!forceNotify && !notifiedCreditLimitKeys.add(dedupeKey)) return

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
        val cleanName = sanitizeSummaryField(accountName, "Credit Card")
        val headline = if (usedPercent >= 100) {
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
            // 1. Check fast local signatures first (zero network latency)
            val localExistingSignatures = LocalAccountDataStore.getAllExistingSmsSignatures(activeUid)
            if (candidate.id in localExistingSignatures ||
                contentKey in localExistingSignatures ||
                adjacentKeys.any { it in localExistingSignatures }
            ) {
                markSignatureNotified(activeUid, candidate)
                return@withLock
            }

            // 2. Resolve profile & household from fast local cache first (with bounded timeout fallback)
            val cachedProfile = LocalAccountDataStore.getUserProfileOnce(activeUid)
            val cachedHousehold = cachedProfile?.activeHouseholdId?.takeIf { it.isNotBlank() }?.let {
                LocalAccountDataStore.getHouseholdOnce(it)
            }

            val parsed = candidate.parsed
            val isIncome = parsed.type == TransactionType.INCOME
            val merchantTitle = parsed.merchant?.takeIf { it.isNotBlank() }
                ?: "${parsed.bankName} ${parsed.type.name.lowercase().replaceFirstChar { it.uppercase() }}"
            val effectiveShared = cachedHousehold != null
            val docId = "sms_${candidate.id}"
            val memberDisplayName = firebaseUser?.displayName
                ?: localUser?.displayName
                ?: cachedProfile?.displayName
                ?: "Member"
            val currencySymbol = cachedProfile?.currencySymbol ?: "₹"
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
            val localSavedId = runCatching {
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

            val paymentSummary = buildString {
                if (candidate.suggestedAccountName.isNotBlank()) append(candidate.suggestedAccountName)
                if (candidate.suggestedPaymentMode.isNotBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append(candidate.suggestedPaymentMode)
                }
            }

            // 4. Post the real-time notification IMMEDIATELY before any slow Firestore network round-trip
            showNewTransactionNotification(
                context = appContext,
                expenseId = localSavedId,
                householdId = cachedHousehold?.id.orEmpty(),
                visibility = if (effectiveShared) "SHARED" else "PERSONAL",
                title = merchantTitle,
                amount = parsed.amount.toDouble(),
                currentCategory = candidate.suggestedCategory,
                currencySymbol = currencySymbol,
                paymentSummary = paymentSummary,
                transactionType = if (isIncome) "INCOME" else "EXPENSE"
            )

            markSignatureNotified(activeUid, candidate)

            if (cachedHousehold != null) {
                showFamilyTransactionPushNotification(
                    context = appContext,
                    expenseId = localSavedId,
                    householdId = cachedHousehold.id,
                    householdName = cachedHousehold.name,
                    memberName = memberDisplayName,
                    title = merchantTitle,
                    amount = parsed.amount.toDouble(),
                    category = candidate.suggestedCategory,
                    currencySymbol = currencySymbol
                )
            }

            // 5. Sync to Firestore with bounded timeout if backed by Firebase Auth
            if (firebaseUser != null) {
                kotlinx.coroutines.withTimeoutOrNull(6_000L) {
                    runCatching {
                        val repo = OneFamilyRepository(appContext)
                        val remoteProfile = repo.getUserProfileOnce() ?: cachedProfile
                        val remoteHousehold = remoteProfile?.activeHouseholdId?.takeIf { it.isNotBlank() }?.let {
                            repo.getHouseholdOnce(it)
                        } ?: cachedHousehold
                        repo.addExpense(
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
                            isShared = remoteHousehold != null,
                            smsSignature = candidate.id,
                            referenceNumber = parsed.reference.orEmpty(),
                            transactionDateMillis = parsed.timestamp,
                            customDocId = docId
                        )
                    }
                }
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

                TransactionNotificationHelper.postUpdatedCategoryNotification(
                    context = context,
                    notificationId = notificationId,
                    title = title,
                    amountText = amountText,
                    newCategory = newCategory
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
        val candidates = groupedBySender.mapNotNull { (sender, parts) ->
            val fullBody = parts.joinToString(separator = "") { part ->
                part.displayMessageBody ?: part.messageBody.orEmpty()
            }
            val timestamp = parts.firstOrNull()?.timestampMillis?.takeIf { it > 0L }
                ?: System.currentTimeMillis()
            if (sender.isBlank() || fullBody.isBlank()) {
                null
            } else {
                reader.parseRawSms(
                    sender = sender,
                    body = fullBody,
                    timestamp = timestamp,
                    defaultShared = true
                )
            }
        }
        if (candidates.isEmpty()) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                for (candidate in candidates) {
                    TransactionNotificationHelper.processIncomingSmsCandidateAndNotify(
                        context = appContext,
                        candidate = candidate
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

    fun computeTriggerMillisForPeriod(
        period: SpendSummaryPeriod,
        settings: com.example.data.model.FcmPushSettings,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val (hour, minute) = settings.getSummaryTime(period.subCategory)
        return computeNextTriggerMillis(
            hourOfDay = hour,
            minute = minute,
            nowMillis = nowMillis
        )
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

