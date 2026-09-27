package com.wafflehq.base.ui.library.demos

import com.wafflehq.lib.folders.FolderDeletionAction
import com.wafflehq.lib.folders.FolderTreeRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FoldersDemoModelTest {

    private fun model() = FoldersDemoModel.sample { "s$it" }

    private fun FoldersDemoModel.folderRows(): List<Pair<Long, Int>> =
        rows().mapNotNull { row -> if (row is FolderTreeRow.FolderRow) row.folder.id to row.depth else null }

    private fun FoldersDemoModel.folderRowIds(): List<Long> = folderRows().map { it.first }

    private fun FoldersDemoModel.entryRowIds(): List<Long> =
        rows().mapNotNull { row -> if (row is FolderTreeRow.EntryRow) row.entry.id else null }

    @Test
    fun collapsedTreeShowsOnlyRootFolders() {
        val model = model()

        assertEquals(3, model.folderRowIds().size)
        assertTrue(model.entryRowIds().isEmpty())
        assertEquals(listOf(FoldersDemoModel.IDEAS), model.unfiledEntries().map { it.id })
    }

    @Test
    fun expandingAFolderRevealsChildFoldersThenEntries() {
        val model = model()

        model.toggleExpanded(FoldersDemoModel.RECIPES)

        assertEquals(1, model.folderRows().first { it.first == FoldersDemoModel.SOUPS }.second)
        assertEquals(listOf(FoldersDemoModel.PASTA), model.entryRowIds())
        assertEquals(FoldersDemoModel.RECIPES, model.selectedFolderId)
    }

    @Test
    fun togglingTwiceCollapsesAgain() {
        val model = model()

        model.toggleExpanded(FoldersDemoModel.NOTES)
        model.toggleExpanded(FoldersDemoModel.NOTES)

        assertTrue(model.expandedIds.isEmpty())
        assertTrue(model.entryRowIds().isEmpty())
    }

    @Test
    fun entryCountIncludesNestedFolders() {
        val model = model()

        assertEquals(2, model.entryCount(FoldersDemoModel.RECIPES))
        assertEquals(1, model.entryCount(FoldersDemoModel.SOUPS))
        assertEquals(0, model.entryCount(FoldersDemoModel.ARCHIVE))
    }

    @Test
    fun addFolderCreatesAUniqueChildAndExpandsTheParent() {
        val model = model()

        val created = model.addFolder("Snacks", FoldersDemoModel.NOTES)

        assertEquals("Snacks", created.name)
        assertEquals(FoldersDemoModel.NOTES, created.parentId)
        assertEquals(model.folders.map { it.id }.toSet().size, model.folders.size)
        assertTrue(FoldersDemoModel.NOTES in model.expandedIds)
    }

    @Test
    fun renameChangesOnlyTheName() {
        val model = model()

        model.rename(FoldersDemoModel.ARCHIVE, "Old")

        assertEquals("Old", model.folderName(FoldersDemoModel.ARCHIVE))
        assertEquals(4, model.folders.size)
    }

    @Test
    fun deletingWithMoveUpKeepsTheContents() {
        val model = model()

        model.delete(FoldersDemoModel.RECIPES, FolderDeletionAction.MOVE_CONTENTS_UP)

        assertNull(model.folderName(FoldersDemoModel.RECIPES))
        assertNull(model.folders.first { it.id == FoldersDemoModel.SOUPS }.parentId)
        assertNull(model.entries.first { it.id == FoldersDemoModel.PASTA }.folderId)
        assertEquals(4, model.entries.size)
    }

    @Test
    fun deletingWithDeleteContentsRemovesTheWholeSubtree() {
        val model = model()

        model.delete(FoldersDemoModel.RECIPES, FolderDeletionAction.DELETE_CONTENTS)

        assertEquals(setOf(FoldersDemoModel.NOTES, FoldersDemoModel.ARCHIVE), model.folders.map { it.id }.toSet())
        assertEquals(setOf(FoldersDemoModel.MEETING, FoldersDemoModel.IDEAS), model.entries.map { it.id }.toSet())
    }

    @Test
    fun deletingTheSelectedFolderClearsTheSelection() {
        val model = model()
        model.select(FoldersDemoModel.SOUPS)

        model.delete(FoldersDemoModel.RECIPES, FolderDeletionAction.DELETE_CONTENTS)

        assertNull(model.selectedFolderId)
    }

    @Test
    fun movingAnEntryIntoAFolderExpandsIt() {
        val model = model()

        model.moveEntry(FoldersDemoModel.IDEAS, FoldersDemoModel.NOTES)

        assertEquals(FoldersDemoModel.NOTES, model.entries.first { it.id == FoldersDemoModel.IDEAS }.folderId)
        assertTrue(FoldersDemoModel.NOTES in model.expandedIds)
    }

    @Test
    fun droppingOutsideAFolderDoesNotUnfileTheEntry() {
        val model = model()

        model.moveEntry(FoldersDemoModel.MEETING, null)
        model.moveEntry(FoldersDemoModel.MEETING, FoldersDemoModel.NOTES)

        assertEquals(FoldersDemoModel.NOTES, model.entries.first { it.id == FoldersDemoModel.MEETING }.folderId)
    }

    @Test
    fun breadcrumbFollowsTheSelectedFolderChain() {
        val model = model()

        model.select(FoldersDemoModel.SOUPS)

        assertEquals(listOf(FoldersDemoModel.RECIPES, FoldersDemoModel.SOUPS), model.breadcrumb().map { it.id })
        model.select(null)
        assertTrue(model.breadcrumb().isEmpty())
    }

    @Test
    fun removeEntryDeletesOnlyThatEntry() {
        val model = model()

        model.removeEntry(FoldersDemoModel.PASTA)

        assertEquals(3, model.entries.size)
        assertNull(model.entries.firstOrNull { it.id == FoldersDemoModel.PASTA })
    }
}
