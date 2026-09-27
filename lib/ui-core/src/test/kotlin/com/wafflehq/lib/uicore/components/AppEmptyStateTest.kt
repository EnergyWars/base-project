package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppEmptyStateTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `renders the text without icon or action`() {
        rule.setContent { MaterialTheme { AppEmptyState(text = "Nothing here") } }

        rule.onNodeWithText("Nothing here").assertIsDisplayed()
    }

    @Test
    fun `action button invokes the callback`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppEmptyState(text = "Empty", icon = Icons.Default.Inbox, actionLabel = "Add", onAction = { clicks++ })
            }
        }

        rule.onNodeWithText("Add").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `action is hidden when no callback is given`() {
        rule.setContent { MaterialTheme { AppEmptyState(text = "Empty", actionLabel = "Add") } }

        rule.onNodeWithText("Add").assertDoesNotExist()
    }

    @Test
    fun `content slot renders below the text`() {
        rule.setContent {
            MaterialTheme { AppEmptyState(text = "Empty", content = { Text("Extra", modifier = Modifier.testTag("extra")) }) }
        }

        rule.onNodeWithTag("extra").assertIsDisplayed()
    }

    @Test
    fun `action modifier keeps the test tag on the button`() {
        rule.setContent {
            MaterialTheme {
                AppEmptyState(text = "Empty", actionLabel = "Add", onAction = {}, actionModifier = Modifier.testTag("action"))
            }
        }

        rule.onNodeWithTag("action").assertIsDisplayed()
    }

    @Test
    fun `accent icon tint is accepted`() {
        rule.setContent {
            MaterialTheme {
                AppEmptyState(text = "Empty", icon = Icons.Default.Inbox, iconTint = androidx.compose.ui.graphics.Color.Red)
            }
        }

        rule.onNodeWithText("Empty").assertIsDisplayed()
    }
}
