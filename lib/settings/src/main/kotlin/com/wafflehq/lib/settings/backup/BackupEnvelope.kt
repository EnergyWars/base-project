package com.wafflehq.lib.settings.backup

import com.wafflehq.lib.database.crypto.PasswordWrappedDek
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Base64

@Serializable
data class KeyEnvelopeDto(
    val salt: String,
    val iv: String,
    val ciphertext: String,
    val iterations: Int
)

fun PasswordWrappedDek.toDto(): KeyEnvelopeDto = KeyEnvelopeDto(
    salt = Base64.getEncoder().encodeToString(salt),
    iv = Base64.getEncoder().encodeToString(iv),
    ciphertext = Base64.getEncoder().encodeToString(ciphertext),
    iterations = iterations
)

fun KeyEnvelopeDto.toPasswordWrappedDek(): PasswordWrappedDek = PasswordWrappedDek(
    salt = Base64.getDecoder().decode(salt),
    iv = Base64.getDecoder().decode(iv),
    ciphertext = Base64.getDecoder().decode(ciphertext),
    iterations = iterations
)

@Serializable
data class BackupEnvelope(
    val format: Int = 2,
    val encrypted: Boolean,
    val keyEnvelope: KeyEnvelopeDto? = null,
    val payloadIv: String? = null,
    val payload: String,
    val backupOnlyEncryption: Boolean = false
)

const val BACKUP_ENVELOPE_AAD_FORMAT = 3

const val BACKUP_ENVELOPE_CURRENT_FORMAT = 3

fun encryptionAad(format: Int, encrypted: Boolean): ByteArray =
    "format=$format;encrypted=$encrypted".toByteArray(Charsets.US_ASCII)

class BackupCorruptedException : Exception()

const val MAX_BACKUP_FILE_BYTES = 300L * 1024 * 1024

fun decodeBackupEnvelope(json: Json, bytes: ByteArray, isLegacyPayload: (String) -> Boolean): BackupEnvelope {
    if (bytes.size > MAX_BACKUP_FILE_BYTES) throw BackupCorruptedException()
    val text = String(bytes)
    try {
        return json.decodeFromString(BackupEnvelope.serializer(), text)
    } catch (envelopeError: Throwable) {
        if (isLegacyPayload(text)) return BackupEnvelope(format = 1, encrypted = false, payload = text)
        throw BackupCorruptedException()
    }
}
