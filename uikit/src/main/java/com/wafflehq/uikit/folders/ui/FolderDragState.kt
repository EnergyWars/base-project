package com.wafflehq.uikit.folders.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.wafflehq.uikit.folders.FolderDrop
import com.wafflehq.uikit.theme.AppRadius
import com.wafflehq.uikit.theme.AppSpacing
import com.wafflehq.uikit.theme.AppTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

object FolderDragDefaults {
    val handleSize = 32.dp
    const val disabledAlpha = 0.38f
    const val HANDLE_TAG = "folder_drag_handle"
    val autoScrollEdgeSize = 64.dp
    val autoScrollMaxSpeed = 28.dp
    const val AUTO_SCROLL_INTERVAL_MS = 16L
}

class FolderDragState<T : Any> {
    var draggedItem by mutableStateOf<T?>(null)
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

    val isDragging: Boolean get() = draggedItem != null

    fun isDropTarget(folderId: Long): Boolean = isDragging && hoveredFolderId == folderId

    /** True while [entryKey]'s row is the current reorder target of an in-progress drag. */
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

    /** Registers the on-screen bounds of an entry row so other dragged entries can be reordered onto it. */
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

    /**
     * Ends the drag. If the drop point landed on another entry's row and [onReorder] is provided,
     * [onReorder] is invoked with that entry's key instead of [onDrop] - the same drag handle this
     * way serves both "move into a different folder" (drop on a folder card) and "reorder relative
     * to a sibling entry" (drop on another entry row).
     */
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
        hoveredFolderId = null
        hoveredEntryKey = null
    }
}

@Composable
fun <T : Any> rememberFolderDragState(): FolderDragState<T> = remember { FolderDragState() }

@Composable
fun <T : Any> FolderDragContainer(
    state: FolderDragState<T>,
    dragLabel: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { state.containerPositionInRoot = it.positionInRoot() },
    ) {
        content()
        state.draggedItem?.let { item ->
            val local = state.dragPositionInRoot - state.containerPositionInRoot
            Surface(
                modifier = Modifier
                    .offset { IntOffset(local.x.roundToInt() + 16, local.y.roundToInt() - 24) }
                    .width(220.dp)
                    .alpha(0.9f),
                shape = RoundedCornerShape(AppRadius.card),
                color = AppTheme.colors.surfaceVariant,
                shadowElevation = 6.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Icon(
                        Icons.Default.DragIndicator,
                        contentDescription = null,
                        tint = AppTheme.colors.onSurfaceVariant,
                    )
                    Text(
                        text = dragLabel(item),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = AppTheme.colors.onSurfaceVariant,
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
    onReorder: ((item: T, targetKey: Any) -> Unit)? = null,
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
                            onDragCancel = { state.cancel() },
                        )
                    }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Default.DragIndicator,
            contentDescription = contentDescription,
            tint = AppTheme.colors.onSurfaceVariant.copy(alpha = if (enabled) 1f else FolderDragDefaults.disabledAlpha),
        )
    }
}

fun folderAutoScrollSpeed(
    positionY: Float,
    containerTop: Float,
    containerBottom: Float,
    edgePx: Float,
    maxSpeedPx: Float,
): Float {
    val distanceFromTop = positionY - containerTop
    val distanceFromBottom = containerBottom - positionY
    return when {
        distanceFromTop < edgePx ->
            -maxSpeedPx * (1f - (distanceFromTop / edgePx).coerceIn(0f, 1f))
        distanceFromBottom < edgePx ->
            maxSpeedPx * (1f - (distanceFromBottom / edgePx).coerceIn(0f, 1f))
        else -> 0f
    }
}

fun <T : Any> Modifier.folderDragAutoScroll(
    state: FolderDragState<T>,
    listState: LazyListState,
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
                    maxSpeedPx = maxSpeedPx,
                )
                if (speed != 0f) listState.scrollBy(speed)
            }
            delay(FolderDragDefaults.AUTO_SCROLL_INTERVAL_MS)
        }
    }
    onGloballyPositioned { bounds = it.boundsInRoot() }
}
