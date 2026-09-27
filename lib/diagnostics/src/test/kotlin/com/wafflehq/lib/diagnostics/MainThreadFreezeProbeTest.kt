package com.wafflehq.lib.diagnostics

import android.os.Looper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class MainThreadFreezeProbeTest {

    private var now = 1_000L
    private val probe = MainThreadFreezeProbe(heartbeatIntervalMs = 100L, nowMs = { now })

    private fun idleMainLooper(millis: Long = 0L) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis))
    }

    @Test
    fun lastHeartbeat_beforeStart_isConstructionTime() {
        assertEquals(1_000L, probe.lastHeartbeatMs())
    }

    @Test
    fun start_updatesHeartbeatOnMainThread() {
        now = 2_000L

        probe.start()
        idleMainLooper()

        assertEquals(2_000L, probe.lastHeartbeatMs())
    }

    @Test
    fun heartbeat_repostsItselfAfterInterval() {
        probe.start()
        idleMainLooper()
        now = 3_000L

        idleMainLooper(millis = 100L)

        assertEquals(3_000L, probe.lastHeartbeatMs())
    }

    @Test
    fun stop_preventsFurtherHeartbeats() {
        probe.start()
        idleMainLooper()
        val lastBeforeStop = probe.lastHeartbeatMs()

        probe.stop()
        now = 9_000L
        idleMainLooper(millis = 500L)

        assertEquals(lastBeforeStop, probe.lastHeartbeatMs())
    }

    @Test
    fun blockedThreadStackTrace_listsMainThreadFrames() {
        val trace = probe.blockedThreadStackTrace()

        assertTrue(trace.contains("at "))
    }
}
