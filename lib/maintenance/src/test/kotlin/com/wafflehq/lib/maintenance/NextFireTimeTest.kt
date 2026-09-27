package com.wafflehq.lib.maintenance

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class NextFireTimeTest {

    private val zone = ZoneId.of("Europe/Berlin")

    @Test
    fun `fires today when target time is still ahead`() {
        val now = ZonedDateTime.of(LocalDate.of(2026, 9, 12), LocalTime.of(8, 0), zone)

        val fireTime = NextFireTime.at(hour = 20, minute = 30, zone = zone, now = now)

        assertEquals(LocalDate.of(2026, 9, 12), fireTime.toLocalDate())
        assertEquals(LocalTime.of(20, 30), fireTime.toLocalTime())
    }

    @Test
    fun `rolls over to tomorrow when target time already passed`() {
        val now = ZonedDateTime.of(LocalDate.of(2026, 9, 12), LocalTime.of(21, 0), zone)

        val fireTime = NextFireTime.at(hour = 20, minute = 30, zone = zone, now = now)

        assertEquals(LocalDate.of(2026, 9, 13), fireTime.toLocalDate())
        assertEquals(LocalTime.of(20, 30), fireTime.toLocalTime())
    }

    @Test
    fun `rolls over to tomorrow when now equals target time exactly`() {
        val now = ZonedDateTime.of(LocalDate.of(2026, 9, 12), LocalTime.of(20, 30), zone)

        val fireTime = NextFireTime.at(hour = 20, minute = 30, zone = zone, now = now)

        assertEquals(LocalDate.of(2026, 9, 13), fireTime.toLocalDate())
    }

    @Test
    fun `delayMillis matches difference between fire time and now`() {
        val now = ZonedDateTime.of(LocalDate.of(2026, 9, 12), LocalTime.of(8, 0), zone)

        val delay = NextFireTime.delayMillis(hour = 20, minute = 30, zone = zone, now = now)

        val expected = NextFireTime.at(hour = 20, minute = 30, zone = zone, now = now)
            .toInstant().toEpochMilli() - now.toInstant().toEpochMilli()
        assertEquals(expected, delay)
        assert(delay > 0)
    }

    @Test
    fun `delayMillis is never negative for the rolled-over day`() {
        val now = ZonedDateTime.of(LocalDate.of(2026, 9, 12), LocalTime.of(23, 59), zone)

        val delay = NextFireTime.delayMillis(hour = 0, minute = 0, zone = zone, now = now)

        assert(delay > 0)
    }

    @Test
    fun `tomorrowAt always rolls to the next calendar day regardless of target time`() {
        val now = ZonedDateTime.of(LocalDate.of(2026, 9, 12), LocalTime.of(1, 0), zone)

        val fireTime = NextFireTime.tomorrowAt(hour = 23, minute = 0, zone = zone, now = now)

        assertEquals(LocalDate.of(2026, 9, 13), fireTime.toLocalDate())
        assertEquals(LocalTime.of(23, 0), fireTime.toLocalTime())
    }

    @Test
    fun `tomorrowDelayMillis is always positive`() {
        val now = ZonedDateTime.of(LocalDate.of(2026, 9, 12), LocalTime.of(23, 59), zone)

        val delay = NextFireTime.tomorrowDelayMillis(hour = 0, minute = 0, zone = zone, now = now)

        assert(delay > 0)
    }

    @Test
    fun `handles daylight saving time spring-forward gap`() {
        val now = ZonedDateTime.of(LocalDate.of(2027, 3, 28), LocalTime.of(1, 0), zone)

        val fireTime = NextFireTime.at(hour = 2, minute = 30, zone = zone, now = now)

        assertEquals(LocalDate.of(2027, 3, 28), fireTime.toLocalDate())
    }
}
