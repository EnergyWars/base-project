package com.wafflehq.base.ui.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppRole
import com.wafflehq.base.ui.theme.AppTheme
import com.wafflehq.base.ui.theme.GeistMono
import com.wafflehq.lib.uicore.components.AppSlider
import com.wafflehq.lib.uicore.theme.AppRadius
import kotlin.math.roundToInt

internal enum class MockThemeMode { System, Light, Dark }

@Immutable
internal data class MockNavItem(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean,
    val onClick: () -> Unit,
    val inspectCode: String? = null,
)

@Immutable
internal data class MockNavSection(
    val label: String? = null,
    val items: List<MockNavItem>,
)

private val boxShape = RoundedCornerShape(AppRadius.card)

@Composable
private fun mockThemeLabel(mode: MockThemeMode): String = when (mode) {
    MockThemeMode.System -> stringResource(R.string.settings_theme_system)
    MockThemeMode.Light -> stringResource(R.string.settings_theme_light)
    MockThemeMode.Dark -> stringResource(R.string.settings_theme_dark)
}

@Composable
internal fun MockSettingsTopBar(
    title: String,
    onBack: () -> Unit,
    backDescription: String,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(pill)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = backDescription,
                    tint = colors.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = title,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
                color = colors.onSurface,
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.outline))
    }
}

@Composable
internal fun MockSettingsListContent(
    featuresLabel: String,
    displayLabel: String,
    displaySubtitle: String,
    modifier: Modifier = Modifier,
    featuresCode: String? = null,
    displayCode: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth().background(AppTheme.colors.surface)) {
        MockSettingsListRow(title = featuresLabel, subtitle = null, inspectCode = featuresCode)
        MockSettingsRowDivider()
        MockSettingsListRow(title = displayLabel, subtitle = displaySubtitle, inspectCode = displayCode)
        MockSettingsRowDivider()
    }
}

@Composable
private fun MockSettingsListRow(title: String, subtitle: String?, inspectCode: String?) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (inspectCode != null) Modifier.inspectId(inspectCode) else Modifier.clickable {})
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
                color = colors.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
                    color = colors.onSurfaceVariant,
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun MockSettingsRowDivider() {
    Box(
        Modifier
            .padding(start = 16.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(AppTheme.colors.outline),
    )
}

@Composable
internal fun MockDisplaySettingsContent(
    themeMode: MockThemeMode,
    onThemeSelected: (MockThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    groupCodes: List<String>? = null,
) {
    val modes = MockThemeMode.entries
    val themeLabels = modes.map { mockThemeLabel(it) }

    var fontSize by remember { mutableFloatStateOf(110f) }
    var contrast by remember { mutableFloatStateOf(3f) }
    var switchA by remember { mutableStateOf(true) }
    var switchB by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        MockSettingsGroup(
            label = stringResource(R.string.settings_group_general),
            tint = AppRole.Primary,
            fraction = 0.08f,
            inspectCode = groupCodes?.getOrNull(0),
        ) {
            MockDropdownField(
                label = stringResource(R.string.settings_design_label),
                value = mockThemeLabel(themeMode),
                options = themeLabels,
                selectedIndex = modes.indexOf(themeMode),
                onSelect = { index -> onThemeSelected(modes[index]) },
            )
        }
        MockGroupDivider()
        MockSettingsGroup(
            label = stringResource(R.string.settings_group_lorem),
            tint = AppRole.Secondary,
            fraction = 0.08f,
            inspectCode = groupCodes?.getOrNull(1),
        ) {
            MockSliderControl(
                label = stringResource(R.string.settings_ctrl_fontsize),
                valueText = stringResource(R.string.settings_percent, fontSize.roundToInt()),
                value = fontSize,
                onValueChange = { fontSize = it },
                valueRange = 80f..140f,
            )
            MockSwitchRow(
                title = stringResource(R.string.settings_srow_a_title),
                subtitle = stringResource(R.string.settings_srow_a_sub),
                checked = switchA,
                onCheckedChange = { switchA = it },
            )
        }
        MockGroupDivider()
        MockSettingsGroup(
            label = stringResource(R.string.settings_group_consectetur),
            tint = AppRole.Tertiary,
            fraction = 0.09f,
            inspectCode = groupCodes?.getOrNull(2),
        ) {
            MockSliderControl(
                label = stringResource(R.string.settings_ctrl_contrast),
                valueText = stringResource(R.string.settings_ratio, contrast.roundToInt(), 5),
                value = contrast,
                onValueChange = { contrast = it },
                valueRange = 0f..5f,
                steps = 4,
            )
            MockSwitchRow(
                title = stringResource(R.string.settings_srow_b_title),
                subtitle = stringResource(R.string.settings_srow_b_sub),
                checked = switchB,
                onCheckedChange = { switchB = it },
            )
        }
    }
}

@Composable
private fun MockSettingsGroup(
    label: String,
    tint: AppRole,
    fraction: Float,
    modifier: Modifier = Modifier,
    inspectCode: String? = null,
    content: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    val accent = colors.forRole(tint).accent
    val boxBackground = lerp(colors.surface, accent, fraction)
    val boxBorder = lerp(colors.outline, accent, 0.18f)
    Column(modifier = modifier.fillMaxWidth().then(if (inspectCode != null) Modifier.inspectTap(inspectCode) else Modifier).padding(16.dp)) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = GeistMono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                letterSpacing = 0.7.sp,
            ),
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 2.dp, bottom = 10.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(boxShape)
                .background(boxBackground)
                .border(1.dp, boxBorder, boxShape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = { content() },
        )
    }
}

@Composable
private fun MockGroupDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(AppTheme.colors.outline))
}

@Composable
private fun MockDropdownField(
    label: String,
    value: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    val colors = AppTheme.colors
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = label,
            style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
            color = colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        Box {
            Row(
                modifier = Modifier
                    .clip(pill)
                    .border(1.dp, colors.outline, pill)
                    .background(colors.surface)
                    .clickable { expanded = true }
                    .padding(start = 14.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = value,
                    style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
                    color = colors.onSurface,
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = colors.surface,
            ) {
                options.forEachIndexed { index, option ->
                    val selected = index == selectedIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (selected) colors.secondary.container else Color.Transparent)
                            .clickable {
                                onSelect(index)
                                expanded = false
                            }
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = option,
                            style = TextStyle(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                            ),
                            color = if (selected) colors.secondary.onContainer else colors.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        if (selected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = colors.secondary.onContainer,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MockSliderControl(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = label,
                style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
                color = colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = valueText,
                style = TextStyle(fontFamily = GeistMono, fontSize = 13.sp, lineHeight = 18.sp),
                color = colors.onSurfaceVariant,
            )
        }
        AppSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
        )
    }
}

@Composable
private fun MockSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
                color = colors.onSurface,
            )
            Text(
                text = subtitle,
                style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
                color = colors.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun MockSideNavDrawer(
    title: String,
    sections: List<MockNavSection>,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val itemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = colors.success.container,
        unselectedContainerColor = Color.Transparent,
        selectedIconColor = colors.success.onContainer,
        unselectedIconColor = colors.onSurfaceVariant,
        selectedTextColor = colors.success.onContainer,
        unselectedTextColor = colors.onSurfaceVariant,
    )
    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = colors.surface,
    ) {
        Column {
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
            )
            sections.forEach { section ->
                if (section.label != null) {
                    Text(
                        text = section.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                    )
                }
                section.items.forEach { item ->
                    MockDrawerNavItem(item, itemColors)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MockDrawerNavItem(item: MockNavItem, itemColors: NavigationDrawerItemColors) {
    val colors = AppTheme.colors
    val pillShape = CircleShape
    val itemModifier = if (item.selected) Modifier.border(1.dp, colors.primary.accent, pillShape) else Modifier
    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
        NavigationDrawerItem(
            icon = { Icon(item.icon, contentDescription = null) },
            label = { Text(item.label) },
            selected = item.selected,
            onClick = item.onClick,
            shape = pillShape,
            colors = itemColors,
            modifier = itemModifier,
        )
        if (item.inspectCode != null) {
            Box(Modifier.matchParentSize().inspectId(item.inspectCode, item.onClick))
        }
    }
}
