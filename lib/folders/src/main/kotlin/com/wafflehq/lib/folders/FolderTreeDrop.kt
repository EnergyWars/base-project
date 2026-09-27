package com.wafflehq.lib.folders

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

data class FolderLevelLayout<K>(
    val parentId: Long?,
    val depth: Int,
    val top: Float,
    val bottom: Float,
    val rowBounds: Map<K, Rect>
)

sealed interface FolderTreeDropTarget<out K> {
    data class Nest(val folderId: Long) : FolderTreeDropTarget<Nothing>
    data class Level<K>(val parentId: Long?, val insertion: InsertionTarget<K>?) : FolderTreeDropTarget<K>

    data object Blocked : FolderTreeDropTarget<Nothing>
}

object FolderTreeDrop {
    fun <K> resolve(
        position: Offset,
        sourceParentId: Long?,
        draggedKey: K,
        folderHeaderBounds: Map<Long, Rect>,
        levels: Collection<FolderLevelLayout<K>>,
        canMoveInto: (Long?) -> Boolean,
        edgeInsetFraction: Float
    ): FolderTreeDropTarget<K> {
        val nestCandidates = folderHeaderBounds.filterKeys { it != sourceParentId && canMoveInto(it) }
        FolderDrop.hoveredNestTarget(nestCandidates, position, edgeInsetFraction)?.let { return FolderTreeDropTarget.Nest(it) }
        val hoveredLevels = levels.filter { position.y >= it.top && position.y < it.bottom }
        val level = hoveredLevels
            .filter { it.parentId == sourceParentId || canMoveInto(it.parentId) }
            .maxByOrNull { it.depth }
        if (level != null) {
            return FolderTreeDropTarget.Level(
                level.parentId,
                FolderDrop.hoveredInsertionTarget(level.rowBounds, position, excludeKey = draggedKey)
            )
        }
        if (hoveredLevels.any { it.parentId != sourceParentId }) return FolderTreeDropTarget.Blocked
        return FolderTreeDropTarget.Level(sourceParentId, null)
    }
}
