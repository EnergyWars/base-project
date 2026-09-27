package com.wafflehq.lib.settings.backup

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.wafflehq.lib.backupcore.BackupVersionTooNewException
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.core.SettingsText
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class BackupErrorMessagesTest {

    private fun labelOf(e: Throwable): Int = (BackupErrorMessages.describe(e) as SettingsText.Label).labelRes

    @Test
    fun `a revoked scope asks for the permission again`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val pendingIntent = PendingIntent.getActivity(context, 0, Intent("resolve"), PendingIntent.FLAG_IMMUTABLE)

        assertEquals(
            R.string.appsettings_backup_error_permission_required,
            labelOf(DriveAuthorizationRequiredException(pendingIntent))
        )
    }

    @Test
    fun `describe maps UserRecoverableAuthIOException to permission required string`() {
        val exception = UserRecoverableAuthIOException(mock<UserRecoverableAuthException>())

        assertEquals(R.string.appsettings_backup_error_permission_required, labelOf(exception))
    }

    @Test
    fun `describe maps BackupVersionTooNewException to version too new string`() {
        val exception = BackupVersionTooNewException(backupVersionCode = 5, currentVersionCode = 3)

        assertEquals(R.string.appsettings_backup_error_version_too_new, labelOf(exception))
    }

    @Test
    fun `describe maps a missing encryption key to its own string`() {
        assertEquals(
            R.string.appsettings_backup_error_encryption_key_unavailable,
            labelOf(BackupEncryptionKeyUnavailableException())
        )
    }

    @Test
    fun `every flavour of unreadable backup data maps to the corrupted string`() {
        assertEquals(R.string.appsettings_backup_error_corrupted, labelOf(BackupCorruptedException()))
        assertEquals(R.string.appsettings_backup_error_corrupted, labelOf(SerializationException("bad json")))
        assertEquals(R.string.appsettings_backup_error_corrupted, labelOf(IllegalArgumentException("bad input")))
    }

    @Test
    fun `a Play Services failure keeps its status code alongside the mapped reason`() {
        val exception = ApiException(Status(CommonStatusCodes.NETWORK_ERROR))

        val text = BackupErrorMessages.describe(exception)

        assertEquals(
            SettingsText.Formatted(
                R.string.appsettings_google_signin_error_with_code,
                listOf(
                    SettingsText.Label(R.string.appsettings_google_signin_error_network),
                    CommonStatusCodes.NETWORK_ERROR
                )
            ),
            text
        )
    }

    @Test
    fun `describe falls back to the exception message for unknown errors`() {
        assertEquals(SettingsText.Literal("boom"), BackupErrorMessages.describe(IllegalStateException("boom")))
    }

    @Test
    fun `describe falls back to toString when the message is null`() {
        val exception = IllegalStateException()

        assertEquals(SettingsText.Literal(exception.toString()), BackupErrorMessages.describe(exception))
    }
}
