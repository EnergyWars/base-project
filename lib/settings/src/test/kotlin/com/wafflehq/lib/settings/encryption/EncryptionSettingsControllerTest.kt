package com.wafflehq.lib.settings.encryption

import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.database.BackupPasswordVerifier
import com.wafflehq.lib.database.crypto.PasswordKeyWrapper
import com.wafflehq.lib.database.state.ConversionUiState
import com.wafflehq.lib.database.state.EncryptionConversionBus
import com.wafflehq.lib.database.state.EncryptionStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class EncryptionSettingsControllerTest {

    private lateinit var stateStore: EncryptionStateStore
    private lateinit var keyWrapper: FakeKeystoreKeyWrapper
    private lateinit var passwordWrapper: PasswordKeyWrapper
    private lateinit var verifier: BackupPasswordVerifier
    private lateinit var gateway: FakeEncryptionBackupGateway
    private lateinit var launcher: RecordingDatabaseConversionLauncher
    private val scopes = mutableListOf<CoroutineScope>()

    @Before
    fun setUp() {
        prefsCounter++
        stateStore = EncryptionStateStore(
            ApplicationProvider.getApplicationContext(),
            prefsName = "encryption_state_test_$prefsCounter"
        )
        keyWrapper = FakeKeystoreKeyWrapper()
        passwordWrapper = PasswordKeyWrapper()
        verifier = BackupPasswordVerifier(stateStore, passwordWrapper)
        gateway = FakeEncryptionBackupGateway()
        launcher = RecordingDatabaseConversionLauncher()
        EncryptionConversionBus.update(ConversionUiState.Idle)
    }

    @After
    fun tearDown() {
        scopes.forEach { it.cancel() }
        scopes.clear()
        EncryptionConversionBus.update(ConversionUiState.Idle)
    }

    private fun controller(): EncryptionSettingsController {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scopes += scope
        return EncryptionSettingsController(
            stateStore,
            keyWrapper,
            passwordWrapper,
            verifier,
            gateway,
            launcher,
            Dispatchers.Default,
            scope
        )
    }

    private fun markEncrypted(dek: ByteArray = DEK) {
        stateStore.commitSwapIntent(newIsEncrypted = true, newWrappedDek = keyWrapper.wrap(dek))
        stateStore.commitSwapComplete()
    }

    @Test
    fun `reads the encryption state once at construction`() = runTest(timeout = TEST_TIMEOUT) {
        markEncrypted()
        val controller = controller()

        assertTrue(controller.isDatabaseEncrypted)
        assertFalse(controller.hasBackupPassword)
    }

    @Test
    fun `hasBackupPassword is true once a password wrap is stored`() = runTest(timeout = TEST_TIMEOUT) {
        markEncrypted()
        stateStore.writePasswordWrappedDek(passwordWrapper.wrap(DEK, PASSWORD.toCharArray()))

        assertTrue(controller().hasBackupPassword)
    }

    @Test
    fun `uiState mirrors the conversion bus`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()

        EncryptionConversionBus.update(ConversionUiState.InProgress)

        assertEquals(ConversionUiState.InProgress, controller.uiState.value)
    }

    @Test
    fun `startConversion on a plain database asks for encryption and forwards the password`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()

        controller.startConversion(PASSWORD.toCharArray())

        assertEquals(listOf(RecordingDatabaseConversionLauncher.Start(true, PASSWORD)), launcher.starts)
    }

    @Test
    fun `startConversion on an encrypted database asks for decryption without a password`() = runTest(timeout = TEST_TIMEOUT) {
        markEncrypted()
        val controller = controller()

        controller.startConversion()

        assertEquals(listOf(RecordingDatabaseConversionLauncher.Start(false, null)), launcher.starts)
    }

    @Test
    fun `changeBackupPassword stores a wrap the verifier then accepts`() = runTest(timeout = TEST_TIMEOUT) {
        markEncrypted()
        val controller = controller()

        controller.changeBackupPassword(PASSWORD)

        assertEquals(ChangePasswordResult.Success, controller.changePasswordState.first { it == ChangePasswordResult.Success })
        assertNotNull(stateStore.readPasswordWrappedDek())
        controller.testBackupPassword(PASSWORD)
        assertEquals(PasswordTestResult.Correct, controller.passwordTestResult.first { it == PasswordTestResult.Correct })
    }

    @Test
    fun `changeBackupPassword reports an error when no wrapped dek exists`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()

        controller.changeBackupPassword(PASSWORD)

        assertEquals(ChangePasswordResult.Error, controller.changePasswordState.first { it == ChangePasswordResult.Error })
        assertNull(stateStore.readPasswordWrappedDek())
    }

    @Test
    fun `changeBackupPassword reports an error when the keystore is unavailable`() = runTest(timeout = TEST_TIMEOUT) {
        markEncrypted()
        val controller = controller()
        keyWrapper.failing = true

        controller.changeBackupPassword(PASSWORD)

        assertEquals(ChangePasswordResult.Error, controller.changePasswordState.first { it == ChangePasswordResult.Error })
        assertNull(stateStore.readPasswordWrappedDek())
    }

    @Test
    fun `dismissChangePasswordResult resets to idle`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        controller.changeBackupPassword(PASSWORD)
        controller.changePasswordState.first { it == ChangePasswordResult.Error }

        controller.dismissChangePasswordResult()

        assertEquals(ChangePasswordResult.Idle, controller.changePasswordState.value)
    }

    @Test
    fun `testBackupPassword reports incorrect when no password wrap is stored`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()

        controller.testBackupPassword(PASSWORD)

        assertEquals(PasswordTestResult.Incorrect, controller.passwordTestResult.first { it == PasswordTestResult.Incorrect })
    }

    @Test
    fun `dismissPasswordTestResult resets to idle`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        controller.testBackupPassword(PASSWORD)
        controller.passwordTestResult.first { it == PasswordTestResult.Incorrect }

        controller.dismissPasswordTestResult()

        assertEquals(PasswordTestResult.Idle, controller.passwordTestResult.value)
    }

    @Test
    fun `hasBackupOnlyEncryption starts from what the gateway reports`() = runTest(timeout = TEST_TIMEOUT) {
        gateway = FakeEncryptionBackupGateway(backupOnlyEncryptionEnabled = true)

        assertTrue(controller().hasBackupOnlyEncryption.value)
    }

    @Test
    fun `enableBackupOnlyEncryption forwards the password and flips the flag`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()

        controller.enableBackupOnlyEncryption(PASSWORD)

        assertEquals(
            BackupOnlyEncryptionResult.Success,
            controller.backupOnlyEncryptionState.first { it == BackupOnlyEncryptionResult.Success }
        )
        assertTrue(controller.hasBackupOnlyEncryption.value)
        assertEquals(listOf(PASSWORD), gateway.enabledPasswords)
    }

    @Test
    fun `enableBackupOnlyEncryption failure keeps the flag off and reports an error`() = runTest(timeout = TEST_TIMEOUT) {
        gateway.enableSucceeds = false
        val controller = controller()

        controller.enableBackupOnlyEncryption(PASSWORD)

        assertEquals(
            BackupOnlyEncryptionResult.Error,
            controller.backupOnlyEncryptionState.first { it == BackupOnlyEncryptionResult.Error }
        )
        assertFalse(controller.hasBackupOnlyEncryption.value)
    }

    @Test
    fun `dismissBackupOnlyEncryptionResult resets to idle`() = runTest(timeout = TEST_TIMEOUT) {
        gateway.enableSucceeds = false
        val controller = controller()
        controller.enableBackupOnlyEncryption(PASSWORD)
        controller.backupOnlyEncryptionState.first { it == BackupOnlyEncryptionResult.Error }

        controller.dismissBackupOnlyEncryptionResult()

        assertEquals(BackupOnlyEncryptionResult.Idle, controller.backupOnlyEncryptionState.value)
    }

    @Test
    fun `disableBackupOnlyEncryption clears the flag`() = runTest(timeout = TEST_TIMEOUT) {
        gateway = FakeEncryptionBackupGateway(backupOnlyEncryptionEnabled = true)
        val controller = controller()

        controller.disableBackupOnlyEncryption()

        assertFalse(controller.hasBackupOnlyEncryption.first { !it })
        assertEquals(1, gateway.disableCount)
    }

    @Test
    fun `replaceAllBackupsWithFreshOne reports success`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()

        controller.replaceAllBackupsWithFreshOne()

        assertEquals(BackupReplaceResult.Success, controller.backupReplaceState.first { it == BackupReplaceResult.Success })
        assertEquals(1, gateway.replaceCount)
    }

    @Test
    fun `replaceAllBackupsWithFreshOne reports an error when the gateway fails`() = runTest(timeout = TEST_TIMEOUT) {
        gateway.replaceSucceeds = false
        val controller = controller()

        controller.replaceAllBackupsWithFreshOne()

        assertEquals(BackupReplaceResult.Error, controller.backupReplaceState.first { it == BackupReplaceResult.Error })
    }

    @Test
    fun `dismissBackupReplaceResult resets to idle`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        controller.replaceAllBackupsWithFreshOne()
        controller.backupReplaceState.first { it == BackupReplaceResult.Success }

        controller.dismissBackupReplaceResult()

        assertEquals(BackupReplaceResult.Idle, controller.backupReplaceState.value)
    }

    @Test
    fun `onPasswordReminderIntervalChanged persists the new interval and updates the flow`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()

        controller.onPasswordReminderIntervalChanged(PasswordReminderInterval.WEEKLY)

        assertEquals(
            PasswordReminderInterval.WEEKLY,
            controller.passwordReminderInterval.first { it == PasswordReminderInterval.WEEKLY }
        )
        assertEquals(PasswordReminderInterval.WEEKLY, gateway.storedInterval)
    }

    private companion object {
        val TEST_TIMEOUT = 60.seconds
        const val PASSWORD = "correct horse battery staple"
        val DEK = ByteArray(32) { it.toByte() }
        var prefsCounter = 0
    }
}
