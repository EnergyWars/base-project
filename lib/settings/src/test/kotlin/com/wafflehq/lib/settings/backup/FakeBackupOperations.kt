package com.wafflehq.lib.settings.backup

import android.content.Intent
import com.wafflehq.lib.settings.backup.access.AuthorizationOutcome
import com.wafflehq.lib.settings.backup.access.BackupOperations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant

internal class FakeBackupOperations(
    var hasScopes: Boolean = true,
    var backupOnlyEncryption: Boolean = false
) : BackupOperations {

    private val email = MutableStateFlow("")

    var authorizeOutcome: AuthorizationOutcome = AuthorizationOutcome.Granted
    val selectedAccounts = mutableListOf<String>()
    var authorizeCount = 0
    val authorizationResultIntents = mutableListOf<Intent?>()

    var listResult: Result<List<BackupMeta>> = Result.success(emptyList())
    var createResult: Result<BackupMeta> = Result.success(backupMeta("created", Instant.parse("2026-03-01T10:00:00Z")))
    var restoreResult: Result<RestoreOutcome> = Result.success(RestoreOutcome())
    var deleteResult: Result<Unit> = Result.success(Unit)
    var exportResult: Result<Unit> = Result.success(Unit)
    var importResult: Result<RestoreOutcome> = Result.success(RestoreOutcome())
    var exportPayload: ByteArray = "exported-payload".toByteArray()

    var signOutCount = 0
    var listCount = 0
    val createdManualFlags = mutableListOf<Boolean>()
    val restoreCalls = mutableListOf<Pair<String, String?>>()
    val importedPasswords = mutableListOf<String?>()
    val importedBytes = mutableListOf<String>()
    val deletedIds = mutableListOf<String>()

    override val accountEmail: Flow<String> = email

    override fun accountChooserIntent(): Intent = Intent("fake.account.chooser")

    override suspend fun selectAccount(email: String) {
        selectedAccounts += email
        this.email.value = email
    }

    override suspend fun authorize(): AuthorizationOutcome {
        authorizeCount++
        return authorizeOutcome
    }

    override suspend fun onAuthorizationResult(data: Intent?): AuthorizationOutcome {
        authorizationResultIntents += data
        return authorizeOutcome
    }

    override suspend fun hasRequiredScopes(): Boolean = hasScopes

    fun linkAccount(address: String) {
        email.value = address
    }

    override fun isBackupOnlyEncryptionEnabled(): Boolean = backupOnlyEncryption

    override suspend fun signOut() {
        signOutCount++
        email.value = ""
    }

    override suspend fun createBackup(isManual: Boolean): Result<BackupMeta> {
        createdManualFlags += isManual
        return createResult
    }

    override suspend fun listBackups(): Result<List<BackupMeta>> {
        listCount++
        return listResult
    }

    override suspend fun restoreBackup(meta: BackupMeta, password: CharArray?): Result<RestoreOutcome> {
        restoreCalls += meta.id to password?.concatToString()
        return restoreResult
    }

    override suspend fun deleteBackup(meta: BackupMeta): Result<Unit> {
        deletedIds += meta.id
        return deleteResult
    }

    override suspend fun exportToStream(out: OutputStream): Result<Unit> {
        out.write(exportPayload)
        return exportResult
    }

    override suspend fun importFromStream(input: InputStream, password: CharArray?): Result<RestoreOutcome> {
        importedBytes += input.readBytes().decodeToString()
        importedPasswords += password?.concatToString()
        return importResult
    }
}

internal fun backupMeta(
    id: String,
    createdAt: Instant,
    encrypted: Boolean = false,
    isManual: Boolean = false,
    sizeBytes: Long? = null,
    isPreRestoreSnapshot: Boolean = false
) = BackupMeta(
    id = id,
    name = "backup_$id",
    createdAt = createdAt,
    encrypted = encrypted,
    isManual = isManual,
    sizeBytes = sizeBytes,
    isPreRestoreSnapshot = isPreRestoreSnapshot
)
