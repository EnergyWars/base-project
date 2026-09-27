package com.wafflehq.lib.modules.calendar

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class CalendarDayMarkerTest {

    @Test
    fun markerDefaultsAreOptional() {
        val marker = CalendarDayMarker(sourceId = "hobby")
        assertNull(marker.icon)
        assertNull(marker.colorTokenId)
        assertNull(marker.label)
    }

    @Test
    fun sourceCanBeExpressedAsLambda() = runTest {
        val day = LocalDate.of(2026, 9, 2)
        val source = CalendarDayMarkerSource { from, to ->
            flowOf(mapOf(from to listOf(CalendarDayMarker("x", label = to.toString()))))
        }
        val result = source.markers(day, day.plusDays(1)).first()
        assertEquals(listOf(CalendarDayMarker("x", label = "2026-09-03")), result[day])
    }
}
