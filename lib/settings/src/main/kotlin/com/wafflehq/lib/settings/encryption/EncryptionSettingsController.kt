package com.wafflehq.lib.settings.encryption

import com.wafflehq.lib.database.BackupPasswordVerifier
import com.wafflehq.lib.database.crypto.KeystoreKeyWrapper
import com.wafflehq.lib.database.crypto.PasswordKeyWrapper
import com.wafflehq.lib.database.state.ConversionUiState
import com.wafflehq.lib.database.state.EncryptionConversionBus
import com.wafflehq.lib.database.state.EncryptionStateStore
import com.wafflehq.lib.settings.encryption.access.DatabaseConversionLauncher
import com.wafflehq.lib.settings.encryption.access.EncryptionBackupGateway
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ChangePasswordResult {
    data object Idle : ChangePasswordResult
    data object InProgress : ChangePasswordResult
    data object Success : ChangePasswordResult
    data object Error : ChangePasswordResult
}

sealed interface BackupOnlyEncryptionResult {
    data object Idle : BackupOnlyEncryptionResult
    data object InProgress : BackupOnlyEncryptionResult
    data object Success : BackupOnlyEncryptionResult
    data object Error : BackupOnlyEncryptionResult
}

sealed interface BackupReplaceResult {
    data object Idle : BackupReplaceResult
    data object InProgress : BackupReplaceResult
    data object Success : BackupReplaceResult
    data object Error : BackupReplaceResult
}

class EncryptionSettingsController(
    private val encryptionStateStore: EncryptionStateStore,
    private val keyWrapper: KeystoreKeyWrapper,
    private val passwordWrapper: PasswordKeyWrapper,
    private val backupPasswordVerifier: BackupPasswordVerifier,
    private val backupGateway: EncryptionBackupGateway,
    private val conversionLauncher: DatabaseConversionLauncher,
    private val ioDispatcher: CoroutineDispatcher,
    private val scope: CoroutineScope
) {

    val isDatabaseEncrypted: Boolean = encryptionStateStore.isEncrypted

    val hasBackupPassword: Boolean = encryptionStateStore.readPasswordWrappedDek() != null

    val uiState: StateFlow<ConversionUiState> = EncryptionConversionBus.state

    private val _changePasswordState = MutableStateFlow<ChangePasswordResult>(ChangePasswordResult.Idle)
    val changePasswordState: StateFlow<ChangePasswordResult> = _changePasswordState.asStateFlow()

    private val _hasBackupOnlyEncryption = MutableStateFlow(backupGateway.isBackupOnlyEncryptionEnabled())
    val hasBackupOnlyEncryption: StateFlow<Boolean> = _hasBackupOnlyEncryption.asStateFlow()

    private val _backupOnlyEncryptionState = MutableStateFlow<BackupOnlyEncryptionResult>(BackupOnlyEncryptionResult.Idle)
    val backupOnlyEncryptionState: StateFlow<BackupOnlyEncryptionResult> = _backupOnlyEncryptionState.asStateFlow()

    fun enableBackupOnlyEncryption(password: String) {
        scope.launch {
            _backupOnlyEncryptionState.value = BackupOnlyEncryptionResult.InProgress
            val succeeded = withContext(ioDispatcher) {
                backupGateway.enableBackupOnlyEncryption(password.toCharArray())
            }
            if (succeeded) _hasBackupOnlyEncryption.value = true
            _backupOnlyEncryptionState.value = if (succeeded) BackupOnlyEncryptionResult.Success else BackupOnlyEncryptionResult.Error
        }
    }

    fun disableBackupOnlyEncryption() {
        scope.launch {
            withContext(ioDispatcher) { backupGateway.disableBackupOnlyEncryption() }
            _hasBackupOnlyEncryption.value = false
        }
    }

    fun dismissBackupOnlyEncryptionResult() {
        _backupOnlyEncryptionState.value = BackupOnlyEncryptionResult.Idle
    }

    private val _passwordTestResult = MutableStateFlow<PasswordTestResult>(PasswordTestResult.Idle)
    val passwordTestResult: StateFlow<PasswordTestResult> = _passwordTestResult.asStateFlow()

    val passwordReminderInterval: StateFlow<PasswordReminderInterval> = backupGateway.passwordReminderInterval
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), PasswordReminderInterval.NEVER)

    fun startConversion(backupPassword: CharArray? = null) {
        conversionLauncher.start(
            targetEncrypted = !isDatabaseEncrypted,
            backupPassword = backupPassword
        )
    }

    fun changeBackupPassword(newPassword: String) {
        scope.launch {
            _changePasswordState.value = ChangePasswordResult.InProgress
            val succeeded = withContext(ioDispatcher) {
                try {
                    val wrappedDek = encryptionStateStore.readWrappedDek() ?: return@withContext false
                    val dek = keyWrapper.unwrap(wrappedDek)
                    val newWrap = passwordWrapper.wrap(dek, newPassword.toCharArray())
                    encryptionStateStore.writePasswordWrappedDek(newWrap)
                    true
                } catch (e: Exception) {
                    false
                }
            }
            _changePasswordState.value = if (succeeded) ChangePasswordResult.Success else ChangePasswordResult.Error
        }
    }

    fun dismissChangePasswordResult() {
        _changePasswordState.value = ChangePasswordResult.Idle
    }

    private val _backupReplaceState = MutableStateFlow<BackupReplaceResult>(BackupReplaceResult.Idle)
    val backupReplaceState: StateFlow<BackupReplaceResult> = _backupReplaceState.asStateFlow()

    fun replaceAllBackupsWithFreshOne() {
        scope.launch {
            _backupReplaceState.value = BackupReplaceResult.InProgress
            val succeeded = withContext(ioDispatcher) { backupGateway.replaceAllBackupsWithFreshOne() }
            _backupReplaceState.value = if (succeeded) BackupReplaceResult.Success else BackupReplaceResult.Error
        }
    }

    fun dismissBackupReplaceResult() {
        _backupReplaceState.value = BackupReplaceResult.Idle
    }

    fun testBackupPassword(password: String) {
        scope.launch {
            _passwordTestResult.value = PasswordTestResult.Checking
            val correct = withContext(ioDispatcher) {
                backupPasswordVerifier.verify(password.toCharArray())
            }
            _passwordTestResult.value = if (correct) PasswordTestResult.Correct else PasswordTestResult.Incorrect
        }
    }

    fun dismissPasswordTestResult() {
        _passwordTestResult.value = PasswordTestResult.Idle
    }

    fun onPasswordReminderIntervalChanged(interval: PasswordReminderInterval) {
        scope.launch { backupGateway.setPasswordReminderInterval(interval) }
    }
}
