package com.wafflehq.lib.quickpicker

import android.app.Application
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

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MonthYearPickerDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private var confirmed: Pair<Int, Int>? = null
    private var dismissed = false

    private fun show(year: Int = 2026, month: Int = 4) {
        confirmed = null
        dismissed = false
        rule.setContent {
            MonthYearPickerDialog(
                initialYear = year,
                initialMonth = month,
                onDismiss = { dismissed = true },
                onConfirm = { y, m -> confirmed = y to m }
            )
        }
    }

    @Test
    fun showsInitialYear() {
        show(year = 2026)

        rule.onNodeWithTag(QuickPickerTestTags.YEAR_LABEL).assertTextEquals("2026")
    }

    @Test
    fun nextYearArrowIncrementsYear() {
        show(year = 2026)

        rule.onNodeWithTag(QuickPickerTestTags.NEXT_YEAR_ARROW).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.NEXT_YEAR_ARROW).performClick()

        rule.onNodeWithTag(QuickPickerTestTags.YEAR_LABEL).assertTextEquals("2028")
    }

    @Test
    fun previousYearArrowDecrementsYear() {
        show(year = 2026)

        rule.onNodeWithTag(QuickPickerTestTags.PREVIOUS_YEAR_ARROW).performClick()

        rule.onNodeWithTag(QuickPickerTestTags.YEAR_LABEL).assertTextEquals("2025")
    }

    @Test
    fun confirmWithoutChangesReturnsInitialValues() {
        show(year = 2026, month = 4)

        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(2026 to 4, confirmed)
    }

    @Test
    fun confirmReturnsChangedYearAndSelectedMonth() {
        show(year = 2026, month = 4)

        rule.onNodeWithTag(QuickPickerTestTags.PREVIOUS_YEAR_ARROW).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.monthButton(11)).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(2025 to 11, confirmed)
    }

    @Test
    fun yearChangeAcrossManyStepsIsReflectedInConfirm() {
        show(year = 2026, month = 1)

        repeat(3) { rule.onNodeWithTag(QuickPickerTestTags.NEXT_YEAR_ARROW).performClick() }
        rule.onNodeWithTag(QuickPickerTestTags.PREVIOUS_YEAR_ARROW).performClick()
        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(2028 to 1, confirmed)
    }

    @Test
    fun cancelInvokesDismissWithoutConfirm() {
        show()

        rule.onNodeWithTag(QuickPickerTestTags.CANCEL_BUTTON).performClick()

        assertTrue(dismissed)
        assertNull(confirmed)
    }
}
