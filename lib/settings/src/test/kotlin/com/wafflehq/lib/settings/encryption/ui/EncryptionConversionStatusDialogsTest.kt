package com.wafflehq.lib.settings.encryption.ui

import com.wafflehq.lib.uicore.R as UiCoreR
import android.app.Application
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.database.conversion.ConversionFailureReason
import com.wafflehq.lib.database.state.ConversionUiState
import com.wafflehq.lib.database.state.EncryptionConversionBus
import com.wafflehq.lib.settings.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EncryptionConversionStatusDialogsTest {

    @get:Rule
    val rule = createComposeRule()

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private fun str(id: Int) = context.getString(id)

    @After
    fun tearDown() {
        EncryptionConversionBus.update(ConversionUiState.Idle)
    }

    @Test
    fun `idle shows no dialog at all`() {
        rule.setContent { EncryptionConversionStatusDialogs(ConversionUiState.Idle, onRestartNow = {}) }

        rule.onNodeWithText(str(R.string.appsettings_encryption_success_title)).assertDoesNotExist()
        rule.onNodeWithText(str(R.string.appsettings_encryption_error_title)).assertDoesNotExist()
    }

    @Test
    fun `the success dialog hands the restart back to the host`() {
        var restarts = 0
        rule.setContent {
            EncryptionConversionStatusDialogs(ConversionUiState.SuccessAwaitingRestart, onRestartNow = { restarts++ })
        }

        rule.onNodeWithText(str(UiCoreR.string.uicore_ok)).performClick()

        assertEquals(1, restarts)
    }

    @Test
    fun `a verification failure explains the verification step`() {
        rule.setContent {
            EncryptionConversionStatusDialogs(
                ConversionUiState.Error(ConversionFailureReason.INTEGRITY_CHECK_FAILED),
                onRestartNow = {}
            )
        }

        rule.onNodeWithText(str(R.string.appsettings_encryption_error_verification_message)).assertExists()
    }

    @Test
    fun `any other failure explains the export step`() {
        rule.setContent {
            EncryptionConversionStatusDialogs(
                ConversionUiState.Error(ConversionFailureReason.EXPORT_ERROR),
                onRestartNow = {}
            )
        }

        rule.onNodeWithText(str(R.string.appsettings_encryption_error_export_message)).assertExists()
    }

    @Test
    fun `confirming an error resets the conversion bus`() {
        EncryptionConversionBus.update(ConversionUiState.Error(ConversionFailureReason.EXPORT_ERROR))
        rule.setContent {
            EncryptionConversionStatusDialogs(
                ConversionUiState.Error(ConversionFailureReason.EXPORT_ERROR),
                onRestartNow = {}
            )
        }

        rule.onNodeWithText(str(R.string.appsettings_confirm)).performClick()

        assertEquals(ConversionUiState.Idle, EncryptionConversionBus.state.value)
    }
}
