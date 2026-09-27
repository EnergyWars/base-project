package com.wafflehq.lib.settings.colors.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.colors.ColorRampTable
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.color.contrastTextColor
import com.wafflehq.lib.uicore.components.AppBarSurface
import com.wafflehq.lib.uicore.components.AppColorDot
import com.wafflehq.lib.uicore.components.AppTab
import com.wafflehq.lib.uicore.components.AppTabRow
import com.wafflehq.lib.uicore.components.AppVerticalDivider
import com.wafflehq.lib.uicore.scaffold.AppScaffold

private enum class RampPickerTab { PALETTE, CUSTOM }

object BaseRampEditorTestTag {
    const val PREVIEW_STRIP = "base_ramp_editor_preview_strip"
    const val SAVE_BUTTON = "base_ramp_editor_save_button"
    const val DEFAULT_SWATCH = "base_ramp_editor_default_swatch"
    const val BEFORE_SWATCH = "base_ramp_editor_before_swatch"
    const val AFTER_SWATCH = "base_ramp_editor_after_swatch"
}

@Composable
fun BaseRampEditorDialog(
    title: String,
    currentValue: Color?,
    defaultSeed: Color,
    onValueSelected: (Color) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var pickedValue by remember(currentValue) { mutableStateOf(currentValue) }
    var tab by remember { mutableStateOf(RampPickerTab.PALETTE) }
    val previewColor = pickedValue ?: defaultSeed
    val beforeColor = currentValue ?: defaultSeed
    val hasNewValue = pickedValue != currentValue

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AppScaffold(
            title = title,
            onBack = onDismiss,
            navigationIcon = Icons.Filled.Close,
            backDescription = stringResource(UiCoreR.string.uicore_close),
            bottomBar = {
                AppBarSurface {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(28.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            RampColorSwatch(
                                color = defaultSeed,
                                label = stringResource(R.string.appsettings_color_picker_default_label),
                                onClick = { pickedValue = null },
                                testTag = BaseRampEditorTestTag.DEFAULT_SWATCH
                            )
                            AppVerticalDivider(modifier = Modifier.height(28.dp))
                            RampColorSwatch(
                                color = beforeColor,
                                label = stringResource(R.string.appsettings_color_picker_before_label),
                                onClick = { pickedValue = currentValue },
                                testTag = BaseRampEditorTestTag.BEFORE_SWATCH
                            )
                            if (hasNewValue) {
                                AppVerticalDivider(modifier = Modifier.height(28.dp))
                                RampColorSwatch(
                                    color = previewColor,
                                    label = stringResource(R.string.appsettings_color_picker_after_label),
                                    onClick = null,
                                    testTag = BaseRampEditorTestTag.AFTER_SWATCH
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            AppButton(
                                text = stringResource(UiCoreR.string.uicore_cancel),
                                role = AppButtonRole.Neutral,
                                variant = AppButtonVariant.Outlined,
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                            )
                            AppButton(
                                text = stringResource(UiCoreR.string.uicore_save),
                                role = AppButtonRole.Primary,
                                onClick = {
                                    val value = pickedValue
                                    if (value == null) onReset() else onValueSelected(value)
                                },
                                modifier = Modifier.weight(1f).testTag(BaseRampEditorTestTag.SAVE_BUTTON),
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    Text(
                        text = stringResource(R.string.appsettings_color_picker_preview_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ColorContrastBadge(
                        color = previewColor,
                        counterpart = previewColor.contrastTextColor(),
                        counterpartLabel = null
                    )
                    ColorRampPreviewStrip(
                        seed = previewColor,
                        modifier = Modifier.height(40.dp),
                        testTag = BaseRampEditorTestTag.PREVIEW_STRIP
                    )
                }
                AppTabRow(selectedTabIndex = tab.ordinal) {
                    AppTab(
                        selected = tab == RampPickerTab.PALETTE,
                        onClick = { tab = RampPickerTab.PALETTE },
                        text = { Text(stringResource(R.string.appsettings_color_picker_tab_palette)) }
                    )
                    AppTab(
                        selected = tab == RampPickerTab.CUSTOM,
                        onClick = { tab = RampPickerTab.CUSTOM },
                        text = { Text(stringResource(R.string.appsettings_color_picker_tab_custom)) }
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(AppSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    when (tab) {
                        RampPickerTab.PALETTE -> PaletteSwatchGrid(
                            onSwatchSelected = { ramp, step -> pickedValue = ColorRampTable.swatch(ramp, step) }
                        )
                        RampPickerTab.CUSTOM -> CustomColorEditor(
                            color = previewColor,
                            onColorChanged = { color -> pickedValue = color }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RampColorSwatch(color: Color, label: String, onClick: (() -> Unit)?, testTag: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        AppColorDot(
            color = color,
            modifier = Modifier.testTag(testTag),
            onClick = onClick,
            size = 28.dp,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
