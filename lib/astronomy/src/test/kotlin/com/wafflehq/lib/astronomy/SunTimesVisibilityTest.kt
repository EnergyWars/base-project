package com.wafflehq.lib.astronomy

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

class SunTimesVisibilityTest {

    private fun fullTimes() = SunTimes(
        morningBlueStart = LocalTime.of(4, 0),
        sunrise = LocalTime.of(5, 0),
        morningGoldenEnd = LocalTime.of(6, 0),
        eveningGoldenStart = LocalTime.of(20, 0),
        sunset = LocalTime.of(21, 0),
        eveningBlueEnd = LocalTime.of(22, 0)
    )

    @Test
    fun `all visible keeps every segment`() {
        val result = fullTimes().withVisibleSegments(true, true, true, true)
        assertEquals(fullTimes(), result)
    }

    @Test
    fun `hiding evening golden removes its boundary but keeps sunset`() {
        val result = fullTimes().withVisibleSegments(
            showMorningGolden = true,
            showEveningGolden = false,
            showMorningBlue = true,
            showEveningBlue = true
        )
        assertNotNull(result)
        assertNull(result!!.eveningGoldenStart)
        assertNotNull(result.sunset)
        assertNotNull(result.eveningBlueEnd)
        assertNotNull(result.morningGoldenEnd)
    }

    @Test
    fun `hiding morning golden keeps sunrise for morning blue segment`() {
        val result = fullTimes().withVisibleSegments(
            showMorningGolden = false,
            showEveningGolden = true,
            showMorningBlue = true,
            showEveningBlue = true
        )
        assertNotNull(result)
        assertNull(result!!.morningGoldenEnd)
        assertNotNull(result.sunrise)
        assertNotNull(result.morningBlueStart)
    }

    @Test
    fun `hiding all segments returns null`() {
        val result = fullTimes().withVisibleSegments(false, false, false, false)
        assertNull(result)
    }

    @Test
    fun `hiding all but one keeps result non null`() {
        val result = fullTimes().withVisibleSegments(
            showMorningGolden = false,
            showEveningGolden = false,
            showMorningBlue = false,
            showEveningBlue = true
        )
        assertNotNull(result)
        assertNotNull(result!!.eveningBlueEnd)
        assertNull(result.morningBlueStart)
        assertNull(result.morningGoldenEnd)
        assertNull(result.eveningGoldenStart)
    }

    @Test
    fun `polar day with null boundaries returns null when no segment visible`() {
        val polar = SunTimes(null, null, null, null, null, null)
        assertNull(polar.withVisibleSegments(true, true, true, true))
    }
}
