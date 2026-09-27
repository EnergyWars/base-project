package com.wafflehq.lib.diagnostics

import android.os.Handler
import android.os.Looper
import java.util.concurrent.atomic.AtomicLong

interface FreezeProbe {
    fun start()
    fun stop()
    fun lastHeartbeatMs(): Long
    fun blockedThreadStackTrace(): String
}

class MainThreadFreezeProbe(
    private val heartbeatIntervalMs: Long = DEFAULT_HEARTBEAT_INTERVAL_MS,
    private val nowMs: () -> Long = System::currentTimeMillis
) : FreezeProbe {

    private val mainLooper = Looper.getMainLooper()
    private val mainHandler = Handler(mainLooper)
    private val lastHeartbeat = AtomicLong(nowMs())

    private val heartbeat = object : Runnable {
        override fun run() {
            lastHeartbeat.set(nowMs())
            mainHandler.postDelayed(this, heartbeatIntervalMs)
        }
    }

    override fun start() {
        mainHandler.removeCallbacks(heartbeat)
        mainHandler.post(heartbeat)
    }

    override fun stop() {
        mainHandler.removeCallbacks(heartbeat)
    }

    override fun lastHeartbeatMs(): Long = lastHeartbeat.get()

    override fun blockedThreadStackTrace(): String =
        mainLooper.thread.stackTrace.joinToString("\n") { "at $it" }

    companion object {
        const val DEFAULT_HEARTBEAT_INTERVAL_MS = 1000L
    }
}
