package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.uicore.R
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
class DeleteConfirmationDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val deleteLabel = context.getString(R.string.uicore_delete)
    private val cancelLabel = context.getString(R.string.uicore_cancel)

    private fun setContent(onConfirm: () -> Unit = {}, onDismiss: () -> Unit = {}) {
        rule.setContent {
            DeleteConfirmationDialog(
                title = "Delete title",
                message = "Delete message",
                onConfirm = onConfirm,
                onDismiss = onDismiss
            )
        }
    }

    @Test
    fun `title and message are displayed`() {
        setContent()

        rule.onNodeWithText("Delete title").assertExists()
        rule.onNodeWithText("Delete message").assertExists()
    }

    @Test
    fun `confirm button triggers onConfirm only`() {
        var confirmed = 0
        var dismissed = 0
        setContent(onConfirm = { confirmed++ }, onDismiss = { dismissed++ })

        rule.onNodeWithText(deleteLabel).performClick()

        assertEquals(1, confirmed)
        assertEquals(0, dismissed)
    }

    @Test
    fun `cancel button triggers onDismiss only`() {
        var confirmed = 0
        var dismissed = 0
        setContent(onConfirm = { confirmed++ }, onDismiss = { dismissed++ })

        rule.onNodeWithText(cancelLabel).performClick()

        assertEquals(0, confirmed)
        assertEquals(1, dismissed)
    }
}
