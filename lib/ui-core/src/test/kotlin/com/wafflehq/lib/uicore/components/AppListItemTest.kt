package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
class AppListItemTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `renders every slot`() {
        rule.setContent {
            MaterialTheme {
                AppListItem(
                    headlineContent = { Text("Headline") },
                    overlineContent = { Text("Overline") },
                    supportingContent = { Text("Supporting") },
                    leadingContent = { Text("Leading") },
                    trailingContent = { Text("Trailing") },
                )
            }
        }

        listOf("Headline", "Overline", "Supporting", "Leading", "Trailing").forEach {
            rule.onNodeWithText(it).assertIsDisplayed()
        }
    }

    @Test
    fun `row is clickable through the modifier`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppListItem(
                    headlineContent = { Text("Tap") },
                    modifier = Modifier.testTag("row").clickable { clicks++ },
                )
            }
        }

        rule.onNodeWithTag("row").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `accepts an explicit container color`() {
        rule.setContent {
            MaterialTheme {
                AppListItem(headlineContent = { Text("Transparent") }, containerColor = Color.Transparent)
            }
        }

        rule.onNodeWithText("Transparent").assertIsDisplayed()
    }
}
