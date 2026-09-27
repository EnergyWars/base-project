package com.wafflehq.lib.settings.backup

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wafflehq.lib.backupcore.BackupRetentionMode
import com.wafflehq.lib.settings.encryption.PasswordReminderInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID

internal val Context.backupDataStore: DataStore<Preferences> by preferencesDataStore("backup_settings")

class BackupPreferenceStore(private val ds: DataStore<Preferences>) {

    constructor(context: Context) : this(context.backupDataStore)

    private val json = Json { ignoreUnknownKeys = true }

    private val KEY_ENABLED              = booleanPreferencesKey("backup_enabled")
    private val KEY_ACCOUNT              = stringPreferencesKey("backup_account")
    private val KEY_DRIVE_AUTHORIZED     = booleanPreferencesKey("drive_authorized")
    private val KEY_DRIVE_ACCOUNT_EMAIL  = stringPreferencesKey("drive_account_email")
    private val KEY_LAST_BACKUP          = stringPreferencesKey("last_backup_time")
    private val KEY_BACKUP_TIME_MINUTES  = intPreferencesKey("backup_time_minutes")
    private val KEY_FAILED_ATTEMPTS      = stringPreferencesKey("failed_backup_attempts")
    private val KEY_FAILURE_ACKNOWLEDGED = booleanPreferencesKey("backup_failure_acknowledged")
    private val KEY_HAS_LOCAL_EXPORT     = booleanPreferencesKey("has_local_export")
    private val KEY_ENCRYPTION_NAG_SUPPRESS_COUNT = intPreferencesKey("encryption_nag_suppress_count")
    private val KEY_PASSWORD_REMINDER_INTERVAL = intPreferencesKey("password_reminder_interval")
    private val KEY_PASSWORD_REMINDER_LAST_SHOWN = longPreferencesKey("password_reminder_last_shown")
    private val KEY_RETENTION_ENABLED    = booleanPreferencesKey("retention_policy_enabled")
    private val KEY_RETENTION_MODE       = intPreferencesKey("retention_mode")
    private val KEY_RETENTION_MAX_COUNT  = intPreferencesKey("retention_max_count")
    private val KEY_RETENTION_MAX_AGE_DAYS = intPreferencesKey("retention_max_age_days")

    val backupEnabled: Flow<Boolean>     = ds.data.map { it[KEY_ENABLED] ?: false }
    val backupAccount: Flow<String>      = ds.data.map { it[KEY_ACCOUNT] ?: "" }

    val driveAuthorized: Flow<Boolean>   = ds.data.map { it[KEY_DRIVE_AUTHORIZED] ?: false }

    val driveAccountEmail: Flow<String>  = ds.data.map { it[KEY_DRIVE_ACCOUNT_EMAIL] ?: "" }
    val lastBackupTime: Flow<String>     = ds.data.map { it[KEY_LAST_BACKUP] ?: "" }
    val backupTimeMinutes: Flow<Int>     = ds.data.map { it[KEY_BACKUP_TIME_MINUTES] ?: DEFAULT_BACKUP_TIME_MINUTES }
    val failedBackupAttempts: Flow<List<FailedBackupAttempt>> = ds.data.map { decodeAttempts(it[KEY_FAILED_ATTEMPTS]) }
    val backupFailureAcknowledged: Flow<Boolean> = ds.data.map { it[KEY_FAILURE_ACKNOWLEDGED] ?: true }
    val hasLocalExport: Flow<Boolean>    = ds.data.map { it[KEY_HAS_LOCAL_EXPORT] ?: false }
    val encryptionNagSuppressCount: Flow<Int> = ds.data.map { it[KEY_ENCRYPTION_NAG_SUPPRESS_COUNT] ?: 0 }
    val passwordReminderInterval: Flow<PasswordReminderInterval> =
        ds.data.map { PasswordReminderInterval.fromOrdinal(it[KEY_PASSWORD_REMINDER_INTERVAL]) }
    val passwordReminderLastShownAt: Flow<Long> = ds.data.map { it[KEY_PASSWORD_REMINDER_LAST_SHOWN] ?: 0L }
    val retentionPolicyEnabled: Flow<Boolean> = ds.data.map { it[KEY_RETENTION_ENABLED] ?: true }
    val retentionMode: Flow<BackupRetentionMode> = ds.data.map { BackupRetentionMode.fromOrdinal(it[KEY_RETENTION_MODE]) }
    val retentionMaxCount: Flow<Int> = ds.data.map { it[KEY_RETENTION_MAX_COUNT] ?: DEFAULT_RETENTION_MAX_COUNT }
    val retentionMaxAgeDays: Flow<Int> = ds.data.map { it[KEY_RETENTION_MAX_AGE_DAYS] ?: DEFAULT_RETENTION_MAX_AGE_DAYS }

    suspend fun setBackupEnabled(enabled: Boolean)  { ds.edit { it[KEY_ENABLED]              = enabled } }
    suspend fun setBackupAccount(account: String)   { ds.edit { it[KEY_ACCOUNT]              = account } }
    suspend fun setDriveAuthorized(authorized: Boolean) { ds.edit { it[KEY_DRIVE_AUTHORIZED] = authorized } }
    suspend fun setDriveAccountEmail(email: String) { ds.edit { it[KEY_DRIVE_ACCOUNT_EMAIL]  = email } }
    suspend fun setLastBackupTime(time: String)     { ds.edit { it[KEY_LAST_BACKUP]         = time } }
    suspend fun setBackupTimeMinutes(minutes: Int)  { ds.edit { it[KEY_BACKUP_TIME_MINUTES]  = minutes } }
    suspend fun setHasLocalExport(value: Boolean)   { ds.edit { it[KEY_HAS_LOCAL_EXPORT]     = value } }
    suspend fun setRetentionPolicyEnabled(enabled: Boolean) { ds.edit { it[KEY_RETENTION_ENABLED] = enabled } }
    suspend fun setRetentionMode(mode: BackupRetentionMode) { ds.edit { it[KEY_RETENTION_MODE] = mode.ordinal } }
    suspend fun setRetentionMaxCount(count: Int)    { ds.edit { it[KEY_RETENTION_MAX_COUNT] = count } }
    suspend fun setRetentionMaxAgeDays(days: Int)   { ds.edit { it[KEY_RETENTION_MAX_AGE_DAYS] = days } }

    suspend fun incrementEncryptionNagSuppressCount() {
        ds.edit { it[KEY_ENCRYPTION_NAG_SUPPRESS_COUNT] = (it[KEY_ENCRYPTION_NAG_SUPPRESS_COUNT] ?: 0) + 1 }
    }

    suspend fun setPasswordReminderInterval(interval: PasswordReminderInterval) {
        ds.edit { it[KEY_PASSWORD_REMINDER_INTERVAL] = interval.ordinal }
    }

    suspend fun setPasswordReminderLastShownAt(epochMillis: Long) {
        ds.edit { it[KEY_PASSWORD_REMINDER_LAST_SHOWN] = epochMillis }
    }

    suspend fun recordBackupFailure(reason: String) {
        val attempt = FailedBackupAttempt(id = UUID.randomUUID().toString(), timestampIso = Instant.now().toString(), reason = reason)
        ds.edit { prefs ->
            val updated = FailedBackupAttempts.withNewFailure(decodeAttempts(prefs[KEY_FAILED_ATTEMPTS]), attempt)
            prefs[KEY_FAILED_ATTEMPTS] = json.encodeToString(updated)
            prefs[KEY_FAILURE_ACKNOWLEDGED] = false
        }
    }

    suspend fun removeFailedBackupAttempt(id: String) {
        ds.edit { prefs ->
            prefs[KEY_FAILED_ATTEMPTS] = json.encodeToString(FailedBackupAttempts.withRemoved(decodeAttempts(prefs[KEY_FAILED_ATTEMPTS]), id))
        }
    }

    suspend fun acknowledgeBackupFailure() { ds.edit { it[KEY_FAILURE_ACKNOWLEDGED] = true } }

    private fun decodeAttempts(raw: String?): List<FailedBackupAttempt> =
        raw?.let { runCatching { json.decodeFromString<List<FailedBackupAttempt>>(it) }.getOrDefault(emptyList()) } ?: emptyList()

    companion object {
        const val DEFAULT_BACKUP_TIME_MINUTES = 180
        const val DEFAULT_RETENTION_MAX_COUNT = 10
        const val DEFAULT_RETENTION_MAX_AGE_DAYS = 30
    }
}
