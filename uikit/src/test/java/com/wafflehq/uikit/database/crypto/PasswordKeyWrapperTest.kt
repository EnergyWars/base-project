package com.wafflehq.uikit.database.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom
import javax.crypto.AEADBadTagException

class PasswordKeyWrapperTest {

    private val wrapper = PasswordKeyWrapper()

    private fun randomDek() = ByteArray(32).also { SecureRandom().nextBytes(it) }

    @Test
    fun `wrap then unwrap with the same password returns the original DEK`() {
        val dek = randomDek()
        val wrapped = wrapper.wrap(dek, "correct horse battery staple".toCharArray())

        val unwrapped = wrapper.unwrap(wrapped, "correct horse battery staple".toCharArray())

        assertArrayEquals(dek, unwrapped)
    }

    @Test
    fun `unwrap with the wrong password throws`() {
        val wrapped = wrapper.wrap(randomDek(), "correct password".toCharArray())

        assertThrows(AEADBadTagException::class.java) {
            wrapper.unwrap(wrapped, "wrong password".toCharArray())
        }
    }

    @Test
    fun `two wraps of the same DEK use different salts`() {
        val dek = randomDek()

        val first = wrapper.wrap(dek, "password".toCharArray())
        val second = wrapper.wrap(dek, "password".toCharArray())

        assertNotEquals(first.salt.toList(), second.salt.toList())
    }

    @Test
    fun `wrap records a positive iteration count and a 16 byte salt`() {
        val wrapped = wrapper.wrap(randomDek(), "password".toCharArray())

        assertTrue(wrapped.iterations > 0)
        assertEquals(16, wrapped.salt.size)
        assertEquals(12, wrapped.iv.size)
    }

    @Test
    fun `unwrap honours the iteration count stored in the wrap`() {
        val dek = randomDek()
        val wrapped = wrapper.wrap(dek, "password".toCharArray())
        val altered = wrapped.copy(iterations = wrapped.iterations + 1)

        assertThrows(AEADBadTagException::class.java) {
            wrapper.unwrap(altered, "password".toCharArray())
        }
    }
}
