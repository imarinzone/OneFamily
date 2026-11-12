package com.example.data.sms

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.example.data.local.LocalAccountDataStore
import com.ritesh.parser.core.ParsedTransaction
import com.ritesh.parser.core.bank.BankParserFactory
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
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
     * of constant size ([DEFAULT_PARALLEL_READER_THREADS]).
     *
     * Thread-Safety & Race-Condition Prevention (Always Active):
     * - `signatureLock` atomically guards `seenSignatures` and sliding-window `seenBodyTimestamps`
     *   (`±10 minutes`) so identical SMS messages — even straddling a 5-minute boundary — can never
     *   produce duplicate candidates across parallel reader threads.
     * - `progressMutex` serializes `onProgress` and `onBatchParsed` callbacks so `readCount` and
     *   `matchedCount` are strictly monotonic and downstream writer threads receive clean batches.
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

        val effectiveThreads = readerThreads.coerceIn(MIN_PARALLEL_THREADS, DEFAULT_PARALLEL_READER_THREADS)
        val effectiveBatchSize = batchSize.coerceAtLeast(1)
        val signatureLock = Any()
        val progressMutex = Mutex()
        val seenSignatures = existingSignatures.toMutableSet()
        val seenBodyTimestamps = mutableMapOf<String, MutableList<Long>>()
        val allCandidates = mutableListOf<SmsCandidate>()
        val readCounter = AtomicInteger(0)
        val matchedCounter = AtomicInteger(0)
        var maxReportedRead = 0
        var maxReportedMatched = 0

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
                                            val adjacentKeys = buildSmsAdjacentContentKeys(body, timestamp)
                                            val normalizedBody = normalizeSmsBody(body)
                                            val bodyIdentityKey = if (normalizedBody.length >= 15) {
                                                "${parsed.type.name}|${String.format(Locale.US, "%.2f", parsed.amount.toDouble())}|$normalizedBody"
                                            } else {
                                                ""
                                            }

                                            LocalAccountDataStore.cacheSmsMessage(
                                                signature = signature,
                                                sender = sender,
                                                body = body,
                                                timestamp = timestamp,
                                                persistImmediately = false
                                            )

                                            val acceptedCandidate: SmsCandidate? = synchronized(signatureLock) {
                                                val alreadySeenBySigOrKey = signature in seenSignatures ||
                                                    contentKey in seenSignatures ||
                                                    adjacentKeys.any { it in seenSignatures }
                                                val alreadySeenBySlidingWindow = if (!alreadySeenBySigOrKey && bodyIdentityKey.isNotEmpty()) {
                                                    seenBodyTimestamps[bodyIdentityKey]?.any { prevTs ->
                                                        abs(timestamp - prevTs) <= SLIDING_DEDUP_WINDOW_MS
                                                    } == true
                                                } else {
                                                    false
                                                }

                                                if (!alreadySeenBySigOrKey && !alreadySeenBySlidingWindow) {
                                                    seenSignatures.add(signature)
                                                    seenSignatures.add(contentKey)
                                                    seenSignatures.addAll(adjacentKeys)
                                                    if (bodyIdentityKey.isNotEmpty()) {
                                                        seenBodyTimestamps.getOrPut(bodyIdentityKey) { mutableListOf() }.add(timestamp)
                                                    }
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

                            val currentReadRaw = readCounter.addAndGet(batchReadCount).coerceAtMost(totalCount)
                            val currentMatchedRaw = matchedCounter.addAndGet(batchCandidates.size)
                            val latestCandidate = batchCandidates.lastOrNull()

                            progressMutex.withLock {
                                if (currentReadRaw > maxReportedRead) maxReportedRead = currentReadRaw
                                if (currentMatchedRaw > maxReportedMatched) maxReportedMatched = currentMatchedRaw
                                val monotonicRead = maxReportedRead
                                val monotonicMatched = maxReportedMatched

                                onProgress(monotonicRead, totalCount, monotonicMatched)
                                onBatchParsed(
                                    batchCandidates,
                                    monotonicRead,
                                    totalCount,
                                    monotonicMatched,
                                    latestCandidate
                                )
                            }

                            if (perBatchDelayMs > 0L && currentReadRaw < totalCount) {
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
     * and processes them in parallel batches across [DEFAULT_PARALLEL_READER_THREADS] threads,
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
                val selection = if (uri == Telephony.Sms.CONTENT_URI) {
                    "${Telephony.Sms.TYPE} = ?"
                } else {
                    null
                }
                val selectionArgs = if (uri == Telephony.Sms.CONTENT_URI) {
                    arrayOf(Telephony.Sms.MESSAGE_TYPE_INBOX.toString())
                } else {
                    null
                }
                context.contentResolver.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
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
                                if (sender.isNotBlank() && body.isNotBlank() && seenRawKeys.add(key)) {
                                    rawRows.add(RawSmsRow(sender = sender, body = body, timestamp = timestamp))
                                }
                            }
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
            readerThreads = readerThreads.coerceIn(MIN_PARALLEL_THREADS, DEFAULT_PARALLEL_READER_THREADS),
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
        val seenBodyTimestamps = mutableMapOf<String, MutableList<Long>>()

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
                val selection = if (uri == Telephony.Sms.CONTENT_URI) {
                    "${Telephony.Sms.TYPE} = ?"
                } else {
                    null
                }
                val selectionArgs = if (uri == Telephony.Sms.CONTENT_URI) {
                    arrayOf(Telephony.Sms.MESSAGE_TYPE_INBOX.toString())
                } else {
                    null
                }
                context.contentResolver.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
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
                        val adjacentKeys = buildSmsAdjacentContentKeys(body, timestamp)
                        val normalizedBody = normalizeSmsBody(body)
                        val bodyIdentityKey = if (normalizedBody.length >= 15) {
                            "${parsed.type.name}|${String.format(Locale.US, "%.2f", parsed.amount.toDouble())}|$normalizedBody"
                        } else {
                            ""
                        }

                        LocalAccountDataStore.cacheSmsMessage(
                            signature = signature,
                            sender = sender,
                            body = body,
                            timestamp = timestamp,
                            persistImmediately = false
                        )
                        if (signature in seenSignatures ||
                            contentKey in seenSignatures ||
                            adjacentKeys.any { it in seenSignatures }
                        ) continue

                        if (bodyIdentityKey.isNotEmpty() &&
                            seenBodyTimestamps[bodyIdentityKey]?.any { abs(timestamp - it) <= SLIDING_DEDUP_WINDOW_MS } == true
                        ) continue

                        seenSignatures.add(signature)
                        seenSignatures.add(contentKey)
                        seenSignatures.addAll(adjacentKeys)
                        if (bodyIdentityKey.isNotEmpty()) {
                            seenBodyTimestamps.getOrPut(bodyIdentityKey) { mutableListOf() }.add(timestamp)
                        }

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
        const val SLIDING_DEDUP_WINDOW_MS = 600_000L
        private const val BOUNDARY_GRACE_MS = 90_000L

        fun normalizeSmsBody(body: String): String =
            body.trim().replace(Regex("\\s+"), " ").lowercase(Locale.US)

        /**
         * Extracts the original raw SMS body from a transaction `notes` field, stripping any
         * `[SENDER] ` prefix and any appended `\n[Linked Duplicate: ...]` metadata suffix.
         */
        fun extractRawSmsBodyFromNotes(notes: String): String {
            val withoutLinkSuffix = notes
                .substringBefore("\n[Linked Duplicate:")
                .substringBefore("[Linked Duplicate:")
                .trim()
            return withoutLinkSuffix.substringAfter("] ", withoutLinkSuffix).trim()
        }

        fun buildSmsContentKey(body: String, timestamp: Long): String {
            val cleanBody = normalizeSmsBody(body)
            val window = timestamp / 300_000L
            return "sms_content_${window}_${cleanBody.hashCode()}"
        }

        /**
         * Emits content keys for the current 5-minute window AND any adjacent 5-minute window
         * within ±90 seconds so SMSC vs content-provider arrival timestamps straddling a 5-minute
         * boundary (e.g. 04:59.5 vs 05:01.0) always match as duplicates.
         */
        fun buildSmsAdjacentContentKeys(body: String, timestamp: Long): List<String> {
            val cleanBody = normalizeSmsBody(body)
            if (cleanBody.isBlank()) return emptyList()
            val hash = cleanBody.hashCode()
            val wCenter = timestamp / 300_000L
            val wBefore = (timestamp - BOUNDARY_GRACE_MS).coerceAtLeast(0L) / 300_000L
            val wAfter = (timestamp + BOUNDARY_GRACE_MS) / 300_000L
            return setOf(wBefore, wCenter, wAfter).map { w -> "sms_content_${w}_$hash" }
        }
    }
}
