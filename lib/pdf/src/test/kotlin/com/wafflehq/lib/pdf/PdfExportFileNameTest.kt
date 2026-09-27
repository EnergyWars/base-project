package com.wafflehq.lib.pdf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfExportFileNameTest {

    @Test
    fun combinesPrefixAndTitle() {
        assertEquals("Rezept - Spaghetti Bolognese.pdf", pdfExportFileName("Rezept", "Spaghetti Bolognese"))
    }

    @Test
    fun fallsBackToPrefixWhenTitleIsBlank() {
        assertEquals("Rezept.pdf", pdfExportFileName("Rezept", "   "))
    }

    @Test
    fun fallsBackToPrefixWhenTitleConsistsOnlyOfUnsafeCharacters() {
        assertEquals("Rezept.pdf", pdfExportFileName("Rezept", "  /:  "))
    }

    @Test
    fun stripsUnsafeCharacters() {
        val name = pdfExportFileName("Rezept", "Salat/Suppe: \"lecker\" <fein> *gut* ?ja? |ok|")

        assertFalse(name.contains("/"))
        assertFalse(name.contains(":"))
        assertFalse(name.contains("\""))
        assertFalse(name.contains("<"))
        assertFalse(name.contains(">"))
        assertFalse(name.contains("*"))
        assertFalse(name.contains("?"))
        assertFalse(name.contains("|"))
        assertTrue(name.endsWith(".pdf"))
    }

    @Test
    fun stripsControlCharacters() {
        assertEquals("Rezept - AB.pdf", pdfExportFileName("Rezept", "A\u0000\nB"))
    }

    @Test
    fun removesUnsafeCharactersAndCollapsesWhitespace() {
        assertEquals("Rezept - Mehl Ei Milch.pdf", pdfExportFileName("Rezept", "  Mehl/ Ei:   Milch?  "))
    }

    @Test
    fun truncatesLongTitles() {
        assertEquals("Rezept - ${"A".repeat(80)}.pdf", pdfExportFileName("Rezept", "A".repeat(200)))
    }
}
