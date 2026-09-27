package com.wafflehq.lib.pdf.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlin.math.ceil

@Stable
class PdfZoomState(
    val minZoom: Float = DEFAULT_MIN_ZOOM,
    val maxZoom: Float = DEFAULT_MAX_ZOOM,
    val doubleTapZoom: Float = DEFAULT_DOUBLE_TAP_ZOOM
) {
    init {
        require(minZoom >= 1f) { "minZoom must be at least 1" }
        require(maxZoom >= minZoom) { "maxZoom must not be below minZoom" }
        require(doubleTapZoom in minZoom..maxZoom) { "doubleTapZoom must lie within the zoom range" }
    }

    var zoom by mutableFloatStateOf(minZoom)
        private set

    var offsetX by mutableFloatStateOf(0f)
        private set

    val isZoomed: Boolean get() = zoom > minZoom + ZOOMED_EPSILON

    fun transform(centroid: Offset, pan: Offset, zoomChange: Float, viewportWidth: Float): Float =
        applyTransform(centroid, pan, (zoom * zoomChange).coerceIn(minZoom, maxZoom), viewportWidth)

    fun panHorizontally(deltaX: Float, viewportWidth: Float) {
        offsetX = clampOffset(offsetX + deltaX, zoom, viewportWidth)
    }

    fun toggleZoom(focus: Offset, viewportWidth: Float): Float {
        val target = if (isZoomed) minZoom else doubleTapZoom
        return applyTransform(focus, Offset.Zero, target, viewportWidth)
    }

    fun reset() {
        zoom = minZoom
        offsetX = 0f
    }

    private fun applyTransform(centroid: Offset, pan: Offset, newZoom: Float, viewportWidth: Float): Float {
        val oldZoom = zoom
        val contentX = (centroid.x - offsetX) / oldZoom
        zoom = newZoom
        offsetX = clampOffset(centroid.x + pan.x - newZoom * contentX, newZoom, viewportWidth)
        return centroid.y / oldZoom - (centroid.y + pan.y) / newZoom
    }

    private fun clampOffset(offset: Float, zoom: Float, viewportWidth: Float): Float =
        offset.coerceIn(viewportWidth * (1f - zoom), 0f)

    companion object {
        const val DEFAULT_MIN_ZOOM = 1f
        const val DEFAULT_MAX_ZOOM = 5f
        const val DEFAULT_DOUBLE_TAP_ZOOM = 2.5f
        private const val ZOOMED_EPSILON = 0.01f
        private const val RENDER_ZOOM_STEP = 0.5f
        private const val RENDER_ZOOM_TOLERANCE = 0.001f

        fun renderZoomFor(zoom: Float): Float =
            ceil(zoom / RENDER_ZOOM_STEP - RENDER_ZOOM_TOLERANCE) * RENDER_ZOOM_STEP
    }
}
