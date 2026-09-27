package com.wafflehq.lib.uicore.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.math.ceil
import kotlin.math.floor

fun snapZoomUp(current: Float, step: Float, min: Float, max: Float): Float {
    val epsilon = step * 0.01f
    val steps = floor(current / step + epsilon) + 1f
    return (steps * step).coerceIn(min, max)
}

fun snapZoomDown(current: Float, step: Float, min: Float, max: Float): Float {
    val epsilon = step * 0.01f
    val steps = ceil(current / step - epsilon) - 1f
    return (steps * step).coerceIn(min, max)
}

suspend fun PointerInputScope.detectPinchZoom(
    onZoom: (centroid: Offset, zoomChange: Float) -> Unit,
    onGestureEnd: () -> Unit = {}
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var zoomed = false
        do {
            val event = awaitPointerEvent()
            if (event.changes.size > 1) {
                val zoomChange = event.calculateZoom()
                if (zoomChange != 1f) {
                    val centroid = event.calculateCentroid()
                    onZoom(centroid, zoomChange)
                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                    zoomed = true
                }
            }
        } while (event.changes.any { it.pressed })
        if (zoomed) onGestureEnd()
    }
}
