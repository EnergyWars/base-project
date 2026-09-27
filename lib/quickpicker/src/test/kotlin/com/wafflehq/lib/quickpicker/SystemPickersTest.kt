package com.wafflehq.lib.quickpicker

import android.app.Application
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import java.time.LocalDate
import java.time.LocalTime
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
class SystemPickersTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `time picker confirm returns the initial time`() {
        var confirmed: LocalTime? = null
        rule.setContent {
            SystemTimePickerDialog(initialTime = LocalTime.of(9, 30), onDismiss = {}, onConfirm = { confirmed = it })
        }

        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(LocalTime.of(9, 30), confirmed)
    }

    @Test
    fun `time picker cancel dismisses without confirming`() {
        var dismissed = false
        var confirmed: LocalTime? = null
        rule.setContent {
            SystemTimePickerDialog(
                initialTime = LocalTime.of(9, 30),
                onDismiss = { dismissed = true },
                onConfirm = { confirmed = it },
            )
        }

        rule.onNodeWithTag(QuickPickerTestTags.CANCEL_BUTTON).performClick()

        assertTrue(dismissed)
        assertNull(confirmed)
    }

    @Test
    fun `date picker confirm returns the initial date`() {
        var confirmed: LocalDate? = null
        rule.setContent {
            SystemDatePickerDialog(initialDate = LocalDate.of(2026, 3, 14), onDismiss = {}, onConfirm = { confirmed = it })
        }

        rule.onNodeWithTag(QuickPickerTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(LocalDate.of(2026, 3, 14), confirmed)
    }

    @Test
    fun `date picker cancel dismisses without confirming`() {
        var dismissed = false
        var confirmed: LocalDate? = null
        rule.setContent {
            SystemDatePickerDialog(
                initialDate = LocalDate.of(2026, 3, 14),
                onDismiss = { dismissed = true },
                onConfirm = { confirmed = it },
            )
        }

        rule.onNodeWithTag(QuickPickerTestTags.CANCEL_BUTTON).performClick()

        assertTrue(dismissed)
        assertNull(confirmed)
    }

    @Test
    fun `picker millis round trip keeps the calendar day`() {
        val date = LocalDate.of(2024, 2, 29)

        assertEquals(date, date.toPickerMillis().fromPickerMillis())
        assertEquals(LocalDate.of(1969, 12, 31), (-1L).fromPickerMillis())
    }
}
