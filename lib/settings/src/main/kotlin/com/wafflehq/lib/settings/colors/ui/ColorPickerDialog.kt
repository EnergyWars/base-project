package com.wafflehq.lib.settings.colors.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.colors.CheckerboardLight
import com.wafflehq.lib.settings.colors.ColorRampTable
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.color.*
import com.wafflehq.lib.uicore.components.AppColorDot
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.components.AppDialogDefaults
import com.wafflehq.lib.uicore.components.AppPillSelector
import com.wafflehq.lib.uicore.components.AppTab
import com.wafflehq.lib.uicore.components.AppTabRow
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.components.rememberGuardedDismiss

private enum class ColorPickerTab { PALETTE, CUSTOM }
private enum class CustomColorInputMode { FREE, HEX, RGB, HSL }

@Composable
fun ColorPickerDialog(
    title: String,
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var pickedColor by remember(initialColor) { mutableStateOf(initialColor) }
    var tab by remember { mutableStateOf(ColorPickerTab.PALETTE) }
    var hasChanges by remember { mutableStateOf(false) }
    val guardedDismiss = rememberGuardedDismiss(hasChanges = hasChanges, onDismiss = onDismiss)

    AppDialog(
        onDismissRequest = guardedDismiss,
        properties = AppDialogDefaults.properties,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                AppTabRow(selectedTabIndex = tab.ordinal) {
                    AppTab(
                        selected = tab == ColorPickerTab.PALETTE,
                        onClick = { tab = ColorPickerTab.PALETTE },
                        text = { Text(stringResource(R.string.appsettings_color_picker_tab_palette)) }
                    )
                    AppTab(
                        selected = tab == ColorPickerTab.CUSTOM,
                        onClick = { tab = ColorPickerTab.CUSTOM },
                        text = { Text(stringResource(R.string.appsettings_color_picker_tab_custom)) }
                    )
                }
                when (tab) {
                    ColorPickerTab.PALETTE -> PaletteSwatchGrid(
                        onSwatchSelected = { ramp, step ->
                            pickedColor = ColorRampTable.swatch(ramp, step)
                        }
                    )
                    ColorPickerTab.CUSTOM -> CustomColorEditor(
                        color = pickedColor,
                        onColorChanged = { pickedColor = it },
                        onTextChanged = { hasChanges = true }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    AppColorDot(
                        color = initialColor,
                        size = 32.dp,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    )
                    AppColorDot(
                        color = pickedColor,
                        size = 32.dp,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = pickedColor.toHexArgb(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            AppButton(
                text = stringResource(UiCoreR.string.uicore_save),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Text,
                onClick = { onColorSelected(pickedColor) },
            )
        },
        dismissButton = {
            AppButton(
                text = stringResource(UiCoreR.string.uicore_cancel),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                onClick = onDismiss,
            )
        }
    )
}

@Composable
internal fun CustomColorEditor(
    color: Color,
    onColorChanged: (Color) -> Unit,
    onTextChanged: () -> Unit = {}
) {
    var mode by remember { mutableStateOf(CustomColorInputMode.FREE) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppPillSelector(
            options = CustomColorInputMode.entries,
            selected = mode,
            onSelect = { mode = it },
            label = { candidate ->
                when (candidate) {
                    CustomColorInputMode.FREE -> stringResource(R.string.appsettings_color_picker_mode_free)
                    CustomColorInputMode.HEX -> stringResource(R.string.appsettings_color_picker_mode_hex)
                    CustomColorInputMode.RGB -> stringResource(R.string.appsettings_color_picker_mode_rgb)
                    CustomColorInputMode.HSL -> stringResource(R.string.appsettings_color_picker_mode_hsl)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        when (mode) {
            CustomColorInputMode.FREE -> ColorCanvasPicker(initialColor = color, onColorChanged = onColorChanged, checkerboardColor = CheckerboardLight)
            CustomColorInputMode.HEX -> HexColorEditor(color, onColorChanged, onTextChanged)
            CustomColorInputMode.RGB -> RgbColorEditor(color, onColorChanged, onTextChanged)
            CustomColorInputMode.HSL -> HslColorEditor(color, onColorChanged, onTextChanged)
        }
    }
}

@Composable
private fun HexColorEditor(
    color: Color,
    onColorChanged: (Color) -> Unit,
    onTextChanged: () -> Unit = {}
) {
    var text by remember { mutableStateOf(color.toHexArgb()) }
    var lastEmitted by remember { mutableStateOf<Color?>(null) }
    LaunchedEffect(color) {
        if (color != lastEmitted) text = color.toHexArgb()
    }
    AppTextField(
        value = text,
        onValueChange = { input ->
            text = input
            onTextChanged()
            hexToColorOrNull(input)?.let {
                lastEmitted = it
                onColorChanged(it)
            }
        },
        label = { Text(stringResource(R.string.appsettings_color_picker_hex_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RgbColorEditor(
    color: Color,
    onColorChanged: (Color) -> Unit,
    onTextChanged: () -> Unit = {}
) {
    var r by remember { mutableStateOf(color.toRgb().r.toString()) }
    var g by remember { mutableStateOf(color.toRgb().g.toString()) }
    var b by remember { mutableStateOf(color.toRgb().b.toString()) }
    var alpha by remember { mutableStateOf(color.alpha) }
    var lastEmitted by remember { mutableStateOf<Color?>(null) }
    LaunchedEffect(color) {
        if (color != lastEmitted) {
            val rgb = color.toRgb()
            r = rgb.r.toString(); g = rgb.g.toString(); b = rgb.b.toString()
            alpha = color.alpha
        }
    }

    fun emit() {
        val ri = r.toIntOrNull()?.coerceIn(0, 255)
        val gi = g.toIntOrNull()?.coerceIn(0, 255)
        val bi = b.toIntOrNull()?.coerceIn(0, 255)
        if (ri != null && gi != null && bi != null) {
            val next = Rgb(ri, gi, bi).toColor(alpha)
            lastEmitted = next
            onColorChanged(next)
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        AppTextField(
            value = r, onValueChange = { r = it; onTextChanged(); emit() },
            label = { Text(stringResource(R.string.appsettings_color_picker_rgb_r)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, modifier = Modifier.weight(1f)
        )
        AppTextField(
            value = g, onValueChange = { g = it; onTextChanged(); emit() },
            label = { Text(stringResource(R.string.appsettings_color_picker_rgb_g)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, modifier = Modifier.weight(1f)
        )
        AppTextField(
            value = b, onValueChange = { b = it; onTextChanged(); emit() },
            label = { Text(stringResource(R.string.appsettings_color_picker_rgb_b)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, modifier = Modifier.weight(1f)
        )
    }
    Text(
        text = stringResource(R.string.appsettings_color_picker_opacity_label),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    AlphaSlider(
        color = color.copy(alpha = 1f),
        alpha = alpha,
        checkerboardColor = CheckerboardLight,
        onAlphaChange = { alpha = it; emit() }
    )
}

@Composable
private fun HslColorEditor(
    color: Color,
    onColorChanged: (Color) -> Unit,
    onTextChanged: () -> Unit = {}
) {
    var h by remember { mutableStateOf(color.toHsl().h.toInt().toString()) }
    var s by remember { mutableStateOf(color.toHsl().s.toInt().toString()) }
    var l by remember { mutableStateOf(color.toHsl().l.toInt().toString()) }
    var alpha by remember { mutableStateOf(color.alpha) }
    var lastEmitted by remember { mutableStateOf<Color?>(null) }
    LaunchedEffect(color) {
        if (color != lastEmitted) {
            val hsl = color.toHsl()
            h = hsl.h.toInt().toString(); s = hsl.s.toInt().toString(); l = hsl.l.toInt().toString()
            alpha = color.alpha
        }
    }

    fun emit() {
        val hf = h.toFloatOrNull()?.coerceIn(0f, 360f)
        val sf = s.toFloatOrNull()?.coerceIn(0f, 100f)
        val lf = l.toFloatOrNull()?.coerceIn(0f, 100f)
        if (hf != null && sf != null && lf != null) {
            val next = Hsl(hf, sf, lf).toColor(alpha)
            lastEmitted = next
            onColorChanged(next)
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        AppTextField(
            value = h, onValueChange = { h = it; onTextChanged(); emit() },
            label = { Text(stringResource(R.string.appsettings_color_picker_hsl_h)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, modifier = Modifier.weight(1f)
        )
        AppTextField(
            value = s, onValueChange = { s = it; onTextChanged(); emit() },
            label = { Text(stringResource(R.string.appsettings_color_picker_hsl_s)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, modifier = Modifier.weight(1f)
        )
        AppTextField(
            value = l, onValueChange = { l = it; onTextChanged(); emit() },
            label = { Text(stringResource(R.string.appsettings_color_picker_hsl_l)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, modifier = Modifier.weight(1f)
        )
    }
    Text(
        text = stringResource(R.string.appsettings_color_picker_opacity_label),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    AlphaSlider(
        color = color.copy(alpha = 1f),
        alpha = alpha,
        checkerboardColor = CheckerboardLight,
        onAlphaChange = { alpha = it; emit() }
    )
}
