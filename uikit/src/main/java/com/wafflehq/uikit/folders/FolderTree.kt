package com.wafflehq.uikit.folders

data class IndentedFolder<T>(val folder: T, val depth: Int)

fun <T> List<T>.toIndentedFolderTree(
    id: (T) -> Long,
    parentId: (T) -> Long?,
    name: (T) -> String,
): List<IndentedFolder<T>> {
    val byParent = groupBy(parentId)
    val result = mutableListOf<IndentedFolder<T>>()
    val visited = HashSet<Long>()

    fun visit(parent: Long?, depth: Int) {
        byParent[parent]
            .orEmpty()
            .sortedBy { name(it).lowercase() }
            .forEach { folder ->
                val folderId = id(folder)
                if (visited.add(folderId)) {
                    result += IndentedFolder(folder, depth)
                    visit(folderId, depth + 1)
                }
            }
    }

    visit(null, 0)
    return result
}

sealed interface FolderTreeRow<out F, out E> {
    data class FolderRow<F>(val folder: F, val depth: Int, val isExpanded: Boolean) : FolderTreeRow<F, Nothing>
    data class EntryRow<E>(val entry: E, val depth: Int) : FolderTreeRow<Nothing, E>
}

fun <F, E> buildFolderTreeRows(
    folders: List<F>,
    entries: List<E>,
    rootParentId: Long?,
    expandedFolderIds: Set<Long>,
    folderId: (F) -> Long,
    folderParentId: (F) -> Long?,
    folderName: (F) -> String,
    entryFolderId: (E) -> Long?,
    entryOrder: Comparator<E>,
): List<FolderTreeRow<F, E>> {
    val foldersByParent = folders.groupBy(folderParentId)
    val entriesByFolder = entries.filter { entryFolderId(it) != null }.groupBy(entryFolderId)
    val result = mutableListOf<FolderTreeRow<F, E>>()
    val visited = HashSet<Long>()

    fun visit(parent: Long?, depth: Int) {
        foldersByParent[parent]
            .orEmpty()
            .sortedBy { folderName(it).lowercase() }
            .forEach { folder ->
                val id = folderId(folder)
                if (visited.add(id)) {
                    val isExpanded = id in expandedFolderIds
                    result += FolderTreeRow.FolderRow(folder, depth, isExpanded)
                    if (isExpanded) {
                        visit(id, depth + 1)
                        entriesByFolder[id].orEmpty().sortedWith(entryOrder).forEach { entry ->
                            result += FolderTreeRow.EntryRow(entry, depth + 1)
                        }
                    }
                }
            }
    }

    visit(rootParentId, 0)
    return result
}
