package com.wafflehq.lib.settings.encryption

import com.wafflehq.lib.settings.encryption.access.DatabaseConversionLauncher

class RecordingDatabaseConversionLauncher : DatabaseConversionLauncher {

    data class Start(val targetEncrypted: Boolean, val backupPassword: String?)

    val starts = mutableListOf<Start>()

    override fun start(targetEncrypted: Boolean, backupPassword: CharArray?) {
        starts += Start(targetEncrypted, backupPassword?.let { String(it) })
    }
}
