package com.wafflehq.lib.diagnostics

import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class AppLifecycleDiagnosticsLoggerTest {

    private val sink = RecordingDiagnosticLogSink()
    private lateinit var callbacks: AppLifecycleDiagnosticsLogger
    private lateinit var activity: Activity

    @Before
    fun setup() {
        callbacks = AppLifecycleDiagnosticsLogger(
            DiagnosticLogger(sink, scope = CoroutineScope(Dispatchers.Unconfined))
        )
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
    }

    @Test
    fun onActivityCreated_withoutSavedState_logsPlainCreate() {
        callbacks.onActivityCreated(activity, null)

        val entry = sink.entries.single()
        assertEquals(DiagnosticLevel.DEBUG, entry.level)
        assertEquals(AppLifecycleDiagnosticsLogger.TAG, entry.tag)
        assertTrue(entry.message.contains("onCreate"))
        assertTrue(entry.message.contains("Activity"))
    }

    @Test
    fun onActivityCreated_withSavedState_marksAsRecreated() {
        callbacks.onActivityCreated(activity, android.os.Bundle())

        assertTrue(sink.entries.single().message.contains("recreated"))
    }

    @Test
    fun activityLifecycleCallbacks_logEveryTransition() {
        callbacks.onActivityStarted(activity)
        callbacks.onActivityResumed(activity)
        callbacks.onActivityPaused(activity)
        callbacks.onActivityStopped(activity)
        callbacks.onActivityDestroyed(activity)

        assertEquals(
            listOf("onStart", "onResume", "onPause", "onStop", "onDestroy"),
            sink.entries.map { it.message.substringAfter(' ') }
        )
    }

    @Test
    fun onActivitySaveInstanceState_isNotLogged() {
        callbacks.onActivitySaveInstanceState(activity, android.os.Bundle())

        assertTrue(sink.entries.isEmpty())
    }

    @Test
    fun onTrimMemory_logsWarningWithLevel() {
        val runningCriticalLevel = 15

        callbacks.onTrimMemory(runningCriticalLevel)

        val entry = sink.entries.single()
        assertEquals(DiagnosticLevel.WARN, entry.level)
        assertTrue(entry.message.contains(runningCriticalLevel.toString()))
    }

    @Test
    fun onLowMemory_logsWarning() {
        callbacks.onLowMemory()

        val entry = sink.entries.single()
        assertEquals(DiagnosticLevel.WARN, entry.level)
        assertTrue(entry.message.contains("onLowMemory"))
    }
}
