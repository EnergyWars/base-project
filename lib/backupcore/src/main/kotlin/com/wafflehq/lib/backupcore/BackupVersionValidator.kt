package com.wafflehq.lib.backupcore

object BackupVersionValidator {
    fun validate(backupVersionCode: Int, currentVersionCode: Int) {
        if (backupVersionCode > currentVersionCode) {
            throw BackupVersionTooNewException(backupVersionCode, currentVersionCode)
        }
    }
}
