package com.wafflehq.lib.entrylock

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EntryLockSessionTest {

    private class RelockCounter {
        var count = 0
    }

    private fun TestScope.countRelocks(session: EntryLockSession): RelockCounter {
        val counter = RelockCounter()
        backgroundScope.launch { session.relockRequests.collect { counter.count++ } }
        runCurrent()
        return counter
    }

    private var nowMillis = 0L

    private fun TestScope.newSession(durationMillis: Long = 1_000L) =
        EntryLockSession(backgroundScope, durationMillis) { nowMillis }

    @Test
    fun sessionDuration_isTenMinutes() {
        assertEquals(10 * 60 * 1000L, EntryLockSession.SESSION_DURATION_MILLIS)
    }

    @Test
    fun promptGrace_isOneSecond() {
        assertEquals(1_000L, EntryLockSession.PROMPT_GRACE_MILLIS)
    }

    @Test
    fun onSessionUnlocked_requestsRelockOnceTheSessionDurationElapsed() = runTest {
        val session = newSession()
        val relocks = countRelocks(session)

        session.onSessionUnlocked()
        advanceTimeBy(999)
        runCurrent()
        assertEquals(0, relocks.count)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, relocks.count)
    }

    @Test
    fun onSessionUnlocked_requestsRelockOnlyOncePerUnlock() = runTest {
        val session = newSession()
        val relocks = countRelocks(session)

        session.onSessionUnlocked()
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(1, relocks.count)
    }

    @Test
    fun onSessionUnlocked_restartsTheTimerWhenUnlockedAgain() = runTest {
        val session = newSession()
        val relocks = countRelocks(session)

        session.onSessionUnlocked()
        advanceTimeBy(600)
        session.onSessionUnlocked()
        advanceTimeBy(600)
        runCurrent()
        assertEquals(0, relocks.count)

        advanceTimeBy(400)
        runCurrent()
        assertEquals(1, relocks.count)
    }

    @Test
    fun onAppForegroundedAfterBackground_requestsRelockImmediately() = runTest {
        val session = newSession()
        val relocks = countRelocks(session)

        session.onAppForegroundedAfterBackground()
        runCurrent()

        assertEquals(1, relocks.count)
    }

    @Test
    fun onAppForegroundedAfterBackground_cancelsThePendingTimer() = runTest {
        val session = newSession()
        val relocks = countRelocks(session)
        session.onSessionUnlocked()

        session.onAppForegroundedAfterBackground()
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(1, relocks.count)
    }

    @Test
    fun onAppForegroundedAfterBackground_isIgnoredWhileAuthPromptIsActive() = runTest {
        val session = newSession()
        val relocks = countRelocks(session)
        session.onAuthPromptShown()

        session.onAppForegroundedAfterBackground()
        runCurrent()

        assertEquals(0, relocks.count)
    }

    @Test
    fun onAppForegroundedAfterBackground_keepsThePendingTimerWhileAuthPromptIsActive() = runTest {
        val session = newSession()
        val relocks = countRelocks(session)
        session.onSessionUnlocked()
        session.onAuthPromptShown()

        session.onAppForegroundedAfterBackground()
        advanceTimeBy(1_000)
        runCurrent()

        assertEquals(1, relocks.count)
    }

    @Test
    fun onAppForegroundedAfterBackground_requestsRelockAgainOnceThePromptFinished() = runTest {
        val session = newSession()
        val relocks = countRelocks(session)
        session.onAuthPromptShown()
        session.onAppForegroundedAfterBackground()

        session.onAuthPromptFinished()
        nowMillis += EntryLockSession.PROMPT_GRACE_MILLIS + 1
        session.onAppForegroundedAfterBackground()
        runCurrent()

        assertEquals(1, relocks.count)
    }

    @Test
    fun onAppForegroundedAfterBackground_isIgnoredWithinTheGraceAfterAPromptFinished() = runTest {
        val session = newSession()
        val relocks = countRelocks(session)
        session.onAuthPromptShown()
        session.onAuthPromptFinished()

        nowMillis += EntryLockSession.PROMPT_GRACE_MILLIS
        session.onAppForegroundedAfterBackground()
        runCurrent()

        assertEquals(0, relocks.count)
    }

    @Test
    fun onAppForegroundedAfterBackground_requestsRelockJustAfterTheGraceElapsed() = runTest {
        val session = newSession()
        val relocks = countRelocks(session)
        session.onAuthPromptShown()
        session.onAuthPromptFinished()

        nowMillis += EntryLockSession.PROMPT_GRACE_MILLIS + 1
        session.onAppForegroundedAfterBackground()
        runCurrent()

        assertEquals(1, relocks.count)
    }

    @Test
    fun onAppForegroundedAfterBackground_withoutCollectorDoesNotFail() = runTest {
        newSession().onAppForegroundedAfterBackground()
        runCurrent()
    }

    @Test
    fun guardAuthPrompt_suppressesRelockWhileTheBlockRuns() = runTest {
        val session = newSession()
        val relocks = countRelocks(session)

        session.guardAuthPrompt { session.onAppForegroundedAfterBackground() }
        runCurrent()

        assertEquals(0, relocks.count)
    }

    @Test
    fun guardAuthPrompt_returnsTheBlockResult() = runTest {
        assertEquals("result", newSession().guardAuthPrompt { "result" })
    }

    @Test
    fun guardAuthPrompt_releasesTheGuardAfterTheBlockCompleted() = runTest {
        val session = newSession()
        val relocks = countRelocks(session)

        session.guardAuthPrompt { }
        nowMillis += EntryLockSession.PROMPT_GRACE_MILLIS + 1
        session.onAppForegroundedAfterBackground()
        runCurrent()

        assertEquals(1, relocks.count)
    }

    @Test
    fun guardAuthPrompt_releasesTheGuardWhenTheBlockThrows() = runTest {
        val session = newSession()
        val relocks = countRelocks(session)

        val failure = runCatching { session.guardAuthPrompt { error("boom") } }
        nowMillis += EntryLockSession.PROMPT_GRACE_MILLIS + 1
        session.onAppForegroundedAfterBackground()
        runCurrent()

        assertTrue(failure.isFailure)
        assertEquals(1, relocks.count)
    }
}
