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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class LinePointStyle { CONNECTED, MARKER_ONLY }

data class LineChartSeries(
    val label: String,
    val color: Color,
    val values: List<Float?>,
    val pointStyle: LinePointStyle = LinePointStyle.CONNECTED
)

private val LINE_CHART_PAD_TOP: Dp = 20.dp
private val LINE_CHART_PAD_BOTTOM: Dp = 18.dp
private val LINE_STROKE_WIDTH: Dp = 2.dp
private val LINE_DOT_RADIUS: Dp = 3.dp
private val LINE_DOT_RADIUS_SELECTED: Dp = 4.5.dp
private val LINE_DOT_RADIUS_MARKER: Dp = 6.dp
private val LINE_DOT_RADIUS_MARKER_SELECTED: Dp = 7.5.dp
private val LINE_DOT_HIT_RADIUS: Dp = 16.dp
private val LINE_AXIS_LABEL_GAP: Dp = 4.dp

internal fun pickAxisStep(
    maxAbsValue: Float,
    availableHalfHeightPx: Float,
    minLabelSpacingPx: Float,
    candidates: List<Float>
): Float {
    val sorted = candidates.filter { it > 0f }.sorted()
    if (sorted.isEmpty()) return maxAbsValue.coerceAtLeast(1f)
    for (step in sorted) {
        val ticks = kotlin.math.ceil(maxAbsValue / step).toInt().coerceAtLeast(1)
        val spacing = availableHalfHeightPx / ticks
        if (spacing > minLabelSpacingPx) return step
    }
    return sorted.last()
}

internal fun axisTickValues(maxAbsValue: Float, step: Float): List<Float> {
    if (step <= 0f) return listOf(0f)
    val positiveTicks = generateSequence(step) { it + step }
        .takeWhile { it <= maxAbsValue + step * 0.001f }
        .toList()
    return positiveTicks.map { -it }.reversed() + listOf(0f) + positiveTicks
}

@Composable
fun InteractiveMultiLineChart(
    series: List<LineChartSeries>,
    pointCount: Int,
    pointLabel: (Int) -> String,
    tooltipLines: (Int) -> List<String>,
    modifier: Modifier = Modifier,
    pointSpacing: Dp = 28.dp,
    interactive: Boolean = true,
    initialFitPoints: Int? = null,
    zoomState: ChartZoomState = remember(series) { ChartZoomState() },
    yAxisTickSteps: List<Float> = emptyList(),
    yAxisValueFormatter: (Float) -> String = { it.toInt().toString() },
) {
    val axisColor = MaterialTheme.colorScheme.outline
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tooltipBg = MaterialTheme.colorScheme.surfaceVariant
    val tooltipText = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall
    val tooltipStyle = MaterialTheme.typography.labelMedium
    val textMeasurer = rememberTextMeasurer()

    var tooltipAnchor by remember(series) { mutableStateOf<ChartTooltipAnchor?>(null) }

    val gestureModifier = if (interactive) {
        Modifier.fillMaxSize().chartZoomGestures(
            key = series,
            state = zoomState,
            inset = 0.dp,
            minContentWidth = pointSpacing * pointCount,
            onGesture = { tooltipAnchor = null },
            onTap = { offset ->
                if (pointCount > 0) {
                    val referenceW = (pointSpacing * pointCount).toPx().coerceAtLeast(1f)
                    val slotW = referenceW * zoomState.zoom / pointCount
                    val idx = ((offset.x - zoomState.plotOffsetX - zoomState.pan) / slotW).toInt()
                        .coerceIn(0, pointCount - 1)
                    val padTop = LINE_CHART_PAD_TOP.toPx()
                    val padBottom = LINE_CHART_PAD_BOTTOM.toPx()
                    val chartH = size.height - padBottom - padTop
                    val midY = padTop + chartH / 2f
                    val halfH = chartH / 2f
                    val safeMaxAbs = zoomState.visibleMax.takeIf { it > 0f } ?: 1f
                    val x = zoomState.plotOffsetX + idx * slotW + slotW / 2f + zoomState.pan
                    val hitRadiusPx = LINE_DOT_HIT_RADIUS.toPx()
                    val hit = series.any { s ->
                        val v = s.values.getOrNull(idx) ?: return@any false
                        val y = midY - halfH * (v / safeMaxAbs)
                        val dx = offset.x - x
                        val dy = offset.y - y
                        dx * dx + dy * dy <= hitRadiusPx * hitRadiusPx
                    }
                    tooltipAnchor = if (hit) {
                        if (tooltipAnchor?.index == idx) {
                            null
                        } else {
                            val ys = series.mapNotNull { s -> s.values.getOrNull(idx) }
                                .map { v -> midY - halfH * (v / safeMaxAbs) }
                            val markerRadius = LINE_DOT_RADIUS_MARKER_SELECTED.toPx()
                            val avoidTop = (ys.minOrNull() ?: midY) - markerRadius
                            val avoidBottom = (ys.maxOrNull() ?: midY) + markerRadius
                            ChartTooltipAnchor(idx, x, avoidTop, avoidBottom)
                        }
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
            val count = pointCount
            if (count == 0 || series.isEmpty()) return@Canvas
            val allValues = series.flatMap { it.values.filterNotNull() }
            val globalMaxAbs = (allValues.maxOfOrNull { kotlin.math.abs(it) } ?: 0f).coerceAtLeast(1f)
            val padTop = LINE_CHART_PAD_TOP.toPx()
            val padBottom = LINE_CHART_PAD_BOTTOM.toPx()
            val chartH = size.height - padBottom - padTop
            val midY = padTop + chartH / 2f
            val halfH = chartH / 2f

            val tickLabels: List<Pair<Float, String>> = if (yAxisTickSteps.isNotEmpty()) {
                val minLabelSpacingPx = textMeasurer.measure("0", style = labelStyle).size.height + 4.dp.toPx()
                val step = pickAxisStep(globalMaxAbs, halfH, minLabelSpacingPx, yAxisTickSteps)
                axisTickValues(globalMaxAbs, step).map { it to yAxisValueFormatter(it) }
            } else {
                emptyList()
            }
            val padLeft = if (tickLabels.isNotEmpty()) {
                val maxLabelWidth = tickLabels.maxOf { (_, label) -> textMeasurer.measure(label, style = labelStyle).size.width }
                maxLabelWidth + LINE_AXIS_LABEL_GAP.toPx()
            } else {
                0f
            }
            zoomState.plotOffsetX = padLeft
            val plotW = (size.width - padLeft).coerceAtLeast(1f)

            val referenceW = if (interactive) (pointSpacing * count).toPx() else plotW
            if (interactive && referenceW > 0f) {
                val minFit = plotW / referenceW
                val initialFit = initialFitPoints?.takeIf { it > 0 }?.let {
                    plotW / (pointSpacing.toPx() * it)
                } ?: minFit
                zoomState.applyZoomBounds(minFit, initialFit)
            }
            val effectiveW = referenceW * zoomState.zoom
            val maxPan = (effectiveW - plotW).coerceAtLeast(0f)
            val panX = if (interactive) zoomState.pan.coerceIn(-maxPan, 0f) else 0f
            val slotW = effectiveW / count
            val safeMaxAbs = if (tickLabels.isNotEmpty()) {
                if (zoomState.visibleMax != globalMaxAbs) zoomState.visibleMax = globalMaxAbs
                globalMaxAbs
            } else if (interactive) {
                val startIdx = ((-panX) / slotW).toInt().coerceIn(0, count - 1)
                val endIdx = ((-panX + plotW) / slotW).toInt().coerceIn(0, count - 1)
                val localValues = (startIdx..endIdx).flatMap { i -> series.mapNotNull { s -> s.values.getOrNull(i) } }
                val localMax = localValues.maxOfOrNull { kotlin.math.abs(it) } ?: 0f
                val effectiveMax = if (localMax > 0f) localMax else globalMaxAbs
                if (zoomState.visibleMax != effectiveMax) zoomState.visibleMax = effectiveMax
                effectiveMax
            } else globalMaxAbs

            fun xFor(i: Int) = padLeft + i * slotW + slotW / 2f + panX
            fun yFor(v: Float) = midY - halfH * (v / safeMaxAbs)

            if (tickLabels.isNotEmpty()) {
                tickLabels.forEach { (value, label) ->
                    val y = midY - halfH * (value / safeMaxAbs)
                    drawLine(
                        color = axisColor.copy(alpha = if (value == 0f) 0.5f else 0.15f),
                        start = Offset(padLeft, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    val measured = textMeasurer.measure(label, style = labelStyle)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = label,
                        style = labelStyle.copy(color = labelColor),
                        topLeft = Offset(padLeft - LINE_AXIS_LABEL_GAP.toPx() - measured.size.width, y - measured.size.height / 2f)
                    )
                }
            } else {
                drawLine(
                    color = axisColor.copy(alpha = 0.5f),
                    start = Offset(0f, midY),
                    end = Offset(size.width, midY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            clipRect(left = padLeft, top = 0f, right = size.width, bottom = size.height) {
                series.forEach { s ->
                    if (s.pointStyle == LinePointStyle.CONNECTED) {
                        var prevX: Float? = null
                        var prevY: Float? = null
                        for (i in 0 until count) {
                            val v = s.values.getOrNull(i)
                            if (v == null) {
                                prevX = null
                                prevY = null
                                continue
                            }
                            val x = xFor(i)
                            val y = yFor(v)
                            val px = prevX
                            val py = prevY
                            if (px != null && py != null) {
                                drawLine(
                                    color = s.color,
                                    start = Offset(px, py),
                                    end = Offset(x, y),
                                    strokeWidth = LINE_STROKE_WIDTH.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                            prevX = x
                            prevY = y
                        }
                    }
                    val dotRadius = if (s.pointStyle == LinePointStyle.MARKER_ONLY) LINE_DOT_RADIUS_MARKER else LINE_DOT_RADIUS
                    val dotRadiusSelected = if (s.pointStyle == LinePointStyle.MARKER_ONLY) {
                        LINE_DOT_RADIUS_MARKER_SELECTED
                    } else {
                        LINE_DOT_RADIUS_SELECTED
                    }
                    for (i in 0 until count) {
                        val v = s.values.getOrNull(i) ?: continue
                        val x = xFor(i)
                        if (x < padLeft - dotRadiusSelected.toPx() || x > size.width + dotRadiusSelected.toPx()) continue
                        val y = yFor(v)
                        val radius = if (i == tooltipAnchor?.index) dotRadiusSelected.toPx() else dotRadius.toPx()
                        drawCircle(color = s.color, radius = radius, center = Offset(x, y))
                    }
                }
            }

            val axisY = padTop + chartH + 2.dp.toPx()
            var lastLabelRight = -Float.MAX_VALUE
            val labelGap = 8.dp.toPx()
            for (i in 0 until count) {
                val x = xFor(i)
                if (x < padLeft || x > size.width) continue
                val label = pointLabel(i)
                val measured = textMeasurer.measure(label, style = labelStyle)
                val left = (x - measured.size.width / 2f).coerceIn(padLeft, (size.width - measured.size.width).coerceAtLeast(padLeft))
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
