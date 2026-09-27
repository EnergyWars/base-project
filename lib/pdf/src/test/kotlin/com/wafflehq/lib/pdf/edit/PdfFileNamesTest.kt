package com.wafflehq.lib.pdf.edit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class PdfFileNamesTest {

    @Test
    fun `sanitize appends the pdf extension`() {
        assertEquals("Invoice.pdf", PdfFileNames.sanitize("Invoice", "fallback"))
    }

    @Test
    fun `sanitize does not duplicate an existing extension regardless of case`() {
        assertEquals("Invoice.pdf", PdfFileNames.sanitize("Invoice.pdf", "fallback"))
        assertEquals("Invoice.pdf", PdfFileNames.sanitize("Invoice.PDF", "fallback"))
    }

    @Test
    fun `sanitize replaces path separators and reserved characters`() {
        assertEquals("a b c d e f g h i.pdf", PdfFileNames.sanitize("a/b\\c:d*e?f\"g<h>i", "fallback"))
    }

    @Test
    fun `sanitize never yields a path traversal segment`() {
        val result = PdfFileNames.sanitize("../../etc/passwd", "fallback")

        assertTrue(!result.contains("/") && !result.contains("\\"))
    }

    @Test
    fun `sanitize strips control characters and collapses whitespace`() {
        assertEquals("a b.pdf", PdfFileNames.sanitize("a\u0000\u0007   b\n", "fallback"))
    }

    @Test
    fun `sanitize uses the fallback for blank or extension only input`() {
        assertEquals("Scan.pdf", PdfFileNames.sanitize("   ", "Scan"))
        assertEquals("Scan.pdf", PdfFileNames.sanitize(".pdf", "Scan"))
        assertEquals("Scan.pdf", PdfFileNames.sanitize("...", "Scan"))
    }

    @Test
    fun `sanitize falls back to a generic name when the fallback is unusable`() {
        assertEquals("document.pdf", PdfFileNames.sanitize("", "///"))
    }

    @Test
    fun `sanitize limits the base name length`() {
        val result = PdfFileNames.sanitize("x".repeat(300), "fallback")

        assertEquals(PdfFileNames.MAX_BASE_NAME_LENGTH + PdfFileNames.EXTENSION.length, result.length)
    }

    @Test
    fun `sanitize strips trailing dots so the name stays valid on every file system`() {
        assertEquals("report.pdf", PdfFileNames.sanitize("report. .", "fallback"))
    }

    @Test
    fun `suggest combines prefix and timestamp`() {
        val time = LocalDateTime.of(2026, 9, 20, 15, 30)

        assertEquals("Scan-20260920-1530", PdfFileNames.suggest(" Scan ", time))
    }
}
