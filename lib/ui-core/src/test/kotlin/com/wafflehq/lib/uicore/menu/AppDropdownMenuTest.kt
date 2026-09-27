package com.wafflehq.lib.uicore.menu

import android.app.Application
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.lib.uicore.components.AppTextField
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
class AppDropdownMenuTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `menu shows items when expanded and invokes selection`() {
        var selected: String? = null
        rule.setContent {
            MaterialTheme {
                val expanded = mutableStateOf(true)
                AppDropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                    DropdownMenuItem(text = { Text("Option A") }, onClick = { selected = "A" })
                    DropdownMenuItem(text = { Text("Option B") }, onClick = { selected = "B" })
                }
            }
        }
        rule.onNodeWithText("Option A").assertIsDisplayed()
        rule.onNodeWithText("Option B").assertIsDisplayed().performClick()
        assertEquals("B", selected)
    }

    @Test
    fun `menu is not shown when collapsed`() {
        rule.setContent {
            MaterialTheme {
                AppDropdownMenu(expanded = false, onDismissRequest = {}) {
                    DropdownMenuItem(text = { Text("Hidden option") }, onClick = {})
                }
            }
        }
        rule.onNodeWithText("Hidden option").assertDoesNotExist()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun `exposed dropdown menu shows items inside box and invokes selection`() {
        var selected: String? = null
        rule.setContent {
            MaterialTheme {
                val expanded = mutableStateOf(true)
                ExposedDropdownMenuBox(
                    expanded = expanded.value,
                    onExpandedChange = { expanded.value = it },
                ) {
                    AppTextField(
                        value = "Field",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    AppExposedDropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = { expanded.value = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Exposed option") },
                            onClick = { selected = "Exposed option" },
                        )
                    }
                }
            }
        }
        rule.onNodeWithText("Exposed option").assertIsDisplayed().performClick()
        assertEquals("Exposed option", selected)
    }
}
