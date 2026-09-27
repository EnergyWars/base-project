package com.wafflehq.lib.settings.colors.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.lib.navigation.R as NavR
import com.wafflehq.lib.navigation.settings.SettingsScaffold
import com.wafflehq.lib.navigation.settings.SettingsSurfaceList
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.colors.ColorSettingsController
import com.wafflehq.lib.settings.colors.ColorToken
import com.wafflehq.lib.settings.colors.ColorTokenId
import com.wafflehq.lib.settings.colors.ColorTokenRegistry
import com.wafflehq.lib.settings.colors.ColorTokenResolver
import com.wafflehq.lib.settings.colors.LocalIsDarkTheme
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.components.AppColorDot
import com.wafflehq.lib.uicore.components.AppDialog

@Composable
fun ColorCategoryScreen(
    categoryKey: String,
    controller: ColorSettingsController,
    registry: ColorTokenRegistry,
    onBack: () -> Unit,
    isDark: Boolean = LocalIsDarkTheme.current,
    hiddenTokenIds: Set<ColorTokenId> = emptySet(),
    idFontFamily: FontFamily? = null,
    preview: ColorTokenPreviewSlot = { _, _, _ -> }
) {
    val overrides by controller.overrides(isDark).collectAsStateWithLifecycle()
    val baseRampOverrides by controller.baseRampOverrides.collectAsStateWithLifecycle()
    val category = registry.categoryByKey[categoryKey]
    val tokens = registry.tokensOf(categoryKey).filter { it.id !in hiddenTokenIds }
    var editingToken by remember { mutableStateOf<ColorToken?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }
    val resolvedInCategory = tokens.associate { it.id to ColorTokenResolver.resolve(it, overrides[it.id], isDark, baseRampOverrides) }

    val resolvedAll = ColorTokenResolver.resolveAll(registry, overrides, isDark, baseRampOverrides)

    editingToken?.let { token ->
        ColorTokenPickerDialog(
            title = stringResource(token.labelRes),
            token = token,
            registry = registry,
            resolvedAll = resolvedAll,
            currentValue = overrides[token.id],
            defaultPreview = ColorTokenResolver.resolve(token, null, isDark, baseRampOverrides),
            baseRampOverrides = baseRampOverrides,
            idFontFamily = idFontFamily,
            preview = preview,
            onValueSelected = { value ->
                controller.setOverride(token.id, isDark, value)
                editingToken = null
            },
            onReset = {
                controller.setOverride(token.id, isDark, null)
                editingToken = null
            },
            onDismiss = { editingToken = null }
        )
    }

    ColorThemeNameRequestHost(controller)

    if (showResetConfirm) {
        AppDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(stringResource(R.string.appsettings_color_reset_category_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.appsettings_color_reset_category_confirm_message,
                        stringResource(
                            if (isDark) R.string.appsettings_color_mode_dark else R.string.appsettings_color_mode_light
                        )
                    )
                )
            },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_color_reset_token),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    onClick = {
                        controller.resetCategory(categoryKey, isDark)
                        showResetConfirm = false
                    },
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = { showResetConfirm = false },
                )
            }
        )
    }

    SettingsScaffold(
        title = category?.let { stringResource(it.labelRes) }.orEmpty(),
        onBack = onBack,
        backDescription = stringResource(NavR.string.navigation_navigate_back)
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    Icon(
                        if (isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(
                            if (isDark) {
                                R.string.appsettings_color_mode_banner_dark
                            } else {
                                R.string.appsettings_color_mode_banner_light
                            }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                SettingsSurfaceList {
                    Column(modifier = Modifier.padding(vertical = AppSpacing.xs)) {
                        tokens.forEach { token ->
                            val resolved = resolvedInCategory.getValue(token.id)
                            val isCustomized = overrides[token.id] != null
                            val shortId = registry.shortIds[token.id]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editingToken = token }
                                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
                                    .testTag(ColorTokenRowTestTag.row(token.id)),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(token.labelRes),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (shortId != null) {
                                        Text(
                                            text = shortId,
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = idFontFamily),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.testTag(ColorTokenRowTestTag.id(token.id))
                                        )
                                    }
                                }
                                ColorTokenInfoButton(
                                    description = stringResource(token.descriptionRes),
                                    testTag = ColorTokenRowTestTag.info(token.id)
                                )
                                if (isCustomized) {
                                    ColorCustomizedBadge()
                                }
                                AppColorDot(
                                    color = resolved,
                                    size = 28.dp,
                                    borderColor = if (isCustomized) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                                    borderWidth = if (isCustomized) 2.dp else 1.dp,
                                )
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(AppSpacing.lg),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AppButton(
                        text = stringResource(R.string.appsettings_color_reset_category),
                        role = AppButtonRole.Neutral,
                        variant = AppButtonVariant.Text,
                        onClick = { showResetConfirm = true },
                    )
                }
            }
        }
    }
}

object ColorTokenRowTestTag {
    fun row(id: ColorTokenId) = "color_token_row_${id.value}"
    fun id(id: ColorTokenId) = "color_token_id_${id.value}"
    fun info(id: ColorTokenId) = "color_token_info_${id.value}"
}

@Composable
internal fun ColorTokenInfoButton(description: String, testTag: String) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        AppIconButton(
            icon = Icons.Outlined.Info,
            contentDescription = stringResource(R.string.appsettings_color_token_info_description),
            role = AppButtonRole.Neutral,
            iconSize = 18.dp,
            onClick = { expanded = true },
            modifier = Modifier.size(32.dp).testTag(testTag),
        )
        if (expanded) {
            Popup(
                alignment = Alignment.TopEnd,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.background,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .widthIn(max = 260.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { expanded = false }
                ) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(AppSpacing.md)
                    )
                }
            }
        }
    }
}
