package com.wafflehq.uikit.entrylock.ui

import android.app.Application
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.uikit.R
import com.wafflehq.uikit.entrylock.AuthResult
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EntryLockUiTest {

    @get:Rule
    val rule = createComposeRule()

    private fun str(id: Int) = ApplicationProvider.getApplicationContext<Application>().getString(id)

    @Test
    fun lockedEntryRowShowsDateAndTime() {
        rule.setContent { LockedEntryRow(dateText = "01.05.2026", timeText = "14:30") }

        rule.onNodeWithText("01.05.2026 · 14:30").assertIsDisplayed()
    }

    @Test
    fun lockedEntryRowShowsDateOnlyAndTrailingContent() {
        rule.setContent { LockedEntryRow(dateText = "01.05.2026", trailingContent = { Text("trailing") }) }

        rule.onNodeWithText("01.05.2026").assertIsDisplayed()
        rule.onNodeWithText("trailing").assertIsDisplayed()
    }

    @Test
    fun selectionActionsDisabledWithoutSelection() {
        rule.setContent {
            Row {
                EntrySelectionTopBarActions(selectedCount = 0, onSelectAll = {}, onLockSelected = {}, onUnlockSelected = {})
            }
        }

        rule.onNodeWithContentDescription(str(R.string.entry_lock_select_all)).assertIsEnabled()
        rule.onNodeWithContentDescription(str(R.string.entry_lock_bulk_lock)).assertIsNotEnabled()
        rule.onNodeWithContentDescription(str(R.string.entry_lock_bulk_unlock)).assertIsNotEnabled()
    }

    @Test
    fun selectionActionsInvokeCallbacks() {
        var selectAll = 0
        var lock = 0
        var unlock = 0
        rule.setContent {
            Row {
                EntrySelectionTopBarActions(
                    selectedCount = 2,
                    onSelectAll = { selectAll++ },
                    onLockSelected = { lock++ },
                    onUnlockSelected = { unlock++ },
                )
            }
        }

        rule.onNodeWithContentDescription(str(R.string.entry_lock_select_all)).performClick()
        rule.onNodeWithContentDescription(str(R.string.entry_lock_bulk_lock)).performClick()
        rule.onNodeWithContentDescription(str(R.string.entry_lock_bulk_unlock)).performClick()

        assertEquals(1, selectAll)
        assertEquals(1, lock)
        assertEquals(1, unlock)
    }

    @Test
    fun statusIconHiddenWhenNotLocked() {
        rule.setContent { EntryLockStatusIcon(isLocked = false, isTempRevealed = false, selectionMode = false, onRelock = {}) }

        rule.onNodeWithContentDescription(str(R.string.entry_lock_relock_row)).assertDoesNotExist()
    }

    @Test
    fun statusIconOffersRelockWhenTemporarilyRevealed() {
        var relocked = 0
        rule.setContent { EntryLockStatusIcon(isLocked = true, isTempRevealed = true, selectionMode = false, onRelock = { relocked++ }) }

        rule.onNodeWithContentDescription(str(R.string.entry_lock_relock_row)).assertIsDisplayed().performClick()

        assertEquals(1, relocked)
    }

    @Test
    fun statusIconShowsPlainLockIconInSelectionMode() {
        rule.setContent { EntryLockStatusIcon(isLocked = true, isTempRevealed = true, selectionMode = true, onRelock = {}) }

        rule.onNodeWithContentDescription(str(R.string.entry_lock_relock_row)).assertDoesNotExist()
        rule.onNode(hasClickAction()).assertDoesNotExist()
    }

    @Test
    fun authLauncherReportsNotAvailableWithoutFragmentActivity() {
        var result: AuthResult? = null
        var launch: ((String) -> Unit)? = null
        rule.setContent {
            launch = entryAuthLauncher(authenticate = { _, _ -> AuthResult.SUCCESS }) { result = it }
        }

        rule.runOnIdle { launch!!("title") }

        rule.runOnIdle { assertEquals(AuthResult.NOT_AVAILABLE, result) }
    }

    @Test
    fun authLauncherDelegatesToAuthenticatorWithFragmentActivity() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        var receivedTitle: String? = null
        var result: AuthResult? = null
        var launch: ((String) -> Unit)? = null
        rule.setContent {
            CompositionLocalProvider(LocalContext provides activity) {
                launch = entryAuthLauncher(authenticate = { _, title -> receivedTitle = title; AuthResult.FAILED }) { result = it }
            }
        }

        rule.runOnIdle { launch!!("Confirm") }

        rule.runOnIdle {
            assertEquals("Confirm", receivedTitle)
            assertEquals(AuthResult.FAILED, result)
        }
    }
}
