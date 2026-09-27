package com.wafflehq.lib.diagnostics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val STACK_TRACE = "at com.example.Blocked.frame"

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class FreezeWatchdogTest {

    private class FakeFreezeProbe(var heartbeatMs: Long = 0L) : FreezeProbe {
        var startCount = 0
        var stopCount = 0

        @Volatile
        var checkCount = 0

        override fun start() {
            startCount++
        }

        override fun stop() {
            stopCount++
        }

        override fun lastHeartbeatMs(): Long {
            checkCount++
            return heartbeatMs
        }

        override fun blockedThreadStackTrace(): String = STACK_TRACE
    }

    private val sink = RecordingDiagnosticLogSink()
    private val logger = DiagnosticLogger(sink, scope = CoroutineScope(Dispatchers.Unconfined))
    private val probe = FakeFreezeProbe()
    private val watchdogScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var now = 0L

    @After
    fun tearDown() {
        watchdogScope.cancel()
    }

    private fun watchdog(thresholdMs: Long = 100L, checkIntervalMs: Long = 5L) = FreezeWatchdog(
        logger = logger,
        detector = FreezeDetector(thresholdMs = thresholdMs),
        probe = probe,
        scope = watchdogScope,
        checkIntervalMs = checkIntervalMs,
        nowMs = { now }
    )

    @Test
    fun checkOnce_whenHeartbeatIsFresh_logsNothing() {
        probe.heartbeatMs = 950L
        now = 1000L

        watchdog().checkOnce()

        assertTrue(sink.entries.isEmpty())
    }

    @Test
    fun checkOnce_whenThresholdExceeded_logsFreezeWithDurationAndStackTrace() {
        probe.heartbeatMs = 1000L
        now = 1500L

        watchdog().checkOnce()

        val entry = sink.entries.single()
        assertEquals(DiagnosticLevel.FREEZE, entry.level)
        assertEquals(FreezeWatchdog.TAG, entry.tag)
        assertTrue(entry.message.contains("500ms"))
        assertEquals(STACK_TRACE, entry.stackTrace)
    }

    @Test
    fun checkOnce_onlyLogsOnceWhileStillFrozen() {
        probe.heartbeatMs = 1000L
        now = 1500L
        val watchdog = watchdog()

        watchdog.checkOnce()
        now = 2000L
        watchdog.checkOnce()

        assertEquals(1, sink.entries.size)
    }

    @Test
    fun checkOnce_afterRecovery_logsWarning() {
        probe.heartbeatMs = 1000L
        now = 1500L
        val watchdog = watchdog()
        watchdog.checkOnce()

        probe.heartbeatMs = 1600L
        now = 1620L
        watchdog.checkOnce()

        assertEquals(listOf(DiagnosticLevel.FREEZE, DiagnosticLevel.WARN), sink.entries.map { it.level })
    }

    @Test
    fun start_startsProbeAndChecksPeriodically() = runBlocking {
        probe.heartbeatMs = 0L
        now = 5000L
        val watchdog = watchdog()

        watchdog.start()
        val entry = sink.awaitEntry()

        assertEquals(DiagnosticLevel.FREEZE, entry.level)
        assertEquals(1, probe.startCount)
        watchdog.stop()
    }

    @Test
    fun start_calledTwice_doesNotRestartProbe() = runBlocking {
        probe.heartbeatMs = 0L
        now = 5000L
        val watchdog = watchdog()

        watchdog.start()
        sink.awaitEntry()
        watchdog.start()

        assertEquals(1, probe.startCount)
        watchdog.stop()
    }

    @Test
    fun stop_stopsProbeAndEndsPeriodicChecks() = runBlocking {
        probe.heartbeatMs = 0L
        now = 5000L
        val watchdog = watchdog()
        watchdog.start()
        sink.awaitEntry()

        watchdog.stop()
        delay(50L)
        val checksAfterStop = probe.checkCount
        delay(50L)

        assertEquals(1, probe.stopCount)
        assertEquals(checksAfterStop, probe.checkCount)
    }
}
