package com.wafflehq.base.ui.library.demos

import android.app.Application
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.quickpicker.DateDisplayFormat
import com.wafflehq.lib.quickpicker.QuickPickerTestTags
import com.wafflehq.lib.quickpicker.R as QuickPickerR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class QuickPickerDemoTest : LibraryDemoTest() {

    private val today = LocalDate.of(2026, 3, 10)

    private fun showDemo() = show { QuickPickerDemo(today = today, initialTime = LocalTime.of(9, 30)) }

    @Test
    fun rendersTheFieldsWithTheInitialValues() {
        showDemo()

        node(DemoTags.section("quickpicker")).assertExists()
        assertTagText(QuickPickerDemoTags.DATE_VALUE, "10.03.2026")
        assertTagText(QuickPickerTestTags.TIME_FIELD, "09:30")
    }

    @Test
    fun internationalFormatChangesTheDateField() {
        showDemo()

        click(QuickPickerDemoTags.format(DateDisplayFormat.INTERNATIONAL))

        assertTagText(QuickPickerDemoTags.DATE_VALUE, "2026-03-10")
    }

    @Test
    fun dateFieldOpensTheQuickInputSheet() {
        showDemo()

        click(QuickPickerDemoTags.DATE_VALUE)

        node(QuickPickerTestTags.CONFIRM_BUTTON).assertExists()
    }

    @Test
    fun timeFieldOpensTheQuickInputSheet() {
        showDemo()

        click(QuickPickerTestTags.TIME_FIELD)

        node(QuickPickerTestTags.CONFIRM_BUTTON).assertExists()
    }

    @Test
    fun disablingQuickInputFallsBackToTheSystemPicker() {
        showDemo()
        click(QuickPickerDemoTags.QUICK_SWITCH)

        click(QuickPickerDemoTags.DATE_VALUE)

        rule.onNodeWithText(string(QuickPickerR.string.quickpicker_ok)).assertExists()
        node(QuickPickerTestTags.CONFIRM_BUTTON).assertDoesNotExist()
    }

    @Test
    fun monthPickerReportsTheConfirmedMonth() {
        showDemo()

        click(QuickPickerDemoTags.MONTH_BUTTON)
        node(QuickPickerTestTags.monthButton(5)).performClick()
        node(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        waitForTag(QuickPickerDemoTags.MONTH_RESULT)
        assertTagText(
            QuickPickerDemoTags.MONTH_RESULT,
            string(R.string.libex_quickpicker_month_result, QuickPickerDemoLogic.monthLabel(2026, 5)),
        )
    }

    @Test
    fun yearPickerReportsTheChosenYear() {
        showDemo()

        click(QuickPickerDemoTags.YEAR_BUTTON)
        rule.onNodeWithText("2026").performClick()

        waitForTag(QuickPickerDemoTags.YEAR_RESULT)
        assertTagText(QuickPickerDemoTags.YEAR_RESULT, string(R.string.libex_quickpicker_year_result, 2026))
    }

    @Test
    fun parserExplainsGermanDateInput() {
        showDemo()

        replaceText(QuickPickerDemoTags.DATE_INPUT, "24.12.25")

        assertTagText(QuickPickerDemoTags.DATE_PARSED, "24.12.2025")
    }

    @Test
    fun parserFlagsInvalidInputAndAcceptsTimeDigits() {
        showDemo()

        replaceText(QuickPickerDemoTags.DATE_INPUT, "99.99")
        assertTagText(QuickPickerDemoTags.DATE_PARSED, string(R.string.libex_quickpicker_invalid))

        replaceText(QuickPickerDemoTags.TIME_INPUT, "0930")
        assertTagText(QuickPickerDemoTags.TIME_PARSED, "09:30")
    }

    @Test
    fun logicParsesAndFormatsDates() {
        val reference = LocalDate.of(2026, 3, 10)

        assertEquals(LocalDate.of(2025, 12, 24), QuickPickerDemoLogic.parseDate("24.12.25", reference, DateDisplayFormat.GERMAN))
        assertEquals(LocalDate.of(2026, 3, 24), QuickPickerDemoLogic.parseDate("24", reference, DateDisplayFormat.GERMAN))
        assertEquals(LocalDate.of(2025, 12, 24), QuickPickerDemoLogic.parseDate("2025-12-24", reference, DateDisplayFormat.INTERNATIONAL))
        assertNull(QuickPickerDemoLogic.parseDate("31.02.", reference, DateDisplayFormat.GERMAN))
        assertEquals("24.12.2025", QuickPickerDemoLogic.formatDate(LocalDate.of(2025, 12, 24), DateDisplayFormat.GERMAN))
        assertEquals("2025-12-24", QuickPickerDemoLogic.formatDate(LocalDate.of(2025, 12, 24), DateDisplayFormat.INTERNATIONAL))
    }

    @Test
    fun logicParsesTimeDigitsAndFormatsMonths() {
        assertEquals(LocalTime.of(9, 30), QuickPickerDemoLogic.parseTime("09:30"))
        assertEquals(LocalTime.of(18, 45), QuickPickerDemoLogic.parseTime("1845"))
        assertNull(QuickPickerDemoLogic.parseTime("2599"))
        assertEquals("March 2026", QuickPickerDemoLogic.monthLabel(2026, 3, Locale.ENGLISH))
        assertEquals("März 2026", QuickPickerDemoLogic.monthLabel(2026, 3, Locale.GERMAN))
    }
}
