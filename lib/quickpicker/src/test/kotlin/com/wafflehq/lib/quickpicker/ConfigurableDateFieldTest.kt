package com.wafflehq.lib.quickpicker

import com.wafflehq.lib.uicore.R as UiCoreR
import android.app.Application
import androidx.compose.material3.MaterialTheme
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
class ConfigurableDateFieldTest {

    @get:Rule
    val rule = createComposeRule()

    private val date = LocalDate.of(2026, 9, 2)
    private val tag = "date_field_under_test"

    private fun str(id: Int) = ApplicationProvider.getApplicationContext<Application>().getString(id)

    private fun setContent(
        value: LocalDate? = date,
        useQuickDateInput: Boolean = false,
        format: DateDisplayFormat = DateDisplayFormat.GERMAN,
        showWeekday: Boolean = false,
        formatter: DateTimeFormatter? = null,
        emptyText: String = "",
        isError: Boolean = false,
        supportingText: String? = null,
        onDateChange: (LocalDate) -> Unit = {}
    ) {
        rule.setContent {
            MaterialTheme {
                ConfigurableDateField(
                    date = value,
                    label = "Start",
                    useQuickDateInput = useQuickDateInput,
                    dateDisplayFormat = format,
                    onDateChange = onDateChange,
                    showWeekday = showWeekday,
                    formatter = formatter,
                    emptyText = emptyText,
                    isError = isError,
                    supportingText = supportingText,
                    testTag = tag
                )
            }
        }
    }

    @Test
    fun showsTheDateInTheGermanPatternByDefault() {
        setContent()

        rule.onNodeWithTag(tag).assertTextContains("02.09.2026")
    }

    @Test
    fun showsTheDateInTheInternationalPattern() {
        setContent(format = DateDisplayFormat.INTERNATIONAL)

        rule.onNodeWithTag(tag).assertTextContains("2026-09-02")
    }

    @Test
    fun usesTheGivenFormatterInsteadOfThePattern() {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
        setContent(formatter = formatter)

        rule.onNodeWithTag(tag).assertTextContains(date.format(formatter))
    }

    @Test
    fun prefixesTheWeekdayWhenEnabled() {
        setContent(showWeekday = true)

        val weekday = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        rule.onNodeWithTag(tag).assertTextContains("$weekday, 02.09.2026")
    }

    @Test
    fun showsTheEmptyTextWithoutADate() {
        setContent(value = null, emptyText = "Open")

        rule.onNodeWithTag(tag).assertTextContains("Open")
    }

    @Test
    fun showsLabelAndSupportingText() {
        setContent(isError = true, supportingText = "Invalid range")

        rule.onNodeWithText("Start").assertExists()
        rule.onNodeWithText("Invalid range").assertExists()
    }

    @Test
    fun confirmingTheMaterialPickerKeepsTheSameDate() {
        val changes = mutableListOf<LocalDate>()
        setContent(onDateChange = { changes += it })

        rule.onNodeWithTag(tag).performClick()
        rule.onNodeWithText(str(R.string.quickpicker_ok)).performClick()

        assertEquals(listOf(date), changes)
    }

    @Test
    fun cancellingTheMaterialPickerDoesNotChangeTheDate() {
        val changes = mutableListOf<LocalDate>()
        setContent(onDateChange = { changes += it })

        rule.onNodeWithTag(tag).performClick()
        rule.onNodeWithText(str(UiCoreR.string.uicore_cancel)).performClick()

        assertTrue(changes.isEmpty())
        rule.onNodeWithText(str(R.string.quickpicker_ok)).assertDoesNotExist()
    }

    @Test
    fun quickInputOpensTheQuickSheet() {
        setContent(useQuickDateInput = true)

        rule.onNodeWithTag(tag).performClick()

        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).assertExists()
    }
}
