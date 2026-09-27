package com.wafflehq.base.ui.library.demos

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NotificationsDemoTest : LibraryDemoTest() {

    private fun manager() = context.getSystemService(NotificationManager::class.java)

    @Test
    fun listsEveryChannelSpecWithoutRegisteringThem() {
        show { NotificationsDemo() }

        NotificationsDemoLogic.specs().forEach { node(NotificationsTags.channel(it.id)).assertExists() }
        assertEquals(0, NotificationsDemoLogic.registeredChannelCount(context))
        assertTagCount(NotificationsTags.COUNT, 0)
    }

    @Test
    fun createAndDeleteButtonsManageTheDemoChannels() {
        show { NotificationsDemo() }

        click(NotificationsTags.CREATE)
        waitForTagText(NotificationsTags.COUNT, string(R.string.libex_notifications_count, 3))
        assertEquals(3, NotificationsDemoLogic.registeredChannelCount(context))

        click(NotificationsTags.DELETE)
        waitForTagText(NotificationsTags.COUNT, string(R.string.libex_notifications_count, 0))
        assertEquals(0, NotificationsDemoLogic.registeredChannelCount(context))
    }

    @Test
    fun privateNotificationHidesItsContentOnTheLockScreen() {
        show { NotificationsDemo() }

        assertTagText(
            NotificationsTags.PRIVATE,
            string(R.string.libex_notifications_private_result, string(R.string.libex_value_yes), string(R.string.libex_notifications_public_title)),
        )
    }

    @Test
    fun createChannelsAppliesTheSpecFlags() {
        NotificationsDemoLogic.createChannels(context)

        val silent = manager().getNotificationChannel(NotificationsDemoLogic.CHANNEL_SILENT)
        val reminders = manager().getNotificationChannel(NotificationsDemoLogic.CHANNEL_REMINDERS)
        assertNotNull(silent)
        assertEquals(NotificationManager.IMPORTANCE_LOW, silent.importance)
        assertFalse(silent.canShowBadge())
        assertEquals(NotificationManager.IMPORTANCE_HIGH, reminders.importance)
        assertTrue(reminders.canShowBadge())
        NotificationsDemoLogic.deleteChannels(context)
    }

    @Test
    fun buildPrivateNotificationAddsAPublicVersion() {
        val notification = NotificationsDemoLogic.buildPrivateNotification(context, "Secret", "Body", "Public", "Hidden")

        assertEquals(Notification.VISIBILITY_PRIVATE, notification.visibility)
        assertEquals("Public", NotificationsDemoLogic.publicTitleOf(notification))
    }

    @Test
    fun importanceLabelsDiffer() {
        val labels = listOf(
            NotificationManager.IMPORTANCE_HIGH,
            NotificationManager.IMPORTANCE_DEFAULT,
            NotificationManager.IMPORTANCE_LOW,
        ).map { NotificationsDemoLogic.importanceLabel(it) }

        assertEquals(3, labels.toSet().size)
    }
}
