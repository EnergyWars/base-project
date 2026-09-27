package com.wafflehq.lib.settings.backup.access

interface BackupPayloadProvider {

    suspend fun collect(): String

    suspend fun apply(payloadJson: String)

    fun isLegacyPayload(text: String): Boolean
}
