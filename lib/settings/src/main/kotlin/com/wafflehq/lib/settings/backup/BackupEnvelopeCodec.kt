package com.wafflehq.lib.settings.backup

import com.wafflehq.lib.database.crypto.AesGcmCipher
import com.wafflehq.lib.database.crypto.KeystoreKeyWrapper
import com.wafflehq.lib.database.crypto.PasswordKeyWrapper
import com.wafflehq.lib.database.state.EncryptionStateStore
import com.wafflehq.lib.settings.backup.access.BackupPayloadProvider
import kotlinx.serialization.json.Json
import java.util.Base64

internal class DecodedBackup(val payloadJson: String, val recoveredDek: ByteArray? = null)

internal class BackupEnvelopeCodec(
    private val json: Json,
    private val payloadProvider: BackupPayloadProvider,
    private val encryptionStateStore: EncryptionStateStore,
    private val keyWrapper: KeystoreKeyWrapper,
    private val backupOnlyKeyWrapper: KeystoreKeyWrapper,
    private val passwordWrapper: PasswordKeyWrapper
) {

    private class EncryptionDekSource(val dek: ByteArray, val keyEnvelope: KeyEnvelopeDto?, val backupOnly: Boolean)

    private fun resolveEncryptionDekSource(): EncryptionDekSource? {
        if (encryptionStateStore.isEncrypted) {
            val wrappedDek = encryptionStateStore.readWrappedDek() ?: throw BackupEncryptionKeyUnavailableException()
            return EncryptionDekSource(
                dek = keyWrapper.unwrap(wrappedDek),
                keyEnvelope = encryptionStateStore.readPasswordWrappedDek()?.toDto(),
                backupOnly = false
            )
        }
        val backupOnlyWrappedDek = encryptionStateStore.readBackupOnlyWrappedDek() ?: return null
        return EncryptionDekSource(
            dek = backupOnlyKeyWrapper.unwrap(backupOnlyWrappedDek),
            keyEnvelope = encryptionStateStore.readBackupOnlyPasswordWrappedDek()?.toDto(),
            backupOnly = true
        )
    }

    fun buildEnvelope(plaintextJson: String): BackupEnvelope {
        val dekSource = resolveEncryptionDekSource()
            ?: return BackupEnvelope(format = BACKUP_ENVELOPE_CURRENT_FORMAT, encrypted = false, payload = plaintextJson)
        val aad = encryptionAad(BACKUP_ENVELOPE_CURRENT_FORMAT, encrypted = true)
        val encrypted = AesGcmCipher.encrypt(dekSource.dek, plaintextJson.toByteArray(), aad)
        return BackupEnvelope(
            format = BACKUP_ENVELOPE_CURRENT_FORMAT,
            encrypted = true,
            keyEnvelope = dekSource.keyEnvelope,
            payloadIv = Base64.getEncoder().encodeToString(encrypted.iv),
            payload = Base64.getEncoder().encodeToString(encrypted.ciphertext),
            backupOnlyEncryption = dekSource.backupOnly
        )
    }

    private class DecryptedPayload(val plaintext: ByteArray, val recoveredDek: ByteArray?)

    private fun decryptPayload(envelope: BackupEnvelope, iv: ByteArray, ciphertext: ByteArray, password: CharArray?): DecryptedPayload {
        val aad = if (envelope.format >= BACKUP_ENVELOPE_AAD_FORMAT) encryptionAad(envelope.format, encrypted = true) else ByteArray(0)

        if (encryptionStateStore.isEncrypted) {
            val localDek = encryptionStateStore.readWrappedDek()?.let {
                runCatching { keyWrapper.unwrap(it) }.getOrNull()
            }
            if (localDek != null) {
                runCatching { AesGcmCipher.decrypt(localDek, iv, ciphertext, aad) }.getOrNull()?.let {
                    return DecryptedPayload(it, recoveredDek = null)
                }
            }
        }
        val localBackupOnlyDek = encryptionStateStore.readBackupOnlyWrappedDek()?.let {
            runCatching { backupOnlyKeyWrapper.unwrap(it) }.getOrNull()
        }
        if (localBackupOnlyDek != null) {
            runCatching { AesGcmCipher.decrypt(localBackupOnlyDek, iv, ciphertext, aad) }.getOrNull()?.let {
                return DecryptedPayload(it, recoveredDek = null)
            }
        }
        val keyEnvelope = envelope.keyEnvelope ?: error("Encrypted backup has no recoverable key envelope")
        if (password == null) throw BackupPasswordRequiredException()
        if (keyEnvelope.iterations !in PasswordKeyWrapper.MIN_ITERATIONS..PasswordKeyWrapper.MAX_ITERATIONS) {
            throw BackupCorruptedException()
        }
        val passwordDek = try {
            passwordWrapper.unwrap(keyEnvelope.toPasswordWrappedDek(), password)
        } catch (e: Exception) {
            throw BackupWrongPasswordException()
        }
        val plaintext = try {
            AesGcmCipher.decrypt(passwordDek, iv, ciphertext, aad)
        } catch (e: Exception) {
            throw BackupWrongPasswordException()
        }

        if (envelope.backupOnlyEncryption) {
            runCatching {
                encryptionStateStore.writeBackupOnlyDek(backupOnlyKeyWrapper.wrap(passwordDek), keyEnvelope.toPasswordWrappedDek())
            }
            return DecryptedPayload(plaintext, recoveredDek = null)
        }
        return DecryptedPayload(plaintext, recoveredDek = passwordDek)
    }

    fun decode(bytes: ByteArray, password: CharArray?): DecodedBackup {
        val envelope = decodeBackupEnvelope(json, bytes, payloadProvider::isLegacyPayload)
        if (!envelope.encrypted) {
            return DecodedBackup(payloadJson = envelope.payload)
        }
        val iv = envelope.payloadIv?.let { Base64.getDecoder().decode(it) }
            ?: error("Encrypted backup is missing its payload IV")
        val ciphertext = Base64.getDecoder().decode(envelope.payload)
        val decrypted = decryptPayload(envelope, iv, ciphertext, password)
        return DecodedBackup(
            payloadJson = String(decrypted.plaintext),
            recoveredDek = decrypted.recoveredDek
        )
    }
}
