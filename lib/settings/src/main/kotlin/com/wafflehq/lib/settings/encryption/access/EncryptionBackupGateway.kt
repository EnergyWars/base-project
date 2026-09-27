package com.wafflehq.lib.settings.encryption.access

import com.wafflehq.lib.settings.encryption.PasswordReminderInterval
import kotlinx.coroutines.flow.Flow

interface EncryptionBackupGateway {

    fun isBackupOnlyEncryptionEnabled(): Boolean

    suspend fun enableBackupOnlyEncryption(password: CharArray): Boolean

    suspend fun disableBackupOnlyEncryption()

    suspend fun replaceAllBackupsWithFreshOne(): Boolean

    val passwordReminderInterval: Flow<PasswordReminderInterval>

    suspend fun setPasswordReminderInterval(interval: PasswordReminderInterval)
}
