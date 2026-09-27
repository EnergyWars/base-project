package com.wafflehq.lib.settings.google

import android.accounts.Account
import android.content.Intent

internal class FakeGoogleAuthorizationGateway(
    var outcome: GoogleAuthorization = GoogleAuthorization.Granted("token-123")
) : GoogleAuthorizationGateway {

    val authorizedAccounts = mutableListOf<Account?>()
    val revokedAccounts = mutableListOf<Account?>()
    var failure: Throwable? = null
    var fromIntentOutcome: GoogleAuthorization? = null
    var revokeFailure: Throwable? = null

    override suspend fun authorize(account: Account?): GoogleAuthorization {
        authorizedAccounts += account
        failure?.let { throw it }
        return outcome
    }

    override suspend fun revoke(account: Account?) {
        revokedAccounts += account
        revokeFailure?.let { throw it }
    }

    override fun fromIntent(data: Intent?): GoogleAuthorization {
        failure?.let { throw it }
        return fromIntentOutcome ?: outcome
    }
}
