package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.uicore.R
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.theme.AppSpacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun formatDraftTimestamp(epochMs: Long): String {
    val dateTime = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.getDefault())
    return dateTime.format(formatter)
}

@Composable
fun IncompleteDraftBanner(
    message: String,
    onResume: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppBanner(
        text = message,
        modifier = modifier,
        role = AppBannerRole.Secondary,
        icon = Icons.Default.History,
        onClick = onResume,
        contentPadding = PaddingValues(start = AppSpacing.lg, top = AppSpacing.sm, bottom = AppSpacing.sm, end = AppSpacing.xs),
        action = {
            AppIconButton(
                icon = Icons.Default.Close,
                contentDescription = stringResource(R.string.uicore_draft_banner_discard),
                containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onDiscard,
            )
        },
    )
}
