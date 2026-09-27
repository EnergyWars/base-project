package com.wafflehq.lib.uicore.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime
import java.util.Locale

class ShortTimeFormatterTest {

    @Test
    fun `german locale uses 24 hour clock without seconds`() {
        assertEquals("08:05", LocalTime.of(8, 5).format(shortTimeFormatter(Locale.GERMANY)))
    }

    @Test
    fun `english us locale uses 12 hour clock`() {
        val text = LocalTime.of(15, 30).format(shortTimeFormatter(Locale.US))
        assertTrue(text, text.startsWith("3:30"))
    }

    @Test
    fun `default locale matches the explicit default`() {
        val time = LocalTime.of(23, 59)
        assertEquals(time.format(shortTimeFormatter(Locale.getDefault())), time.format(shortTimeFormatter()))
    }
}
