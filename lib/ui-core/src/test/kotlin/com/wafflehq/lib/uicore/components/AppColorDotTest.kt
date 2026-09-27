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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.foundation.layout.Column
import org.junit.Assert.assertEquals
import androidx.compose.ui.test.assertWidthIsEqualTo

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppColorDotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `dot uses the requested size`() {
        rule.setContent {
            MaterialTheme { AppColorDot(color = Color.Red, size = 14.dp, modifier = Modifier.testTag("dot")) }
        }

        rule.onNodeWithTag("dot").assertWidthIsEqualTo(14.dp).assertHeightIsEqualTo(14.dp)
    }

    @Test
    fun `dot with border keeps its size`() {
        rule.setContent {
            MaterialTheme {
                AppColorDot(color = Color.Red, borderColor = Color.Black, modifier = Modifier.testTag("dot"))
            }
        }

        rule.onNodeWithTag("dot").assertWidthIsEqualTo(AppColorDotDefaults.size)
    }

    @Test
    fun `legend item shows the label`() {
        rule.setContent { MaterialTheme { AppLegendItem(color = Color.Green, label = "Period") } }

        rule.onNodeWithText("Period").assertIsDisplayed()
    }

    @Test
    fun `dot with onClick reacts to a click and is not clickable otherwise`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                Column {
                    AppColorDot(color = Color.Red, modifier = Modifier.testTag("clickable"), onClick = { clicks++ })
                    AppColorDot(color = Color.Blue, modifier = Modifier.testTag("static"))
                }
            }
        }

        rule.onNodeWithTag("clickable").performClick()
        rule.onNodeWithTag("static").assertHasNoClickAction()

        assertEquals(1, clicks)
    }

    @Test
    fun `legend item with border keeps the label`() {
        rule.setContent {
            MaterialTheme { AppLegendItem(color = Color.Green, label = "Holiday", dotSize = 14.dp, borderColor = Color.Black) }
        }

        rule.onNodeWithText("Holiday").assertIsDisplayed()
    }
}
