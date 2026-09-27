package com.wafflehq.base.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.base.ui.navigation.Routes
import com.wafflehq.lib.navigation.shell.AppNavItem
import com.wafflehq.lib.navigation.shell.AppNavSection
import com.wafflehq.lib.navigation.shell.AppSideNavDrawer

private data class DrawerPage(
    val route: String,
    val icon: ImageVector,
    val labelRes: Int
)

private val DrawerPages = listOf(
    DrawerPage(Routes.HOME, Icons.Outlined.Home, R.string.header_home),
    DrawerPage(Routes.EXAMPLE_1, Icons.AutoMirrored.Outlined.Article, R.string.example_1_title),
    DrawerPage(Routes.EXAMPLE_2, Icons.Outlined.Layers, R.string.example_2_title),
    DrawerPage(Routes.EXAMPLE_3, Icons.Outlined.Widgets, R.string.example_3_title),
    DrawerPage(Routes.LIBRARY_EXAMPLES, Icons.Outlined.Extension, R.string.nav_library_examples),
    DrawerPage(Routes.SETTINGS, Icons.Outlined.Settings, R.string.label_settings)
)

@Composable
fun AppDrawer(
    currentRoute: String?,
    onSelect: (String) -> Unit,
    onClose: () -> Unit
) {
    AppSideNavDrawer(
        title = stringResource(R.string.app_name),
        searchPlaceholder = stringResource(R.string.nav_search_placeholder),
        closeDescription = stringResource(R.string.label_close),
        onClose = onClose,
        sections = listOf(
            AppNavSection(
                label = stringResource(R.string.nav_section_pages),
                items = DrawerPages.map { page ->
                    AppNavItem(
                        label = stringResource(page.labelRes),
                        icon = page.icon,
                        selected = currentRoute == page.route,
                        onClick = { onSelect(page.route) }
                    )
                }
            )
        )
    )
}
