package com.wafflehq.lib.diagnostics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class UncaughtCrashLoggerTest {

    private val sink = RecordingDiagnosticLogSink()
    private val logger = DiagnosticLogger(sink, scope = CoroutineScope(Dispatchers.Unconfined))

    @Test
    fun uncaughtException_logsCrashAndDelegatesToPreviousHandler() {
        var delegatedThread: Thread? = null
        var delegatedThrowable: Throwable? = null
        val previous = Thread.UncaughtExceptionHandler { thread, throwable ->
            delegatedThread = thread
            delegatedThrowable = throwable
        }
        val handler = UncaughtCrashLogger(logger, previous)
        val throwable = RuntimeException("boom")
        val thread = Thread.currentThread()

        handler.uncaughtException(thread, throwable)

        val entry = sink.entries.single()
        assertEquals(DiagnosticLevel.CRASH, entry.level)
        assertEquals(UncaughtCrashLogger.TAG, entry.tag)
        assertTrue(entry.message.contains(thread.name))
        assertTrue(entry.stackTrace!!.contains("boom"))
        assertEquals(thread, delegatedThread)
        assertEquals(throwable, delegatedThrowable)
    }

    @Test
    fun uncaughtException_withoutPreviousHandler_stillLogs() {
        val handler = UncaughtCrashLogger(logger, null)

        handler.uncaughtException(Thread.currentThread(), RuntimeException("boom"))

        val entry = sink.entries.single()
        assertEquals(DiagnosticLevel.CRASH, entry.level)
        assertTrue(entry.stackTrace!!.contains("boom"))
    }
}
