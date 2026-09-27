package com.wafflehq.lib.settings.encryption.access

fun interface DatabaseConversionLauncher {
    fun start(targetEncrypted: Boolean, backupPassword: CharArray?)
}
