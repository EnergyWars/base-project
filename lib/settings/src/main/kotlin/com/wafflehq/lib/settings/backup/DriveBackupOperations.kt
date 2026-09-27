package com.wafflehq.lib.settings.backup

import android.content.Intent
import com.wafflehq.lib.settings.backup.access.AuthorizationOutcome
import com.wafflehq.lib.settings.backup.access.BackupOperations
import com.wafflehq.lib.settings.google.GoogleAuthorization
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

class DriveBackupOperations(private val repository: DriveBackupRepository) : BackupOperations {

    override val accountEmail: Flow<String> = repository.accountEmail

    override fun accountChooserIntent(): Intent = repository.accountChooserIntent()

    override suspend fun selectAccount(email: String) {
        repository.selectAccount(email)
    }

    override suspend fun authorize(): AuthorizationOutcome =
        runCatching { repository.authorize() }.fold(
            onSuccess = { it.toOutcome() },
            onFailure = { AuthorizationOutcome.Failed(it) }
        )

    override suspend fun onAuthorizationResult(data: Intent?): AuthorizationOutcome =
        repository.resultFromIntent(data).fold(
            onSuccess = { it.toOutcome() },
            onFailure = { AuthorizationOutcome.Failed(it) }
        )

    override suspend fun hasRequiredScopes(): Boolean = repository.hasRequiredScopes()

    override fun isBackupOnlyEncryptionEnabled(): Boolean = repository.isBackupOnlyEncryptionEnabled()

    override suspend fun signOut() {
        repository.signOut()
    }

    override suspend fun createBackup(isManual: Boolean): Result<BackupMeta> = repository.createBackup(isManual)

    override suspend fun listBackups(): Result<List<BackupMeta>> = repository.listBackups()

    override suspend fun restoreBackup(meta: BackupMeta, password: CharArray?): Result<RestoreOutcome> =
        repository.restoreBackup(meta, password)

    override suspend fun deleteBackup(meta: BackupMeta): Result<Unit> = repository.deleteBackup(meta)

    override suspend fun exportToStream(out: OutputStream): Result<Unit> = repository.exportToStream(out)

    override suspend fun importFromStream(input: InputStream, password: CharArray?): Result<RestoreOutcome> =
        repository.importFromStream(input, password)

    private suspend fun GoogleAuthorization.toOutcome(): AuthorizationOutcome = when (this) {
        is GoogleAuthorization.ResolutionRequired -> AuthorizationOutcome.ResolutionRequired(pendingIntent)
        is GoogleAuthorization.Granted -> {
            repository.onAuthorized()
            AuthorizationOutcome.Granted
        }
    }
}
