package com.wafflehq.base.ui.library.demos

import android.app.Application
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.backupcore.BackupRetentionMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BackupCoreDemoTest : LibraryDemoTest() {

    private val now = Instant.parse("2026-06-01T12:00:00Z")

    private fun deleteCount(mode: BackupRetentionMode, maxCount: Int = 5, maxAgeDays: Int = 14): Int {
        val candidates = BackupCoreDemoLogic.candidates(now)
        return BackupCoreDemoLogic.idsToDelete(candidates, true, mode, maxCount, maxAgeDays, now).size
    }

    @Test
    fun countModeKeepsTheNewestFiveAutomaticBackups() {
        show { BackupCoreDemo(now = now) }

        node(DemoTags.section("backupcore")).assertExists()
        assertTagText(BackupCoreTags.SUMMARY, string(R.string.libex_backupcore_summary, 6, 6))
        assertTagText(BackupCoreTags.decision("backup-1"), string(R.string.libex_backupcore_keep))
        assertTagText(BackupCoreTags.decision("backup-12"), string(R.string.libex_backupcore_delete))
    }

    @Test
    fun manualBackupsAreNeverDeleted() {
        show { BackupCoreDemo(now = now) }

        repeat(4) { stepDown(BackupCoreTags.MAX_COUNT) }

        assertTagText(BackupCoreTags.decision("backup-2"), string(R.string.libex_backupcore_delete))
        assertTagText(BackupCoreTags.decision("backup-5"), string(R.string.libex_backupcore_keep))
    }

    @Test
    fun disablingRetentionKeepsEverything() {
        show { BackupCoreDemo(now = now) }

        click(BackupCoreTags.ENABLED)

        assertTagText(BackupCoreTags.SUMMARY, string(R.string.libex_backupcore_summary, 0, 12))
    }

    @Test
    fun ageModeFollowsTheMaximumAge() {
        show { BackupCoreDemo(now = now) }
        click(BackupCoreTags.mode(BackupRetentionMode.AGE))
        val before = deleteCount(BackupRetentionMode.AGE, maxAgeDays = 14)

        assertTagText(BackupCoreTags.SUMMARY, string(R.string.libex_backupcore_summary, before, 12 - before))

        repeat(10) { stepDown(BackupCoreTags.MAX_AGE) }

        val after = deleteCount(BackupRetentionMode.AGE, maxAgeDays = 4)
        assertTagText(BackupCoreTags.SUMMARY, string(R.string.libex_backupcore_summary, after, 12 - after))
    }

    @Test
    fun versionCheckFlagsBackupsFromNewerApps() {
        show { BackupCoreDemo(now = now) }

        assertTagText(BackupCoreTags.VERSION_RESULT, string(R.string.libex_backupcore_version_too_new, 4, 3))

        stepUp(BackupCoreTags.APP_VERSION)

        assertTagText(BackupCoreTags.VERSION_RESULT, string(R.string.libex_backupcore_version_ok))
    }

    @Test
    fun candidatesAreSpreadOverTime() {
        val candidates = BackupCoreDemoLogic.candidates(now)

        assertEquals(12, candidates.size)
        assertEquals(1, candidates.count { it.isManual })
        assertEquals(0L, BackupCoreDemoLogic.ageInDays(candidates.first(), now))
        assertEquals(90L, BackupCoreDemoLogic.ageInDays(candidates.last(), now))
    }

    @Test
    fun countModeDeletesEverythingBeyondTheLimit() {
        assertEquals(6, deleteCount(BackupRetentionMode.COUNT, maxCount = 5))
        assertEquals(0, deleteCount(BackupRetentionMode.COUNT, maxCount = 12))
    }

    @Test
    fun generationalModeKeepsOneBackupPerBucket() {
        val deleted = deleteCount(BackupRetentionMode.GENERATIONAL)

        assertTrue(deleted in 1..10)
    }

    @Test
    fun versionValidatorOnlyRejectsNewerBackups() {
        assertNull(BackupCoreDemoLogic.versionTooNew(3, 3))
        assertNull(BackupCoreDemoLogic.versionTooNew(2, 3))
        val error = BackupCoreDemoLogic.versionTooNew(5, 3)
        assertNotNull(error)
        assertEquals(5, error!!.backupVersionCode)
        assertEquals(3, error.currentVersionCode)
    }
}
