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
class AppSearchFieldTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `shows the placeholder while empty and no clear button`() {
        rule.setContent {
            MaterialTheme { AppSearchField(query = "", onQueryChange = {}, placeholder = "Search", clearContentDescription = "Clear") }
        }

        rule.onNodeWithText("Search").assertIsDisplayed()
        rule.onNodeWithContentDescription("Clear").assertDoesNotExist()
    }

    @Test
    fun `clear button resets the query`() {
        var query = "abc"
        rule.setContent {
            MaterialTheme {
                AppSearchField(query = query, onQueryChange = { query = it }, placeholder = "Search", clearContentDescription = "Clear")
            }
        }

        rule.onNodeWithContentDescription("Clear").performClick()

        assertEquals("", query)
    }

    @Test
    fun `typing forwards the text`() {
        var query = ""
        rule.setContent {
            MaterialTheme {
                AppSearchField(
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = "Search",
                    modifier = Modifier.testTag("field"),
                )
            }
        }

        rule.onNodeWithTag("field").performTextInput("milk")

        assertEquals("milk", query)
    }

    @Test
    fun `blank query hides the clear button`() {
        rule.setContent {
            MaterialTheme { AppSearchField(query = "  ", onQueryChange = {}, placeholder = "Search", clearContentDescription = "Clear") }
        }

        rule.onNodeWithContentDescription("Clear").assertDoesNotExist()
    }
}
