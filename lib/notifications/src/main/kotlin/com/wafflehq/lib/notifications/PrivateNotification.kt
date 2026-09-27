package com.wafflehq.lib.notifications

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat

fun NotificationCompat.Builder.hideContentOnLockScreen(
    context: Context,
    channelId: String,
    @DrawableRes smallIcon: Int,
    publicTitle: String,
    publicText: String
): NotificationCompat.Builder {
    setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
    setPublicVersion(
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(publicTitle)
            .setContentText(publicText)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    )
    return this
}
