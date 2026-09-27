package com.wafflehq.lib.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

data class RadarSeries(val values: List<Int>, val color: Color, val filled: Boolean, val dashed: Boolean = false)

@Composable
fun RadarChart(
    labels: List<String>,
    series: List<RadarSeries>,
    maxValue: Int,
    gridColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true
) {
    val density = LocalDensity.current
    val labelSizePx = with(density) { 11.sp.toPx() }
    val strokePx = with(density) { 2.dp.toPx() }
    val labelPaddingPx = with(density) { 18.dp.toPx() }

    Canvas(modifier = modifier.aspectRatio(1f)) {
        val count = labels.size
        if (count < 3) return@Canvas
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = min(size.width, size.height) / 2f - (if (showLabels) labelPaddingPx * 2.2f else strokePx * 2)
        if (radius <= 0f) return@Canvas

        for (ring in 1..RINGS) {
            val fraction = ring.toFloat() / RINGS
            drawPolygonPath(
                (0 until count).map { RadarGeometry.pointFor(it, count, fraction, centerX, centerY, radius) },
                gridColor, strokePx / 2f, fill = false
            )
        }
        for (i in 0 until count) {
            val p = RadarGeometry.pointFor(i, count, 1f, centerX, centerY, radius)
            drawLine(gridColor, Offset(centerX, centerY), Offset(p.x, p.y), strokeWidth = strokePx / 2f)
        }

        series.forEach { s ->
            val points = RadarGeometry.polygon(s.values, maxValue, centerX, centerY, radius)
            if (points.size != count) return@forEach
            if (s.filled) drawPolygonPath(points, s.color.copy(alpha = 0.25f), 0f, fill = true)
            drawPolygonPath(points, s.color, strokePx, fill = false, dashed = s.dashed)
            points.forEach { drawCircle(s.color, radius = strokePx * 1.5f, center = Offset(it.x, it.y)) }
        }

        if (showLabels) {
            val paint = android.graphics.Paint().apply {
                color = labelColor.toArgb()
                textSize = labelSizePx
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            labels.forEachIndexed { i, label ->
                val p = RadarGeometry.pointFor(i, count, 1f, centerX, centerY, radius + labelPaddingPx)
                drawContext.canvas.nativeCanvas.drawText(label, p.x, p.y + labelSizePx / 3f, paint)
            }
        }
    }
}

private fun DrawScope.drawPolygonPath(points: List<RadarPoint>, color: Color, strokeWidth: Float, fill: Boolean, dashed: Boolean = false) {
    if (points.isEmpty()) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
    if (fill) {
        drawPath(path, color)
    } else {
        val effect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(strokeWidth * 4, strokeWidth * 3)) else null
        drawPath(path, color, style = Stroke(width = strokeWidth, pathEffect = effect))
    }
}

private const val RINGS = 5
