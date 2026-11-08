package com.example.data.sms

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.AxioRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.ritesh.parser.core.TransactionType
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object TransactionNotificationHelper {
    const val CHANNEL_ID = "axio_realtime_transactions"
    const val EXTRA_EDIT_EXPENSE_ID = "extra_edit_expense_id"
    const val EXTRA_EDIT_HOUSEHOLD_ID = "extra_edit_household_id"
    const val EXTRA_EDIT_VISIBILITY = "extra_edit_visibility"
    const val EXTRA_NEW_CATEGORY = "extra_new_category"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val extraTitle = "extra_tx_title"
    const val extraAmount = "extra_tx_amount"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.BOARD.length && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Real-Time Expense & Category Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant alerts for new SMS transactions with quick category editing"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun showNewTransactionNotification(
        context: Context,
        expenseId: String,
        householdId: String,
        visibility: String,
        title: String,
        amount: Double,
        currentCategory: String,
        currencySymbol: String = "₹"
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
        val formattedAmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
            maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        }.format(amount)

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
        ).filterNot { it.equals(currentCategory, ignoreCase = true) }.take(3)

        val scopeLabel = if (visibility == "SHARED" && householdId.isNotBlank()) "Household Shared" else "Personal"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("$currencySymbol$formattedAmt • $title")
            .setContentText("Category: $currentCategory ($scopeLabel) • Tap to edit")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Category: $currentCategory ($scopeLabel)\nTap notification to open full editor, or tap a button below to change category in real time:"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        quickCategories.forEachIndexed { index, targetCategory ->
            val actionIntent = Intent(context, NotificationCategoryActionReceiver::class.java).apply {
                putExtra(EXTRA_EDIT_EXPENSE_ID, expenseId)
                putExtra(EXTRA_EDIT_HOUSEHOLD_ID, householdId)
                putExtra(EXTRA_EDIT_VISIBILITY, visibility)
                putExtra(EXTRA_NEW_CATEGORY, targetCategory)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(extraTitle, title)
                putExtra(extraAmount, "$currencySymbol$formattedAmt")
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
}

/**
 * Handles clicks on the quick category action buttons inside the Android notification
 * and updates the transaction category in Firestore in real time.
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
        val hasSession = Firebase.auth.currentUser != null ||
            com.example.data.local.LocalAccountSessionManager.getCurrentUser(context) != null
        if (!hasSession) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repo = AxioRepository(context.applicationContext)
                repo.updateExpenseCategoryById(
                    expenseId = expenseId,
                    householdId = householdId,
                    visibility = visibility,
                    newCategory = newCategory
                )

                // Update notification to confirm real-time category change
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                ) {
                    val updatedBuilder = NotificationCompat.Builder(context, TransactionNotificationHelper.CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("$amountText • $title")
                        .setContentText("Category updated to $newCategory ✓")
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
 * Listens for real-time incoming SMS messages (`SMS_RECEIVED_ACTION`), parses bank/UPI alerts
 * via Cashiro's `BankParserFactory`, saves them to Firestore (Shared Household by default when linked),
 * and posts an interactive notification allowing instant category editing.
 */
class IncomingSmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val firebaseUser = Firebase.auth.currentUser
        val localUser = com.example.data.local.LocalAccountSessionManager.getCurrentUser(context)
        if (firebaseUser == null && localUser == null) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages.firstOrNull()?.displayOriginatingAddress.orEmpty()
        val fullBody = messages.joinToString(separator = "") { it.displayMessageBody.orEmpty() }
        val timestamp = messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()

        if (sender.isBlank() || fullBody.isBlank()) return

        val reader = SmsInboxReader(context.applicationContext)
        val candidate = reader.parseRawSms(
            sender = sender,
            body = fullBody,
            timestamp = timestamp,
            defaultShared = true
        ) ?: return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repo = AxioRepository(context.applicationContext)
                val profile = repo.getUserProfileOnce()
                val household = profile?.activeHouseholdId?.takeIf { it.isNotBlank() }?.let {
                    repo.getHouseholdOnce(it)
                }

                val parsed = candidate.parsed
                val isIncome = parsed.type == TransactionType.INCOME
                val merchantTitle = parsed.merchant?.takeIf { it.isNotBlank() }
                    ?: "${parsed.bankName} ${parsed.type.name.lowercase().replaceFirstChar { it.uppercase() }}"
                val effectiveShared = household != null
                val docId = "sms_${candidate.id}"

                val savedResult = repo.addExpense(
                    userName = firebaseUser?.displayName ?: localUser?.displayName ?: profile?.displayName ?: "Member",
                    household = household,
                    title = merchantTitle,
                    amount = parsed.amount.toDouble(),
                    type = if (isIncome) "INCOME" else "EXPENSE",
                    category = candidate.suggestedCategory,
                    accountName = candidate.suggestedAccountName,
                    paymentMode = candidate.suggestedPaymentMode,
                    tags = listOf("sms-auto", parsed.bankName.lowercase().replace(" ", "-")),
                    notes = "Auto-parsed from ${parsed.sender}${parsed.reference?.let { " • Ref: $it" }.orEmpty()}",
                    isShared = effectiveShared,
                    smsSignature = candidate.id,
                    referenceNumber = parsed.reference.orEmpty(),
                    transactionDateMillis = parsed.timestamp,
                    customDocId = docId
                )

                savedResult.onSuccess { savedId ->
                    TransactionNotificationHelper.showNewTransactionNotification(
                        context = context.applicationContext,
                        expenseId = savedId,
                        householdId = household?.id.orEmpty(),
                        visibility = if (effectiveShared) "SHARED" else "PERSONAL",
                        title = merchantTitle,
                        amount = parsed.amount.toDouble(),
                        currentCategory = candidate.suggestedCategory,
                        currencySymbol = profile?.currencySymbol ?: "₹"
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
