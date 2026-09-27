package com.wafflehq.lib.settings.google

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.core.SettingsText
import com.wafflehq.lib.settings.core.resolveSettingsText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class AuthorizationErrorMapperTest {

    private val context: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun `a known status code becomes its reason plus the code`() {
        val error = ApiException(Status(CommonStatusCodes.TIMEOUT)).toAuthorizationError()

        assertEquals(
            SettingsText.Formatted(
                R.string.appsettings_google_signin_error_with_code,
                listOf(SettingsText.Label(R.string.appsettings_google_signin_error_timeout), CommonStatusCodes.TIMEOUT)
            ),
            error.text
        )
        assertFalse(error.isUserCancelled)
    }

    @Test
    fun `an unmapped status code falls back to the unknown reason`() {
        val error = ApiException(Status(9999)).toAuthorizationError()

        assertEquals(
            SettingsText.Formatted(
                R.string.appsettings_google_signin_error_with_code,
                listOf(SettingsText.Label(R.string.appsettings_google_signin_error_unknown), 9999)
            ),
            error.text
        )
    }

    @Test
    fun `a developer error gets its own detailed explanation`() {
        val error = ApiException(Status(CommonStatusCodes.DEVELOPER_ERROR)).toAuthorizationError()

        assertEquals(
            SettingsText.Formatted(
                R.string.appsettings_google_signin_error_developer_detailed,
                listOf(CommonStatusCodes.DEVELOPER_ERROR)
            ),
            error.text
        )
    }

    @Test
    fun `a cancelled dialog is flagged so the UI can stay silent`() {
        val error = ApiException(Status(CommonStatusCodes.CANCELED)).toAuthorizationError()

        assertTrue(error.isUserCancelled)
    }

    @Test
    fun `any other failure keeps its own message`() {
        val error = IllegalStateException("no token").toAuthorizationError()

        assertEquals(SettingsText.Literal("no token"), error.text)
        assertFalse(error.isUserCancelled)
    }

    @Test
    fun `a failure without a message falls back to the unknown string`() {
        val error = IllegalStateException().toAuthorizationError()

        assertEquals(SettingsText.Label(R.string.appsettings_google_signin_error_unknown), error.text)
    }

    @Test
    fun `the nested reason resolves into one readable sentence`() {
        val error = ApiException(Status(CommonStatusCodes.NETWORK_ERROR)).toAuthorizationError()

        assertEquals(
            context.getString(
                R.string.appsettings_google_signin_error_with_code,
                context.getString(R.string.appsettings_google_signin_error_network),
                CommonStatusCodes.NETWORK_ERROR
            ),
            context.resolveSettingsText(error.text)
        )
    }
}
