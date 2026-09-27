package com.wafflehq.lib.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.getSystemService

data class NotificationChannelSpec(
    val id: String,
    @param:StringRes val nameRes: Int,
    @param:StringRes val descriptionRes: Int,
    val importance: Int,
    val showBadge: Boolean = true,
    val silent: Boolean = false,
    val publicOnLockScreen: Boolean = false
)

object NotificationChannels {

    fun create(
        context: Context,
        specs: List<NotificationChannelSpec>,
        obsoleteChannelIds: List<String> = emptyList()
    ) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        obsoleteChannelIds.forEach { id -> manager.deleteNotificationChannel(id) }
        manager.createNotificationChannels(specs.map { spec -> spec.toChannel(context) })
    }

    private fun NotificationChannelSpec.toChannel(context: Context): NotificationChannel =
        NotificationChannel(id, context.getString(nameRes), importance).apply {
            description = context.getString(descriptionRes)
            if (!showBadge) setShowBadge(false)
            if (silent) {
                setSound(null, null)
                enableVibration(false)
                enableLights(false)
            }
            if (publicOnLockScreen) {
                @Suppress("DEPRECATION")
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        }
}
