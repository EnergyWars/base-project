package com.wafflehq.uikit.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class AppContentPaddingTest {

    @Test
    fun fullScreenSubPage_usesZeroPadding() {
        val inner = PaddingValues(top = 48.dp, bottom = 16.dp)
        val result = appContentPadding(isFullScreenSubPage = true, innerPadding = inner)
        assertEquals(0.dp, result.calculateTopPadding())
        assertEquals(0.dp, result.calculateBottomPadding())
        assertEquals(0.dp, result.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(0.dp, result.calculateRightPadding(LayoutDirection.Ltr))
    }

    @Test
    fun regularPage_keepsScaffoldPadding() {
        val inner = PaddingValues(top = 48.dp, bottom = 16.dp)
        assertEquals(inner, appContentPadding(isFullScreenSubPage = false, innerPadding = inner))
    }
}
