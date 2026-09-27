package com.wafflehq.lib.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticLogFormatterTest {

    @Test
    fun format_emptyList_returnsPlaceholder() {
        val result = DiagnosticLogFormatter.format(emptyList(), "no logs")

        assertEquals("no logs", result)
    }

    @Test
    fun format_singleEntry_containsTimestampLevelTagAndMessage() {
        val entry = DiagnosticLogEntry(
            timestampEpochMs = 0L,
            level = DiagnosticLevel.ERROR,
            tag = "Test",
            message = "something failed"
        )

        val result = DiagnosticLogFormatter.format(listOf(entry), "no logs")

        assertTrue(result.contains("ERROR"))
        assertTrue(result.contains("[Test]"))
        assertTrue(result.contains("something failed"))
        assertTrue(result.startsWith("19"))
    }

    @Test
    fun format_entryWithStackTrace_appendsStackTraceOnNewLine() {
        val entry = DiagnosticLogEntry(
            timestampEpochMs = 0L,
            level = DiagnosticLevel.CRASH,
            tag = "Crash",
            message = "boom",
            stackTrace = "at com.example.Foo.bar"
        )

        val result = DiagnosticLogFormatter.format(listOf(entry), "no logs")

        assertTrue(result.contains("boom\nat com.example.Foo.bar"))
    }

    @Test
    fun format_entryWithBlankStackTrace_omitsStackTraceLine() {
        val entry = DiagnosticLogEntry(
            timestampEpochMs = 0L,
            level = DiagnosticLevel.WARN,
            tag = "Test",
            message = "hint",
            stackTrace = "  "
        )

        val result = DiagnosticLogFormatter.format(listOf(entry), "no logs")

        assertEquals(1, result.lines().size)
    }

    @Test
    fun format_multipleEntries_ordersChronologicallyAscending() {
        val newer = DiagnosticLogEntry(
            timestampEpochMs = 2000L,
            level = DiagnosticLevel.DEBUG,
            tag = "Test",
            message = "second"
        )
        val older = DiagnosticLogEntry(
            timestampEpochMs = 1000L,
            level = DiagnosticLevel.DEBUG,
            tag = "Test",
            message = "first"
        )

        val result = DiagnosticLogFormatter.format(listOf(newer, older), "no logs")

        val indexOfFirst = result.indexOf("first")
        val indexOfSecond = result.indexOf("second")
        assertTrue(indexOfFirst in 0 until indexOfSecond)
    }
}
