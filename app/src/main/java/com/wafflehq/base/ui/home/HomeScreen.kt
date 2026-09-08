package com.wafflehq.base.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.uikit.theme.ThemeMode
import com.wafflehq.uikit.components.AppScaffold
import com.wafflehq.uikit.components.HeaderItem
import com.wafflehq.base.ui.settings.SettingsViewModel
import com.wafflehq.uikit.theme.AppSpacing
import com.wafflehq.uikit.theme.AppTheme
import com.wafflehq.uikit.showcase.Section01Typography
import com.wafflehq.uikit.showcase.Section02Weights
import com.wafflehq.uikit.showcase.Section03Ramps
import com.wafflehq.uikit.showcase.Section04Surfaces
import com.wafflehq.uikit.showcase.Section05Roles
import com.wafflehq.uikit.showcase.Section06Buttons
import com.wafflehq.uikit.showcase.Section07Fab
import com.wafflehq.uikit.showcase.Section08IconButtons
import com.wafflehq.uikit.showcase.Section09Chips
import com.wafflehq.uikit.showcase.Section10TextFields
import com.wafflehq.uikit.showcase.Section11Cards
import com.wafflehq.uikit.showcase.Section12List
import com.wafflehq.uikit.showcase.Section13Selection
import com.wafflehq.uikit.showcase.Section14Segmented
import com.wafflehq.uikit.showcase.Section15SliderProgress
import com.wafflehq.uikit.showcase.Section16Badges
import com.wafflehq.uikit.showcase.Section17Banners
import com.wafflehq.uikit.showcase.Section18SnackbarDialog
import com.wafflehq.uikit.showcase.Section19Icons
import com.wafflehq.uikit.showcase.Section20Dividers
import com.wafflehq.uikit.showcase.Section21Spacing
import com.wafflehq.uikit.showcase.Section22AppHeader
import com.wafflehq.uikit.showcase.Section23SettingsList
import com.wafflehq.uikit.showcase.Section24SettingsDetail
import com.wafflehq.uikit.showcase.Section25FilterList
import com.wafflehq.uikit.showcase.Section26DndList
import com.wafflehq.uikit.showcase.Section27DeleteList
import com.wafflehq.uikit.showcase.Section28PlainList
import com.wafflehq.uikit.showcase.Section29GroupedList
import com.wafflehq.uikit.showcase.Section30AccordionList
import com.wafflehq.uikit.showcase.Section31ControlList
import com.wafflehq.uikit.showcase.Section32ContainerBoxes
import com.wafflehq.uikit.showcase.Section33ComboList
import com.wafflehq.uikit.showcase.ElementInspectorHost
import com.wafflehq.uikit.showcase.InspectSection
import com.wafflehq.uikit.showcase.ShowcaseLede
import com.wafflehq.uikit.showcase.ShowcaseThemeToggle

@Composable
fun HomeScreen(
    onOpenMenu: () -> Unit,
    onNavigateHome: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    ElementInspectorHost(enabled = true) {
        AppScaffold(
            activeItem = HeaderItem.Home,
            onOpenMenu = onOpenMenu,
            onNavigateHome = onNavigateHome,
            onOpenSettings = onOpenSettings,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.background)
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
            ) {
                item { InspectSection("section.intro") { ShowcaseLede() } }
                item { InspectSection("section.theme-toggle") { ShowcaseThemeToggle(dark) { viewModel.onThemeModeSelected(if (it) ThemeMode.DARK else ThemeMode.LIGHT) } } }

                item { InspectSection("section.typography") { Section01Typography() } }
                item { InspectSection("section.font-weights") { Section02Weights() } }
                item { InspectSection("section.hue-ramps") { Section03Ramps() } }
                item { InspectSection("section.surfaces") { Section04Surfaces() } }
                item { InspectSection("section.roles") { Section05Roles() } }
                item { InspectSection("section.buttons") { Section06Buttons() } }
                item { InspectSection("section.fab") { Section07Fab() } }
                item { InspectSection("section.icon-buttons") { Section08IconButtons() } }
                item { InspectSection("section.chips") { Section09Chips() } }
                item { InspectSection("section.text-fields") { Section10TextFields() } }
                item { InspectSection("section.cards") { Section11Cards() } }
                item { InspectSection("section.list-items") { Section12List() } }
                item { InspectSection("section.selection-controls") { Section13Selection() } }
                item { InspectSection("section.segmented-buttons") { Section14Segmented() } }
                item { InspectSection("section.slider-progress") { Section15SliderProgress() } }
                item { InspectSection("section.badges") { Section16Badges() } }
                item { InspectSection("section.banners") { Section17Banners() } }
                item { InspectSection("section.snackbar-dialog") { Section18SnackbarDialog() } }
                item { InspectSection("section.icons") { Section19Icons() } }
                item { InspectSection("section.dividers") { Section20Dividers() } }
                item { InspectSection("section.spacing-radii") { Section21Spacing() } }
                item { InspectSection("section.app-header") { Section22AppHeader() } }
                item { InspectSection("section.settings-list") { Section23SettingsList() } }
                item { InspectSection("section.settings-detail") { Section24SettingsDetail() } }
                item { InspectSection("section.filterable-list") { Section25FilterList() } }
                item { InspectSection("section.dnd-list") { Section26DndList() } }
                item { InspectSection("section.select-delete-list") { Section27DeleteList() } }
                item { InspectSection("section.plain-list") { Section28PlainList() } }
                item { InspectSection("section.grouped-list") { Section29GroupedList() } }
                item { InspectSection("section.expandable-list") { Section30AccordionList() } }
                item { InspectSection("section.control-list") { Section31ControlList() } }
                item { InspectSection("section.outlined-container") { Section32ContainerBoxes() } }
                item { InspectSection("section.combined-list") { Section33ComboList() } }

                item { Spacer(Modifier.height(AppSpacing.xxl)) }
            }
        }
    }
}
