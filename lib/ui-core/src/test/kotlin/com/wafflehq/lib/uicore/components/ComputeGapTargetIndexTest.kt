package com.wafflehq.lib.uicore.components

import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeGapTargetIndexTest {

    private val uniform = listOf(100, 100, 100, 100)

    @Test
    fun fingerOverOwnGapKeepsIndex() {
        assertEquals(1, computeGapTargetIndex(uniform, spacingPx = 0, draggingIndex = 1, currentTargetIndex = 1, pointerY = 150f))
    }

    @Test
    fun upperHalfOfNextNeighborKeepsGapBeforeIt() {
        assertEquals(0, computeGapTargetIndex(uniform, spacingPx = 0, draggingIndex = 0, currentTargetIndex = 0, pointerY = 120f))
    }

    @Test
    fun lowerHalfOfNeighborMovesGapAfterIt() {
        assertEquals(2, computeGapTargetIndex(uniform, spacingPx = 0, draggingIndex = 0, currentTargetIndex = 0, pointerY = 280f))
    }

    @Test
    fun fingerPastWholeNeighborMovesGapAfterIt() {
        assertEquals(1, computeGapTargetIndex(uniform, spacingPx = 0, draggingIndex = 0, currentTargetIndex = 0, pointerY = 220f))
    }

    @Test
    fun movingUpUsesUpperAndLowerHalfOfPreviousNeighbor() {
        assertEquals(2, computeGapTargetIndex(uniform, spacingPx = 0, draggingIndex = 3, currentTargetIndex = 3, pointerY = 210f))
        assertEquals(3, computeGapTargetIndex(uniform, spacingPx = 0, draggingIndex = 3, currentTargetIndex = 3, pointerY = 290f))
    }

    @Test
    fun fingerInSpacingKeepsGap() {
        assertEquals(1, computeGapTargetIndex(listOf(100, 100, 100), spacingPx = 20, draggingIndex = 0, currentTargetIndex = 1, pointerY = 110f))
    }

    @Test
    fun fingerAboveAllRowsMovesGapToStart() {
        assertEquals(0, computeGapTargetIndex(listOf(100, 100, 100), spacingPx = 0, draggingIndex = 2, currentTargetIndex = 2, pointerY = -10f))
    }

    @Test
    fun fingerBelowAllRowsMovesGapToEnd() {
        assertEquals(3, computeGapTargetIndex(uniform, spacingPx = 0, draggingIndex = 0, currentTargetIndex = 0, pointerY = 10_000f))
    }

    @Test
    fun singleItemAlwaysStaysAtZero() {
        assertEquals(0, computeGapTargetIndex(listOf(100), spacingPx = 0, draggingIndex = 0, currentTargetIndex = 0, pointerY = 500f))
    }

    @Test
    fun invalidDraggingIndexKeepsCurrentTarget() {
        assertEquals(2, computeGapTargetIndex(uniform, spacingPx = 0, draggingIndex = 7, currentTargetIndex = 2, pointerY = 50f))
    }

    @Test
    fun outOfRangeCurrentTargetIsClamped() {
        assertEquals(3, computeGapTargetIndex(uniform, spacingPx = 0, draggingIndex = 0, currentTargetIndex = 9, pointerY = 350f))
    }

    @Test
    fun tallDraggedRowDoesNotDisplaceShortNeighborBeforeFingerReachesIt() {
        val heights = listOf(300, 60, 60)

        assertEquals(0, computeGapTargetIndex(heights, spacingPx = 12, draggingIndex = 0, currentTargetIndex = 0, pointerY = 250f))
        assertEquals(0, computeGapTargetIndex(heights, spacingPx = 12, draggingIndex = 0, currentTargetIndex = 0, pointerY = 330f))
        assertEquals(1, computeGapTargetIndex(heights, spacingPx = 12, draggingIndex = 0, currentTargetIndex = 0, pointerY = 360f))
    }

    @Test
    fun tallNeighborIsOnlyPassedOnceFingerReachesItsLowerHalf() {
        val heights = listOf(40, 400, 40, 40)

        assertEquals(0, computeGapTargetIndex(heights, spacingPx = 0, draggingIndex = 0, currentTargetIndex = 0, pointerY = 200f))
        assertEquals(1, computeGapTargetIndex(heights, spacingPx = 0, draggingIndex = 0, currentTargetIndex = 0, pointerY = 300f))
    }

    @Test
    fun eachNeighborsOwnHeightIsUsed() {
        val heights = listOf(40, 40, 400, 40)

        assertEquals(1, computeGapTargetIndex(heights, spacingPx = 0, draggingIndex = 0, currentTargetIndex = 0, pointerY = 70f))
        assertEquals(1, computeGapTargetIndex(heights, spacingPx = 0, draggingIndex = 0, currentTargetIndex = 0, pointerY = 250f))
        assertEquals(2, computeGapTargetIndex(heights, spacingPx = 0, draggingIndex = 0, currentTargetIndex = 0, pointerY = 300f))
    }

    @Test
    fun shortDraggedRowOverTallNeighborIsStableAfterShift() {
        val heights = listOf(40, 200)

        val moved = computeGapTargetIndex(heights, spacingPx = 0, draggingIndex = 0, currentTargetIndex = 0, pointerY = 150f)
        val again = computeGapTargetIndex(heights, spacingPx = 0, draggingIndex = 0, currentTargetIndex = moved, pointerY = 150f)

        assertEquals(1, moved)
        assertEquals(1, again)
    }

    @Test
    fun tallDraggedRowOverShortNeighborIsStableAfterShift() {
        val heights = listOf(300, 60)

        val moved = computeGapTargetIndex(heights, spacingPx = 12, draggingIndex = 0, currentTargetIndex = 0, pointerY = 360f)
        val again = computeGapTargetIndex(heights, spacingPx = 12, draggingIndex = 0, currentTargetIndex = moved, pointerY = 360f)

        assertEquals(1, moved)
        assertEquals(1, again)
    }
}
