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
     * Reads ALL SMS messages from the phone (`content://sms/inbox` and fallback `content://sms`)
     * without an artificial row cap, parses every transactional bank/UPI/card alert using
     * Cashiro's `BankParserFactory`, and returns them ordered by timestamp (`DATE ASC`)
     * so they are added chronologically by time.
     */
    fun scanDeviceSmsInbox(
        existingSignatures: Set<String>,
        maxMessages: Int = Int.MAX_VALUE
    ): List<SmsCandidate> {
        val candidates = mutableListOf<SmsCandidate>()
        val seenSignatures = existingSignatures.toMutableSet()

        val urisToTry = listOf(
            Telephony.Sms.Inbox.CONTENT_URI,
            Telephony.Sms.CONTENT_URI
        )

        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )

        for (uri: Uri in urisToTry) {
            try {
                context.contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    "${Telephony.Sms.DATE} ASC"
                )?.use { cursor ->
                    val addressIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                    val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                    val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
                    val typeIdx = cursor.getColumnIndex(Telephony.Sms.TYPE)

                    if (addressIdx < 0 || bodyIdx < 0 || dateIdx < 0) return@use

                    var count = 0
                    while (cursor.moveToNext() && count < maxMessages) {
                        // If querying Telephony.Sms.CONTENT_URI, only read received/inbox messages (type == 1)
                        if (uri == Telephony.Sms.CONTENT_URI && typeIdx >= 0) {
                            val msgType = cursor.getInt(typeIdx)
                            if (msgType != Telephony.Sms.MESSAGE_TYPE_INBOX) continue
                        }

                        val sender = cursor.getString(addressIdx).orEmpty()
                        val body = cursor.getString(bodyIdx).orEmpty()
                        val timestamp = cursor.getLong(dateIdx)

                        if (sender.isBlank() || body.isBlank()) continue
                        count++

                        val parsed = BankParserFactory.parseSms(
                            smsBody = body,
                            sender = sender,
                            timestamp = timestamp
                        ) ?: continue

                        val signature = parsed.generateTransactionId()
                        if (signature in seenSignatures) continue
                        seenSignatures.add(signature)

                        val category = BankParserFactory.inferCategory(parsed)

                        candidates.add(
                            SmsCandidate(
                                id = signature,
                                parsed = parsed,
                                suggestedCategory = category,
                                suggestedAccountName = BankParserFactory.formatAccountDisplayName(parsed),
                                suggestedPaymentMode = BankParserFactory.inferPaymentMode(parsed),
                                isSharedByDefault = true
                            )
                        )
                    }
                }
                if (candidates.isNotEmpty()) break
            } catch (_: SecurityException) {
                // Handled by caller when READ_SMS is not granted
            } catch (_: Exception) {
                // Safe fallback if Telephony provider is unavailable
            }
        }

        return candidates.sortedBy { it.parsed.timestamp }
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
        val isShared = defaultShared ?: true

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
