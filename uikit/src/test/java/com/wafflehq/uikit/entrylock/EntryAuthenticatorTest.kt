package com.wafflehq.uikit.entrylock

import android.content.Context
import androidx.biometric.BiometricPrompt
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock

class EntryAuthenticatorTest {

    private val authenticator = BiometricEntryAuthenticator(mock<Context>())

    @Test
    fun `user cancellation maps to CANCELLED`() {
        assertEquals(AuthResult.CANCELLED, authenticator.resultForError(BiometricPrompt.ERROR_USER_CANCELED))
    }

    @Test
    fun `negative button maps to CANCELLED`() {
        assertEquals(AuthResult.CANCELLED, authenticator.resultForError(BiometricPrompt.ERROR_NEGATIVE_BUTTON))
    }

    @Test
    fun `generic cancel maps to CANCELLED`() {
        assertEquals(AuthResult.CANCELLED, authenticator.resultForError(BiometricPrompt.ERROR_CANCELED))
    }

    @Test
    fun `lockout maps to FAILED`() {
        assertEquals(AuthResult.FAILED, authenticator.resultForError(BiometricPrompt.ERROR_LOCKOUT))
    }

    @Test
    fun `no biometrics enrolled maps to FAILED`() {
        assertEquals(AuthResult.FAILED, authenticator.resultForError(BiometricPrompt.ERROR_NO_BIOMETRICS))
    }
}
