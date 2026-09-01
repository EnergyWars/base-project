package com.wafflehq.base.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.base.data.settings.ThemeMode
import com.wafflehq.base.ui.components.SettingsDropdownField
import com.wafflehq.base.ui.components.SettingsGroup
import com.wafflehq.base.ui.components.SettingsGroupDivider
import com.wafflehq.base.ui.components.SettingsSliderControl
import com.wafflehq.base.ui.components.SettingsSwitchRow
import com.wafflehq.base.ui.theme.AppRole
import kotlin.math.roundToInt

@Composable
fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
    ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
}

@Composable
fun DisplaySettingsContent(
    themeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    groupCodes: List<String>? = null,
) {
    val modes = ThemeMode.entries
    val themeLabels = modes.map { themeModeLabel(it) }

    var fontSize by remember { mutableFloatStateOf(110f) }
    var contrast by remember { mutableFloatStateOf(3f) }
    var switchA by remember { mutableStateOf(true) }
    var switchB by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        SettingsGroup(
            label = stringResource(R.string.settings_group_general),
            tint = AppRole.Primary,
            fraction = 0.08f,
            inspectCode = groupCodes?.getOrNull(0),
        ) {
            SettingsDropdownField(
                label = stringResource(R.string.settings_design_label),
                value = themeModeLabel(themeMode),
                options = themeLabels,
                selectedIndex = modes.indexOf(themeMode),
                onSelect = { index -> onThemeSelected(modes[index]) },
            )
        }
        SettingsGroupDivider()
        SettingsGroup(
            label = stringResource(R.string.settings_group_lorem),
            tint = AppRole.Secondary,
            fraction = 0.08f,
            inspectCode = groupCodes?.getOrNull(1),
        ) {
            SettingsSliderControl(
                label = stringResource(R.string.settings_ctrl_fontsize),
                valueText = stringResource(R.string.settings_percent, fontSize.roundToInt()),
                value = fontSize,
                onValueChange = { fontSize = it },
                valueRange = 80f..140f,
            )
            SettingsSwitchRow(
                title = stringResource(R.string.settings_srow_a_title),
                subtitle = stringResource(R.string.settings_srow_a_sub),
                checked = switchA,
                onCheckedChange = { switchA = it },
            )
        }
        SettingsGroupDivider()
        SettingsGroup(
            label = stringResource(R.string.settings_group_consectetur),
            tint = AppRole.Tertiary,
            fraction = 0.09f,
            inspectCode = groupCodes?.getOrNull(2),
        ) {
            SettingsSliderControl(
                label = stringResource(R.string.settings_ctrl_contrast),
                valueText = stringResource(R.string.settings_ratio, contrast.roundToInt(), 5),
                value = contrast,
                onValueChange = { contrast = it },
                valueRange = 0f..5f,
                steps = 4,
            )
            SettingsSwitchRow(
                title = stringResource(R.string.settings_srow_b_title),
                subtitle = stringResource(R.string.settings_srow_b_sub),
                checked = switchB,
                onCheckedChange = { switchB = it },
            )
        }
    }
}
