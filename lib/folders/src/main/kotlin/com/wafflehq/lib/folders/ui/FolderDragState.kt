package com.wafflehq.lib.folders.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.folders.FolderDrop
import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import androidx.compose.foundation.shape.RoundedCornerShape
import com.wafflehq.lib.uicore.gesture.awaitLongPress
import com.wafflehq.lib.uicore.gesture.dragAutoScrollSpeed
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.theme.AppSpacing

object FolderDragDefaults {
    val handleSize = 32.dp
    val ghostWidth = 220.dp
    val ghostElevation = 4.dp
    const val ghostAlpha = 0.95f
    const val disabledAlpha = 0.38f
    const val HANDLE_TAG = "folder_drag_handle"
    val autoScrollEdgeSize = 64.dp
    val autoScrollMaxSpeed = 28.dp
    const val AUTO_SCROLL_INTERVAL_MS = 16L
}

class FolderDragState<T : Any> {
    var draggedItem by mutableStateOf<T?>(null)
        private set
    var draggedFolderId by mutableStateOf<Long?>(null)
        private set
    var hoveredFolderId by mutableStateOf<Long?>(null)
        private set
    var hoveredEntryKey by mutableStateOf<Any?>(null)
        private set
    var dragPositionInRoot by mutableStateOf(Offset.Zero)
        private set
    var containerPositionInRoot by mutableStateOf(Offset.Zero)
        internal set
    private val folderBounds = mutableStateMapOf<Long, Rect>()
    private val entryBounds = mutableStateMapOf<Any, Rect>()

    val isDragging: Boolean get() = draggedItem != null || draggedFolderId != null

    fun isDropTarget(folderId: Long): Boolean = isDragging && hoveredFolderId == folderId

    fun isReorderTarget(entryKey: Any): Boolean = isDragging && hoveredEntryKey == entryKey

    fun registerFolderBounds(folderId: Long, coordinates: LayoutCoordinates) {
        registerFolderBounds(folderId, coordinates.boundsInRoot())
    }

    fun registerFolderBounds(folderId: Long, bounds: Rect) {
        folderBounds[folderId] = bounds
    }

    fun unregisterFolderBounds(folderId: Long) {
        folderBounds.remove(folderId)
    }

    fun registerEntryBounds(entryKey: Any, coordinates: LayoutCoordinates) {
        registerEntryBounds(entryKey, coordinates.boundsInRoot())
    }

    fun registerEntryBounds(entryKey: Any, bounds: Rect) {
        entryBounds[entryKey] = bounds
    }

    fun unregisterEntryBounds(entryKey: Any) {
        entryBounds.remove(entryKey)
    }

    fun start(item: T, positionInRoot: Offset) {
        draggedItem = item
        dragPositionInRoot = positionInRoot
        hoveredFolderId = FolderDrop.hoveredFolder(folderBounds, positionInRoot)
        hoveredEntryKey = FolderDrop.hoveredKey(entryBounds, positionInRoot)
    }

    fun move(delta: Offset) {
        dragPositionInRoot += delta
        hoveredFolderId = FolderDrop.hoveredFolder(folderBounds, dragPositionInRoot)
        hoveredEntryKey = FolderDrop.hoveredKey(entryBounds, dragPositionInRoot)
    }

    fun end(onDrop: (item: T, targetFolderId: Long?) -> Unit, onReorder: ((item: T, targetKey: Any) -> Unit)? = null) {
        val item = draggedItem
        val folderTarget = hoveredFolderId
        val entryTarget = hoveredEntryKey
        cancel()
        if (item == null) return
        if (entryTarget != null && onReorder != null) {
            onReorder(item, entryTarget)
        } else {
            onDrop(item, folderTarget)
        }
    }

    fun cancel() {
        draggedItem = null
        draggedFolderId = null
        hoveredFolderId = null
        hoveredEntryKey = null
    }

    fun updateFolderDragPosition(folderId: Long, positionInRoot: Offset) {
        draggedFolderId = folderId
        dragPositionInRoot = positionInRoot
        hoveredFolderId = FolderDrop.hoveredFolder(folderBounds, positionInRoot)?.takeIf { it != folderId }
        hoveredEntryKey = null
    }

    fun consumeFolderDrop(folderId: Long): Long? {
        val target = hoveredFolderId.takeIf { draggedFolderId == folderId }
        cancel()
        return target
    }
}

@Composable
fun <T : Any> rememberFolderDragState(): FolderDragState<T> = remember { FolderDragState() }

@Composable
fun <T : Any> FolderDragContainer(
    state: FolderDragState<T>,
    dragLabel: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { state.containerPositionInRoot = it.positionInRoot() }
    ) {
        content()
        state.draggedItem?.let { item ->
            val local = state.dragPositionInRoot - state.containerPositionInRoot
            Surface(
                modifier = Modifier
                    .offset { IntOffset(local.x.roundToInt() + 16, local.y.roundToInt() - 24) }
                    .width(FolderDragDefaults.ghostWidth)
                    .alpha(FolderDragDefaults.ghostAlpha),
                shape = RoundedCornerShape(AppRadius.card),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(FolderListDefaults.dropTargetBorderWidth, MaterialTheme.colorScheme.primary),
                shadowElevation = FolderDragDefaults.ghostElevation
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    Icon(
                        Icons.Default.DragIndicator,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = dragLabel(item),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun <T : Any> FolderDragHandle(
    state: FolderDragState<T>,
    item: T,
    contentDescription: String,
    onDrop: (item: T, targetFolderId: Long?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onReorder: ((item: T, targetKey: Any) -> Unit)? = null
) {
    var handleCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val currentItem by rememberUpdatedState(item)
    val currentOnDrop by rememberUpdatedState(onDrop)
    val currentOnReorder by rememberUpdatedState(onReorder)
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .size(FolderDragDefaults.handleSize)
            .onGloballyPositioned { handleCoordinates = it }
            .testTag(FolderDragDefaults.HANDLE_TAG)
            .then(
                if (enabled) {
                    Modifier.pointerInput(Unit) {
                        awaitEachGesture { awaitFirstDown(requireUnconsumed = false).consume() }
                    }.pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                state.start(currentItem, handleCoordinates?.localToRoot(offset) ?: offset)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                state.move(dragAmount)
                            },
                            onDragEnd = { state.end(currentOnDrop, currentOnReorder) },
                            onDragCancel = { state.cancel() }
                        )
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.DragIndicator,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = if (enabled) FolderListDefaults.handleAlpha else FolderDragDefaults.disabledAlpha
            )
        )
    }
}

@Composable
fun <T : Any> Modifier.folderLongPressDrag(
    state: FolderDragState<T>,
    item: T,
    onDrop: (item: T, targetFolderId: Long?) -> Unit,
    enabled: Boolean = true,
    onReorder: ((item: T, targetKey: Any) -> Unit)? = null
): Modifier {
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val currentItem by rememberUpdatedState(item)
    val currentOnDrop by rememberUpdatedState(onDrop)
    val currentOnReorder by rememberUpdatedState(onReorder)
    val haptic = LocalHapticFeedback.current
    return this
        .onGloballyPositioned { coordinates = it }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                if (!awaitLongPress(down, viewConfiguration.longPressTimeoutMillis, viewConfiguration.touchSlop)) {
                    return@awaitEachGesture
                }
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                state.start(currentItem, coordinates?.localToRoot(down.position) ?: down.position)
                var finished = false
                try {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        change.consume()
                        if (!change.pressed) {
                            finished = true
                            state.end(currentOnDrop, currentOnReorder)
                            break
                        }
                        state.move(change.position - change.previousPosition)
                    }
                } catch (cancellation: CancellationException) {
                    state.cancel()
                    throw cancellation
                }
                if (!finished) state.cancel()
            }
        }
}

fun folderAutoScrollSpeed(
    positionY: Float,
    containerTop: Float,
    containerBottom: Float,
    edgePx: Float,
    maxSpeedPx: Float
): Float = dragAutoScrollSpeed(positionY, containerTop, containerBottom, edgePx, maxSpeedPx)

fun <T : Any> Modifier.folderDragAutoScroll(
    state: FolderDragState<T>,
    listState: LazyListState
): Modifier = composed {
    var bounds by remember { mutableStateOf<Rect?>(null) }
    val density = LocalDensity.current
    LaunchedEffect(state.isDragging, bounds) {
        if (!state.isDragging) return@LaunchedEffect
        val edgePx = with(density) { FolderDragDefaults.autoScrollEdgeSize.toPx() }
        val maxSpeedPx = with(density) { FolderDragDefaults.autoScrollMaxSpeed.toPx() }
        while (state.isDragging) {
            val currentBounds = bounds
            if (currentBounds != null) {
                val speed = folderAutoScrollSpeed(
                    positionY = state.dragPositionInRoot.y,
                    containerTop = currentBounds.top,
                    containerBottom = currentBounds.bottom,
                    edgePx = edgePx,
                    maxSpeedPx = maxSpeedPx
                )
                if (speed != 0f) listState.scrollBy(speed)
            }
            delay(FolderDragDefaults.AUTO_SCROLL_INTERVAL_MS)
        }
    }
    onGloballyPositioned { bounds = it.boundsInRoot() }
}
