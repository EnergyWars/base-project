package com.wafflehq.lib.settings.backup

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.settings.backup.access.AuthorizationOutcome
import com.wafflehq.lib.settings.google.GoogleAuthorization
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class DriveBackupOperationsTest {

    private val repository = mock<DriveBackupRepository> {
        on { accountEmail } doReturn flowOf("me@example.com")
    }
    private val operations = DriveBackupOperations(repository)

    @Test
    fun `a result without a resolution counts as granted and is remembered`() = runBlocking {
        whenever(repository.authorize()).thenReturn(GoogleAuthorization.Granted("token-123"))

        assertEquals(AuthorizationOutcome.Granted, operations.authorize())
        verifyBlocking(repository) { onAuthorized() }
    }

    @Test
    fun `a result with a resolution is handed on untouched`() = runBlocking {
        val pendingIntent = pendingIntent()
        whenever(repository.authorize()).thenReturn(GoogleAuthorization.ResolutionRequired(pendingIntent))

        assertEquals(AuthorizationOutcome.ResolutionRequired(pendingIntent), operations.authorize())
        verifyBlocking(repository, never()) { onAuthorized() }
    }

    @Test
    fun `a failing authorization is reported with its cause`() = runBlocking {
        val cause = IllegalStateException("no play services")
        whenever(repository.authorize()).thenThrow(cause)

        assertEquals(AuthorizationOutcome.Failed(cause), operations.authorize())
    }

    @Test
    fun `the consent dialog's result is mapped the same way`() = runBlocking {
        val data = Intent("resolution.result")
        whenever(repository.resultFromIntent(data)).thenReturn(Result.success(GoogleAuthorization.Granted("token-123")))

        assertEquals(AuthorizationOutcome.Granted, operations.onAuthorizationResult(data))
    }

    @Test
    fun `a cancelled consent dialog is reported with its cause`() = runBlocking {
        val cause = IllegalStateException("cancelled")
        whenever(repository.resultFromIntent(null)).thenReturn(Result.failure(cause))

        assertEquals(AuthorizationOutcome.Failed(cause), operations.onAuthorizationResult(null))
    }

    private fun pendingIntent(): PendingIntent = PendingIntent.getActivity(
        ApplicationProvider.getApplicationContext(),
        0,
        Intent("resolution"),
        PendingIntent.FLAG_IMMUTABLE
    )
}
