package com.wafflehq.lib.quickpicker

import android.app.Application
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
        onConfirm: (LocalTime) -> Unit = {}
    ) {
        rule.setContent {
            QuickTimeFieldSheetContent(
                label = "Time",
                prefillTime = prefillTime,
                onDismiss = onDismiss,
                onConfirm = onConfirm
            )
        }
    }

    private fun clickDigits(vararg digits: Int) {
        digits.forEach { rule.onNodeWithTag(QuickPickerTestTags.digit(it)).performClick() }
        rule.waitForIdle()
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

        assertEquals(LocalTime.of(12, 34), confirmed)
    }

    @Test
    fun backspaceInPrefillModeClearsAndStartsFreshEntry() {
        var confirmed: LocalTime? = null
        setContent(onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.BACKSPACE).performClick()
        clickDigits(1, 2, 3, 4)

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

    private val fixedNow: LocalTime = LocalTime.of(12, 0)

    private fun setContentAtFixedNow(onConfirm: (LocalTime) -> Unit = {}) {
        rule.setContent {
            QuickTimeFieldSheetContent(
                label = "Time",
                prefillTime = prefillTime,
                onDismiss = {},
                onConfirm = onConfirm,
                now = fixedNow
            )
        }
    }

    private fun toggleRelativeMode() {
        rule.onNodeWithTag(QuickPickerTestTags.TIME_RELATIVE_TOGGLE_BUTTON).performClick()
        rule.waitForIdle()
    }

    @Test
    fun relativeModeConfirmsNowPlusMinutes() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        toggleRelativeMode()
        clickDigits(1, 5)
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(LocalTime.of(12, 15), confirmed)
    }

    @Test
    fun relativeModeConfirmsNowMinusHours() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        toggleRelativeMode()
        rule.onNodeWithTag(QuickPickerTestTags.TIME_RELATIVE_SIGN).performClick()
        clickDigits(3)
        rule.onNodeWithTag(QuickPickerTestTags.TIME_UNIT_HOURS).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(LocalTime.of(9, 0), confirmed)
    }

    @Test
    fun relativeModeWithoutDigitsConfirmsNowInsteadOfPrefill() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        toggleRelativeMode()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(fixedNow, confirmed)
    }

    @Test
    fun relativeModeDoesNotAutoConfirmAfterFourDigits() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        toggleRelativeMode()
        clickDigits(1, 2, 3, 4)

        assertNull(confirmed)
    }

    @Test
    fun relativeModeBackspaceRemovesLastAmountDigit() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        toggleRelativeMode()
        clickDigits(2, 0)
        rule.onNodeWithTag(QuickPickerTestTags.BACKSPACE).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(LocalTime.of(12, 2), confirmed)
    }

    @Test
    fun leavingRelativeModeKeepsResultAndDoesNotAutoConfirm() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        toggleRelativeMode()
        clickDigits(3, 0)
        toggleRelativeMode()

        assertNull(confirmed)
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()
        assertEquals(LocalTime.of(12, 30), confirmed)
    }

    @Test
    fun typingAfterLeavingRelativeModeStartsFromBackspacedResult() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        toggleRelativeMode()
        clickDigits(3, 0)
        toggleRelativeMode()
        repeat(QuickTimeInputLogic.MAX_DIGITS) {
            rule.onNodeWithTag(QuickPickerTestTags.BACKSPACE).performClick()
        }
        clickDigits(0, 8, 4, 5)

        assertEquals(LocalTime.of(8, 45), confirmed)
    }

    @Test
    fun relativeModeShowsResultPreview() {
        setContentAtFixedNow()

        toggleRelativeMode()
        clickDigits(9, 0)

        rule.onNodeWithTag(QuickPickerTestTags.TIME_RELATIVE_PREVIEW).assertTextContains("13:30", substring = true)
    }
}
