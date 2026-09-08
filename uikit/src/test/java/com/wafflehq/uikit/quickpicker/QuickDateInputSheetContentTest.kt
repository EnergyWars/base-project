package com.wafflehq.uikit.quickpicker

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.uikit.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class QuickDateInputSheetContentTest {

    @get:Rule
    val rule = createComposeRule()

    private val today: LocalDate = LocalDate.of(2026, 9, 2)
    private val locale: Locale = Locale.getDefault()
    private val longFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

    private fun str(id: Int) = ApplicationProvider.getApplicationContext<Application>().getString(id)

    private fun weekday(day: LocalDate) = day.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)

    private fun setContent(
        showWeekday: Boolean = true,
        initialDate: LocalDate? = null,
        onNotNow: (() -> Unit)? = null,
        onDismiss: () -> Unit = {},
        onConfirm: (LocalDate) -> Unit = {},
    ) {
        rule.setContent {
            QuickDateInputSheetContent(
                label = "Date",
                format = DateDisplayFormat.GERMAN,
                showWeekday = showWeekday,
                today = today,
                initialDate = initialDate,
                onDismiss = onDismiss,
                onNotNow = onNotNow,
                onConfirm = onConfirm,
            )
        }
    }

    @Test
    fun showsWeekNavigationButtonsAndTodayButton() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.LAST_WEEK_BUTTON).assertIsDisplayed()
        rule.onNodeWithTag(QuickPickerTestTags.TODAY_BUTTON).assertIsDisplayed()
        rule.onNodeWithTag(QuickPickerTestTags.NEXT_WEEK_BUTTON).assertIsDisplayed()
    }

    @Test
    fun dayChipsAreHiddenUntilAWeekButtonIsExpanded() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.dayChip(today.plusDays(1))).assertDoesNotExist()
        rule.onNodeWithTag(QuickPickerTestTags.dayChip(today.minusDays(1))).assertDoesNotExist()
    }

    @Test
    fun nextWeekButtonExpandsTomorrowThroughPlusSevenDays() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.NEXT_WEEK_BUTTON).performClick()

        (1L..7L).forEach { offset ->
            rule.onNodeWithTag(QuickPickerTestTags.dayChip(today.plusDays(offset))).assertIsDisplayed()
        }
        rule.onNodeWithTag(QuickPickerTestTags.dayChip(today)).assertDoesNotExist()
    }

    @Test
    fun lastWeekButtonExpandsYesterdayThroughMinusSevenDays() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.LAST_WEEK_BUTTON).performClick()

        (1L..7L).forEach { offset ->
            rule.onNodeWithTag(QuickPickerTestTags.dayChip(today.minusDays(offset))).assertIsDisplayed()
        }
        rule.onNodeWithTag(QuickPickerTestTags.dayChip(today)).assertDoesNotExist()
    }

    @Test
    fun onlyOneExpandedWeekIsShownAtATime() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.NEXT_WEEK_BUTTON).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.dayChip(today.plusDays(1))).assertIsDisplayed()

        rule.onNodeWithTag(QuickPickerTestTags.LAST_WEEK_BUTTON).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.dayChip(today.plusDays(1))).assertDoesNotExist()
        rule.onNodeWithTag(QuickPickerTestTags.dayChip(today.minusDays(1))).assertIsDisplayed()
    }

    @Test
    fun weekChipsShowWeekdayAndDayOfMonth() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.NEXT_WEEK_BUTTON).performClick()
        val day = today.plusDays(4)
        rule.onNodeWithTag(QuickPickerTestTags.dayChip(day))
            .assertTextContains(weekday(day), substring = true)
        rule.onNodeWithTag(QuickPickerTestTags.dayChip(day))
            .assertTextContains(day.dayOfMonth.toString(), substring = true)
    }

    @Test
    fun tappingDayChipFillsDateWithoutConfirming() {
        var confirmed: LocalDate? = null
        setContent(onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.NEXT_WEEK_BUTTON).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.dayChip(today.plusDays(4))).performClick()

        assertNull(confirmed)
        rule.onNodeWithTag(QuickPickerTestTags.DATE_DISPLAY).assertTextEquals(
            QuickDateInputLogic.formatForInput(today.plusDays(4), DateDisplayFormat.GERMAN),
        )

        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()
        assertEquals(today.plusDays(4), confirmed)
    }

    @Test
    fun tappingLastWeekDayChipFillsDateWithoutConfirming() {
        var confirmed: LocalDate? = null
        setContent(onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.LAST_WEEK_BUTTON).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.dayChip(today.minusDays(6))).performClick()

        assertNull(confirmed)
        rule.onNodeWithTag(QuickPickerTestTags.DATE_DISPLAY).assertTextEquals(
            QuickDateInputLogic.formatForInput(today.minusDays(6), DateDisplayFormat.GERMAN),
        )

        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()
        assertEquals(today.minusDays(6), confirmed)
    }

    @Test
    fun tappingTodayButtonFillsTodayWithoutConfirming() {
        var confirmed: LocalDate? = null
        setContent(onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.TODAY_BUTTON).performClick()

        assertNull(confirmed)
        rule.onNodeWithTag(QuickPickerTestTags.DATE_DISPLAY).assertTextEquals(
            QuickDateInputLogic.formatForInput(today, DateDisplayFormat.GERMAN),
        )

        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()
        assertEquals(today, confirmed)
    }

    @Test
    fun tappingDayChipCollapsesTheExpandedWeek() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.NEXT_WEEK_BUTTON).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.dayChip(today.plusDays(4))).performClick()

        rule.onNodeWithTag(QuickPickerTestTags.dayChip(today.plusDays(1))).assertDoesNotExist()
    }

    @Test
    fun relativeToggleButtonEntersRelativeModeAndConfirmsOffsetFromToday() {
        var confirmed: LocalDate? = null
        setContent(onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.RELATIVE_TOGGLE_BUTTON).performClick()
        rule.onNodeWithText(str(R.string.quickpicker_days_unit)).assertIsDisplayed()
        rule.onNodeWithTag(QuickPickerTestTags.SEPARATOR).assertIsNotEnabled()
        rule.onNodeWithTag(QuickPickerTestTags.digit(3)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(today.plusDays(3), confirmed)
    }

    @Test
    fun relativeToggleSignFlipGoesBackwardsFromToday() {
        var confirmed: LocalDate? = null
        setContent(onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.RELATIVE_TOGGLE_BUTTON).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.TODAY_SIGN_TOGGLE).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.digit(1)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.digit(2)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(today.minusDays(12), confirmed)
    }

    @Test
    fun relativeToggleUsesTypedDateAsBaseInsteadOfToday() {
        var confirmed: LocalDate? = null
        setContent(onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.digit(2)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.digit(0)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.SEPARATOR).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.digit(9)).performClick()
        val typedDate = LocalDate.of(2026, 9, 20)

        rule.onNodeWithTag(QuickPickerTestTags.RELATIVE_TOGGLE_BUTTON).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.digit(5)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(typedDate.plusDays(5), confirmed)
    }

    @Test
    fun relativeToggleUsesInitialDateAsBaseWhenNothingTyped() {
        var confirmed: LocalDate? = null
        val initial = LocalDate.of(2026, 10, 1)
        setContent(initialDate = initial, onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.RELATIVE_TOGGLE_BUTTON).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(initial, confirmed)
    }

    @Test
    fun togglingRelativeModeOffWithoutTypingKeepsPreviousBaseDate() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.RELATIVE_TOGGLE_BUTTON).performClick()
        rule.onNodeWithText(str(R.string.quickpicker_days_unit)).assertIsDisplayed()
        rule.onNodeWithTag(QuickPickerTestTags.RELATIVE_TOGGLE_BUTTON).performClick()

        rule.onNodeWithText(str(R.string.quickpicker_days_unit)).assertDoesNotExist()
        rule.onNodeWithTag(QuickPickerTestTags.SEPARATOR).assertIsEnabled()
        rule.onNodeWithTag(QuickPickerTestTags.DATE_DISPLAY).assertTextEquals(
            QuickDateInputLogic.formatForInput(today, DateDisplayFormat.GERMAN),
        )
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).assertIsEnabled()
    }

    @Test
    fun togglingRelativeModeOffAfterTypingOffsetKeepsResultingDate() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.RELATIVE_TOGGLE_BUTTON).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.digit(3)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.RELATIVE_TOGGLE_BUTTON).performClick()

        rule.onNodeWithTag(QuickPickerTestTags.DATE_DISPLAY).assertTextEquals(
            QuickDateInputLogic.formatForInput(today.plusDays(3), DateDisplayFormat.GERMAN),
        )
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).assertIsEnabled()
    }

    @Test
    fun relativeModeWithoutDigitsConfirmsToday() {
        var confirmed: LocalDate? = null
        setContent(onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.RELATIVE_TOGGLE_BUTTON).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(today, confirmed)
    }

    @Test
    fun restoreButtonDoesNotExistWithoutInitialDate() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.RESTORE_BUTTON).assertDoesNotExist()
    }

    @Test
    fun restoreButtonFillsInitialDateWithoutClosing() {
        var confirmed: LocalDate? = null
        val initial = LocalDate.of(2026, 12, 24)
        setContent(initialDate = initial, onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.RESTORE_BUTTON).performClick()

        assertNull(confirmed)
        rule.onNodeWithTag(QuickPickerTestTags.DATE_DISPLAY).assertTextEquals("24.12.2026")
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).assertIsEnabled()
    }

    @Test
    fun restoreButtonThenArrowsAdjustFromInitialDate() {
        var confirmed: LocalDate? = null
        val initial = LocalDate.of(2026, 12, 24)
        setContent(initialDate = initial, onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.RESTORE_BUTTON).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.NEXT_DAY_ARROW).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(initial.plusDays(1), confirmed)
    }

    @Test
    fun arrowsDoNotExistWithoutAValidValue() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.PREVIOUS_DAY_ARROW).assertDoesNotExist()
        rule.onNodeWithTag(QuickPickerTestTags.NEXT_DAY_ARROW).assertDoesNotExist()
    }

    @Test
    fun arrowsAppearOnceAValidDateIsTypedAndShiftByOneDayRepeatedly() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.digit(1)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.digit(5)).performClick()
        val typed = LocalDate.of(2026, 9, 15)
        rule.onNodeWithTag(QuickPickerTestTags.DATE_DISPLAY).assertTextEquals("15.")

        rule.onNodeWithTag(QuickPickerTestTags.NEXT_DAY_ARROW).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.DATE_DISPLAY).assertTextEquals(
            QuickDateInputLogic.formatForInput(typed.plusDays(1), DateDisplayFormat.GERMAN),
        )

        rule.onNodeWithTag(QuickPickerTestTags.PREVIOUS_DAY_ARROW).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.PREVIOUS_DAY_ARROW).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.DATE_DISPLAY).assertTextEquals(
            QuickDateInputLogic.formatForInput(typed.minusDays(1), DateDisplayFormat.GERMAN),
        )
    }

    @Test
    fun arrowShiftAcrossMonthBoundaryConfirmsCorrectDate() {
        var confirmed: LocalDate? = null
        setContent(onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.digit(3)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.digit(0)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.digit(9)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.NEXT_DAY_ARROW).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(LocalDate.of(2026, 10, 1), confirmed)
    }

    @Test
    fun previewShowsWeekdayWhileTypingWhenEnabled() {
        setContent(showWeekday = true)

        rule.onNodeWithTag(QuickPickerTestTags.digit(1)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.digit(5)).performClick()

        val expected = LocalDate.of(2026, 9, 15)
        rule.onNodeWithTag(QuickPickerTestTags.DATE_PREVIEW)
            .assertTextEquals("${weekday(expected)}, ${expected.format(longFormat)}")
    }

    @Test
    fun previewOmitsWeekdayWhenDisabled() {
        setContent(showWeekday = false)

        rule.onNodeWithTag(QuickPickerTestTags.digit(1)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.digit(5)).performClick()

        val expected = LocalDate.of(2026, 9, 15)
        rule.onNodeWithTag(QuickPickerTestTags.DATE_PREVIEW).assertTextEquals(expected.format(longFormat))
    }

    @Test
    fun previewShowsWeekdayInRelativeMode() {
        setContent(showWeekday = true)

        rule.onNodeWithTag(QuickPickerTestTags.RELATIVE_TOGGLE_BUTTON).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.digit(2)).performClick()

        val expected = today.plusDays(2)
        rule.onNodeWithTag(QuickPickerTestTags.DATE_PREVIEW)
            .assertTextEquals("${weekday(expected)}, ${expected.format(longFormat)}")
    }

    @Test
    fun previewIsBlankWithoutInput() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.DATE_PREVIEW).assertTextEquals(" ")
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun backspaceRemovesLastDigitAndClearsPreview() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.digit(1)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.DATE_DISPLAY).assertTextEquals("1")
        rule.onNodeWithTag(QuickPickerTestTags.BACKSPACE).performClick()

        rule.onNodeWithTag(QuickPickerTestTags.DATE_DISPLAY).assertTextEquals("−")
        rule.onNodeWithTag(QuickPickerTestTags.DATE_PREVIEW).assertTextEquals(" ")
    }

    @Test
    fun cancelInvokesDismissAndNotNowIsOptional() {
        var dismissed = false
        setContent(onDismiss = { dismissed = true })

        rule.onNodeWithTag(QuickPickerTestTags.NOT_NOW_BUTTON).assertDoesNotExist()
        rule.onNodeWithTag(QuickPickerTestTags.CANCEL_BUTTON).performClick()

        assertTrue(dismissed)
    }

    @Test
    fun notNowButtonInvokesCallbackWhenProvided() {
        var notNow = false
        var confirmed: LocalDate? = null
        setContent(onNotNow = { notNow = true }, onConfirm = { confirmed = it })

        rule.onNodeWithTag(QuickPickerTestTags.NOT_NOW_BUTTON).performClick()

        assertTrue(notNow)
        assertNull(confirmed)
    }
}
