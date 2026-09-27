package com.wafflehq.lib.diagnostics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class DiagnosticLoggerTest {

    private val sink = RecordingDiagnosticLogSink()

    private fun logger(
        sink: DiagnosticLogSink = this.sink,
        logcatTag: String = "TestDiag"
    ) = DiagnosticLogger(
        sink = sink,
        logcatTag = logcatTag,
        scope = CoroutineScope(Dispatchers.Unconfined),
        nowMs = { FIXED_NOW_MS }
    )

    @Test
    fun d_persistsDebugEntryWithTimestamp() {
        logger().d("Test", "hello")

        val entry = sink.entries.single()
        assertEquals(DiagnosticLevel.DEBUG, entry.level)
        assertEquals("Test", entry.tag)
        assertEquals("hello", entry.message)
        assertEquals(FIXED_NOW_MS, entry.timestampEpochMs)
        assertNull(entry.stackTrace)
    }

    @Test
    fun w_withThrowable_persistsStackTrace() {
        logger().w("Test", "watch out", RuntimeException("careful"))

        val entry = sink.entries.single()
        assertEquals(DiagnosticLevel.WARN, entry.level)
        assertTrue(entry.stackTrace!!.contains("careful"))
    }

    @Test
    fun e_withoutThrowable_persistsErrorWithoutStackTrace() {
        logger().e("Test", "failed")

        val entry = sink.entries.single()
        assertEquals(DiagnosticLevel.ERROR, entry.level)
        assertNull(entry.stackTrace)
    }

    @Test
    fun freeze_persistsGivenStackTrace() {
        logger().freeze("Watchdog", "blockiert seit 5000ms", "at com.example.Foo.bar")

        val entry = sink.entries.single()
        assertEquals(DiagnosticLevel.FREEZE, entry.level)
        assertEquals("at com.example.Foo.bar", entry.stackTrace)
    }

    @Test
    fun crash_persistsCrashLevelWithStackTrace() {
        logger().crash("Crash", "unbehandelt", RuntimeException("boom"))

        val entry = sink.entries.single()
        assertEquals(DiagnosticLevel.CRASH, entry.level)
        assertTrue(entry.stackTrace!!.contains("boom"))
    }

    @Test
    fun sinkFailure_doesNotCrashCaller() {
        val failing = RecordingDiagnosticLogSink(failure = IllegalStateException("db closed"))

        logger(sink = failing).e("Test", "should not throw")

        assertTrue(failing.entries.isEmpty())
    }

    @Test
    fun logging_prefixesLogcatTagWithConfiguredPrefix() {
        ShadowLog.clear()

        logger(logcatTag = "MyApp").d("Test", "hello")

        assertTrue(ShadowLog.getLogs().any { it.tag == "MyApp.Test" && it.msg == "hello" })
    }

    private companion object {
        const val FIXED_NOW_MS = 1_700_000_000_000L
    }
}
