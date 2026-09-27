package com.wafflehq.base.ui.library.demos

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wafflehq.base.R
import com.wafflehq.lib.navigation.settings.CollapsibleSettingsGroup
import com.wafflehq.lib.navigation.settings.EditorScaffold
import com.wafflehq.lib.navigation.settings.SettingsDropdownField
import com.wafflehq.lib.navigation.settings.SettingsGroup
import com.wafflehq.lib.navigation.settings.SettingsNavRow
import com.wafflehq.lib.navigation.settings.SettingsRowDivider
import com.wafflehq.lib.navigation.settings.SettingsSliderControl
import com.wafflehq.lib.navigation.settings.SettingsSurfaceList
import com.wafflehq.lib.navigation.settings.SettingsSwitchRow
import com.wafflehq.lib.navigation.shell.AppNavItem
import com.wafflehq.lib.navigation.shell.AppNavSection
import com.wafflehq.lib.navigation.shell.AppSideNavDrawer
import com.wafflehq.lib.navigation.shell.MAX_NAV_SHORTCUTS
import com.wafflehq.lib.navigation.shell.NavShortcutMode
import com.wafflehq.lib.navigation.shell.NavShortcuts
import com.wafflehq.lib.navigation.shell.arrangeNavSections
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppDialogDefaults
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppPillSelector
import com.wafflehq.lib.uicore.components.AppSuggestionChip
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.theme.AppSpacing

internal enum class DemoNavGroup { TOOLS, PERSONAL }

internal enum class DemoNavTarget(
    val id: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val group: DemoNavGroup,
) {
    CALENDAR("calendar", R.string.libex_navigation_target_calendar, Icons.Filled.CalendarMonth, DemoNavGroup.TOOLS),
    NOTES("notes", R.string.libex_navigation_target_notes, Icons.Filled.Edit, DemoNavGroup.TOOLS),
    TASKS("todo", R.string.libex_navigation_target_todo, Icons.Filled.Checklist, DemoNavGroup.TOOLS),
    WEATHER("weather", R.string.libex_navigation_target_weather, Icons.Filled.WbSunny, DemoNavGroup.TOOLS),
    JOURNAL("journal", R.string.libex_navigation_target_journal, Icons.Filled.Book, DemoNavGroup.PERSONAL),
    HEALTH("health", R.string.libex_navigation_target_health, Icons.Filled.Favorite, DemoNavGroup.PERSONAL),
}

internal object NavigationDemoLogic {

    val availableIds: Set<String> = DemoNavTarget.entries.map { it.id }.toSet()

    fun visit(recent: List<String>, id: String): List<String> = NavShortcuts.recordVisit(recent, id)

    fun toggleCustom(custom: List<String>, id: String): List<String> = NavShortcuts.toggleCustom(custom, id)

    fun shortcuts(mode: NavShortcutMode, recent: List<String>, custom: List<String>): List<String> =
        NavShortcuts.resolve(mode, recent, custom, availableIds)

    fun sections(
        label: (Int) -> String,
        pinned: Set<String>,
        selectedId: String?,
        pinnedLabel: String,
        onSelect: (String) -> Unit,
        onPinToggle: (String) -> Unit,
    ): List<AppNavSection> {
        fun item(target: DemoNavTarget) = AppNavItem(
            label = label(target.labelRes),
            icon = target.icon,
            selected = target.id == selectedId,
            id = target.id,
            pinned = target.id in pinned,
            onPinToggle = { onPinToggle(target.id) },
            onClick = { onSelect(target.id) },
        )
        val sections = listOf(
            AppNavSection(
                label = label(R.string.libex_navigation_group_tools),
                items = DemoNavTarget.entries.filter { it.group == DemoNavGroup.TOOLS }.map(::item),
                id = DemoNavGroup.TOOLS.name,
            ),
            AppNavSection(
                label = label(R.string.libex_navigation_group_personal),
                items = DemoNavTarget.entries.filter { it.group == DemoNavGroup.PERSONAL }.map(::item),
                id = DemoNavGroup.PERSONAL.name,
            ),
        )
        return arrangeNavSections(sections, pinnedLabel)
    }
}

internal object NavigationTags {
    const val OPENED = "libex_navigation_opened"
    const val SHORTCUTS = "libex_navigation_shortcuts"
    const val EDITOR_OPEN = "libex_navigation_editor_open"
    const val EDITOR_FIELD = "libex_navigation_editor_field"
    const val EDITOR_RESULT = "libex_navigation_editor_result"
    fun mode(mode: NavShortcutMode) = "libex_navigation_mode_${mode.name}"
    fun target(target: DemoNavTarget) = "libex_navigation_target_${target.id}"
}

private val DRAWER_PREVIEW_HEIGHT = 420.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NavigationDemo() {
    val context = LocalContext.current
    var notifications by remember { mutableStateOf(true) }
    var densityIndex by remember { mutableIntStateOf(1) }
    var volume by remember { mutableFloatStateOf(40f) }
    var opened by remember { mutableStateOf<Int?>(null) }
    var mode by remember { mutableStateOf(NavShortcutMode.RECENT) }
    var recent by remember { mutableStateOf(emptyList<String>()) }
    var custom by remember { mutableStateOf(emptyList<String>()) }
    var pinned by remember { mutableStateOf(setOf<String>()) }
    var selected by remember { mutableStateOf<String?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var savedText by remember { mutableStateOf<String?>(null) }
    val densityOptions = listOf(
        stringResource(R.string.libex_navigation_density_compact),
        stringResource(R.string.libex_navigation_density_normal),
        stringResource(R.string.libex_navigation_density_relaxed),
    )
    val pinnedLabel = stringResource(R.string.libex_navigation_pinned)
    val sections = remember(pinned, selected, pinnedLabel) {
        NavigationDemoLogic.sections(
            label = { context.getString(it) },
            pinned = pinned,
            selectedId = selected,
            pinnedLabel = pinnedLabel,
            onSelect = { selected = it },
            onPinToggle = { id -> pinned = if (id in pinned) pinned - id else pinned + id },
        )
    }
    val resolved = NavigationDemoLogic.shortcuts(mode, recent, custom)

    DemoSection(
        id = "navigation",
        titleRes = R.string.libex_navigation_title,
        descriptionRes = R.string.libex_navigation_desc,
        moduleRes = R.string.libex_module_navigation,
    ) {
        SettingsGroup(label = stringResource(R.string.libex_navigation_settings_group)) {
            SettingsSwitchRow(
                title = stringResource(R.string.libex_navigation_switch_title),
                subtitle = stringResource(R.string.libex_navigation_switch_subtitle),
                checked = notifications,
                onCheckedChange = { notifications = it },
            )
            SettingsDropdownField(
                label = stringResource(R.string.libex_navigation_density),
                value = densityOptions[densityIndex],
                options = densityOptions,
                selectedIndex = densityIndex,
                onSelect = { densityIndex = it },
            )
            SettingsSliderControl(
                label = stringResource(R.string.libex_navigation_volume),
                valueText = { level -> context.getString(R.string.libex_value_percent, level.toInt()) },
                value = volume,
                onValueChange = { volume = it },
                valueRange = 0f..100f,
                steps = 9,
            )
            CollapsibleSettingsGroup(label = stringResource(R.string.libex_navigation_advanced)) {
                DemoMetaText(stringResource(R.string.libex_navigation_advanced_body))
            }
        }
        SettingsSurfaceList {
            SettingsNavRow(
                title = stringResource(R.string.libex_navigation_row_profile),
                subtitle = stringResource(R.string.libex_navigation_row_profile_desc),
                onClick = { opened = R.string.libex_navigation_row_profile },
            )
            SettingsRowDivider()
            SettingsNavRow(
                title = stringResource(R.string.libex_navigation_row_privacy),
                onClick = { opened = R.string.libex_navigation_row_privacy },
            )
        }
        opened?.let {
            DemoMetaText(
                text = stringResource(R.string.libex_navigation_opened, stringResource(it)),
                modifier = Modifier.testTag(NavigationTags.OPENED),
            )
        }
        AppHorizontalDivider()
        DemoLabelText(stringResource(R.string.libex_navigation_shortcuts_heading))
        AppPillSelector(
            options = NavShortcutMode.entries,
            selected = mode,
            onSelect = { mode = it },
            label = {
                stringResource(
                    if (it == NavShortcutMode.RECENT) R.string.libex_navigation_mode_recent else R.string.libex_navigation_mode_custom
                )
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = { NavigationTags.mode(it) },
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            DemoNavTarget.entries.forEach { target ->
                AppSuggestionChip(
                    onClick = {
                        if (mode == NavShortcutMode.RECENT) {
                            recent = NavigationDemoLogic.visit(recent, target.id)
                        } else {
                            custom = NavigationDemoLogic.toggleCustom(custom, target.id)
                        }
                    },
                    label = { Text(stringResource(target.labelRes)) },
                    modifier = Modifier.testTag(NavigationTags.target(target)),
                )
            }
        }
        DemoBodyText(
            text = if (resolved.isEmpty()) {
                stringResource(R.string.libex_navigation_shortcuts_empty)
            } else {
                stringResource(
                    R.string.libex_navigation_shortcuts_result,
                    MAX_NAV_SHORTCUTS,
                    resolved.joinToString { id -> DemoNavTarget.entries.first { it.id == id }.let { context.getString(it.labelRes) } },
                )
            },
            modifier = Modifier.testTag(NavigationTags.SHORTCUTS),
        )
        AppHorizontalDivider()
        DemoLabelText(stringResource(R.string.libex_navigation_drawer_heading))
        DemoMetaText(stringResource(R.string.libex_navigation_drawer_hint))
        Box(modifier = Modifier.fillMaxWidth().height(DRAWER_PREVIEW_HEIGHT)) {
            AppSideNavDrawer(
                title = stringResource(R.string.libex_navigation_drawer_title),
                subtitle = stringResource(R.string.libex_navigation_drawer_subtitle),
                headerIcon = Icons.Filled.Menu,
                sections = sections,
                searchPlaceholder = stringResource(R.string.libex_navigation_drawer_search),
                pinnedLabel = pinnedLabel,
            )
        }
        AppHorizontalDivider()
        AppButton(
            text = stringResource(R.string.libex_navigation_editor_open),
            role = AppButtonRole.Primary,
            variant = AppButtonVariant.Tonal,
            onClick = { showEditor = true },
            modifier = Modifier.testTag(NavigationTags.EDITOR_OPEN),
        )
        savedText?.let {
            DemoBodyText(
                text = stringResource(R.string.libex_navigation_editor_saved, it),
                modifier = Modifier.testTag(NavigationTags.EDITOR_RESULT),
            )
        }
    }

    if (showEditor) {
        Dialog(onDismissRequest = { showEditor = false }, properties = AppDialogDefaults.fullWidthProperties) {
            var value by remember { mutableStateOf("") }
            EditorScaffold(
                title = stringResource(R.string.libex_navigation_editor_title),
                onBack = { showEditor = false },
                onSave = {
                    savedText = value
                    showEditor = false
                },
                canSave = value.isNotBlank(),
            ) { padding ->
                Column(modifier = Modifier.padding(padding).padding(AppSpacing.lg)) {
                    AppTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text(stringResource(R.string.libex_navigation_editor_field)) },
                        modifier = Modifier.fillMaxWidth().testTag(NavigationTags.EDITOR_FIELD),
                    )
                }
            }
        }
    }
}
