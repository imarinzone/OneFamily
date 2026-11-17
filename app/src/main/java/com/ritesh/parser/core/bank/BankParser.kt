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
        // Strip balance/limit clauses first when searching for transaction amount if needed,
        // or match primary transaction amount patterns
        for (pattern in CompiledPatterns.AMOUNT_PATTERNS) {
            val matches = pattern.findAll(message)
            for (match in matches) {
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
            "mutual fund", "sip ", "zerodha", "groww", "kuvera", "upstox",
            "nps", "ppf", "folio", "nav ", "units allotted", "indmoney"
        )
        if (investmentKeywords.any { lowerMessage.contains(it) } &&
            (lowerMessage.contains("debited") || lowerMessage.contains("paid") || lowerMessage.contains("invested") || lowerMessage.contains("purchase") || lowerMessage.contains("allotted"))
        ) {
            return TransactionType.INVESTMENT
        }

        val expenseKeywords = listOf(
            "debited", "spent", "paid", "withdrawn", "sent", "charged",
            "purchase", "deducted", "used at", "transferred to",
            "dr ", "dr.", "trf to"
        )
        val incomeKeywords = listOf(
            "credited", "received", "deposited", "refund", "cashback",
            "salary", "cr ", "cr.", "credit to", "added to", "reversal",
            "reimbursement", "interest credited", "dividend"
        )

        val debitIndex = expenseKeywords
            .map { lowerMessage.indexOf(it) }
            .filter { it >= 0 }
            .minOrNull()

        val creditIndex = incomeKeywords
            .map { lowerMessage.indexOf(it) }
            .filter { it >= 0 }
            .minOrNull()

        // Handle "credited to <payee> and debited from your a/c" or "A/c debited ... <merchant> credited"
        if (debitIndex != null && creditIndex != null) {
            if (lowerMessage.contains("debited from") ||
                lowerMessage.contains("debited by") ||
                lowerMessage.contains("debited for") ||
                lowerMessage.contains("debited with") ||
                lowerMessage.contains("is debited") ||
                lowerMessage.contains("has been debited") ||
                lowerMessage.contains("a/c debited") ||
                lowerMessage.contains("acct debited") ||
                lowerMessage.contains("sent rs") ||
                lowerMessage.contains("spent on") ||
                lowerMessage.contains("paid rs")
            ) {
                return TransactionType.EXPENSE
            }
            if (lowerMessage.contains("credited to your") ||
                lowerMessage.contains("credited to a/c") ||
                lowerMessage.contains("credited with") ||
                lowerMessage.contains("is credited") ||
                lowerMessage.contains("has been credited") ||
                lowerMessage.contains("a/c credited") ||
                lowerMessage.contains("received in")
            ) {
                return TransactionType.INCOME
            }
            return if (debitIndex <= creditIndex) TransactionType.EXPENSE else TransactionType.INCOME
        }

        if (debitIndex != null) return TransactionType.EXPENSE
        if (creditIndex != null) return TransactionType.INCOME

        return null
    }

    /**
     * Removes trailing security disclaimers, fraud-reporting footers, helpline instructions,
     * OTP warnings, and balance clauses so they can never be mistaken for a merchant/payee name.
     */
    protected fun stripTrailingDisclaimers(message: String): String {
        var result = message
        // Truncate from the start of any trailing security/fraud/helpline disclaimer sentence
        val trailingFooterPattern = Regex(
            """(?:^|[.\n\r!;]|\s+-\s*)\s*(?:not\s+you\b|if\s+not\b|if\s+this\s+was\s+not\b|if\s+you\s+(?:have\s+)?not\b|if\s+unauthori[sz]ed\b|sms\s+block\b|to\s+(?:block|dispute|report|check|know|view|opt|stop|disable|manage|register|avail|prevent|avoid)\b|for\s+(?:dispute|any\s+query|queries|assistance|help|support)\b|call\s+(?:1800|1860|022|080|011|044|\+?91[\s\-]?[0-9]|[0-9]{7,})\b|(?:please\s+)?do\s+not\s+share\b|never\s+share\b|don'?t\s+share\b|forward\s+this\s+sms\b|warm\s+regards\b|thank\s+you\s+for\s+banking\b).*$""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        result = result.replace(trailingFooterPattern, "")

        // Also strip inline balance/limit clauses up to end of clause/line
        val inlineBalanceClause = Regex(
            """(?:[.\n\r;,]|\s+)\s*(?:avl\.?\s*bal|available\s+bal|avail\.?\s*(?:bal|limit)|curr(?:ent)?\s+bal|clear\s+bal|updated\s+bal|closing\s+bal|total\s+avl)[^.\n\r]*""",
            RegexOption.IGNORE_CASE
        )
        result = result.replace(inlineBalanceClause, "")
        return result.trim()
    }

    protected open fun extractMerchant(message: String, sender: String): String? {
        val sanitized = stripTrailingDisclaimers(message)
        if (sanitized.isBlank()) return null

        val txType = extractTransactionType(message)
        val patterns = buildList {
            // UPI/P2M/123456/MERCHANT or UPI-123456-MERCHANT
            add(
                Regex(
                    """(?:UPI|IMPS|NEFT|RTGS|POS)[/\-:](?:P2[AM][/\-:])?(?:[0-9]{6,16}[/\-:])?([A-Za-z0-9\s&._'\-]{2,35}?)(?:[/\-:]|\s+on\b|\s+ref\b|\.|$|\n)""",
                    RegexOption.IGNORE_CASE
                )
            )
            // trf to <Merchant> Refno
            add(
                Regex(
                    """\btrf\s+to\s+([A-Za-z0-9\s&._'\-@]{2,40}?)(?:\s+ref|\s+on\b|\s+upi\b|\.|$|\n)""",
                    RegexOption.IGNORE_CASE
                )
            )
            // ; <Merchant> credited
            add(
                Regex(
                    """;\s*([A-Za-z0-9\s&._'\-@]{2,40}?)\s+credited""",
                    RegexOption.IGNORE_CASE
                )
            )
            // Spent on Card at MERCHANT
            add(
                Regex(
                    """(?:spent|used|purchase|txn|transaction)\s+(?:on\s+.*?\s+)?at\s+([A-Za-z0-9\s&._'\-]{2,40}?)(?:\s+on\b|\s+for\b|\s+ref\b|\.\s|\.$|$|\n)""",
                    RegexOption.IGNORE_CASE
                )
            )
            // To / at / towards / VPA <Merchant>
            add(
                Regex(
                    """\b(?:to|at|towards|vpa|beneficiary|payee)\s+([A-Za-z0-9._\-@\s&'/]{2,42}?)(?:\s+on\b|\s+ref\b|\s+upi\b|\s+avl\b|\s+bal\b|\s+via\b|\.\s|\.$|$|\n)""",
                    RegexOption.IGNORE_CASE
                )
            )
            // Credited by / from SENDER — strictly for INCOME/credit messages so "debited from A/c" or "by you" never matches
            if (txType == TransactionType.INCOME) {
                add(
                    Regex(
                        """(?:credited|received|deposited|trf|transfer(?:red)?)\b[^.\n\r]{0,35}?\b(?:from|by)\s+([A-Za-z0-9\s&._@\-]{2,40}?)(?:\s+on\b|\s+ref\b|\s+upi\b|\s+towards\b|\.\s|\.$|$|\n)""",
                        RegexOption.IGNORE_CASE
                    )
                )
            }
        }

        for (pattern in patterns) {
            for (match in pattern.findAll(sanitized)) {
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
        if (isMandateOrBillReminderOrNonTransaction(message)) {
            return false
        }

        // Strip "ignore if already paid" / "if already paid" before checking transaction indicators
        val cleanedLower = message.lowercase()
            .replace("ignore if already paid", "")
            .replace("ignore if paid", "")
            .replace("if already paid", "")

        val transactionIndicators = listOf(
            "debited", "credited", "spent", "paid", "withdrawn",
            "received", "deposited", "sent",
            "purchase", "refund", "deducted", "transferred", "trf to",
            "charged", "used at", "cashback"
        )
        return transactionIndicators.any { cleanedLower.contains(it) }
    }

    companion object {
        private val PURE_OTP_PATTERNS = listOf(
            Regex("""\botp\s+(?:is|for|to)\b"""),
            Regex("""\bis\s+your\s+(?:otp|one\s+time\s+password|verification\s+code)\b"""),
            Regex("""\b(?:one\s+time\s+password|verification\s+code|auth\s+code)\s+(?:is|for)\b"""),
            Regex("""\buse\s+otp\b""")
        )

        private val MANDATE_AND_PREDEBIT_PHRASES = listOf(
            "mandate", "e-mandate", "emandate", "umn:", "umn ",
            "nach ", "e-nach", "enach", "standing instruction",
            "si registration", "si created", "autopay setup",
            "autopay registered", "autopay registration", "autopay created",
            "autopay active", "autopay cancelled", "autopay revoked",
            "auto-debit setup", "auto-debit registered", "auto debit setup",
            "pre-debit", "pre debit", "will be debited", "shall be debited",
            "scheduled for debit", "going to be debited", "upcoming debit",
            "upcoming autopay", "upcoming emi", "approve mandate",
            "create mandate", "modify mandate", "revoke mandate",
            "recurring payment setup", "recurring mandate"
        )

        private val BILL_REMINDER_AND_NON_TXN_PHRASES = listOf(
            "requested money", "collect request", "has requested",
            "bill is generated", "bill generated", "statement is ready",
            "statement is generated", "statement generated", "statement for",
            "stmt for", "e-statement", "estatement",
            "minimum amount due", "min amount due", "min amt due", "min due",
            "minimum due", "total amount due", "total amt due", "total due",
            "bill is due", "bill due", "payment due", "payment is due",
            "amount due", "amt due", "is due on", "due on ", "due date",
            "due by ", "pay by ", "pay before", "payable by",
            "bill of rs", "bill for rs", "reminder", "kindly pay",
            "please pay", "to avoid late", "late payment",
            "ignore if already paid", "ignore if paid",
            "ensure sufficient balance", "maintain sufficient balance",
            "emi due", "overdue", "failed", "declined",
            "could not be processed", "was unsuccessful"
        )

        fun isMandateOrBillReminderOrNonTransaction(message: String): Boolean {
            val lowerMessage = message.lowercase()
            if (PURE_OTP_PATTERNS.any { it.containsMatchIn(lowerMessage) }) {
                return true
            }
            if (MANDATE_AND_PREDEBIT_PHRASES.any { lowerMessage.contains(it) }) {
                return true
            }
            if (BILL_REMINDER_AND_NON_TXN_PHRASES.any { lowerMessage.contains(it) }) {
                return true
            }
            return false
        }
    }

    protected open fun detectIsCard(message: String): Boolean {
        val lower = message.lowercase()
        val cardIndicators = listOf(
            "credit card", "debit card", "card ending", "card xx", "card x",
            "card no", "cc ", "spent on card", "avl limit", "available limit", "curr limit"
        )
        return cardIndicators.any { lower.contains(it) }
    }

    protected open fun cleanMerchantName(merchant: String): String {
        var cleaned = stripTrailingDisclaimers(merchant)
            .replace(Regex("""@[a-zA-Z0-9._\-]+"""), "")
            .replace(Regex("""^(?:VPA|UPI|POS|IMPS|NEFT|RTGS|ACH|MMT|BY|TO|AT|FROM)[:/\-\s]+""", RegexOption.IGNORE_CASE), "")
            .replace(
                Regex(
                    """(?:\s+on|\s+ref|\s+upi|\s+avl|\s+bal|\s+not\s+you|\s+if\s+not|\s+call\s+[0-9]|\s+sms\s+block|\s+to\s+block|\s+to\s+dispute|\s+to\s+report|\s+do\s+not\s+share|\s+never\s+share).*$""",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )
            .replace(Regex("""[^a-zA-Z0-9\s&'\-.]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .trim('.', '-', '\'')
            .trim()

        val lowerCleaned = cleaned.lowercase()
        if (lowerCleaned in setOf(
                "your a c", "your account", "a c", "ac", "account", "acct",
                "you", "your", "me", "self", "bank", "upi", "imps", "neft"
            ) ||
            lowerCleaned.startsWith("a c ") ||
            lowerCleaned.startsWith("ac ") ||
            lowerCleaned.startsWith("acct ") ||
            lowerCleaned.startsWith("your a c") ||
            lowerCleaned.startsWith("your acct") ||
            lowerCleaned.startsWith("your account") ||
            Regex("""\b(?:a\s*c|acct|account|card)\s*(?:no\.?\s*)?[x*]*[0-9]{3,}\b""").containsMatchIn(lowerCleaned)
        ) {
            return ""
        }

        if (cleaned.length > 4 && cleaned == cleaned.uppercase()) {
            cleaned = cleaned.lowercase().split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
        }
        return cleaned.take(40).trim()
    }

    protected fun isValidMerchantName(name: String): Boolean {
        if (name.length < 2) return false
        val lower = name.lowercase().trim()
        val invalidWords = setOf(
            "a/c", "ac", "account", "acct", "bank", "card", "upi",
            "rs", "inr", "bal", "balance", "sms", "block", "call", "you", "your",
            "debited", "credited", "date", "time", "ref", "refno", "info",
            "dispute", "report", "fraud", "customer", "helpline", "otp", "pin"
        )
        if (lower in invalidWords) return false

        // Reject disclaimer/action verb phrases or helpline fragments
        val invalidPrefixes = listOf(
            "block ", "dispute ", "report ", "call ", "sms ", "forward ",
            "click ", "visit ", "check ", "know ", "view ", "opt ", "stop ",
            "avoid ", "prevent ", "share ", "not you", "if not", "you ",
            "your ", "dear ", "helpline", "toll free", "customer care",
            "avl ", "available ", "ending ", "xx"
        )
        if (invalidPrefixes.any { lower.startsWith(it) }) return false

        val invalidSubstrings = listOf(
            "report fraud", "sms block", "block upi", "not done by you",
            "do not share", "never share", "debited from", "credited to",
            "call 1800", "call 1860", "avl bal", "available bal"
        )
        if (invalidSubstrings.any { lower.contains(it) }) return false

        // Reject 7+ consecutive digits (phone/helpline/reference numbers)
        if (Regex("""[0-9]{7,}""").containsMatchIn(lower)) return false
        if (!lower.any { it.isLetter() }) return false
        if (name.all { it.isDigit() || it == 'x' || it == 'X' || it == '*' || it == '.' || it == '-' }) return false
        return true
    }
}
