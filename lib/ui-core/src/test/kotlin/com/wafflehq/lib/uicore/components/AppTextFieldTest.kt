package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.wafflehq.lib.uicore.theme.AppRadius
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
class AppTextFieldTest {

    @get:Rule
    val rule = createComposeRule()

    private val fieldLabel = "Field label"

    @Test
    fun `default shape is the app text field radius`() {
        assertEquals(RoundedCornerShape(AppRadius.textField), AppTextFieldDefaults.shape)
    }

    @Test
    fun `default shape ignores the material shape scale`() {
        var themed: Any? = null
        rule.setContent {
            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(AppRadius.pill))) {
                themed = AppTextFieldDefaults.shape
            }
        }

        assertEquals(RoundedCornerShape(AppRadius.textField), themed)
    }

    @Test
    fun `label and value are displayed`() {
        rule.setContent {
            AppTextField(value = "Anna", onValueChange = {}, label = { Text(fieldLabel) })
        }

        rule.onNodeWithText(fieldLabel).assertExists()
        rule.onNodeWithText("Anna").assertExists()
    }

    @Test
    fun `typing reports the new value`() {
        val changes = mutableListOf<String>()
        rule.setContent {
            var text by remember { mutableStateOf("") }
            AppTextField(
                value = text,
                onValueChange = { text = it; changes += it },
                label = { Text(fieldLabel) },
                singleLine = true,
            )
        }

        rule.onNodeWithText(fieldLabel).performTextInput("Hi")

        assertTrue(changes.isNotEmpty())
        assertEquals("Hi", changes.last())
    }

    @Test
    fun `clearing reports an empty value`() {
        val changes = mutableListOf<String>()
        rule.setContent {
            var text by remember { mutableStateOf("Anna") }
            AppTextField(value = text, onValueChange = { text = it; changes += it }, label = { Text(fieldLabel) })
        }

        rule.onNodeWithText("Anna").performTextClearance()

        assertEquals(listOf(""), changes)
    }

    @Test
    fun `read only fields offer no text input`() {
        rule.setContent {
            AppTextField(value = "Anna", onValueChange = {}, readOnly = true, label = { Text(fieldLabel) })
        }

        rule.onNodeWithText("Anna").assert(hasSetTextAction().not())
    }

    @Test
    fun `editable fields offer text input`() {
        rule.setContent {
            AppTextField(value = "Anna", onValueChange = {}, label = { Text(fieldLabel) })
        }

        rule.onNodeWithText("Anna").assert(hasSetTextAction())
    }

    @Test
    fun `disabled fields are not enabled`() {
        rule.setContent {
            AppTextField(value = "Anna", onValueChange = {}, enabled = false, label = { Text(fieldLabel) })
        }

        rule.onNodeWithText("Anna").assertIsNotEnabled()
    }

    @Test
    fun `placeholder is shown while the value is empty`() {
        rule.setContent {
            AppTextField(value = "", onValueChange = {}, placeholder = { Text("Placeholder") })
        }

        rule.onNodeWithText("Placeholder").assertExists()
    }

    @Test
    fun `supporting text is displayed`() {
        rule.setContent {
            AppTextField(value = "", onValueChange = {}, isError = true, supportingText = { Text("Too short") })
        }

        rule.onNodeWithText("Too short").assertExists()
    }

    @Test
    fun `prefix and suffix are displayed`() {
        rule.setContent {
            AppTextField(value = "12", onValueChange = {}, prefix = { Text("~") }, suffix = { Text("kg") })
        }

        rule.onNodeWithText("~").assertExists()
        rule.onNodeWithText("kg").assertExists()
    }

    @Test
    fun `text field value overload appends at the cursor`() {
        val changes = mutableListOf<TextFieldValue>()
        rule.setContent {
            var state by remember { mutableStateOf(TextFieldValue("Anna", TextRange(4))) }
            AppTextField(
                value = state,
                onValueChange = { state = it; changes += it },
                label = { Text(fieldLabel) },
                singleLine = true,
            )
        }

        rule.onNodeWithText("Anna").performTextInput("!")

        assertEquals("Anna!", changes.last().text)
        assertEquals(TextRange(5), changes.last().selection)
    }

    @Test
    fun `text field value overload renders its text`() {
        rule.setContent {
            AppTextField(value = TextFieldValue("Berta"), onValueChange = {}, label = { Text(fieldLabel) })
        }

        rule.onNodeWithText("Berta").assertExists()
    }

    @Test
    fun `defaults colors forward overrides`() {
        var border = Color.Unspecified
        var labelColor = Color.Unspecified
        rule.setContent {
            val colors = AppTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
            )
            border = colors.focusedIndicatorColor
            labelColor = colors.focusedLabelColor
        }

        assertEquals(Color.Black, border)
        assertEquals(Color.Black, labelColor)
    }

    @Test
    fun `defaults colors resolve against the theme when nothing is overridden`() {
        var fallback = Color.Unspecified
        var overridden = Color.Unspecified
        rule.setContent {
            fallback = AppTextFieldDefaults.colors().focusedIndicatorColor
            overridden = AppTextFieldDefaults.colors(focusedBorderColor = Color.Black).focusedIndicatorColor
        }

        assertNotEquals(Color.Unspecified, fallback)
        assertNotEquals(fallback, overridden)
    }
}
