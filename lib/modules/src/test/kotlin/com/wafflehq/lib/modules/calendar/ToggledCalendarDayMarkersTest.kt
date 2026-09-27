package com.wafflehq.lib.modules.calendar

import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ToggledCalendarDayMarkersTest {

    private val marker = CalendarDayMarker(sourceId = "test")

    @Test
    fun toggledCalendarDayMarkers_disabled_returnsEmptyMapWithoutInvokingSource() = runTest {
        var invoked = false
        val result = toggledCalendarDayMarkers(flowOf(false)) {
            invoked = true
            flowOf(mapOf(LocalDate.of(2026, 1, 1) to listOf(marker)))
        }.first()

        assertTrue(result.isEmpty())
        assertTrue(!invoked)
    }

    @Test
    fun toggledCalendarDayMarkers_enabled_delegatesToSource() = runTest {
        val expected = mapOf(LocalDate.of(2026, 1, 1) to listOf(marker))
        val result = toggledCalendarDayMarkers(flowOf(true)) { flowOf(expected) }.first()

        assertEquals(expected, result)
    }

    @Test
    fun singleMarkerPerDate_filtersToRangeAndDeduplicatesDates() = runTest {
        data class Entry(val epochDay: Long)

        val from = LocalDate.of(2026, 3, 1)
        val to = LocalDate.of(2026, 3, 31)
        val inRange = LocalDate.of(2026, 3, 10)
        val entries = listOf(
            Entry(inRange.toEpochDay()),
            Entry(inRange.toEpochDay()),
            Entry(LocalDate.of(2026, 2, 1).toEpochDay())
        )

        val result = singleMarkerPerDate(
            entries = flowOf(entries),
            from = from,
            to = to,
            marker = marker,
            dateOf = { LocalDate.ofEpochDay(it.epochDay) }
        ).first()

        assertEquals(mapOf(inRange to listOf(marker)), result)
    }

    @Test
    fun singleMarkerPerDate_noEntriesInRange_returnsEmptyMap() = runTest {
        val result = singleMarkerPerDate(
            entries = flowOf(listOf(LocalDate.of(2026, 1, 1))),
            from = LocalDate.of(2026, 3, 1),
            to = LocalDate.of(2026, 3, 31),
            marker = marker,
            dateOf = { it }
        ).first()

        assertTrue(result.isEmpty())
    }
}
