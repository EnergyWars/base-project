package com.wafflehq.lib.charts

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InteractiveLineChartTest {

    @Test
    fun `LineChartSeries defaults to a connected line`() {
        val series = LineChartSeries(label = "A", color = Color.Red, values = listOf(1f, 2f))

        assertEquals(LinePointStyle.CONNECTED, series.pointStyle)
    }

    @Test
    fun `LineChartSeries can opt into marker-only rendering`() {
        val series = LineChartSeries(
            label = "As needed",
            color = Color.Blue,
            values = listOf(0f, null),
            pointStyle = LinePointStyle.MARKER_ONLY
        )

        assertEquals(LinePointStyle.MARKER_ONLY, series.pointStyle)
    }

    @Test
    fun `pickAxisStep chooses the finest step that still leaves room for labels`() {
        val step = pickAxisStep(
            maxAbsValue = 45f,
            availableHalfHeightPx = 100f,
            minLabelSpacingPx = 20f,
            candidates = listOf(5f, 10f, 15f, 30f, 60f)
        )

        assertEquals(15f, step)
    }

    @Test
    fun `pickAxisStep falls back to the coarsest candidate when nothing fits`() {
        val step = pickAxisStep(
            maxAbsValue = 1000f,
            availableHalfHeightPx = 10f,
            minLabelSpacingPx = 50f,
            candidates = listOf(5f, 10f, 60f, 1440f)
        )

        assertEquals(1440f, step)
    }

    @Test
    fun `pickAxisStep returns clamped max value when candidates are empty`() {
        val step = pickAxisStep(
            maxAbsValue = 0.2f,
            availableHalfHeightPx = 100f,
            minLabelSpacingPx = 20f,
            candidates = emptyList()
        )

        assertEquals(1f, step)
    }

    @Test
    fun `pickAxisStep ignores non-positive candidates`() {
        val step = pickAxisStep(
            maxAbsValue = 45f,
            availableHalfHeightPx = 100f,
            minLabelSpacingPx = 20f,
            candidates = listOf(0f, -5f, 15f)
        )

        assertEquals(15f, step)
    }

    @Test
    fun `axisTickValues is symmetric around zero`() {
        val ticks = axisTickValues(maxAbsValue = 45f, step = 15f)

        assertEquals(listOf(-45f, -30f, -15f, 0f, 15f, 30f, 45f), ticks)
    }

    @Test
    fun `axisTickValues only contains zero when the range is smaller than the step`() {
        val ticks = axisTickValues(maxAbsValue = 3f, step = 15f)

        assertEquals(listOf(0f), ticks)
    }

    @Test
    fun `axisTickValues returns only zero for a non-positive step`() {
        val ticks = axisTickValues(maxAbsValue = 45f, step = 0f)

        assertEquals(listOf(0f), ticks)
    }

    @Test
    fun `axisTickValues includes a tick exactly at the max value`() {
        val ticks = axisTickValues(maxAbsValue = 30f, step = 10f)

        assertTrue(ticks.contains(30f))
        assertTrue(ticks.contains(-30f))
    }
}
