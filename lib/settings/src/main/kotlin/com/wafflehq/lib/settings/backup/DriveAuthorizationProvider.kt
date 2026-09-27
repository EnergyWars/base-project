package com.wafflehq.lib.settings.backup

import android.accounts.Account
import android.accounts.AccountManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.wafflehq.lib.settings.google.GoogleAccounts
import com.wafflehq.lib.settings.google.GoogleAuthorization
import com.wafflehq.lib.settings.google.GoogleAuthorizationGateway
import com.wafflehq.lib.settings.google.PlayGoogleAuthorizationGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class DriveAuthorizationRequiredException(val pendingIntent: PendingIntent) : Exception()

class DriveAuthorizationProvider internal constructor(
    private val backupPrefs: BackupPreferenceStore,
    private val driveApplicationName: String,
    private val gateway: GoogleAuthorizationGateway,
    private val legacyAccountEmail: () -> String?
) {
    constructor(
        context: Context,
        backupPrefs: BackupPreferenceStore,
        driveApplicationName: String
    ) : this(
        backupPrefs = backupPrefs,
        driveApplicationName = driveApplicationName,
        gateway = PlayGoogleAuthorizationGateway(context, listOf(Scope(DriveScopes.DRIVE_APPDATA))),
        legacyAccountEmail = { GoogleAccounts.legacySignedInEmail(context) }
    )

    val isAuthorized: Flow<Boolean> = backupPrefs.driveAuthorized
    val accountEmail: Flow<String> = backupPrefs.driveAccountEmail

    fun accountChooserIntent(): Intent = GoogleAccounts.chooserIntent()

    suspend fun selectAccount(email: String) {
        backupPrefs.setDriveAccountEmail(email)
    }

    suspend fun isSignedIn(): Boolean = currentAccountEmail().isNotEmpty()

    suspend fun authorize(): GoogleAuthorization = withContext(Dispatchers.IO) {
        gateway.authorize(currentAccount())
    }

    fun resultFromIntent(data: Intent?): Result<GoogleAuthorization> = runCatching { gateway.fromIntent(data) }

    suspend fun onAuthorized() {
        backupPrefs.setDriveAuthorized(true)
    }

    suspend fun hasRequiredScopes(): Boolean =
        runCatching { authorize() is GoogleAuthorization.Granted }.getOrDefault(false)

    suspend fun signOut(): Unit = withContext(Dispatchers.IO) {
        runCatching { gateway.revoke(currentAccount()) }
        backupPrefs.setDriveAuthorized(false)
        backupPrefs.setDriveAccountEmail("")
    }

    suspend fun get(): Drive = withContext(Dispatchers.IO) {
        when (val authorization = authorize()) {
            is GoogleAuthorization.ResolutionRequired ->
                throw DriveAuthorizationRequiredException(authorization.pendingIntent)
            is GoogleAuthorization.Granted -> {
                val requestInitializer = HttpRequestInitializer { request ->
                    request.headers.authorization = "Bearer ${authorization.accessToken}"
                }
                Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), requestInitializer)
                    .setApplicationName(driveApplicationName)
                    .build()
            }
        }
    }

    private suspend fun currentAccount(): Account? =
        currentAccountEmail().takeIf { it.isNotEmpty() }?.let { GoogleAccounts.accountFor(it) }

    private suspend fun currentAccountEmail(): String {
        val stored = backupPrefs.driveAccountEmail.first()
        if (stored.isNotEmpty()) return stored
        val legacy = legacyAccountEmail()?.takeIf { it.isNotEmpty() } ?: return ""
        backupPrefs.setDriveAccountEmail(legacy)
        return legacy
    }
}
