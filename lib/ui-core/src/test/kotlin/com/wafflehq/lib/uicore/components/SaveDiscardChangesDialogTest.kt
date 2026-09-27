package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
class SaveDiscardChangesDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private fun str(id: Int) = ApplicationProvider.getApplicationContext<Application>().getString(id)

    private fun setDialog(
        extraText: String? = null,
        onSave: () -> Unit = {},
        onDiscard: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        rule.setContent {
            SaveDiscardChangesDialog(
                onSave = onSave,
                onDiscard = onDiscard,
                onDismiss = onDismiss,
                extraText = extraText,
            )
        }
    }

    @Test
    fun showsTitleAndMessage() {
        setDialog()

        rule.onNodeWithTag(SaveDiscardChangesDialogTestTags.DIALOG).assertExists()
        rule.onNodeWithText(str(R.string.uicore_save_changes_title)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.uicore_save_changes_message)).assertIsDisplayed()
    }

    @Test
    fun extraTextIsShownWhenProvided() {
        setDialog(extraText = "history note")

        rule.onNodeWithText("history note").assertIsDisplayed()
    }

    @Test
    fun extraTextIsAbsentByDefault() {
        setDialog()

        rule.onNodeWithText("history note").assertDoesNotExist()
    }

    @Test
    fun showsAllThreeActions() {
        setDialog()

        rule.onNodeWithTag(SaveDiscardChangesDialogTestTags.SAVE).assertExists()
        rule.onNodeWithTag(SaveDiscardChangesDialogTestTags.DISCARD).assertExists()
        rule.onNodeWithTag(SaveDiscardChangesDialogTestTags.CANCEL).assertExists()
        rule.onNodeWithText(str(R.string.uicore_save)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.uicore_discard_changes_confirm)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.uicore_cancel)).assertIsDisplayed()
    }

    @Test
    fun saveInvokesOnSaveOnly() {
        val calls = mutableListOf<String>()
        setDialog(onSave = { calls += "save" }, onDiscard = { calls += "discard" }, onDismiss = { calls += "dismiss" })

        rule.onNodeWithTag(SaveDiscardChangesDialogTestTags.SAVE).performClick()

        assertEquals(listOf("save"), calls)
    }

    @Test
    fun discardInvokesOnDiscardOnly() {
        val calls = mutableListOf<String>()
        setDialog(onSave = { calls += "save" }, onDiscard = { calls += "discard" }, onDismiss = { calls += "dismiss" })

        rule.onNodeWithTag(SaveDiscardChangesDialogTestTags.DISCARD).performClick()

        assertEquals(listOf("discard"), calls)
    }

    @Test
    fun cancelInvokesOnDismissOnly() {
        val calls = mutableListOf<String>()
        setDialog(onSave = { calls += "save" }, onDiscard = { calls += "discard" }, onDismiss = { calls += "dismiss" })

        rule.onNodeWithTag(SaveDiscardChangesDialogTestTags.CANCEL).performClick()

        assertEquals(listOf("dismiss"), calls)
    }
}
