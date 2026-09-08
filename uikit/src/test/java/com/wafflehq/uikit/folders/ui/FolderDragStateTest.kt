package com.wafflehq.uikit.folders.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderDragStateTest {

    private fun stateWithFolders(): FolderDragState<String> = FolderDragState<String>().apply {
        registerFolderBounds(1L, Rect(0f, 0f, 100f, 50f))
        registerFolderBounds(2L, Rect(0f, 60f, 100f, 110f))
    }

    @Test
    fun initialState_isIdle() {
        val state = FolderDragState<String>()

        assertFalse(state.isDragging)
        assertNull(state.draggedItem)
        assertNull(state.hoveredFolderId)
        assertFalse(state.isDropTarget(1L))
    }

    @Test
    fun start_overFolder_marksFolderAsDropTarget() {
        val state = stateWithFolders()

        state.start("item", Offset(10f, 10f))

        assertTrue(state.isDragging)
        assertEquals("item", state.draggedItem)
        assertEquals(1L, state.hoveredFolderId)
        assertTrue(state.isDropTarget(1L))
        assertFalse(state.isDropTarget(2L))
    }

    @Test
    fun move_updatesPositionAndHoveredFolder() {
        val state = stateWithFolders()
        state.start("item", Offset(10f, 10f))

        state.move(Offset(0f, 70f))

        assertEquals(Offset(10f, 80f), state.dragPositionInRoot)
        assertEquals(2L, state.hoveredFolderId)
    }

    @Test
    fun move_outsideAllFolders_clearsHoveredFolder() {
        val state = stateWithFolders()
        state.start("item", Offset(10f, 10f))

        state.move(Offset(500f, 500f))

        assertNull(state.hoveredFolderId)
        assertTrue(state.isDragging)
    }

    @Test
    fun end_deliversItemAndTargetThenResets() {
        val state = stateWithFolders()
        state.start("item", Offset(10f, 80f))
        var dropped: Pair<String, Long?>? = null

        state.end(onDrop = { item, target -> dropped = item to target })

        assertEquals("item" to 2L, dropped)
        assertFalse(state.isDragging)
        assertNull(state.hoveredFolderId)
    }

    @Test
    fun end_outsideFolders_deliversNullTarget() {
        val state = stateWithFolders()
        state.start("item", Offset(500f, 500f))
        var dropped: Pair<String, Long?>? = null

        state.end(onDrop = { item, target -> dropped = item to target })

        assertEquals("item" to null, dropped)
    }

    @Test
    fun end_withoutDrag_doesNotInvokeCallback() {
        val state = stateWithFolders()
        var invoked = false

        state.end(onDrop = { _, _ -> invoked = true })

        assertFalse(invoked)
    }

    @Test
    fun cancel_resetsWithoutCallback() {
        val state = stateWithFolders()
        state.start("item", Offset(10f, 10f))

        state.cancel()

        assertFalse(state.isDragging)
        assertNull(state.draggedItem)
        assertNull(state.hoveredFolderId)
    }

    @Test
    fun unregisterFolderBounds_removesDropTarget() {
        val state = stateWithFolders()
        state.unregisterFolderBounds(1L)

        state.start("item", Offset(10f, 10f))

        assertNull(state.hoveredFolderId)
    }

    @Test
    fun registerFolderBounds_overwritesPreviousBounds() {
        val state = stateWithFolders()
        state.registerFolderBounds(1L, Rect(200f, 200f, 300f, 300f))

        state.start("item", Offset(10f, 10f))

        assertNull(state.hoveredFolderId)
        state.move(Offset(240f, 240f))
        assertEquals(1L, state.hoveredFolderId)
    }

    @Test
    fun start_overEntry_marksEntryAsReorderTarget() {
        val state = stateWithFolders()
        state.registerEntryBounds("entry-A", Rect(0f, 200f, 100f, 250f))

        state.start("item", Offset(10f, 220f))

        assertEquals("entry-A", state.hoveredEntryKey)
        assertTrue(state.isReorderTarget("entry-A"))
        assertNull(state.hoveredFolderId)
    }

    @Test
    fun move_fromEntryToFolder_updatesBothIndependently() {
        val state = stateWithFolders()
        state.registerEntryBounds("entry-A", Rect(0f, 200f, 100f, 250f))
        state.start("item", Offset(10f, 220f))

        state.move(Offset(0f, -180f))

        assertEquals(1L, state.hoveredFolderId)
        assertNull(state.hoveredEntryKey)
    }

    @Test
    fun end_overEntry_invokesOnReorderInsteadOfOnDrop() {
        val state = stateWithFolders()
        state.registerEntryBounds("entry-A", Rect(0f, 200f, 100f, 250f))
        state.start("item", Offset(10f, 220f))
        var dropped: Pair<String, Long?>? = null
        var reordered: Pair<String, Any>? = null

        state.end(
            onDrop = { item, target -> dropped = item to target },
            onReorder = { item, targetKey -> reordered = item to targetKey },
        )

        assertNull(dropped)
        assertEquals("item" to "entry-A", reordered)
        assertFalse(state.isDragging)
        assertNull(state.hoveredEntryKey)
    }

    @Test
    fun end_overEntry_withoutOnReorder_fallsBackToOnDrop() {
        val state = stateWithFolders()
        state.registerEntryBounds("entry-A", Rect(0f, 200f, 100f, 250f))
        state.start("item", Offset(10f, 220f))
        var dropped: Pair<String, Long?>? = null

        state.end(onDrop = { item, target -> dropped = item to target })

        assertEquals("item" to null, dropped)
    }

    @Test
    fun unregisterEntryBounds_removesReorderTarget() {
        val state = stateWithFolders()
        state.registerEntryBounds("entry-A", Rect(0f, 200f, 100f, 250f))
        state.unregisterEntryBounds("entry-A")

        state.start("item", Offset(10f, 220f))

        assertNull(state.hoveredEntryKey)
    }

    @Test
    fun autoScrollSpeed_farFromEdges_isZero() {
        val speed = folderAutoScrollSpeed(
            positionY = 500f,
            containerTop = 0f,
            containerBottom = 1000f,
            edgePx = 100f,
            maxSpeedPx = 40f,
        )

        assertEquals(0f, speed)
    }

    @Test
    fun autoScrollSpeed_atTopEdge_scrollsUpwardAtMaxSpeed() {
        val speed = folderAutoScrollSpeed(
            positionY = -50f,
            containerTop = 0f,
            containerBottom = 1000f,
            edgePx = 100f,
            maxSpeedPx = 40f,
        )

        assertEquals(-40f, speed)
    }

    @Test
    fun autoScrollSpeed_atBottomEdge_scrollsDownwardAtMaxSpeed() {
        val speed = folderAutoScrollSpeed(
            positionY = 1050f,
            containerTop = 0f,
            containerBottom = 1000f,
            edgePx = 100f,
            maxSpeedPx = 40f,
        )

        assertEquals(40f, speed)
    }

    @Test
    fun autoScrollSpeed_halfwayIntoTopZone_isHalfMaxSpeed() {
        val speed = folderAutoScrollSpeed(
            positionY = 50f,
            containerTop = 0f,
            containerBottom = 1000f,
            edgePx = 100f,
            maxSpeedPx = 40f,
        )

        assertEquals(-20f, speed)
    }

    @Test
    fun autoScrollSpeed_halfwayIntoBottomZone_isHalfMaxSpeed() {
        val speed = folderAutoScrollSpeed(
            positionY = 950f,
            containerTop = 0f,
            containerBottom = 1000f,
            edgePx = 100f,
            maxSpeedPx = 40f,
        )

        assertEquals(20f, speed)
    }

    @Test
    fun autoScrollSpeed_exactlyOnEdgeBoundary_isZero() {
        val speed = folderAutoScrollSpeed(
            positionY = 100f,
            containerTop = 0f,
            containerBottom = 1000f,
            edgePx = 100f,
            maxSpeedPx = 40f,
        )

        assertEquals(0f, speed)
    }
}
