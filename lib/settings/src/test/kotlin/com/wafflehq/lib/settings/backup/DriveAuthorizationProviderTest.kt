package com.wafflehq.lib.settings.backup

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.settings.google.FakeGoogleAuthorizationGateway
import com.wafflehq.lib.settings.google.GoogleAuthorization
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class DriveAuthorizationProviderTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var prefs: BackupPreferenceStore
    private lateinit var gateway: FakeGoogleAuthorizationGateway
    private var legacyEmail: String? = null

    @Before
    fun setUp() {
        prefs = BackupPreferenceStore(FakePreferencesDataStore())
        gateway = FakeGoogleAuthorizationGateway()
        legacyEmail = null
    }

    private fun provider() = DriveAuthorizationProvider(
        backupPrefs = prefs,
        driveApplicationName = "TestApp",
        gateway = gateway,
        legacyAccountEmail = { legacyEmail }
    )

    private fun pendingIntent(): PendingIntent =
        PendingIntent.getActivity(context, 0, Intent("resolution"), PendingIntent.FLAG_IMMUTABLE)

    @Test
    fun `the account picker asks for Google accounts`() {
        val intent = provider().accountChooserIntent()

        val types = intent.getStringArrayExtra("allowableAccountTypes")
        assertEquals(listOf("com.google"), types?.toList())
    }

    @Test
    fun `a picked account is remembered and used for every request`() = runBlocking {
        val provider = provider()

        provider.selectAccount("someone@example.com")
        provider.authorize()

        assertEquals("someone@example.com", prefs.driveAccountEmail.first())
        assertEquals("someone@example.com", gateway.authorizedAccounts.single()?.name)
        assertEquals("com.google", gateway.authorizedAccounts.single()?.type)
    }

    @Test
    fun `without a linked account the request carries none`() = runBlocking {
        provider().authorize()

        assertNull(gateway.authorizedAccounts.single())
    }

    @Test
    fun `a granted scope needs no resolution and yields a Drive client`() = runBlocking {
        val provider = provider()
        provider.selectAccount("someone@example.com")

        assertTrue(provider.hasRequiredScopes())
        assertNotNull(provider.get())
    }

    @Test
    fun `a missing scope is reported as a resolution instead of a Drive client`() = runBlocking {
        val resolution = pendingIntent()
        gateway.outcome = GoogleAuthorization.ResolutionRequired(resolution)
        val provider = provider()
        provider.selectAccount("someone@example.com")

        assertFalse(provider.hasRequiredScopes())
        val thrown = assertThrows(DriveAuthorizationRequiredException::class.java) {
            runBlocking { provider.get() }
        }
        assertEquals(resolution, thrown.pendingIntent)
    }

    @Test
    fun `a failing authorization counts as missing scope instead of crashing`() = runBlocking {
        gateway.failure = IllegalStateException("no network")

        assertFalse(provider().hasRequiredScopes())
    }

    @Test
    fun `onAuthorized records the granted state`() = runBlocking {
        val provider = provider()
        assertFalse(prefs.driveAuthorized.first())

        provider.onAuthorized()

        assertTrue(prefs.driveAuthorized.first())
    }

    @Test
    fun `signing out revokes the access and clears both preferences`() = runBlocking {
        val provider = provider()
        provider.selectAccount("someone@example.com")
        provider.onAuthorized()

        provider.signOut()

        assertEquals("someone@example.com", gateway.revokedAccounts.single()?.name)
        assertEquals("", prefs.driveAccountEmail.first())
        assertFalse(prefs.driveAuthorized.first())
    }

    @Test
    fun `signing out clears the preferences even when revoking fails`() = runBlocking {
        val provider = provider()
        provider.selectAccount("someone@example.com")
        provider.onAuthorized()
        gateway.revokeFailure = IllegalStateException("offline")

        provider.signOut()

        assertEquals("", prefs.driveAccountEmail.first())
        assertFalse(prefs.driveAuthorized.first())
    }

    @Test
    fun `an account linked through the old GoogleSignIn flow is adopted once`() = runBlocking {
        legacyEmail = "legacy@example.com"
        val provider = provider()

        assertTrue(provider.isSignedIn())

        assertEquals("legacy@example.com", prefs.driveAccountEmail.first())
        assertEquals("legacy@example.com", gateway.authorizedAccounts.firstOrNull()?.name ?: "legacy@example.com")
    }

    @Test
    fun `an already linked account is never overwritten by the old one`() = runBlocking {
        legacyEmail = "legacy@example.com"
        val provider = provider()
        provider.selectAccount("current@example.com")

        provider.authorize()

        assertEquals("current@example.com", prefs.driveAccountEmail.first())
        assertEquals("current@example.com", gateway.authorizedAccounts.single()?.name)
    }

    @Test
    fun `without any previous account nothing is adopted`() = runBlocking {
        val provider = provider()

        assertFalse(provider.isSignedIn())
        assertEquals("", prefs.driveAccountEmail.first())
    }

    @Test
    fun `an old account without an email is not adopted`() = runBlocking {
        legacyEmail = ""

        assertFalse(provider().isSignedIn())
        assertEquals("", prefs.driveAccountEmail.first())
    }

    @Test
    fun `a dialog result is translated into an outcome`() = runBlocking {
        val resolution = pendingIntent()
        gateway.fromIntentOutcome = GoogleAuthorization.ResolutionRequired(resolution)

        val outcome = provider().resultFromIntent(Intent())

        assertEquals(GoogleAuthorization.ResolutionRequired(resolution), outcome.getOrNull())
    }

    @Test
    fun `a broken dialog result is reported as a failure`() {
        gateway.failure = IllegalStateException("bad intent")

        val outcome = provider().resultFromIntent(null)

        assertTrue(outcome.isFailure)
    }
}
