package com.wafflehq.lib.folders

import kotlinx.coroutines.flow.StateFlow

data class FolderAppearanceKey(val scope: String, val folderId: Long)

data class FolderAppearance(
    val iconKey: String? = null,
    val colorArgb: Int? = null
) {
    val isDefault: Boolean get() = iconKey == null && colorArgb == null
}

interface FolderAppearanceStore {
    val appearances: StateFlow<Map<FolderAppearanceKey, FolderAppearance>>

    fun save(key: FolderAppearanceKey, appearance: FolderAppearance)
}
