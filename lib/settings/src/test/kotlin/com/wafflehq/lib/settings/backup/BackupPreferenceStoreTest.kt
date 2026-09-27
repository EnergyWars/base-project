package com.wafflehq.lib.settings.backup

import com.wafflehq.lib.backupcore.BackupRetentionMode
import com.wafflehq.lib.settings.encryption.PasswordReminderInterval
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BackupPreferenceStoreTest {

    private lateinit var store: BackupPreferenceStore

    @Before
    fun setUp() {
        store = BackupPreferenceStore(FakePreferencesDataStore())
    }

    @Test
    fun `defaults match the shipped behaviour`() = runBlocking {
        assertFalse(store.backupEnabled.first())
        assertEquals("", store.backupAccount.first())
        assertEquals("", store.lastBackupTime.first())
        assertEquals(BackupPreferenceStore.DEFAULT_BACKUP_TIME_MINUTES, store.backupTimeMinutes.first())
        assertTrue(store.backupFailureAcknowledged.first())
        assertFalse(store.hasLocalExport.first())
        assertEquals(0, store.encryptionNagSuppressCount.first())
        assertFalse(store.driveAuthorized.first())
        assertEquals("", store.driveAccountEmail.first())
        assertEquals(PasswordReminderInterval.NEVER, store.passwordReminderInterval.first())
        assertEquals(0L, store.passwordReminderLastShownAt.first())
        assertTrue(store.retentionPolicyEnabled.first())
        assertEquals(BackupRetentionMode.COUNT, store.retentionMode.first())
        assertEquals(BackupPreferenceStore.DEFAULT_RETENTION_MAX_COUNT, store.retentionMaxCount.first())
        assertEquals(BackupPreferenceStore.DEFAULT_RETENTION_MAX_AGE_DAYS, store.retentionMaxAgeDays.first())
    }

    @Test
    fun `schedule and account settings round trip`() = runBlocking {
        store.setBackupEnabled(true)
        store.setBackupAccount("me@example.com")
        store.setLastBackupTime("2026-01-01T00:00:00Z")
        store.setBackupTimeMinutes(420)
        store.setHasLocalExport(true)

        assertTrue(store.backupEnabled.first())
        assertEquals("me@example.com", store.backupAccount.first())
        assertEquals("2026-01-01T00:00:00Z", store.lastBackupTime.first())
        assertEquals(420, store.backupTimeMinutes.first())
        assertTrue(store.hasLocalExport.first())
    }

    @Test
    fun `the drive authorization state round trips`() = runBlocking {
        store.setDriveAccountEmail("me@example.com")
        store.setDriveAuthorized(true)

        assertEquals("me@example.com", store.driveAccountEmail.first())
        assertTrue(store.driveAuthorized.first())

        store.setDriveAccountEmail("")
        store.setDriveAuthorized(false)

        assertEquals("", store.driveAccountEmail.first())
        assertFalse(store.driveAuthorized.first())
    }

    @Test
    fun `retention settings round trip`() = runBlocking {
        store.setRetentionPolicyEnabled(false)
        store.setRetentionMode(BackupRetentionMode.GENERATIONAL)
        store.setRetentionMaxCount(3)
        store.setRetentionMaxAgeDays(90)

        assertFalse(store.retentionPolicyEnabled.first())
        assertEquals(BackupRetentionMode.GENERATIONAL, store.retentionMode.first())
        assertEquals(3, store.retentionMaxCount.first())
        assertEquals(90, store.retentionMaxAgeDays.first())
    }

    @Test
    fun `password reminder settings round trip`() = runBlocking {
        store.setPasswordReminderInterval(PasswordReminderInterval.WEEKLY)
        store.setPasswordReminderLastShownAt(1_700_000_000_000L)
        store.incrementEncryptionNagSuppressCount()
        store.incrementEncryptionNagSuppressCount()

        assertEquals(PasswordReminderInterval.WEEKLY, store.passwordReminderInterval.first())
        assertEquals(1_700_000_000_000L, store.passwordReminderLastShownAt.first())
        assertEquals(2, store.encryptionNagSuppressCount.first())
    }

    @Test
    fun `a recorded failure becomes a ghost entry and re-arms the failure dialog`() = runBlocking {
        store.acknowledgeBackupFailure()

        store.recordBackupFailure("network error")

        val attempts = store.failedBackupAttempts.first()
        assertEquals(1, attempts.size)
        assertEquals("network error", attempts.first().reason)
        assertFalse(store.backupFailureAcknowledged.first())
    }

    @Test
    fun `acknowledging a failure keeps the ghost entries`() = runBlocking {
        store.recordBackupFailure("network error")

        store.acknowledgeBackupFailure()

        assertTrue(store.backupFailureAcknowledged.first())
        assertEquals(1, store.failedBackupAttempts.first().size)
    }

    @Test
    fun `a ghost entry can be removed by id`() = runBlocking {
        store.recordBackupFailure("network error")
        val id = store.failedBackupAttempts.first().first().id

        store.removeFailedBackupAttempt(id)

        assertTrue(store.failedBackupAttempts.first().isEmpty())
    }

    @Test
    fun `failed attempts are capped at the retention window`() = runBlocking {
        repeat(FailedBackupAttempts.MAX_ENTRIES + 4) { store.recordBackupFailure("failure $it") }

        val attempts = store.failedBackupAttempts.first()
        assertEquals(FailedBackupAttempts.MAX_ENTRIES, attempts.size)
        assertEquals("failure ${FailedBackupAttempts.MAX_ENTRIES + 3}", attempts.first().reason)
    }
}
