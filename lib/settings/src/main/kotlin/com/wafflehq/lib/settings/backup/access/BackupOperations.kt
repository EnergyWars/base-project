package com.wafflehq.lib.settings.backup.access

import android.app.PendingIntent
import android.content.Intent
import com.wafflehq.lib.settings.backup.BackupMeta
import com.wafflehq.lib.settings.backup.RestoreOutcome
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

sealed interface AuthorizationOutcome {

    data object Granted : AuthorizationOutcome

    data class ResolutionRequired(val pendingIntent: PendingIntent) : AuthorizationOutcome

    data class Failed(val cause: Throwable) : AuthorizationOutcome
}

interface BackupOperations {

    val accountEmail: Flow<String>

    fun accountChooserIntent(): Intent

    suspend fun selectAccount(email: String)

    suspend fun authorize(): AuthorizationOutcome

    suspend fun onAuthorizationResult(data: Intent?): AuthorizationOutcome

    suspend fun hasRequiredScopes(): Boolean

    fun isBackupOnlyEncryptionEnabled(): Boolean

    suspend fun signOut()

    suspend fun createBackup(isManual: Boolean): Result<BackupMeta>

    suspend fun listBackups(): Result<List<BackupMeta>>

    suspend fun restoreBackup(meta: BackupMeta, password: CharArray?): Result<RestoreOutcome>

    suspend fun deleteBackup(meta: BackupMeta): Result<Unit>

    suspend fun exportToStream(out: OutputStream): Result<Unit>

    suspend fun importFromStream(input: InputStream, password: CharArray?): Result<RestoreOutcome>
}
