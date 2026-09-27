package com.wafflehq.lib.pdf.ui

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfZoomStateTest {

    private val viewportWidth = 1000f
    private val delta = 0.001f

    @Test
    fun `starts unzoomed and centered`() {
        val state = PdfZoomState()

        assertEquals(1f, state.zoom, delta)
        assertEquals(0f, state.offsetX, delta)
        assertFalse(state.isZoomed)
    }

    @Test
    fun `transform zooms around the centroid and keeps the content point under it`() {
        val state = PdfZoomState()

        state.transform(Offset(500f, 400f), Offset.Zero, 2f, viewportWidth)

        assertEquals(2f, state.zoom, delta)
        assertEquals(-500f, state.offsetX, delta)
        assertTrue(state.isZoomed)
    }

    @Test
    fun `transform keeps the content point stable across repeated zooming`() {
        val state = PdfZoomState()
        state.transform(Offset(500f, 0f), Offset.Zero, 2f, viewportWidth)
        val contentX = (600f - state.offsetX) / state.zoom

        state.transform(Offset(600f, 0f), Offset.Zero, 1.5f, viewportWidth)

        assertEquals(3f, state.zoom, delta)
        assertEquals(contentX, (600f - state.offsetX) / state.zoom, delta)
    }

    @Test
    fun `zooming at the left edge keeps the left edge in place`() {
        val state = PdfZoomState()

        state.transform(Offset(0f, 0f), Offset.Zero, 2f, viewportWidth)

        assertEquals(0f, state.offsetX, delta)
    }

    @Test
    fun `zooming at the right edge keeps the right edge in place`() {
        val state = PdfZoomState()

        state.transform(Offset(viewportWidth, 0f), Offset.Zero, 2f, viewportWidth)

        assertEquals(-viewportWidth, state.offsetX, delta)
    }

    @Test
    fun `transform returns the vertical scroll that keeps the content under the centroid`() {
        val state = PdfZoomState()

        val scrollDelta = state.transform(Offset(500f, 400f), Offset.Zero, 2f, viewportWidth)

        assertEquals(200f, scrollDelta, delta)
    }

    @Test
    fun `transform without zoom converts a downward pan into a backward scroll`() {
        val state = PdfZoomState()

        val scrollDelta = state.transform(Offset(0f, 0f), Offset(0f, 50f), 1f, viewportWidth)

        assertEquals(-50f, scrollDelta, delta)
    }

    @Test
    fun `transform divides the vertical pan by the zoom`() {
        val state = PdfZoomState()
        state.transform(Offset(0f, 0f), Offset.Zero, 2f, viewportWidth)

        val scrollDelta = state.transform(Offset(0f, 0f), Offset(0f, 100f), 1f, viewportWidth)

        assertEquals(-50f, scrollDelta, delta)
    }

    @Test
    fun `transform clamps the zoom to the maximum`() {
        val state = PdfZoomState()

        state.transform(Offset(500f, 0f), Offset.Zero, 100f, viewportWidth)

        assertEquals(PdfZoomState.DEFAULT_MAX_ZOOM, state.zoom, delta)
    }

    @Test
    fun `transform clamps the zoom to the minimum and recenters`() {
        val state = PdfZoomState()
        state.transform(Offset(500f, 0f), Offset.Zero, 2f, viewportWidth)

        state.transform(Offset(500f, 0f), Offset.Zero, 0.1f, viewportWidth)

        assertEquals(PdfZoomState.DEFAULT_MIN_ZOOM, state.zoom, delta)
        assertEquals(0f, state.offsetX, delta)
        assertFalse(state.isZoomed)
    }

    @Test
    fun `transform pans horizontally within the content bounds`() {
        val state = PdfZoomState()
        state.transform(Offset(500f, 0f), Offset.Zero, 2f, viewportWidth)

        state.transform(Offset(500f, 0f), Offset(-200f, 0f), 1f, viewportWidth)

        assertEquals(-700f, state.offsetX, delta)
    }

    @Test
    fun `panHorizontally never leaves the left edge`() {
        val state = PdfZoomState()
        state.transform(Offset(500f, 0f), Offset.Zero, 2f, viewportWidth)

        state.panHorizontally(900f, viewportWidth)

        assertEquals(0f, state.offsetX, delta)
    }

    @Test
    fun `panHorizontally never leaves the right edge`() {
        val state = PdfZoomState()
        state.transform(Offset(500f, 0f), Offset.Zero, 2f, viewportWidth)

        state.panHorizontally(-900f, viewportWidth)

        assertEquals(-viewportWidth, state.offsetX, delta)
    }

    @Test
    fun `panHorizontally is ignored while unzoomed`() {
        val state = PdfZoomState()

        state.panHorizontally(-300f, viewportWidth)

        assertEquals(0f, state.offsetX, delta)
    }

    @Test
    fun `toggleZoom zooms in to the double tap zoom`() {
        val state = PdfZoomState()

        val scrollDelta = state.toggleZoom(Offset(500f, 500f), viewportWidth)

        assertEquals(PdfZoomState.DEFAULT_DOUBLE_TAP_ZOOM, state.zoom, delta)
        assertEquals(300f, scrollDelta, delta)
    }

    @Test
    fun `toggleZoom zooms back out and restores the scroll position`() {
        val state = PdfZoomState()
        val zoomIn = state.toggleZoom(Offset(500f, 500f), viewportWidth)

        val zoomOut = state.toggleZoom(Offset(500f, 500f), viewportWidth)

        assertEquals(PdfZoomState.DEFAULT_MIN_ZOOM, state.zoom, delta)
        assertEquals(0f, state.offsetX, delta)
        assertEquals(0f, zoomIn + zoomOut, delta)
    }

    @Test
    fun `toggleZoom zooms out from any zoomed level`() {
        val state = PdfZoomState()
        state.transform(Offset(500f, 0f), Offset.Zero, 4f, viewportWidth)

        state.toggleZoom(Offset(500f, 0f), viewportWidth)

        assertFalse(state.isZoomed)
    }

    @Test
    fun `reset returns to the unzoomed state`() {
        val state = PdfZoomState()
        state.transform(Offset(700f, 0f), Offset.Zero, 3f, viewportWidth)

        state.reset()

        assertEquals(PdfZoomState.DEFAULT_MIN_ZOOM, state.zoom, delta)
        assertEquals(0f, state.offsetX, delta)
    }

    @Test
    fun `custom limits are respected`() {
        val state = PdfZoomState(minZoom = 1f, maxZoom = 3f, doubleTapZoom = 2f)

        state.transform(Offset(500f, 0f), Offset.Zero, 10f, viewportWidth)

        assertEquals(3f, state.zoom, delta)
    }

    @Test
    fun `rejects a minimum zoom below one`() {
        assertThrows(IllegalArgumentException::class.java) { PdfZoomState(minZoom = 0.5f) }
    }

    @Test
    fun `rejects a maximum zoom below the minimum`() {
        assertThrows(IllegalArgumentException::class.java) { PdfZoomState(minZoom = 2f, maxZoom = 1.5f, doubleTapZoom = 2f) }
    }

    @Test
    fun `rejects a double tap zoom outside the range`() {
        assertThrows(IllegalArgumentException::class.java) { PdfZoomState(maxZoom = 3f, doubleTapZoom = 4f) }
    }

    @Test
    fun `renderZoomFor rounds up to the next half step`() {
        assertEquals(1f, PdfZoomState.renderZoomFor(1f), delta)
        assertEquals(1.5f, PdfZoomState.renderZoomFor(1.01f), delta)
        assertEquals(1.5f, PdfZoomState.renderZoomFor(1.5f), delta)
        assertEquals(2.5f, PdfZoomState.renderZoomFor(2.4f), delta)
        assertEquals(5f, PdfZoomState.renderZoomFor(5f), delta)
    }

    @Test
    fun `renderZoomFor ignores float noise around a step`() {
        assertEquals(1f, PdfZoomState.renderZoomFor(1.0000001f), delta)
    }
}
