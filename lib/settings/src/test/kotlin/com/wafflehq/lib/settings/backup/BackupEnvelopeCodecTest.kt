package com.wafflehq.lib.settings.backup

import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.database.crypto.AesGcmCipher
import com.wafflehq.lib.database.crypto.PasswordKeyWrapper
import com.wafflehq.lib.database.state.EncryptionStateStore
import com.wafflehq.lib.settings.encryption.FakeKeystoreKeyWrapper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Base64

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class BackupEnvelopeCodecTest {

    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var stateStore: EncryptionStateStore
    private lateinit var keyWrapper: FakeKeystoreKeyWrapper
    private lateinit var backupOnlyKeyWrapper: FakeKeystoreKeyWrapper
    private lateinit var payloadProvider: RecordingBackupPayloadProvider
    private lateinit var codec: BackupEnvelopeCodec

    @Before
    fun setUp() {
        prefsCounter++
        stateStore = EncryptionStateStore(
            ApplicationProvider.getApplicationContext(),
            prefsName = "encryption_state_backup_codec_$prefsCounter"
        )
        keyWrapper = FakeKeystoreKeyWrapper()
        backupOnlyKeyWrapper = FakeKeystoreKeyWrapper()
        payloadProvider = RecordingBackupPayloadProvider()
        codec = buildCodec()
    }

    private fun buildCodec() = BackupEnvelopeCodec(
        json = json,
        payloadProvider = payloadProvider,
        encryptionStateStore = stateStore,
        keyWrapper = keyWrapper,
        backupOnlyKeyWrapper = backupOnlyKeyWrapper,
        passwordWrapper = PasswordKeyWrapper()
    )

    private fun encryptDatabaseWith(dek: ByteArray) {
        stateStore.commitSwapIntent(newIsEncrypted = true, newWrappedDek = keyWrapper.wrap(dek))
    }

    @Test
    fun `builds a plaintext format-3 envelope when nothing is encrypted`() {
        val envelope = codec.buildEnvelope(payloadProvider.payloadText())

        assertEquals(BACKUP_ENVELOPE_CURRENT_FORMAT, envelope.format)
        assertFalse(envelope.encrypted)
        assertEquals(payloadProvider.payloadText(), envelope.payload)
        assertNull(envelope.payloadIv)
    }

    @Test
    fun `an encrypted format-3 backup round trips byte for byte`() {
        encryptDatabaseWith(ByteArray(32) { it.toByte() })

        val envelope = codec.buildEnvelope(payloadProvider.payloadText())
        assertTrue(envelope.encrypted)
        assertEquals(BACKUP_ENVELOPE_CURRENT_FORMAT, envelope.format)
        assertNotNull(envelope.payloadIv)

        val decoded = codec.decode(json.encodeToString(envelope).toByteArray(), password = null)

        assertArrayEquals(
            payloadProvider.payloadText().toByteArray(),
            decoded.payloadJson.toByteArray()
        )
        assertNull(decoded.recoveredDek)
    }

    @Test
    fun `reads a plaintext envelope without touching the payload`() {
        val envelope = BackupEnvelope(format = 2, encrypted = false, payload = payloadProvider.payloadText())

        val decoded = codec.decode(json.encodeToString(envelope).toByteArray(), password = null)

        assertEquals(payloadProvider.payloadText(), decoded.payloadJson)
    }

    @Test
    fun `reads a legacy format-1 document that has no envelope at all`() {
        val decoded = codec.decode(payloadProvider.payloadText().toByteArray(), password = null)

        assertEquals(payloadProvider.payloadText(), decoded.payloadJson)
    }

    @Test
    fun `reads an encrypted format-2 envelope written without AAD`() {
        val dek = ByteArray(32) { (it + 7).toByte() }
        encryptDatabaseWith(dek)
        val encrypted = AesGcmCipher.encrypt(dek, payloadProvider.payloadText().toByteArray())
        val envelope = BackupEnvelope(
            format = 2,
            encrypted = true,
            payloadIv = Base64.getEncoder().encodeToString(encrypted.iv),
            payload = Base64.getEncoder().encodeToString(encrypted.ciphertext)
        )

        val decoded = codec.decode(json.encodeToString(envelope).toByteArray(), password = null)

        assertEquals(payloadProvider.payloadText(), decoded.payloadJson)
    }

    @Test
    fun `garbage bytes are reported as a corrupted backup`() {
        assertThrows(BackupCorruptedException::class.java) {
            codec.decode("{ not a backup at all".toByteArray(), password = null)
        }
    }

    @Test
    fun `a backup-only encrypted backup is recoverable with its password on a fresh device`() {
        val dek = ByteArray(32) { (it + 3).toByte() }
        val passwordWrapper = PasswordKeyWrapper()
        stateStore.writeBackupOnlyDek(
            backupOnlyKeyWrapper.wrap(dek),
            passwordWrapper.wrap(dek, "correct horse".toCharArray())
        )

        val envelope = codec.buildEnvelope(payloadProvider.payloadText())
        assertTrue(envelope.encrypted)
        assertTrue(envelope.backupOnlyEncryption)
        assertNotNull(envelope.keyEnvelope)
        val bytes = json.encodeToString(envelope).toByteArray()

        stateStore.clearBackupOnlyDek()
        val decoded = codec.decode(bytes, "correct horse".toCharArray())

        assertEquals(payloadProvider.payloadText(), decoded.payloadJson)

        assertNull(decoded.recoveredDek)
        assertNotNull(stateStore.readBackupOnlyWrappedDek())
    }

    @Test
    fun `a wrong password is rejected`() {
        val dek = ByteArray(32) { (it + 3).toByte() }
        stateStore.writeBackupOnlyDek(
            backupOnlyKeyWrapper.wrap(dek),
            PasswordKeyWrapper().wrap(dek, "correct horse".toCharArray())
        )
        val bytes = json.encodeToString(codec.buildEnvelope(payloadProvider.payloadText())).toByteArray()
        stateStore.clearBackupOnlyDek()

        assertThrows(BackupWrongPasswordException::class.java) {
            codec.decode(bytes, "wrong horse".toCharArray())
        }
    }

    @Test
    fun `an encrypted backup without any local key demands a password`() {
        val dek = ByteArray(32) { (it + 3).toByte() }
        stateStore.writeBackupOnlyDek(
            backupOnlyKeyWrapper.wrap(dek),
            PasswordKeyWrapper().wrap(dek, "correct horse".toCharArray())
        )
        val bytes = json.encodeToString(codec.buildEnvelope(payloadProvider.payloadText())).toByteArray()
        stateStore.clearBackupOnlyDek()

        assertThrows(BackupPasswordRequiredException::class.java) {
            codec.decode(bytes, password = null)
        }
    }

    @Test
    fun `tampering with the format field breaks the AAD bound ciphertext`() {
        val dek = ByteArray(32) { (it + 3).toByte() }
        stateStore.writeBackupOnlyDek(
            backupOnlyKeyWrapper.wrap(dek),
            PasswordKeyWrapper().wrap(dek, "correct horse".toCharArray())
        )
        val original = json.encodeToString(codec.buildEnvelope(payloadProvider.payloadText()))
        stateStore.clearBackupOnlyDek()
        val tampered = original.replace("\"format\":3", "\"format\":2")
        assertFalse(tampered == original)

        assertThrows(BackupWrongPasswordException::class.java) {
            codec.decode(tampered.toByteArray(), "correct horse".toCharArray())
        }
    }

    @Test
    fun `an encrypted database without a local key refuses to build a backup`() {
        stateStore.commitSwapIntent(newIsEncrypted = true, newWrappedDek = null)

        assertThrows(BackupEncryptionKeyUnavailableException::class.java) {
            codec.buildEnvelope(payloadProvider.payloadText())
        }
    }

    @Test
    fun `a password recovered database DEK is surfaced for re-enabling local encryption`() {
        val dek = ByteArray(32) { (it + 11).toByte() }
        val passwordWrapper = PasswordKeyWrapper()
        val passwordWrapped = passwordWrapper.wrap(dek, "correct horse".toCharArray())
        stateStore.commitSwapIntent(newIsEncrypted = true, newWrappedDek = keyWrapper.wrap(dek))
        stateStore.writePasswordWrappedDek(passwordWrapped)

        val envelope = codec.buildEnvelope(payloadProvider.payloadText())
        assertFalse(envelope.backupOnlyEncryption)
        val bytes = json.encodeToString(envelope).toByteArray()

        val freshStore = EncryptionStateStore(
            ApplicationProvider.getApplicationContext(),
            prefsName = "encryption_state_backup_codec_fresh_$prefsCounter"
        )
        val freshCodec = BackupEnvelopeCodec(
            json = json,
            payloadProvider = payloadProvider,
            encryptionStateStore = freshStore,
            keyWrapper = FakeKeystoreKeyWrapper(),
            backupOnlyKeyWrapper = FakeKeystoreKeyWrapper(),
            passwordWrapper = passwordWrapper
        )

        val decoded = freshCodec.decode(bytes, "correct horse".toCharArray())

        assertEquals(payloadProvider.payloadText(), decoded.payloadJson)
        assertArrayEquals(dek, decoded.recoveredDek)
    }

    private companion object {
        var prefsCounter = 0
    }
}
