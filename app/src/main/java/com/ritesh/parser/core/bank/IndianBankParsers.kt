package com.ritesh.parser.core.bank

import java.math.BigDecimal

/**
 * HDFC Bank Parser (`HDFCBK`, `HDFCBN`, `HDFCCB`)
 * Supports HDFC multi-line UPI alerts ("Sent Rs.X From HDFC Bank A/C *1234 To Merchant"),
 * Account debits/credits, and HDFC Credit Card transactions.
 */
class HDFCBankParser : BankParser() {
    override val bankName: String = "HDFC Bank"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("HDFC") || normalized in setOf("HDFCBK", "HDFCBN", "HDFCCB", "HDFCLN")
    }

    override fun extractMerchant(message: String, sender: String): String? {
        // HDFC multiline format: "Sent Rs.450.00\nFrom HDFC Bank A/C *4821\nTo Zomato Limited\nOn 05/11"
        val multilineTo = Regex("""To\s+([^\n\r]+?)(?:\r?\n|\s+On\s+|\s+Ref\s+|$)""", RegexOption.IGNORE_CASE)
        multilineTo.find(message)?.let {
            val cleaned = cleanMerchantName(it.groupValues[1])
            if (isValidMerchantName(cleaned)) return cleaned
        }

        // HDFC Card format: "Rs 1,299.00 spent on HDFC Bank Card x4821 at AMAZON INDIA on..."
        val cardAt = Regex("""at\s+([A-Za-z0-9\s&._'\-]+?)\s+on\s+[0-9]{1,2}""", RegexOption.IGNORE_CASE)
        cardAt.find(message)?.let {
            val cleaned = cleanMerchantName(it.groupValues[1])
            if (isValidMerchantName(cleaned)) return cleaned
        }

        return super.extractMerchant(message, sender)
    }
}

/**
 * State Bank of India Parser (`SBIINB`, `SBIPSG`, `SBICRD`, `ATMSBI`, `CBSSBI`)
 * Supports SBI YONO/UPI ("Dear UPI user A/C X1234 debited by 350.0 on date... trf to Merchant Refno...")
 * and SBI Card alerts.
 */
class SBIBankParser : BankParser() {
    override val bankName: String = "State Bank of India"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("SBI") || normalized in setOf("SBIINB", "SBIPSG", "SBICRD", "ATMSBI", "CBSSBI", "SBIUPI")
    }

    override fun extractAmount(message: String): BigDecimal? {
        // SBI often omits Rs. in UPI SMS: "A/C X9102 debited by 480.00 on date"
        val sbiDebitedBy = Regex("""(?:debited|credited)\s+by\s+(?:Rs\.?|INR|₹)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
        sbiDebitedBy.find(message)?.let {
            val amount = it.groupValues[1].replace(",", "").toBigDecimalOrNull()
            if (amount != null && amount > BigDecimal.ZERO) return amount
        }
        return super.extractAmount(message)
    }

    override fun extractMerchant(message: String, sender: String): String? {
        // SBI "trf to Swiggy Refno 431012345678"
        val trfTo = Regex("""trf\s+to\s+([A-Za-z0-9\s&._'\-]+?)(?:\s+Ref(?:\s*No)?|\s+on|\.|$)""", RegexOption.IGNORE_CASE)
        trfTo.find(message)?.let {
            val cleaned = cleanMerchantName(it.groupValues[1])
            if (isValidMerchantName(cleaned)) return cleaned
        }
        return super.extractMerchant(message, sender)
    }
}

/**
 * ICICI Bank Parser (`ICICIB`, `ICICIT`, `ICICIC`)
 * Supports ICICI Acct XX123 debited for Rs X; <Merchant> credited.
 */
class ICICIBankParser : BankParser() {
    override val bankName: String = "ICICI Bank"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("ICICI") || normalized in setOf("ICICIB", "ICICIT", "ICICIC")
    }

    override fun extractMerchant(message: String, sender: String): String? {
        // ICICI format: "ICICI Bank Acct XX819 debited for Rs 620.00 on 05-Nov-26; BIGBASKET credited. UPI:..."
        val semiCredited = Regex(""";\s*([A-Za-z0-9\s&._'\-@]+?)\s+credited""", RegexOption.IGNORE_CASE)
        semiCredited.find(message)?.let {
            val cleaned = cleanMerchantName(it.groupValues[1])
            if (isValidMerchantName(cleaned)) return cleaned
        }
        return super.extractMerchant(message, sender)
    }
}

/**
 * Axis Bank Parser (`AXISBK`, `AXISMR`, `AXISCC`)
 */
class AxisBankParser : BankParser() {
    override val bankName: String = "Axis Bank"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("AXIS") || normalized in setOf("AXISBK", "AXISMR", "AXISCC")
    }

    override fun extractMerchant(message: String, sender: String): String? {
        // Axis UPI format: "UPI/P2M/431098765432/STARBUCKS COFFEE"
        val axisUpi = Regex("""UPI/(?:P2[AM]/)?[0-9]+/([^\n\r./]+)""", RegexOption.IGNORE_CASE)
        axisUpi.find(message)?.let {
            val cleaned = cleanMerchantName(it.groupValues[1])
            if (isValidMerchantName(cleaned)) return cleaned
        }
        return super.extractMerchant(message, sender)
    }
}

/**
 * Kotak Mahindra Bank Parser (`KOTAKB`, `KOTAKM`, `KOTAKC`)
 */
class KotakBankParser : BankParser() {
    override val bankName: String = "Kotak Bank"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("KOTAK") || normalized in setOf("KOTAKB", "KOTAKM", "KOTAKC")
    }
}

/**
 * IDFC First Bank Parser (`IDFCFB`, `IDFCBK`)
 */
class IDFCFirstBankParser : BankParser() {
    override val bankName: String = "IDFC First Bank"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("IDFC") || normalized in setOf("IDFCFB", "IDFCBK")
    }
}

/**
 * IndusInd Bank Parser (`INDUSB`, `INDUSL`)
 */
class IndusIndBankParser : BankParser() {
    override val bankName: String = "IndusInd Bank"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("INDUS") || normalized in setOf("INDUSB", "INDUSL")
    }
}

/**
 * Yes Bank Parser (`YESBNK`, `YESBCC`)
 */
class YesBankParser : BankParser() {
    override val bankName: String = "Yes Bank"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("YESB") || normalized in setOf("YESBNK", "YESBCC")
    }
}

/**
 * Punjab National Bank Parser (`PNBSMS`, `PNBCRD`)
 */
class PNBBankParser : BankParser() {
    override val bankName: String = "Punjab National Bank"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("PNB") || normalized in setOf("PNBSMS", "PNBCRD")
    }
}

/**
 * Bank of Baroda Parser (`BARODA`, `BOBTXN`, `BOBSMS`)
 */
class BankOfBarodaParser : BankParser() {
    override val bankName: String = "Bank of Baroda"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("BOB") || normalized.contains("BARODA") || normalized in setOf("BARODA", "BOBTXN", "BOBSMS")
    }
}

/**
 * Federal Bank Parser (`FEDBNK`, `FEDFIB`)
 */
class FederalBankParser : BankParser() {
    override val bankName: String = "Federal Bank"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("FEDBNK") || normalized.contains("FEDERAL") || normalized == "FEDFIB"
    }
}

/**
 * Canara Bank Parser (`CANBNK`)
 */
class CanaraBankParser : BankParser() {
    override val bankName: String = "Canara Bank"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("CANBNK") || normalized.contains("CANARA")
    }
}

/**
 * Union Bank of India Parser (`UNIONB`)
 */
class UnionBankParser : BankParser() {
    override val bankName: String = "Union Bank"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("UNIONB") || normalized.contains("UNION")
    }
}

/**
 * OneCard Credit Card Parser (`ONECARD`, `FPLCRD`)
 */
class OneCardParser : BankParser() {
    override val bankName: String = "OneCard"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("ONECARD") || normalized.contains("ONECRD") || normalized == "FPLCRD"
    }

    override fun detectIsCard(message: String): Boolean = true
}

/**
 * American Express Parser (`AMEXIN`, `AMEX`)
 */
class AmexBankParser : BankParser() {
    override val bankName: String = "American Express"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("AMEX")
    }

    override fun detectIsCard(message: String): Boolean = true
}

/**
 * Paytm Payments Bank / Wallet Parser (`PAYTMB`, `PYTMPB`)
 */
class PaytmBankParser : BankParser() {
    override val bankName: String = "Paytm Bank"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("PAYTM") || normalized.contains("PYTM")
    }
}

/**
 * Airtel Payments Bank Parser (`AIRBNK`, `AIRTEL`)
 */
class AirtelPaymentsBankParser : BankParser() {
    override val bankName: String = "Airtel Payments Bank"

    override fun canHandle(sender: String): Boolean {
        val normalized = sender.uppercase()
        return normalized.contains("AIRBNK") || normalized.contains("AIRTEL")
    }
}

/**
 * Generic fallback Bank/UPI parser for any bank DLT sender not matched above.
 */
class GenericBankParser(override val bankName: String = "Bank Account") : BankParser() {
    override fun canHandle(sender: String): Boolean = true
}
