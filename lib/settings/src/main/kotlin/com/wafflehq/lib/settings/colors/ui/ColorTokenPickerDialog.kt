package com.wafflehq.lib.settings.colors.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.colors.CheckerboardLight
import com.wafflehq.lib.settings.colors.ColorRamp
import com.wafflehq.lib.settings.colors.ColorRampTable
import com.wafflehq.lib.settings.colors.ColorToken
import com.wafflehq.lib.settings.colors.ColorTokenId
import com.wafflehq.lib.settings.colors.ColorTokenRegistry
import com.wafflehq.lib.settings.colors.ColorValue
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.color.AlphaSlider
import com.wafflehq.lib.uicore.color.contrastTextColor
import com.wafflehq.lib.uicore.components.AppBarSurface
import com.wafflehq.lib.uicore.components.AppColorDot
import com.wafflehq.lib.uicore.components.AppTab
import com.wafflehq.lib.uicore.components.AppTabRow
import com.wafflehq.lib.uicore.components.AppVerticalDivider
import com.wafflehq.lib.uicore.scaffold.AppScaffold
import com.wafflehq.lib.uicore.scaffold.AppScaffoldDefaults

private enum class TokenPickerTab { PALETTE, CUSTOM }

typealias ColorTokenPreviewSlot =
    @Composable (token: ColorToken, live: Color, resolved: Map<ColorTokenId, Color>) -> Unit

private fun ColorValue.resolvePreview(baseRampOverrides: Map<ColorRamp, Color>): Color = when (this) {
    is ColorValue.Palette -> ColorRampTable.swatch(ramp, step, baseRampOverrides).copy(alpha = alpha)
    is ColorValue.Custom -> Color(argb)
}

@Composable
fun ColorTokenPickerDialog(
    title: String,
    token: ColorToken,
    registry: ColorTokenRegistry,
    resolvedAll: Map<ColorTokenId, Color>,
    currentValue: ColorValue?,
    defaultPreview: Color,
    onValueSelected: (ColorValue) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    baseRampOverrides: Map<ColorRamp, Color> = emptyMap(),
    idFontFamily: FontFamily? = null,
    preview: ColorTokenPreviewSlot = { _, _, _ -> }
) {
    var pickedValue by remember(currentValue) { mutableStateOf(currentValue) }
    var tab by remember { mutableStateOf(TokenPickerTab.PALETTE) }
    val previewColor = pickedValue?.resolvePreview(baseRampOverrides) ?: defaultPreview
    val beforeColor = currentValue?.resolvePreview(baseRampOverrides) ?: defaultPreview
    val hasNewValue = pickedValue != currentValue
    val contrastCounterpartId = remember(registry, token.id) { registry.contrastCounterpartOrNull(token.id) }
    val contrastCounterpartToken = contrastCounterpartId?.let { registry.byId[it] }
    val contrastCounterpartColor = contrastCounterpartId?.let { resolvedAll[it] } ?: previewColor.contrastTextColor()
    val contrastCounterpartLabel = contrastCounterpartToken?.let { stringResource(it.labelRes) }
    val shortId = registry.shortIds[token.id]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AppScaffold(
            titleContent = {
                Column {
                    Text(title, style = AppScaffoldDefaults.topBarTitleStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (shortId != null) {
                        Text(
                            text = shortId,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = idFontFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
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
                            ColorPreviewSwatch(
                                color = defaultPreview,
                                label = stringResource(R.string.appsettings_color_picker_default_label),
                                onClick = { pickedValue = null }
                            )
                            AppVerticalDivider(modifier = Modifier.height(28.dp))
                            ColorPreviewSwatch(
                                color = beforeColor,
                                label = stringResource(R.string.appsettings_color_picker_before_label),
                                onClick = { pickedValue = currentValue }
                            )
                            if (hasNewValue) {
                                AppVerticalDivider(modifier = Modifier.height(28.dp))
                                ColorPreviewSwatch(
                                    color = previewColor,
                                    label = stringResource(R.string.appsettings_color_picker_after_label),
                                    onClick = null
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
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        ) { padding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val previewMaxHeight = maxHeight * 0.55f
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = previewMaxHeight)
                            .verticalScroll(rememberScrollState())
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
                            counterpart = contrastCounterpartColor,
                            counterpartLabel = contrastCounterpartLabel
                        )
                        preview(token, previewColor, resolvedAll)
                    }
                    AppTabRow(selectedTabIndex = tab.ordinal) {
                        AppTab(
                            selected = tab == TokenPickerTab.PALETTE,
                            onClick = { tab = TokenPickerTab.PALETTE },
                            text = { Text(stringResource(R.string.appsettings_color_picker_tab_palette)) }
                        )
                        AppTab(
                            selected = tab == TokenPickerTab.CUSTOM,
                            onClick = { tab = TokenPickerTab.CUSTOM },
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
                            TokenPickerTab.PALETTE -> {
                                PaletteSwatchGrid(
                                    baseRampOverrides = baseRampOverrides,
                                    onSwatchSelected = { ramp, step ->
                                        val alpha = (pickedValue as? ColorValue.Palette)?.alpha ?: 1f
                                        pickedValue = ColorValue.Palette(ramp, step, alpha)
                                    }
                                )
                                val paletteValue = pickedValue as? ColorValue.Palette
                                Text(
                                    text = stringResource(R.string.appsettings_color_picker_opacity_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AlphaSlider(
                                    color = paletteValue?.let { ColorRampTable.swatch(it.ramp, it.step, baseRampOverrides) } ?: defaultPreview,
                                    alpha = paletteValue?.alpha ?: 1f,
                                    enabled = paletteValue != null,
                                    checkerboardColor = CheckerboardLight,
                                    onAlphaChange = { newAlpha -> paletteValue?.let { pickedValue = it.copy(alpha = newAlpha) } }
                                )
                            }
                            TokenPickerTab.CUSTOM -> CustomColorEditor(
                                color = previewColor,
                                onColorChanged = { color -> pickedValue = ColorValue.Custom(color.toArgb()) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPreviewSwatch(color: Color, label: String, onClick: (() -> Unit)?) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        AppColorDot(
            color = color,
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
