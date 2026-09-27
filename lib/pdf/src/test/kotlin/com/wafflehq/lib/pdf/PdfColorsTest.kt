package com.wafflehq.lib.pdf

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class PdfColorsTest {

    @After
    fun restoreIdentity() {
        PdfColors.themeTransform = { it }
    }

    @Test
    fun `themed leaves a color untouched by default`() {
        assertEquals(0xFF112233.toInt(), PdfColors.themed(0xFF112233.toInt()))
    }

    @Test
    fun `themed applies the installed transform`() {
        PdfColors.themeTransform = { it xor 0x00FFFFFF }

        assertEquals(0xFFEEDDCC.toInt(), PdfColors.themed(0xFF112233.toInt()))
    }

    @Test
    fun `a later transform replaces the earlier one`() {
        PdfColors.themeTransform = { 1 }
        PdfColors.themeTransform = { 2 }

        assertEquals(2, PdfColors.themed(0))
    }
}
