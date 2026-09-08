package com.wafflehq.uikit.folders

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
        2L to Rect(0f, 60f, 100f, 110f),
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
}
