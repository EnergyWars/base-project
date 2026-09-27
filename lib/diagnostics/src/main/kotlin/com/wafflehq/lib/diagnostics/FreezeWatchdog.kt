package com.wafflehq.lib.diagnostics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FreezeWatchdog(
    private val logger: DiagnosticLogger,
    private val detector: FreezeDetector = FreezeDetector(),
    private val probe: FreezeProbe = MainThreadFreezeProbe(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val checkIntervalMs: Long = DEFAULT_CHECK_INTERVAL_MS,
    private val nowMs: () -> Long = System::currentTimeMillis
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        probe.start()
        job = scope.launch {
            while (isActive) {
                delay(checkIntervalMs)
                checkOnce()
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        probe.stop()
    }

    internal fun checkOnce() {
        val now = nowMs()
        val heartbeat = probe.lastHeartbeatMs()
        when (detector.evaluate(heartbeat, now)) {
            FreezeTransition.STARTED -> logger.freeze(
                TAG,
                "UI-Thread blockiert seit ${now - heartbeat}ms",
                probe.blockedThreadStackTrace()
            )
            FreezeTransition.RESOLVED -> logger.w(TAG, "UI-Thread wieder reaktiv")
            FreezeTransition.NONE -> Unit
        }
    }

    companion object {
        internal const val TAG = "FreezeWatchdog"
        const val DEFAULT_CHECK_INTERVAL_MS = 1000L
    }
}
