package com.wafflehq.lib.settings.google

import android.accounts.Account
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.tasks.await

sealed interface GoogleAuthorization {
    data class Granted(val accessToken: String) : GoogleAuthorization
    data class ResolutionRequired(val pendingIntent: PendingIntent) : GoogleAuthorization
}

interface GoogleAuthorizationGateway {
    suspend fun authorize(account: Account?): GoogleAuthorization
    suspend fun revoke(account: Account?)
    fun fromIntent(data: Intent?): GoogleAuthorization
}

class PlayGoogleAuthorizationGateway(
    context: Context,
    private val scopes: List<Scope>
) : GoogleAuthorizationGateway {

    private val client by lazy { Identity.getAuthorizationClient(context) }

    override suspend fun authorize(account: Account?): GoogleAuthorization =
        client.authorize(request(account)).await().toGoogleAuthorization()

    override suspend fun revoke(account: Account?) {
        client.revokeAccess(
            RevokeAccessRequest.builder()
                .setScopes(scopes)
                .apply { if (account != null) setAccount(account) }
                .build()
        ).await()
    }

    override fun fromIntent(data: Intent?): GoogleAuthorization =
        client.getAuthorizationResultFromIntent(data ?: Intent()).toGoogleAuthorization()

    private fun request(account: Account?): AuthorizationRequest =
        AuthorizationRequest.builder()
            .setRequestedScopes(scopes)
            .apply { if (account != null) setAccount(account) }
            .build()

    private fun AuthorizationResult.toGoogleAuthorization(): GoogleAuthorization =
        if (hasResolution()) {
            GoogleAuthorization.ResolutionRequired(requireNotNull(pendingIntent))
        } else {
            GoogleAuthorization.Granted(accessToken.orEmpty())
        }
}
