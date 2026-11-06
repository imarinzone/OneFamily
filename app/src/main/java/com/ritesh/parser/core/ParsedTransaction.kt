package com.ritesh.parser.core

import java.math.BigDecimal
import java.security.MessageDigest

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER,
    INVESTMENT,
    CREDIT
}

data class ParsedTransaction(
    val amount: BigDecimal,
    val type: TransactionType,
    val merchant: String?,
    val reference: String?,
    val accountNumber: String?,
    val balance: BigDecimal?,
    val creditLimit: BigDecimal? = null,
    val smsBody: String,
    val sender: String,
    val timestamp: Long,
    val bankName: String,
    val isFromCard: Boolean = false,
    val currency: String = "INR"
) {
    fun generateTransactionId(): String {
        val normalizedAmount = amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
        val bucketTime = timestamp / 60_000L // 1-minute bucket for SMS deduplication
        val rawKey = "$sender|$bankName|$normalizedAmount|${type.name}|${accountNumber.orEmpty()}|${reference.orEmpty()}|$bucketTime"
        val digest = MessageDigest.getInstance("MD5").digest(rawKey.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(24)
    }
}

object CompiledPatterns {
    val DLT_PATTERNS = listOf(
        Regex("^[A-Z]{2}-[A-Z0-9]+-[A-Z]$", RegexOption.IGNORE_CASE),
        Regex("^[A-Z]{2}-[A-Z0-9]+$", RegexOption.IGNORE_CASE),
        Regex("^[A-Z]{2}-[0-9]{6}$", RegexOption.IGNORE_CASE)
    )

    val DLT_PREFIX_REGEX = Regex("^[A-Z]{2}-", RegexOption.IGNORE_CASE)
    val DLT_SUFFIX_REGEX = Regex("-[A-Z]$", RegexOption.IGNORE_CASE)

    val AMOUNT_PATTERNS = listOf(
        Regex("""(?:Rs\.?|INR|₹|\$|USD|EUR|GBP)\s*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:Rs\.?|INR|₹)""", RegexOption.IGNORE_CASE),
        Regex("""(?:debited|credited|spent|paid|received|withdrawn|txn\s+of|amt\s+of|amount\s+of)\s+(?:by|for|with|of)?\s*(?:Rs\.?|INR|₹|\$)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
    )

    val BALANCE_PATTERNS = listOf(
        Regex("""(?:avl\.?\s*bal(?:ance)?|available\s+balance|bal(?:ance)?|updated\s+balance|total\s+bal|a/c\s+bal|clear\s+bal)[\s:is\-]*(?:Rs\.?|INR|₹|\$)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(?:Rs\.?|INR|₹|\$)\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:is\s+your\s+available\s+balance|avl\s+bal)""", RegexOption.IGNORE_CASE)
    )

    val CREDIT_LIMIT_PATTERNS = listOf(
        Regex("""(?:avl\.?\s*(?:cr\.?\s*)?limit|available\s+(?:credit\s+)?limit|curr(?:ent)?\s+limit)[\s:is\-]*(?:Rs\.?|INR|₹|\$)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
    )

    val ACCOUNT_PATTERNS = listOf(
        Regex("""(?:a/c|acct?|account|card)\s*(?:no\.?|number|ending\s+with|ending)?\s*(?:[xX*•]+)?\s*([0-9]{3,4})\b""", RegexOption.IGNORE_CASE),
        Regex("""(?:[xX*•]{2,})([0-9]{3,4})\b""")
    )

    val REFERENCE_PATTERNS = listOf(
        Regex("""(?:UPI\s*Ref(?:\s*No\.?)?|Ref(?:\s*No\.?)?|RRN|UTR|txn\s*id|reference\s*no\.?)[\s:\-#]*([A-Za-z0-9]{6,22})""", RegexOption.IGNORE_CASE),
        Regex("""UPI[:/\-]([0-9]{9,14})""", RegexOption.IGNORE_CASE)
    )
}
