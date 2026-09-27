package com.wafflehq.lib.charts

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import kotlin.math.abs

const val CHART_ABSOLUTE_MIN_ZOOM = 0.1f
const val CHART_MAX_ZOOM = 5f

class ChartZoomState {
    var zoom by mutableFloatStateOf(1f)
    var pan by mutableFloatStateOf(0f)
    var minZoom by mutableFloatStateOf(1f)
    var visibleMax by mutableFloatStateOf(0f)
    var plotOffsetX by mutableFloatStateOf(0f)
    private var zoomInitialized = false

    fun applyZoomBounds(minZoomFit: Float, initialFit: Float) {
        val clampedMin = minZoomFit.coerceIn(CHART_ABSOLUTE_MIN_ZOOM, CHART_MAX_ZOOM)
        val clampedInitial = initialFit.coerceIn(clampedMin, CHART_MAX_ZOOM)
        minZoom = clampedMin
        zoom = if (!zoomInitialized) {
            zoomInitialized = true
            clampedInitial
        } else {
            zoom.coerceIn(clampedMin, CHART_MAX_ZOOM)
        }
    }

    fun applyFitZoom(fitZoom: Float) = applyZoomBounds(fitZoom, fitZoom)
}

fun Modifier.chartZoomGestures(
    key: Any?,
    state: ChartZoomState,
    inset: Dp,
    minContentWidth: Dp,
    onGesture: () -> Unit,
    onTap: AwaitPointerEventScope.(Offset) -> Unit
): Modifier = pointerInput(key) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var didMove = false
        val insetPx = inset.toPx()
        val visibleW = (size.width - insetPx * 2).coerceAtLeast(1f)
        val referenceW = minContentWidth.toPx().coerceAtLeast(1f)

        do {
            val event = awaitPointerEvent()
            val activePointers = event.changes.filter { it.pressed }

            if (activePointers.size >= 2) {
                val zoomFactor = event.calculateZoom()
                val panDelta = event.calculatePan()
                val centroid = event.calculateCentroid(useCurrent = true)
                val oldZoom = state.zoom
                val newZoom = (oldZoom * zoomFactor).coerceIn(state.minZoom, CHART_MAX_ZOOM)
                if (newZoom != oldZoom) {
                    val focalX = centroid.x - insetPx
                    val contentX = (focalX - state.pan) / oldZoom
                    state.zoom = newZoom
                    state.pan = focalX - contentX * newZoom
                }
                state.pan += panDelta.x
                val maxPan = (referenceW * state.zoom - visibleW).coerceAtLeast(0f)
                state.pan = state.pan.coerceIn(-maxPan, 0f)
                onGesture()
                didMove = true
                event.changes.forEach { it.consume() }
            } else if (activePointers.size == 1) {
                val p = activePointers.first()
                val drag = p.position - p.previousPosition
                val maxPan = (referenceW * state.zoom - visibleW).coerceAtLeast(0f)
                if (maxPan > 0f && (abs(drag.x) > 3f || abs(drag.y) > 3f)) {
                    state.pan = (state.pan + drag.x).coerceIn(-maxPan, 0f)
                    onGesture()
                    didMove = true
                    p.consume()
                }
            }
        } while (event.changes.any { it.pressed })

        if (!didMove) onTap(down.position)
    }
}
