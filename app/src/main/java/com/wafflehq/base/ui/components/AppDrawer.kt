package com.wafflehq.base.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.navigation.Routes
import com.wafflehq.base.ui.theme.AppTheme

private data class DrawerPage(
    val route: String,
    val icon: ImageVector,
    val labelRes: Int,
)

@Composable
fun AppDrawer(
    currentRoute: String?,
    onSelect: (String) -> Unit,
) {
    val pages = listOf(
        DrawerPage(Routes.HOME, Icons.Outlined.Home, R.string.header_home),
        DrawerPage(Routes.EXAMPLE_1, Icons.Outlined.Article, R.string.example_1_title),
        DrawerPage(Routes.EXAMPLE_2, Icons.Outlined.Layers, R.string.example_2_title),
        DrawerPage(Routes.EXAMPLE_3, Icons.Outlined.Widgets, R.string.example_3_title),
        DrawerPage(Routes.SETTINGS, Icons.Outlined.Settings, R.string.label_settings),
    )
    ModalDrawerSheet(
        drawerContainerColor = AppTheme.colors.surface,
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.onSurface,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
        )
        Text(
            text = stringResource(R.string.nav_section_pages),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
        )
        pages.forEach { page ->
            NavigationDrawerItem(
                icon = { Icon(page.icon, contentDescription = null) },
                label = { Text(stringResource(page.labelRes)) },
                selected = currentRoute == page.route,
                onClick = { onSelect(page.route) },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}
