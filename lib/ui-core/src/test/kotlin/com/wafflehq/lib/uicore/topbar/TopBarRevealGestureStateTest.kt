package com.wafflehq.lib.uicore.topbar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TopBarRevealGestureStateTest {

    private class Harness(scope: CoroutineScope, initiallyExpanded: Boolean = false) {
        var expanded = initiallyExpanded
        var reveals = 0
        var collapses = 0
        val state = TopBarRevealGestureState(
            revealThresholdPx = REVEAL_THRESHOLD,
            collapseThresholdPx = COLLAPSE_THRESHOLD,
            holdDurationMillis = HOLD_MS,
            coroutineScope = scope,
            isExpanded = { expanded },
            onReveal = { reveals++; expanded = true },
            onCollapse = { collapses++; expanded = false }
        )

        fun unconsumedDragDown(px: Float) = state.onDrag(deltaY = px, consumedElsewhere = false)

        fun unconsumedDragUp(px: Float) = state.onDrag(deltaY = -px, consumedElsewhere = false)

        fun consumedDrag(deltaY: Float) = state.onDrag(deltaY = deltaY, consumedElsewhere = true)

        fun release() = state.onGestureEnd()
    }

    private fun TestScope.harness(initiallyExpanded: Boolean = false) = Harness(backgroundScope, initiallyExpanded)

    @Test
    fun revealsAfterOverscrollThresholdAndHold() = runTest {
        val h = harness()
        h.unconsumedDragDown(60f)
        h.unconsumedDragDown(60f)
        advanceTimeBy(HOLD_MS - 1)
        runCurrent()
        assertEquals(0, h.reveals)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, h.reveals)
    }

    @Test
    fun doesNotRevealBelowOverscrollThreshold() = runTest {
        val h = harness()
        h.unconsumedDragDown(REVEAL_THRESHOLD - 1f)
        advanceTimeBy(HOLD_MS * 2)
        runCurrent()
        assertEquals(0, h.reveals)
    }

    @Test
    fun holdIsNotRestartedByFurtherOverscroll() = runTest {
        val h = harness()
        h.unconsumedDragDown(REVEAL_THRESHOLD)
        advanceTimeBy(HOLD_MS / 2)
        h.unconsumedDragDown(REVEAL_THRESHOLD)
        advanceTimeBy(HOLD_MS / 2)
        runCurrent()
        assertEquals(1, h.reveals)
    }

    @Test
    fun releaseBeforeHoldCancelsReveal() = runTest {
        val h = harness()
        h.unconsumedDragDown(REVEAL_THRESHOLD)
        advanceTimeBy(HOLD_MS / 2)
        h.release()
        advanceTimeBy(HOLD_MS)
        runCurrent()
        assertEquals(0, h.reveals)
    }

    @Test
    fun scrollingBackUpCancelsHoldAndResetsOverscroll() = runTest {
        val h = harness()
        h.unconsumedDragDown(REVEAL_THRESHOLD)
        h.unconsumedDragUp(1f)
        advanceTimeBy(HOLD_MS * 2)
        runCurrent()
        assertEquals(0, h.reveals)
        h.unconsumedDragDown(REVEAL_THRESHOLD - 1f)
        advanceTimeBy(HOLD_MS * 2)
        runCurrent()
        assertEquals(0, h.reveals)
    }

    @Test
    fun scrollableChildStillHavingRoomCancelsHold() = runTest {
        val h = harness()
        h.unconsumedDragDown(REVEAL_THRESHOLD)
        h.consumedDrag(-5f)
        advanceTimeBy(HOLD_MS * 2)
        runCurrent()
        assertEquals(0, h.reveals)
    }

    @Test
    fun nonScrollableAreaRevealsJustLikeScrollableAtTop() = runTest {
        val nonScrollable = harness()
        nonScrollable.unconsumedDragDown(REVEAL_THRESHOLD)
        advanceTimeBy(HOLD_MS)
        runCurrent()
        assertEquals(1, nonScrollable.reveals)

        val scrollableAtTop = harness()
        scrollableAtTop.unconsumedDragDown(REVEAL_THRESHOLD)
        advanceTimeBy(HOLD_MS)
        runCurrent()
        assertEquals(1, scrollableAtTop.reveals)
    }

    @Test
    fun overscrollWhileExpandedDoesNothing() = runTest {
        val h = harness(initiallyExpanded = true)
        h.unconsumedDragDown(REVEAL_THRESHOLD)
        advanceTimeBy(HOLD_MS * 2)
        runCurrent()
        assertEquals(0, h.reveals)
    }

    @Test
    fun collapsesAfterAccumulatedDownwardScrollWhileExpanded() = runTest {
        val h = harness(initiallyExpanded = true)
        h.unconsumedDragUp(COLLAPSE_THRESHOLD / 2)
        assertEquals(0, h.collapses)
        h.unconsumedDragUp(COLLAPSE_THRESHOLD / 2)
        assertEquals(1, h.collapses)
    }

    @Test
    fun collapseIgnoresConsumptionFlag() = runTest {
        val h = harness(initiallyExpanded = true)
        h.consumedDrag(-COLLAPSE_THRESHOLD)
        assertEquals(1, h.collapses)
    }

    @Test
    fun upwardScrollResetsCollapseAccumulation() = runTest {
        val h = harness(initiallyExpanded = true)
        h.unconsumedDragUp(COLLAPSE_THRESHOLD - 1f)
        h.unconsumedDragDown(3f)
        h.unconsumedDragUp(COLLAPSE_THRESHOLD - 1f)
        assertEquals(0, h.collapses)
    }

    @Test
    fun dragWhileCollapsedDoesNothingToCollapse() = runTest {
        val h = harness()
        h.unconsumedDragUp(COLLAPSE_THRESHOLD * 5)
        assertEquals(0, h.collapses)
    }

    @Test
    fun revealedBarIsNotCollapsedWithinSameGesture() = runTest {
        val h = harness()
        h.unconsumedDragDown(REVEAL_THRESHOLD)
        advanceTimeBy(HOLD_MS)
        runCurrent()
        assertEquals(1, h.reveals)
        h.unconsumedDragUp(COLLAPSE_THRESHOLD * 5)
        assertEquals(0, h.collapses)
        h.release()
        h.unconsumedDragUp(COLLAPSE_THRESHOLD)
        assertEquals(1, h.collapses)
    }

    @Test
    fun collapsedBarIsNotRevealedWithinSameGesture() = runTest {
        val h = harness(initiallyExpanded = true)
        h.unconsumedDragUp(COLLAPSE_THRESHOLD)
        assertEquals(1, h.collapses)
        h.unconsumedDragDown(REVEAL_THRESHOLD * 2)
        advanceTimeBy(HOLD_MS * 2)
        runCurrent()
        assertEquals(0, h.reveals)
        h.release()
        h.unconsumedDragDown(REVEAL_THRESHOLD)
        advanceTimeBy(HOLD_MS)
        runCurrent()
        assertEquals(1, h.reveals)
    }

    @Test
    fun collapseOnlyFiresOncePerGesture() = runTest {
        val h = harness(initiallyExpanded = true)
        h.unconsumedDragUp(COLLAPSE_THRESHOLD)
        h.expanded = true
        h.unconsumedDragUp(COLLAPSE_THRESHOLD)
        assertEquals(1, h.collapses)
    }

    private companion object {
        const val REVEAL_THRESHOLD = 100f
        const val COLLAPSE_THRESHOLD = 10f
        const val HOLD_MS = 500L
    }
}
