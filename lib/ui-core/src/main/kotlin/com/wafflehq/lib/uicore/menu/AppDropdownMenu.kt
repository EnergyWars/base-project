package com.wafflehq.lib.uicore.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.testing.exposeTestTagsInDebugBuilds

val AppMenuBorderWidth: Dp = 1.dp
val AppMenuTonalElevation: Dp = 6.dp
val AppMenuShadowElevation: Dp = 4.dp

@Composable
fun AppDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.exposeTestTagsInDebugBuilds(),
        offset = offset,
        scrollState = scrollState,
        properties = properties,
        shape = RoundedCornerShape(AppRadius.card),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = AppMenuTonalElevation,
        shadowElevation = AppMenuShadowElevation,
        border = BorderStroke(AppMenuBorderWidth, MaterialTheme.colorScheme.outline),
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenuBoxScope.AppExposedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    matchAnchorWidth: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.exposeTestTagsInDebugBuilds(),
        scrollState = scrollState,
        matchAnchorWidth = matchAnchorWidth,
        shape = RoundedCornerShape(AppRadius.card),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = AppMenuTonalElevation,
        shadowElevation = AppMenuShadowElevation,
        border = BorderStroke(AppMenuBorderWidth, MaterialTheme.colorScheme.outline),
        content = content,
    )
}
