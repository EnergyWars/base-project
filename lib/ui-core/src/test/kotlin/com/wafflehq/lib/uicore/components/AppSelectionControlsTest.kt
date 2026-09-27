package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
class AppSelectionControlsTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `checkbox toggles its state`() {
        rule.setContent {
            MaterialTheme {
                var checked by remember { mutableStateOf(false) }
                AppCheckbox(checked = checked, onCheckedChange = { checked = it }, modifier = Modifier.testTag("box"))
            }
        }

        rule.onNodeWithTag("box").assertIsOff()
        rule.onNodeWithTag("box").performClick()
        rule.onNodeWithTag("box").assertIsOn()
    }

    @Test
    fun `checkbox accepts accent colors`() {
        rule.setContent {
            MaterialTheme {
                AppCheckbox(
                    checked = true,
                    onCheckedChange = {},
                    modifier = Modifier.testTag("accent"),
                    checkedColor = Color.Magenta,
                    uncheckedColor = Color.Cyan,
                )
            }
        }

        rule.onNodeWithTag("accent").assertIsOn()
    }

    @Test
    fun `disabled checkbox does not report changes`() {
        var changes = 0
        rule.setContent {
            MaterialTheme {
                AppCheckbox(checked = false, onCheckedChange = { changes++ }, enabled = false, modifier = Modifier.testTag("off"))
            }
        }

        rule.onNodeWithTag("off").performClick()

        assertEquals(0, changes)
    }

    @Test
    fun `switch toggles its state`() {
        rule.setContent {
            MaterialTheme {
                var checked by remember { mutableStateOf(true) }
                AppSwitch(checked = checked, onCheckedChange = { checked = it }, modifier = Modifier.testTag("switch"))
            }
        }

        rule.onNodeWithTag("switch").assertIsOn()
        rule.onNodeWithTag("switch").performClick()
        rule.onNodeWithTag("switch").assertIsOff()
    }

    @Test
    fun `switch colors come from the color scheme`() {
        var primary: Color = Color.Unspecified
        var checkedTrack: Color = Color.Unspecified
        rule.setContent {
            MaterialTheme {
                primary = MaterialTheme.colorScheme.primary
                checkedTrack = AppSwitchDefaults.colors().checkedTrackColor
            }
        }

        assertEquals(primary, checkedTrack)
    }

    @Test
    fun `radio button reports selection and clicks`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                Column {
                    AppRadioButton(selected = true, onClick = { clicks++ }, modifier = Modifier.testTag("selected"))
                    AppRadioButton(selected = false, onClick = { clicks++ }, modifier = Modifier.testTag("other"))
                }
            }
        }

        rule.onNodeWithTag("selected").assertIsSelected()
        rule.onNodeWithTag("other").assertIsNotSelected()
        rule.onNodeWithTag("other").performClick()

        assertEquals(1, clicks)
    }
}
