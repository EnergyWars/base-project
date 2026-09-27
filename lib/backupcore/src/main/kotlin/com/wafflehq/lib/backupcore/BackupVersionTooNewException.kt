package com.wafflehq.lib.backupcore

class BackupVersionTooNewException(
    val backupVersionCode: Int,
    val currentVersionCode: Int
) : Exception("Backup version $backupVersionCode > app version $currentVersionCode")
