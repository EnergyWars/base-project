package com.wafflehq.lib.uicore.topbar

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TOP_BAR_REVEAL_OVERSCROLL_THRESHOLD_DP = 48
private const val TOP_BAR_COLLAPSE_ON_SCROLL_THRESHOLD_DP = 8

fun Modifier.topBarRevealGesture(
    enabled: Boolean,
    isExpanded: Boolean,
    holdDurationMillis: Long,
    onReveal: () -> Unit,
    onCollapse: () -> Unit
): Modifier = composed {
    if (!enabled) return@composed this

    val coroutineScope = rememberCoroutineScope()
    val currentOnReveal by rememberUpdatedState(onReveal)
    val currentOnCollapse by rememberUpdatedState(onCollapse)
    val currentIsExpanded by rememberUpdatedState(isExpanded)
    val density = LocalDensity.current
    val revealThresholdPx = with(density) { TOP_BAR_REVEAL_OVERSCROLL_THRESHOLD_DP.dp.toPx() }
    val collapseThresholdPx = with(density) { TOP_BAR_COLLAPSE_ON_SCROLL_THRESHOLD_DP.dp.toPx() }

    val state = remember(revealThresholdPx, collapseThresholdPx, holdDurationMillis, coroutineScope) {
        TopBarRevealGestureState(
            revealThresholdPx = revealThresholdPx,
            collapseThresholdPx = collapseThresholdPx,
            holdDurationMillis = holdDurationMillis,
            coroutineScope = coroutineScope,
            isExpanded = { currentIsExpanded },
            onReveal = { currentOnReveal() },
            onCollapse = { currentOnCollapse() }
        )
    }
    val nestedScrollConnection = remember(state) { TopBarRevealNestedScrollConnection(state) }

    this
        .nestedScroll(nestedScrollConnection)
        .pointerInput(state) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val pointerId = down.id
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                    if (!change.pressed) break
                    state.onDrag(change.positionChangeIgnoreConsumed().y, change.isConsumed)
                }
                state.onGestureEnd()
            }
        }
}

internal class TopBarRevealNestedScrollConnection(
    private val state: TopBarRevealGestureState
) : NestedScrollConnection {

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (source == NestedScrollSource.UserInput && available.y > 0f) {
            state.onDrag(deltaY = available.y, consumedElsewhere = false)
        }
        return Offset.Zero
    }
}

internal class TopBarRevealGestureState(
    private val revealThresholdPx: Float,
    private val collapseThresholdPx: Float,
    private val holdDurationMillis: Long,
    private val coroutineScope: CoroutineScope,
    private val isExpanded: () -> Boolean,
    private val onReveal: () -> Unit,
    private val onCollapse: () -> Unit
) {
    private var overscrollPx = 0f
    private var collapseScrollPx = 0f
    private var holdJob: Job? = null
    private var gestureSettled = false

    fun onDrag(deltaY: Float, consumedElsewhere: Boolean) {
        if (gestureSettled) return
        if (isExpanded()) {
            trackCollapse(deltaY)
        } else {
            trackReveal(deltaY, consumedElsewhere)
        }
    }

    fun onGestureEnd() {
        cancelHold()
        collapseScrollPx = 0f
        gestureSettled = false
    }

    private fun trackCollapse(deltaY: Float) {
        if (deltaY < 0f) {
            collapseScrollPx -= deltaY
            if (collapseScrollPx >= collapseThresholdPx) {
                settle()
                onCollapse()
            }
        } else if (deltaY > 0f) {
            collapseScrollPx = 0f
        }
    }

    private fun trackReveal(deltaY: Float, consumedElsewhere: Boolean) {
        if (consumedElsewhere) {
            if (deltaY < 0f) cancelHold()
            return
        }
        if (deltaY > 0f) {
            overscrollPx += deltaY
            if (overscrollPx >= revealThresholdPx && holdJob?.isActive != true) {
                holdJob = coroutineScope.launch {
                    delay(holdDurationMillis)
                    holdJob = null
                    settle()
                    onReveal()
                }
            }
        } else if (deltaY < 0f) {
            cancelHold()
        }
    }

    private fun settle() {
        gestureSettled = true
        cancelHold()
        collapseScrollPx = 0f
    }

    private fun cancelHold() {
        holdJob?.cancel()
        holdJob = null
        overscrollPx = 0f
    }
}
