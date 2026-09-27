package com.wafflehq.lib.backupcore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupVersionValidatorTest {

    @Test
    fun `same version does not throw`() {
        BackupVersionValidator.validate(backupVersionCode = 1, currentVersionCode = 1)
    }

    @Test
    fun `backup older than current does not throw`() {
        BackupVersionValidator.validate(backupVersionCode = 1, currentVersionCode = 5)
    }

    @Test
    fun `backup version zero does not throw`() {
        BackupVersionValidator.validate(backupVersionCode = 0, currentVersionCode = 1)
    }

    @Test
    fun `negative backup version does not throw`() {
        BackupVersionValidator.validate(backupVersionCode = -1, currentVersionCode = 0)
    }

    @Test
    fun `maximum versions on both sides do not throw`() {
        BackupVersionValidator.validate(backupVersionCode = Int.MAX_VALUE, currentVersionCode = Int.MAX_VALUE)
    }

    @Test
    fun `backup exactly one version newer throws`() {
        val e = assertThrows(BackupVersionTooNewException::class.java) {
            BackupVersionValidator.validate(backupVersionCode = 2, currentVersionCode = 1)
        }

        assertEquals(2, e.backupVersionCode)
        assertEquals(1, e.currentVersionCode)
    }

    @Test
    fun `backup much newer than current throws`() {
        val e = assertThrows(BackupVersionTooNewException::class.java) {
            BackupVersionValidator.validate(backupVersionCode = 100, currentVersionCode = 1)
        }

        assertEquals(100, e.backupVersionCode)
        assertEquals(1, e.currentVersionCode)
    }

    @Test
    fun `backup newer than a current version of zero throws`() {
        assertThrows(BackupVersionTooNewException::class.java) {
            BackupVersionValidator.validate(backupVersionCode = 1, currentVersionCode = 0)
        }
    }

    @Test
    fun `maximum backup version against a lower current version throws`() {
        assertThrows(BackupVersionTooNewException::class.java) {
            BackupVersionValidator.validate(backupVersionCode = Int.MAX_VALUE, currentVersionCode = Int.MAX_VALUE - 1)
        }
    }

    @Test
    fun `exception message contains both version codes`() {
        val e = assertThrows(BackupVersionTooNewException::class.java) {
            BackupVersionValidator.validate(backupVersionCode = 5, currentVersionCode = 3)
        }

        assertEquals("Backup version 5 > app version 3", e.message)
    }
}
