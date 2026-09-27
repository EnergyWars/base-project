package com.wafflehq.lib.uicore.finance

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinanceAmountFormatterTest {

    @Test
    fun format_usesCorrectMinorUnitConversion() {
        val formatted = FinanceAmountFormatter.format(123_45L, "EUR", Locale.GERMANY)
        assertTrue("expected fractional part in $formatted", formatted.contains("123") && formatted.contains("45"))
    }

    @Test
    fun format_negativeAmount_keepsSign() {
        val formatted = FinanceAmountFormatter.format(-500L, "EUR", Locale.GERMANY)
        assertTrue("expected a minus sign in $formatted", formatted.contains("-"))
    }

    @Test
    fun format_zeroAmount_doesNotThrow() {
        val formatted = FinanceAmountFormatter.format(0L, "USD", Locale.US)
        assertEquals("$0.00", formatted)
    }

    @Test
    fun format_unknownCurrencyCode_fallsBackWithoutCrashing() {
        val formatted = FinanceAmountFormatter.format(100L, "NOT_A_CURRENCY", Locale.US)
        assertTrue(formatted.isNotBlank())
    }

    @Test
    fun parseAmountCents_commaIsTreatedAsDecimalSeparator() {
        assertEquals(1050L, parseAmountCents("10,50"))
    }

    @Test
    fun parseAmountCents_dotIsTreatedAsDecimalSeparator() {
        assertEquals(1050L, parseAmountCents("10.50"))
    }

    @Test
    fun parseAmountCents_blankInput_returnsNull() {
        assertNull(parseAmountCents(""))
        assertNull(parseAmountCents("   "))
    }

    @Test
    fun parseAmountCents_nonNumericInput_returnsNull() {
        assertNull(parseAmountCents("abc"))
    }

    @Test
    fun parseAmountCents_zero_rejectedByDefault() {
        assertNull(parseAmountCents("0"))
    }

    @Test
    fun parseAmountCents_zero_allowedWhenAllowZeroIsTrue() {
        assertEquals(0L, parseAmountCents("0", allowZero = true))
    }

    @Test
    fun parseAmountCents_negative_alwaysRejected() {
        assertNull(parseAmountCents("-5"))
        assertNull(parseAmountCents("-5", allowZero = true))
    }
}
