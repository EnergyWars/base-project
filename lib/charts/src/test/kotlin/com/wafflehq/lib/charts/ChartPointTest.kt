package com.wafflehq.lib.charts

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ChartPointTest {

    @Test
    fun `total sums all segment values`() {
        val point = ChartPoint(
            segments = listOf(
                ChartSegment(3f, Color.Red),
                ChartSegment(2f, Color.Blue)
            )
        )

        assertEquals(5f, point.total)
    }

    @Test
    fun `total is zero for a point with no segments`() {
        val point = ChartPoint(segments = emptyList())

        assertEquals(0f, point.total)
    }
}
