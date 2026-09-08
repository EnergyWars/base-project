package com.wafflehq.uikit.entrylock.ui

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.uikit.entrylock.AuthResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EntryLockPendingActionsTest {

    @get:Rule
    val rule = createComposeRule()

    private data class Entry(val id: Long, val locked: Boolean)

    private class Recorder {
        val opened = mutableListOf<Entry>()
        val edited = mutableListOf<Entry>()
        val temporaryUnlocks = mutableListOf<Long>()
        var sessionUnlocks = 0
        var unlockAll = 0
        var bulkUnlock = 0
        val authTitles = mutableListOf<String>()
    }

    private fun directActions(recorder: Recorder, pending: MutableMap<String, Any?>): EntryLockPendingActions<Entry> =
        EntryLockPendingActions(
            idOf = { it.id },
            isRevealed = { !it.locked },
            onOpenViewer = { recorder.opened += it },
            onEditEntry = { recorder.edited += it },
            launchAuth = { recorder.authTitles += it },
            authTitle = "auth",
            setPendingView = { pending["view"] = it },
            setPendingEdit = { pending["edit"] = it },
            setPendingTemporaryUnlockId = { pending["temp"] = it },
            setPendingUnlockAll = { pending["unlockAll"] = it },
            setPendingSessionUnlock = { pending["session"] = it },
            setPendingBulkUnlock = { pending["bulk"] = it },
        )

    @Test
    fun revealedEntriesOpenAndEditWithoutAuthentication() {
        val recorder = Recorder()
        val pending = mutableMapOf<String, Any?>()
        val actions = directActions(recorder, pending)
        val entry = Entry(1, locked = false)

        actions.requestView(entry)
        actions.requestEdit(entry)

        assertEquals(listOf(entry), recorder.opened)
        assertEquals(listOf(entry), recorder.edited)
        assertTrue(recorder.authTitles.isEmpty())
        assertTrue(pending.isEmpty())
    }

    @Test
    fun lockedEntriesAreParkedAndAuthenticationIsLaunched() {
        val recorder = Recorder()
        val pending = mutableMapOf<String, Any?>()
        val actions = directActions(recorder, pending)
        val entry = Entry(2, locked = true)

        actions.requestView(entry)
        actions.requestEdit(entry)
        actions.requestTemporaryUnlock(entry)
        actions.requestUnlockAll()
        actions.requestSessionUnlock()
        actions.requestBulkUnlock()

        assertEquals(entry, pending["view"])
        assertEquals(entry, pending["edit"])
        assertEquals(2L, pending["temp"])
        assertEquals(true, pending["unlockAll"])
        assertEquals(true, pending["session"])
        assertEquals(true, pending["bulk"])
        assertEquals(List(6) { "auth" }, recorder.authTitles)
        assertTrue(recorder.opened.isEmpty())
        assertTrue(recorder.edited.isEmpty())
    }

    @Test
    fun withoutFragmentActivityAuthenticationIsUnavailableAndToastShown() {
        val recorder = Recorder()
        var actions: EntryLockPendingActions<Entry>? = null
        rule.setContent {
            actions = rememberEntryLockPendingActions(
                authenticate = { _, _ -> AuthResult.SUCCESS },
                authTitle = "auth",
                unavailableMessage = "unavailable",
                isRevealed = { !it.locked },
                idOf = { it.id },
                onOpenViewer = { recorder.opened += it },
                onEditEntry = { recorder.edited += it },
                onTemporaryUnlockConfirmed = { recorder.temporaryUnlocks += it },
                onSessionUnlockConfirmed = { recorder.sessionUnlocks++ },
                onUnlockAllEntriesConfirmed = { recorder.unlockAll++ },
                onBulkUnlockSelectedConfirmed = { recorder.bulkUnlock++ },
            )
        }

        rule.runOnIdle { actions!!.requestView(Entry(1, locked = true)) }

        rule.runOnIdle {
            assertEquals("unavailable", ShadowToast.getTextOfLatestToast())
            assertTrue(recorder.opened.isEmpty())
        }
    }

    private fun setWithActivity(recorder: Recorder, result: AuthResult): EntryLockPendingActions<Entry> {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        var actions: EntryLockPendingActions<Entry>? = null
        rule.setContent {
            CompositionLocalProvider(LocalContext provides activity) {
                actions = rememberEntryLockPendingActions(
                    authenticate = { _, title -> recorder.authTitles += title; result },
                    authTitle = "auth",
                    unavailableMessage = "unavailable",
                    isRevealed = { !it.locked },
                    idOf = { it.id },
                    onOpenViewer = { recorder.opened += it },
                    onEditEntry = { recorder.edited += it },
                    onTemporaryUnlockConfirmed = { recorder.temporaryUnlocks += it },
                    onSessionUnlockConfirmed = { recorder.sessionUnlocks++ },
                    onUnlockAllEntriesConfirmed = { recorder.unlockAll++ },
                    onBulkUnlockSelectedConfirmed = { recorder.bulkUnlock++ },
                )
            }
        }
        rule.waitForIdle()
        return actions!!
    }

    @Test
    fun successfulAuthenticationRunsPendingActions() {
        val recorder = Recorder()
        val actions = setWithActivity(recorder, AuthResult.SUCCESS)
        val entry = Entry(5, locked = true)

        rule.runOnIdle { actions.requestView(entry) }
        rule.runOnIdle { actions.requestEdit(entry) }
        rule.runOnIdle { actions.requestTemporaryUnlock(entry) }
        rule.runOnIdle { actions.requestUnlockAll() }
        rule.runOnIdle { actions.requestSessionUnlock() }
        rule.runOnIdle { actions.requestBulkUnlock() }

        rule.runOnIdle {
            assertEquals(listOf(entry), recorder.opened)
            assertEquals(listOf(entry), recorder.edited)
            assertEquals(listOf(5L), recorder.temporaryUnlocks)
            assertEquals(1, recorder.unlockAll)
            assertEquals(1, recorder.sessionUnlocks)
            assertEquals(1, recorder.bulkUnlock)
            assertEquals(List(6) { "auth" }, recorder.authTitles)
        }
    }

    @Test
    fun failedAuthenticationClearsPendingWithoutRunningActions() {
        val recorder = Recorder()
        val actions = setWithActivity(recorder, AuthResult.FAILED)

        rule.runOnIdle { actions.requestView(Entry(7, locked = true)) }
        rule.runOnIdle { actions.requestUnlockAll() }

        rule.runOnIdle {
            assertTrue(recorder.opened.isEmpty())
            assertEquals(0, recorder.unlockAll)
            assertNull(ShadowToast.getLatestToast())
        }
    }

    @Test
    fun cancelledAuthenticationIsIgnored() {
        val recorder = Recorder()
        val actions = setWithActivity(recorder, AuthResult.CANCELLED)

        rule.runOnIdle { actions.requestSessionUnlock() }

        rule.runOnIdle {
            assertEquals(0, recorder.sessionUnlocks)
            assertEquals(listOf("auth"), recorder.authTitles)
        }
    }
}
