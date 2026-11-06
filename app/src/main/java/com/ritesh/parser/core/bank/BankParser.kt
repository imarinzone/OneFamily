package com.ritesh.parser.core.bank

import com.ritesh.parser.core.CompiledPatterns
import com.ritesh.parser.core.ParsedTransaction
import com.ritesh.parser.core.TransactionType
import java.math.BigDecimal

/**
 * Base abstract class for all bank parsers following the Cashiro
 * `com.ritesh.parser.core.bank.BankParser` architecture.
 */
abstract class BankParser {

    abstract val bankName: String

    abstract fun canHandle(sender: String): Boolean

    open fun parse(
        smsBody: String,
        sender: String,
        timestamp: Long
    ): ParsedTransaction? {
        if (!isTransactionMessage(smsBody)) {
            return null
        }

        val amount = extractAmount(smsBody) ?: return null
        if (amount <= BigDecimal.ZERO) return null

        val type = extractTransactionType(smsBody) ?: return null
        val accountNumber = extractAccountNumber(smsBody)
        val balance = extractBalance(smsBody)
        val availableLimit = extractAvailableLimit(smsBody)
        val reference = extractReference(smsBody)
        val isCard = detectIsCard(smsBody)
        val rawMerchant = extractMerchant(smsBody, sender)
        val cleanedMerchant = rawMerchant?.let { cleanMerchantName(it) }?.takeIf { it.isNotBlank() }

        val finalType = if (type == TransactionType.EXPENSE && isCard) {
            TransactionType.CREDIT
        } else {
            type
        }

        return ParsedTransaction(
            amount = amount,
            type = finalType,
            merchant = cleanedMerchant,
            reference = reference,
            accountNumber = accountNumber,
            balance = balance,
            creditLimit = availableLimit,
            smsBody = smsBody,
            sender = sender,
            timestamp = timestamp,
            bankName = bankName,
            isFromCard = isCard
        )
    }

    protected open fun extractAmount(message: String): BigDecimal? {
        for (pattern in CompiledPatterns.AMOUNT_PATTERNS) {
            val match = pattern.find(message)
            if (match != null) {
                val raw = match.groupValues[1].replace(",", "").trim()
                val parsed = raw.toBigDecimalOrNull()
                if (parsed != null && parsed > BigDecimal.ZERO) {
                    return parsed
                }
            }
        }
        return null
    }

    protected open fun extractTransactionType(message: String): TransactionType? {
        val lowerMessage = message.lowercase()

        val investmentKeywords = listOf(
            "mutual fund", "sip", "zerodha", "groww", "kuvera", "upstox",
            "nps", "ppf", "folio", "nav", "allotted", "units allotted", "indmoney"
        )
        if (investmentKeywords.any { lowerMessage.contains(it) } &&
            (lowerMessage.contains("debited") || lowerMessage.contains("paid") || lowerMessage.contains("invested") || lowerMessage.contains("purchase"))
        ) {
            return TransactionType.INVESTMENT
        }

        val expenseKeywords = listOf(
            "debited", "spent", "paid", "withdrawn", "sent", "charged",
            "purchase", "billed", "deducted", "used at", "transferred to",
            "dr ", "dr.", "debit", "autopay", "auto-debit", "emi"
        )
        val incomeKeywords = listOf(
            "credited", "received", "deposited", "refund", "cashback",
            "salary", "cr ", "cr.", "credit to", "added to", "reversal"
        )

        val debitIndex = expenseKeywords
            .map { lowerMessage.indexOf(it) }
            .filter { it >= 0 }
            .minOrNull()

        val creditIndex = incomeKeywords
            .map { lowerMessage.indexOf(it) }
            .filter { it >= 0 }
            .minOrNull()

        // Handle "credited to <payee> and debited from your a/c"
        if (debitIndex != null && creditIndex != null) {
            if (lowerMessage.contains("debited from") || lowerMessage.contains("a/c debited") || lowerMessage.contains("sent rs") || lowerMessage.contains("spent on")) {
                return TransactionType.EXPENSE
            }
            if (lowerMessage.contains("credited to your") || lowerMessage.contains("a/c credited") || lowerMessage.contains("received in")) {
                return TransactionType.INCOME
            }
            return if (debitIndex < creditIndex) TransactionType.EXPENSE else TransactionType.INCOME
        }

        if (debitIndex != null) return TransactionType.EXPENSE
        if (creditIndex != null) return TransactionType.INCOME

        return null
    }

    protected open fun extractMerchant(message: String, sender: String): String? {
        val patterns = listOf(
            // UPI VPA or To <Merchant> on
            Regex("""(?:to|at|towards|for|info:?|vpa)\s+([A-Za-z0-9._\-@\s&'/]{2,45}?)(?:\s+on\b|\s+ref\b|\s+upi\b|\s+avl\b|\s+bal\b|\.\s|$|\n)""", RegexOption.IGNORE_CASE),
            // Info: UPI/P2M/123456/MERCHANT
            Regex("""(?:UPI|IMPS|NEFT|POS)[/\-:](?:P2[AM][/\-:])?(?:[0-9]{6,14}[/\-:])?([A-Za-z0-9\s&._\-]{2,35}?)(?:[/\-:]|\s+on|\s+ref|\.|$)""", RegexOption.IGNORE_CASE),
            // Spent on Card at MERCHANT
            Regex("""(?:spent|used|purchase|txn)\s+(?:on\s+.*?\s+)?at\s+([A-Za-z0-9\s&._'\-]{2,40}?)(?:\s+on\b|\s+for\b|\.\s|$)""", RegexOption.IGNORE_CASE),
            // Credited by / from SENDER
            Regex("""(?:from|by)\s+([A-Za-z0-9\s&._@\-]{2,40}?)(?:\s+on\b|\s+ref\b|\s+upi\b|\.\s|$|\n)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                val candidate = cleanMerchantName(match.groupValues[1])
                if (isValidMerchantName(candidate)) {
                    return candidate
                }
            }
        }
        return null
    }

    protected open fun extractAccountNumber(message: String): String? {
        for (pattern in CompiledPatterns.ACCOUNT_PATTERNS) {
            val match = pattern.find(message)
            if (match != null) {
                val digits = match.groupValues[1].trim()
                if (digits.length in 3..4) {
                    return digits
                }
            }
        }
        return null
    }

    protected open fun extractBalance(message: String): BigDecimal? {
        for (pattern in CompiledPatterns.BALANCE_PATTERNS) {
            val match = pattern.find(message)
            if (match != null) {
                val raw = match.groupValues[1].replace(",", "").trim()
                return raw.toBigDecimalOrNull()
            }
        }
        return null
    }

    protected open fun extractAvailableLimit(message: String): BigDecimal? {
        for (pattern in CompiledPatterns.CREDIT_LIMIT_PATTERNS) {
            val match = pattern.find(message)
            if (match != null) {
                val raw = match.groupValues[1].replace(",", "").trim()
                return raw.toBigDecimalOrNull()
            }
        }
        return null
    }

    protected open fun extractReference(message: String): String? {
        for (pattern in CompiledPatterns.REFERENCE_PATTERNS) {
            val match = pattern.find(message)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return null
    }

    protected open fun isTransactionMessage(message: String): Boolean {
        val lowerMessage = message.lowercase()

        val ignorePhrases = listOf(
            "otp", "one time password", "verification code", "auth code",
            "requested money", "collect request", "has requested",
            "bill is generated", "statement is ready", "minimum amount due",
            "total amount due", "due on", "apply now", "pre-approved",
            "offer", "win ", "discount", "voucher", "failed", "declined",
            "could not be processed", "mandate", "e-mandate", "kyc"
        )
        if (ignorePhrases.any { lowerMessage.contains(it) }) {
            return false
        }

        val transactionIndicators = listOf(
            "debited", "credited", "spent", "paid", "withdrawn",
            "received", "deposited", "sent", "txn", "purchase", "refund"
        )
        return transactionIndicators.any { lowerMessage.contains(it) }
    }

    protected open fun detectIsCard(message: String): Boolean {
        val lower = message.lowercase()
        val cardIndicators = listOf(
            "credit card", "debit card", "card ending", "card xx",
            "card no", "cc ", "spent on card", "avl limit", "available limit"
        )
        return cardIndicators.any { lower.contains(it) }
    }

    protected open fun cleanMerchantName(merchant: String): String {
        var cleaned = merchant
            .replace(Regex("""@[a-zA-Z0-9._\-]+"""), "") // strip VPA suffix like @okaxis
            .replace(Regex("""^(?:VPA|UPI|POS|IMPS|NEFT|RTGS|ACH|MMT)[:/\-\s]+""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""(?:\s+on|\s+ref|\s+upi|\s+avl|\s+bal|\s+not\s+you).*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""[^a-zA-Z0-9\s&'\-.]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        if (cleaned.equals("your a c", ignoreCase = true) ||
            cleaned.equals("your account", ignoreCase = true) ||
            cleaned.equals("a c", ignoreCase = true)
        ) {
            return ""
        }

        // Convert ALL CAPS merchant names into clean Title Case unless short acronym
        if (cleaned.length > 4 && cleaned == cleaned.uppercase()) {
            cleaned = cleaned.lowercase().split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
        }
        return cleaned.take(40).trim()
    }

    protected fun isValidMerchantName(name: String): Boolean {
        if (name.length < 2) return false
        val invalidWords = setOf(
            "a/c", "ac", "account", "acct", "bank", "card", "upi",
            "rs", "inr", "bal", "balance", "sms", "block", "call", "you", "your"
        )
        if (name.lowercase() in invalidWords) return false
        if (name.all { it.isDigit() || it == 'x' || it == 'X' || it == '*' }) return false
        return true
    }
}
