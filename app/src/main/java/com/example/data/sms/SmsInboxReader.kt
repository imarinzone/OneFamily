package com.example.data.sms

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.example.data.local.LocalAccountDataStore
import com.ritesh.parser.core.ParsedTransaction
import com.ritesh.parser.core.bank.BankParserFactory
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

data class SmsCandidate(
    val id: String,
    val parsed: ParsedTransaction,
    val suggestedCategory: String,
    val suggestedAccountName: String,
    val suggestedPaymentMode: String,
    val isSharedByDefault: Boolean = true
)

data class RawSmsRow(
    val sender: String,
    val body: String,
    val timestamp: Long
)

class SmsInboxReader(private val context: Context) {

    /**
     * Parses a list of [RawSmsRow] items in parallel batches using a dedicated fixed thread pool
     * of at least [MIN_PARALLEL_THREADS] (5+) threads.
     *
     * Thread-Safety & Race-Condition Prevention:
     * - `signatureLock` guards `seenSignatures` so checking and inserting both `signature` and
     *   `contentKey` is strictly atomic across all reader threads.
     * - `progressLock` serializes progress and streaming batch callbacks (`onBatchParsed`) so
     *   downstream writer threads receive clean, non-overlapping batches immediately as they are read.
     */
    suspend fun parseSmsBatchesInParallel(
        rawRows: List<RawSmsRow>,
        existingSignatures: Set<String>,
        batchSize: Int = DEFAULT_SMS_BATCH_SIZE,
        readerThreads: Int = DEFAULT_PARALLEL_READER_THREADS,
        perBatchDelayMs: Long = 35L,
        onProgress: suspend (readCount: Int, totalCount: Int, matchedCount: Int) -> Unit = { _, _, _ -> },
        onBatchParsed: suspend (
            batchCandidates: List<SmsCandidate>,
            readCount: Int,
            totalCount: Int,
            matchedCount: Int,
            latestCandidate: SmsCandidate?
        ) -> Unit = { _, _, _, _, _ -> }
    ): List<SmsCandidate> {
        val totalCount = rawRows.size
        if (totalCount == 0) {
            onProgress(0, 0, 0)
            return emptyList()
        }

        val effectiveThreads = readerThreads.coerceAtLeast(MIN_PARALLEL_THREADS)
        val effectiveBatchSize = batchSize.coerceAtLeast(1)
        val signatureLock = Any()
        val seenSignatures = existingSignatures.toMutableSet()
        val allCandidates = mutableListOf<SmsCandidate>()
        val readCounter = AtomicInteger(0)
        val matchedCounter = AtomicInteger(0)

        onProgress(0, totalCount, 0)

        val batches = rawRows.chunked(effectiveBatchSize)
        val readerExecutor = Executors.newFixedThreadPool(effectiveThreads) { runnable ->
            Thread(runnable, "SmsReaderPool-Worker").apply { isDaemon = true }
        }
        val readerDispatcher = readerExecutor.asCoroutineDispatcher()
        val concurrencySemaphore = Semaphore(effectiveThreads)

        try {
            coroutineScope {
                val deferredBatches = batches.mapIndexed { batchIndex, batchRows ->
                    async(readerDispatcher) {
                        concurrencySemaphore.withPermit {
                            val batchCandidates = mutableListOf<SmsCandidate>()
                            var batchReadCount = 0

                            for (row in batchRows) {
                                batchReadCount++
                                runCatching {
                                    val sender = row.sender
                                    val body = row.body
                                    val timestamp = row.timestamp

                                    if (sender.isNotBlank() && body.isNotBlank()) {
                                        val parsed = BankParserFactory.parseSms(
                                            smsBody = body,
                                            sender = sender,
                                            timestamp = timestamp
                                        )
                                        if (parsed != null) {
                                            val signature = parsed.generateTransactionId()
                                            val contentKey = buildSmsContentKey(body, timestamp)
                                            LocalAccountDataStore.cacheSmsMessage(
                                                signature = signature,
                                                sender = sender,
                                                body = body,
                                                timestamp = timestamp,
                                                persistImmediately = false
                                            )

                                            val acceptedCandidate: SmsCandidate? = synchronized(signatureLock) {
                                                if (signature !in seenSignatures && contentKey !in seenSignatures) {
                                                    seenSignatures.add(signature)
                                                    seenSignatures.add(contentKey)
                                                    val category = BankParserFactory.inferCategory(parsed)
                                                    val candidate = SmsCandidate(
                                                        id = signature,
                                                        parsed = parsed,
                                                        suggestedCategory = category,
                                                        suggestedAccountName = BankParserFactory.formatAccountDisplayName(parsed),
                                                        suggestedPaymentMode = BankParserFactory.inferPaymentMode(parsed),
                                                        isSharedByDefault = true
                                                    )
                                                    allCandidates.add(candidate)
                                                    candidate
                                                } else {
                                                    null
                                                }
                                            }

                                            if (acceptedCandidate != null) {
                                                batchCandidates.add(acceptedCandidate)
                                            }
                                        }
                                    }
                                }.onFailure { rowErr ->
                                    com.example.data.local.LocalAccountSessionManager.recordDiagnostic(
                                        context = context,
                                        subsystem = "SMS_AUTO_SYNC",
                                        severity = "NON_FATAL",
                                        message = "Guarded SMS batch #$batchIndex row: ${rowErr.javaClass.simpleName}"
                                    )
                                }
                            }

                            val currentRead = readCounter.addAndGet(batchReadCount).coerceAtMost(totalCount)
                            val currentMatched = matchedCounter.addAndGet(batchCandidates.size)
                            val latestCandidate = batchCandidates.lastOrNull()

                            onProgress(currentRead, totalCount, currentMatched)
                            onBatchParsed(
                                batchCandidates,
                                currentRead,
                                totalCount,
                                currentMatched,
                                latestCandidate
                            )

                            if (perBatchDelayMs > 0L && currentRead < totalCount) {
                                delay(perBatchDelayMs)
                            }
                            batchCandidates
                        }
                    }
                }
                deferredBatches.awaitAll()
            }
        } finally {
            runCatching { readerDispatcher.close() }
            runCatching { readerExecutor.shutdown() }
        }

        LocalAccountDataStore.persistSmsRawCache()
        return synchronized(signatureLock) {
            allCandidates.sortedBy { it.parsed.timestamp }
        }
    }

    /**
     * Reads SMS messages from the phone (`content://sms/inbox` and fallback `content://sms`)
     * and processes them in parallel batches across at least 5 reader threads (`readerThreads = 6`),
     * streaming each parsed batch via `onBatchParsed` so transaction writer threads can save them
     * concurrently without waiting for the entire inbox scan to finish.
     */
    suspend fun scanDeviceSmsInboxWithProgress(
        existingSignatures: Set<String>,
        maxMessages: Int = Int.MAX_VALUE,
        batchSize: Int = DEFAULT_SMS_BATCH_SIZE,
        readerThreads: Int = DEFAULT_PARALLEL_READER_THREADS,
        perBatchDelayMs: Long = 45L,
        onProgress: suspend (readCount: Int, totalCount: Int, matchedCount: Int) -> Unit = { _, _, _ -> },
        onBatchParsed: suspend (
            batchCandidates: List<SmsCandidate>,
            readCount: Int,
            totalCount: Int,
            matchedCount: Int,
            latestCandidate: SmsCandidate?
        ) -> Unit = { _, _, _, _, _ -> }
    ): List<SmsCandidate> = withContext(Dispatchers.IO) {
        val rawRows = mutableListOf<RawSmsRow>()

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
                    val seenRawKeys = mutableSetOf<String>()
                    var cursorRead = 0
                    while (cursor.moveToNext() && cursorRead < effectiveTotal) {
                        cursorRead++
                        val isInboxMsg = if (uri == Telephony.Sms.CONTENT_URI && typeIdx >= 0) {
                            cursor.getInt(typeIdx) == Telephony.Sms.MESSAGE_TYPE_INBOX
                        } else {
                            true
                        }
                        if (isInboxMsg) {
                            runCatching {
                                val sender = cursor.getString(addressIdx).orEmpty()
                                val body = cursor.getString(bodyIdx).orEmpty()
                                val timestamp = cursor.getLong(dateIdx)
                                val key = "$sender|$timestamp|${body.trim()}"
                                if (seenRawKeys.add(key)) {
                                    rawRows.add(RawSmsRow(sender = sender, body = body, timestamp = timestamp))
                                }
                            }
                        } else {
                            // Keep total count accurate for progress bar even for non-inbox rows
                            rawRows.add(RawSmsRow(sender = "", body = "", timestamp = 0L))
                        }
                    }
                }
            } catch (_: SecurityException) {
                // Handled by caller when READ_SMS is not granted
            } catch (e: Exception) {
                com.example.data.local.LocalAccountSessionManager.recordDiagnostic(
                    context = context,
                    subsystem = "SMS_AUTO_SYNC",
                    severity = "NON_FATAL",
                    message = "Guarded SMS provider query on $uri: ${e.javaClass.simpleName}"
                )
            }
        }

        parseSmsBatchesInParallel(
            rawRows = rawRows,
            existingSignatures = existingSignatures,
            batchSize = batchSize,
            readerThreads = readerThreads.coerceAtLeast(MIN_PARALLEL_THREADS),
            perBatchDelayMs = perBatchDelayMs,
            onProgress = onProgress,
            onBatchParsed = onBatchParsed
        )
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
                    if (cursor.count > 0) processedAnyUri = true

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
                        val contentKey = buildSmsContentKey(body, timestamp)
                        LocalAccountDataStore.cacheSmsMessage(
                            signature = signature,
                            sender = sender,
                            body = body,
                            timestamp = timestamp,
                            persistImmediately = false
                        )
                        if (signature in seenSignatures || contentKey in seenSignatures) continue
                        seenSignatures.add(signature)
                        seenSignatures.add(contentKey)

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

    companion object {
        const val MIN_PARALLEL_THREADS = 5
        const val DEFAULT_PARALLEL_READER_THREADS = 6
        const val DEFAULT_PARALLEL_WRITER_THREADS = 6
        const val DEFAULT_SMS_BATCH_SIZE = 12

        fun buildSmsContentKey(body: String, timestamp: Long): String {
            val cleanBody = body.trim().replace(Regex("\\s+"), " ").lowercase()
            val window = timestamp / 300_000L
            return "sms_content_${window}_${cleanBody.hashCode()}"
        }
    }
}
