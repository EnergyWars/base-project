package com.wafflehq.lib.settings.google

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent

object GoogleAccounts {

    const val ACCOUNT_TYPE = "com.google"

    fun chooserIntent(): Intent =
        AccountManager.newChooseAccountIntent(null, null, arrayOf(ACCOUNT_TYPE), null, null, null, null)

    fun accountFor(email: String): Account = Account(email, ACCOUNT_TYPE)

    @Suppress("DEPRECATION")
    fun legacySignedInEmail(context: Context): String? = runCatching {
        com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)?.email
    }.getOrNull()?.takeIf { it.isNotEmpty() }
}
