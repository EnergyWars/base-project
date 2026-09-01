package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Section19Icons() = Section(R.string.sc_s19_title, R.string.sc_s19_desc) {
    val icons = listOf(
        Icons.Outlined.Home, Icons.Outlined.CalendarMonth, Icons.Outlined.Schedule, Icons.Outlined.LocationOn,
        Icons.Outlined.Notifications, Icons.Outlined.Search, Icons.Outlined.LocalShipping, Icons.Outlined.ChatBubbleOutline,
        Icons.Outlined.Check, Icons.Outlined.Close, Icons.Outlined.DeleteOutline, Icons.Outlined.Edit,
        Icons.Outlined.CheckBox, Icons.Outlined.Settings, Icons.Outlined.ChevronLeft, Icons.Filled.Favorite,
    )
    Panel {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            maxItemsInEachRow = 8,
        ) {
            icons.forEachIndexed { index, icon ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(radiusS)
                        .background(AppTheme.colors.surfaceVariant)
                        .inspectId("19a.${index + 1}"),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null, tint = AppTheme.colors.onSurface, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}
