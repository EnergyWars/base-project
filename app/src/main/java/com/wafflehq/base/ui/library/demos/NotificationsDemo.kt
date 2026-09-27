package com.wafflehq.base.ui.library.demos

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationCompat
import com.wafflehq.base.R
import com.wafflehq.lib.notifications.NotificationChannelSpec
import com.wafflehq.lib.notifications.NotificationChannels
import com.wafflehq.lib.notifications.hideContentOnLockScreen
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppLabelChip
import com.wafflehq.lib.uicore.theme.AppSpacing

internal object NotificationsDemoLogic {

    const val CHANNEL_PREFIX = "libex_"
    const val CHANNEL_REMINDERS = "libex_reminders"
    const val CHANNEL_SILENT = "libex_silent"
    const val CHANNEL_LOCKSCREEN = "libex_lockscreen"
    const val CHANNEL_OBSOLETE = "libex_obsolete"

    fun specs(): List<NotificationChannelSpec> = listOf(
        NotificationChannelSpec(
            id = CHANNEL_REMINDERS,
            nameRes = R.string.libex_notifications_reminders,
            descriptionRes = R.string.libex_notifications_reminders_desc,
            importance = NotificationManager.IMPORTANCE_HIGH,
        ),
        NotificationChannelSpec(
            id = CHANNEL_SILENT,
            nameRes = R.string.libex_notifications_silent,
            descriptionRes = R.string.libex_notifications_silent_desc,
            importance = NotificationManager.IMPORTANCE_LOW,
            showBadge = false,
            silent = true,
        ),
        NotificationChannelSpec(
            id = CHANNEL_LOCKSCREEN,
            nameRes = R.string.libex_notifications_lockscreen,
            descriptionRes = R.string.libex_notifications_lockscreen_desc,
            importance = NotificationManager.IMPORTANCE_DEFAULT,
            publicOnLockScreen = true,
        ),
    )

    @StringRes
    fun importanceLabel(importance: Int): Int = when (importance) {
        NotificationManager.IMPORTANCE_HIGH -> R.string.libex_notifications_importance_high
        NotificationManager.IMPORTANCE_LOW -> R.string.libex_notifications_importance_low
        else -> R.string.libex_notifications_importance_default
    }

    fun createChannels(context: Context) {
        NotificationChannels.create(context, specs(), obsoleteChannelIds = listOf(CHANNEL_OBSOLETE))
    }

    fun deleteChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        specs().forEach { manager.deleteNotificationChannel(it.id) }
    }

    fun registeredChannelCount(context: Context): Int {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return 0
        return manager.notificationChannels.count { it.id.startsWith(CHANNEL_PREFIX) }
    }

    fun buildPrivateNotification(
        context: Context,
        title: String,
        text: String,
        publicTitle: String,
        publicText: String,
    ): Notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(text)
        .hideContentOnLockScreen(
            context = context,
            channelId = CHANNEL_REMINDERS,
            smallIcon = android.R.drawable.ic_dialog_info,
            publicTitle = publicTitle,
            publicText = publicText,
        )
        .build()

    fun publicTitleOf(notification: Notification): String? =
        notification.publicVersion?.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
}

internal object NotificationsTags {
    const val CREATE = "libex_notifications_create"
    const val DELETE = "libex_notifications_delete"
    const val COUNT = "libex_notifications_count"
    const val PRIVATE = "libex_notifications_private_result"
    fun channel(id: String) = "libex_notifications_channel_$id"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NotificationsDemo() {
    val context = LocalContext.current
    val specs = remember { NotificationsDemoLogic.specs() }
    var registered by remember { mutableIntStateOf(-1) }
    val privateTitle = stringResource(R.string.libex_notifications_private_title)
    val privateText = stringResource(R.string.libex_notifications_private_text)
    val publicTitle = stringResource(R.string.libex_notifications_public_title)
    val publicText = stringResource(R.string.libex_notifications_public_text)
    val notification = remember(privateTitle, privateText, publicTitle, publicText) {
        NotificationsDemoLogic.buildPrivateNotification(context, privateTitle, privateText, publicTitle, publicText)
    }
    val yes = stringResource(R.string.libex_value_yes)
    val no = stringResource(R.string.libex_value_no)

    DemoSection(
        id = "notifications",
        titleRes = R.string.libex_notifications_title,
        descriptionRes = R.string.libex_notifications_desc,
        moduleRes = R.string.libex_module_notifications,
    ) {
        specs.forEach { spec ->
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                modifier = Modifier.testTag(NotificationsTags.channel(spec.id)),
            ) {
                Text(
                    text = stringResource(spec.nameRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                DemoMetaText(stringResource(spec.descriptionRes))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    AppLabelChip(text = stringResource(NotificationsDemoLogic.importanceLabel(spec.importance)))
                    if (!spec.showBadge) AppLabelChip(text = stringResource(R.string.libex_notifications_no_badge))
                    if (spec.silent) AppLabelChip(text = stringResource(R.string.libex_notifications_silent_flag))
                    if (spec.publicOnLockScreen) AppLabelChip(text = stringResource(R.string.libex_notifications_public_flag))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            AppButton(
                text = stringResource(R.string.libex_notifications_create),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Tonal,
                onClick = {
                    NotificationsDemoLogic.createChannels(context)
                    registered = NotificationsDemoLogic.registeredChannelCount(context)
                },
                modifier = Modifier.testTag(NotificationsTags.CREATE),
            )
            AppButton(
                text = stringResource(R.string.libex_notifications_delete),
                role = AppButtonRole.Error,
                variant = AppButtonVariant.Text,
                onClick = {
                    NotificationsDemoLogic.deleteChannels(context)
                    registered = NotificationsDemoLogic.registeredChannelCount(context)
                },
                modifier = Modifier.testTag(NotificationsTags.DELETE),
            )
        }
        if (registered >= 0) {
            DemoBodyText(
                text = stringResource(R.string.libex_notifications_count, registered),
                modifier = Modifier.testTag(NotificationsTags.COUNT),
            )
        }
        AppHorizontalDivider()
        DemoLabelText(stringResource(R.string.libex_notifications_private_heading))
        DemoBodyText(
            text = stringResource(
                R.string.libex_notifications_private_result,
                if (notification.visibility == NotificationCompat.VISIBILITY_PRIVATE) yes else no,
                NotificationsDemoLogic.publicTitleOf(notification).orEmpty(),
            ),
            modifier = Modifier.testTag(NotificationsTags.PRIVATE),
        )
    }
}
