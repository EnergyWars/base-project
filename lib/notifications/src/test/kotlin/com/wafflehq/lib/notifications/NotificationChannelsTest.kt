package com.wafflehq.lib.notifications

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class NotificationChannelsTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val manager = context.getSystemService(NotificationManager::class.java)

    private fun spec(
        id: String = "channel_test",
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
        showBadge: Boolean = true,
        silent: Boolean = false,
        publicOnLockScreen: Boolean = false
    ) = NotificationChannelSpec(
        id = id,
        nameRes = android.R.string.copy,
        descriptionRes = android.R.string.cancel,
        importance = importance,
        showBadge = showBadge,
        silent = silent,
        publicOnLockScreen = publicOnLockScreen
    )

    @Test
    fun `a channel is created with name description and importance`() {
        NotificationChannels.create(context, listOf(spec(importance = NotificationManager.IMPORTANCE_HIGH)))

        val channel = manager.getNotificationChannel("channel_test")
        assertNotNull(channel)
        assertEquals(context.getString(android.R.string.copy), channel.name.toString())
        assertEquals(context.getString(android.R.string.cancel), channel.description)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
    }

    @Test
    fun `by default a channel keeps its sound and shows a badge`() {
        NotificationChannels.create(context, listOf(spec()))

        val channel = manager.getNotificationChannel("channel_test")
        assertNotNull(channel.sound)
        assertTrue(channel.canShowBadge())
    }

    @Test
    fun `a silent channel has no sound no vibration and no lights`() {
        NotificationChannels.create(context, listOf(spec(silent = true)))

        val channel = manager.getNotificationChannel("channel_test")
        assertNull(channel.sound)
        assertFalse(channel.shouldVibrate())
        assertFalse(channel.shouldShowLights())
    }

    @Test
    fun `a channel without a badge does not show one`() {
        NotificationChannels.create(context, listOf(spec(showBadge = false)))

        assertFalse(manager.getNotificationChannel("channel_test").canShowBadge())
    }

    @Test
    fun `a public channel stays visible on the lock screen`() {
        NotificationChannels.create(context, listOf(spec(publicOnLockScreen = true)))

        assertEquals(
            Notification.VISIBILITY_PUBLIC,
            manager.getNotificationChannel("channel_test").lockscreenVisibility
        )
    }

    @Test
    fun `without the flag the lock screen visibility is left alone`() {
        NotificationChannels.create(context, listOf(spec()))

        val channel = manager.getNotificationChannel("channel_test")
        assertTrue(channel.lockscreenVisibility != Notification.VISIBILITY_PUBLIC)
    }

    @Test
    fun `obsolete channels are deleted before the current ones are created`() {
        manager.createNotificationChannel(
            NotificationChannel("channel_old", "Old", NotificationManager.IMPORTANCE_DEFAULT)
        )

        NotificationChannels.create(context, listOf(spec()), obsoleteChannelIds = listOf("channel_old"))

        assertNull(manager.getNotificationChannel("channel_old"))
        assertNotNull(manager.getNotificationChannel("channel_test"))
    }

    @Test
    fun `several specs are created in one call`() {
        NotificationChannels.create(
            context,
            listOf(spec(id = "channel_a"), spec(id = "channel_b"), spec(id = "channel_c"))
        )

        listOf("channel_a", "channel_b", "channel_c").forEach { id ->
            assertNotNull(manager.getNotificationChannel(id))
        }
    }

    @Test
    fun `an empty spec list deletes the obsolete channels anyway`() {
        manager.createNotificationChannel(
            NotificationChannel("channel_old", "Old", NotificationManager.IMPORTANCE_DEFAULT)
        )

        NotificationChannels.create(context, emptyList(), obsoleteChannelIds = listOf("channel_old"))

        assertNull(manager.getNotificationChannel("channel_old"))
    }
}
