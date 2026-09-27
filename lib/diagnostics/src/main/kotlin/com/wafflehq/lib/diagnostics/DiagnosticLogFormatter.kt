package com.wafflehq.lib.diagnostics

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DiagnosticLogFormatter {

    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    fun format(entries: List<DiagnosticLogEntry>, emptyPlaceholder: String): String {
        if (entries.isEmpty()) return emptyPlaceholder
        return entries
            .sortedBy { it.timestampEpochMs }
            .joinToString(separator = "\n") { formatEntry(it) }
    }

    private fun formatEntry(entry: DiagnosticLogEntry): String {
        val timestamp = Instant.ofEpochMilli(entry.timestampEpochMs)
            .atZone(ZoneId.systemDefault())
            .format(timestampFormatter)
        val header = "$timestamp ${entry.level} [${entry.tag}] ${entry.message}"
        return if (entry.stackTrace.isNullOrBlank()) header else "$header\n${entry.stackTrace}"
    }
}
