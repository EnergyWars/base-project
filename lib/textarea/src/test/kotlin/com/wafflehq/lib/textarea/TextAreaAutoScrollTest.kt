package com.wafflehq.lib.textarea

import org.junit.Assert.assertEquals
import org.junit.Test

class TextAreaAutoScrollTest {

    @Test
    fun cursorInsideViewportDoesNotScroll() {
        val delta = TextAreaAutoScroll.scrollDelta(
            cursorTop = 300f,
            cursorBottom = 320f,
            viewportTop = 0f,
            viewportBottom = 1000f
        )

        assertEquals(0f, delta, 0f)
    }

    @Test
    fun cursorTouchingBothEdgesDoesNotScroll() {
        val delta = TextAreaAutoScroll.scrollDelta(
            cursorTop = 0f,
            cursorBottom = 1000f,
            viewportTop = 0f,
            viewportBottom = 1000f
        )

        assertEquals(0f, delta, 0f)
    }

    @Test
    fun cursorBelowViewportScrollsExactlyToBottomEdge() {
        val delta = TextAreaAutoScroll.scrollDelta(
            cursorTop = 1040f,
            cursorBottom = 1060f,
            viewportTop = 0f,
            viewportBottom = 1000f
        )

        assertEquals(60f, delta, 0f)
    }

    @Test
    fun cursorAboveViewportScrollsExactlyToTopEdge() {
        val delta = TextAreaAutoScroll.scrollDelta(
            cursorTop = -70f,
            cursorBottom = -50f,
            viewportTop = 0f,
            viewportBottom = 1000f
        )

        assertEquals(-70f, delta, 0f)
    }

    @Test
    fun viewportTopOffsetIsRespected() {
        val delta = TextAreaAutoScroll.scrollDelta(
            cursorTop = 100f,
            cursorBottom = 120f,
            viewportTop = 150f,
            viewportBottom = 1000f
        )

        assertEquals(-50f, delta, 0f)
    }

    @Test
    fun cursorPartiallyBelowScrollsOnlyByTheOverlap() {
        val delta = TextAreaAutoScroll.scrollDelta(
            cursorTop = 990f,
            cursorBottom = 1010f,
            viewportTop = 0f,
            viewportBottom = 1000f
        )

        assertEquals(10f, delta, 0f)
    }

    @Test
    fun cursorTallerThanViewportIsAlignedToTop() {
        val delta = TextAreaAutoScroll.scrollDelta(
            cursorTop = 500f,
            cursorBottom = 900f,
            viewportTop = 0f,
            viewportBottom = 300f
        )

        assertEquals(500f, delta, 0f)
    }

    @Test
    fun emptyViewportNeverScrolls() {
        val delta = TextAreaAutoScroll.scrollDelta(
            cursorTop = 500f,
            cursorBottom = 520f,
            viewportTop = 400f,
            viewportBottom = 400f
        )

        assertEquals(0f, delta, 0f)
    }

    @Test
    fun cursorInsideFieldIsNotClamped() {
        assertEquals(40f, TextAreaAutoScroll.clampToField(40f, fieldHeight = 200f), 0f)
    }

    @Test
    fun cursorBeyondInternallyScrolledFieldIsClampedToItsEdges() {
        assertEquals(200f, TextAreaAutoScroll.clampToField(900f, fieldHeight = 200f), 0f)
        assertEquals(0f, TextAreaAutoScroll.clampToField(-40f, fieldHeight = 200f), 0f)
    }

    @Test
    fun unknownFieldHeightSkipsClamping() {
        assertEquals(900f, TextAreaAutoScroll.clampToField(900f, fieldHeight = 0f), 0f)
    }

    @Test
    fun clipEdgeFallsBackToWindowWhenFieldIsNotVisible() {
        assertEquals(500f, TextAreaAutoScroll.clipEdge(500f, clipEdge = null, pickMax = false), 0f)
    }

    @Test
    fun clipEdgeUsesTheTighterBottomBound() {
        assertEquals(300f, TextAreaAutoScroll.clipEdge(500f, clipEdge = 300f, pickMax = false), 0f)
        assertEquals(500f, TextAreaAutoScroll.clipEdge(500f, clipEdge = 700f, pickMax = false), 0f)
    }

    @Test
    fun clipEdgeUsesTheTighterTopBound() {
        assertEquals(120f, TextAreaAutoScroll.clipEdge(40f, clipEdge = 120f, pickMax = true), 0f)
        assertEquals(40f, TextAreaAutoScroll.clipEdge(40f, clipEdge = 10f, pickMax = true), 0f)
    }
}
