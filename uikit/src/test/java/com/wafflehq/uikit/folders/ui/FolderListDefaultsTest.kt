package com.wafflehq.uikit.folders.ui

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class FolderListDefaultsTest {

    @Test
    fun depthIndent_rootHasNoIndent() {
        assertEquals(0.dp, FolderListDefaults.depthIndent(0))
    }

    @Test
    fun depthIndent_growsLinearlyPerLevel() {
        assertEquals(16.dp, FolderListDefaults.depthIndent(1))
        assertEquals(48.dp, FolderListDefaults.depthIndent(3))
    }

    @Test
    fun depthIndent_negativeDepthIsClampedToRoot() {
        assertEquals(0.dp, FolderListDefaults.depthIndent(-2))
    }

    @Test
    fun spacingTokens_matchDesignGrid() {
        assertEquals(12.dp, FolderListDefaults.itemSpacing)
        assertEquals(16.dp, FolderListDefaults.indentPerDepth)
        assertEquals(36.dp, FolderListDefaults.iconButtonSize)
    }
}
