package com.wafflehq.lib.folders.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object FolderListDefaults {
    val itemSpacing: Dp = 12.dp
    val contentPadding: PaddingValues = PaddingValues(16.dp)
    val indentPerDepth: Dp = 24.dp
    val cardBorderWidth: Dp = 1.dp
    val expandedBorderWidth: Dp = 1.5.dp
    val dropTargetBorderWidth: Dp = 2.dp
    val nestedFrameIndent: Dp = 12.dp
    val nestedFramePadding: Dp = 12.5.dp
    const val nestedFrameAlpha: Float = 0.6f
    val cardBorderAlpha: Float = 0.4f
    val draggingAlpha: Float = 0.3f
    val tileSize: Dp = 32.dp
    val tileGlyphSize: Dp = 20.dp
    val iconTouchSize: Dp = 44.dp
    val previewTileSize: Dp = 48.dp
    val previewGlyphSize: Dp = 28.dp
    val entryTileSize: Dp = 40.dp
    val entryTileGlyphSize: Dp = 22.dp
    const val tileAlpha: Float = 0.18f
    const val idleTintBlend: Float = 0.07f
    const val expandedTintBlend: Float = 0.16f
    const val tintBorderAlpha: Float = 0.22f
    val metaIconSize: Dp = 14.dp
    const val handleAlpha: Float = 0.6f
    const val metaAlpha: Float = 0.8f
    val iconPickerCellSize: Dp = 52.dp
    val iconPickerGridHeight: Dp = 280.dp
    const val crumbSeparatorAlpha: Float = 0.6f
    val cardHorizontalPadding: Dp = 16.dp
    val cardVerticalPadding: Dp = 12.dp
    val cardContentSpacing: Dp = 12.dp
    val iconButtonSize: Dp = 36.dp
    const val nestZoneEdgeInsetFraction: Float = 0.25f
    val insertionLineThickness: Dp = 3.dp
    val insertionLineVerticalPadding: Dp = 2.dp

    val insertionLineSlot: Dp = insertionLineThickness + insertionLineVerticalPadding * 2
    val nestedFrameInset: Dp = expandedBorderWidth + nestedFramePadding
    val nestedFrameStep: Dp = nestedFrameIndent + nestedFrameInset

    fun depthIndent(depth: Int): Dp = indentPerDepth * depth.coerceAtLeast(0)

    fun tintBlend(isExpanded: Boolean): Float = if (isExpanded) expandedTintBlend else idleTintBlend

    fun borderWidth(isDropTarget: Boolean, isExpanded: Boolean): Dp = when {
        isDropTarget -> dropTargetBorderWidth
        isExpanded -> expandedBorderWidth
        else -> cardBorderWidth
    }
}

object FolderTestTags {
    const val FOLDER_CARD = "folder_card"
    const val FOLDER_MENU_BUTTON = "folder_menu_button"
    const val FOLDER_EXPAND_BUTTON = "folder_expand_button"
    const val NESTED_FRAME = "folder_nested_frame"
    const val FOLDER_LAST_ACTIVITY = "folder_last_activity"
    const val ENTRY_CARD = "folder_entry_card"
    const val ENTRY_MENU_BUTTON = "folder_entry_menu_button"
    const val BREADCRUMB = "folder_breadcrumb"
    const val INSERTION_LINE = "folder_insertion_line"
    fun breadcrumbItem(index: Int) = "folder_breadcrumb_item_$index"
}
