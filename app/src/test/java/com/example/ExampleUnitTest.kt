package com.example

import com.ritesh.parser.core.TransactionType
import com.ritesh.parser.core.bank.BankParserFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun bankSmsWithDoNotShareOtpWarning_isParsedCorrectly() {
        val smsBody = "Rs. 450.00 debited from A/c XX4821 on 06-11-26 to VPA zomato@hdfcbank (UPI Ref No 631011223344). Never share OTP or PIN with anyone."
        val parser = BankParserFactory.getParser("AD-HDFCBK-S", smsBody)
        assertNotNull(parser)
        val parsed = parser!!.parse(smsBody, "AD-HDFCBK-S", 1730900000000L)
        assertNotNull(parsed)
        assertEquals(TransactionType.EXPENSE, parsed!!.type)
        assertEquals(450.0, parsed.amount.toDouble(), 0.01)
    }

    @Test
    fun pureOtpMessage_isIgnored() {
        val otpSms = "482910 is your OTP for HDFC Bank NetBanking login. Do not share with anyone."
        val parser = BankParserFactory.getParser("AD-HDFCBK-S", otpSms)
        val parsed = parser?.parse(otpSms, "AD-HDFCBK-S", 1730900000000L)
        assertNull(parsed)
    }

    @Test
    fun distinctSmsMessagesAtDifferentSeconds_haveUniqueTransactionIds() {
        val body1 = "Sent Rs.250.00 From HDFC Bank A/C *4821 To SWIGGY On 05/11/26 Ref 111122223333"
        val body2 = "Sent Rs.250.00 From HDFC Bank A/C *4821 To SWIGGY On 05/11/26 Ref 444455556666"
        val parser = BankParserFactory.getParser("AD-HDFCBK-S", body1)!!
        val tx1 = parser.parse(body1, "AD-HDFCBK-S", 1730900001000L)!!
        val tx2 = parser.parse(body2, "AD-HDFCBK-S", 1730900025000L)!!
        assertNotEquals(tx1.generateTransactionId(), tx2.generateTransactionId())
    }
}
