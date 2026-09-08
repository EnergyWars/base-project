package com.wafflehq.uikit.folders

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

object FolderDrop {
    fun <K> hoveredKey(bounds: Map<K, Rect>, position: Offset): K? =
        bounds.entries.firstOrNull { it.value.contains(position) }?.key

    fun hoveredFolder(bounds: Map<Long, Rect>, position: Offset): Long? = hoveredKey(bounds, position)

    fun shouldMove(currentFolderId: Long?, targetFolderId: Long?, allowUnfile: Boolean): Boolean {
        if (targetFolderId == currentFolderId) return false
        if (targetFolderId == null && !allowUnfile) return false
        return true
    }
}
