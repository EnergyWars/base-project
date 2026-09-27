package com.wafflehq.lib.uicore.csv

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvParserTest {

    @Test
    fun parse_emptyInput_returnsNoRows() {
        assertEquals(emptyList<List<String>>(), CsvParser.parse(""))
    }

    @Test
    fun parse_simpleRows_splitsOnCommaAndNewline() {
        assertEquals(
            listOf(listOf("a", "b"), listOf("c", "d")),
            CsvParser.parse("a,b\nc,d\n")
        )
    }

    @Test
    fun parse_lastRowWithoutTrailingNewline_isKept() {
        assertEquals(listOf(listOf("a", "b")), CsvParser.parse("a,b"))
    }

    @Test
    fun parse_crlfLineEndings_areTreatedAsRowSeparators() {
        assertEquals(
            listOf(listOf("a", "b"), listOf("c", "d")),
            CsvParser.parse("a,b\r\nc,d\r\n")
        )
    }

    @Test
    fun parse_quotedFieldWithComma_staysOneField() {
        assertEquals(listOf(listOf("a, b", "c")), CsvParser.parse("\"a, b\",c\n"))
    }

    @Test
    fun parse_quotedFieldWithNewline_staysOneField() {
        assertEquals(listOf(listOf("line1\nline2", "x")), CsvParser.parse("\"line1\nline2\",x\n"))
    }

    @Test
    fun parse_quotedFieldWithCrlf_keepsCarriageReturnInsideQuotes() {
        assertEquals(listOf(listOf("line1\r\nline2")), CsvParser.parse("\"line1\r\nline2\"\r\n"))
    }

    @Test
    fun parse_doubledQuotes_becomeSingleQuote() {
        assertEquals(listOf(listOf("say \"hi\"", "x")), CsvParser.parse("\"say \"\"hi\"\"\",x\n"))
    }

    @Test
    fun parse_blankRows_areSkipped() {
        assertEquals(
            listOf(listOf("a"), listOf("b")),
            CsvParser.parse("a\n\n \nb\n\n")
        )
    }

    @Test
    fun parse_emptyFields_arePreserved() {
        assertEquals(listOf(listOf("a", "", "c")), CsvParser.parse("a,,c\n"))
    }

    @Test
    fun parse_roundTripsEncoderOutput() {
        val header = listOf("Name", "Note")
        val rows = listOf(listOf("Doe, John", "Say \"hi\""), listOf("multi\nline", "plain"))

        val parsed = CsvParser.parse(CsvEncoding.encode(header, rows))

        assertEquals(listOf(header) + rows, parsed)
    }
}
