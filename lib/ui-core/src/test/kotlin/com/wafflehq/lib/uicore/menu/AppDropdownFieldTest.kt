package com.wafflehq.lib.uicore.menu

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppDropdownFieldTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `field shows the value and keeps the menu closed initially`() {
        rule.setContent {
            MaterialTheme {
                AppDropdownField(value = "Current", label = { Text("Label") }) { _ ->
                    AppDropdownMenuItem(text = { Text("Entry") }, onClick = {})
                }
            }
        }
        rule.onNodeWithText("Current").assertIsDisplayed()
        rule.onNodeWithText("Entry").assertDoesNotExist()
    }

    @Test
    fun `clicking the field opens the menu and selecting dismisses it`() {
        var picked: String? = null
        rule.setContent {
            MaterialTheme {
                AppDropdownField(value = "Current", fieldModifier = Modifier.testTag("field")) { dismiss ->
                    AppDropdownMenuItem(
                        text = { Text("Entry") },
                        onClick = {
                            picked = "Entry"
                            dismiss()
                        },
                    )
                }
            }
        }
        rule.onNodeWithTag("field").performClick()
        rule.onNodeWithText("Entry").assertIsDisplayed().performClick()
        assertEquals("Entry", picked)
        rule.onNodeWithText("Entry").assertDoesNotExist()
    }

    @Test
    fun `disabled field does not open the menu`() {
        rule.setContent {
            MaterialTheme {
                AppDropdownField(
                    value = "Current",
                    enabled = false,
                    fieldModifier = Modifier.testTag("field"),
                ) { _ ->
                    AppDropdownMenuItem(text = { Text("Entry") }, onClick = {})
                }
            }
        }
        rule.onNodeWithTag("field").performClick()
        rule.onNodeWithText("Entry").assertDoesNotExist()
    }

    @Test
    fun `option field shows the selected label, marks it and reports the selection`() {
        var selected by mutableStateOf<String?>("B")
        rule.setContent {
            MaterialTheme {
                AppOptionDropdownField(
                    options = listOf("A", "B", "C"),
                    selected = selected,
                    onSelect = { selected = it },
                    optionLabel = { "Option $it" },
                    fieldModifier = Modifier.testTag("field"),
                )
            }
        }
        rule.onNodeWithText("Option B").assertIsDisplayed()
        rule.onNodeWithTag("field").performClick()
        rule.onNodeWithText("Option A").assertIsNotSelected()
        rule.onNodeWithText("Option C").assertIsNotSelected().performClick()
        assertEquals("C", selected)
        rule.onNodeWithText("Option C").assertIsDisplayed()
    }

    @Test
    fun `option field falls back to the empty value without a selection`() {
        rule.setContent {
            MaterialTheme {
                AppOptionDropdownField(
                    options = listOf("A"),
                    selected = null,
                    onSelect = {},
                    optionLabel = { it },
                    emptyValue = "Nothing chosen",
                )
            }
        }
        rule.onNodeWithText("Nothing chosen").assertIsDisplayed()
    }

    @Test
    fun `option field applies option modifiers and icons`() {
        rule.setContent {
            MaterialTheme {
                AppOptionDropdownField(
                    options = listOf("A"),
                    selected = "A",
                    onSelect = {},
                    optionLabel = { it },
                    fieldModifier = Modifier.testTag("field"),
                    optionIcon = { Text("icon-$it") },
                    optionModifier = { Modifier.testTag("option-$it") },
                )
            }
        }
        rule.onNodeWithTag("field").performClick()
        rule.onNodeWithTag("option-A").assertIsDisplayed().assertIsSelected()
        rule.onNodeWithText("icon-A").assertIsDisplayed()
    }

    @Test
    fun `autocomplete shows suggestions only after typing and when suggestions exist`() {
        var text by mutableStateOf("")
        var withSuggestions by mutableStateOf(true)
        var picked: String? = null
        rule.setContent {
            MaterialTheme {
                AppAutocompleteField(
                    value = text,
                    onValueChange = { text = it },
                    hasSuggestions = withSuggestions,
                    fieldModifier = Modifier.testTag("field"),
                ) { dismiss ->
                    AppDropdownMenuItem(
                        text = { Text("Suggestion") },
                        onClick = {
                            picked = "Suggestion"
                            dismiss()
                        },
                    )
                }
            }
        }
        rule.onNodeWithText("Suggestion").assertDoesNotExist()
        rule.onNodeWithTag("field").performTextInput("a")
        rule.onNodeWithText("Suggestion").assertIsDisplayed()
        assertNull(picked)
        rule.onNodeWithText("Suggestion").performClick()
        assertEquals("Suggestion", picked)
        rule.onNodeWithText("Suggestion").assertDoesNotExist()
        rule.onNodeWithTag("field").performTextInput("b")
        rule.onNodeWithText("Suggestion").assertIsDisplayed()
        withSuggestions = false
        rule.waitForIdle()
        rule.onNodeWithText("Suggestion").assertDoesNotExist()
        assertEquals("ab", text)
    }
}
