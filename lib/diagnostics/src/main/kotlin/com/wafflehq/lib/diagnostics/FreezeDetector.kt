package com.wafflehq.lib.diagnostics

enum class FreezeTransition { NONE, STARTED, RESOLVED }

class FreezeDetector(private val thresholdMs: Long = DEFAULT_THRESHOLD_MS) {

    var isFrozen: Boolean = false
        private set

    fun evaluate(lastHeartbeatMs: Long, nowMs: Long): FreezeTransition {
        val nowFrozen = nowMs - lastHeartbeatMs >= thresholdMs
        val transition = when {
            nowFrozen && !isFrozen -> FreezeTransition.STARTED
            !nowFrozen && isFrozen -> FreezeTransition.RESOLVED
            else -> FreezeTransition.NONE
        }
        isFrozen = nowFrozen
        return transition
    }

    companion object {
        const val DEFAULT_THRESHOLD_MS = 3000L
    }
}
