package com.wafflehq.lib.pdf.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun PdfZoomContainer(
    state: PdfZoomState,
    onScrollBy: (Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val currentOnScrollBy by rememberUpdatedState(onScrollBy)
    Layout(
        content = content,
        modifier = modifier
            .clipToBounds()
            .pointerInput(state) {
                detectTapGestures(
                    onDoubleTap = { focus ->
                        val scrollDelta = state.toggleZoom(focus, size.width.toFloat())
                        if (scrollDelta != 0f) currentOnScrollBy(scrollDelta)
                    }
                )
            }
            .pointerInput(state) {
                detectPdfTransformGestures(state) { currentOnScrollBy(it) }
            }
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val contentHeight = (height / state.zoom).roundToInt().coerceAtLeast(1)
        val placeables = measurables.map { it.measure(Constraints.fixed(width, contentHeight)) }
        layout(width, height) {
            placeables.forEach { placeable ->
                placeable.placeWithLayer(0, 0) {
                    transformOrigin = TransformOrigin(0f, 0f)
                    scaleX = state.zoom
                    scaleY = state.zoom
                    translationX = state.offsetX
                }
            }
        }
    }
}

private suspend fun PointerInputScope.detectPdfTransformGestures(
    state: PdfZoomState,
    onScrollBy: (Float) -> Unit
) {
    val touchSlop = viewConfiguration.touchSlop
    awaitEachGesture {
        var zoomAccumulated = 1f
        var panAccumulated = Offset.Zero
        var pinchStarted = false
        var dragAccumulated = 0f
        var dragStarted = false
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val pressedCount = event.changes.count { it.pressed }
            val viewportWidth = size.width.toFloat()
            if (pressedCount >= 2) {
                val zoomChange = event.calculateZoom()
                val pan = event.calculatePan()
                if (!pinchStarted) {
                    zoomAccumulated *= zoomChange
                    panAccumulated += pan
                    val zoomMotion = abs(1f - zoomAccumulated) * event.calculateCentroidSize(useCurrent = false)
                    pinchStarted = zoomMotion > touchSlop || panAccumulated.getDistance() > touchSlop
                }
                if (pinchStarted) {
                    val scrollDelta = state.transform(
                        centroid = event.calculateCentroid(useCurrent = false),
                        pan = pan,
                        zoomChange = zoomChange,
                        viewportWidth = viewportWidth
                    )
                    if (scrollDelta != 0f) onScrollBy(scrollDelta)
                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                }
            } else if (pressedCount == 1 && state.isZoomed) {
                val deltaX = event.calculatePan().x
                dragAccumulated += deltaX
                if (dragStarted) {
                    state.panHorizontally(deltaX, viewportWidth)
                } else if (abs(dragAccumulated) > touchSlop) {
                    dragStarted = true
                    state.panHorizontally(dragAccumulated, viewportWidth)
                }
            }
        } while (event.changes.any { it.pressed })
    }
}
