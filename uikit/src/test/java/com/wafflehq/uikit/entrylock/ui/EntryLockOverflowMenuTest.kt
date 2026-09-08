package com.wafflehq.uikit.entrylock.ui

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.uikit.R
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
class EntryLockOverflowMenuTest {

    @get:Rule
    val rule = createComposeRule()

    private fun str(id: Int) = ApplicationProvider.getApplicationContext<Application>().getString(id)

    private class Calls {
        var dismiss = 0
        var select = 0
        var lockAll = 0
        var unlockAll = 0
        var sessionUnlock = 0
    }

    private fun setMenu(hasLockedEntries: Boolean, sessionUnlocked: Boolean): Calls {
        val calls = Calls()
        rule.setContent {
            EntryLockOverflowMenu(
                expanded = true,
                onDismiss = { calls.dismiss++ },
                hasLockedEntries = hasLockedEntries,
                sessionUnlocked = sessionUnlocked,
                onSelect = { calls.select++ },
                onLockAll = { calls.lockAll++ },
                onUnlockAll = { calls.unlockAll++ },
                onSessionUnlock = { calls.sessionUnlock++ },
            )
        }
        return calls
    }

    @Test
    fun allItemsAreDisplayed() {
        setMenu(hasLockedEntries = true, sessionUnlocked = false)

        rule.onNodeWithText(str(R.string.entry_lock_menu_select)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.entry_lock_menu_lock_all)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.entry_lock_menu_unlock_all)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.entry_lock_menu_session_unlock)).assertIsDisplayed()
    }

    @Test
    fun clickingItemsDismissesAndInvokesCallback() {
        val calls = setMenu(hasLockedEntries = true, sessionUnlocked = false)

        rule.onNodeWithText(str(R.string.entry_lock_menu_select)).performClick()
        rule.onNodeWithText(str(R.string.entry_lock_menu_lock_all)).performClick()
        rule.onNodeWithText(str(R.string.entry_lock_menu_unlock_all)).performClick()
        rule.onNodeWithText(str(R.string.entry_lock_menu_session_unlock)).performClick()

        assertEquals(1, calls.select)
        assertEquals(1, calls.lockAll)
        assertEquals(1, calls.unlockAll)
        assertEquals(1, calls.sessionUnlock)
        assertEquals(4, calls.dismiss)
    }

    @Test
    fun sessionUnlockDisabledWithoutLockedEntries() {
        setMenu(hasLockedEntries = false, sessionUnlocked = false)

        rule.onNodeWithText(str(R.string.entry_lock_menu_session_unlock)).assertIsNotEnabled()
    }

    @Test
    fun sessionUnlockDisabledWhenSessionAlreadyUnlocked() {
        setMenu(hasLockedEntries = true, sessionUnlocked = true)

        rule.onNodeWithText(str(R.string.entry_lock_menu_session_unlock)).assertIsNotEnabled()
    }

    @Test
    fun sessionUnlockEnabledWithLockedEntriesAndLockedSession() {
        setMenu(hasLockedEntries = true, sessionUnlocked = false)

        rule.onNodeWithText(str(R.string.entry_lock_menu_session_unlock)).assertIsEnabled()
    }
}
