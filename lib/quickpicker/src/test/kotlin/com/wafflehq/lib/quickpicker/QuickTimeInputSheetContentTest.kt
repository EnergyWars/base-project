package com.wafflehq.lib.quickpicker

import android.app.Application
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
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
class QuickTimeInputSheetContentTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        prefillNow: Boolean = false,
        onDismiss: () -> Unit = {},
        onConfirm: (LocalTime) -> Unit = {},
        onAllDay: (() -> Unit)? = null
    ) {
        rule.setContent {
            QuickTimeInputSheetContent(
                label = "Time",
                prefillNow = prefillNow,
                onDismiss = onDismiss,
                onConfirm = onConfirm,
                onAllDay = onAllDay
            )
        }
    }

    private fun clickDigits(vararg digits: Int) {
        digits.forEach { rule.onNodeWithTag(QuickPickerTestTags.digit(it)).performClick() }
        rule.waitForIdle()
    }

    @Test
    fun enteringFourDigitsAutoConfirms() {
        var confirmed: LocalTime? = null
        setContent(onConfirm = { confirmed = it })

        clickDigits(1, 2, 3, 4)

        assertEquals(LocalTime.of(12, 34), confirmed)
    }

    @Test
    fun confirmButtonIsDisabledWithoutAnyInput() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun partialTwoDigitEntryAlreadyAllowsConfirmingWithMinutesZero() {
        var confirmed: LocalTime? = null
        setContent(onConfirm = { confirmed = it })

        clickDigits(1, 4)
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(LocalTime.of(14, 0), confirmed)
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
    fun backspaceRemovesLastDigit() {
        var confirmed: LocalTime? = null
        setContent(onConfirm = { confirmed = it })

        clickDigits(1, 2, 3)
        rule.onNodeWithTag(QuickPickerTestTags.BACKSPACE).performClick()
        clickDigits(3, 4)

        assertEquals(LocalTime.of(12, 34), confirmed)
    }

    @Test
    fun nowChipPrefillsWithoutAutoConfirming() {
        val now = LocalTime.now()
        var confirmed: LocalTime? = null
        setContent(onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.NOW_CHIP).performClick()

        assertNull(confirmed)
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(now.hour, confirmed?.hour)
        assertEquals(now.minute, confirmed?.minute)
    }

    @Test
    fun cancelInvokesDismiss() {
        var dismissed = false
        setContent(onDismiss = { dismissed = true })

        rule.onNodeWithTag(QuickPickerTestTags.CANCEL_BUTTON).performClick()

        assertTrue(dismissed)
    }

    @Test
    fun allDayButtonIsOptional() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.ALL_DAY_BUTTON).assertDoesNotExist()
    }

    @Test
    fun allDayButtonInvokesCallbackWhenProvided() {
        var allDay = false
        setContent(onAllDay = { allDay = true })

        rule.onNodeWithTag(QuickPickerTestTags.ALL_DAY_BUTTON).performClick()

        assertTrue(allDay)
    }

    @Test
    fun prefillNowConfirmsCurrentTimeWithoutEditing() {
        val now = LocalTime.now()
        var confirmed: LocalTime? = null
        setContent(prefillNow = true, onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(now.hour, confirmed?.hour)
        assertEquals(now.minute, confirmed?.minute)
    }

    @Test
    fun prefillNowAllowsOverwritingAfterClearingWithBackspace() {
        var confirmed: LocalTime? = null
        setContent(prefillNow = true, onConfirm = { confirmed = it })

        repeat(QuickTimeInputLogic.MAX_DIGITS) {
            rule.onNodeWithTag(QuickPickerTestTags.BACKSPACE).performClick()
        }
        clickDigits(1, 2, 3, 4)

        assertEquals(LocalTime.of(12, 34), confirmed)
    }

    private val fixedNow: LocalTime = LocalTime.of(12, 0)

    private fun setContentAtFixedNow(
        onConfirm: (LocalTime) -> Unit = {}
    ) {
        rule.setContent {
            QuickTimeInputSheetContent(
                label = "Time",
                onDismiss = {},
                onConfirm = onConfirm,
                now = fixedNow
            )
        }
    }

    private fun enterRelativeMode() {
        rule.onNodeWithTag(QuickPickerTestTags.TIME_RELATIVE_TOGGLE_BUTTON).performClick()
        rule.waitForIdle()
    }

    @Test
    fun relativeModeConfirmsNowPlusMinutes() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        enterRelativeMode()
        clickDigits(3, 0)
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(LocalTime.of(12, 30), confirmed)
    }

    @Test
    fun relativeModeConfirmsNowPlusHours() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        enterRelativeMode()
        clickDigits(2)
        rule.onNodeWithTag(QuickPickerTestTags.TIME_UNIT_HOURS).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(LocalTime.of(14, 0), confirmed)
    }

    @Test
    fun relativeModeSignFlipGoesBackwards() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        enterRelativeMode()
        rule.onNodeWithTag(QuickPickerTestTags.TIME_RELATIVE_SIGN).performClick()
        clickDigits(4, 5)
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(LocalTime.of(11, 15), confirmed)
    }

    @Test
    fun relativeModeDoesNotAutoConfirmAfterFourDigits() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        enterRelativeMode()
        clickDigits(1, 2, 3, 4)

        assertNull(confirmed)
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()
        assertEquals(LocalTime.of(12, 0).plusMinutes(1234), confirmed)
    }

    @Test
    fun relativeModeWithoutDigitsConfirmsNow() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        enterRelativeMode()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(fixedNow, confirmed)
    }

    @Test
    fun relativeModeBackspaceRemovesLastAmountDigit() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        enterRelativeMode()
        clickDigits(1, 5)
        rule.onNodeWithTag(QuickPickerTestTags.BACKSPACE).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(LocalTime.of(12, 1), confirmed)
    }

    @Test
    fun relativeModeShowsResultPreview() {
        setContentAtFixedNow()

        enterRelativeMode()
        clickDigits(4, 5)

        rule.onNodeWithTag(QuickPickerTestTags.TIME_RELATIVE_PREVIEW).assertTextContains("12:45", substring = true)
        rule.onNodeWithTag(QuickPickerTestTags.TIME_RELATIVE_AMOUNT).assertTextEquals("45")
    }

    @Test
    fun relativeModeWrapsAroundMidnight() {
        var confirmed: LocalTime? = null
        rule.setContent {
            QuickTimeInputSheetContent(
                label = "Time",
                onDismiss = {},
                onConfirm = { confirmed = it },
                now = LocalTime.of(23, 30)
            )
        }

        enterRelativeMode()
        clickDigits(1)
        rule.onNodeWithTag(QuickPickerTestTags.TIME_UNIT_HOURS).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(LocalTime.of(0, 30), confirmed)
    }

    @Test
    fun leavingRelativeModeKeepsResultAndDoesNotAutoConfirm() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        enterRelativeMode()
        clickDigits(3, 0)
        enterRelativeMode()

        assertNull(confirmed)
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()
        assertEquals(LocalTime.of(12, 30), confirmed)
    }

    @Test
    fun reenteringRelativeModeResetsAmountAndUnit() {
        setContentAtFixedNow()

        enterRelativeMode()
        clickDigits(5)
        rule.onNodeWithTag(QuickPickerTestTags.TIME_UNIT_HOURS).performClick()
        enterRelativeMode()
        enterRelativeMode()

        rule.onNodeWithTag(QuickPickerTestTags.TIME_RELATIVE_AMOUNT).assertTextEquals("0")
        rule.onNodeWithTag(QuickPickerTestTags.TIME_RELATIVE_PREVIEW).assertTextContains("12:00", substring = true)
    }

    @Test
    fun nowChipLeavesRelativeMode() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        enterRelativeMode()
        clickDigits(3, 0)
        rule.onNodeWithTag(QuickPickerTestTags.NOW_CHIP).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.TIME_RELATIVE_PREVIEW).assertDoesNotExist()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(fixedNow, confirmed)
    }

    @Test
    fun absoluteEntryStillWorksAfterRelativeModeWasLeft() {
        var confirmed: LocalTime? = null
        setContentAtFixedNow { confirmed = it }

        enterRelativeMode()
        enterRelativeMode()
        repeat(QuickTimeInputLogic.MAX_DIGITS) {
            rule.onNodeWithTag(QuickPickerTestTags.BACKSPACE).performClick()
        }
        clickDigits(0, 9, 1, 5)

        assertEquals(LocalTime.of(9, 15), confirmed)
    }
}
