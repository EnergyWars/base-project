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
import com.wafflehq.base.data.settings.ThemeMode
import com.wafflehq.base.ui.components.AppScaffold
import com.wafflehq.base.ui.components.HeaderItem
import com.wafflehq.base.ui.settings.SettingsViewModel
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme
import com.wafflehq.base.ui.theme.showcase.Section01Typography
import com.wafflehq.base.ui.theme.showcase.Section02Weights
import com.wafflehq.base.ui.theme.showcase.Section03Ramps
import com.wafflehq.base.ui.theme.showcase.Section04Surfaces
import com.wafflehq.base.ui.theme.showcase.Section05Roles
import com.wafflehq.base.ui.theme.showcase.Section06Buttons
import com.wafflehq.base.ui.theme.showcase.Section07Fab
import com.wafflehq.base.ui.theme.showcase.Section08IconButtons
import com.wafflehq.base.ui.theme.showcase.Section09Chips
import com.wafflehq.base.ui.theme.showcase.Section10TextFields
import com.wafflehq.base.ui.theme.showcase.Section11Cards
import com.wafflehq.base.ui.theme.showcase.Section12List
import com.wafflehq.base.ui.theme.showcase.Section13Selection
import com.wafflehq.base.ui.theme.showcase.Section14Segmented
import com.wafflehq.base.ui.theme.showcase.Section15SliderProgress
import com.wafflehq.base.ui.theme.showcase.Section16Badges
import com.wafflehq.base.ui.theme.showcase.Section17Banners
import com.wafflehq.base.ui.theme.showcase.Section18SnackbarDialog
import com.wafflehq.base.ui.theme.showcase.Section19Icons
import com.wafflehq.base.ui.theme.showcase.Section20Dividers
import com.wafflehq.base.ui.theme.showcase.Section21Spacing
import com.wafflehq.base.ui.theme.showcase.Section22AppHeader
import com.wafflehq.base.ui.theme.showcase.Section23SettingsList
import com.wafflehq.base.ui.theme.showcase.Section24SettingsDetail
import com.wafflehq.base.ui.theme.showcase.Section25FilterList
import com.wafflehq.base.ui.theme.showcase.Section26DndList
import com.wafflehq.base.ui.theme.showcase.Section27DeleteList
import com.wafflehq.base.ui.theme.showcase.Section28PlainList
import com.wafflehq.base.ui.theme.showcase.Section29GroupedList
import com.wafflehq.base.ui.theme.showcase.Section30AccordionList
import com.wafflehq.base.ui.theme.showcase.Section31ControlList
import com.wafflehq.base.ui.theme.showcase.Section32ContainerBoxes
import com.wafflehq.base.ui.theme.showcase.Section33ComboList
import com.wafflehq.base.ui.theme.showcase.ElementInspectorHost
import com.wafflehq.base.ui.theme.showcase.InspectSection
import com.wafflehq.base.ui.theme.showcase.ShowcaseLede
import com.wafflehq.base.ui.theme.showcase.ShowcaseThemeToggle

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

    ElementInspectorHost {
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
