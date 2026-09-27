package com.wafflehq.lib.settings.backup

import com.wafflehq.lib.database.crypto.PasswordWrappedDek
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

@Serializable
private data class TestPayload(val createdAt: String, val categories: List<String>)

class BackupEnvelopeTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun isLegacyPayload(text: String): Boolean =
        runCatching { json.decodeFromString(TestPayload.serializer(), text) }.isSuccess

    @Test
    fun `decodes an unencrypted envelope round trip`() {
        val envelope = BackupEnvelope(encrypted = false, payload = "{\"some\":\"json\"}")

        val decoded = decodeBackupEnvelope(json, json.encodeToString(envelope).toByteArray(), this::isLegacyPayload)

        assertEquals(2, decoded.format)
        assertFalse(decoded.encrypted)
        assertEquals("{\"some\":\"json\"}", decoded.payload)
        assertNull(decoded.keyEnvelope)
    }

    @Test
    fun `decodes an encrypted envelope round trip including the key envelope`() {
        val keyEnvelope = KeyEnvelopeDto(salt = "c2FsdA==", iv = "aXY=", ciphertext = "Y2lwaGVy", iterations = 600_000)
        val envelope = BackupEnvelope(
            encrypted = true,
            keyEnvelope = keyEnvelope,
            payloadIv = "cGF5bG9hZElW",
            payload = "cGF5bG9hZA=="
        )

        val decoded = decodeBackupEnvelope(json, json.encodeToString(envelope).toByteArray(), this::isLegacyPayload)

        assertTrue(decoded.encrypted)
        assertEquals(keyEnvelope, decoded.keyEnvelope)
        assertEquals("cGF5bG9hZElW", decoded.payloadIv)
        assertEquals("cGF5bG9hZA==", decoded.payload)
    }

    @Test
    fun `falls back to a legacy unenveloped payload document`() {
        val legacyPayload = TestPayload(createdAt = "2025-01-01T00:00:00Z", categories = emptyList())
        val legacyJson = json.encodeToString(legacyPayload)

        val decoded = decodeBackupEnvelope(json, legacyJson.toByteArray(), this::isLegacyPayload)

        assertEquals(1, decoded.format)
        assertFalse(decoded.encrypted)
        assertEquals(legacyJson, decoded.payload)
        assertEquals(legacyPayload, json.decodeFromString(TestPayload.serializer(), decoded.payload))
    }

    @Test
    fun `throws BackupCorruptedException for bytes that are neither an envelope nor a legacy payload document`() {
        val garbage = "{ this is not valid json at all".toByteArray()

        assertThrows(BackupCorruptedException::class.java) {
            decodeBackupEnvelope(json, garbage, this::isLegacyPayload)
        }
    }

    @Test
    fun `throws BackupCorruptedException for truncated envelope bytes`() {
        val envelope = BackupEnvelope(encrypted = true, payloadIv = "aXY=", payload = "Y2lwaGVy")
        val fullBytes = json.encodeToString(envelope).toByteArray()
        val truncated = fullBytes.copyOfRange(0, fullBytes.size / 2)

        assertThrows(BackupCorruptedException::class.java) {
            decodeBackupEnvelope(json, truncated, this::isLegacyPayload)
        }
    }

    @Test
    fun `decodes a format-3 envelope carrying backupOnlyEncryption`() {
        val envelope = BackupEnvelope(
            format = BACKUP_ENVELOPE_CURRENT_FORMAT,
            encrypted = true,
            payloadIv = "aXY=",
            payload = "Y2lwaGVy",
            backupOnlyEncryption = true
        )

        val decoded = decodeBackupEnvelope(json, json.encodeToString(envelope).toByteArray(), this::isLegacyPayload)

        assertEquals(BACKUP_ENVELOPE_AAD_FORMAT, decoded.format)
        assertTrue(decoded.backupOnlyEncryption)
    }

    @Test
    fun `serializes an unencrypted envelope with the documented field names and order`() {
        val envelope = BackupEnvelope(format = 3, encrypted = false, payload = "{}")

        assertEquals(
            "{\"format\":3,\"encrypted\":false,\"payload\":\"{}\"}",
            json.encodeToString(envelope)
        )
    }

    @Test
    fun `serializes a fully populated envelope with the documented field names and order`() {
        val envelope = BackupEnvelope(
            format = 3,
            encrypted = true,
            keyEnvelope = KeyEnvelopeDto(salt = "c2FsdA==", iv = "aXY=", ciphertext = "Y2lwaGVy", iterations = 600_000),
            payloadIv = "cGF5bG9hZElW",
            payload = "cGF5bG9hZA==",
            backupOnlyEncryption = true
        )

        assertEquals(
            "{\"format\":3,\"encrypted\":true," +
                "\"keyEnvelope\":{\"salt\":\"c2FsdA==\",\"iv\":\"aXY=\",\"ciphertext\":\"Y2lwaGVy\",\"iterations\":600000}," +
                "\"payloadIv\":\"cGF5bG9hZElW\",\"payload\":\"cGF5bG9hZA==\",\"backupOnlyEncryption\":true}",
            json.encodeToString(envelope)
        )
    }

    @Test
    fun `an envelope without a format field is read as format 2`() {
        val decoded = decodeBackupEnvelope(
            json,
            "{\"encrypted\":false,\"payload\":\"{}\"}".toByteArray(),
            this::isLegacyPayload
        )

        assertEquals(2, decoded.format)
        assertFalse(decoded.backupOnlyEncryption)
    }

    @Test
    fun `encryptionAad has the exact documented byte encoding`() {
        assertArrayEquals("format=3;encrypted=true".toByteArray(Charsets.US_ASCII), encryptionAad(3, true))
    }

    @Test
    fun `encryptionAad is deterministic for the same format and encrypted flag`() {
        assertArrayEquals(encryptionAad(3, true), encryptionAad(3, true))
    }

    @Test
    fun `encryptionAad differs when format or encrypted differ`() {
        val base = encryptionAad(3, true)
        assertFalse(base.contentEquals(encryptionAad(2, true)))
        assertFalse(base.contentEquals(encryptionAad(3, false)))
    }

    @Test
    fun `PasswordWrappedDek to dto and back round trips`() {
        val wrapped = PasswordWrappedDek(
            salt = byteArrayOf(1, 2, 3),
            iv = byteArrayOf(4, 5, 6),
            ciphertext = byteArrayOf(7, 8, 9, 10),
            iterations = 600_000
        )

        val roundTripped = wrapped.toDto().toPasswordWrappedDek()

        assertArrayEquals(wrapped.salt, roundTripped.salt)
        assertArrayEquals(wrapped.iv, roundTripped.iv)
        assertArrayEquals(wrapped.ciphertext, roundTripped.ciphertext)
        assertEquals(wrapped.iterations, roundTripped.iterations)
    }
}
