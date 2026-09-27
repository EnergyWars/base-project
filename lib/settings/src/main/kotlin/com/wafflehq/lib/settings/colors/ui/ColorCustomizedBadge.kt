package com.wafflehq.lib.settings.colors.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.uicore.components.AppLabelChip

@Composable
internal fun ColorCustomizedBadge() {
    AppLabelChip(
        text = stringResource(R.string.appsettings_color_token_status_customized),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        textStyle = MaterialTheme.typography.labelSmall,
        leadingIcon = Icons.Filled.Edit,
    )
}
