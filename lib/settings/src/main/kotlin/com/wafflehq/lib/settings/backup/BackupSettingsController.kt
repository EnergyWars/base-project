package com.wafflehq.lib.settings.backup

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import com.wafflehq.lib.backupcore.BackupRetentionMode
import com.wafflehq.lib.database.state.EncryptionStateStore
import com.wafflehq.lib.settings.backup.access.AuthorizationOutcome
import com.wafflehq.lib.settings.backup.access.BackupFileGateway
import com.wafflehq.lib.settings.backup.access.BackupOperations
import com.wafflehq.lib.settings.backup.access.BackupScheduleGateway
import com.wafflehq.lib.settings.backup.access.RestoredEncryptionActivator
import com.wafflehq.lib.settings.core.SettingsText
import com.wafflehq.lib.settings.google.isUserCancelledAuthorization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

sealed interface RestoreSource {
    data class Drive(val meta: BackupMeta) : RestoreSource
    data class LocalFile(val uri: Uri) : RestoreSource
}

data class PasswordPromptState(
    val source: RestoreSource,
    val wrongPassword: Boolean = false
)

enum class BackupMessage { CREATED, EXPORTED, RESTORED, DELETED }

data class BackupUiState(
    val isSignedIn: Boolean = false,
    val accountEmail: String = "",
    val backups: List<BackupMeta> = emptyList(),
    val isLoading: Boolean = false,
    val lastBackupTime: String = "",
    val error: SettingsText? = null,
    val signOutConfirm: Boolean = false,
    val createConfirm: Boolean = false,
    val restoreConfirmTarget: BackupMeta? = null,
    val deleteConfirmTarget: BackupMeta? = null,
    val localImportConfirmUri: Uri? = null,
    val passwordPrompt: PasswordPromptState? = null,
    val snackbarMessage: BackupMessage? = null,
    val autoBackupEnabled: Boolean = false,
    val backupTimeMinutes: Int = BackupPreferenceStore.DEFAULT_BACKUP_TIME_MINUTES,
    val useQuickTimeInput: Boolean = false,
    val retentionPolicyEnabled: Boolean = true,
    val retentionMode: BackupRetentionMode = BackupRetentionMode.COUNT,
    val retentionMaxCount: Int = BackupPreferenceStore.DEFAULT_RETENTION_MAX_COUNT,
    val retentionMaxAgeDays: Int = BackupPreferenceStore.DEFAULT_RETENTION_MAX_AGE_DAYS
)

class BackupSettingsController(
    private val operations: BackupOperations,
    private val backupPrefs: BackupPreferenceStore,
    private val scheduler: BackupScheduleGateway,
    private val files: BackupFileGateway,
    private val restoredEncryptionActivator: RestoredEncryptionActivator,
    encryptionStateStore: EncryptionStateStore,
    useQuickTimeInput: Flow<Boolean>,
    private val scope: CoroutineScope
) {

    val backupsAreEncrypted: Boolean = encryptionStateStore.isEncrypted || operations.isBackupOnlyEncryptionEnabled()

    private val _state = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _state.asStateFlow()

    private val _accountPickerEvent = MutableSharedFlow<Intent>(extraBufferCapacity = 1)
    val accountPickerEvent: SharedFlow<Intent> = _accountPickerEvent.asSharedFlow()

    private val _authorizationRequiredEvent = MutableSharedFlow<PendingIntent>(extraBufferCapacity = 1)
    val authorizationRequiredEvent: SharedFlow<PendingIntent> = _authorizationRequiredEvent.asSharedFlow()

    init {
        scope.launch {
            backupPrefs.lastBackupTime.collect { time ->
                _state.update { it.copy(lastBackupTime = time) }
            }
        }
        scope.launch {
            backupPrefs.backupEnabled.collect { enabled ->
                _state.update { it.copy(autoBackupEnabled = enabled) }
            }
        }
        scope.launch {
            backupPrefs.backupTimeMinutes.collect { minutes ->
                _state.update { it.copy(backupTimeMinutes = minutes) }
            }
        }
        scope.launch {
            useQuickTimeInput.collect { enabled ->
                _state.update { it.copy(useQuickTimeInput = enabled) }
            }
        }
        scope.launch {
            backupPrefs.retentionPolicyEnabled.collect { enabled ->
                _state.update { it.copy(retentionPolicyEnabled = enabled) }
            }
        }
        scope.launch {
            backupPrefs.retentionMode.collect { mode ->
                _state.update { it.copy(retentionMode = mode) }
            }
        }
        scope.launch {
            backupPrefs.retentionMaxCount.collect { count ->
                _state.update { it.copy(retentionMaxCount = count) }
            }
        }
        scope.launch {
            backupPrefs.retentionMaxAgeDays.collect { days ->
                _state.update { it.copy(retentionMaxAgeDays = days) }
            }
        }
        refreshSignInState()
    }

    fun requestSignIn() {
        _accountPickerEvent.tryEmit(operations.accountChooserIntent())
    }

    fun onAccountPicked(email: String?) {
        if (email.isNullOrEmpty()) return
        scope.launch {
            operations.selectAccount(email)
            applyAuthorizationOutcome(operations.authorize())
        }
    }

    fun onAuthorizationResult(data: Intent?) {
        scope.launch { applyAuthorizationOutcome(operations.onAuthorizationResult(data)) }
    }

    private fun applyAuthorizationOutcome(outcome: AuthorizationOutcome) {
        when (outcome) {
            AuthorizationOutcome.Granted -> onSignedIn()
            is AuthorizationOutcome.ResolutionRequired -> _authorizationRequiredEvent.tryEmit(outcome.pendingIntent)
            is AuthorizationOutcome.Failed ->
                if (!outcome.cause.isUserCancelledAuthorization) {
                    _state.update { it.copy(error = BackupErrorMessages.describe(outcome.cause)) }
                }
        }
    }

    fun onAutoBackupToggled(enabled: Boolean) {
        scope.launch {
            backupPrefs.setBackupEnabled(enabled)
            if (enabled) scheduler.schedule() else scheduler.cancel()
        }
    }

    fun onBackupTimeChanged(minutes: Int) {
        scope.launch {
            backupPrefs.setBackupTimeMinutes(minutes)
            scheduler.schedule()
        }
    }

    fun onRetentionPolicyToggled(enabled: Boolean) {
        scope.launch { backupPrefs.setRetentionPolicyEnabled(enabled) }
    }

    fun onRetentionModeChanged(mode: BackupRetentionMode) {
        scope.launch { backupPrefs.setRetentionMode(mode) }
    }

    fun onRetentionMaxCountChanged(count: Int) {
        scope.launch { backupPrefs.setRetentionMaxCount(count) }
    }

    fun onRetentionMaxAgeDaysChanged(days: Int) {
        scope.launch { backupPrefs.setRetentionMaxAgeDays(days) }
    }

    fun refreshSignInState() {
        scope.launch {
            val hasScopes = operations.hasRequiredScopes()
            val email = operations.accountEmail.first()
            val signedIn = hasScopes && email.isNotEmpty()
            _state.update { it.copy(isSignedIn = signedIn, accountEmail = if (signedIn) email else "") }
            if (signedIn) loadBackups()
        }
    }

    fun onSignedIn() {
        refreshSignInState()
        scope.launch {
            if (!backupPrefs.backupEnabled.first()) {
                backupPrefs.setBackupEnabled(true)
                scheduler.schedule()
            }
        }
    }

    fun confirmSignOut() { _state.update { it.copy(signOutConfirm = true) } }

    fun dismissSignOut() { _state.update { it.copy(signOutConfirm = false) } }

    fun signOut() {
        scope.launch {
            _state.update { it.copy(signOutConfirm = false) }
            operations.signOut()
            _state.update { it.copy(isSignedIn = false, accountEmail = "", backups = emptyList()) }
            backupPrefs.setBackupEnabled(false)
            scheduler.cancel()
        }
    }

    fun loadBackups() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val ghosts = backupPrefs.failedBackupAttempts.first().map { attempt ->
                BackupMeta(
                    id = attempt.id,
                    name = attempt.reason,
                    createdAt = runCatching { Instant.parse(attempt.timestampIso) }.getOrDefault(Instant.EPOCH),
                    encrypted = false,
                    isManual = false,
                    isFailed = true,
                    failureReason = attempt.reason
                )
            }
            operations.listBackups().fold(
                onSuccess = { list ->
                    val merged = (list + ghosts).sortedByDescending { it.createdAt }
                    _state.update { it.copy(backups = merged, isLoading = false) }
                },
                onFailure = { e -> _state.update { it.copy(error = handleFailure(e), isLoading = false) } }
            )
        }
    }

    fun confirmCreateBackup() { _state.update { it.copy(createConfirm = true) } }

    fun dismissCreateBackup() { _state.update { it.copy(createConfirm = false) } }

    fun createBackup() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null, createConfirm = false) }
            operations.createBackup(isManual = true).fold(
                onSuccess = { meta ->
                    backupPrefs.setLastBackupTime(meta.createdAt.toString())
                    _state.update { it.copy(isLoading = false, snackbarMessage = BackupMessage.CREATED) }
                    loadBackups()
                },
                onFailure = { e -> _state.update { it.copy(error = handleFailure(e), isLoading = false) } }
            )
        }
    }

    fun clearSnackbar() { _state.update { it.copy(snackbarMessage = null) } }

    fun confirmRestore(meta: BackupMeta) { _state.update { it.copy(restoreConfirmTarget = meta) } }

    fun dismissRestore() { _state.update { it.copy(restoreConfirmTarget = null) } }

    fun restoreBackup() {
        val meta = _state.value.restoreConfirmTarget ?: return
        _state.update { it.copy(restoreConfirmTarget = null) }
        performRestore(RestoreSource.Drive(meta), password = null)
    }

    fun dismissPasswordPrompt() { _state.update { it.copy(passwordPrompt = null) } }

    fun retryWithPassword(password: String) {
        val prompt = _state.value.passwordPrompt ?: return
        performRestore(prompt.source, password.toCharArray())
    }

    fun confirmLocalImport(uri: Uri) { _state.update { it.copy(localImportConfirmUri = uri) } }

    fun dismissLocalImport() { _state.update { it.copy(localImportConfirmUri = null) } }

    fun importFromFile() {
        val uri = _state.value.localImportConfirmUri ?: return
        _state.update { it.copy(localImportConfirmUri = null) }
        performRestore(RestoreSource.LocalFile(uri), password = null)
    }

    fun exportToFile(uri: Uri) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = files.openForWrite(uri)?.use { out -> operations.exportToStream(out) }
                ?: Result.failure(IllegalStateException("Could not open $uri for writing"))
            result.fold(
                onSuccess = {
                    backupPrefs.setHasLocalExport(true)
                    _state.update { it.copy(isLoading = false, snackbarMessage = BackupMessage.EXPORTED) }
                },
                onFailure = { e -> _state.update { it.copy(error = handleFailure(e), isLoading = false) } }
            )
        }
    }

    private fun performRestore(source: RestoreSource, password: CharArray?) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null, passwordPrompt = null) }
            val result = when (source) {
                is RestoreSource.Drive -> operations.restoreBackup(source.meta, password)
                is RestoreSource.LocalFile -> files.openForRead(source.uri)?.use { input ->
                    operations.importFromStream(input, password)
                } ?: Result.failure(IllegalStateException("Could not open ${source.uri} for reading"))
            }
            result.fold(
                onSuccess = { outcome ->
                    _state.update { it.copy(isLoading = false, snackbarMessage = BackupMessage.RESTORED) }
                    reenableLocalEncryptionIfRecovered(outcome.recoveredDek, password)
                },
                onFailure = { e -> handlePasswordAwareFailure(e, source) }
            )
        }
    }

    private fun handlePasswordAwareFailure(e: Throwable, source: RestoreSource) {
        when (e) {
            is BackupPasswordRequiredException ->
                _state.update { it.copy(isLoading = false, passwordPrompt = PasswordPromptState(source)) }
            is BackupWrongPasswordException ->
                _state.update { it.copy(isLoading = false, passwordPrompt = PasswordPromptState(source, wrongPassword = true)) }
            else -> _state.update { it.copy(error = handleFailure(e), isLoading = false) }
        }
    }

    private fun reenableLocalEncryptionIfRecovered(recoveredDek: ByteArray?, password: CharArray?) {
        if (recoveredDek != null && password != null) {
            restoredEncryptionActivator.activate(recoveredDek, password)
        }
    }

    fun confirmDelete(meta: BackupMeta) { _state.update { it.copy(deleteConfirmTarget = meta) } }

    fun dismissDelete() { _state.update { it.copy(deleteConfirmTarget = null) } }

    fun deleteBackup() {
        val meta = _state.value.deleteConfirmTarget ?: return
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null, deleteConfirmTarget = null) }
            if (meta.isFailed) {
                backupPrefs.removeFailedBackupAttempt(meta.id)
                _state.update { it.copy(isLoading = false) }
                loadBackups()
                return@launch
            }
            operations.deleteBackup(meta).fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, snackbarMessage = BackupMessage.DELETED) }
                    loadBackups()
                },
                onFailure = { e -> _state.update { it.copy(error = handleFailure(e), isLoading = false) } }
            )
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }

    private fun handleFailure(e: Throwable): SettingsText {
        if (e is DriveAuthorizationRequiredException) {
            _authorizationRequiredEvent.tryEmit(e.pendingIntent)
        }
        return BackupErrorMessages.describe(e)
    }
}
