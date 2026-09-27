package com.wafflehq.base.data

import com.wafflehq.base.data.model.ThemeMode
import com.wafflehq.base.ui.theme.resolveDarkTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `fromName returns the matching mode`() {
        ThemeMode.entries.forEach { assertEquals(it, ThemeMode.fromName(it.name)) }
    }

    @Test
    fun `fromName falls back to system for null and unknown names`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName("bogus"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName(""))
    }

    @Test
    fun `dark theme resolution honours explicit modes and the system setting`() {
        assertTrue(resolveDarkTheme(ThemeMode.DARK, systemDark = false))
        assertFalse(resolveDarkTheme(ThemeMode.LIGHT, systemDark = true))
        assertTrue(resolveDarkTheme(ThemeMode.SYSTEM, systemDark = true))
        assertFalse(resolveDarkTheme(ThemeMode.SYSTEM, systemDark = false))
    }
}
