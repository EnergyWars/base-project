package com.wafflehq.uikit.folders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FolderNamesTest {

    @Test
    fun normalize_trimsWhitespace() {
        assertEquals("Kuchen", FolderNames.normalize("  Kuchen \n"))
    }

    @Test
    fun normalize_blank_returnsNull() {
        assertNull(FolderNames.normalize(""))
        assertNull(FolderNames.normalize("   "))
    }

    @Test
    fun normalize_keepsInnerSpaces() {
        assertEquals("Mein Ordner", FolderNames.normalize("Mein Ordner"))
    }
}
