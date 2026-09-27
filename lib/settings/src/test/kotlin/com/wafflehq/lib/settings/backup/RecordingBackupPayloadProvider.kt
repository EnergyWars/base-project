package com.wafflehq.lib.settings.backup

import com.wafflehq.lib.settings.backup.access.BackupPayloadProvider

internal class RecordingBackupPayloadProvider(
    private val payload: String = """{"createdAt":"2026-01-01T00:00:00Z","categories":["events"]}""",
    private val legacyMarker: String = "\"createdAt\""
) : BackupPayloadProvider {

    val applied = mutableListOf<String>()

    fun payloadText(): String = payload

    override suspend fun collect(): String = payload

    override suspend fun apply(payloadJson: String) {
        applied += payloadJson
    }

    override fun isLegacyPayload(text: String): Boolean = text.contains(legacyMarker)
}
