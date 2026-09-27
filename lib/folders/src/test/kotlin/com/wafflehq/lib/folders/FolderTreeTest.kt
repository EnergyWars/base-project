package com.wafflehq.lib.folders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private data class TestFolder(val id: Long, val parentId: Long?, val name: String, val sortOrder: Int = 0)
private data class TestEntry(val id: Long, val folderId: Long?, val title: String, val sortOrder: Int = 0)

private val entryOrder = compareBy<TestEntry> { it.title.lowercase() }

private fun buildRows(
    folders: List<TestFolder>,
    entries: List<TestEntry>,
    rootParentId: Long? = null,
    expandedFolderIds: Set<Long> = emptySet()
) = buildFolderTreeRows(
    folders = folders,
    entries = entries,
    rootParentId = rootParentId,
    expandedFolderIds = expandedFolderIds,
    folderId = { it.id },
    folderParentId = { it.parentId },
    folderName = { it.name },
    entryFolderId = { it.folderId },
    entryOrder = entryOrder
)

class FolderTreeTest {

    @Test
    fun `empty input returns empty list`() {
        val rows = buildRows(emptyList(), emptyList())
        assertTrue(rows.isEmpty())
    }

    @Test
    fun `flat folders without expand show only folder rows sorted case insensitively`() {
        val folders = listOf(
            TestFolder(1, null, "banane"),
            TestFolder(2, null, "Apfel"),
            TestFolder(3, null, "Zitrone")
        )
        val rows = buildRows(folders, emptyList())
        assertEquals(3, rows.size)
        assertEquals(listOf(2L, 1L, 3L), rows.map { (it as FolderTreeRow.FolderRow).folder.id })
        assertTrue(rows.all { (it as FolderTreeRow.FolderRow).depth == 0 })
        assertTrue(rows.all { !(it as FolderTreeRow.FolderRow).isExpanded })
    }

    @Test
    fun `expanded folder shows its subfolders and entries indented`() {
        val folders = listOf(
            TestFolder(1, null, "Root"),
            TestFolder(2, 1, "Child")
        )
        val entries = listOf(
            TestEntry(10, 1, "b-entry"),
            TestEntry(11, 1, "a-entry"),
            TestEntry(12, 2, "child-entry")
        )
        val rows = buildRows(folders, entries, expandedFolderIds = setOf(1L))

        assertEquals(4, rows.size)
        val folderRow = rows[0] as FolderTreeRow.FolderRow
        assertEquals(1L, folderRow.folder.id)
        assertEquals(0, folderRow.depth)
        assertTrue(folderRow.isExpanded)

        val childFolderRow = rows[1] as FolderTreeRow.FolderRow
        assertEquals(2L, childFolderRow.folder.id)
        assertEquals(1, childFolderRow.depth)
        assertTrue(!childFolderRow.isExpanded)

        val entryRow1 = rows[2] as FolderTreeRow.EntryRow
        val entryRow2 = rows[3] as FolderTreeRow.EntryRow
        assertEquals(11L, entryRow1.entry.id)
        assertEquals(10L, entryRow2.entry.id)
        assertEquals(1, entryRow1.depth)
        assertEquals(1, entryRow2.depth)
    }

    @Test
    fun `collapsed folder does not expose its children`() {
        val folders = listOf(
            TestFolder(1, null, "Root"),
            TestFolder(2, 1, "Child")
        )
        val entries = listOf(TestEntry(10, 1, "entry"))
        val rows = buildRows(folders, entries, expandedFolderIds = emptySet())

        assertEquals(1, rows.size)
        assertEquals(1L, (rows[0] as FolderTreeRow.FolderRow).folder.id)
    }

    @Test
    fun `multiple expanded levels flatten the whole visible path`() {
        val folders = listOf(
            TestFolder(1, null, "Root"),
            TestFolder(2, 1, "Middle"),
            TestFolder(3, 2, "Leaf")
        )
        val entries = listOf(
            TestEntry(10, 1, "root-entry"),
            TestEntry(11, 2, "middle-entry"),
            TestEntry(12, 3, "leaf-entry")
        )
        val rows = buildRows(folders, entries, expandedFolderIds = setOf(1L, 2L, 3L))

        assertEquals(6, rows.size)
        assertEquals(
            listOf(1L to 0, 2L to 1, 3L to 2, 12L to 3, 11L to 2, 10L to 1),
            rows.map { row ->
                when (row) {
                    is FolderTreeRow.FolderRow -> row.folder.id to row.depth
                    is FolderTreeRow.EntryRow -> row.entry.id to row.depth
                }
            }
        )
    }

    @Test
    fun `entries without a folder are ignored by the tree builder`() {
        val folders = listOf(TestFolder(1, null, "Root"))
        val entries = listOf(TestEntry(10, null, "unfiled"))
        val rows = buildRows(folders, entries, expandedFolderIds = setOf(1L))
        assertEquals(1, rows.size)
        assertTrue(rows[0] is FolderTreeRow.FolderRow)
    }

    @Test
    fun `rootParentId scopes the top level to a specific folder`() {
        val folders = listOf(
            TestFolder(1, null, "Root"),
            TestFolder(2, 1, "Child A"),
            TestFolder(3, 1, "Child B")
        )
        val rows = buildRows(folders, emptyList(), rootParentId = 1L)
        assertEquals(2, rows.size)
        assertEquals(listOf(2L, 3L), rows.map { (it as FolderTreeRow.FolderRow).folder.id })
    }

    @Test
    fun `duplicate folder id reachable through a self referencing parent is visited only once`() {
        val folders = listOf(
            TestFolder(1, null, "A"),
            TestFolder(1, 1, "A-duplicate")
        )
        val rows = buildRows(folders, emptyList(), rootParentId = null, expandedFolderIds = setOf(1L))
        assertEquals(1, rows.size)
        assertEquals(1L, (rows[0] as FolderTreeRow.FolderRow).folder.id)
    }

    @Test
    fun `collectFolderSubtreeIds includes root and all nested descendants`() {
        val folders = listOf(
            TestFolder(1, null, "Root"),
            TestFolder(2, 1, "Child"),
            TestFolder(3, 2, "Grandchild"),
            TestFolder(4, null, "Unrelated")
        )
        val subtree = folders.collectFolderSubtreeIds(1L, { it.id }, { it.parentId })
        assertEquals(setOf(1L, 2L, 3L), subtree)
    }

    @Test
    fun `collectFolderSubtreeIds of a leaf folder is just itself`() {
        val folders = listOf(
            TestFolder(1, null, "Root"),
            TestFolder(2, 1, "Leaf")
        )
        val subtree = folders.collectFolderSubtreeIds(2L, { it.id }, { it.parentId })
        assertEquals(setOf(2L), subtree)
    }

    private fun orderedRows(
        folders: List<TestFolder>,
        entries: List<TestEntry>,
        parentId: Long? = null,
        expandedFolderIds: Set<Long> = emptySet()
    ) = orderedFolderLevelRows(
        folders = folders,
        entries = entries,
        parentId = parentId,
        expandedFolderIds = expandedFolderIds,
        folderId = { it.id },
        folderParentId = { it.parentId },
        folderSortOrder = { it.sortOrder },
        entryFolderId = { it.folderId },
        entrySortOrder = { it.sortOrder }
    )

    private fun rowIds(rows: List<FolderTreeRow<TestFolder, TestEntry>>): List<Long> = rows.map { row ->
        when (row) {
            is FolderTreeRow.FolderRow -> row.folder.id
            is FolderTreeRow.EntryRow -> row.entry.id
        }
    }

    @Test
    fun `orderedFolderLevelRows interleaves folders and entries by combined sort order`() {
        val folders = listOf(TestFolder(1, null, "A", sortOrder = 1))
        val entries = listOf(
            TestEntry(10, null, "x", sortOrder = 0),
            TestEntry(11, null, "y", sortOrder = 2)
        )
        val rows = orderedRows(folders, entries)
        assertEquals(listOf(10L, 1L, 11L), rowIds(rows))
    }

    @Test
    fun `orderedFolderLevelRows breaks ties folder-before-entry so unreordered data stays grouped`() {
        val folders = listOf(TestFolder(1, null, "A", sortOrder = 0), TestFolder(2, null, "B", sortOrder = 0))
        val entries = listOf(TestEntry(10, null, "x", sortOrder = 0), TestEntry(11, null, "y", sortOrder = 0))
        val rows = orderedRows(folders, entries)
        assertEquals(listOf(1L, 2L, 10L, 11L), rowIds(rows))
    }

    @Test
    fun `orderedFolderLevelRows only returns direct children of parentId`() {
        val folders = listOf(TestFolder(1, null, "Root"), TestFolder(2, 1, "Child"))
        val entries = listOf(TestEntry(10, 1, "in child"), TestEntry(11, null, "at root"))
        val rows = orderedRows(folders, entries, parentId = null)
        assertEquals(listOf(1L, 11L), rowIds(rows))
    }

    @Test
    fun `orderedFolderLevelRows marks isExpanded from expandedFolderIds`() {
        val folders = listOf(TestFolder(1, null, "A"))
        val rows = orderedRows(folders, emptyList(), expandedFolderIds = setOf(1L))
        assertTrue((rows.single() as FolderTreeRow.FolderRow).isExpanded)
    }

    @Test
    fun `combinedLevelOrder assigns each row its combined-list index per type`() {
        val folders = listOf(TestFolder(1, null, "A"), TestFolder(2, null, "B"))
        val entries = listOf(TestEntry(10, null, "x"))
        val rows = listOf(
            FolderTreeRow.EntryRow(entries[0], 0),
            FolderTreeRow.FolderRow(folders[0], 0, isExpanded = false),
            FolderTreeRow.FolderRow(folders[1], 0, isExpanded = false)
        )
        val (folderOrders, entryOrders) = combinedLevelOrder(rows, folderId = { it.id }, entryId = { it.id })
        assertEquals(mapOf(1L to 1, 2L to 2), folderOrders)
        assertEquals(mapOf(10L to 0), entryOrders)
    }

    @Test
    fun `orderedFolderLevelRows then combinedLevelOrder round trips a reordered interleave`() {
        val folders = listOf(TestFolder(1, null, "A"))
        val entries = listOf(TestEntry(10, null, "x"))
        val reordered = listOf(
            FolderTreeRow.EntryRow(entries[0], 0),
            FolderTreeRow.FolderRow(folders[0], 0, isExpanded = false)
        )
        val (folderOrders, entryOrders) = combinedLevelOrder(reordered, folderId = { it.id }, entryId = { it.id })
        val persistedFolders = folders.map { it.copy(sortOrder = folderOrders.getValue(it.id)) }
        val persistedEntries = entries.map { it.copy(sortOrder = entryOrders.getValue(it.id)) }
        val rows = orderedRows(persistedFolders, persistedEntries)
        assertEquals(listOf(10L, 1L), rowIds(rows))
    }
}
