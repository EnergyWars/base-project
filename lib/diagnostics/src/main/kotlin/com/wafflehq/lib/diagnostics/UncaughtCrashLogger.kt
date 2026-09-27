package com.wafflehq.lib.diagnostics

class UncaughtCrashLogger(
    private val logger: DiagnosticLogger,
    private val previousHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        logger.crash(TAG, "Unbehandelte Exception auf Thread ${thread.name}", throwable)
        previousHandler?.uncaughtException(thread, throwable)
    }

    companion object {
        internal const val TAG = "Crash"
    }
}
