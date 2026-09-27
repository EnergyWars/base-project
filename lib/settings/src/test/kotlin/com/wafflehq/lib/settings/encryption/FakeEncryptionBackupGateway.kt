package com.wafflehq.lib.settings.encryption

import com.wafflehq.lib.settings.encryption.access.EncryptionBackupGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeEncryptionBackupGateway(
    private var backupOnlyEncryptionEnabled: Boolean = false,
    var enableSucceeds: Boolean = true,
    var replaceSucceeds: Boolean = true
) : EncryptionBackupGateway {

    val enabledPasswords = mutableListOf<String>()
    var disableCount = 0
        private set
    var replaceCount = 0
        private set

    private val intervalFlow = MutableStateFlow(PasswordReminderInterval.NEVER)

    override fun isBackupOnlyEncryptionEnabled(): Boolean = backupOnlyEncryptionEnabled

    override suspend fun enableBackupOnlyEncryption(password: CharArray): Boolean {
        enabledPasswords += String(password)
        if (enableSucceeds) backupOnlyEncryptionEnabled = true
        return enableSucceeds
    }

    override suspend fun disableBackupOnlyEncryption() {
        disableCount++
        backupOnlyEncryptionEnabled = false
    }

    override suspend fun replaceAllBackupsWithFreshOne(): Boolean {
        replaceCount++
        return replaceSucceeds
    }

    override val passwordReminderInterval: Flow<PasswordReminderInterval> = intervalFlow

    override suspend fun setPasswordReminderInterval(interval: PasswordReminderInterval) {
        intervalFlow.value = interval
    }

    val storedInterval: PasswordReminderInterval get() = intervalFlow.value
}
