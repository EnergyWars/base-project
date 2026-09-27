package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.window.DialogProperties
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.theme.AppRadius
import androidx.compose.foundation.shape.RoundedCornerShape
import org.junit.Assert.assertEquals
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
class AppDialogTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `renders icon title text and both buttons`() {
        rule.setContent {
            MaterialTheme {
                AppDialog(
                    onDismissRequest = {},
                    icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.testTag("icon")) },
                    title = { Text("Dialog title") },
                    text = { Text("Dialog body") },
                    confirmButton = {
                        AppButton(text = "Confirm", role = AppButtonRole.Primary, variant = AppButtonVariant.Text, onClick = {})
                    },
                    dismissButton = {
                        AppButton(text = "Cancel", role = AppButtonRole.Neutral, variant = AppButtonVariant.Text, onClick = {})
                    },
                )
            }
        }

        rule.onNodeWithTag("icon").assertIsDisplayed()
        rule.onNodeWithText("Dialog title").assertIsDisplayed()
        rule.onNodeWithText("Dialog body").assertIsDisplayed()
        rule.onNodeWithText("Confirm").assertIsDisplayed()
        rule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun `confirm and dismiss buttons invoke their own callbacks only`() {
        var confirmed = 0
        var dismissed = 0
        rule.setContent {
            MaterialTheme {
                AppDialog(
                    onDismissRequest = {},
                    title = { Text("Title") },
                    confirmButton = {
                        AppButton(text = "Confirm", role = AppButtonRole.Primary, variant = AppButtonVariant.Text, onClick = { confirmed++ })
                    },
                    dismissButton = {
                        AppButton(text = "Cancel", role = AppButtonRole.Neutral, variant = AppButtonVariant.Text, onClick = { dismissed++ })
                    },
                )
            }
        }

        rule.onNodeWithText("Confirm").performClick()
        assertEquals(1, confirmed)
        assertEquals(0, dismissed)

        rule.onNodeWithText("Cancel").performClick()
        assertEquals(1, confirmed)
        assertEquals(1, dismissed)
    }

    @Test
    fun `optional slots may be omitted`() {
        rule.setContent {
            MaterialTheme {
                AppDialog(
                    onDismissRequest = {},
                    confirmButton = {
                        AppButton(text = "Only action", role = AppButtonRole.Primary, variant = AppButtonVariant.Text, onClick = {})
                    },
                )
            }
        }

        rule.onNodeWithText("Only action").assertIsDisplayed()
    }

    @Test
    fun `default properties match Material and the strict variant stays opt-in`() {
        assertTrue(DialogProperties().dismissOnClickOutside)
        assertTrue(!AppDialogDefaults.properties.dismissOnClickOutside)
    }

    @Test
    fun `chassis values come from AppDialogDefaults`() {
        assertEquals(RoundedCornerShape(AppRadius.dialog), AppDialogDefaults.shape)
        assertEquals(1, AppDialogDefaults.tonalElevation.value.toInt())
    }

    @Test
    fun `content recomposes with its state`() {
        rule.setContent {
            MaterialTheme {
                var counter by remember { mutableStateOf(0) }
                AppDialog(
                    onDismissRequest = {},
                    title = { Text("Count $counter") },
                    confirmButton = {
                        AppButton(text = "Increment", role = AppButtonRole.Primary, variant = AppButtonVariant.Text, onClick = { counter++ })
                    },
                )
            }
        }

        rule.onNodeWithText("Count 0").assertIsDisplayed()
        rule.onNodeWithText("Increment").performClick()
        rule.onNodeWithText("Count 1").assertIsDisplayed()
    }
}
