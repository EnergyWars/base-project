package com.wafflehq.lib.settings.encryption.ui

import com.wafflehq.lib.uicore.R as UiCoreR
import android.app.Application
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.encryption.PasswordTestResult
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
class PasswordTestDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private fun str(id: Int) = context.getString(id)

    private fun setContent(
        result: PasswordTestResult,
        onTest: (String) -> Unit = {},
        onResultDismissed: () -> Unit = {},
        onDismiss: () -> Unit = {}
    ) {
        rule.setContent {
            PasswordTestDialog(
                title = TITLE,
                message = MESSAGE,
                result = result,
                onTest = onTest,
                onResultDismissed = onResultDismissed,
                onDismiss = onDismiss
            )
        }
    }

    @Test
    fun `the check action stays disabled until a password is entered`() {
        setContent(PasswordTestResult.Idle)

        rule.onNodeWithText(str(R.string.appsettings_backup_password_test_action)).assertIsNotEnabled()

        rule.onNodeWithText(str(R.string.appsettings_backup_password_field_label)).performTextInput("secret")

        rule.onNodeWithText(str(R.string.appsettings_backup_password_test_action)).assertIsEnabled()
    }

    @Test
    fun `the entered password is handed to onTest unchanged`() {
        val tested = mutableListOf<String>()
        setContent(PasswordTestResult.Idle, onTest = { tested += it })

        rule.onNodeWithText(str(R.string.appsettings_backup_password_field_label)).performTextInput("correct horse")
        rule.onNodeWithText(str(R.string.appsettings_backup_password_test_action)).performClick()

        assertEquals(listOf("correct horse"), tested)
    }

    @Test
    fun `typing after a result clears it`() {
        var dismissedResults = 0
        setContent(PasswordTestResult.Incorrect, onResultDismissed = { dismissedResults++ })

        rule.onNodeWithText(str(R.string.appsettings_backup_password_field_label)).performTextInput("x")

        assertEquals(1, dismissedResults)
    }

    @Test
    fun `an incorrect result is shown`() {
        setContent(PasswordTestResult.Incorrect)

        rule.onNodeWithText(str(R.string.appsettings_backup_password_test_incorrect)).assertExists()
    }

    @Test
    fun `a correct result is shown`() {
        setContent(PasswordTestResult.Correct)

        rule.onNodeWithText(str(R.string.appsettings_backup_password_test_correct)).assertExists()
    }

    @Test
    fun `while checking neither the field nor the actions accept input`() {
        setContent(PasswordTestResult.Checking)

        rule.onNodeWithText(str(R.string.appsettings_backup_password_test_action)).assertIsNotEnabled()
        rule.onNodeWithText(str(UiCoreR.string.uicore_close)).assertIsNotEnabled()
    }

    @Test
    fun `close reports the dismissal`() {
        var dismissed = 0
        setContent(PasswordTestResult.Idle, onDismiss = { dismissed++ })

        rule.onNodeWithText(str(UiCoreR.string.uicore_close)).performClick()

        assertEquals(1, dismissed)
    }

    private companion object {
        const val TITLE = "Passwort testen"
        const val MESSAGE = "Gib dein Passwort ein."
    }
}
