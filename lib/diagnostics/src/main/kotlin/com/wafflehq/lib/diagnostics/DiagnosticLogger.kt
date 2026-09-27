package com.wafflehq.lib.diagnostics

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DiagnosticLogger(
    private val sink: DiagnosticLogSink,
    private val logcatTag: String = DEFAULT_LOGCAT_TAG,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val nowMs: () -> Long = System::currentTimeMillis
) {

    fun d(tag: String, message: String) {
        Log.d(androidTag(tag), message)
        persist(DiagnosticLevel.DEBUG, tag, message, null)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(androidTag(tag), message, throwable)
        persist(DiagnosticLevel.WARN, tag, message, throwable?.stackTraceToString())
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(androidTag(tag), message, throwable)
        persist(DiagnosticLevel.ERROR, tag, message, throwable?.stackTraceToString())
    }

    fun freeze(tag: String, message: String, mainThreadStackTrace: String) {
        Log.e(androidTag(tag), "$message\n$mainThreadStackTrace")
        persist(DiagnosticLevel.FREEZE, tag, message, mainThreadStackTrace)
    }

    fun crash(tag: String, message: String, throwable: Throwable) {
        Log.e(androidTag(tag), message, throwable)
        persist(DiagnosticLevel.CRASH, tag, message, throwable.stackTraceToString())
    }

    private fun persist(level: DiagnosticLevel, tag: String, message: String, stackTrace: String?) {
        val entry = DiagnosticLogEntry(
            timestampEpochMs = nowMs(),
            level = level,
            tag = tag,
            message = message,
            stackTrace = stackTrace
        )
        scope.launch {
            runCatching { sink.log(entry) }
        }
    }

    private fun androidTag(tag: String) = "$logcatTag.$tag"

    companion object {
        const val DEFAULT_LOGCAT_TAG = "Diagnostics"
    }
}
