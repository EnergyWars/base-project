package com.wafflehq.lib.database.crypto

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class PasswordWrappedDek(
    val salt: ByteArray,
    val iv: ByteArray,
    val ciphertext: ByteArray,
    val iterations: Int
)

class PasswordKeyWrapper {

    fun wrap(dek: ByteArray, password: CharArray): PasswordWrappedDek {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt, ITERATIONS)
        try {
            val encrypted = AesGcmCipher.encrypt(key, dek)
            return PasswordWrappedDek(salt = salt, iv = encrypted.iv, ciphertext = encrypted.ciphertext, iterations = ITERATIONS)
        } finally {
            key.fill(0)
        }
    }

    fun unwrap(wrapped: PasswordWrappedDek, password: CharArray): ByteArray {
        require(wrapped.iterations in MIN_ITERATIONS..MAX_ITERATIONS) { "Iteration count out of the accepted range" }
        val key = deriveKey(password, wrapped.salt, wrapped.iterations)
        try {
            return AesGcmCipher.decrypt(key, wrapped.iv, wrapped.ciphertext)
        } finally {
            key.fill(0)
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    companion object {
        const val MIN_ITERATIONS = 10_000
        const val MAX_ITERATIONS = 10_000_000
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 600_000
        private const val SALT_LENGTH_BYTES = 16
        private const val KEY_LENGTH_BITS = 256
    }
}
