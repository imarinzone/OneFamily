package com.ritesh.parser.core.bank

import com.ritesh.parser.core.CompiledPatterns
import com.ritesh.parser.core.ParsedTransaction
import com.ritesh.parser.core.TransactionType

/**
 * Factory and router for Cashiro's `com.ritesh.parser.core.bank` parser suite.
 * Routes SMS sender IDs (including DLT headers like `AD-HDFCBK-S`, `VM-SBIINB`, `JX-ICICIB`)
 * and body patterns to the appropriate `BankParser`.
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
            lowerBody.contains("federal") -> FederalBankParser()
            lowerBody.contains("canara") -> CanaraBankParser()
            lowerBody.contains("union bank") -> UnionBankParser()
            lowerBody.contains("onecard") -> OneCardParser()
            lowerBody.contains("amex") || lowerBody.contains("american express") -> AmexBankParser()
            lowerBody.contains("paytm") -> PaytmBankParser()
            lowerBody.contains("airtel") -> AirtelPaymentsBankParser()
            lowerBody.contains("rbl") -> GenericBankParser("RBL Bank")
            lowerBody.contains("au bank") || lowerBody.contains("au small") -> GenericBankParser("AU Small Finance Bank")
            lowerBody.contains("bandhan") -> GenericBankParser("Bandhan Bank")
            lowerBody.contains("indian bank") -> GenericBankParser("Indian Bank")
            lowerBody.contains("bank of india") || lowerBody.contains("boi") -> GenericBankParser("Bank of India")
            lowerBody.contains("central bank") -> GenericBankParser("Central Bank")
            lowerBody.contains("uco bank") -> GenericBankParser("UCO Bank")
            lowerBody.contains("iob") || lowerBody.contains("indian overseas") -> GenericBankParser("Indian Overseas Bank")
            lowerBody.contains("hsbc") -> GenericBankParser("HSBC Bank")
            lowerBody.contains("standard chartered") || lowerBody.contains("scb") -> GenericBankParser("Standard Chartered")
            lowerBody.contains("citi") -> GenericBankParser("Citi Bank")
            lowerBody.contains("dbs") -> GenericBankParser("DBS Bank")
            lowerBody.contains("jupiter") -> GenericBankParser("Jupiter")
            lowerBody.contains("fi money") -> GenericBankParser("Fi Money")
            lowerBody.contains("slice") -> GenericBankParser("Slice")
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
        // Reject conversational personal numbers (+9198..., 10-digit mobile numbers) unless message clearly has bank/UPI structure
        val digitsOnly = sender.filter { it.isDigit() }
        val looksLikePersonalPhone = digitsOnly.length >= 10 && !sender.contains("-")

        val hasCurrencyOrAmount = lower.contains("rs") || lower.contains("inr") || lower.contains("₹") ||
            lower.contains("debited") || lower.contains("credited") || lower.contains("spent") || lower.contains("paid")
        val hasFinancialContext = lower.contains("a/c") || lower.contains("acct") || lower.contains("ac ") ||
            lower.contains("account") || lower.contains("card") || lower.contains("upi") ||
            lower.contains("vpa") || lower.contains("imps") || lower.contains("neft") ||
            lower.contains("avl bal") || lower.contains("available bal") || lower.contains("ref") ||
            lower.contains("trf to") || lower.contains("debited") || lower.contains("credited")

        if (looksLikePersonalPhone) {
            return hasCurrencyOrAmount && (lower.contains("a/c") || lower.contains("acct") || lower.contains("upi") || lower.contains("card"))
        }
        return hasCurrencyOrAmount && hasFinancialContext
    }

    private fun inferBankLabelFromSender(cleanSender: String): String {
        val trimmed = cleanSender.filter { it.isLetterOrDigit() }
        return if (trimmed.isNotBlank() && trimmed.length in 3..10 && trimmed.any { it.isLetter() }) {
            trimmed
        } else {
            "Bank Account"
        }
    }

    fun inferCategory(parsed: ParsedTransaction): String {
        val haystack = "${parsed.merchant.orEmpty()} ${parsed.smsBody}".lowercase()
        if (parsed.type == TransactionType.INCOME) {
            return when {
                listOf("cashback", "cash back", "reward").any { haystack.contains(it) } -> "Cashback"
                listOf("salary", "payroll", "stipend").any { haystack.contains(it) } -> "Salary & Income"
                else -> "Income"
            }
        }
        if (parsed.type == TransactionType.INVESTMENT) return "Investments"

        return when {
            listOf("bigbasket", "blinkit", "zepto", "instamart", "dmart", "reliance fresh", "nature's basket", "grocery", "supermarket", "milk", "vegetables", "kirana", "jiomart", "ratnadeep", "more retail").any { haystack.contains(it) } -> "Groceries"
            listOf("zomato", "swiggy", "starbucks", "mcdonald", "kfc", "domino", "pizza", "cafe", "restaurant", "dining", "burger", "haldiram", "barbeque", "eatfit", "bakery", "coffee", "tea", "bistro", "biryani", "sweet").any { haystack.contains(it) } -> "Food & Dining"
            listOf("uber", "ola", "rapido", "irctc", "makemytrip", "indigo", "air india", "vistara", "akasa", "shell", "hpcl", "bpcl", "indian oil", "iocl", "petrol", "fuel", "metro", "fastag", "toll", "namma yatri", "redbus", "yatra", "parking").any { haystack.contains(it) } -> "Fuel & Travel"
            listOf("bescom", "electricity", "jio", "airtel", "vi ", "vodafone", "bsnl", "act fibernet", "tata play", "gas", "lpg", "indane", "bharatgas", "hp gas", "water bill", "broadband", "recharge", "billdesk", "utility", "bbps", "mseb", "tneb", "tpddl").any { haystack.contains(it) } -> "Bills & Utilities"
            listOf("rent", "nobroker", "society", "maintenance", "landlord", "housing", "mygate", "apnacomplex", "adda").any { haystack.contains(it) } -> "Rent & Housing"
            listOf("amazon", "flipkart", "myntra", "ajio", "nykaa", "meesho", "zara", "h&m", "decathlon", "croma", "reliance digital", "vijay sales", "ikea", "shopping", "tata cliq", "firstcry", "lenskart", "titan").any { haystack.contains(it) } -> "Shopping"
            listOf("apollo", "pharmeasy", "1mg", "medplus", "netmeds", "hospital", "clinic", "pharmacy", "doctor", "diagnostic", "cult.fit", "health", "medical", "dental", "lab", "practo").any { haystack.contains(it) } -> "Health & Fitness"
            listOf("netflix", "spotify", "prime video", "hotstar", "jiocinema", "bookmyshow", "pvr", "inox", "youtube", "sony liv", "zee5", "steam", "playstation", "apple.com/bill", "google play").any { haystack.contains(it) } -> "Entertainment"
            else -> "Unknown"
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
}
