package com.wafflehq.lib.entrylock

import android.app.Application
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class GuardedEntryAuthenticatorTest {

    private fun newActivity(): FragmentActivity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()

    private class FakeAuthenticator(
        private val available: Boolean = true,
        private val result: AuthResult = AuthResult.SUCCESS,
        private val onAuthenticate: () -> Unit = {}
    ) : EntryAuthenticator {
        var receivedTitle: String? = null
        var receivedActivity: FragmentActivity? = null

        override fun isAvailable(): Boolean = available

        override suspend fun authenticate(activity: FragmentActivity, title: String): AuthResult {
            receivedActivity = activity
            receivedTitle = title
            onAuthenticate()
            return result
        }
    }

    @Test
    fun isAvailable_delegatesToTheWrappedAuthenticator() {
        assertTrue(GuardedEntryAuthenticator(FakeAuthenticator(available = true), EntryLockSession()).isAvailable())
        assertFalse(GuardedEntryAuthenticator(FakeAuthenticator(available = false), EntryLockSession()).isAvailable())
    }

    @Test
    fun authenticate_passesArgumentsAndReturnsTheDelegateResult() = runTest {
        val delegate = FakeAuthenticator(result = AuthResult.CANCELLED)
        val activity = newActivity()

        val result = GuardedEntryAuthenticator(delegate, EntryLockSession()).authenticate(activity, "Unlock")

        assertEquals(AuthResult.CANCELLED, result)
        assertEquals("Unlock", delegate.receivedTitle)
        assertSame(activity, delegate.receivedActivity)
    }

    @Test
    fun authenticate_suppressesRelockWhileThePromptIsShown() = runTest {
        var nowMillis = 0L
        val session = EntryLockSession(backgroundScope) { nowMillis }
        var relocks = 0
        backgroundScope.launch { session.relockRequests.collect { relocks++ } }
        runCurrent()
        val delegate = FakeAuthenticator(onAuthenticate = { session.onAppForegroundedAfterBackground() })

        GuardedEntryAuthenticator(delegate, session).authenticate(newActivity(), "Unlock")
        nowMillis += EntryLockSession.PROMPT_GRACE_MILLIS + 1
        session.onAppForegroundedAfterBackground()
        runCurrent()

        assertEquals(1, relocks)
    }
}
