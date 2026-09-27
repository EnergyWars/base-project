package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
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
class DiscardChangesDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private fun str(id: Int) = ApplicationProvider.getApplicationContext<Application>().getString(id)

    @Test
    fun dialogShowsTextsAndInvokesCallbacks() {
        var confirmed = 0
        var dismissed = 0
        rule.setContent { DiscardChangesDialog(onConfirm = { confirmed++ }, onDismiss = { dismissed++ }) }

        rule.onNodeWithText(str(R.string.uicore_discard_changes_title)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.uicore_discard_changes_message)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.uicore_discard_changes_confirm)).performClick()
        rule.onNodeWithText(str(R.string.uicore_cancel)).performClick()

        assertEquals(1, confirmed)
        assertEquals(1, dismissed)
    }

    @Test
    fun guardedDismissWithoutChangesDismissesImmediately() {
        var dismissed = 0
        rule.setContent {
            val guarded = rememberGuardedDismiss(hasChanges = false, onDismiss = { dismissed++ })
            Button(onClick = guarded) { Text("close") }
        }

        rule.onNodeWithText("close").performClick()

        assertEquals(1, dismissed)
        rule.onNodeWithText(str(R.string.uicore_discard_changes_title)).assertDoesNotExist()
    }

    @Test
    fun guardedDismissWithChangesAsksForConfirmation() {
        var dismissed = 0
        rule.setContent {
            val guarded = rememberGuardedDismiss(hasChanges = true, onDismiss = { dismissed++ })
            Button(onClick = guarded) { Text("close") }
        }

        rule.onNodeWithText("close").performClick()
        rule.onNodeWithText(str(R.string.uicore_discard_changes_title)).assertIsDisplayed()
        assertEquals(0, dismissed)

        rule.onNodeWithText(str(R.string.uicore_discard_changes_confirm)).performClick()

        assertEquals(1, dismissed)
        rule.onNodeWithText(str(R.string.uicore_discard_changes_title)).assertDoesNotExist()
    }

    @Test
    fun guardedDismissCancelKeepsEditorOpen() {
        var dismissed = 0
        rule.setContent {
            val guarded = rememberGuardedDismiss(hasChanges = true, onDismiss = { dismissed++ })
            Button(onClick = guarded) { Text("close") }
        }

        rule.onNodeWithText("close").performClick()
        rule.onNodeWithText(str(R.string.uicore_cancel)).performClick()

        assertEquals(0, dismissed)
        rule.onNodeWithText(str(R.string.uicore_discard_changes_title)).assertDoesNotExist()
    }
}
