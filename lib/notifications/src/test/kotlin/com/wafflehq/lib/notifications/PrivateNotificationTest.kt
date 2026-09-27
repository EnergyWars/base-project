package com.wafflehq.lib.notifications

import android.app.Application
import android.app.Notification
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class PrivateNotificationTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private fun notification(): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Sensitive title")
            .setContentText("Sensitive text")
            .hideContentOnLockScreen(
                context = context,
                channelId = CHANNEL_ID,
                smallIcon = android.R.drawable.ic_dialog_alert,
                publicTitle = "App name",
                publicText = "Hidden"
            )
            .build()

    @Test
    fun `the notification itself becomes private`() {
        assertEquals(NotificationCompat.VISIBILITY_PRIVATE, notification().visibility)
    }

    @Test
    fun `a public version replaces title and text on the lock screen`() {
        val public = notification().publicVersion

        assertNotNull(public)
        assertEquals("App name", public.extras.getCharSequence(Notification.EXTRA_TITLE).toString())
        assertEquals("Hidden", public.extras.getCharSequence(Notification.EXTRA_TEXT).toString())
        assertEquals(Notification.VISIBILITY_PUBLIC, public.visibility)
    }

    @Test
    fun `the sensitive content stays on the notification itself`() {
        val built = notification()

        assertEquals("Sensitive title", built.extras.getCharSequence(Notification.EXTRA_TITLE).toString())
        assertEquals("Sensitive text", built.extras.getCharSequence(Notification.EXTRA_TEXT).toString())
    }

    @Test
    fun `the builder is returned so calls can be chained`() {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        val returned = builder.hideContentOnLockScreen(
            context = context,
            channelId = CHANNEL_ID,
            smallIcon = android.R.drawable.ic_dialog_alert,
            publicTitle = "App name",
            publicText = "Hidden"
        )

        assertEquals(builder, returned)
    }

    private companion object {
        const val CHANNEL_ID = "channel_test"
    }
}
