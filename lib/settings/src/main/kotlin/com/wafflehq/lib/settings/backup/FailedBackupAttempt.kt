package com.wafflehq.lib.settings.backup

import kotlinx.serialization.Serializable

@Serializable
data class FailedBackupAttempt(
    val id: String,
    val timestampIso: String,
    val reason: String
)

object FailedBackupAttempts {
    const val MAX_ENTRIES = 10

    fun withNewFailure(current: List<FailedBackupAttempt>, attempt: FailedBackupAttempt): List<FailedBackupAttempt> =
        (listOf(attempt) + current).take(MAX_ENTRIES)

    fun withRemoved(current: List<FailedBackupAttempt>, id: String): List<FailedBackupAttempt> =
        current.filterNot { it.id == id }
}
