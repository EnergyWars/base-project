package com.wafflehq.lib.uicore.components

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class IncompleteDraftBannerFormatTest {

    @Test
    fun formatDraftTimestamp_matchesShortLocalizedDateTimeFormat() {
        val dateTime = LocalDateTime.of(2026, 3, 5, 14, 30)
        val epochMs = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val expected = dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.getDefault()))
        assertEquals(expected, formatDraftTimestamp(epochMs))
    }

    @Test
    fun formatDraftTimestamp_roundTripsDifferentInstants() {
        val first = LocalDateTime.of(2026, 1, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val second = LocalDateTime.of(2026, 12, 31, 23, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        assertEquals(false, formatDraftTimestamp(first) == formatDraftTimestamp(second))
    }
}
