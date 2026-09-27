package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ErrorDetailDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setDialog(detail: String?, onDismiss: () -> Unit = {}) {
        rule.setContent {
            ErrorDetailDialog(
                title = "Sync failed",
                message = "Something broke",
                dismissText = "Close",
                onDismiss = onDismiss,
                icon = Icons.Default.Warning,
                detail = detail
            )
        }
    }

    @Test
    fun showsTitleMessageAndDetail() {
        setDialog(detail = "IllegalStateException: boom")

        rule.onNodeWithText("Sync failed").assertIsDisplayed()
        rule.onNodeWithTag(ErrorDetailDialogTestTags.MESSAGE).assertIsDisplayed()
        rule.onNodeWithTag(ErrorDetailDialogTestTags.DETAIL).assertExists()
        rule.onNodeWithText("IllegalStateException: boom").assertExists()
    }

    @Test
    fun nullDetailHidesDetailBox() {
        setDialog(detail = null)

        rule.onNodeWithTag(ErrorDetailDialogTestTags.DETAIL).assertDoesNotExist()
    }

    @Test
    fun emptyDetailHidesDetailBox() {
        setDialog(detail = "")

        rule.onNodeWithTag(ErrorDetailDialogTestTags.DETAIL).assertDoesNotExist()
    }

    @Test
    fun dismissButtonInvokesCallback() {
        var dismissed = 0
        setDialog(detail = null, onDismiss = { dismissed++ })

        rule.onNodeWithTag(ErrorDetailDialogTestTags.DISMISS).performClick()

        assertEquals(1, dismissed)
    }

    @Test
    fun detailTextShowsClassAndMessage() {
        assertEquals("IllegalStateException: boom", IllegalStateException("boom").detailText())
    }

    @Test
    fun detailTextOmitsMessageEqualToGivenMessage() {
        assertEquals("IllegalStateException", IllegalStateException("boom").detailText("boom"))
    }

    @Test
    fun detailTextWithoutMessageShowsOnlyClass() {
        assertEquals("IllegalStateException", IllegalStateException().detailText())
    }

    @Test
    fun detailTextAppendsCause() {
        val error = RuntimeException("outer", IllegalArgumentException("inner"))

        assertEquals(
            "RuntimeException: outer\nCause: IllegalArgumentException: inner",
            error.detailText()
        )
    }

    @Test
    fun detailTextCauseWithoutMessageShowsOnlyClass() {
        val error = RuntimeException("outer", IllegalArgumentException())

        assertEquals("RuntimeException: outer\nCause: IllegalArgumentException", error.detailText())
    }
}
