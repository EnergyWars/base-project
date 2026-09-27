package com.wafflehq.lib.uicore.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppShapesTest {

    @Test
    fun radiiFollowDesignSystem() {
        assertEquals(20.dp, AppRadius.card)
        assertEquals(20.dp, AppRadius.button)
        assertEquals(6.dp, AppRadius.chip)
        assertEquals(14.dp, AppRadius.textField)
        assertEquals(28.dp, AppRadius.sheet)
        assertEquals(28.dp, AppRadius.dialog)
        assertEquals(999.dp, AppRadius.pill)
    }

    @Test
    fun spacingIsMonotonicOnEightPointGrid() {
        val steps = listOf(AppSpacing.xs, AppSpacing.sm, AppSpacing.md, AppSpacing.lg, AppSpacing.xl, AppSpacing.xxl)
        assertEquals(4.dp, AppSpacing.xs)
        assertEquals(8.dp, AppSpacing.sm)
        assertEquals(16.dp, AppSpacing.lg)
        assertTrue(steps.zipWithNext().all { (a, b) -> a < b })
    }

    @Test
    fun fabClearanceCoversStackedFabs() {
        assertTrue(FabClearance.stackedFolderFabs > FabClearance.singleFab)
    }
}
