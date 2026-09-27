package com.wafflehq.lib.folders

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderDropTest {

    private val bounds = mapOf(
        1L to Rect(0f, 0f, 100f, 50f),
        2L to Rect(0f, 60f, 100f, 110f)
    )

    @Test
    fun hoveredFolder_insideFirstFolder() {
        assertEquals(1L, FolderDrop.hoveredFolder(bounds, Offset(10f, 10f)))
    }

    @Test
    fun hoveredFolder_insideSecondFolder() {
        assertEquals(2L, FolderDrop.hoveredFolder(bounds, Offset(50f, 100f)))
    }

    @Test
    fun hoveredFolder_outsideAll_returnsNull() {
        assertNull(FolderDrop.hoveredFolder(bounds, Offset(200f, 200f)))
    }

    @Test
    fun hoveredFolder_emptyBounds_returnsNull() {
        assertNull(FolderDrop.hoveredFolder(emptyMap(), Offset.Zero))
    }

    @Test
    fun hoveredNestTarget_centerOfCard_hitsIt() {
        assertEquals(1L, FolderDrop.hoveredNestTarget(bounds, Offset(50f, 25f)))
    }

    @Test
    fun hoveredNestTarget_topEdgeBand_isNull() {
        assertNull(FolderDrop.hoveredNestTarget(bounds, Offset(50f, 5f)))
    }

    @Test
    fun hoveredNestTarget_bottomEdgeBand_isNull() {
        assertNull(FolderDrop.hoveredNestTarget(bounds, Offset(50f, 45f)))
    }

    @Test
    fun hoveredNestTarget_gapBetweenCards_isNull() {
        assertNull(FolderDrop.hoveredNestTarget(bounds, Offset(50f, 55f)))
    }

    @Test
    fun hoveredNestTarget_customInsetFraction_isRespected() {
        assertEquals(1L, FolderDrop.hoveredNestTarget(bounds, Offset(50f, 5f), edgeInsetFraction = 0f))
        assertNull(FolderDrop.hoveredNestTarget(bounds, Offset(50f, 5f), edgeInsetFraction = 0.4f))
    }

    @Test
    fun hoveredInsertionTarget_aboveCenterOfCard_reportsAbove() {
        val target = FolderDrop.hoveredInsertionTarget(bounds, Offset(50f, 10f))

        assertEquals(1L, target?.key)
        assertTrue(target!!.above)
    }

    @Test
    fun hoveredInsertionTarget_belowCenterOfCard_reportsBelow() {
        val target = FolderDrop.hoveredInsertionTarget(bounds, Offset(50f, 40f))

        assertEquals(1L, target?.key)
        assertFalse(target!!.above)
    }

    @Test
    fun hoveredInsertionTarget_inGapBetweenCards_picksNearestByCenterDistance() {
        val target = FolderDrop.hoveredInsertionTarget(bounds, Offset(50f, 58f))

        assertEquals(2L, target?.key)
        assertTrue(target!!.above)
    }

    @Test
    fun hoveredInsertionTarget_excludesDraggedRowItself() {
        val target = FolderDrop.hoveredInsertionTarget(bounds, Offset(50f, 25f), excludeKey = 1L)

        assertEquals(2L, target?.key)
    }

    @Test
    fun hoveredInsertionTarget_emptyBounds_returnsNull() {
        assertNull(FolderDrop.hoveredInsertionTarget(emptyMap<Long, Rect>(), Offset.Zero))
    }

    @Test
    fun hoveredInsertionTarget_onlyExcludedRowLeft_returnsNull() {
        val single = mapOf(1L to Rect(0f, 0f, 100f, 50f))

        assertNull(FolderDrop.hoveredInsertionTarget(single, Offset(50f, 25f), excludeKey = 1L))
    }

    @Test
    fun insertionTargetForIndex_originalIndex_isNull() {
        assertNull(FolderDrop.insertionTargetForIndex(listOf("a", "b", "c"), "b", 1))
    }

    @Test
    fun insertionTargetForIndex_movedUp_marksAboveOtherRowAtIndex() {
        assertEquals(InsertionTarget("a", above = true), FolderDrop.insertionTargetForIndex(listOf("a", "b", "c"), "c", 0))
    }

    @Test
    fun insertionTargetForIndex_movedDownInside_marksAboveFollowingRow() {
        assertEquals(InsertionTarget("d", above = true), FolderDrop.insertionTargetForIndex(listOf("a", "b", "c", "d"), "a", 2))
    }

    @Test
    fun insertionTargetForIndex_movedToEnd_marksBelowLastRow() {
        assertEquals(InsertionTarget("c", above = false), FolderDrop.insertionTargetForIndex(listOf("a", "b", "c"), "a", 2))
    }

    @Test
    fun insertionTargetForIndex_unknownDraggedKey_isNull() {
        assertNull(FolderDrop.insertionTargetForIndex(listOf("a", "b"), "x", 1))
    }

    @Test
    fun insertionTargetForIndex_singleRow_isNull() {
        assertNull(FolderDrop.insertionTargetForIndex(listOf("a"), "a", 1))
    }

    @Test
    fun shouldMove_sameFolder_false() {
        assertFalse(FolderDrop.shouldMove(1L, 1L, allowUnfile = true))
        assertFalse(FolderDrop.shouldMove(null, null, allowUnfile = true))
    }

    @Test
    fun shouldMove_differentFolder_true() {
        assertTrue(FolderDrop.shouldMove(1L, 2L, allowUnfile = false))
        assertTrue(FolderDrop.shouldMove(null, 2L, allowUnfile = false))
    }

    @Test
    fun shouldMove_toUnfiled_respectsAllowUnfile() {
        assertTrue(FolderDrop.shouldMove(1L, null, allowUnfile = true))
        assertFalse(FolderDrop.shouldMove(1L, null, allowUnfile = false))
    }

    private data class TestFolder(val id: Long, val parentId: Long?)

    private val nestingFolders = listOf(
        TestFolder(1L, null),
        TestFolder(2L, 1L),
        TestFolder(3L, 2L),
        TestFolder(4L, null)
    )

    private fun canNest(folderId: Long, targetFolderId: Long?) =
        FolderDrop.canNestFolder(folderId, targetFolderId, nestingFolders, { it.id }, { it.parentId })

    @Test
    fun canNestFolder_toUnrelatedFolder_true() {
        assertTrue(canNest(4L, 1L))
    }

    @Test
    fun canNestFolder_toRoot_alwaysTrue() {
        assertTrue(canNest(2L, null))
    }

    @Test
    fun canNestFolder_ontoSelf_false() {
        assertFalse(canNest(1L, 1L))
    }

    @Test
    fun canNestFolder_ontoCurrentParent_false() {
        assertFalse(canNest(2L, 1L))
    }

    @Test
    fun canNestFolder_ontoOwnDescendant_false() {
        assertFalse(canNest(1L, 2L))
        assertFalse(canNest(1L, 3L))
    }

    @Test
    fun canNestFolder_ontoOwnChild_false() {
        assertFalse(canNest(2L, 3L))
    }

    @Test
    fun reorderInsertion_draggedDownOntoTarget_marksBelowTarget() {
        assertEquals(InsertionTarget("c", above = false), FolderDrop.reorderInsertion(listOf("a", "b", "c"), "a", "c"))
    }

    @Test
    fun reorderInsertion_draggedUpOntoTarget_marksAboveTarget() {
        assertEquals(InsertionTarget("a", above = true), FolderDrop.reorderInsertion(listOf("a", "b", "c"), "c", "a"))
    }

    @Test
    fun reorderInsertion_adjacentRowDraggedDown_marksBelowNeighbor() {
        assertEquals(InsertionTarget("b", above = false), FolderDrop.reorderInsertion(listOf("a", "b", "c"), "a", "b"))
    }

    @Test
    fun reorderInsertion_targetIsDraggedRow_isNull() {
        assertNull(FolderDrop.reorderInsertion(listOf("a", "b"), "a", "a"))
    }

    @Test
    fun reorderInsertion_draggedKeyNotInList_isNull() {
        assertNull(FolderDrop.reorderInsertion(listOf("a", "b"), "x", "a"))
    }

    @Test
    fun reorderInsertion_targetKeyNotInList_isNull() {
        assertNull(FolderDrop.reorderInsertion(listOf("a", "b"), "a", "x"))
    }
}
