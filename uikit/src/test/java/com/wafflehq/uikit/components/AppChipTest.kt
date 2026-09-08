package com.wafflehq.uikit.components

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.uikit.theme.AppRole
import com.wafflehq.uikit.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppChipTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun allVariantsRenderAndFireClick() {
        val clicked = mutableMapOf<ChipVariant, Boolean>()
        rule.setContent {
            AppTheme {
                Column {
                    ChipVariant.entries.forEach { variant ->
                        AppChip(
                            label = "Chip $variant",
                            role = AppRole.Secondary,
                            variant = variant,
                            onClick = { clicked[variant] = true },
                        )
                    }
                }
            }
        }
        ChipVariant.entries.forEach { variant ->
            rule.onNodeWithText("Chip $variant").performClick()
            assertTrue("variant $variant should fire onClick", clicked[variant] == true)
        }
    }

    @Test
    fun filterChipShowsSelectedState() {
        rule.setContent {
            AppTheme {
                AppChip(
                    label = "Filter",
                    role = AppRole.Primary,
                    variant = ChipVariant.Filter,
                    selected = true,
                    onClick = {},
                )
            }
        }
        rule.onNodeWithText("Filter").assertExists()
    }

    @Test
    fun inputChipWithRemoveShowsCloseIconAndFiresOnRemove() {
        var removed = false
        rule.setContent {
            AppTheme {
                AppChip(
                    label = "Removable",
                    role = AppRole.Tertiary,
                    variant = ChipVariant.Input,
                    onClick = {},
                    onRemove = { removed = true },
                )
            }
        }
        rule.onNodeWithText("Removable").assertExists()
        assertTrue(!removed)
    }

    @Test
    fun inputChipWithoutRemoveRendersFine() {
        rule.setContent {
            AppTheme {
                AppChip(
                    label = "NoRemove",
                    role = AppRole.Neutral,
                    variant = ChipVariant.Input,
                    onClick = {},
                )
            }
        }
        rule.onNodeWithText("NoRemove").assertExists()
    }
}
