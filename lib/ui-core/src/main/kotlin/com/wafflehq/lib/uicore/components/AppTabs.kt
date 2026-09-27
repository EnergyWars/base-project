package com.wafflehq.lib.uicore.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    if (scrollable) {
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = modifier,
            containerColor = colors.surface,
            contentColor = colors.primary,
            tabs = content,
        )
    } else {
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = modifier,
            containerColor = colors.surface,
            contentColor = colors.primary,
            tabs = content,
        )
    }
}

@Composable
fun AppTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        text = text,
        icon = icon,
        selectedContentColor = colors.primary,
        unselectedContentColor = colors.onSurfaceVariant,
    )
}
