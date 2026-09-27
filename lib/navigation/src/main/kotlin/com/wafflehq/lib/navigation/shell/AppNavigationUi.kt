package com.wafflehq.lib.navigation.shell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FabPosition
import androidx.compose.material3.ModalNavigationDrawer
import com.wafflehq.lib.uicore.scaffold.AppScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

object AppNavigationTestTags {
    const val TOP_BAR = "app_nav_top_bar"
    const val TOP_BAR_TITLE = "app_nav_top_bar_title"
    const val TOP_BAR_MENU = "app_nav_top_bar_menu"
    const val TOP_BAR_SEARCH = "app_nav_top_bar_search"
    const val TOP_BAR_SETTINGS = "app_nav_top_bar_settings"
    const val TOP_BAR_HOME = "app_nav_top_bar_home"
    const val TOP_BAR_STATUS = "app_nav_top_bar_status"
    const val DRAWER = "app_nav_drawer"
    const val DRAWER_SEARCH = "app_nav_drawer_search"
    const val DRAWER_CLOSE = "app_nav_drawer_close"
    const val DRAWER_FOOTER_ACTION = "app_nav_drawer_footer_action"
    const val DRAWER_PIN_HINT = "app_nav_drawer_pin_hint"
    const val DRAWER_SHOW_HIDDEN = "app_nav_drawer_show_hidden"
    fun topBarShortcut(id: String) = "app_nav_shortcut_$id"
    fun topBarShortcutLabel(id: String) = "app_nav_shortcut_label_$id"
    fun drawerItem(label: String) = "app_nav_drawer_item_$label"
    fun drawerPinnedItem(label: String) = "app_nav_drawer_pinned_$label"
    fun drawerPinToggle(label: String) = "app_nav_drawer_pin_toggle_$label"
    fun drawerHideToggle(label: String) = "app_nav_drawer_hide_toggle_$label"
    fun drawerSection(label: String) = "app_nav_drawer_section_$label"
}

data class AppNavItem(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean,
    val enabled: Boolean = true,
    val id: String = label,
    val pinned: Boolean = false,
    val accentIndex: Int? = null,
    val onPinToggle: (() -> Unit)? = null,
    val hidden: Boolean = false,
    val onHideToggle: (() -> Unit)? = null,
    val onClick: () -> Unit
)

data class AppNavSection(
    val label: String? = null,
    val items: List<AppNavItem>,
    val pinned: Boolean = false,
    val id: String = label.orEmpty()
)

@Composable
fun AppNavigationScaffold(
    drawerState: DrawerState,
    drawerContent: @Composable () -> Unit,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    gesturesEnabled: Boolean = drawerState.isOpen,
    content: @Composable (PaddingValues) -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = drawerContent
    ) {
        AppScaffold(
            topBar = topBar,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            content = content
        )
    }
}

fun appContentPadding(isFullScreenSubPage: Boolean, innerPadding: PaddingValues): PaddingValues =
    if (isFullScreenSubPage) PaddingValues(0.dp) else innerPadding
