package com.wafflehq.lib.folders.ui

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderListDefaultsTest {

    @Test
    fun depthIndent_rootHasNoIndent() {
        assertEquals(0.dp, FolderListDefaults.depthIndent(0))
    }

    @Test
    fun depthIndent_growsLinearlyPerLevel() {
        assertEquals(24.dp, FolderListDefaults.depthIndent(1))
        assertEquals(72.dp, FolderListDefaults.depthIndent(3))
    }

    @Test
    fun depthIndent_negativeDepthIsClampedToRoot() {
        assertEquals(0.dp, FolderListDefaults.depthIndent(-2))
    }

    @Test
    fun spacingTokens_matchDesignGrid() {
        assertEquals(12.dp, FolderListDefaults.itemSpacing)
        assertEquals(24.dp, FolderListDefaults.indentPerDepth)
        assertEquals(36.dp, FolderListDefaults.iconButtonSize)
    }

    @Test
    fun insertionLineTokens_areSet() {
        assertEquals(3.dp, FolderListDefaults.insertionLineThickness)
        assertEquals(2.dp, FolderListDefaults.insertionLineVerticalPadding)
    }

    @Test
    fun insertionLineSlot_sumsThicknessAndVerticalPadding() {
        assertEquals(7.dp, FolderListDefaults.insertionLineSlot)
    }

    @Test
    fun nestedFrame_insetCombinesBorderAndPadding() {
        assertEquals(
            FolderListDefaults.expandedBorderWidth + FolderListDefaults.nestedFramePadding,
            FolderListDefaults.nestedFrameInset
        )
        assertEquals(
            FolderListDefaults.nestedFrameIndent + FolderListDefaults.nestedFrameInset,
            FolderListDefaults.nestedFrameStep
        )
    }

    @Test
    fun nestedFrame_borderIsVisibleButLighterThanFullOpacity() {
        assertTrue(FolderListDefaults.nestedFrameAlpha > 0f && FolderListDefaults.nestedFrameAlpha < 1f)
        assertTrue(FolderListDefaults.nestedFrameStep > FolderListDefaults.nestedFrameInset)
    }

    @Test
    fun borderWidth_dropTargetWinsOverExpanded() {
        assertEquals(2.dp, FolderListDefaults.borderWidth(isDropTarget = true, isExpanded = true))
        assertEquals(2.dp, FolderListDefaults.borderWidth(isDropTarget = true, isExpanded = false))
    }

    @Test
    fun borderWidth_expandedIsThickerThanCollapsed() {
        assertEquals(1.5.dp, FolderListDefaults.borderWidth(isDropTarget = false, isExpanded = true))
        assertEquals(1.dp, FolderListDefaults.borderWidth(isDropTarget = false, isExpanded = false))
        assertTrue(
            FolderListDefaults.expandedBorderWidth > FolderListDefaults.cardBorderWidth
        )
    }

    @Test
    fun tintBlend_expandedIsStrongerThanIdleButStaysSubtle() {
        assertEquals(FolderListDefaults.idleTintBlend, FolderListDefaults.tintBlend(isExpanded = false), 0f)
        assertEquals(FolderListDefaults.expandedTintBlend, FolderListDefaults.tintBlend(isExpanded = true), 0f)
        assertTrue(FolderListDefaults.expandedTintBlend > FolderListDefaults.idleTintBlend)
        assertTrue(FolderListDefaults.expandedTintBlend < 0.5f)
        assertTrue(FolderListDefaults.idleTintBlend > 0f)
    }

    @Test
    fun tiles_glyphFitsInsideTile() {
        assertTrue(FolderListDefaults.tileGlyphSize < FolderListDefaults.tileSize)
        assertTrue(FolderListDefaults.entryTileGlyphSize < FolderListDefaults.entryTileSize)
        assertTrue(FolderListDefaults.previewGlyphSize < FolderListDefaults.previewTileSize)
    }

    @Test
    fun folderTile_isSmallButItsTouchTargetIsAccessible() {
        assertEquals(32.dp, FolderListDefaults.tileSize)
        assertEquals(40.dp, FolderListDefaults.entryTileSize)
        assertTrue(FolderListDefaults.tileSize < FolderListDefaults.previewTileSize)
        assertTrue(FolderListDefaults.iconTouchSize >= 40.dp)
        assertTrue(FolderListDefaults.iconTouchSize > FolderListDefaults.tileSize)
    }

    @Test
    fun alphaTokens_areValidFractions() {
        listOf(
            FolderListDefaults.tileAlpha,
            FolderListDefaults.tintBorderAlpha,
            FolderListDefaults.idleTintBlend,
            FolderListDefaults.handleAlpha,
            FolderListDefaults.metaAlpha,
            FolderListDefaults.crumbSeparatorAlpha,
            FolderListDefaults.cardBorderAlpha
        ).forEach { assertTrue(it > 0f && it <= 1f) }
    }

    @Test
    fun iconPicker_cellsAreTouchFriendly() {
        assertTrue(FolderListDefaults.iconPickerCellSize >= 48.dp)
    }
}
