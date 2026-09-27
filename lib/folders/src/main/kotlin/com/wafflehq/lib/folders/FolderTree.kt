package com.wafflehq.lib.folders

data class IndentedFolder<T>(val folder: T, val depth: Int)

fun <T> List<T>.toIndentedFolderTree(
    id: (T) -> Long,
    parentId: (T) -> Long?,
    name: (T) -> String
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
    entryOrder: Comparator<E>
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

fun <F, E> orderedFolderLevelRows(
    folders: List<F>,
    entries: List<E>,
    parentId: Long?,
    expandedFolderIds: Set<Long>,
    folderId: (F) -> Long,
    folderParentId: (F) -> Long?,
    folderSortOrder: (F) -> Int,
    entryFolderId: (E) -> Long?,
    entrySortOrder: (E) -> Int
): List<FolderTreeRow<F, E>> {
    val childFolders = folders.filter { folderParentId(it) == parentId }
    val childEntries = entries.filter { entryFolderId(it) == parentId }
    val rows = mutableListOf<FolderTreeRow<F, E>>()
    childFolders.forEach { rows += FolderTreeRow.FolderRow(it, 0, isExpanded = folderId(it) in expandedFolderIds) }
    childEntries.forEach { rows += FolderTreeRow.EntryRow(it, 0) }
    return rows.sortedWith(
        compareBy(
            { row -> row.levelSortOrder(folderSortOrder, entrySortOrder) },
            { row -> if (row is FolderTreeRow.FolderRow) 0 else 1 }
        )
    )
}

private fun <F, E> FolderTreeRow<F, E>.levelSortOrder(folderSortOrder: (F) -> Int, entrySortOrder: (E) -> Int): Int =
    when (this) {
        is FolderTreeRow.FolderRow -> folderSortOrder(folder)
        is FolderTreeRow.EntryRow -> entrySortOrder(entry)
    }

fun <F, E> combinedLevelOrder(
    rows: List<FolderTreeRow<F, E>>,
    folderId: (F) -> Long,
    entryId: (E) -> Long
): Pair<Map<Long, Int>, Map<Long, Int>> {
    val folderOrders = LinkedHashMap<Long, Int>()
    val entryOrders = LinkedHashMap<Long, Int>()
    rows.forEachIndexed { index, row ->
        when (row) {
            is FolderTreeRow.FolderRow -> folderOrders[folderId(row.folder)] = index
            is FolderTreeRow.EntryRow -> entryOrders[entryId(row.entry)] = index
        }
    }
    return folderOrders to entryOrders
}

fun <R, K> List<R>.withRowInserted(row: R, insertion: InsertionTarget<K>?, keyOf: (R) -> K): List<R> {
    val rowKey = keyOf(row)
    val rest = filterTo(mutableListOf()) { keyOf(it) != rowKey }
    val index = if (insertion == null) {
        rest.size
    } else {
        val anchor = rest.indexOfFirst { keyOf(it) == insertion.key }
        when {
            anchor < 0 -> rest.size
            insertion.above -> anchor
            else -> anchor + 1
        }
    }
    rest.add(index, row)
    return rest
}

fun <T> List<T>.collectAncestorFolderIds(folderId: Long?, id: (T) -> Long, parentId: (T) -> Long?): Set<Long> {
    if (folderId == null) return emptySet()
    val byId = associateBy(id)
    val result = LinkedHashSet<Long>()
    var current: Long? = folderId
    while (current != null && result.add(current)) {
        current = byId[current]?.let(parentId)
    }
    return result
}

fun <T> List<T>.folderAncestryChain(folderId: Long?, id: (T) -> Long, parentId: (T) -> Long?): List<T> {
    if (folderId == null) return emptyList()
    val byId = associateBy(id)
    val chain = ArrayDeque<T>()
    val visited = HashSet<Long>()
    var current = byId[folderId]
    while (current != null && visited.add(id(current))) {
        chain.addFirst(current)
        current = parentId(current)?.let { byId[it] }
    }
    return chain.toList()
}

fun <T> List<T>.collectFolderSubtreeIds(rootId: Long, id: (T) -> Long, parentId: (T) -> Long?): Set<Long> {
    val childrenByParent = groupBy(parentId)
    val result = mutableSetOf(rootId)
    val queue = ArrayDeque(listOf(rootId))
    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        childrenByParent[current].orEmpty().forEach { child ->
            val childId = id(child)
            if (result.add(childId)) queue.add(childId)
        }
    }
    return result
}
