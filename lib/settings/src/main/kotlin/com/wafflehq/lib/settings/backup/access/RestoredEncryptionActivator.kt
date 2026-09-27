package com.wafflehq.lib.settings.backup.access

fun interface RestoredEncryptionActivator {
    fun activate(recoveredDek: ByteArray, backupPassword: CharArray)
}
