package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.uicore.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NameInputDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private val confirm = "Confirm it"
    private val dismiss = "Dismiss it"

    private fun str(id: Int) = ApplicationProvider.getApplicationContext<Application>().getString(id)

    private fun setContent(
        initialName: String,
        onConfirm: (String) -> Unit = {},
        onDismiss: () -> Unit = {}
    ) {
        rule.setContent {
            NameInputDialog(
                title = "Dialog title",
                label = "Name field",
                initialName = initialName,
                onConfirm = onConfirm,
                onDismiss = onDismiss,
                confirmLabel = confirm,
                dismissLabel = dismiss
            )
        }
    }

    @Test
    fun `title and initial name are displayed`() {
        setContent(initialName = "Anna")

        rule.onNodeWithText("Dialog title").assertIsDisplayed()
        rule.onNodeWithText("Anna").assertExists()
    }

    @Test
    fun `confirm is disabled for a blank name`() {
        setContent(initialName = "")

        rule.onNodeWithText(confirm).assertIsNotEnabled()
    }

    @Test
    fun `confirm is disabled for a whitespace only name`() {
        setContent(initialName = "")

        rule.onNodeWithText("Name field").performTextInput("   ")

        rule.onNodeWithText(confirm).assertIsNotEnabled()
    }

    @Test
    fun `confirm is enabled for a non blank name`() {
        setContent(initialName = "Anna")

        rule.onNodeWithText(confirm).assertIsEnabled()
    }

    @Test
    fun `confirm reports the trimmed name`() {
        val results = mutableListOf<String>()
        setContent(initialName = "", onConfirm = { results += it })

        rule.onNodeWithText("Name field").performTextInput("  Berta  ")
        rule.onNodeWithText(confirm).performClick()

        assertEquals(listOf("Berta"), results)
    }

    @Test
    fun `confirm reports the initial name unchanged when nothing was edited`() {
        val results = mutableListOf<String>()
        setContent(initialName = "Anna", onConfirm = { results += it })

        rule.onNodeWithText(confirm).performClick()

        assertEquals(listOf("Anna"), results)
    }

    @Test
    fun `clearing the text disables confirm again`() {
        setContent(initialName = "Anna")

        rule.onNodeWithText("Anna").performTextClearance()

        rule.onNodeWithText(confirm).assertIsNotEnabled()
    }

    @Test
    fun `dismiss button invokes the dismiss callback without confirming`() {
        var dismissed = 0
        val results = mutableListOf<String>()
        setContent(initialName = "Anna", onConfirm = { results += it }, onDismiss = { dismissed++ })

        rule.onNodeWithText(dismiss).performClick()

        assertEquals(1, dismissed)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `default button labels use the library strings`() {
        rule.setContent {
            NameInputDialog(
                title = "Dialog title",
                label = "Name field",
                initialName = "Anna",
                onConfirm = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithText(str(R.string.uicore_ok)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.uicore_cancel)).assertIsDisplayed()
    }

    @Test
    fun `an optional message is shown above the field`() {
        rule.setContent {
            NameInputDialog(
                title = "Dialog title",
                label = "Name field",
                initialName = "",
                onConfirm = {},
                onDismiss = {},
                message = "Explain why a name is needed"
            )
        }

        rule.onNodeWithText("Explain why a name is needed").assertIsDisplayed()
    }

    @Test
    fun `without a message no explanation text is shown`() {
        setContent(initialName = "Anna")

        rule.onNodeWithText("Explain why a name is needed").assertDoesNotExist()
    }
}
