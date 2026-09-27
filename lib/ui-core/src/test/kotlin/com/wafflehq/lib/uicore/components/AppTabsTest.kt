package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppTabsTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `fixed tab row selects the clicked tab`() {
        rule.setContent {
            MaterialTheme {
                var selected by remember { mutableIntStateOf(0) }
                AppTabRow(selectedTabIndex = selected) {
                    listOf("One", "Two").forEachIndexed { index, label ->
                        AppTab(selected = selected == index, onClick = { selected = index }, text = { Text(label) })
                    }
                }
            }
        }

        rule.onNodeWithText("One").assertIsSelected()
        rule.onNodeWithText("Two").performClick()
        rule.onNodeWithText("Two").assertIsSelected()
    }

    @Test
    fun `scrollable tab row renders every tab`() {
        rule.setContent {
            MaterialTheme {
                AppTabRow(selectedTabIndex = 1, scrollable = true) {
                    listOf("One", "Two").forEachIndexed { index, label ->
                        AppTab(selected = index == 1, onClick = {}, text = { Text(label) })
                    }
                }
            }
        }

        rule.onNodeWithText("One").assertIsDisplayed()
        rule.onNodeWithText("Two").assertIsSelected()
    }
}
