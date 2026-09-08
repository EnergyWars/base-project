package com.wafflehq.uikit.quickpicker

import android.app.Application
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TimePickerDialogTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun confirmButtonInvokesOnConfirm() {
        var confirmed = false
        rule.setContent {
            val state = rememberTimePickerState(initialHour = 9, initialMinute = 30, is24Hour = true)
            TimePickerDialog(state = state, onDismiss = {}, onConfirm = { confirmed = true })
        }

        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertTrue(confirmed)
    }

    @Test
    fun cancelButtonInvokesOnDismiss() {
        var dismissed = false
        rule.setContent {
            val state = rememberTimePickerState(initialHour = 9, initialMinute = 30, is24Hour = true)
            TimePickerDialog(state = state, onDismiss = { dismissed = true }, onConfirm = {})
        }

        rule.onNodeWithTag(QuickPickerTestTags.CANCEL_BUTTON).performClick()

        assertTrue(dismissed)
    }
}
