package com.wafflehq.uikit.astronomy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MoonPhaseCalculatorTest {

    @Test
    fun `reference date 2000-01-06 is new moon`() {
        assertEquals(MoonPhase.NEW_MOON, MoonPhaseCalculator.getMajorPhase(LocalDate.of(2000, 1, 6)))
    }

    @Test
    fun `2024-01-11 is new moon`() {
        assertEquals(MoonPhase.NEW_MOON, MoonPhaseCalculator.getMajorPhase(LocalDate.of(2024, 1, 11)))
    }

    @Test
    fun `2024-01-18 is first quarter`() {
        assertEquals(MoonPhase.FIRST_QUARTER, MoonPhaseCalculator.getMajorPhase(LocalDate.of(2024, 1, 18)))
    }

    @Test
    fun `2024-01-25 is full moon`() {
        assertEquals(MoonPhase.FULL_MOON, MoonPhaseCalculator.getMajorPhase(LocalDate.of(2024, 1, 25)))
    }

    @Test
    fun `2024-02-02 is last quarter`() {
        assertEquals(MoonPhase.LAST_QUARTER, MoonPhaseCalculator.getMajorPhase(LocalDate.of(2024, 2, 2)))
    }

    @Test
    fun `2024-02-09 is new moon`() {
        assertEquals(MoonPhase.NEW_MOON, MoonPhaseCalculator.getMajorPhase(LocalDate.of(2024, 2, 9)))
    }

    @Test
    fun `random mid-cycle days have no major phase`() {
        assertNull(MoonPhaseCalculator.getMajorPhase(LocalDate.of(2024, 1, 14)))
        assertNull(MoonPhaseCalculator.getMajorPhase(LocalDate.of(2024, 1, 21)))
    }

    @Test
    fun `each major phase appears at most once per synodic month`() {
        val from = LocalDate.of(2024, 1, 11)
        val to = LocalDate.of(2024, 2, 7)
        val phases = MoonPhaseCalculator.getPhaseForRange(from, to)
        val grouped = phases.values.groupBy { it }
        grouped.forEach { (phase, list) ->
            assertEquals("Phase $phase should appear at most once", 1, list.size)
        }
    }

    @Test
    fun `all four phases appear within one synodic month`() {
        val from = LocalDate.of(2024, 1, 11)
        val to = LocalDate.of(2024, 2, 7)
        val phases = MoonPhaseCalculator.getPhaseForRange(from, to)
        assertTrue(MoonPhase.NEW_MOON in phases.values)
        assertTrue(MoonPhase.FIRST_QUARTER in phases.values)
        assertTrue(MoonPhase.FULL_MOON in phases.values)
        assertTrue(MoonPhase.LAST_QUARTER in phases.values)
    }

    @Test
    fun `continuous phase is 0 for reference date`() {
        assertEquals(0.0, MoonPhaseCalculator.continuousPhase(LocalDate.of(2000, 1, 6)), 0.001)
    }

    @Test
    fun `continuous phase increases by correct daily step`() {
        val date = LocalDate.of(2024, 3, 1)
        val phase1 = MoonPhaseCalculator.continuousPhase(date)
        val phase2 = MoonPhaseCalculator.continuousPhase(date.plusDays(1))
        val expectedStep = 1.0 / 29.530588853
        val diff = (phase2 - phase1 + 1.0).mod(1.0)
        assertEquals(expectedStep, diff, 0.0001)
    }

    @Test
    fun `getPhaseForRange for single new moon day returns it`() {
        val date = LocalDate.of(2024, 1, 11)
        val phases = MoonPhaseCalculator.getPhaseForRange(date, date)
        assertEquals(1, phases.size)
        assertEquals(MoonPhase.NEW_MOON, phases[date])
    }

    @Test
    fun `getPhaseForRange for single non-phase day returns empty`() {
        val phases = MoonPhaseCalculator.getPhaseForRange(
            LocalDate.of(2024, 1, 14), LocalDate.of(2024, 1, 14)
        )
        assertTrue(phases.isEmpty())
    }

    @Test
    fun `phases near year boundary are detected`() {
        val from = LocalDate.of(2023, 12, 20)
        val to = LocalDate.of(2024, 1, 20)
        val phases = MoonPhaseCalculator.getPhaseForRange(from, to)
        assertTrue("Should find at least one phase", phases.isNotEmpty())
    }

    @Test
    fun `phases are found in multiple consecutive months`() {
        val from = LocalDate.of(2024, 1, 1)
        val to = LocalDate.of(2024, 6, 30)
        val phases = MoonPhaseCalculator.getPhaseForRange(from, to)
        assertTrue("Should find at least 20 phases in 6 months", phases.size >= 20)
    }

    @Test
    fun `no two major phases of same type within 20 days of each other`() {
        val from = LocalDate.of(2024, 1, 1)
        val to = LocalDate.of(2024, 12, 31)
        val phases = MoonPhaseCalculator.getPhaseForRange(from, to)
        val byType = phases.entries.groupBy { it.value }
        byType.forEach { (type, entries) ->
            val dates = entries.map { it.key }.sorted()
            for (i in 1 until dates.size) {
                val gap = java.time.temporal.ChronoUnit.DAYS.between(dates[i - 1], dates[i])
                assertTrue("Two $type phases should be at least 20 days apart, got $gap", gap >= 20)
            }
        }
    }

    @Test
    fun `getPhaseForRange returns empty for empty range`() {
        val date = LocalDate.of(2024, 1, 14)
        val phases = MoonPhaseCalculator.getPhaseForRange(date.plusDays(1), date)
        assertTrue(phases.isEmpty())
    }

    @Test
    fun `nearestPhase matches getMajorPhase on exact major-phase days`() {
        val date = LocalDate.of(2024, 1, 25)
        assertEquals(MoonPhase.FULL_MOON, MoonPhaseCalculator.getMajorPhase(date))
        assertEquals(MoonPhase.FULL_MOON, MoonPhaseCalculator.nearestPhase(date))
    }

    @Test
    fun `nearestPhase never returns null for mid-cycle days`() {
        assertNotNull(MoonPhaseCalculator.nearestPhase(LocalDate.of(2024, 1, 14)))
        assertNotNull(MoonPhaseCalculator.nearestPhase(LocalDate.of(2024, 1, 21)))
    }

    @Test
    fun `nearestPhase is stable for every day across a full year`() {
        var d = LocalDate.of(2024, 1, 1)
        val end = LocalDate.of(2024, 12, 31)
        while (!d.isAfter(end)) {
            assertNotNull(MoonPhaseCalculator.nearestPhase(d))
            d = d.plusDays(1)
        }
    }
}
