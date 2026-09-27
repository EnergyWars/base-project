package com.wafflehq.lib.folders

import org.junit.Assert.assertEquals
import org.junit.Test

class FolderTreeMoveHelpersTest {

    private val rows = listOf("a", "b", "c")

    @Test
    fun withRowInserted_aboveAnchor_insertsBeforeIt() {
        assertEquals(listOf("a", "x", "b", "c"), rows.withRowInserted("x", InsertionTarget("b", above = true)) { it })
    }

    @Test
    fun withRowInserted_belowAnchor_insertsAfterIt() {
        assertEquals(listOf("a", "b", "x", "c"), rows.withRowInserted("x", InsertionTarget("b", above = false)) { it })
    }

    @Test
    fun withRowInserted_belowLastRow_appends() {
        assertEquals(listOf("a", "b", "c", "x"), rows.withRowInserted("x", InsertionTarget("c", above = false)) { it })
    }

    @Test
    fun withRowInserted_withoutInsertion_appends() {
        assertEquals(listOf("a", "b", "c", "x"), rows.withRowInserted("x", null) { it })
    }

    @Test
    fun withRowInserted_unknownAnchor_appends() {
        assertEquals(listOf("a", "b", "c", "x"), rows.withRowInserted("x", InsertionTarget("zz", above = true)) { it })
    }

    @Test
    fun withRowInserted_rowAlreadyPresent_isMovedInsteadOfDuplicated() {
        assertEquals(listOf("b", "c", "a"), rows.withRowInserted("a", InsertionTarget("c", above = false)) { it })
    }

    @Test
    fun withRowInserted_intoEmptyList_returnsOnlyRow() {
        assertEquals(listOf("x"), emptyList<String>().withRowInserted("x", null) { it })
    }

    private data class Folder(val id: Long, val parentId: Long?)

    private val folders = listOf(
        Folder(1L, null),
        Folder(2L, 1L),
        Folder(3L, 2L),
        Folder(4L, null)
    )

    @Test
    fun collectAncestorFolderIds_includesStartAndAllAncestors() {
        assertEquals(setOf(3L, 2L, 1L), folders.collectAncestorFolderIds(3L, { it.id }, { it.parentId }))
    }

    @Test
    fun collectAncestorFolderIds_rootFolder_containsOnlyItself() {
        assertEquals(setOf(4L), folders.collectAncestorFolderIds(4L, { it.id }, { it.parentId }))
    }

    @Test
    fun collectAncestorFolderIds_null_isEmpty() {
        assertEquals(emptySet<Long>(), folders.collectAncestorFolderIds(null, { it.id }, { it.parentId }))
    }

    @Test
    fun collectAncestorFolderIds_unknownFolder_containsOnlyItself() {
        assertEquals(setOf(99L), folders.collectAncestorFolderIds(99L, { it.id }, { it.parentId }))
    }

    @Test
    fun collectAncestorFolderIds_cycle_terminates() {
        val cyclic = listOf(Folder(1L, 2L), Folder(2L, 1L))

        assertEquals(setOf(1L, 2L), cyclic.collectAncestorFolderIds(1L, { it.id }, { it.parentId }))
    }
}
