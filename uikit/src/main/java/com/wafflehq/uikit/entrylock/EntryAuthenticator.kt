package com.wafflehq.uikit.entrylock

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

enum class AuthResult { SUCCESS, FAILED, CANCELLED, NOT_AVAILABLE }

interface EntryAuthenticator {
    fun isAvailable(): Boolean
    suspend fun authenticate(activity: FragmentActivity, title: String): AuthResult
}

class BiometricEntryAuthenticator(private val context: Context) : EntryAuthenticator {

    private val allowedAuthenticators = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    override fun isAvailable(): Boolean =
        BiometricManager.from(context).canAuthenticate(allowedAuthenticators) == BiometricManager.BIOMETRIC_SUCCESS

    override suspend fun authenticate(activity: FragmentActivity, title: String): AuthResult {
        if (!isAvailable()) return AuthResult.NOT_AVAILABLE
        return suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (continuation.isActive) continuation.resume(AuthResult.SUCCESS)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (!continuation.isActive) return
                    continuation.resume(resultForError(errorCode))
                }
            }
            val prompt = BiometricPrompt(activity, executor, callback)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setAllowedAuthenticators(allowedAuthenticators)
                .build()
            prompt.authenticate(promptInfo)
        }
    }

    internal fun resultForError(errorCode: Int): AuthResult = when (errorCode) {
        BiometricPrompt.ERROR_USER_CANCELED,
        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
        BiometricPrompt.ERROR_CANCELED -> AuthResult.CANCELLED
        else -> AuthResult.FAILED
    }
}
