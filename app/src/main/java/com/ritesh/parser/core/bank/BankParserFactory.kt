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
        AirtelPaymentsBankParser(),
        RBLBankParser(),
        AUBankParser(),
        BandhanBankParser(),
        IndianBankParser(),
        BankOfIndiaParser(),
        CentralBankParser(),
        UCOBankParser(),
        IOBBankParser(),
        HSBCBankParser(),
        StandardCharteredParser(),
        CitiBankParser(),
        DBSBankParser(),
        JupiterParser(),
        FiMoneyParser(),
        SliceParser(),
        SouthIndianBankParser(),
        KarurVysyaBankParser(),
        KarnatakaBankParser(),
        DOPBankParser()
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

        // Do not match personal 10+ digit phone numbers unless sender matches a Cashiro bank parser
        val digitsOnly = sender.filter { it.isDigit() }
        if (digitsOnly.length >= 10 && !sender.contains("-")) {
            return null
        }

        // Strict Cashiro bank name matching when DLT sender ID is generic (e.g. VM-TXNSMS)
        val lowerBody = smsBody.lowercase()
        return when {
            lowerBody.contains("hdfc bank") || lowerBody.contains("hdfc card") || lowerBody.contains("hdfcbk") -> HDFCBankParser()
            lowerBody.contains("sbi ") || lowerBody.contains("state bank of india") || lowerBody.contains("sbi card") -> SBIBankParser()
            lowerBody.contains("icici bank") || lowerBody.contains("icici card") -> ICICIBankParser()
            lowerBody.contains("axis bank") || lowerBody.contains("axis card") -> AxisBankParser()
            lowerBody.contains("kotak bank") || lowerBody.contains("kotak mahindra") -> KotakBankParser()
            lowerBody.contains("idfc first") || lowerBody.contains("idfc bank") -> IDFCFirstBankParser()
            lowerBody.contains("indusind bank") -> IndusIndBankParser()
            lowerBody.contains("yes bank") -> YesBankParser()
            lowerBody.contains("pnb ") || lowerBody.contains("punjab national bank") -> PNBBankParser()
            lowerBody.contains("bank of baroda") -> BankOfBarodaParser()
            lowerBody.contains("federal bank") -> FederalBankParser()
            lowerBody.contains("canara bank") -> CanaraBankParser()
            lowerBody.contains("union bank") -> UnionBankParser()
            lowerBody.contains("onecard") -> OneCardParser()
            lowerBody.contains("american express") || lowerBody.contains("amex card") -> AmexBankParser()
            lowerBody.contains("paytm payments bank") -> PaytmBankParser()
            lowerBody.contains("airtel payments bank") -> AirtelPaymentsBankParser()
            lowerBody.contains("rbl bank") -> RBLBankParser()
            lowerBody.contains("au small finance") || lowerBody.contains("au bank") -> AUBankParser()
            lowerBody.contains("bandhan bank") -> BandhanBankParser()
            lowerBody.contains("indian bank") -> IndianBankParser()
            lowerBody.contains("bank of india") -> BankOfIndiaParser()
            lowerBody.contains("central bank of india") -> CentralBankParser()
            lowerBody.contains("uco bank") -> UCOBankParser()
            lowerBody.contains("indian overseas bank") -> IOBBankParser()
            lowerBody.contains("hsbc bank") || lowerBody.contains("hsbc card") -> HSBCBankParser()
            lowerBody.contains("standard chartered") -> StandardCharteredParser()
            lowerBody.contains("citibank") || lowerBody.contains("citi bank") -> CitiBankParser()
            lowerBody.contains("dbs bank") -> DBSBankParser()
            lowerBody.contains("south indian bank") -> SouthIndianBankParser()
            lowerBody.contains("karur vysya") -> KarurVysyaBankParser()
            lowerBody.contains("karnataka bank") -> KarnatakaBankParser()
            else -> null
        }
    }

    fun parseSms(
        smsBody: String,
        sender: String,
        timestamp: Long = System.currentTimeMillis()
    ): ParsedTransaction? {
        if (BankParser.isMandateOrBillReminderOrNonTransaction(smsBody)) {
            return null
        }
        val parser = getParser(sender, smsBody) ?: return null
        return parser.parse(smsBody, sender, timestamp)
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
