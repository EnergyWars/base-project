package com.wafflehq.uikit.quickpicker

import android.app.Application
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class QuickTimeFieldSheetContentTest {

    @get:Rule
    val rule = createComposeRule()

    private val prefillTime: LocalTime = LocalTime.of(9, 5)

    private fun setContent(
        onDismiss: () -> Unit = {},
        onConfirm: (LocalTime) -> Unit = {},
    ) {
        rule.setContent {
            QuickTimeFieldSheetContent(
                label = "Time",
                prefillTime = prefillTime,
                onDismiss = onDismiss,
                onConfirm = onConfirm,
            )
        }
    }

    private fun clickDigits(vararg digits: Int) {
        digits.forEach { rule.onNodeWithTag(QuickPickerTestTags.digit(it)).performClick() }
    }

    @Test
    fun confirmingWithoutEditingKeepsPrefillTime() {
        var confirmed: LocalTime? = null
        setContent(onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(prefillTime, confirmed)
    }

    @Test
    fun firstDigitClearsPrefillAndStartsFreshEntry() {
        var confirmed: LocalTime? = null
        setContent(onConfirm = { confirmed = it })

        clickDigits(1, 2, 3, 4)
        rule.waitForIdle()

        assertEquals(LocalTime.of(12, 34), confirmed)
    }

    @Test
    fun backspaceInPrefillModeClearsAndStartsFreshEntry() {
        var confirmed: LocalTime? = null
        setContent(onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.BACKSPACE).performClick()
        clickDigits(1, 2, 3, 4)
        rule.waitForIdle()

        assertEquals(LocalTime.of(12, 34), confirmed)
    }

    @Test
    fun invalidFourDigitsAreClearedAndNotConfirmed() {
        var confirmed: LocalTime? = null
        setContent(onConfirm = { confirmed = it })

        clickDigits(1, 3, 7, 0)

        assertNull(confirmed)
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun cancelInvokesDismiss() {
        var dismissed = false
        setContent(onDismiss = { dismissed = true })

        rule.onNodeWithTag(QuickPickerTestTags.CANCEL_BUTTON).performClick()

        assertTrue(dismissed)
    }
}
