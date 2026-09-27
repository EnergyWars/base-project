package com.wafflehq.base.ui.library.demos

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.color.ContrastLevel
import com.wafflehq.lib.uicore.color.ColorCanvasPicker
import com.wafflehq.lib.uicore.color.contrastLevelForNormalText
import com.wafflehq.lib.uicore.color.contrastRatio
import com.wafflehq.lib.uicore.color.contrastTextColor
import com.wafflehq.lib.uicore.components.AppCard
import com.wafflehq.lib.uicore.components.AppCheckbox
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppLegendItem
import com.wafflehq.lib.uicore.components.AppListItem
import com.wafflehq.lib.uicore.components.AppRadioButton
import com.wafflehq.lib.uicore.components.AppSearchField
import com.wafflehq.lib.uicore.components.AppSwitch
import com.wafflehq.lib.uicore.components.AppWeekdayChipRow
import com.wafflehq.lib.uicore.components.BreadcrumbBar
import com.wafflehq.lib.uicore.components.BreadcrumbItem
import com.wafflehq.lib.uicore.components.IconPickerGrid
import com.wafflehq.lib.uicore.components.IconPickerOption
import com.wafflehq.lib.uicore.menu.AppOptionDropdownField
import com.wafflehq.lib.uicore.theme.AppSpacing
import java.time.DayOfWeek
import java.util.Locale

internal object UiCoreInputLogic {

    val ITEM_RES: List<Int> = listOf(
        R.string.libex_uicore_item_belgian,
        R.string.libex_uicore_item_liege,
        R.string.libex_uicore_item_stroop,
        R.string.libex_uicore_item_hongkong,
        R.string.libex_uicore_item_pandan,
    )

    val DEFAULT_DAYS: Set<DayOfWeek> = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)

    fun filter(items: List<String>, query: String): List<String> =
        if (query.isBlank()) items else items.filter { it.contains(query.trim(), ignoreCase = true) }

    fun toggle(selected: Set<DayOfWeek>, day: DayOfWeek): Set<DayOfWeek> =
        if (day in selected) selected - day else selected + day

    fun contrastText(ratio: Double, locale: Locale = Locale.getDefault()): String = String.format(locale, "%.1f:1", ratio)

    @StringRes
    fun contrastLevelLabel(level: ContrastLevel): Int = when (level) {
        ContrastLevel.AAA -> R.string.libex_uicore_contrast_aaa
        ContrastLevel.AA -> R.string.libex_uicore_contrast_aa
        ContrastLevel.LOW -> R.string.libex_uicore_contrast_low
    }

    @StringRes
    fun priorityLabel(priority: DemoPriority): Int = when (priority) {
        DemoPriority.LOW -> R.string.libex_uicore_priority_low
        DemoPriority.NORMAL -> R.string.libex_uicore_priority_normal
        DemoPriority.HIGH -> R.string.libex_uicore_priority_high
    }

    val ICON_KEYS: List<String> = listOf("home", "star", "favorite", "cake", "pets", "work")
}

internal object UiCoreInputTags {
    const val SWITCH = "libex_uicore_switch"
    const val CHECKBOX = "libex_uicore_checkbox"
    const val RADIO_ONE = "libex_uicore_radio_one"
    const val RADIO_TWO = "libex_uicore_radio_two"
    const val SEARCH = "libex_uicore_search"
    const val ITEMS = "libex_uicore_items"
    const val DAYS_RESULT = "libex_uicore_days_result"
    const val ICON_RESULT = "libex_uicore_icon_result"
    const val CRUMB_DEEPER = "libex_uicore_crumb_deeper"
    const val CRUMB_RESULT = "libex_uicore_crumb_result"
    const val CONTRAST = "libex_uicore_contrast"
    const val PRIORITY_RESULT = "libex_uicore_priority_result"
    const val PRIORITY_FIELD = "libex_uicore_priority_field"
}

@Composable
internal fun UiCoreInputDemo() {
    var switchOn by remember { mutableStateOf(true) }
    var checked by remember { mutableStateOf(false) }
    var radio by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var days by remember { mutableStateOf(UiCoreInputLogic.DEFAULT_DAYS) }
    var priority by remember { mutableStateOf(DemoPriority.NORMAL) }
    var iconKey by remember { mutableStateOf<String?>(null) }
    var crumbLevel by remember { mutableIntStateOf(0) }
    var pickedColor by remember { mutableStateOf<Color?>(null) }
    val items = UiCoreInputLogic.ITEM_RES.map { stringResource(it) }
    val visibleItems = UiCoreInputLogic.filter(items, query)
    val crumbs = listOf(
        stringResource(R.string.libex_uicore_crumb_home),
        stringResource(R.string.libex_uicore_crumb_library),
        stringResource(R.string.libex_uicore_crumb_docs),
    )
    val iconLabels = listOf(
        stringResource(R.string.libex_uicore_icon_home),
        stringResource(R.string.libex_uicore_icon_star),
        stringResource(R.string.libex_uicore_icon_favorite),
        stringResource(R.string.libex_uicore_icon_cake),
        stringResource(R.string.libex_uicore_icon_pets),
        stringResource(R.string.libex_uicore_icon_work),
    )
    val iconOptions = listOf(
        IconPickerOption(UiCoreInputLogic.ICON_KEYS[0], Icons.Filled.Home, iconLabels[0]),
        IconPickerOption(UiCoreInputLogic.ICON_KEYS[1], Icons.Filled.Star, iconLabels[1]),
        IconPickerOption(UiCoreInputLogic.ICON_KEYS[2], Icons.Filled.Favorite, iconLabels[2]),
        IconPickerOption(UiCoreInputLogic.ICON_KEYS[3], Icons.Filled.Cake, iconLabels[3]),
        IconPickerOption(UiCoreInputLogic.ICON_KEYS[4], Icons.Filled.Pets, iconLabels[4]),
        IconPickerOption(UiCoreInputLogic.ICON_KEYS[5], Icons.Filled.Work, iconLabels[5]),
    )
    val surface = MaterialTheme.colorScheme.surface

    DemoSection(
        id = "uicore_input",
        titleRes = R.string.libex_uicore_input_title,
        descriptionRes = R.string.libex_uicore_input_desc,
        moduleRes = R.string.libex_module_uicore,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            AppSwitch(checked = switchOn, onCheckedChange = { switchOn = it }, modifier = Modifier.testTag(UiCoreInputTags.SWITCH))
            AppCheckbox(checked = checked, onCheckedChange = { checked = it }, modifier = Modifier.testTag(UiCoreInputTags.CHECKBOX))
            AppRadioButton(selected = radio == 0, onClick = { radio = 0 }, modifier = Modifier.testTag(UiCoreInputTags.RADIO_ONE))
            AppRadioButton(selected = radio == 1, onClick = { radio = 1 }, modifier = Modifier.testTag(UiCoreInputTags.RADIO_TWO))
        }
        AppHorizontalDivider()
        AppSearchField(
            query = query,
            onQueryChange = { query = it },
            placeholder = stringResource(R.string.libex_uicore_search_placeholder),
            modifier = Modifier.fillMaxWidth().testTag(UiCoreInputTags.SEARCH),
        )
        visibleItems.forEach { item ->
            AppListItem(
                headlineContent = { Text(item) },
                modifier = Modifier.testTag(UiCoreInputTags.ITEMS),
            )
        }
        if (visibleItems.isEmpty()) DemoMetaText(stringResource(R.string.libex_uicore_search_empty))
        AppHorizontalDivider()
        AppWeekdayChipRow(
            selected = days,
            onToggle = { days = UiCoreInputLogic.toggle(days, it) },
            modifier = Modifier.fillMaxWidth(),
        )
        DemoMetaText(
            text = stringResource(R.string.libex_uicore_days_result, days.size),
            modifier = Modifier.testTag(UiCoreInputTags.DAYS_RESULT),
        )
        AppOptionDropdownField(
            options = DemoPriority.entries,
            selected = priority,
            onSelect = { priority = it },
            optionLabel = { stringResource(UiCoreInputLogic.priorityLabel(it)) },
            label = { Text(stringResource(R.string.libex_uicore_priority_label)) },
            modifier = Modifier.fillMaxWidth(),
            fieldModifier = Modifier.testTag(UiCoreInputTags.PRIORITY_FIELD),
        )
        DemoMetaText(
            text = stringResource(R.string.libex_uicore_priority_result, priority.name),
            modifier = Modifier.testTag(UiCoreInputTags.PRIORITY_RESULT),
        )
        AppHorizontalDivider()
        IconPickerGrid(
            options = iconOptions,
            selectedKey = iconKey,
            onSelect = { iconKey = it },
            modifier = Modifier.fillMaxWidth(),
            gridHeight = ICON_GRID_HEIGHT,
        )
        DemoMetaText(
            text = iconKey?.let { key -> stringResource(R.string.libex_uicore_icon_result, iconOptions.first { it.key == key }.label) }
                ?: stringResource(R.string.libex_uicore_icon_none),
            modifier = Modifier.testTag(UiCoreInputTags.ICON_RESULT),
        )
        AppHorizontalDivider()
        BreadcrumbBar(
            items = crumbs.take(crumbLevel + 1).mapIndexed { index, label ->
                BreadcrumbItem(label) { crumbLevel = index }
            },
        )
        AppButton(
            text = stringResource(R.string.libex_uicore_crumb_deeper),
            role = AppButtonRole.Primary,
            variant = AppButtonVariant.Outlined,
            enabled = crumbLevel < crumbs.lastIndex,
            onClick = { crumbLevel += 1 },
            modifier = Modifier.testTag(UiCoreInputTags.CRUMB_DEEPER),
        )
        DemoMetaText(
            text = stringResource(R.string.libex_uicore_crumb_result, crumbLevel + 1),
            modifier = Modifier.testTag(UiCoreInputTags.CRUMB_RESULT),
        )
        AppHorizontalDivider()
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            AppLegendItem(color = MaterialTheme.colorScheme.primary, label = stringResource(R.string.libex_uicore_legend_primary))
            AppLegendItem(color = MaterialTheme.colorScheme.secondary, label = stringResource(R.string.libex_uicore_legend_secondary))
            AppLegendItem(color = MaterialTheme.colorScheme.tertiary, label = stringResource(R.string.libex_uicore_legend_tertiary))
        }
        ColorCanvasPicker(
            initialColor = MaterialTheme.colorScheme.primary,
            onColorChanged = { pickedColor = it },
            modifier = Modifier.fillMaxWidth(),
        )
        pickedColor?.let { color ->
            val ratio = contrastRatio(color, surface)
            AppCard(
                containerColor = color,
                contentColor = color.contrastTextColor(),
                modifier = Modifier.fillMaxWidth().testTag(UiCoreInputTags.CONTRAST),
            ) {
                Text(
                    text = stringResource(
                        R.string.libex_uicore_contrast_result,
                        UiCoreInputLogic.contrastText(ratio),
                        stringResource(UiCoreInputLogic.contrastLevelLabel(contrastLevelForNormalText(ratio))),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(AppSpacing.md),
                )
            }
        }
    }
}

private val ICON_GRID_HEIGHT = 120.dp
