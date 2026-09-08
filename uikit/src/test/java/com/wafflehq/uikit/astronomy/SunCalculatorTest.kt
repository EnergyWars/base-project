package com.wafflehq.uikit.astronomy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class SunCalculatorTest {

    private val utc = ZoneOffset.UTC

    @Test
    fun `sunrise is before noon for Berlin in summer`() {
        val times = SunCalculator.calculate(LocalDate.of(2024, 6, 21), 52.52, 13.41, utc)
        assertNotNull(times.sunrise)
        assertTrue(times.sunrise!!.isBefore(LocalTime.NOON))
    }

    @Test
    fun `sunset is after noon for Berlin in summer`() {
        val times = SunCalculator.calculate(LocalDate.of(2024, 6, 21), 52.52, 13.41, utc)
        assertNotNull(times.sunset)
        assertTrue(times.sunset!!.isAfter(LocalTime.NOON))
    }

    @Test
    fun `morning blue hour starts before sunrise`() {
        val times = SunCalculator.calculate(LocalDate.of(2024, 6, 21), 52.52, 13.41, utc)
        assertNotNull(times.morningBlueStart)
        assertNotNull(times.sunrise)
        assertTrue(times.morningBlueStart!!.isBefore(times.sunrise!!))
    }

    @Test
    fun `morning golden hour ends after sunrise`() {
        val times = SunCalculator.calculate(LocalDate.of(2024, 6, 21), 52.52, 13.41, utc)
        assertNotNull(times.sunrise)
        assertNotNull(times.morningGoldenEnd)
        assertTrue(times.morningGoldenEnd!!.isAfter(times.sunrise!!))
    }

    @Test
    fun `evening golden hour starts before sunset`() {
        val times = SunCalculator.calculate(LocalDate.of(2024, 6, 21), 52.52, 13.41, utc)
        assertNotNull(times.eveningGoldenStart)
        assertNotNull(times.sunset)
        assertTrue(times.eveningGoldenStart!!.isBefore(times.sunset!!))
    }

    @Test
    fun `evening blue hour ends after sunset`() {
        val times = SunCalculator.calculate(LocalDate.of(2024, 6, 21), 52.52, 13.41, utc)
        assertNotNull(times.sunset)
        assertNotNull(times.eveningBlueEnd)
        assertTrue(times.eveningBlueEnd!!.isAfter(times.sunset!!))
    }

    @Test
    fun `all six times are in chronological order on equinox Paris`() {
        val times = SunCalculator.calculate(LocalDate.of(2024, 3, 20), 48.85, 2.35, utc)
        val ordered = listOfNotNull(
            times.morningBlueStart, times.sunrise, times.morningGoldenEnd,
            times.eveningGoldenStart, times.sunset, times.eveningBlueEnd
        )
        assertEquals(6, ordered.size)
        for (i in 0 until ordered.size - 1) {
            assertTrue("Index $i (${ordered[i]}) should be before ${i + 1} (${ordered[i + 1]})",
                ordered[i].isBefore(ordered[i + 1]))
        }
    }

    @Test
    fun `polar region in arctic winter returns null for sunrise and sunset`() {
        val times = SunCalculator.calculate(LocalDate.of(2024, 12, 21), 89.0, 0.0, utc)
        assertNull(times.sunrise)
        assertNull(times.sunset)
    }

    @Test
    fun `equator on equinox sunrise close to 06h00 UTC`() {
        val times = SunCalculator.calculate(LocalDate.of(2024, 3, 20), 0.0, 0.0, utc)
        assertNotNull(times.sunrise)
        val sunrise = times.sunrise!!
        val minutes = sunrise.hour * 60 + sunrise.minute
        assertTrue("Sunrise at equator on equinox should be near 6:00, got ${times.sunrise}", minutes in 330..390)
    }

    @Test
    fun `winter sunrise later than summer sunrise for northern hemisphere`() {
        val summer = SunCalculator.calculate(LocalDate.of(2024, 6, 21), 52.52, 13.41, utc)
        val winter = SunCalculator.calculate(LocalDate.of(2024, 12, 21), 52.52, 13.41, utc)
        assertNotNull(summer.sunrise)
        assertNotNull(winter.sunrise)
        assertTrue("Winter sunrise should be later than summer sunrise",
            winter.sunrise!!.isAfter(summer.sunrise!!))
    }

    @Test
    fun `winter sunset earlier than summer sunset for northern hemisphere`() {
        val summer = SunCalculator.calculate(LocalDate.of(2024, 6, 21), 52.52, 13.41, utc)
        val winter = SunCalculator.calculate(LocalDate.of(2024, 12, 21), 52.52, 13.41, utc)
        assertNotNull(summer.sunset)
        assertNotNull(winter.sunset)
        assertTrue("Winter sunset should be earlier than summer sunset",
            winter.sunset!!.isBefore(summer.sunset!!))
    }

    @Test
    fun `southern hemisphere has opposite seasonality`() {
        val aest = ZoneOffset.ofHours(10)
        val jun = SunCalculator.calculate(LocalDate.of(2024, 6, 21), -33.87, 151.21, aest)
        val dec = SunCalculator.calculate(LocalDate.of(2024, 12, 21), -33.87, 151.21, aest)
        assertNotNull(jun.sunrise)
        assertNotNull(dec.sunrise)
        assertTrue("December (local summer) sunrise in Sydney should be earlier than June (local winter)",
            dec.sunrise!!.isBefore(jun.sunrise!!))
    }

    @Test
    fun `eastern longitude sunrise is earlier UTC than western at same latitude`() {
        val london = SunCalculator.calculate(LocalDate.of(2024, 6, 21), 51.5, 0.0, utc)
        val newYork = SunCalculator.calculate(LocalDate.of(2024, 6, 21), 51.5, -74.0, utc)
        assertNotNull(london.sunrise)
        assertNotNull(newYork.sunrise)
        assertTrue("London sunrise UTC should be before NY sunrise UTC",
            london.sunrise!!.isBefore(newYork.sunrise!!))
    }

    @Test
    fun `morning golden hour duration is approximately 40 to 90 minutes in summer`() {
        val times = SunCalculator.calculate(LocalDate.of(2024, 6, 21), 52.52, 13.41, utc)
        assertNotNull(times.sunrise)
        assertNotNull(times.morningGoldenEnd)
        val minutes = java.time.Duration.between(times.sunrise!!, times.morningGoldenEnd!!).toMinutes()
        assertTrue("Morning golden hour should be 40-90 min, got $minutes", minutes in 40..90)
    }

    @Test
    fun `blue hour duration is 20 to 60 minutes`() {
        val times = SunCalculator.calculate(LocalDate.of(2024, 3, 20), 51.5, 0.0, utc)
        assertNotNull(times.morningBlueStart)
        assertNotNull(times.sunrise)
        val minutes = java.time.Duration.between(times.morningBlueStart!!, times.sunrise!!).toMinutes()
        assertTrue("Morning blue hour should be 20-60 min, got $minutes", minutes in 20..60)
    }

    @Test
    fun `julian day for year 2000 jan 1`() {
        val jd = SunCalculator.julianDay(LocalDate.of(2000, 1, 1))
        assertEquals(2451545.0, jd, 1.0)
    }

    @Test
    fun `calcTime returns null for impossible zenith`() {
        val jd = SunCalculator.julianDay(LocalDate.of(2024, 12, 21))
        val result = SunCalculator.calcTime(jd, 89.0, 0.0, 90.833, true, 0)
        assertNull(result)
    }

    @Test
    fun `calculate works across multiple years`() {
        listOf(2020, 2022, 2024, 2025, 2026).forEach { year ->
            val times = SunCalculator.calculate(LocalDate.of(year, 6, 21), 52.52, 13.41, utc)
            assertNotNull("Sunrise should exist in $year", times.sunrise)
            assertTrue("Sunrise in $year should be before noon", times.sunrise!!.isBefore(LocalTime.NOON))
        }
    }
}
