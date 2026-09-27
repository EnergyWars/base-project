package com.wafflehq.lib.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ChartSegment(val value: Float, val color: Color)

data class ChartPoint(val segments: List<ChartSegment>) {
    val total: Float get() = segments.sumOf { it.value.toDouble() }.toFloat()
}

private val CHART_PAD_TOP: Dp = 20.dp
private val CHART_PAD_BOTTOM: Dp = 18.dp
private val BAR_GAP: Dp = 6.dp
private const val AXIS_TICKS = 4

internal fun barWidthForSlot(slotW: Float, gapPx: Float): Float {
    val gap = minOf(gapPx, slotW * 0.3f)
    return (slotW - gap).coerceAtLeast(1f)
}

@Composable
fun InteractiveStackedBarChart(
    points: List<ChartPoint>,
    pointLabel: (Int) -> String,
    tooltipLines: (Int) -> List<String>,
    modifier: Modifier = Modifier,
    pointSpacing: Dp = 28.dp,
    interactive: Boolean = true,
    initialFitPoints: Int? = null,
    zoomState: ChartZoomState = remember(points) { ChartZoomState() },
) {
    val axisColor = MaterialTheme.colorScheme.outline
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tooltipBg = MaterialTheme.colorScheme.surfaceVariant
    val tooltipText = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall
    val tooltipStyle = MaterialTheme.typography.labelMedium
    val textMeasurer = rememberTextMeasurer()

    var tooltipAnchor by remember(points) { mutableStateOf<ChartTooltipAnchor?>(null) }

    val gestureModifier = if (interactive) {
        Modifier.fillMaxSize().chartZoomGestures(
            key = points,
            state = zoomState,
            inset = 0.dp,
            minContentWidth = pointSpacing * points.size,
            onGesture = { tooltipAnchor = null },
            onTap = { offset ->
                val count = points.size
                if (count > 0) {
                    val referenceW = (pointSpacing * count).toPx().coerceAtLeast(1f)
                    val slotW = referenceW * zoomState.zoom / count
                    val idx = ((offset.x - zoomState.pan) / slotW).toInt().coerceIn(0, count - 1)
                    val total = points.getOrNull(idx)?.total ?: 0f
                    var hit = false
                    var localX = 0f
                    var avoidTop = 0f
                    var avoidBottom = 0f
                    if (total > 0f) {
                        val barW = barWidthForSlot(slotW, BAR_GAP.toPx())
                        val barX = idx * slotW + (slotW - barW) / 2f + zoomState.pan
                        val padTop = CHART_PAD_TOP.toPx()
                        val padBottom = CHART_PAD_BOTTOM.toPx()
                        val chartH = size.height - padBottom - padTop
                        val safeMax = zoomState.visibleMax.takeIf { it > 0f } ?: total
                        val barH = chartH * (total / safeMax)
                        val barTop = padTop + chartH - barH
                        val barBottom = padTop + chartH
                        if (offset.x in barX..(barX + barW) && offset.y in barTop..barBottom) {
                            hit = true
                            localX = barX + barW / 2f
                            avoidTop = barTop
                            avoidBottom = barBottom
                        }
                    }
                    tooltipAnchor = if (hit) {
                        if (tooltipAnchor?.index == idx) null else ChartTooltipAnchor(idx, localX, avoidTop, avoidBottom)
                    } else {
                        null
                    }
                }
            }
        )
    } else {
        Modifier.fillMaxSize()
    }

    Box(modifier = modifier) {
        Canvas(modifier = gestureModifier) {
            val count = points.size
            if (count == 0) return@Canvas
            val globalMax = (points.maxOfOrNull { it.total } ?: 0f).coerceAtLeast(1f)
            val padTop = CHART_PAD_TOP.toPx()
            val padBottom = CHART_PAD_BOTTOM.toPx()
            val chartH = size.height - padBottom - padTop
            val referenceW = if (interactive) (pointSpacing * count).toPx() else size.width
            if (interactive && referenceW > 0f) {
                val minFit = size.width / referenceW
                val initialFit = initialFitPoints?.takeIf { it > 0 }?.let {
                    size.width / (pointSpacing.toPx() * it)
                } ?: minFit
                zoomState.applyZoomBounds(minFit, initialFit)
            }
            val effectiveW = referenceW * zoomState.zoom
            val maxPan = (effectiveW - size.width).coerceAtLeast(0f)
            val panX = if (interactive) zoomState.pan.coerceIn(-maxPan, 0f) else 0f
            val slotW = effectiveW / count
            val safeMax = if (interactive) {
                val startIdx = ((-panX) / slotW).toInt().coerceIn(0, count - 1)
                val endIdx = ((-panX + size.width) / slotW).toInt().coerceIn(0, count - 1)
                val localMax = (startIdx..endIdx).maxOf { points[it].total }
                val effectiveMax = if (localMax > 0f) localMax else globalMax
                if (zoomState.visibleMax != effectiveMax) zoomState.visibleMax = effectiveMax
                effectiveMax
            } else globalMax
            val barW = barWidthForSlot(slotW, BAR_GAP.toPx())

            for (t in 0..AXIS_TICKS) {
                val y = padTop + chartH - chartH * t / AXIS_TICKS
                drawLine(
                    color = axisColor.copy(alpha = if (t == 0) 0.5f else 0.15f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
                for (i in 0 until count) {
                    val point = points[i]
                    if (point.total <= 0f) continue
                    val x = i * slotW + (slotW - barW) / 2f + panX
                    if (x + barW < 0f || x > size.width) continue
                    var stackTop = padTop + chartH
                    point.segments.forEach { segment ->
                        if (segment.value <= 0f) return@forEach
                        val segmentH = chartH * (segment.value / safeMax)
                        val color = if (i == tooltipAnchor?.index) segment.color.copy(alpha = 0.75f) else segment.color
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x, stackTop - segmentH),
                            size = Size(barW, segmentH),
                            cornerRadius = CornerRadius(barW / 3f, barW / 3f)
                        )
                        stackTop -= segmentH
                    }
                }
            }

            val axisY = padTop + chartH + 2.dp.toPx()
            var lastLabelRight = -Float.MAX_VALUE
            val labelGap = 8.dp.toPx()
            for (i in 0 until count) {
                val x = i * slotW + slotW / 2f + panX
                if (x < 0f || x > size.width) continue
                val label = pointLabel(i)
                val measured = textMeasurer.measure(label, style = labelStyle)
                val left = (x - measured.size.width / 2f).coerceIn(0f, size.width - measured.size.width)
                if (left < lastLabelRight + labelGap) continue
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    style = labelStyle.copy(color = labelColor),
                    topLeft = Offset(left, axisY)
                )
                lastLabelRight = left + measured.size.width
            }
        }

        tooltipAnchor?.let { anchor ->
            val label = tooltipLines(anchor.index).joinToString("\n")
            if (label.isNotBlank()) {
                ChartTooltipPopup(
                    anchor = anchor,
                    text = label,
                    backgroundColor = tooltipBg,
                    contentColor = tooltipText,
                    style = tooltipStyle,
                    onDismiss = { tooltipAnchor = null }
                )
            }
        }
    }
}

@Composable
fun InteractiveCenteredBarChart(
    values: List<Float>,
    pointLabel: (Int) -> String,
    tooltipLines: (Int) -> List<String>,
    positiveColor: Color,
    negativeColor: Color,
    modifier: Modifier = Modifier,
    pointSpacing: Dp = 28.dp,
    interactive: Boolean = true,
    initialFitPoints: Int? = null,
    zoomState: ChartZoomState = remember(values) { ChartZoomState() },
) {
    val axisColor = MaterialTheme.colorScheme.outline
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tooltipBg = MaterialTheme.colorScheme.surfaceVariant
    val tooltipText = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall
    val tooltipStyle = MaterialTheme.typography.labelMedium
    val textMeasurer = rememberTextMeasurer()

    var tooltipAnchor by remember(values) { mutableStateOf<ChartTooltipAnchor?>(null) }

    val gestureModifier = if (interactive) {
        Modifier.fillMaxSize().chartZoomGestures(
            key = values,
            state = zoomState,
            inset = 0.dp,
            minContentWidth = pointSpacing * values.size,
            onGesture = { tooltipAnchor = null },
            onTap = { offset ->
                val count = values.size
                if (count > 0) {
                    val referenceW = (pointSpacing * count).toPx().coerceAtLeast(1f)
                    val slotW = referenceW * zoomState.zoom / count
                    val idx = ((offset.x - zoomState.pan) / slotW).toInt().coerceIn(0, count - 1)
                    val value = values.getOrElse(idx) { 0f }
                    var hit = false
                    var localX = 0f
                    var avoidTop = 0f
                    var avoidBottom = 0f
                    if (value != 0f) {
                        val barW = barWidthForSlot(slotW, BAR_GAP.toPx())
                        val barX = idx * slotW + (slotW - barW) / 2f + zoomState.pan
                        val padTop = CHART_PAD_TOP.toPx()
                        val padBottom = CHART_PAD_BOTTOM.toPx()
                        val chartH = size.height - padBottom - padTop
                        val midY = padTop + chartH / 2f
                        val halfH = chartH / 2f
                        val safeMaxAbs = zoomState.visibleMax.takeIf { it > 0f } ?: kotlin.math.abs(value).coerceAtLeast(1f)
                        val barH = halfH * (kotlin.math.abs(value) / safeMaxAbs)
                        val barTop = if (value > 0f) midY - barH else midY
                        val barBottom = if (value > 0f) midY else midY + barH
                        if (offset.x in barX..(barX + barW) && offset.y in barTop..barBottom) {
                            hit = true
                            localX = barX + barW / 2f
                            avoidTop = barTop
                            avoidBottom = barBottom
                        }
                    }
                    tooltipAnchor = if (hit) {
                        if (tooltipAnchor?.index == idx) null else ChartTooltipAnchor(idx, localX, avoidTop, avoidBottom)
                    } else {
                        null
                    }
                }
            }
        )
    } else {
        Modifier.fillMaxSize()
    }

    Box(modifier = modifier) {
        Canvas(modifier = gestureModifier) {
            val count = values.size
            if (count == 0) return@Canvas
            val globalMaxAbs = (values.maxOfOrNull { kotlin.math.abs(it) } ?: 0f).coerceAtLeast(1f)
            val padTop = CHART_PAD_TOP.toPx()
            val padBottom = CHART_PAD_BOTTOM.toPx()
            val chartH = size.height - padBottom - padTop
            val midY = padTop + chartH / 2f
            val referenceW = if (interactive) (pointSpacing * count).toPx() else size.width
            if (interactive && referenceW > 0f) {
                val minFit = size.width / referenceW
                val initialFit = initialFitPoints?.takeIf { it > 0 }?.let {
                    size.width / (pointSpacing.toPx() * it)
                } ?: minFit
                zoomState.applyZoomBounds(minFit, initialFit)
            }
            val effectiveW = referenceW * zoomState.zoom
            val maxPan = (effectiveW - size.width).coerceAtLeast(0f)
            val panX = if (interactive) zoomState.pan.coerceIn(-maxPan, 0f) else 0f
            val slotW = effectiveW / count
            val safeMaxAbs = if (interactive) {
                val startIdx = ((-panX) / slotW).toInt().coerceIn(0, count - 1)
                val endIdx = ((-panX + size.width) / slotW).toInt().coerceIn(0, count - 1)
                val localMax = (startIdx..endIdx).maxOf { kotlin.math.abs(values[it]) }
                val effectiveMax = if (localMax > 0f) localMax else globalMaxAbs
                if (zoomState.visibleMax != effectiveMax) zoomState.visibleMax = effectiveMax
                effectiveMax
            } else globalMaxAbs
            val barW = barWidthForSlot(slotW, BAR_GAP.toPx())
            val halfH = chartH / 2f

            drawLine(
                color = axisColor.copy(alpha = 0.5f),
                start = Offset(0f, midY),
                end = Offset(size.width, midY),
                strokeWidth = 1.dp.toPx()
            )

            clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
                for (i in 0 until count) {
                    val value = values[i]
                    if (value == 0f) continue
                    val x = i * slotW + (slotW - barW) / 2f + panX
                    if (x + barW < 0f || x > size.width) continue
                    val barH = halfH * (kotlin.math.abs(value) / safeMaxAbs)
                    val baseColor = if (value > 0f) positiveColor else negativeColor
                    val color = if (i == tooltipAnchor?.index) baseColor.copy(alpha = 0.75f) else baseColor
                    val top = if (value > 0f) midY - barH else midY
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, top),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(barW / 3f, barW / 3f)
                    )
                }
            }

            val axisY = padTop + chartH + 2.dp.toPx()
            var lastLabelRight = -Float.MAX_VALUE
            val labelGap = 8.dp.toPx()
            for (i in 0 until count) {
                val x = i * slotW + slotW / 2f + panX
                if (x < 0f || x > size.width) continue
                val label = pointLabel(i)
                val measured = textMeasurer.measure(label, style = labelStyle)
                val left = (x - measured.size.width / 2f).coerceIn(0f, size.width - measured.size.width)
                if (left < lastLabelRight + labelGap) continue
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    style = labelStyle.copy(color = labelColor),
                    topLeft = Offset(left, axisY)
                )
                lastLabelRight = left + measured.size.width
            }
        }

        tooltipAnchor?.let { anchor ->
            val label = tooltipLines(anchor.index).joinToString("\n")
            if (label.isNotBlank()) {
                ChartTooltipPopup(
                    anchor = anchor,
                    text = label,
                    backgroundColor = tooltipBg,
                    contentColor = tooltipText,
                    style = tooltipStyle,
                    onDismiss = { tooltipAnchor = null }
                )
            }
        }
    }
}
