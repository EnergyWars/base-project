package com.wafflehq.lib.settings.backup

import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.wafflehq.lib.backupcore.BackupVersionTooNewException
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.core.SettingsText
import com.wafflehq.lib.settings.google.toAuthorizationError
import kotlinx.serialization.SerializationException

object BackupErrorMessages {
    fun describe(e: Throwable): SettingsText = when (e) {
        is DriveAuthorizationRequiredException ->
            SettingsText.Label(R.string.appsettings_backup_error_permission_required)
        is UserRecoverableAuthIOException ->
            SettingsText.Label(R.string.appsettings_backup_error_permission_required)
        is ApiException -> e.toAuthorizationError().text
        is BackupVersionTooNewException ->
            SettingsText.Label(R.string.appsettings_backup_error_version_too_new)
        is BackupEncryptionKeyUnavailableException ->
            SettingsText.Label(R.string.appsettings_backup_error_encryption_key_unavailable)
        is BackupCorruptedException -> SettingsText.Label(R.string.appsettings_backup_error_corrupted)
        is SerializationException -> SettingsText.Label(R.string.appsettings_backup_error_corrupted)
        is IllegalArgumentException -> SettingsText.Label(R.string.appsettings_backup_error_corrupted)
        else -> SettingsText.Literal(e.message ?: e.toString())
    }
}
