package com.wafflehq.lib.uicore.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val DRAG_ACTIVATION_DELAY_MS = 150L
private const val DRAGGING_ROW_ALPHA = 0.3f

internal fun computeGapTargetIndex(
    heights: List<Int>,
    spacingPx: Int,
    draggingIndex: Int,
    currentTargetIndex: Int,
    pointerY: Float
): Int {
    if (draggingIndex !in heights.indices) return currentTargetIndex
    val otherCount = heights.lastIndex
    if (otherCount == 0) return 0
    val current = currentTargetIndex.coerceIn(0, otherCount)
    val gap = heights[draggingIndex] + spacingPx
    var slotTop = 0f
    var position = 0
    var firstTop = 0f
    var lastBottom = 0f
    for (index in heights.indices) {
        if (index == draggingIndex) continue
        val height = heights[index]
        val top = slotTop + if (position >= current) gap else 0
        val bottom = top + height
        if (position == 0) firstTop = top
        if (pointerY >= top && pointerY < bottom) {
            return if (pointerY < top + height / 2f) position else position + 1
        }
        lastBottom = bottom
        slotTop += height + spacingPx
        position++
    }
    return when {
        pointerY < firstTop -> 0
        pointerY >= lastBottom -> otherCount
        else -> current
    }
}

private class DragGeometry {
    var listCoordinates: LayoutCoordinates? = null
    var startListTop = 0f
    var startPointerY = 0f
    var pointerInRoot = Offset.Zero

    fun listTop(): Float = listCoordinates?.takeIf { it.isAttached }?.positionInRoot()?.y ?: 0f
}

@Composable
fun <T : Any> DragReorderableList(
    items: List<T>,
    keyOf: (T) -> Any,
    onOrderChanged: (List<T>) -> Unit,
    modifier: Modifier = Modifier,
    draggingContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    idleContainerColor: Color = Color.Transparent,
    itemSpacing: Dp = 0.dp,
    onDragMove: ((item: T, positionInRoot: Offset, targetIndex: Int) -> Boolean)? = null,
    onDragEnd: ((item: T) -> Boolean)? = null,
    onDragCancel: ((item: T) -> Unit)? = null,
    raisedItemKey: Any? = null,
    activationDelayMs: Long = DRAG_ACTIVATION_DELAY_MS,
    itemContent: @Composable (item: T, index: Int, isDragging: Boolean, dragHandleModifier: Modifier) -> Unit
) {
    val currentItems by rememberUpdatedState(items)
    val currentKeyOf by rememberUpdatedState(keyOf)
    val currentOnOrderChanged by rememberUpdatedState(onOrderChanged)
    val currentOnDragMove by rememberUpdatedState(onDragMove)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)
    val currentActivationDelayMs by rememberUpdatedState(activationDelayMs)
    var draggingItem by remember { mutableStateOf<T?>(null) }
    var draggingOffsetY by remember { mutableFloatStateOf(0f) }
    var targetIndex by remember { mutableIntStateOf(-1) }
    val itemHeights = remember { mutableStateMapOf<Any, Int>() }
    val geometry = remember { DragGeometry() }
    val haptic = LocalHapticFeedback.current
    val spacingPx = with(LocalDensity.current) { itemSpacing.roundToPx() }
    val currentSpacingPx by rememberUpdatedState(spacingPx)

    fun indexOf(item: T): Int {
        val itemKey = currentKeyOf(item)
        return currentItems.indexOfFirst { currentKeyOf(it) == itemKey }
    }

    fun updateDrag(item: T) {
        val listTop = geometry.listTop()
        draggingOffsetY = (geometry.pointerInRoot.y - geometry.startPointerY) - (listTop - geometry.startListTop)
        val from = indexOf(item)
        if (from < 0) return
        val heights = currentItems.map { itemHeights[currentKeyOf(it)] ?: 0 }
        val current = if (targetIndex >= 0) targetIndex else from
        val tentative = computeGapTargetIndex(heights, currentSpacingPx, from, current, geometry.pointerInRoot.y - listTop)
        val hold = currentOnDragMove?.invoke(item, geometry.pointerInRoot, tentative) == true
        if (!hold) targetIndex = tentative
    }

    fun resetDrag() {
        draggingItem = null
        draggingOffsetY = 0f
        targetIndex = -1
    }

    fun startDrag(item: T, pointerInRoot: Offset) {
        geometry.pointerInRoot = pointerInRoot
        geometry.startPointerY = pointerInRoot.y
        geometry.startListTop = geometry.listTop()
        draggingItem = item
        targetIndex = indexOf(item)
        updateDrag(item)
    }

    fun finishDrag(item: T) {
        updateDrag(item)
        val handledElsewhere = currentOnDragEnd?.invoke(item) == true
        val list = currentItems
        val from = indexOf(item)
        val to = targetIndex
        resetDrag()
        if (!handledElsewhere && from >= 0 && to in list.indices && to != from) {
            currentOnOrderChanged(list.toMutableList().apply { add(to, removeAt(from)) })
        }
    }

    val draggingKey = draggingItem?.let(keyOf)
    val draggingIndex = draggingKey?.let { dragged -> items.indexOfFirst { keyOf(it) == dragged }.takeIf { it >= 0 } }
    val draggingItemHeight = draggingKey?.let { itemHeights[it] } ?: 0
    val gapIndex = if (draggingIndex != null && targetIndex >= 0) targetIndex else draggingIndex

    Column(
        modifier = modifier.onGloballyPositioned { coordinates ->
            geometry.listCoordinates = coordinates
            draggingItem?.let { updateDrag(it) }
        },
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        items.forEachIndexed { index, item ->
            key(keyOf(item)) {
                val itemKey = keyOf(item)
                val isDragging = itemKey == draggingKey
                val currentItem by rememberUpdatedState(item)

                val targetOffset = when {
                    draggingIndex == null || gapIndex == null -> 0f
                    isDragging -> draggingOffsetY
                    draggingIndex < index && gapIndex >= index -> -(draggingItemHeight + spacingPx).toFloat()
                    draggingIndex > index && gapIndex <= index -> (draggingItemHeight + spacingPx).toFloat()
                    else -> 0f
                }

                val animatedOffset by animateFloatAsState(
                    targetValue = targetOffset,
                    animationSpec = if (isDragging) snap() else spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "reorder_offset_$index"
                )
                val elevation by animateDpAsState(
                    targetValue = if (isDragging) 6.dp else 0.dp,
                    label = "reorder_elevation_$index"
                )

                var handleCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

                Surface(
                    shadowElevation = elevation,
                    color = if (isDragging) draggingContainerColor else idleContainerColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { if (it.height > 0) itemHeights[itemKey] = it.height }
                        .zIndex(if (isDragging || itemKey == raisedItemKey) 1f else 0f)
                        .offset { IntOffset(0, animatedOffset.roundToInt()) }
                        .alpha(if (isDragging) DRAGGING_ROW_ALPHA else 1f)
                ) {
                    val dragHandleModifier = Modifier
                        .onGloballyPositioned { handleCoordinates = it }
                        .pointerInput(Unit) {
                            coroutineScope {
                                val scope = this
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                    var activated = false
                                    var finished = false
                                    val job = scope.launch {
                                        delay(currentActivationDelayMs)
                                        activated = true
                                        startDrag(currentItem, handleCoordinates?.localToRoot(down.position) ?: down.position)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    try {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            val change = event.changes.find { it.id == down.id }
                                            if (change == null || !change.pressed) {
                                                job.cancel()
                                                if (activated) {
                                                    change?.let {
                                                        it.consume()
                                                        geometry.pointerInRoot += it.position - it.previousPosition
                                                    }
                                                    finished = true
                                                    finishDrag(currentItem)
                                                }
                                                break
                                            }
                                            if (activated) {
                                                change.consume()
                                                geometry.pointerInRoot += change.position - change.previousPosition
                                                updateDrag(currentItem)
                                            } else if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) {
                                                job.cancel()
                                                break
                                            }
                                        }
                                    } finally {
                                        job.cancel()
                                        if (activated && !finished) {
                                            resetDrag()
                                            currentOnDragCancel?.invoke(currentItem)
                                        }
                                    }
                                }
                            }
                        }
                    itemContent(item, index, isDragging, dragHandleModifier)
                }
            }
        }
    }
}
