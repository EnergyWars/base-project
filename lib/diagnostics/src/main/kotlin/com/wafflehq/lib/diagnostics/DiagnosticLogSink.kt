package com.wafflehq.lib.diagnostics

enum class DiagnosticLevel { DEBUG, WARN, ERROR, FREEZE, CRASH }

data class DiagnosticLogEntry(
    val timestampEpochMs: Long,
    val level: DiagnosticLevel,
    val tag: String,
    val message: String,
    val stackTrace: String? = null
)

interface DiagnosticLogSink {
    suspend fun log(entry: DiagnosticLogEntry)
}
