package com.wafflehq.lib.settings.backup.ui

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.backupcore.BackupRetentionMode
import com.wafflehq.lib.database.state.EncryptionStateStore
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.backup.BackupPreferenceStore
import com.wafflehq.lib.settings.backup.BackupSettingsController
import com.wafflehq.lib.settings.backup.FakeBackupOperations
import com.wafflehq.lib.settings.backup.FakePreferencesDataStore
import com.wafflehq.lib.settings.backup.InMemoryBackupFileGateway
import com.wafflehq.lib.settings.backup.RecordingBackupScheduler
import com.wafflehq.lib.settings.backup.RecordingRestoredEncryptionActivator
import com.wafflehq.lib.settings.backup.backupMeta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BackupSettingsScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private fun str(id: Int) = context.getString(id)

    private lateinit var operations: FakeBackupOperations
    private lateinit var prefs: BackupPreferenceStore
    private lateinit var scheduler: RecordingBackupScheduler
    private val quickTimeInput = MutableStateFlow(false)
    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @Before
    fun setUp() {
        operations = FakeBackupOperations()
        prefs = BackupPreferenceStore(FakePreferencesDataStore())
        scheduler = RecordingBackupScheduler()
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun signedIn() {
        operations.linkAccount("me@example.com")
        operations.hasScopes = true
    }

    private fun setContent(): BackupSettingsController {
        val controller = BackupSettingsController(
            operations = operations,
            backupPrefs = prefs,
            scheduler = scheduler,
            files = InMemoryBackupFileGateway(),
            restoredEncryptionActivator = RecordingRestoredEncryptionActivator(),
            encryptionStateStore = EncryptionStateStore(context, prefsName = "encryption_state_backup_screen"),
            useQuickTimeInput = quickTimeInput,
            scope = scope
        )
        rule.setContent {
            BackupSettingsScreen(
                controller = controller,
                onBack = {},
                exportFileNamePrefix = "test_export",
                unencryptedWarningText = WARNING,
                timeField = { time, label, _, _ -> Text("$label $time") },
                formatTimestamp = { "01.02.2026 08:30" }
            )
        }
        return controller
    }

    @Test
    fun `without an account only the sign-in action is offered`() {
        setContent()

        rule.onNodeWithTag(BackupSettingsTestTags.SIGN_IN_ACTION).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.appsettings_backup_sign_in_description)).assertExists()
        rule.onNodeWithTag(BackupSettingsTestTags.SIGN_OUT_ACTION).assertDoesNotExist()
        rule.onNodeWithTag(BackupSettingsTestTags.CREATE_NOW_ACTION).assertDoesNotExist()
    }

    @Test
    fun `a signed-in account without backups shows the account row and the empty hint`() {
        signedIn()

        setContent()

        rule.onNodeWithText("me@example.com").assertExists()
        rule.onNodeWithTag(BackupSettingsTestTags.SIGN_OUT_ACTION).assertExists()
        rule.onNodeWithTag(BackupSettingsTestTags.EMPTY).assertExists()
        rule.onNodeWithTag(BackupSettingsTestTags.SIGN_IN_ACTION).assertDoesNotExist()
    }

    @Test
    fun `every backup gets a restore and a delete action`() {
        signedIn()
        operations.listResult = Result.success(
            listOf(
                backupMeta("alpha", Instant.parse("2026-02-01T08:30:00Z"), encrypted = true, sizeBytes = 2048),
                backupMeta("beta", Instant.parse("2026-01-01T08:30:00Z"), isManual = true)
            )
        )

        setContent()

        rule.onNodeWithTag(BackupSettingsTestTags.EMPTY).assertDoesNotExist()
        listOf("alpha", "beta").forEach { id ->
            rule.onNodeWithTag(BackupSettingsTestTags.row(id)).assertExists()
            rule.onNodeWithTag(BackupSettingsTestTags.restore(id)).assertExists()
            rule.onNodeWithTag(BackupSettingsTestTags.delete(id)).assertExists()
        }
    }

    @Test
    fun `a failed attempt is listed as a ghost entry that can only be dismissed`() {
        signedIn()
        runBlocking { prefs.recordBackupFailure("no network") }

        val controller = setContent()
        val ghostId = controller.uiState.value.backups.single { it.isFailed }.id

        rule.onNodeWithTag(BackupSettingsTestTags.row(ghostId)).assertExists()
        rule.onNodeWithTag(BackupSettingsTestTags.delete(ghostId)).assertExists()
        rule.onNodeWithTag(BackupSettingsTestTags.restore(ghostId)).assertDoesNotExist()
        rule.onNodeWithText(str(R.string.appsettings_backup_failed_label) + " · no network").assertExists()
    }

    @Test
    fun `toggling automatic backup reveals the time field and schedules the work`() {
        signedIn()

        setContent()
        rule.onNodeWithTag(BackupSettingsTestTags.AUTO_TIME_FIELD).assertDoesNotExist()

        rule.onNodeWithText(str(R.string.appsettings_backup_auto_toggle)).performClick()
        rule.waitForIdle()

        rule.onNodeWithTag(BackupSettingsTestTags.AUTO_TIME_FIELD).assertExists()
        assertTrue(runBlocking { prefs.backupEnabled.first() })
        assertEquals(1, scheduler.scheduleCount)
    }

    @Test
    fun `the unencrypted warning appears once automatic backup is on`() {
        signedIn()

        setContent()
        rule.onNodeWithTag(BackupSettingsTestTags.UNENCRYPTED_WARNING).assertDoesNotExist()

        rule.onNodeWithText(str(R.string.appsettings_backup_auto_toggle)).performClick()
        rule.waitForIdle()

        rule.onNodeWithTag(BackupSettingsTestTags.UNENCRYPTED_WARNING).assertExists()
        rule.onNodeWithText(WARNING).assertExists()
    }

    @Test
    fun `the retention rule decides which field is shown`() {
        signedIn()

        val controller = setContent()

        rule.onNodeWithTag(BackupSettingsTestTags.RETENTION_MODE_FIELD).assertExists()
        rule.onNodeWithTag(BackupSettingsTestTags.RETENTION_MAX_COUNT_FIELD).assertExists()

        controller.onRetentionModeChanged(BackupRetentionMode.AGE)
        rule.waitForIdle()
        rule.onNodeWithTag(BackupSettingsTestTags.RETENTION_MAX_AGE_FIELD).assertExists()
        rule.onNodeWithTag(BackupSettingsTestTags.RETENTION_MAX_COUNT_FIELD).assertDoesNotExist()

        controller.onRetentionModeChanged(BackupRetentionMode.GENERATIONAL)
        rule.waitForIdle()
        rule.onNodeWithTag(BackupSettingsTestTags.RETENTION_GENERATIONAL_HINT).assertExists()
        rule.onNodeWithTag(BackupSettingsTestTags.RETENTION_MAX_AGE_FIELD).assertDoesNotExist()
    }

    @Test
    fun `switching the retention policy off hides its fields`() {
        signedIn()

        val controller = setContent()
        rule.onNodeWithText(str(R.string.appsettings_backup_retention_toggle)).performClick()
        rule.waitForIdle()

        assertTrue(!controller.uiState.value.retentionPolicyEnabled)
        rule.onNodeWithTag(BackupSettingsTestTags.RETENTION_MODE_FIELD).assertDoesNotExist()
    }

    @Test
    fun `restoring is only run after the confirmation is accepted`() {
        signedIn()
        operations.listResult = Result.success(listOf(backupMeta("alpha", Instant.parse("2026-02-01T08:30:00Z"))))

        setContent()

        rule.onNodeWithTag(BackupSettingsTestTags.restore("alpha")).performClick()
        rule.waitForIdle()
        rule.onNodeWithText(str(R.string.appsettings_backup_restore_confirm_title)).assertExists()
        assertTrue(operations.restoreCalls.isEmpty())

        rule.onNodeWithTag(BackupSettingsTestTags.RESTORE_CONFIRM).performClick()
        rule.waitForIdle()

        assertEquals(listOf("alpha" to null), operations.restoreCalls)
    }

    @Test
    fun `deleting is only run after the confirmation is accepted`() {
        signedIn()
        operations.listResult = Result.success(listOf(backupMeta("alpha", Instant.parse("2026-02-01T08:30:00Z"))))

        setContent()

        rule.onNodeWithTag(BackupSettingsTestTags.delete("alpha")).performClick()
        rule.waitForIdle()
        rule.onNodeWithText(str(R.string.appsettings_backup_delete_confirm_title)).assertExists()
        assertTrue(operations.deletedIds.isEmpty())

        rule.onNodeWithTag(BackupSettingsTestTags.DELETE_CONFIRM).performClick()
        rule.waitForIdle()

        assertEquals(listOf("alpha"), operations.deletedIds)
    }

    @Test
    fun `creating a backup asks first and reports the result`() {
        signedIn()

        setContent()

        rule.onNodeWithTag(BackupSettingsTestTags.CREATE_NOW_ACTION).performClick()
        rule.waitForIdle()
        rule.onNodeWithText(str(R.string.appsettings_backup_create_confirm_title)).assertExists()

        rule.onNodeWithTag(BackupSettingsTestTags.CREATE_CONFIRM).performClick()
        rule.waitForIdle()

        assertEquals(listOf(true), operations.createdManualFlags)
    }

    @Test
    fun `a listing failure is shown as a dismissible error`() {
        signedIn()
        operations.listResult = Result.failure(IllegalArgumentException("broken"))

        setContent()

        rule.onNodeWithText(str(R.string.appsettings_backup_error_title)).assertExists()
        rule.onNodeWithText(str(R.string.appsettings_backup_error_corrupted)).assertExists()

        rule.onNodeWithTag(BackupSettingsTestTags.ERROR_DISMISS).performClick()
        rule.waitForIdle()

        rule.onNodeWithText(str(R.string.appsettings_backup_error_title)).assertDoesNotExist()
    }

    private companion object {
        const val WARNING = "Backups are not encrypted"
    }
}
