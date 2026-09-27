package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class IconPickerGridTest {

    @get:Rule
    val rule = createComposeRule()

    private val options = listOf(
        IconPickerOption(key = "home", icon = Icons.Default.Home, label = "Start"),
        IconPickerOption(key = "star", icon = Icons.Default.Star, label = "Stern")
    )

    @Test
    fun marksSelectedOptionOnly() {
        rule.setContent {
            IconPickerGrid(options = options, selectedKey = "star", onSelect = {})
        }

        rule.onNodeWithTag(IconPickerTestTags.cell("star")).assertIsSelected()
        rule.onNodeWithTag(IconPickerTestTags.cell("home")).assertIsNotSelected()
    }

    @Test
    fun selectingOptionReportsItsKey() {
        var selected: String? = null
        rule.setContent {
            IconPickerGrid(options = options, selectedKey = "home", onSelect = { selected = it })
        }

        rule.onNodeWithTag(IconPickerTestTags.cell("star")).performClick()

        assertEquals("star", selected)
    }

    @Test
    fun rendersEveryOptionAndKeepsGridTag() {
        rule.setContent {
            IconPickerGrid(options = options, selectedKey = null, onSelect = {})
        }

        rule.onNodeWithTag(IconPickerTestTags.GRID).assertExists()
        options.forEach { rule.onNodeWithTag(IconPickerTestTags.cell(it.key)).assertExists() }
    }

    @Test
    fun defaultsAreTouchFriendlyAndValid() {
        assertTrue(IconPickerDefaults.cellSize >= 48.dp)
        assertTrue(IconPickerDefaults.gridHeight > IconPickerDefaults.cellSize)
        assertTrue(IconPickerDefaults.idleAlpha > 0f && IconPickerDefaults.idleAlpha <= 1f)
        assertTrue(IconPickerDefaults.selectedBorderWidth > 0.dp)
    }
}
