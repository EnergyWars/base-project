package com.wafflehq.lib.folders.ui

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.folders.FolderAppearanceKey
import com.wafflehq.lib.folders.FolderDrop
import com.wafflehq.lib.folders.FolderLevelLayout
import com.wafflehq.lib.folders.FolderTreeDrop
import com.wafflehq.lib.folders.FolderTreeDropTarget
import com.wafflehq.lib.folders.FolderTreeRow
import com.wafflehq.lib.folders.InsertionTarget
import com.wafflehq.lib.folders.collectAncestorFolderIds
import com.wafflehq.lib.folders.collectFolderSubtreeIds
import com.wafflehq.lib.folders.combinedLevelOrder
import com.wafflehq.lib.folders.orderedFolderLevelRows
import com.wafflehq.lib.folders.withRowInserted
import com.wafflehq.lib.uicore.components.DragReorderableList
import kotlinx.coroutines.delay

class FolderDragScrollState {
    var isDragging by mutableStateOf(false)
        internal set
    var positionY by mutableFloatStateOf(0f)
        internal set
}

@Composable
fun rememberFolderDragScrollState(): FolderDragScrollState = remember { FolderDragScrollState() }

fun Modifier.dragLevelAutoScroll(
    scrollState: FolderDragScrollState,
    listState: LazyListState
): Modifier = composed {
    var bounds by remember { mutableStateOf<Rect?>(null) }
    val density = LocalDensity.current
    LaunchedEffect(scrollState.isDragging, bounds) {
        if (!scrollState.isDragging) return@LaunchedEffect
        val edgePx = with(density) { FolderDragDefaults.autoScrollEdgeSize.toPx() }
        val maxSpeedPx = with(density) { FolderDragDefaults.autoScrollMaxSpeed.toPx() }
        while (scrollState.isDragging) {
            val currentBounds = bounds
            if (currentBounds != null) {
                val speed = folderAutoScrollSpeed(
                    positionY = scrollState.positionY,
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

internal class FolderDragTreeState {
    val headerBounds = HashMap<Long, Rect>()
    private val levelBounds = HashMap<Long?, Pair<Int, Rect>>()
    private val rowBounds = HashMap<Long?, HashMap<Any, Rect>>()
    var nestTargetId by mutableStateOf<Long?>(null)
    var insertion by mutableStateOf<InsertionTarget<Any>?>(null)
    var raisedFolderIds by mutableStateOf<Set<Long>>(emptySet())
    var blocked by mutableStateOf(false)
    var pendingTarget: FolderTreeDropTarget<Any>? = null

    fun registerLevel(parentId: Long?, depth: Int, bounds: Rect) {
        levelBounds[parentId] = depth to bounds
    }

    fun unregisterLevel(parentId: Long?) {
        levelBounds.remove(parentId)
    }

    fun registerRow(parentId: Long?, key: Any, bounds: Rect) {
        rowBounds.getOrPut(parentId) { HashMap() }[key] = bounds
    }

    fun unregisterRow(parentId: Long?, key: Any) {
        rowBounds[parentId]?.remove(key)
    }

    fun levels(): List<FolderLevelLayout<Any>> = levelBounds.map { (parentId, level) ->
        FolderLevelLayout(parentId, level.first, level.second.top, level.second.bottom, rowBounds[parentId].orEmpty())
    }

    fun clear() {
        nestTargetId = null
        insertion = null
        raisedFolderIds = emptySet()
        blocked = false
        pendingTarget = null
    }
}

private class FolderDragTree<F : Any, E : Any>(
    val folders: List<F>,
    val entries: List<E>,
    val expandedFolderIds: Set<Long>,
    val folderId: (F) -> Long,
    val folderParentId: (F) -> Long?,
    val folderSortOrder: (F) -> Int,
    val entryFolderId: (E) -> Long?,
    val entrySortOrder: (E) -> Int,
    val entryId: (E) -> Long,
    val onReorderLevel: (parentId: Long?, folderOrders: Map<Long, Int>, entryOrders: Map<Long, Int>) -> Unit,
    val onMoveEntry: (entry: E, targetFolderId: Long?) -> Unit,
    val onMoveFolder: ((folder: F, targetFolderId: Long?) -> Unit)?,
    val canNestFolder: (F, Long?) -> Boolean,
    val allowUnfile: Boolean,
    val scrollState: FolderDragScrollState?,
    val itemSpacing: Dp,
    val state: FolderDragTreeState,
    val appearanceScope: String?,
    val accentColor: Color,
    val expandedContent: (@Composable ColumnScope.(folder: F) -> Unit)?,
    val hasExpandedContent: (F) -> Boolean,
    val folderContent: @Composable (folder: F, isExpanded: Boolean, isNestTarget: Boolean, dragHandleModifier: Modifier) -> Unit,
    val entryContent: @Composable (entry: E, isDragging: Boolean, dragHandleModifier: Modifier) -> Unit
) {
    fun rowKey(row: FolderTreeRow<F, E>): Any = when (row) {
        is FolderTreeRow.FolderRow -> "folder-${folderId(row.folder)}"
        is FolderTreeRow.EntryRow -> "entry-${entryId(row.entry)}"
    }

    fun levelRows(parentId: Long?): List<FolderTreeRow<F, E>> = orderedFolderLevelRows(
        folders, entries, parentId, expandedFolderIds,
        folderId, folderParentId, folderSortOrder, entryFolderId, entrySortOrder
    )

    fun onDragMove(
        row: FolderTreeRow<F, E>,
        sourceParentId: Long?,
        sourceRows: List<FolderTreeRow<F, E>>,
        position: Offset,
        targetIndex: Int
    ): Boolean {
        scrollState?.isDragging = true
        scrollState?.positionY = position.y
        state.raisedFolderIds = folders.collectAncestorFolderIds(sourceParentId, folderId, folderParentId)
        val draggedKey = rowKey(row)
        val target = FolderTreeDrop.resolve(
            position = position,
            sourceParentId = sourceParentId,
            draggedKey = draggedKey,
            folderHeaderBounds = state.headerBounds,
            levels = state.levels(),
            canMoveInto = movePredicate(row),
            edgeInsetFraction = FolderListDefaults.nestZoneEdgeInsetFraction
        )
        state.pendingTarget = target
        return when (target) {
            is FolderTreeDropTarget.Nest -> {
                state.nestTargetId = target.folderId
                state.insertion = null
                state.blocked = false
                true
            }
            is FolderTreeDropTarget.Level -> if (target.parentId == sourceParentId) {
                state.nestTargetId = null
                state.insertion = FolderDrop.insertionTargetForIndex(sourceRows.map { rowKey(it) }, draggedKey, targetIndex)
                state.blocked = false
                false
            } else {
                state.nestTargetId = target.parentId.takeIf { target.insertion == null }
                state.insertion = target.insertion
                state.blocked = false
                true
            }
            FolderTreeDropTarget.Blocked -> {
                state.nestTargetId = null
                state.insertion = null
                state.blocked = true
                true
            }
        }
    }

    fun onDragEnd(row: FolderTreeRow<F, E>, sourceParentId: Long?): Boolean {
        val target = state.pendingTarget
        finishDrag()
        return when (target) {
            null -> false
            FolderTreeDropTarget.Blocked -> false
            is FolderTreeDropTarget.Nest -> {
                move(row, target.folderId)
                true
            }
            is FolderTreeDropTarget.Level -> if (target.parentId == sourceParentId) {
                false
            } else {
                move(row, target.parentId)
                val reordered = levelRows(target.parentId).withRowInserted(row, target.insertion) { rowKey(it) }
                val (folderOrders, entryOrders) = combinedLevelOrder(reordered, folderId, entryId)
                onReorderLevel(target.parentId, folderOrders, entryOrders)
                true
            }
        }
    }

    fun finishDrag() {
        state.clear()
        scrollState?.isDragging = false
    }

    private fun movePredicate(row: FolderTreeRow<F, E>): (Long?) -> Boolean {
        if (row !is FolderTreeRow.FolderRow) return { target -> target != null || allowUnfile }
        if (onMoveFolder == null) return { false }
        val folder = row.folder
        val blocked = folders.collectFolderSubtreeIds(folderId(folder), folderId, folderParentId)
        return { target -> (target == null || target !in blocked) && canNestFolder(folder, target) }
    }

    private fun move(row: FolderTreeRow<F, E>, targetParentId: Long?) {
        when (row) {
            is FolderTreeRow.FolderRow -> onMoveFolder?.invoke(row.folder, targetParentId)
            is FolderTreeRow.EntryRow -> onMoveEntry(row.entry, targetParentId)
        }
    }
}

@Composable
fun <F : Any, E : Any> FolderDragLevel(
    folders: List<F>,
    entries: List<E>,
    parentId: Long?,
    expandedFolderIds: Set<Long>,
    folderId: (F) -> Long,
    folderParentId: (F) -> Long?,
    folderSortOrder: (F) -> Int,
    entryFolderId: (E) -> Long?,
    entrySortOrder: (E) -> Int,
    entryId: (E) -> Long,
    onReorderLevel: (parentId: Long?, folderOrders: Map<Long, Int>, entryOrders: Map<Long, Int>) -> Unit,
    onMoveEntry: (entry: E, targetFolderId: Long?) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: FolderDragScrollState? = null,
    onMoveFolder: ((folder: F, targetFolderId: Long?) -> Unit)? = null,
    canNestFolder: (F, Long?) -> Boolean = { _, _ -> false },
    allowUnfile: Boolean = true,
    itemSpacing: Dp = FolderListDefaults.itemSpacing,
    appearanceScope: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    expandedContent: (@Composable ColumnScope.(folder: F) -> Unit)? = null,
    hasExpandedContent: (F) -> Boolean = { true },
    folderContent: @Composable (folder: F, isExpanded: Boolean, isNestTarget: Boolean, dragHandleModifier: Modifier) -> Unit,
    entryContent: @Composable (entry: E, isDragging: Boolean, dragHandleModifier: Modifier) -> Unit
) {
    val state = remember { FolderDragTreeState() }
    val tree = FolderDragTree(
        folders = folders,
        entries = entries,
        expandedFolderIds = expandedFolderIds,
        folderId = folderId,
        folderParentId = folderParentId,
        folderSortOrder = folderSortOrder,
        entryFolderId = entryFolderId,
        entrySortOrder = entrySortOrder,
        entryId = entryId,
        onReorderLevel = onReorderLevel,
        onMoveEntry = onMoveEntry,
        onMoveFolder = onMoveFolder,
        canNestFolder = canNestFolder,
        allowUnfile = allowUnfile,
        scrollState = scrollState,
        itemSpacing = itemSpacing,
        state = state,
        appearanceScope = appearanceScope ?: LocalFolderAppearanceScope.current,
        accentColor = accentColor,
        expandedContent = expandedContent,
        hasExpandedContent = hasExpandedContent,
        folderContent = folderContent,
        entryContent = entryContent
    )
    CompositionLocalProvider(LocalFolderAppearanceScope provides tree.appearanceScope) {
        FolderDragLevelContent(tree = tree, parentId = parentId, depth = 0, modifier = modifier)
    }
}

@Composable
private fun <F : Any, E : Any> FolderDragLevelContent(
    tree: FolderDragTree<F, E>,
    parentId: Long?,
    depth: Int,
    modifier: Modifier
) {
    val state = tree.state
    val rows = remember(tree.folders, tree.entries, parentId, tree.expandedFolderIds) { tree.levelRows(parentId) }
    val raisedKey = rows
        .firstOrNull { it is FolderTreeRow.FolderRow && tree.folderId(it.folder) in state.raisedFolderIds }
        ?.let { tree.rowKey(it) }

    DisposableEffect(parentId) {
        onDispose { state.unregisterLevel(parentId) }
    }

    DragReorderableList(
        items = rows,
        keyOf = { tree.rowKey(it) },
        onOrderChanged = { newRows ->
            val (folderOrders, entryOrders) = combinedLevelOrder(newRows, tree.folderId, tree.entryId)
            tree.onReorderLevel(parentId, folderOrders, entryOrders)
        },
        modifier = Modifier
            .onGloballyPositioned { state.registerLevel(parentId, depth, it.boundsInRoot()) }
            .then(modifier),
        draggingContainerColor = if (state.blocked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
        itemSpacing = tree.itemSpacing,
        onDragMove = { row, position, targetIndex -> tree.onDragMove(row, parentId, rows, position, targetIndex) },
        onDragEnd = { row -> tree.onDragEnd(row, parentId) },
        onDragCancel = { tree.finishDrag() },
        raisedItemKey = raisedKey,
        activationDelayMs = LocalViewConfiguration.current.longPressTimeoutMillis
    ) { row, _, isDragging, dragHandleModifier ->
        val key = tree.rowKey(row)
        val insertion = state.insertion
        when (row) {
            is FolderTreeRow.FolderRow -> {
                val id = tree.folderId(row.folder)
                DisposableEffect(id, parentId) {
                    onDispose {
                        state.headerBounds.remove(id)
                        state.unregisterRow(parentId, key)
                    }
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned {
                                val bounds = it.boundsInRoot()
                                state.headerBounds[id] = bounds
                                state.registerRow(parentId, key, bounds)
                            }
                    ) {
                        FolderInsertionLine(visible = insertion != null && insertion.key == key && insertion.above)
                        tree.folderContent(row.folder, row.isExpanded, state.nestTargetId == id, dragHandleModifier)
                        FolderInsertionLine(visible = insertion != null && insertion.key == key && !insertion.above)
                    }
                    if (row.isExpanded) {
                        val hasNestedRows = remember(tree.folders, tree.entries, id, tree.expandedFolderIds) {
                            tree.levelRows(id).isNotEmpty()
                        }
                        val extra = tree.expandedContent?.takeIf { tree.hasExpandedContent(row.folder) }
                        if (hasNestedRows || extra != null) {
                            val frameColor = rememberFolderTint(
                                key = tree.appearanceScope?.let { FolderAppearanceKey(it, id) },
                                default = tree.accentColor
                            )
                            Column(
                                modifier = Modifier
                                    .padding(top = tree.itemSpacing)
                                    .folderNestedFrame(frameColor)
                            ) {
                                extra?.invoke(this, row.folder)
                                if (hasNestedRows) {
                                    FolderDragLevelContent(
                                        tree = tree,
                                        parentId = id,
                                        depth = depth + 1,
                                        modifier = Modifier.padding(top = if (extra != null) tree.itemSpacing else 0.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is FolderTreeRow.EntryRow -> {
                DisposableEffect(key, parentId) {
                    onDispose { state.unregisterRow(parentId, key) }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { state.registerRow(parentId, key, it.boundsInRoot()) }
                ) {
                    FolderInsertionLine(visible = insertion != null && insertion.key == key && insertion.above)
                    tree.entryContent(row.entry, isDragging, dragHandleModifier)
                    FolderInsertionLine(visible = insertion != null && insertion.key == key && !insertion.above)
                }
            }
        }
    }
}
