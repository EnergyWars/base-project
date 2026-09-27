package com.wafflehq.lib.uicore.time

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class TimeFormatsTest {

    @Test
    fun `pads single digit hour and minute`() {
        assertEquals("09:05", LocalTime.of(9, 5).format(TimeFormats.HOUR_MINUTE))
    }

    @Test
    fun `formats last minute of day`() {
        assertEquals("23:59", LocalTime.of(23, 59).format(TimeFormats.HOUR_MINUTE))
    }

    @Test
    fun `formats midnight`() {
        assertEquals("00:00", LocalTime.MIDNIGHT.format(TimeFormats.HOUR_MINUTE))
    }

    @Test
    fun `drops seconds`() {
        assertEquals("12:34", LocalTime.of(12, 34, 56).format(TimeFormats.HOUR_MINUTE))
    }
}
