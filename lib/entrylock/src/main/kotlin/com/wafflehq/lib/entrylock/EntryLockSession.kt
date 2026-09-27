package com.wafflehq.lib.entrylock

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class EntryLockSession(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val sessionDurationMillis: Long = SESSION_DURATION_MILLIS,
    private val clockMillis: () -> Long = { System.nanoTime() / NANOS_PER_MILLI }
) {
    private val _relockRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val relockRequests: SharedFlow<Unit> = _relockRequests.asSharedFlow()

    private var timerJob: Job? = null

    @Volatile
    private var authPromptActive = false

    @Volatile
    private var lastPromptFinishedAt: Long? = null

    @Synchronized
    fun onSessionUnlocked() {
        timerJob?.cancel()
        timerJob = scope.launch {
            delay(sessionDurationMillis)
            _relockRequests.tryEmit(Unit)
        }
    }

    fun onAuthPromptShown() {
        authPromptActive = true
    }

    fun onAuthPromptFinished() {
        authPromptActive = false
        lastPromptFinishedAt = clockMillis()
    }

    suspend fun <T> guardAuthPrompt(block: suspend () -> T): T {
        onAuthPromptShown()
        try {
            return block()
        } finally {
            onAuthPromptFinished()
        }
    }

    @Synchronized
    fun onAppForegroundedAfterBackground() {
        if (authPromptActive || isWithinPromptGrace()) return
        timerJob?.cancel()
        timerJob = null
        _relockRequests.tryEmit(Unit)
    }

    private fun isWithinPromptGrace(): Boolean {
        val finishedAt = lastPromptFinishedAt ?: return false
        return clockMillis() - finishedAt <= PROMPT_GRACE_MILLIS
    }

    companion object {
        const val SESSION_DURATION_MILLIS = 10 * 60 * 1000L
        const val PROMPT_GRACE_MILLIS = 1_000L
        private const val NANOS_PER_MILLI = 1_000_000L
    }
}
