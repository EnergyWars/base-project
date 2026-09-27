package com.wafflehq.lib.folders

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class FolderCrudDelegate<F>(
    private val scope: CoroutineScope,
    private val create: suspend (name: String, parentFolderId: Long?) -> Unit,
    private val rename: suspend (folder: F, name: String) -> Unit,
    private val delete: suspend (folder: F, action: FolderDeletionAction) -> Unit
) {

    fun createFolder(name: String, parentFolderId: Long? = null) {
        val normalized = FolderNames.normalize(name) ?: return
        scope.launch { create(normalized, parentFolderId) }
    }

    fun renameFolder(folder: F, name: String) {
        val normalized = FolderNames.normalize(name) ?: return
        scope.launch { rename(folder, normalized) }
    }

    fun deleteFolder(folder: F, action: FolderDeletionAction) {
        scope.launch { delete(folder, action) }
    }
}
