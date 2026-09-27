package com.wafflehq.lib.charts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartZoomStateTest {

    @Test
    fun `first applyZoomBounds seeds zoom from initialFit not minZoomFit`() {
        val state = ChartZoomState()

        state.applyZoomBounds(minZoomFit = 0.5f, initialFit = 2f)

        assertEquals(0.5f, state.minZoom)
        assertEquals(2f, state.zoom)
    }

    @Test
    fun `minZoom always reflects the fit-all bound so user can zoom out fully`() {
        val state = ChartZoomState()
        state.applyZoomBounds(minZoomFit = 0.3f, initialFit = 3f)

        state.zoom = state.minZoom
        state.applyZoomBounds(minZoomFit = 0.3f, initialFit = 3f)

        assertEquals(0.3f, state.zoom)
    }

    @Test
    fun `initialFit only applies once, later calls do not reset a user zoom`() {
        val state = ChartZoomState()
        state.applyZoomBounds(minZoomFit = 0.3f, initialFit = 3f)

        state.zoom = 1.5f
        state.applyZoomBounds(minZoomFit = 0.3f, initialFit = 3f)

        assertEquals(1.5f, state.zoom)
    }

    @Test
    fun `zoom is coerced up when minZoom grows past the current zoom`() {
        val state = ChartZoomState()
        state.applyZoomBounds(minZoomFit = 0.3f, initialFit = 3f)
        state.zoom = 0.3f

        state.applyZoomBounds(minZoomFit = 0.8f, initialFit = 0.8f)

        assertEquals(0.8f, state.zoom)
        assertEquals(0.8f, state.minZoom)
    }

    @Test
    fun `minZoomFit is clamped into the absolute zoom bounds`() {
        val state = ChartZoomState()

        state.applyZoomBounds(minZoomFit = 0.001f, initialFit = 0.001f)

        assertEquals(CHART_ABSOLUTE_MIN_ZOOM, state.minZoom)
    }

    @Test
    fun `initialFit below minZoomFit is clamped up to minZoomFit`() {
        val state = ChartZoomState()

        state.applyZoomBounds(minZoomFit = 1f, initialFit = 0.2f)

        assertEquals(1f, state.zoom)
    }

    @Test
    fun `initialFit above max zoom is clamped down`() {
        val state = ChartZoomState()

        state.applyZoomBounds(minZoomFit = 0.3f, initialFit = 50f)

        assertTrue(state.zoom <= CHART_MAX_ZOOM)
    }

    @Test
    fun `applyFitZoom uses the same value for min and initial zoom`() {
        val state = ChartZoomState()

        state.applyFitZoom(0.6f)

        assertEquals(0.6f, state.minZoom)
        assertEquals(0.6f, state.zoom)
    }
}
