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
class QuickTimeInputSheetContentTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        prefillNow: Boolean = false,
        onDismiss: () -> Unit = {},
        onConfirm: (LocalTime) -> Unit = {},
        onAllDay: (() -> Unit)? = null,
    ) {
        rule.setContent {
            QuickTimeInputSheetContent(
                label = "Time",
                prefillNow = prefillNow,
                onDismiss = onDismiss,
                onConfirm = onConfirm,
                onAllDay = onAllDay,
            )
        }
    }

    private fun clickDigits(vararg digits: Int) {
        digits.forEach { rule.onNodeWithTag(QuickPickerTestTags.digit(it)).performClick() }
    }

    @Test
    fun enteringFourDigitsAutoConfirms() {
        var confirmed: LocalTime? = null
        setContent(onConfirm = { confirmed = it })

        clickDigits(1, 2, 3, 4)
        rule.waitForIdle()

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
        rule.waitForIdle()

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
        rule.waitForIdle()

        assertEquals(LocalTime.of(12, 34), confirmed)
    }
}
