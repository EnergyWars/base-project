package com.wafflehq.lib.folders

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

data class InsertionTarget<K>(val key: K, val above: Boolean)

object FolderDrop {
    fun <K> hoveredKey(bounds: Map<K, Rect>, position: Offset): K? =
        bounds.entries.firstOrNull { it.value.contains(position) }?.key

    fun <K> hoveredInsertionTarget(bounds: Map<K, Rect>, position: Offset, excludeKey: K? = null): InsertionTarget<K>? {
        val candidates = if (excludeKey != null) bounds.filterKeys { it != excludeKey } else bounds
        val nearest = candidates.entries.minByOrNull { (_, rect) ->
            kotlin.math.abs(position.y - (rect.top + rect.bottom) / 2f)
        } ?: return null
        val center = (nearest.value.top + nearest.value.bottom) / 2f
        return InsertionTarget(nearest.key, above = position.y < center)
    }

    fun <K> insertionTargetForIndex(keys: List<K>, draggedKey: K, targetIndex: Int): InsertionTarget<K>? {
        val from = keys.indexOf(draggedKey)
        if (from < 0 || targetIndex == from) return null
        val others = keys.filter { it != draggedKey }
        if (others.isEmpty()) return null
        return if (targetIndex < others.size) {
            InsertionTarget(others[targetIndex.coerceAtLeast(0)], above = true)
        } else {
            InsertionTarget(others.last(), above = false)
        }
    }

    fun <K> reorderInsertion(keys: List<K>, draggedKey: K, targetKey: K): InsertionTarget<K>? {
        val from = keys.indexOf(draggedKey)
        val to = keys.indexOf(targetKey)
        if (from < 0 || to < 0 || from == to) return null
        return InsertionTarget(targetKey, above = from > to)
    }

    fun hoveredFolder(bounds: Map<Long, Rect>, position: Offset): Long? = hoveredKey(bounds, position)

    fun hoveredNestTarget(bounds: Map<Long, Rect>, position: Offset, edgeInsetFraction: Float = 0.25f): Long? =
        bounds.entries.firstOrNull { (_, rect) -> insetVertically(rect, edgeInsetFraction).contains(position) }?.key

    private fun insetVertically(rect: Rect, fraction: Float): Rect {
        val inset = rect.height * fraction
        return Rect(rect.left, rect.top + inset, rect.right, rect.bottom - inset)
    }

    fun shouldMove(currentFolderId: Long?, targetFolderId: Long?, allowUnfile: Boolean): Boolean {
        if (targetFolderId == currentFolderId) return false
        if (targetFolderId == null && !allowUnfile) return false
        return true
    }

    fun <T> canNestFolder(
        folderId: Long,
        targetFolderId: Long?,
        allFolders: List<T>,
        id: (T) -> Long,
        parentId: (T) -> Long?
    ): Boolean {
        if (targetFolderId == null) return true
        if (targetFolderId == folderId) return false
        val currentParent = allFolders.firstOrNull { id(it) == folderId }?.let(parentId)
        if (targetFolderId == currentParent) return false
        val subtree = allFolders.collectFolderSubtreeIds(folderId, id, parentId)
        return targetFolderId !in subtree
    }
}
