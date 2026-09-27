package com.wafflehq.lib.database.crypto

import org.junit.Assert.assertEquals
import org.junit.Test

class BackupPasswordStrengthTest {

    @Test
    fun `shorter than the minimum length is always weak`() {
        assertEquals(PasswordStrength.WEAK, BackupPasswordStrength.evaluate("Ab1!Ab1"))
    }

    @Test
    fun `common password is weak even when long enough`() {
        assertEquals(PasswordStrength.WEAK, BackupPasswordStrength.evaluate("superpassword123"))
    }

    @Test
    fun `repeated single character is weak regardless of length`() {
        assertEquals(PasswordStrength.WEAK, BackupPasswordStrength.evaluate("aaaaaaaaaaaaaa"))
    }

    @Test
    fun `ascending sequential run is weak`() {
        assertEquals(PasswordStrength.WEAK, BackupPasswordStrength.evaluate("abcdefghijklmnop"))
    }

    @Test
    fun `descending sequential run is weak`() {
        assertEquals(PasswordStrength.WEAK, BackupPasswordStrength.evaluate("ponmlkjihgfedcba"))
    }

    @Test
    fun `long password using only lowercase letters is medium at best`() {
        assertEquals(PasswordStrength.WEAK, BackupPasswordStrength.evaluate("correcthorsebattery"))
    }

    @Test
    fun `ten or more chars with two character classes is medium`() {
        assertEquals(PasswordStrength.MEDIUM, BackupPasswordStrength.evaluate("Sturmvogel42"))
    }

    @Test
    fun `long password with three or more character classes is strong`() {
        assertEquals(PasswordStrength.STRONG, BackupPasswordStrength.evaluate("Sturmvogel-42-Ozean!"))
    }

    @Test
    fun `minimum length constant is ten`() {
        assertEquals(10, BackupPasswordStrength.MIN_LENGTH)
    }
}
