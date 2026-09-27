package com.wafflehq.lib.folders

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Test

class FolderTreeDropTest {

    private val rootLevel = FolderLevelLayout(
        parentId = null,
        depth = 0,
        top = 0f,
        bottom = 400f,
        rowBounds = mapOf<Any, Rect>(
            "folder-1" to Rect(0f, 0f, 100f, 60f),
            "entry-20" to Rect(0f, 300f, 100f, 360f)
        )
    )

    private val childLevel = FolderLevelLayout(
        parentId = 1L,
        depth = 1,
        top = 72f,
        bottom = 288f,
        rowBounds = mapOf<Any, Rect>(
            "entry-10" to Rect(16f, 72f, 100f, 132f),
            "folder-2" to Rect(16f, 144f, 100f, 204f)
        )
    )

    private val headers = mapOf(
        1L to Rect(0f, 0f, 100f, 60f),
        2L to Rect(16f, 144f, 100f, 204f)
    )

    private fun resolve(
        position: Offset,
        sourceParentId: Long?,
        draggedKey: Any,
        canMoveInto: (Long?) -> Boolean = { true }
    ) = FolderTreeDrop.resolve(
        position = position,
        sourceParentId = sourceParentId,
        draggedKey = draggedKey,
        folderHeaderBounds = headers,
        levels = listOf(rootLevel, childLevel),
        canMoveInto = canMoveInto,
        edgeInsetFraction = 0.25f
    )

    @Test
    fun centerOfOtherFolderHeader_nestsIntoIt() {
        assertEquals(FolderTreeDropTarget.Nest(2L), resolve(Offset(50f, 174f), null, "entry-20"))
    }

    @Test
    fun edgeBandOfFolderHeader_insertsNextToIt() {
        assertEquals(
            FolderTreeDropTarget.Level(1L, InsertionTarget<Any>("folder-2", above = true)),
            resolve(Offset(50f, 148f), null, "entry-20")
        )
    }

    @Test
    fun centerOfOwnParentHeader_doesNotNestButTargetsParentLevel() {
        assertEquals(
            FolderTreeDropTarget.Level(null, InsertionTarget<Any>("folder-1", above = false)),
            resolve(Offset(50f, 40f), 1L, "entry-10")
        )
    }

    @Test
    fun insideExpandedChildLevel_targetsDeepestLevel() {
        assertEquals(
            FolderTreeDropTarget.Level(1L, InsertionTarget<Any>("entry-10", above = false)),
            resolve(Offset(50f, 110f), null, "entry-20")
        )
    }

    @Test
    fun belowChildLevel_draggedFromChild_targetsParentLevel() {
        assertEquals(
            FolderTreeDropTarget.Level(null, InsertionTarget<Any>("entry-20", above = true)),
            resolve(Offset(50f, 320f), 1L, "entry-10")
        )
    }

    @Test
    fun insideSourceLevel_excludesDraggedRowFromInsertion() {
        assertEquals(
            FolderTreeDropTarget.Level(1L, InsertionTarget<Any>("folder-2", above = true)),
            resolve(Offset(50f, 100f), 1L, "entry-10")
        )
    }

    @Test
    fun disallowedLevel_reportsBlockedInsteadOfSilentlySnappingBack() {
        assertEquals(
            FolderTreeDropTarget.Blocked,
            resolve(Offset(50f, 320f), 1L, "entry-10", canMoveInto = { it != null })
        )
    }

    @Test
    fun disallowedNestTarget_isIgnored() {
        assertEquals(
            FolderTreeDropTarget.Level(1L, InsertionTarget<Any>("folder-2", above = false)),
            resolve(Offset(50f, 180f), null, "entry-20", canMoveInto = { it != 2L })
        )
    }

    @Test
    fun sourceLevelIsAlwaysAllowedEvenIfPredicateRejectsIt() {
        assertEquals(
            FolderTreeDropTarget.Level(1L, InsertionTarget<Any>("folder-2", above = true)),
            resolve(Offset(50f, 100f), 1L, "entry-10", canMoveInto = { false })
        )
    }

    @Test
    fun outsideAllLevels_fallsBackToSourceLevelWithoutInsertion() {
        assertEquals(FolderTreeDropTarget.Level<Any>(1L, null), resolve(Offset(50f, 500f), 1L, "entry-10"))
    }
}
