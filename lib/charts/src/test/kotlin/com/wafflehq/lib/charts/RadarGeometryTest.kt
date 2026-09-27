package com.wafflehq.lib.charts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class RadarGeometryTest {

    @Test
    fun firstPoint_pointsStraightUp() {
        val p = RadarGeometry.pointFor(0, 4, 1f, 100f, 100f, 50f)
        assertEquals(100f, p.x, 0.001f)
        assertEquals(50f, p.y, 0.001f)
    }

    @Test
    fun fourPoints_formCross() {
        val right = RadarGeometry.pointFor(1, 4, 1f, 100f, 100f, 50f)
        val bottom = RadarGeometry.pointFor(2, 4, 1f, 100f, 100f, 50f)
        val left = RadarGeometry.pointFor(3, 4, 1f, 100f, 100f, 50f)
        assertEquals(150f, right.x, 0.001f)
        assertEquals(100f, right.y, 0.001f)
        assertEquals(150f, bottom.y, 0.001f)
        assertEquals(50f, left.x, 0.001f)
    }

    @Test
    fun fraction_scalesRadius() {
        val half = RadarGeometry.pointFor(0, 3, 0.5f, 0f, 0f, 100f)
        assertEquals(-50f, half.y, 0.001f)
    }

    @Test
    fun fraction_isClamped() {
        val over = RadarGeometry.pointFor(0, 3, 2f, 0f, 0f, 100f)
        assertEquals(-100f, over.y, 0.001f)
    }

    @Test
    fun polygon_mapsValuesToMax() {
        val points = RadarGeometry.polygon(listOf(10, 5, 0), 10, 0f, 0f, 100f)
        assertEquals(3, points.size)
        assertEquals(-100f, points[0].y, 0.001f)
        assertTrue(abs(points[1].x) > 0f)
        assertEquals(0f, points[2].x, 0.001f)
        assertEquals(0f, points[2].y, 0.001f)
    }

    @Test
    fun polygon_emptyForNoValues() {
        assertTrue(RadarGeometry.polygon(emptyList(), 10, 0f, 0f, 100f).isEmpty())
        assertTrue(RadarGeometry.polygon(listOf(1), 0, 0f, 0f, 100f).isEmpty())
    }
}
