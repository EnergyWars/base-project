package com.wafflehq.lib.settings.backup

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.wafflehq.lib.backupcore.BackupRetentionMode
import com.wafflehq.lib.database.state.EncryptionStateStore
import com.wafflehq.lib.settings.backup.access.AuthorizationOutcome
import com.wafflehq.lib.settings.core.SettingsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class BackupSettingsControllerTest {

    private lateinit var operations: FakeBackupOperations
    private lateinit var prefs: BackupPreferenceStore
    private lateinit var scheduler: RecordingBackupScheduler
    private lateinit var files: InMemoryBackupFileGateway
    private lateinit var activator: RecordingRestoredEncryptionActivator
    private lateinit var stateStore: EncryptionStateStore
    private val quickTimeInput = MutableStateFlow(false)
    private val scopes = mutableListOf<CoroutineScope>()

    @Before
    fun setUp() {
        prefsCounter++
        operations = FakeBackupOperations()
        prefs = BackupPreferenceStore(FakePreferencesDataStore())
        scheduler = RecordingBackupScheduler()
        files = InMemoryBackupFileGateway()
        activator = RecordingRestoredEncryptionActivator()
        stateStore = EncryptionStateStore(
            ApplicationProvider.getApplicationContext(),
            prefsName = "encryption_state_backup_controller_$prefsCounter"
        )
    }

    @After
    fun tearDown() {
        scopes.forEach { it.cancel() }
        scopes.clear()
    }

    private fun controller(): BackupSettingsController {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        scopes += scope
        return BackupSettingsController(
            operations = operations,
            backupPrefs = prefs,
            scheduler = scheduler,
            files = files,
            restoredEncryptionActivator = activator,
            encryptionStateStore = stateStore,
            useQuickTimeInput = quickTimeInput,
            scope = scope
        )
    }

    private fun signedIn() {
        operations.linkAccount("me@example.com")
        operations.hasScopes = true
    }

    @Test
    fun `without a linked account the state stays signed out and nothing is listed`() {
        val controller = controller()

        assertFalse(controller.uiState.value.isSignedIn)
        assertEquals("", controller.uiState.value.accountEmail)
        assertEquals(0, operations.listCount)
    }

    @Test
    fun `a linked account without the drive scope counts as signed out`() {
        operations.linkAccount("me@example.com")
        operations.hasScopes = false

        val controller = controller()

        assertFalse(controller.uiState.value.isSignedIn)
        assertEquals(0, operations.listCount)
    }

    @Test
    fun `a linked account loads the backup list on start`() {
        signedIn()
        operations.listResult = Result.success(
            listOf(
                backupMeta("old", Instant.parse("2026-01-01T00:00:00Z")),
                backupMeta("new", Instant.parse("2026-02-01T00:00:00Z"))
            )
        )

        val controller = controller()

        val state = controller.uiState.value
        assertTrue(state.isSignedIn)
        assertEquals("me@example.com", state.accountEmail)
        assertEquals(listOf("new", "old"), state.backups.map { it.id })
        assertFalse(state.isLoading)
    }

    @Test
    fun `failed attempts show up as ghost entries merged by date`() = runBlocking {
        signedIn()
        prefs.recordBackupFailure("no network")
        operations.listResult = Result.success(listOf(backupMeta("real", Instant.parse("2020-01-01T00:00:00Z"))))

        val controller = controller()

        val state = controller.uiState.value
        assertEquals(2, state.backups.size)
        val ghost = state.backups.first()
        assertTrue(ghost.isFailed)
        assertEquals("no network", ghost.failureReason)
        assertEquals("real", state.backups.last().id)
    }

    @Test
    fun `a listing failure surfaces the cause`() {
        signedIn()
        val boom = IllegalStateException("drive down")
        operations.listResult = Result.failure(boom)

        val controller = controller()

        assertEquals(SettingsText.Literal("drive down"), controller.uiState.value.error)
        assertFalse(controller.uiState.value.isLoading)
    }

    @Test
    fun `enabling automatic backup persists the flag and schedules the work`() = runBlocking {
        val controller = controller()

        controller.onAutoBackupToggled(true)

        assertTrue(prefs.backupEnabled.first())
        assertTrue(controller.uiState.value.autoBackupEnabled)
        assertEquals(1, scheduler.scheduleCount)
        assertEquals(0, scheduler.cancelCount)
    }

    @Test
    fun `disabling automatic backup cancels the work`() = runBlocking {
        val controller = controller()
        controller.onAutoBackupToggled(true)

        controller.onAutoBackupToggled(false)

        assertFalse(prefs.backupEnabled.first())
        assertEquals(1, scheduler.cancelCount)
    }

    @Test
    fun `changing the backup time persists the minutes and reschedules`() = runBlocking {
        val controller = controller()

        controller.onBackupTimeChanged(7 * 60 + 30)

        assertEquals(450, prefs.backupTimeMinutes.first())
        assertEquals(450, controller.uiState.value.backupTimeMinutes)
        assertEquals(1, scheduler.scheduleCount)
    }

    @Test
    fun `retention settings round trip into the state`() = runBlocking {
        val controller = controller()

        controller.onRetentionPolicyToggled(false)
        controller.onRetentionModeChanged(BackupRetentionMode.AGE)
        controller.onRetentionMaxCountChanged(42)
        controller.onRetentionMaxAgeDaysChanged(99)

        val state = controller.uiState.value
        assertFalse(state.retentionPolicyEnabled)
        assertEquals(BackupRetentionMode.AGE, state.retentionMode)
        assertEquals(42, state.retentionMaxCount)
        assertEquals(99, state.retentionMaxAgeDays)
        assertEquals(BackupRetentionMode.AGE, prefs.retentionMode.first())
    }

    @Test
    fun `the quick time input preference is mirrored into the state`() {
        val controller = controller()
        assertFalse(controller.uiState.value.useQuickTimeInput)

        quickTimeInput.value = true

        assertTrue(controller.uiState.value.useQuickTimeInput)
    }

    @Test
    fun `linking an account here turns automatic backup on once`() = runBlocking {
        val controller = controller()
        signedIn()

        controller.onSignedIn()

        assertTrue(prefs.backupEnabled.first())
        assertEquals(1, scheduler.scheduleCount)
        assertTrue(controller.uiState.value.isSignedIn)
    }

    @Test
    fun `signing out clears the list and stops the automatic backup`() = runBlocking {
        signedIn()
        operations.listResult = Result.success(listOf(backupMeta("one", Instant.parse("2026-01-01T00:00:00Z"))))
        val controller = controller()
        controller.onAutoBackupToggled(true)

        controller.confirmSignOut()
        assertTrue(controller.uiState.value.signOutConfirm)
        controller.signOut()

        val state = controller.uiState.value
        assertFalse(state.signOutConfirm)
        assertFalse(state.isSignedIn)
        assertEquals("", state.accountEmail)
        assertTrue(state.backups.isEmpty())
        assertEquals(1, operations.signOutCount)
        assertFalse(prefs.backupEnabled.first())
        assertEquals(1, scheduler.cancelCount)
    }

    @Test
    fun `creating a backup stores its timestamp and reports success`() = runBlocking {
        signedIn()
        val created = backupMeta("fresh", Instant.parse("2026-03-01T10:00:00Z"))
        operations.createResult = Result.success(created)
        val controller = controller()

        controller.confirmCreateBackup()
        assertTrue(controller.uiState.value.createConfirm)
        controller.createBackup()

        val state = controller.uiState.value
        assertFalse(state.createConfirm)
        assertEquals(BackupMessage.CREATED, state.snackbarMessage)
        assertEquals(listOf(true), operations.createdManualFlags)
        assertEquals(created.createdAt.toString(), prefs.lastBackupTime.first())

        controller.clearSnackbar()
        assertNull(controller.uiState.value.snackbarMessage)
    }

    @Test
    fun `deleting a drive backup removes it and reloads`() {
        signedIn()
        val meta = backupMeta("gone", Instant.parse("2026-01-01T00:00:00Z"))
        operations.listResult = Result.success(listOf(meta))
        val controller = controller()
        val listsBefore = operations.listCount

        controller.confirmDelete(meta)
        controller.deleteBackup()

        assertEquals(listOf("gone"), operations.deletedIds)
        assertEquals(BackupMessage.DELETED, controller.uiState.value.snackbarMessage)
        assertEquals(listsBefore + 1, operations.listCount)
    }

    @Test
    fun `deleting a ghost entry only drops the recorded attempt`() = runBlocking {
        signedIn()
        prefs.recordBackupFailure("no network")
        val controller = controller()
        val ghost = controller.uiState.value.backups.first { it.isFailed }

        controller.confirmDelete(ghost)
        controller.deleteBackup()

        assertTrue(operations.deletedIds.isEmpty())
        assertTrue(prefs.failedBackupAttempts.first().isEmpty())
        assertTrue(controller.uiState.value.backups.isEmpty())
    }

    @Test
    fun `restoring an encrypted backup asks for the password and retries with it`() {
        signedIn()
        val meta = backupMeta("secret", Instant.parse("2026-01-01T00:00:00Z"), encrypted = true)
        operations.listResult = Result.success(listOf(meta))
        operations.restoreResult = Result.failure(BackupPasswordRequiredException())
        val controller = controller()

        controller.confirmRestore(meta)
        controller.restoreBackup()

        val prompt = controller.uiState.value.passwordPrompt
        assertNotNull(prompt)
        assertFalse(prompt!!.wrongPassword)
        assertEquals(RestoreSource.Drive(meta), prompt.source)
        assertNull(controller.uiState.value.error)

        operations.restoreResult = Result.failure(BackupWrongPasswordException())
        controller.retryWithPassword("nope")
        assertTrue(controller.uiState.value.passwordPrompt!!.wrongPassword)

        operations.restoreResult = Result.success(RestoreOutcome(recoveredDek = "dek".toByteArray()))
        controller.retryWithPassword("correct")

        assertNull(controller.uiState.value.passwordPrompt)
        assertEquals(BackupMessage.RESTORED, controller.uiState.value.snackbarMessage)
        assertEquals(listOf("dek" to "correct"), activator.activations)
        assertEquals(listOf("secret" to null, "secret" to "nope", "secret" to "correct"), operations.restoreCalls)
    }

    @Test
    fun `a plain restore does not re-enable local encryption`() {
        signedIn()
        val meta = backupMeta("plain", Instant.parse("2026-01-01T00:00:00Z"))
        val controller = controller()

        controller.confirmRestore(meta)
        controller.restoreBackup()

        assertEquals(BackupMessage.RESTORED, controller.uiState.value.snackbarMessage)
        assertTrue(activator.activations.isEmpty())
    }

    @Test
    fun `exporting writes the payload to the picked file`() = runBlocking {
        val controller = controller()

        controller.exportToFile(Uri.parse("content://test/export.json"))

        assertEquals("exported-payload", files.writtenText())
        assertEquals(BackupMessage.EXPORTED, controller.uiState.value.snackbarMessage)
        assertTrue(prefs.hasLocalExport.first())
    }

    @Test
    fun `an unwritable export target becomes an error`() {
        files.writable = false
        val controller = controller()

        controller.exportToFile(Uri.parse("content://test/export.json"))

        assertEquals(SettingsText.Literal("Could not open content://test/export.json for writing"), controller.uiState.value.error)
        assertFalse(controller.uiState.value.isLoading)
    }

    @Test
    fun `importing reads the picked file after confirmation`() {
        val uri = Uri.parse("content://test/import.json")
        val controller = controller()

        controller.confirmLocalImport(uri)
        assertEquals(uri, controller.uiState.value.localImportConfirmUri)
        controller.importFromFile()

        assertNull(controller.uiState.value.localImportConfirmUri)
        assertEquals(listOf("imported-payload"), operations.importedBytes)
        assertEquals(BackupMessage.RESTORED, controller.uiState.value.snackbarMessage)
    }

    @Test
    fun `dismissing a confirmation leaves the state untouched`() {
        signedIn()
        val meta = backupMeta("keep", Instant.parse("2026-01-01T00:00:00Z"))
        val controller = controller()

        controller.confirmRestore(meta)
        controller.dismissRestore()
        controller.confirmDelete(meta)
        controller.dismissDelete()
        controller.confirmCreateBackup()
        controller.dismissCreateBackup()
        controller.confirmSignOut()
        controller.dismissSignOut()
        controller.confirmLocalImport(Uri.parse("content://test/x.json"))
        controller.dismissLocalImport()

        val state = controller.uiState.value
        assertNull(state.restoreConfirmTarget)
        assertNull(state.deleteConfirmTarget)
        assertFalse(state.createConfirm)
        assertFalse(state.signOutConfirm)
        assertNull(state.localImportConfirmUri)
        assertTrue(operations.restoreCalls.isEmpty())
        assertTrue(operations.deletedIds.isEmpty())
        assertTrue(operations.createdManualFlags.isEmpty())
    }

    @Test
    fun `an error can be cleared again`() {
        operations.listResult = Result.failure(IllegalStateException("drive down"))
        signedIn()
        val controller = controller()
        assertEquals(SettingsText.Literal("drive down"), controller.uiState.value.error)

        controller.clearError()

        assertNull(controller.uiState.value.error)
    }

    @Test
    fun `backups count as encrypted when only the backup key is set up`() {
        operations.backupOnlyEncryption = true

        assertTrue(controller().backupsAreEncrypted)
    }

    @Test
    fun `backups count as unencrypted without a database key and without a backup key`() {
        assertFalse(controller().backupsAreEncrypted)
    }

    @Test
    fun `starting the sign-in asks the screen for the account picker`() {
        val controller = controller()
        val picked = collect(controller.accountPickerEvent)

        controller.requestSignIn()

        assertEquals(1, picked.size)
        assertEquals("fake.account.chooser", picked.first().action)
    }

    @Test
    fun `a silently granted authorization links the account and starts the automatic backup`() = runBlocking {
        val controller = controller()
        val resolutions = collect(controller.authorizationRequiredEvent)
        operations.authorizeOutcome = AuthorizationOutcome.Granted

        controller.onAccountPicked("me@example.com")

        assertEquals(listOf("me@example.com"), operations.selectedAccounts)
        assertEquals(1, operations.authorizeCount)
        assertTrue(resolutions.isEmpty())
        assertTrue(controller.uiState.value.isSignedIn)
        assertEquals("me@example.com", controller.uiState.value.accountEmail)
        assertTrue(prefs.backupEnabled.first())
    }

    @Test
    fun `an authorization that needs a resolution hands its pending intent to the screen`() {
        val controller = controller()
        val resolutions = collect(controller.authorizationRequiredEvent)
        val pendingIntent = pendingIntent()
        operations.authorizeOutcome = AuthorizationOutcome.ResolutionRequired(pendingIntent)

        controller.onAccountPicked("me@example.com")

        assertEquals(listOf(pendingIntent), resolutions)
        assertFalse(controller.uiState.value.isSignedIn)
        assertNull(controller.uiState.value.error)
    }

    @Test
    fun `a cancelled account picker changes nothing`() {
        val controller = controller()
        val picked = collect(controller.accountPickerEvent)

        controller.onAccountPicked(null)
        controller.onAccountPicked("")

        assertTrue(operations.selectedAccounts.isEmpty())
        assertEquals(0, operations.authorizeCount)
        assertTrue(picked.isEmpty())
        assertFalse(controller.uiState.value.isSignedIn)
        assertNull(controller.uiState.value.error)
    }

    @Test
    fun `a completed resolution links the account`() {
        signedIn()
        val controller = controller()
        operations.authorizeOutcome = AuthorizationOutcome.Granted

        controller.onAuthorizationResult(Intent("resolution.result"))

        assertEquals(1, operations.authorizationResultIntents.size)
        assertTrue(controller.uiState.value.isSignedIn)
    }

    @Test
    fun `a cancelled consent dialog reports no error, a real failure does`() {
        val controller = controller()

        operations.authorizeOutcome = AuthorizationOutcome.Failed(apiException(CommonStatusCodes.CANCELED))
        controller.onAuthorizationResult(null)
        assertNull(controller.uiState.value.error)

        operations.authorizeOutcome = AuthorizationOutcome.Failed(apiException(CommonStatusCodes.NETWORK_ERROR))
        controller.onAuthorizationResult(null)
        assertNotNull(controller.uiState.value.error)
    }

    @Test
    fun `a revoked scope during an operation re-opens the consent dialog`() {
        signedIn()
        val pendingIntent = pendingIntent()
        operations.listResult = Result.failure(DriveAuthorizationRequiredException(pendingIntent))
        val controller = controller()
        val resolutions = collect(controller.authorizationRequiredEvent)

        controller.loadBackups()

        assertEquals(listOf(pendingIntent), resolutions)
        assertFalse(controller.uiState.value.isLoading)
        assertNotNull(controller.uiState.value.error)
    }

    private fun <T> collect(events: SharedFlow<T>): List<T> {
        val received = mutableListOf<T>()
        val scope = CoroutineScope(Dispatchers.Unconfined)
        scopes += scope
        scope.launch { events.collect { received += it } }
        return received
    }

    private fun pendingIntent(): PendingIntent = PendingIntent.getActivity(
        ApplicationProvider.getApplicationContext(),
        0,
        Intent("resolution"),
        PendingIntent.FLAG_IMMUTABLE
    )

    private fun apiException(statusCode: Int) = ApiException(Status(statusCode))

    private companion object {
        var prefsCounter = 0
    }
}
