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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
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
class AppStepperTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `increase and decrease step the value`() {
        var value by mutableIntStateOf(5)
        rule.setContent {
            MaterialTheme {
                AppStepper(
                    value = value,
                    onValueChange = { value = it },
                    range = 1..10,
                    decreaseContentDescription = "minus",
                    increaseContentDescription = "plus",
                )
            }
        }

        rule.onNodeWithContentDescription("plus").performClick()
        assertEquals(6, value)
        rule.onNodeWithContentDescription("minus").performClick()
        assertEquals(5, value)
    }

    @Test
    fun `buttons are disabled at the range bounds`() {
        rule.setContent {
            MaterialTheme {
                Column {
                    AppStepper(1, {}, 1..3, decreaseContentDescription = "min-a", increaseContentDescription = "max-a")
                    AppStepper(3, {}, 1..3, decreaseContentDescription = "min-b", increaseContentDescription = "max-b")
                }
            }
        }

        rule.onNodeWithContentDescription("min-a").assertIsNotEnabled()
        rule.onNodeWithContentDescription("max-a").assertIsEnabled()
        rule.onNodeWithContentDescription("max-b").assertIsNotEnabled()
        rule.onNodeWithContentDescription("min-b").assertIsEnabled()
    }

    @Test
    fun `disabled stepper disables both buttons`() {
        rule.setContent {
            MaterialTheme {
                AppStepper(2, {}, 1..3, enabled = false, decreaseContentDescription = "minus", increaseContentDescription = "plus")
            }
        }

        rule.onNodeWithContentDescription("minus").assertIsNotEnabled()
        rule.onNodeWithContentDescription("plus").assertIsNotEnabled()
    }

    @Test
    fun `custom value text and step are used`() {
        var value = 10
        rule.setContent {
            MaterialTheme {
                AppStepper(
                    value = value,
                    onValueChange = { value = it },
                    range = 0..100,
                    step = 5,
                    valueText = "10 days",
                    decreaseContentDescription = "minus",
                    increaseContentDescription = "plus",
                )
            }
        }

        rule.onNodeWithText("10 days").assertIsDisplayed()
        rule.onNodeWithContentDescription("plus").performClick()
        assertEquals(15, value)
    }

    @Test
    fun `repeat on hold variant still reacts to a click`() {
        var value = 2
        rule.setContent {
            MaterialTheme {
                AppStepper(
                    value = value,
                    onValueChange = { value = it },
                    range = 0..5,
                    repeatOnHold = true,
                    decreaseContentDescription = "minus",
                    increaseContentDescription = "plus",
                )
            }
        }

        rule.onNodeWithContentDescription("minus").performClick()

        assertEquals(1, value)
    }

    @Test
    fun `value never leaves the range`() {
        var value = 3
        rule.setContent {
            MaterialTheme {
                AppStepper(value, { value = it }, 1..3, step = 5, decreaseContentDescription = "minus", increaseContentDescription = "plus")
            }
        }

        rule.onNodeWithContentDescription("plus").assertIsNotEnabled()
        assertFalse(value > 3)
    }
}
