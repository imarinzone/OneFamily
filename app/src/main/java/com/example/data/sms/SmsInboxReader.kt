package com.example.data.sms

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.example.data.local.LocalAccountDataStore
import com.ritesh.parser.core.ParsedTransaction
import com.ritesh.parser.core.bank.BankParserFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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
     * Reads SMS messages from the phone (`content://sms/inbox` and fallback `content://sms`)
     * on `Dispatchers.IO` with paced batch delays (`perBatchDelayMs`) so the UI never hangs or crashes,
     * and reports real-time progress (`readCount`, `totalCount`, `matchedCount`) to the caller.
     */
    suspend fun scanDeviceSmsInboxWithProgress(
        existingSignatures: Set<String>,
        maxMessages: Int = Int.MAX_VALUE,
        batchSize: Int = 12,
        perBatchDelayMs: Long = 55L,
        onProgress: suspend (readCount: Int, totalCount: Int, matchedCount: Int) -> Unit = { _, _, _ -> }
    ): List<SmsCandidate> = withContext(Dispatchers.IO) {
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

        var processedAnyUri = false
        for (uri: Uri in urisToTry) {
            if (processedAnyUri) break
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

                    val rawTotal = cursor.count.coerceAtLeast(0)
                    val effectiveTotal = rawTotal.coerceAtMost(maxMessages)
                    if (effectiveTotal == 0) return@use

                    processedAnyUri = true
                    onProgress(0, effectiveTotal, 0)

                    var readCount = 0
                    while (cursor.moveToNext() && readCount < effectiveTotal) {
                        readCount++

                        // If querying Telephony.Sms.CONTENT_URI, only read received/inbox messages (type == 1)
                        val isInboxMsg = if (uri == Telephony.Sms.CONTENT_URI && typeIdx >= 0) {
                            cursor.getInt(typeIdx) == Telephony.Sms.MESSAGE_TYPE_INBOX
                        } else {
                            true
                        }

                        if (isInboxMsg) {
                            val sender = cursor.getString(addressIdx).orEmpty()
                            val body = cursor.getString(bodyIdx).orEmpty()
                            val timestamp = cursor.getLong(dateIdx)

                            if (sender.isNotBlank() && body.isNotBlank()) {
                                val parsed = BankParserFactory.parseSms(
                                    smsBody = body,
                                    sender = sender,
                                    timestamp = timestamp
                                )
                                if (parsed != null) {
                                    val signature = parsed.generateTransactionId()
                                    LocalAccountDataStore.cacheSmsMessage(
                                        signature = signature,
                                        sender = sender,
                                        body = body,
                                        timestamp = timestamp,
                                        persistImmediately = false
                                    )
                                    if (signature !in seenSignatures) {
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
                            }
                        }

                        if (readCount % batchSize == 0 || readCount == effectiveTotal) {
                            onProgress(readCount, effectiveTotal, candidates.size)
                            if (perBatchDelayMs > 0L && readCount < effectiveTotal) {
                                delay(perBatchDelayMs)
                            }
                        }
                    }
                    LocalAccountDataStore.persistSmsRawCache()
                }
            } catch (_: SecurityException) {
                // Handled by caller when READ_SMS is not granted
            } catch (_: Exception) {
                // Safe fallback if Telephony provider is unavailable
            }
        }

        candidates.sortedBy { it.parsed.timestamp }
    }

    /**
     * Synchronous helper for quick small queries or unit tests.
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
                        LocalAccountDataStore.cacheSmsMessage(
                            signature = signature,
                            sender = sender,
                            body = body,
                            timestamp = timestamp,
                            persistImmediately = false
                        )
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
                    LocalAccountDataStore.persistSmsRawCache()
                }
                if (candidates.isNotEmpty()) break
            } catch (_: SecurityException) {
            } catch (_: Exception) {
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
        LocalAccountDataStore.cacheSmsMessage(
            signature = signature,
            sender = sender,
            body = body,
            timestamp = timestamp
        )
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
