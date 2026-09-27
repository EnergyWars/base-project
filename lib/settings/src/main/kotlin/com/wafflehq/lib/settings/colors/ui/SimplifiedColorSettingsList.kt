package com.wafflehq.lib.settings.colors.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.colors.ColorRamp
import com.wafflehq.lib.settings.colors.ColorRampTable
import com.wafflehq.lib.settings.colors.labelRes
import com.wafflehq.lib.navigation.settings.SettingsRowDivider
import com.wafflehq.lib.navigation.settings.SettingsSurfaceList
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppIconButton

object SimplifiedColorRowTestTag {
    fun row(ramp: ColorRamp) = "simplified_color_row_${ramp.name}"
    fun reset(ramp: ColorRamp) = "simplified_color_reset_${ramp.name}"
}

@Composable
fun SimplifiedColorSettingsList(
    baseRampOverrides: Map<ColorRamp, Color>,
    onEditRamp: (ColorRamp) -> Unit,
    onResetRamp: (ColorRamp) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.appsettings_color_simplified_intro),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm)
        )
        SettingsSurfaceList {
            Column(modifier = Modifier.padding(vertical = AppSpacing.xs)) {
                ColorRamp.entries.forEachIndexed { index, ramp ->
                    val override = baseRampOverrides[ramp]
                    val seed = override ?: ColorRampTable.baseSeed(ramp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditRamp(ramp) }
                            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
                            .testTag(SimplifiedColorRowTestTag.row(ramp)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(ramp.labelRes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            ColorRampPreviewStrip(seed = seed, modifier = Modifier.height(20.dp))
                        }
                        if (override != null) {
                            ColorCustomizedBadge()
                            AppIconButton(
                                icon = Icons.Filled.Refresh,
                                contentDescription = stringResource(R.string.appsettings_color_simplified_reset_ramp),
                                role = AppButtonRole.Neutral,
                                iconSize = 18.dp,
                                onClick = { onResetRamp(ramp) },
                                modifier = Modifier.size(32.dp).testTag(SimplifiedColorRowTestTag.reset(ramp)),
                            )
                        }
                    }
                    if (index != ColorRamp.entries.lastIndex) {
                        SettingsRowDivider()
                    }
                }
            }
        }
    }
}
