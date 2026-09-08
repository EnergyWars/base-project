package com.wafflehq.uikit.folders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private data class TestFolder(val id: Long, val parentId: Long?, val name: String)
private data class TestEntry(val id: Long, val folderId: Long?, val title: String)

private val entryOrder = compareBy<TestEntry> { it.title.lowercase() }

private fun buildRows(
    folders: List<TestFolder>,
    entries: List<TestEntry>,
    rootParentId: Long? = null,
    expandedFolderIds: Set<Long> = emptySet(),
) = buildFolderTreeRows(
    folders = folders,
    entries = entries,
    rootParentId = rootParentId,
    expandedFolderIds = expandedFolderIds,
    folderId = { it.id },
    folderParentId = { it.parentId },
    folderName = { it.name },
    entryFolderId = { it.folderId },
    entryOrder = entryOrder,
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
            TestFolder(3, null, "Zitrone"),
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
            TestFolder(2, 1, "Child"),
        )
        val entries = listOf(
            TestEntry(10, 1, "b-entry"),
            TestEntry(11, 1, "a-entry"),
            TestEntry(12, 2, "child-entry"),
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
            TestFolder(2, 1, "Child"),
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
            TestFolder(3, 2, "Leaf"),
        )
        val entries = listOf(
            TestEntry(10, 1, "root-entry"),
            TestEntry(11, 2, "middle-entry"),
            TestEntry(12, 3, "leaf-entry"),
        )
        val rows = buildRows(folders, entries, expandedFolderIds = setOf(1L, 2L, 3L))

        assertEquals(6, rows.size)
        assertEquals(listOf(0, 1, 2, 3, 2, 1), rows.map { row ->
            when (row) {
                is FolderTreeRow.FolderRow -> row.depth
                is FolderTreeRow.EntryRow -> row.depth
            }
        })
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
            TestFolder(3, 1, "Child B"),
        )
        val rows = buildRows(folders, emptyList(), rootParentId = 1L)
        assertEquals(2, rows.size)
        assertEquals(listOf(2L, 3L), rows.map { (it as FolderTreeRow.FolderRow).folder.id })
    }

    @Test
    fun `duplicate folder id reachable through a self referencing parent is visited only once`() {
        val folders = listOf(
            TestFolder(1, null, "A"),
            TestFolder(1, 1, "A-duplicate"),
        )
        val rows = buildRows(folders, emptyList(), rootParentId = null, expandedFolderIds = setOf(1L))
        assertEquals(1, rows.size)
        assertEquals(1L, (rows[0] as FolderTreeRow.FolderRow).folder.id)
    }
}
