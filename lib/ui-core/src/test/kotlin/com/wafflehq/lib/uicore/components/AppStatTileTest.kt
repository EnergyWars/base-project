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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppStatTileTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `renders value and label for every emphasis`() {
        rule.setContent {
            MaterialTheme {
                Column {
                    AppStatTileEmphasis.entries.forEach { emphasis ->
                        AppStatTile(value = "v-$emphasis", label = "l-$emphasis", emphasis = emphasis)
                    }
                }
            }
        }

        AppStatTileEmphasis.entries.forEach {
            rule.onNodeWithText("v-$it").assertIsDisplayed()
            rule.onNodeWithText("l-$it").assertIsDisplayed()
        }
    }

    @Test
    fun `label first swaps the vertical order`() {
        rule.setContent {
            MaterialTheme {
                Column {
                    AppStatTile(value = "value-a", label = "label-a", modifier = Modifier.testTag("a"))
                    AppStatTile(value = "value-b", label = "label-b", labelFirst = true, modifier = Modifier.testTag("b"))
                }
            }
        }

        val valueTop = rule.onNodeWithText("value-a").fetchSemanticsNode().boundsInRoot.top
        val labelTop = rule.onNodeWithText("label-a").fetchSemanticsNode().boundsInRoot.top
        val valueTopB = rule.onNodeWithText("value-b").fetchSemanticsNode().boundsInRoot.top
        val labelTopB = rule.onNodeWithText("label-b").fetchSemanticsNode().boundsInRoot.top
        assertEquals(true, valueTop < labelTop)
        assertEquals(true, labelTopB < valueTopB)
    }
}
