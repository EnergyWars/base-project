package com.wafflehq.lib.quickpicker

import com.wafflehq.lib.uicore.R as UiCoreR
import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import org.junit.Assert.assertEquals
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
class QuickDateFieldTest {

    @get:Rule
    val rule = createComposeRule()

    private val date = LocalDate.of(2026, 9, 2)
    private val locale: Locale = Locale.getDefault()
    private val formatted = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))

    private fun str(id: Int) = ApplicationProvider.getApplicationContext<Application>().getString(id)

    private fun setContent(
        useQuickDateInput: Boolean = false,
        showWeekday: Boolean = false,
        onDateChange: (LocalDate) -> Unit = {}
    ) {
        rule.setContent {
            MaterialTheme {
                QuickDateField(
                    date = date,
                    label = "Start",
                    useQuickDateInput = useQuickDateInput,
                    dateDisplayFormat = DateDisplayFormat.GERMAN,
                    accent = Color.Red,
                    showWeekday = showWeekday,
                    onDateChange = onDateChange
                )
            }
        }
    }

    @Test
    fun showsTheFormattedDate() {
        setContent()

        rule.onNodeWithTag(QuickPickerTestTags.DATE_FIELD).assertTextContains(formatted)
    }

    @Test
    fun showsTheLabel() {
        setContent()

        rule.onNodeWithText("Start").assertExists()
    }

    @Test
    fun prefixesTheWeekdayWhenEnabled() {
        setContent(showWeekday = true)

        val weekday = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
        rule.onNodeWithTag(QuickPickerTestTags.DATE_FIELD).assertTextContains("$weekday, $formatted")
    }

    @Test
    fun omitsTheWeekdayWhenDisabled() {
        setContent(showWeekday = false)

        val weekday = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
        rule.onNodeWithTag(QuickPickerTestTags.DATE_FIELD).assertTextContains(formatted)
        rule.onNodeWithText("$weekday, $formatted", substring = true).assertDoesNotExist()
    }

    @Test
    fun tappingOpensTheMaterialDatePickerAndConfirmKeepsTheSameDate() {
        val changes = mutableListOf<LocalDate>()
        setContent(onDateChange = { changes += it })

        rule.onNodeWithTag(QuickPickerTestTags.DATE_FIELD).performClick()
        rule.onNodeWithText(str(R.string.quickpicker_ok)).performClick()

        assertEquals(listOf(date), changes)
    }

    @Test
    fun cancellingTheMaterialDatePickerDoesNotChangeTheDate() {
        val changes = mutableListOf<LocalDate>()
        setContent(onDateChange = { changes += it })

        rule.onNodeWithTag(QuickPickerTestTags.DATE_FIELD).performClick()
        rule.onNodeWithText(str(UiCoreR.string.uicore_cancel)).performClick()

        assertTrue(changes.isEmpty())
        rule.onNodeWithText(str(R.string.quickpicker_ok)).assertDoesNotExist()
    }

    @Test
    fun tappingWithQuickInputOpensTheQuickSheetInsteadOfTheMaterialPicker() {
        setContent(useQuickDateInput = true)

        rule.onNodeWithTag(QuickPickerTestTags.DATE_FIELD).performClick()

        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).assertExists()
    }
}
