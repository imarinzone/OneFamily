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
        // Use a 5-minute bucket (300_000ms) + normalized SMS body & reference so the same SMS
        // produces the exact same ID whether read from SMS_RECEIVED (SMSC timestamp) or
        // content://sms/inbox (device insert timestamp a few seconds later).
        val timeBucket = timestamp / 300_000L
        val normalizedBody = smsBody.trim().replace(Regex("\\s+"), " ").lowercase().take(140)
        val rawKey = "$bankName|$normalizedAmount|${type.name}|${accountNumber.orEmpty()}|${reference.orEmpty().trim().uppercase()}|$timeBucket|$normalizedBody"
        val digest = MessageDigest.getInstance("MD5").digest(rawKey.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(24)
    }
}

data class ParsedBalanceUpdate(
    val bankName: String,
    val accountNumber: String?,
    val balance: BigDecimal,
    val creditLimit: BigDecimal? = null,
    val isFromCard: Boolean = false,
    val smsBody: String,
    val sender: String,
    val timestamp: Long,
    val currency: String = "INR"
)

object CompiledPatterns {
    val DLT_PATTERNS = listOf(
        Regex("^[A-Z]{2}-[A-Z0-9]+-[A-Z]$", RegexOption.IGNORE_CASE),
        Regex("^[A-Z]{2}-[A-Z0-9]+$", RegexOption.IGNORE_CASE),
        Regex("^[A-Z]{2}-[0-9]{5,8}$", RegexOption.IGNORE_CASE),
        Regex("^[A-Z0-9]{5,12}$", RegexOption.IGNORE_CASE)
    )

    val DLT_PREFIX_REGEX = Regex("^[A-Z]{2}[-_]", RegexOption.IGNORE_CASE)
    val DLT_SUFFIX_REGEX = Regex("[-_][A-Z]$", RegexOption.IGNORE_CASE)

    val AMOUNT_PATTERNS = listOf(
        // Currency before amount: Rs. 1,234.50, INR 500, Rs: 250, ₹1,200, Rs-500
        Regex("""(?:Rs\.?|INR\.?|₹|\$|USD|EUR|GBP)[\s:\-]*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        // Amount before currency: 1,234.50 INR, 500 Rs
        Regex("""([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:Rs\.?|INR\.?|₹)""", RegexOption.IGNORE_CASE),
        // Verb + optional preposition + optional currency + amount: "debited by 500.00", "debited with 1200", "debited for 350", "credited by 1000"
        Regex("""(?:debited|credited|deducted|spent|paid|received|withdrawn|sent|transferred|charged|billed|txn\s+of|amt\s+of|amount\s+of|purchase\s+of)\s*(?:by|for|with|of|is|to|from|:)?\s*(?:Rs\.?|INR\.?|₹|\$)?[\s:\-]*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        // "for/of Rs 500" or "Amt 500.00"
        Regex("""\b(?:amt|amount|sum|total|value)[\s:\-of]*(?:Rs\.?|INR\.?|₹)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
    )

    val BALANCE_PATTERNS = listOf(
        // Direct balance phrase + currency + amount: "Avl Bal: Rs. 41,250.00", "Avl Bal of Rs 48,900.25", "Total balance: INR 52,400.00"
        Regex("""(?<!unclear\s)(?<!min\s)(?<!minimum\s)(?<!hold\s)\b(?:avl\.?\s*bal(?:ance)?|available\s+bal(?:ance)?|avail\.?\s*bal(?:ance)?|updated\s+bal(?:ance)?|total\s+bal(?:ance)?|a/c\s+bal(?:ance)?|clear\s+bal(?:ance)?|closing\s+bal(?:ance)?|new\s+bal(?:ance)?|curr(?:ent)?\s+bal(?:ance)?|bal(?:ance)?)[\s:\-]*(?:is|of|:)?[\s:\-]*(?:Rs\.?|INR\.?|₹|\$)[\s:\-]*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        // HDFC & Indian bank daily balance SMS with intervening account/date clause:
        // "Available balance in A/c XX1234 on 21-Nov-26 is INR 1,45,230.50"
        // "Your HDFC Bank A/c *4821 closing balance on 21/11/2026 is Rs. 1,20,500.00"
        // "Bal in HDFC Bank A/c XX1234 on 21-Nov-26 is Rs.1,12,345.67"
        Regex("""(?<!unclear\s)(?<!min\s)(?<!minimum\s)(?<!hold\s)\b(?:avl\.?\s*bal(?:ance)?|available\s+bal(?:ance)?|avail\.?\s*bal(?:ance)?|total\s+bal(?:ance)?|closing\s+bal(?:ance)?|clear\s+bal(?:ance)?|updated\s+bal(?:ance)?|a/c\s+bal(?:ance)?|curr(?:ent)?\s+bal(?:ance)?|bal(?:ance)?)\b[^.\n\r;]{0,90}?\b(?:is|of|at|:)\s*(?:Rs\.?|INR\.?|₹|\$)[\s:\-]*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        // Direct balance phrase without currency symbol: "Avl Bal: 41,250.00"
        Regex("""(?<!unclear\s)(?<!min\s)(?<!minimum\s)(?<!hold\s)\b(?:avl\.?\s*bal(?:ance)?|available\s+bal(?:ance)?|avail\.?\s*bal(?:ance)?|updated\s+bal(?:ance)?|total\s+bal(?:ance)?|a/c\s+bal(?:ance)?|clear\s+bal(?:ance)?|closing\s+bal(?:ance)?|new\s+bal(?:ance)?)[\s:is\-]+([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        // Amount before balance label: "INR 64,210.00 is your available balance"
        Regex("""(?:Rs\.?|INR\.?|₹|\$)\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:is\s+your\s+(?:available|total|closing|clear|account|a/c)?\s*bal(?:ance)?|avl\.?\s*bal)""", RegexOption.IGNORE_CASE)
    )

    val CREDIT_LIMIT_PATTERNS = listOf(
        Regex("""(?:avl\.?\s*(?:cr\.?\s*)?limit|available\s+(?:credit\s+)?limit|curr(?:ent)?\s+limit|limit\s+available)[\s:is\-]*(?:Rs\.?|INR\.?|₹|\$)?[\s:\-]*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
    )

    val ACCOUNT_PATTERNS = listOf(
        Regex("""(?:a/c|acct?|account|card|ac)\s*(?:no\.?|number|ending\s+with|ending|#)?\s*(?:[xX*•.\-]+)?\s*([0-9]{3,4})\b""", RegexOption.IGNORE_CASE),
        Regex("""(?:[xX*•]{2,}|\bXX|\b\*\*)([0-9]{3,4})\b""")
    )

    val REFERENCE_PATTERNS = listOf(
        Regex("""(?:UPI\s*Ref(?:\s*No\.?)?|Ref(?:\s*No\.?)?|RRN|UTR|txn\s*id|reference\s*no\.?|imps\s*ref)[\s:\-#]*([A-Za-z0-9]{6,22})""", RegexOption.IGNORE_CASE),
        Regex("""UPI[:/\-]([0-9]{9,14})""", RegexOption.IGNORE_CASE)
    )
}
