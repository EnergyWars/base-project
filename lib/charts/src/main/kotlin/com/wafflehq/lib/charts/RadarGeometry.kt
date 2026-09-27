package com.wafflehq.lib.charts

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class RadarPoint(val x: Float, val y: Float)

object RadarGeometry {

    fun angleFor(index: Int, count: Int): Double =
        -PI / 2 + 2 * PI * index / count

    fun pointFor(index: Int, count: Int, fraction: Float, centerX: Float, centerY: Float, radius: Float): RadarPoint {
        val angle = angleFor(index, count)
        val r = radius * fraction.coerceIn(0f, 1f)
        return RadarPoint((centerX + r * cos(angle)).toFloat(), (centerY + r * sin(angle)).toFloat())
    }

    fun polygon(values: List<Int>, maxValue: Int, centerX: Float, centerY: Float, radius: Float): List<RadarPoint> {
        if (values.isEmpty() || maxValue <= 0) return emptyList()
        return values.mapIndexed { index, value ->
            pointFor(index, values.size, value.toFloat() / maxValue, centerX, centerY, radius)
        }
    }
}
