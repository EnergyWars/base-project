package com.wafflehq.lib.quickpicker

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
class YearPickerDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private var selected: Int? = null
    private var dismissed = false

    private fun show(currentYear: Int = 2026) {
        selected = null
        dismissed = false
        rule.setContent {
            YearPickerDialog(
                currentYear = currentYear,
                onDismiss = { dismissed = true },
                onYearSelected = { selected = it },
            )
        }
    }

    @Test
    fun yearRangeCoversFiveBackAndTenForward() {
        val years = YearPickerDefaults.years(2026)

        assertEquals(2021, years.first())
        assertEquals(2036, years.last())
        assertEquals(16, years.size)
    }

    @Test
    fun showsTitleAndCurrentYear() {
        show(currentYear = 2026)

        rule.onNodeWithText("Select Year").assertIsDisplayed()
        rule.onNodeWithText("2026").assertIsDisplayed()
    }

    @Test
    fun clickingYearReportsSelection() {
        show(currentYear = 2026)

        rule.onNodeWithText("2027").performClick()

        assertEquals(2027, selected)
    }

    @Test
    fun cancelInvokesDismissWithoutSelection() {
        show()

        rule.onNodeWithText("Cancel").performClick()

        assertTrue(dismissed)
        assertNull(selected)
    }
}
