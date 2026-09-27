package com.wafflehq.lib.settings.backup

import android.content.Context
import android.content.Intent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File as DriveFile
import com.wafflehq.lib.backupcore.BackupRetentionCandidate
import com.wafflehq.lib.backupcore.BackupRetentionPolicy
import com.wafflehq.lib.database.crypto.KeystoreKeyWrapper
import com.wafflehq.lib.database.crypto.PasswordKeyWrapper
import com.wafflehq.lib.database.state.EncryptionStateStore
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.backup.access.BackupPayloadProvider
import com.wafflehq.lib.settings.google.GoogleAuthorization
import com.wafflehq.lib.uicore.io.readBytesLimited
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.time.Instant
import java.time.format.DateTimeFormatter

class DriveBackupRepository(
    private val context: Context,
    private val config: DriveBackupConfig,
    private val authorization: DriveAuthorizationProvider,
    private val payloadProvider: BackupPayloadProvider,
    private val encryptionStateStore: EncryptionStateStore,
    private val keyWrapper: KeystoreKeyWrapper,
    private val backupOnlyKeyWrapper: KeystoreKeyWrapper,
    private val passwordWrapper: PasswordKeyWrapper,
    private val backupPreferences: BackupPreferenceStore
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val codec = BackupEnvelopeCodec(
        json = json,
        payloadProvider = payloadProvider,
        encryptionStateStore = encryptionStateStore,
        keyWrapper = keyWrapper,
        backupOnlyKeyWrapper = backupOnlyKeyWrapper,
        passwordWrapper = passwordWrapper
    )

    val accountEmail: Flow<String> = authorization.accountEmail

    fun accountChooserIntent(): Intent = authorization.accountChooserIntent()

    suspend fun selectAccount(email: String) {
        authorization.selectAccount(email)
    }

    suspend fun authorize(): GoogleAuthorization = authorization.authorize()

    fun resultFromIntent(data: Intent?): Result<GoogleAuthorization> = authorization.resultFromIntent(data)

    suspend fun onAuthorized() {
        authorization.onAuthorized()
    }

    suspend fun isSignedIn(): Boolean = authorization.isSignedIn()

    suspend fun hasRequiredScopes(): Boolean = authorization.hasRequiredScopes()

    suspend fun signOut(): Unit = withContext(Dispatchers.IO) {
        authorization.signOut()
        runCatching { config.onSignedOut() }
    }

    suspend fun createBackup(isManual: Boolean): Result<BackupMeta> = withContext(Dispatchers.IO) {
        runCatching {
            val drive = buildDriveService()
            val meta = uploadBackup(drive, origin = if (isManual) ORIGIN_MANUAL else ORIGIN_AUTO)
            pruneOldBackups(drive)
            meta
        }
    }

    private suspend fun uploadBackup(drive: Drive, origin: String): BackupMeta {
        val envelope = codec.buildEnvelope(payloadProvider.collect())
        val encoded = json.encodeToString(envelope)
        val now = Instant.now()

        val fileNamePrefix = if (envelope.encrypted) config.encryptedFileNamePrefix else config.fileNamePrefix
        val fileName = "$fileNamePrefix${DateTimeFormatter.ISO_INSTANT.format(now)}.json"
        val encodedBytes = encoded.toByteArray()

        val fileMeta = DriveFile().apply {
            name     = fileName
            parents  = listOf("appDataFolder")
            mimeType = "application/json"
            properties = mapOf(PROP_ORIGIN to origin)
        }
        val content = com.google.api.client.http.InputStreamContent(
            "application/json",
            ByteArrayInputStream(encodedBytes)
        )
        val created = drive.files().create(fileMeta, content)
            .setFields("id, name, createdTime")
            .execute()

        return BackupMeta(
            id = created.id,
            name = created.name,
            createdAt = created.createdTime?.value?.let { Instant.ofEpochMilli(it) } ?: now,
            encrypted = envelope.encrypted,
            isManual = origin == ORIGIN_MANUAL,
            isPreRestoreSnapshot = origin == ORIGIN_PRE_RESTORE,
            sizeBytes = encodedBytes.size.toLong()
        )
    }

    suspend fun listBackups(): Result<List<BackupMeta>> = withContext(Dispatchers.IO) {
        runCatching {
            val drive = buildDriveService()
            listAllBackupFiles(drive, "id, name, createdTime, properties, size", ordered = true)
                .map {
                    BackupMeta(
                        id = it.id,
                        name = it.name,
                        createdAt = it.createdTime?.value?.let { ms -> Instant.ofEpochMilli(ms) } ?: Instant.EPOCH,
                        encrypted = it.name?.contains(config.encryptedFileNamePrefix) == true,
                        isManual = it.properties?.get(PROP_ORIGIN) == ORIGIN_MANUAL,
                        isPreRestoreSnapshot = it.properties?.get(PROP_ORIGIN) == ORIGIN_PRE_RESTORE,
                        sizeBytes = it.getSize()
                    )
                }
        }
    }

    suspend fun restoreBackup(meta: BackupMeta, password: CharArray? = null): Result<RestoreOutcome> = withContext(Dispatchers.IO) {
        runCatching {
            val drive = buildDriveService()
            val bytes = drive.files().get(meta.id).executeMediaAsInputStream()
                .use { it.readBytesLimited(MAX_BACKUP_FILE_BYTES) { BackupCorruptedException() } }
            val decoded = codec.decode(bytes, password)
            uploadBackup(drive, origin = ORIGIN_PRE_RESTORE)
            payloadProvider.apply(decoded.payloadJson)
            RestoreOutcome(recoveredDek = decoded.recoveredDek)
        }
    }

    suspend fun exportToStream(out: java.io.OutputStream): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val envelope = codec.buildEnvelope(payloadProvider.collect())
            out.write(json.encodeToString(envelope).toByteArray())
        }
    }

    suspend fun importFromStream(input: java.io.InputStream, password: CharArray? = null): Result<RestoreOutcome> = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = input.readBytesLimited(MAX_BACKUP_FILE_BYTES) { BackupCorruptedException() }
            val decoded = codec.decode(bytes, password)
            buildDriveServiceOrNull()?.let { drive -> uploadBackup(drive, origin = ORIGIN_PRE_RESTORE) }
            payloadProvider.apply(decoded.payloadJson)
            RestoreOutcome(recoveredDek = decoded.recoveredDek)
        }
    }

    suspend fun deleteBackup(meta: BackupMeta): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val drive = buildDriveService()
            drive.files().delete(meta.id).execute()
            Unit
        }
    }

    suspend fun replaceAllBackupsWithFreshOne(): Result<BackupMeta> = withContext(Dispatchers.IO) {
        runCatching {
            val drive = buildDriveService()
            val fresh = uploadBackup(drive, origin = ORIGIN_MANUAL)
            val staleIds = listAllBackupFiles(drive, "id", ordered = false)
                .mapNotNull { it.id }
                .filterNot { it == fresh.id }
            staleIds.forEach { id -> drive.files().delete(id).execute() }
            fresh
        }
    }

    fun enableBackupOnlyEncryption(password: CharArray): Result<Unit> = runCatching {
        val dek = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
        val wrappedDek = backupOnlyKeyWrapper.wrap(dek)
        val passwordWrappedDek = passwordWrapper.wrap(dek, password)
        encryptionStateStore.writeBackupOnlyDek(wrappedDek, passwordWrappedDek)
    }

    fun disableBackupOnlyEncryption() {
        encryptionStateStore.clearBackupOnlyDek()
        backupOnlyKeyWrapper.deleteKey()
    }

    fun isBackupOnlyEncryptionEnabled(): Boolean = encryptionStateStore.readBackupOnlyWrappedDek() != null

    private suspend fun buildDriveService(): Drive {
        if (!authorization.isSignedIn()) error(context.getString(R.string.appsettings_backup_error_not_signed_in))
        return authorization.get()
    }

    private suspend fun buildDriveServiceOrNull(): Drive? = runCatching { buildDriveService() }.getOrNull()

    private fun listAllBackupFiles(drive: Drive, fields: String, ordered: Boolean): List<DriveFile> {
        val result = mutableListOf<DriveFile>()
        var pageToken: String? = null
        var pages = 0
        do {
            val request = drive.files().list()
                .setSpaces("appDataFolder")
                .setFields("nextPageToken, files($fields)")
                .setQ("name contains '${config.fileNamePrefix}'")
                .setPageSize(LIST_PAGE_SIZE)
                .setPageToken(pageToken)
            if (ordered) request.setOrderBy("createdTime desc")
            val page = request.execute()
            result += page.files.orEmpty()
            pageToken = page.nextPageToken
            pages++
        } while (pageToken != null && pages < MAX_LIST_PAGES)
        return result
    }

    private suspend fun pruneOldBackups(drive: Drive) {
        val files = listAllBackupFiles(drive, "id, createdTime, properties", ordered = true)
        val candidates = files.mapNotNull { file ->
            val id = file.id ?: return@mapNotNull null
            BackupRetentionCandidate(
                id = id,
                createdAt = file.createdTime?.value?.let { Instant.ofEpochMilli(it) } ?: Instant.EPOCH,
                isManual = file.properties?.get(PROP_ORIGIN) == ORIGIN_MANUAL
            )
        }
        val idsToDelete = BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = backupPreferences.retentionPolicyEnabled.first(),
            mode = backupPreferences.retentionMode.first(),
            maxCount = backupPreferences.retentionMaxCount.first(),
            maxAgeDays = backupPreferences.retentionMaxAgeDays.first()
        )
        idsToDelete.forEach { drive.files().delete(it).execute() }
    }

    companion object {
        private const val PROP_ORIGIN = "origin"
        private const val ORIGIN_MANUAL = "manual"
        private const val ORIGIN_AUTO = "auto"
        private const val ORIGIN_PRE_RESTORE = "pre_restore"
        private const val LIST_PAGE_SIZE = 1000
        private const val MAX_LIST_PAGES = 20
    }
}

data class BackupMeta(
    val id: String,
    val name: String,
    val createdAt: Instant,
    val encrypted: Boolean,
    val isManual: Boolean,
    val sizeBytes: Long? = null,

    val isFailed: Boolean = false,
    val failureReason: String? = null,

    val isPreRestoreSnapshot: Boolean = false
)

data class RestoreOutcome(

    val recoveredDek: ByteArray? = null
)

class BackupPasswordRequiredException : Exception()

class BackupWrongPasswordException : Exception()

class BackupEncryptionKeyUnavailableException : Exception()
