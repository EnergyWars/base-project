package com.wafflehq.lib.settings.google

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.core.SettingsText

data class AuthorizationError(
    val text: SettingsText,
    val isUserCancelled: Boolean
)

fun Throwable.toAuthorizationError(): AuthorizationError {
    if (this is ApiException) {
        val withDetails = if (statusCode == CommonStatusCodes.DEVELOPER_ERROR) {
            SettingsText.Formatted(R.string.appsettings_google_signin_error_developer_detailed, listOf(statusCode))
        } else {
            SettingsText.Formatted(
                R.string.appsettings_google_signin_error_with_code,
                listOf(SettingsText.Label(authorizationMessageFor(statusCode)), statusCode)
            )
        }
        return AuthorizationError(text = withDetails, isUserCancelled = isUserCancelledAuthorization)
    }
    return AuthorizationError(
        text = message?.let { SettingsText.Literal(it) }
            ?: SettingsText.Label(R.string.appsettings_google_signin_error_unknown),
        isUserCancelled = false
    )
}

val Throwable.isUserCancelledAuthorization: Boolean
    get() = this is ApiException && statusCode == CommonStatusCodes.CANCELED

private fun authorizationMessageFor(statusCode: Int): Int = when (statusCode) {
    CommonStatusCodes.CANCELED -> R.string.appsettings_google_signin_error_cancelled
    CommonStatusCodes.NETWORK_ERROR -> R.string.appsettings_google_signin_error_network
    CommonStatusCodes.SIGN_IN_REQUIRED -> R.string.appsettings_google_signin_error_required
    CommonStatusCodes.INTERNAL_ERROR -> R.string.appsettings_google_signin_error_internal
    CommonStatusCodes.DEVELOPER_ERROR -> R.string.appsettings_google_signin_error_developer
    CommonStatusCodes.INVALID_ACCOUNT -> R.string.appsettings_google_signin_error_invalid_account
    CommonStatusCodes.RESOLUTION_REQUIRED -> R.string.appsettings_google_signin_error_resolution_required
    CommonStatusCodes.TIMEOUT -> R.string.appsettings_google_signin_error_timeout
    CommonStatusCodes.API_NOT_CONNECTED -> R.string.appsettings_google_signin_error_api_not_connected
    else -> R.string.appsettings_google_signin_error_unknown
}
