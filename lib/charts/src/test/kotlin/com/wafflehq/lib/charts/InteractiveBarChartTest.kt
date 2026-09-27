package com.wafflehq.lib.charts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InteractiveBarChartTest {

    @Test
    fun `bar width always leaves a positive gap for wide slots`() {
        val slotW = 400f
        val gapPx = 20f

        val barW = barWidthForSlot(slotW, gapPx)

        assertEquals(slotW - gapPx, barW, 0.001f)
        assertTrue(slotW - barW > 0f)
    }

    @Test
    fun `bar width leaves a proportional gap when the fixed gap would exceed the slot`() {
        val slotW = 40f
        val gapPx = 100f

        val barW = barWidthForSlot(slotW, gapPx)

        assertEquals(slotW * 0.7f, barW, 0.001f)
        assertTrue(slotW - barW > 0f)
    }

    @Test
    fun `bar width never collapses to zero or negative for a tiny slot`() {
        val barW = barWidthForSlot(slotW = 1f, gapPx = 20f)

        assertTrue(barW >= 1f)
    }

    @Test
    fun `bar width never exceeds the slot width`() {
        val barW = barWidthForSlot(slotW = 40f, gapPx = 6f)

        assertTrue(barW < 40f)
    }
}
