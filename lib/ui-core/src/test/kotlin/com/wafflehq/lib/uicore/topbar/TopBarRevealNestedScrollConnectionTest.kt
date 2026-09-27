package com.wafflehq.lib.uicore.topbar

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TopBarRevealNestedScrollConnectionTest {

    private class Harness(scope: TestScope, initiallyExpanded: Boolean = false) {
        var expanded = initiallyExpanded
        var reveals = 0
        var collapses = 0
        val state = TopBarRevealGestureState(
            revealThresholdPx = REVEAL_THRESHOLD,
            collapseThresholdPx = COLLAPSE_THRESHOLD,
            holdDurationMillis = HOLD_MS,
            coroutineScope = scope.backgroundScope,
            isExpanded = { expanded },
            onReveal = { reveals++; expanded = true },
            onCollapse = { collapses++; expanded = false }
        )
        val connection = TopBarRevealNestedScrollConnection(state)

        fun postScroll(availableY: Float, source: NestedScrollSource = NestedScrollSource.UserInput): Offset =
            connection.onPostScroll(consumed = Offset.Zero, available = Offset(0f, availableY), source = source)
    }

    @Test
    fun unconsumedDownwardOverscrollFromScrollableChildRevealsAfterHold() = runTest {
        val h = Harness(this)
        assertEquals(Offset.Zero, h.postScroll(REVEAL_THRESHOLD))
        advanceTimeBy(HOLD_MS)
        runCurrent()
        assertEquals(1, h.reveals)
    }

    @Test
    fun overscrollAccumulatesAcrossFrames() = runTest {
        val h = Harness(this)
        h.postScroll(REVEAL_THRESHOLD / 2)
        h.postScroll(REVEAL_THRESHOLD / 2)
        advanceTimeBy(HOLD_MS)
        runCurrent()
        assertEquals(1, h.reveals)
    }

    @Test
    fun overscrollBelowThresholdDoesNotReveal() = runTest {
        val h = Harness(this)
        h.postScroll(REVEAL_THRESHOLD - 1f)
        advanceTimeBy(HOLD_MS * 2)
        runCurrent()
        assertEquals(0, h.reveals)
    }

    @Test
    fun flingOverscrollIsIgnored() = runTest {
        val h = Harness(this)
        h.postScroll(REVEAL_THRESHOLD * 2, source = NestedScrollSource.SideEffect)
        advanceTimeBy(HOLD_MS * 2)
        runCurrent()
        assertEquals(0, h.reveals)
    }

    @Test
    fun upwardOverscrollAtListEndIsNotForwarded() = runTest {
        val h = Harness(this, initiallyExpanded = true)
        h.postScroll(-COLLAPSE_THRESHOLD * 5)
        assertEquals(0, h.collapses)
    }

    @Test
    fun connectionNeverConsumesScroll() = runTest {
        val h = Harness(this)
        assertEquals(Offset.Zero, h.postScroll(REVEAL_THRESHOLD * 3))
        assertEquals(Offset.Zero, h.postScroll(-REVEAL_THRESHOLD))
        assertEquals(Offset.Zero, h.postScroll(0f))
    }

    @Test
    fun downwardOverscrollWhileExpandedDoesNotReveal() = runTest {
        val h = Harness(this, initiallyExpanded = true)
        h.postScroll(REVEAL_THRESHOLD * 2)
        advanceTimeBy(HOLD_MS * 2)
        runCurrent()
        assertEquals(0, h.reveals)
    }

    private companion object {
        const val REVEAL_THRESHOLD = 100f
        const val COLLAPSE_THRESHOLD = 10f
        const val HOLD_MS = 500L
    }
}
