package com.wafflehq.base.ui.library.demos

import android.app.Application
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.uicore.color.ContrastLevel
import com.wafflehq.lib.uicore.components.AppWeekdayChipRowTestTags
import com.wafflehq.lib.uicore.components.IconPickerTestTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.DayOfWeek
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UiCoreInputDemoTest : LibraryDemoTest() {

    @Test
    fun selectionControlsToggle() {
        show { UiCoreInputDemo() }
        node(DemoTags.section("uicore_input")).assertExists()
        node(UiCoreInputTags.SWITCH).assertIsOn()
        node(UiCoreInputTags.CHECKBOX).assertIsOff()
        node(UiCoreInputTags.RADIO_ONE).assertIsSelected()

        click(UiCoreInputTags.SWITCH)
        click(UiCoreInputTags.CHECKBOX)
        click(UiCoreInputTags.RADIO_TWO)

        node(UiCoreInputTags.SWITCH).assertIsOff()
        node(UiCoreInputTags.CHECKBOX).assertIsOn()
        node(UiCoreInputTags.RADIO_TWO).assertIsSelected()
        node(UiCoreInputTags.RADIO_ONE).assertIsNotSelected()
    }

    @Test
    fun searchFiltersTheListItems() {
        show { UiCoreInputDemo() }
        assertTagCount(UiCoreInputTags.ITEMS, 5)

        replaceText(UiCoreInputTags.SEARCH, "hong")

        assertTagCount(UiCoreInputTags.ITEMS, 1)
        rule.onNodeWithText(string(R.string.libex_uicore_item_hongkong)).assertExists()
    }

    @Test
    fun searchWithoutMatchShowsTheEmptyHint() {
        show { UiCoreInputDemo() }

        replaceText(UiCoreInputTags.SEARCH, "zzz")

        assertTagCount(UiCoreInputTags.ITEMS, 0)
        rule.onNodeWithText(string(R.string.libex_uicore_search_empty)).assertExists()
    }

    @Test
    fun weekdayChipsToggleTheSelection() {
        show { UiCoreInputDemo() }
        assertTagText(UiCoreInputTags.DAYS_RESULT, string(R.string.libex_uicore_days_result, 2))

        click(AppWeekdayChipRowTestTags.chip(DayOfWeek.FRIDAY))

        assertTagText(UiCoreInputTags.DAYS_RESULT, string(R.string.libex_uicore_days_result, 3))
    }

    @Test
    fun priorityDropdownReportsTheSelection() {
        show { UiCoreInputDemo() }

        click(UiCoreInputTags.PRIORITY_FIELD)
        rule.onNodeWithText(string(R.string.libex_uicore_priority_high)).performClick()

        assertTagText(UiCoreInputTags.PRIORITY_RESULT, DemoPriority.HIGH.name)
    }

    @Test
    fun iconPickerReportsTheChosenIcon() {
        show { UiCoreInputDemo() }
        assertTagText(UiCoreInputTags.ICON_RESULT, string(R.string.libex_uicore_icon_none))

        click(IconPickerTestTags.cell("star"))

        assertTagText(UiCoreInputTags.ICON_RESULT, string(R.string.libex_uicore_icon_result, string(R.string.libex_uicore_icon_star)))
        node(IconPickerTestTags.cell("star")).assertIsSelected()
    }

    @Test
    fun breadcrumbGrowsWithEachStep() {
        show { UiCoreInputDemo() }
        assertTagText(UiCoreInputTags.CRUMB_RESULT, string(R.string.libex_uicore_crumb_result, 1))

        click(UiCoreInputTags.CRUMB_DEEPER)
        click(UiCoreInputTags.CRUMB_DEEPER)

        assertTagText(UiCoreInputTags.CRUMB_RESULT, string(R.string.libex_uicore_crumb_result, 3))
    }

    @Test
    fun filterIsCaseInsensitiveAndKeepsOrder() {
        val items = listOf("Belgian waffle", "Stroopwafel", "Pandan waffle")

        assertEquals(items, UiCoreInputLogic.filter(items, "  "))
        assertEquals(listOf("Belgian waffle", "Pandan waffle"), UiCoreInputLogic.filter(items, "WAFFLE"))
        assertTrue(UiCoreInputLogic.filter(items, "xyz").isEmpty())
    }

    @Test
    fun toggleAddsAndRemovesDays() {
        val start = UiCoreInputLogic.DEFAULT_DAYS

        val added = UiCoreInputLogic.toggle(start, DayOfWeek.SUNDAY)
        val removed = UiCoreInputLogic.toggle(added, DayOfWeek.MONDAY)

        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY), added)
        assertEquals(setOf(DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY), removed)
    }

    @Test
    fun contrastHelpersFormatAndLabel() {
        assertEquals("4.5:1", UiCoreInputLogic.contrastText(4.5, Locale.ENGLISH))
        assertEquals("4,5:1", UiCoreInputLogic.contrastText(4.5, Locale.GERMAN))
        assertEquals(3, ContrastLevel.entries.map { UiCoreInputLogic.contrastLevelLabel(it) }.toSet().size)
        assertEquals(3, DemoPriority.entries.map { UiCoreInputLogic.priorityLabel(it) }.toSet().size)
        assertEquals(6, UiCoreInputLogic.ICON_KEYS.size)
    }
}
