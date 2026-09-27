package com.wafflehq.lib.settings.backup.access

interface BackupScheduleGateway {

    suspend fun schedule()

    fun cancel()
}
