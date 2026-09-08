package com.wafflehq.uikit.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.uikit.database.crypto.PasswordKeyWrapper
import com.wafflehq.uikit.database.crypto.WrappedDek
import com.wafflehq.uikit.database.state.EncryptionStateStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.SecureRandom

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupPasswordVerifierTest {

    private lateinit var stateStore: EncryptionStateStore
    private val passwordWrapper = PasswordKeyWrapper()
    private lateinit var verifier: BackupPasswordVerifier

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
        stateStore = EncryptionStateStore(context, PREFS_NAME)
        verifier = BackupPasswordVerifier(stateStore, passwordWrapper)
    }

    private fun randomDek() = ByteArray(32).also { SecureRandom().nextBytes(it) }

    @Test
    fun `returns false when no password wrap is stored`() {
        assertFalse(verifier.verify("anything".toCharArray()))
    }

    @Test
    fun `returns true for the correct password`() {
        val wrapped = passwordWrapper.wrap(randomDek(), "correct horse battery staple".toCharArray())
        stateStore.commitSwapIntent(
            newIsEncrypted = true,
            newWrappedDek = WrappedDek(iv = byteArrayOf(1), ciphertext = byteArrayOf(2)),
            newPasswordWrappedDek = wrapped,
        )

        assertTrue(verifier.verify("correct horse battery staple".toCharArray()))
    }

    @Test
    fun `returns false for the wrong password`() {
        val wrapped = passwordWrapper.wrap(randomDek(), "correct horse battery staple".toCharArray())
        stateStore.writePasswordWrappedDek(wrapped)

        assertFalse(verifier.verify("wrong password".toCharArray()))
    }

    @Test
    fun `returns false after the password wrap has been removed`() {
        val wrapped = passwordWrapper.wrap(randomDek(), "password".toCharArray())
        stateStore.writePasswordWrappedDek(wrapped)
        stateStore.writePasswordWrappedDek(null)

        assertFalse(verifier.verify("password".toCharArray()))
    }

    private companion object {
        const val PREFS_NAME = "test_encryption_state"
    }
}
