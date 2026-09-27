package com.wafflehq.base.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.button.AppIconButtonVariant
import com.wafflehq.lib.uicore.scaffold.AppScaffold

enum class HeaderItem { Home, Settings, None }

@Composable
fun AppHeaderScaffold(
    title: String,
    activeItem: HeaderItem,
    onOpenMenu: () -> Unit,
    onNavigateHome: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    AppScaffold(
        title = title,
        onBack = onOpenMenu,
        modifier = modifier,
        backDescription = stringResource(R.string.cd_menu),
        navigationIcon = Icons.Outlined.Menu,
        actions = {
            HeaderAction(
                selected = activeItem == HeaderItem.Home,
                onClick = onNavigateHome,
                icon = Icons.Outlined.Home,
                description = stringResource(R.string.cd_home)
            )
            HeaderAction(
                selected = activeItem == HeaderItem.Settings,
                onClick = onOpenSettings,
                icon = Icons.Outlined.Settings,
                description = stringResource(R.string.cd_open_settings)
            )
        },
        content = content
    )
}

@Composable
private fun RowScope.HeaderAction(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    description: String
) {
    AppIconButton(
        icon = icon,
        contentDescription = description,
        role = if (selected) AppButtonRole.Primary else AppButtonRole.Neutral,
        variant = if (selected) AppIconButtonVariant.Tonal else AppIconButtonVariant.Standard,
        onClick = onClick
    )
}
