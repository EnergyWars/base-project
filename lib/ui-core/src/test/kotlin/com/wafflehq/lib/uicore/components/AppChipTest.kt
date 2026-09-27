package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.lib.uicore.theme.AppRadius
import org.junit.Assert.assertEquals
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
    fun `filter chip shows its label and reports the selected state`() {
        rule.setContent {
            MaterialTheme {
                Column {
                    AppFilterChip(selected = true, onClick = {}, label = { Text("On") }, modifier = Modifier.testTag("on"))
                    AppFilterChip(selected = false, onClick = {}, label = { Text("Off") }, modifier = Modifier.testTag("off"))
                }
            }
        }

        rule.onNodeWithText("On").assertIsDisplayed()
        rule.onNodeWithTag("on").assertIsSelected()
        rule.onNodeWithTag("off").assertIsNotSelected()
    }

    @Test
    fun `filter chip invokes onClick`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme { AppFilterChip(selected = false, onClick = { clicks++ }, label = { Text("Tap") }) }
        }

        rule.onNodeWithText("Tap").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `disabled filter chip does not invoke onClick`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme { AppFilterChip(selected = false, onClick = { clicks++ }, label = { Text("Off") }, enabled = false) }
        }

        rule.onNodeWithText("Off").performClick()

        assertEquals(0, clicks)
    }

    @Test
    fun `filter chip with a selected container color renders and reacts to clicks`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppFilterChip(
                    selected = true,
                    onClick = { clicks++ },
                    label = { Text("Accent") },
                    selectedContainerColor = Color.Magenta,
                    selectedContentColor = Color.White,
                )
            }
        }

        rule.onNodeWithText("Accent").assertIsDisplayed()
        rule.onNodeWithText("Accent").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `assist input and suggestion chips invoke onClick`() {
        var assist = 0
        var input = 0
        var suggestion = 0
        rule.setContent {
            MaterialTheme {
                Column {
                    AppAssistChip(onClick = { assist++ }, label = { Text("Assist") })
                    AppInputChip(selected = false, onClick = { input++ }, label = { Text("Input") })
                    AppSuggestionChip(onClick = { suggestion++ }, label = { Text("Suggestion") })
                }
            }
        }

        rule.onNodeWithText("Assist").performClick()
        rule.onNodeWithText("Input").performClick()
        rule.onNodeWithText("Suggestion").performClick()

        assertEquals(listOf(1, 1, 1), listOf(assist, input, suggestion))
    }

    @Test
    fun `label chip renders text and slot content`() {
        rule.setContent {
            MaterialTheme {
                Column {
                    AppLabelChip(text = "Tag")
                    AppLabelChip(containerColor = Color.Magenta, contentColor = Color.White) { Text("Slot") }
                }
            }
        }

        rule.onNodeWithText("Tag").assertIsDisplayed()
        rule.onNodeWithText("Slot").assertIsDisplayed()
    }

    @Test
    fun `label chip is clickable only when an onClick is given`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme { AppLabelChip(text = "Go", onClick = { clicks++ }) }
        }

        rule.onNodeWithText("Go").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `label chip with a leading icon shows icon and text`() {
        rule.setContent {
            MaterialTheme {
                AppLabelChip(
                    text = "Customized",
                    leadingIcon = Icons.Default.Edit,
                    modifier = Modifier.testTag("chip"),
                )
            }
        }

        rule.onNodeWithText("Customized").assertIsDisplayed()
        rule.onNodeWithTag("chip").assertIsDisplayed()
    }

    @Test
    fun `defaults use the chip radius`() {
        assertEquals(RoundedCornerShape(AppRadius.chip), AppChipDefaults.shape)
        assertEquals(18f, AppChipDefaults.iconSize.value, 0.0001f)
    }
}
