package com.ritesh.parser.core.bank

import com.ritesh.parser.core.CompiledPatterns
import com.ritesh.parser.core.ParsedTransaction
import com.ritesh.parser.core.TransactionType

/**
 * Factory and router for Cashiro's `com.ritesh.parser.core.bank` parser suite.
 * Routes SMS sender IDs (including DLT headers like `AD-HDFCBK-S`, `VM-SBIINB`, `JX-ICICIB`)
 * to the appropriate `BankParser`, with body-based fallback detection and category mapping.
 */
object BankParserFactory {

    private val parsers: List<BankParser> = listOf(
        HDFCBankParser(),
        SBIBankParser(),
        ICICIBankParser(),
        AxisBankParser(),
        KotakBankParser(),
        IDFCFirstBankParser(),
        IndusIndBankParser(),
        YesBankParser(),
        PNBBankParser(),
        BankOfBarodaParser(),
        FederalBankParser(),
        CanaraBankParser(),
        UnionBankParser(),
        OneCardParser(),
        AmexBankParser(),
        PaytmBankParser(),
        AirtelPaymentsBankParser()
    )

    fun normalizeSender(sender: String): String {
        return sender.trim()
            .replace(CompiledPatterns.DLT_PREFIX_REGEX, "")
            .replace(CompiledPatterns.DLT_SUFFIX_REGEX, "")
            .uppercase()
    }

    fun getParser(sender: String, smsBody: String = ""): BankParser? {
        val cleanSender = normalizeSender(sender)
        val matchedBySender = parsers.firstOrNull { it.canHandle(cleanSender) || it.canHandle(sender) }
        if (matchedBySender != null) return matchedBySender

        // Fallback: inspect SMS body for bank names when testing or pasting SMS messages
        val lowerBody = smsBody.lowercase()
        return when {
            lowerBody.contains("hdfc") -> HDFCBankParser()
            lowerBody.contains("sbi") || lowerBody.contains("state bank") -> SBIBankParser()
            lowerBody.contains("icici") -> ICICIBankParser()
            lowerBody.contains("axis") -> AxisBankParser()
            lowerBody.contains("kotak") -> KotakBankParser()
            lowerBody.contains("idfc") -> IDFCFirstBankParser()
            lowerBody.contains("indusind") -> IndusIndBankParser()
            lowerBody.contains("yes bank") -> YesBankParser()
            lowerBody.contains("pnb") || lowerBody.contains("punjab national") -> PNBBankParser()
            lowerBody.contains("bank of baroda") || lowerBody.contains("bob") -> BankOfBarodaParser()
            lowerBody.contains("federal bank") -> FederalBankParser()
            lowerBody.contains("canara") -> CanaraBankParser()
            lowerBody.contains("union bank") -> UnionBankParser()
            lowerBody.contains("onecard") -> OneCardParser()
            lowerBody.contains("amex") || lowerBody.contains("american express") -> AmexBankParser()
            lowerBody.contains("paytm") -> PaytmBankParser()
            isLikelyFinancialSms(sender, smsBody) -> GenericBankParser(inferBankLabelFromSender(cleanSender))
            else -> null
        }
    }

    fun parseSms(
        smsBody: String,
        sender: String,
        timestamp: Long = System.currentTimeMillis()
    ): ParsedTransaction? {
        val parser = getParser(sender, smsBody) ?: return null
        return parser.parse(smsBody, sender, timestamp)
    }

    private fun isLikelyFinancialSms(sender: String, body: String): Boolean {
        val lower = body.lowercase()
        val hasCurrencyOrAmount = lower.contains("rs") || lower.contains("inr") || lower.contains("₹") || lower.contains("debited") || lower.contains("credited")
        val hasFinancialAction = lower.contains("a/c") || lower.contains("acct") || lower.contains("card") || lower.contains("upi") || lower.contains("spent") || lower.contains("avl bal")
        val isTransactionalSender = CompiledPatterns.DLT_PATTERNS.any { it.matches(sender.trim()) } || sender.length >= 5
        return hasCurrencyOrAmount && hasFinancialAction && isTransactionalSender
    }

    private fun inferBankLabelFromSender(cleanSender: String): String {
        return if (cleanSender.isNotBlank() && cleanSender.length <= 10) {
            "$cleanSender Bank"
        } else {
            "Bank Account"
        }
    }

    /**
     * Deterministic category inference based on extracted merchant and SMS text,
     * matching Axio's smart categorization buckets.
     */
    fun inferCategory(parsed: ParsedTransaction): String {
        if (parsed.type == TransactionType.INCOME) return "Salary & Income"
        if (parsed.type == TransactionType.INVESTMENT) return "Investments"

        val haystack = "${parsed.merchant.orEmpty()} ${parsed.smsBody}".lowercase()
        return when {
            listOf("bigbasket", "blinkit", "zepto", "instamart", "dmart", "reliance fresh", "nature's basket", "grocery", "supermarket", "milk", "vegetables").any { haystack.contains(it) } -> "Groceries"
            listOf("zomato", "swiggy", "starbucks", "mcdonald", "kfc", "domino", "pizza", "cafe", "restaurant", "dining", "burger", "haldiram", "barbeque").any { haystack.contains(it) } -> "Food & Dining"
            listOf("uber", "ola", "rapido", "irctc", "makemytrip", "indigo", "air india", "shell", "hpcl", "bpcl", "indian oil", "petrol", "fuel", "metro", "fastag", "toll").any { haystack.contains(it) } -> "Fuel & Travel"
            listOf("bescom", "electricity", "jio", "airtel", "vi ", "vodafone", "act fibernet", "tata play", "gas", "water bill", "broadband", "recharge", "billdesk", "utility").any { haystack.contains(it) } -> "Bills & Utilities"
            listOf("rent", "nobroker", "society", "maintenance", "landlord", "housing").any { haystack.contains(it) } -> "Rent & Housing"
            listOf("amazon", "flipkart", "myntra", "ajio", "nykaa", "meesho", "zara", "h&m", "decathlon", "croma", "ikea", "shopping").any { haystack.contains(it) } -> "Shopping"
            listOf("apollo", "pharmeasy", "1mg", "medplus", "hospital", "clinic", "pharmacy", "doctor", "diagnostic", "cult.fit", "health").any { haystack.contains(it) } -> "Health & Fitness"
            listOf("netflix", "spotify", "prime video", "hotstar", "bookmyshow", "pvr", "inox", "youtube", "sony liv", "steam").any { haystack.contains(it) } -> "Entertainment"
            else -> "General"
        }
    }

    fun inferPaymentMode(parsed: ParsedTransaction): String {
        val lower = parsed.smsBody.lowercase()
        return when {
            parsed.isFromCard || parsed.type == TransactionType.CREDIT -> "CARD"
            lower.contains("upi") || lower.contains("vpa") -> "UPI"
            parsed.bankName.contains("Paytm", ignoreCase = true) || lower.contains("wallet") -> "WALLET"
            else -> "BANK"
        }
    }

    fun formatAccountDisplayName(parsed: ParsedTransaction): String {
        val suffix = parsed.accountNumber?.let { " ••$it" } ?: ""
        return if (parsed.isFromCard) {
            "${parsed.bankName} Card$suffix"
        } else {
            "${parsed.bankName}$suffix"
        }
    }

    data class SampleBankSms(
        val label: String,
        val sender: String,
        val body: String,
        val defaultShared: Boolean
    )

    val SAMPLE_BANK_SMS_TEMPLATES: List<SampleBankSms> = listOf(
        SampleBankSms(
            label = "HDFC Bank UPI • BigBasket Groceries",
            sender = "AD-HDFCBK-S",
            body = "Sent Rs.1,845.00\nFrom HDFC Bank A/C *4821\nTo BIGBASKET GROCERY\nOn 05/11/26\nRef 631098451209\nAvl Bal Rs.64,210.50",
            defaultShared = true
        ),
        SampleBankSms(
            label = "SBI UPI • Zomato Food Order",
            sender = "VM-SBIINB-S",
            body = "Dear UPI user A/C X9102 debited by 540.00 on date 05Nov26 trf to Zomato Limited Refno 431088761234. Avl Bal Rs.28,460.00 -SBI",
            defaultShared = true
        ),
        SampleBankSms(
            label = "ICICI Bank • Electricity Utility Bill",
            sender = "JX-ICICIB-S",
            body = "ICICI Bank Acct XX3319 debited for Rs 2,380.00 on 05-Nov-26; BESCOM ELECTRICITY credited. UPI:431077219081. Avl Bal is Rs 51,920.00.",
            defaultShared = true
        ),
        SampleBankSms(
            label = "Axis Credit Card • Amazon Shopping",
            sender = "JD-AXISBK-S",
            body = "Spent Rs.3,499.00 on Axis Bank Credit Card no. XX7741 at AMAZON INDIA on 05-11-2026. Avl Limit: Rs.1,46,501.00. Not you? SMS BLOCK",
            defaultShared = false
        ),
        SampleBankSms(
            label = "Kotak Bank • Indian Oil Fuel",
            sender = "JM-KOTAKB-S",
            body = "Sent Rs.2,100.00 from Kotak Bank AC X5504 to INDIAN OIL FUEL on 05-11-26. UPI Ref 431065432198. Bal: Rs.39,150.00",
            defaultShared = true
        ),
        SampleBankSms(
            label = "HDFC Bank • Monthly Salary Credit",
            sender = "AD-HDFCBK-S",
            body = "Update! INR 95,000.00 credited to HDFC Bank A/c XX4821 on 01-11-26 by ACME TECH PAYROLL. Available Balance: INR 1,59,210.50",
            defaultShared = false
        )
    )
}
