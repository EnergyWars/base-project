package com.wafflehq.uikit.database.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.security.SecureRandom
import javax.crypto.AEADBadTagException

class AesGcmCipherTest {

    private fun randomKey() = ByteArray(32).also { SecureRandom().nextBytes(it) }

    @Test
    fun `encrypt then decrypt returns original plaintext`() {
        val key = randomKey()
        val plaintext = "hello world".toByteArray()

        val encrypted = AesGcmCipher.encrypt(key, plaintext)
        val decrypted = AesGcmCipher.decrypt(key, encrypted.iv, encrypted.ciphertext)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `two encryptions of the same plaintext use different IVs and ciphertexts`() {
        val key = randomKey()
        val plaintext = "same input".toByteArray()

        val first = AesGcmCipher.encrypt(key, plaintext)
        val second = AesGcmCipher.encrypt(key, plaintext)

        assertNotEquals(first.iv.toList(), second.iv.toList())
        assertNotEquals(first.ciphertext.toList(), second.ciphertext.toList())
    }

    @Test
    fun `decrypt with wrong key throws`() {
        val encrypted = AesGcmCipher.encrypt(randomKey(), "secret".toByteArray())

        assertThrows(AEADBadTagException::class.java) {
            AesGcmCipher.decrypt(randomKey(), encrypted.iv, encrypted.ciphertext)
        }
    }

    @Test
    fun `decrypt with tampered ciphertext throws`() {
        val key = randomKey()
        val encrypted = AesGcmCipher.encrypt(key, "secret".toByteArray())
        val tampered = encrypted.ciphertext.copyOf().also { it[0] = it[0].inc() }

        assertThrows(AEADBadTagException::class.java) {
            AesGcmCipher.decrypt(key, encrypted.iv, tampered)
        }
    }
}
