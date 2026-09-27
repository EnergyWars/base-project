package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.DayOfWeek

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppWeekdayChipRowTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun blockedOnlyWhenSelectedAndAtMinimum() {
        val selected = setOf(DayOfWeek.MONDAY)

        assertTrue(isWeekdayToggleBlocked(selected, DayOfWeek.MONDAY, 1))
        assertFalse(isWeekdayToggleBlocked(selected, DayOfWeek.TUESDAY, 1))
        assertFalse(isWeekdayToggleBlocked(selected, DayOfWeek.MONDAY, 0))
        assertFalse(isWeekdayToggleBlocked(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), DayOfWeek.MONDAY, 1))
    }

    @Test
    fun clickInvokesToggleWithDay() {
        val toggled = mutableListOf<DayOfWeek>()
        rule.setContent {
            AppWeekdayChipRow(
                selected = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                onToggle = { toggled += it }
            )
        }

        rule.onNodeWithTag(AppWeekdayChipRowTestTags.chip(DayOfWeek.WEDNESDAY)).performClick()
        rule.onNodeWithTag(AppWeekdayChipRowTestTags.chip(DayOfWeek.MONDAY)).performClick()

        assertEquals(listOf(DayOfWeek.WEDNESDAY, DayOfWeek.MONDAY), toggled)
    }

    @Test
    fun lastSelectedDayIsKeptAndReportsMinReached() {
        val toggled = mutableListOf<DayOfWeek>()
        var minReached = 0
        rule.setContent {
            AppWeekdayChipRow(
                selected = setOf(DayOfWeek.FRIDAY),
                onToggle = { toggled += it },
                onMinReached = { minReached++ }
            )
        }

        rule.onNodeWithTag(AppWeekdayChipRowTestTags.chip(DayOfWeek.FRIDAY)).performClick()

        assertTrue(toggled.isEmpty())
        assertEquals(1, minReached)
    }

    @Test
    fun lastSelectedDayWithoutCallbackIsSilentlyKept() {
        val toggled = mutableListOf<DayOfWeek>()
        rule.setContent {
            AppWeekdayChipRow(selected = setOf(DayOfWeek.FRIDAY), onToggle = { toggled += it })
        }

        rule.onNodeWithTag(AppWeekdayChipRowTestTags.chip(DayOfWeek.FRIDAY)).performClick()

        assertTrue(toggled.isEmpty())
    }

    @Test
    fun zeroMinimumAllowsDeselectingLastDay() {
        val toggled = mutableListOf<DayOfWeek>()
        rule.setContent {
            AppWeekdayChipRow(
                selected = setOf(DayOfWeek.FRIDAY),
                onToggle = { toggled += it },
                minSelected = 0,
                chipSize = 36.dp
            )
        }

        rule.onNodeWithTag(AppWeekdayChipRowTestTags.chip(DayOfWeek.FRIDAY)).performClick()

        assertEquals(listOf(DayOfWeek.FRIDAY), toggled)
    }
}
