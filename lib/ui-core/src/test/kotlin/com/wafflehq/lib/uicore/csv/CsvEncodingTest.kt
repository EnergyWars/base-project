package com.wafflehq.lib.uicore.csv

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvEncodingTest {

    @Test
    fun encode_writesHeaderFollowedByRows() {
        val csv = CsvEncoding.encode(
            header = listOf("Date", "Value"),
            rows = listOf(listOf("2026-03-05", "70.5"), listOf("2026-03-06", "71.0"))
        )
        assertEquals("Date,Value\n2026-03-05,70.5\n2026-03-06,71.0\n", csv)
    }

    @Test
    fun encode_noRows_writesOnlyHeader() {
        val csv = CsvEncoding.encode(header = listOf("Date", "Value"), rows = emptyList())
        assertEquals("Date,Value\n", csv)
    }

    @Test
    fun encode_valueWithComma_isQuoted() {
        val csv = CsvEncoding.encode(header = listOf("Name"), rows = listOf(listOf("Doe, John")))
        assertEquals("Name\n\"Doe, John\"\n", csv)
    }

    @Test
    fun encode_valueWithQuote_isQuotedAndDoubled() {
        val csv = CsvEncoding.encode(header = listOf("Note"), rows = listOf(listOf("Say \"hi\"")))
        assertEquals("Note\n\"Say \"\"hi\"\"\"\n", csv)
    }

    @Test
    fun encode_valueWithNewline_isQuoted() {
        val csv = CsvEncoding.encode(header = listOf("Note"), rows = listOf(listOf("Line1\nLine2")))
        assertEquals("Note\n\"Line1\nLine2\"\n", csv)
    }

    @Test
    fun encode_plainValue_isNotQuoted() {
        val csv = CsvEncoding.encode(header = listOf("Value"), rows = listOf(listOf("plain")))
        assertEquals("Value\nplain\n", csv)
    }

    @Test
    fun encode_formulaTrigger_isPrefixedWithApostrophe() {
        val csv = CsvEncoding.encode(header = listOf("Name"), rows = listOf(listOf("=HYPERLINK(\"http://evil\")")))
        assertEquals("Name\n\"'=HYPERLINK(\"\"http://evil\"\")\"\n", csv)
    }

    @Test
    fun encode_atSignAndPlus_arePrefixed() {
        val csv = CsvEncoding.encode(header = listOf("A", "B"), rows = listOf(listOf("@SUM(1)", "+cmd")))
        assertEquals("A,B\n'@SUM(1),'+cmd\n", csv)
    }

    @Test
    fun encode_negativeNumber_isLeftUntouched() {
        val csv = CsvEncoding.encode(header = listOf("Delta"), rows = listOf(listOf("-1.5", "-2")))
        assertEquals("Delta\n-1.5,-2\n", csv)
    }

    @Test
    fun encode_dateAndTimeValues_areLeftUntouched() {
        val csv = CsvEncoding.encode(header = listOf("D"), rows = listOf(listOf("2026-06-01", "08:30")))
        assertEquals("D\n2026-06-01,08:30\n", csv)
    }
}
