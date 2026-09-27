package com.wafflehq.lib.diagnostics

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout
import java.util.concurrent.CopyOnWriteArrayList

class RecordingDiagnosticLogSink(
    private val failure: Throwable? = null
) : DiagnosticLogSink {

    val entries = CopyOnWriteArrayList<DiagnosticLogEntry>()
    private val received = Channel<DiagnosticLogEntry>(Channel.UNLIMITED)

    override suspend fun log(entry: DiagnosticLogEntry) {
        failure?.let { throw it }
        entries += entry
        received.send(entry)
    }

    suspend fun awaitEntry(timeoutMs: Long = 2_000L): DiagnosticLogEntry =
        withTimeout(timeoutMs) { received.receive() }
}
