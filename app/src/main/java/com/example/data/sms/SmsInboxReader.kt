package com.example.data.sms

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.ritesh.parser.core.ParsedTransaction
import com.ritesh.parser.core.bank.BankParserFactory

data class SmsCandidate(
    val id: String,
    val parsed: ParsedTransaction,
    val suggestedCategory: String,
    val suggestedAccountName: String,
    val suggestedPaymentMode: String,
    val isSharedByDefault: Boolean = true
)

class SmsInboxReader(private val context: Context) {

    /**
     * Reads recent SMS messages from the Android device's `content://sms/inbox`
     * and parses transactional bank/UPI/card alerts using Cashiro's `BankParserFactory`.
     */
    fun scanDeviceSmsInbox(
        existingSignatures: Set<String>,
        maxMessages: Int = 250
    ): List<SmsCandidate> {
        val candidates = mutableListOf<SmsCandidate>()
        val seenSignatures = existingSignatures.toMutableSet()

        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        try {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )?.use { cursor ->
                val addressIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)

                var count = 0
                while (cursor.moveToNext() && count < maxMessages) {
                    count++
                    val sender = cursor.getString(addressIdx).orEmpty()
                    val body = cursor.getString(bodyIdx).orEmpty()
                    val timestamp = cursor.getLong(dateIdx)

                    if (sender.isBlank() || body.isBlank()) continue

                    val parsed = BankParserFactory.parseSms(
                        smsBody = body,
                        sender = sender,
                        timestamp = timestamp
                    ) ?: continue

                    val signature = parsed.generateTransactionId()
                    if (signature in seenSignatures) continue
                    seenSignatures.add(signature)

                    val category = BankParserFactory.inferCategory(parsed)
                    val isSharedDefault = category in setOf(
                        "Groceries",
                        "Food & Dining",
                        "Rent & Housing",
                        "Bills & Utilities",
                        "Fuel & Travel"
                    )

                    candidates.add(
                        SmsCandidate(
                            id = signature,
                            parsed = parsed,
                            suggestedCategory = category,
                            suggestedAccountName = BankParserFactory.formatAccountDisplayName(parsed),
                            suggestedPaymentMode = BankParserFactory.inferPaymentMode(parsed),
                            isSharedByDefault = isSharedDefault
                        )
                    )
                }
            }
        } catch (_: SecurityException) {
            // Handled by caller when READ_SMS is not granted
        } catch (_: Exception) {
            // Safe fallback if Telephony provider is unavailable on device
        }

        return candidates
    }

    /**
     * Parses a single SMS message string using Cashiro's `BankParserFactory`.
     */
    fun parseRawSms(
        sender: String,
        body: String,
        timestamp: Long = System.currentTimeMillis(),
        defaultShared: Boolean? = null
    ): SmsCandidate? {
        val parsed = BankParserFactory.parseSms(
            smsBody = body,
            sender = sender,
            timestamp = timestamp
        ) ?: return null

        val signature = parsed.generateTransactionId()
        val category = BankParserFactory.inferCategory(parsed)
        val isShared = defaultShared ?: (category in setOf(
            "Groceries",
            "Food & Dining",
            "Rent & Housing",
            "Bills & Utilities",
            "Fuel & Travel"
        ))

        return SmsCandidate(
            id = signature,
            parsed = parsed,
            suggestedCategory = category,
            suggestedAccountName = BankParserFactory.formatAccountDisplayName(parsed),
            suggestedPaymentMode = BankParserFactory.inferPaymentMode(parsed),
            isSharedByDefault = isShared
        )
    }
}
