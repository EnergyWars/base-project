package com.wafflehq.lib.navigation.settings

import com.wafflehq.lib.uicore.components.AppEmptyState
import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.animation.AnimatedVisibility
import com.wafflehq.lib.uicore.components.AppBanner
import com.wafflehq.lib.uicore.components.AppBannerVariant
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.components.AppDialogDefaults
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FabPosition
import com.wafflehq.lib.uicore.scaffold.AppScaffold
import androidx.compose.material3.Text
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.menu.AppDropdownMenu
import com.wafflehq.lib.uicore.menu.AppDropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.components.AppCard
import com.wafflehq.lib.uicore.components.AppCardDefaults
import com.wafflehq.lib.uicore.components.AppSlider
import com.wafflehq.lib.uicore.components.AppSwitch
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppDividerTone
import androidx.compose.foundation.border

private val pill = RoundedCornerShape(AppRadius.pill)

private const val DROPDOWN_VALUE_MAX_WIDTH_FRACTION = 0.6f

@Composable
fun SettingsScaffold(
    title: String,
    onBack: (() -> Unit)?,
    backDescription: String? = null,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.background,
    topBarContainerColor: Color = MaterialTheme.colorScheme.surface,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    content: @Composable (PaddingValues) -> Unit,
) {
    AppScaffold(
        title = title,
        onBack = onBack,
        backDescription = backDescription,
        modifier = modifier,
        containerColor = containerColor,
        topBarContainerColor = topBarContainerColor,
        actions = actions,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        content = content,
    )
}

@Composable
fun SettingsNavRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    highlighted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val titleColor = if (highlighted) colors.primary else colors.onSurface
    val subtitleColor = if (highlighted) colors.primary else colors.onSurfaceVariant
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (highlighted) colors.primaryContainer else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(AppSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (leading != null) leading()
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = if (highlighted) {
                    MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                } else {
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                },
                color = titleColor,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor,
                )
            }
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = subtitleColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun SettingsSurfaceList(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        content()
    }
}

@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    label: String? = null,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth().padding(AppSpacing.lg)) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = LocalSettingsTypography.current.labelFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.7.sp,
                ),
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 2.dp, bottom = 10.dp),
            )
        }
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = { content() },
            )
        }
    }
}

@Composable
fun CollapsibleSettingsGroup(
    label: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "detailsChevronRotation",
    )
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(chevronRotation),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                modifier = Modifier.weight(1f).padding(start = 10.dp),
            )
        }
        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = { content() },
            )
        }
        SettingsGroupDivider()
    }
}

@Composable
fun SettingsGroupDivider() {
    AppHorizontalDivider(tone = AppDividerTone.Subtle)
}

@Composable
fun SettingsRowDivider() {
    AppHorizontalDivider(tone = AppDividerTone.Subtle)
}

@Composable
fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = LocalSettingsTypography.current.labelFontFamily,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.7.sp,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = AppSpacing.lg, top = AppSpacing.lg, bottom = AppSpacing.sm),
    )
}

@Composable
fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val titleColor = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = 0.38f)
    val subColor = if (enabled) colors.onSurfaceVariant else colors.onSurfaceVariant.copy(alpha = 0.38f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = titleColor,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subColor,
                )
            }
        }
        AppSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun SettingsRowScaffold(
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    trailing: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val titleColor = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = 0.38f)
    val subColor = if (enabled) colors.onSurfaceVariant else colors.onSurfaceVariant.copy(alpha = 0.38f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = titleColor,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subColor,
                )
            }
        }
        trailing()
    }
}

@Composable
fun SettingsDropdownField(
    label: String,
    value: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val valueMaxWidth = maxWidth * DROPDOWN_VALUE_MAX_WIDTH_FRACTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.weight(1f),
            )
            Box(modifier = Modifier.widthIn(max = valueMaxWidth)) {
                Row(
                    modifier = Modifier
                        .clip(pill)
                        .border(1.dp, colors.outline.copy(alpha = 0.4f), pill)
                        .background(colors.surface)
                        .clickable(enabled = enabled) { expanded = true }
                        .padding(start = 14.dp, end = 10.dp, top = AppSpacing.sm, bottom = AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.onSurface,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
                AppDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    options.forEachIndexed { index, option ->
                        AppDropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Medium,
                                )
                            },
                            selected = index == selectedIndex,
                            onClick = {
                                onSelect(index)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSliderControl(
    label: String,
    valueText: (Float) -> String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    var sliderValue by remember(value) { mutableFloatStateOf(value) }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = valueText(sliderValue),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = LocalSettingsTypography.current.labelFontFamily),
                color = colors.onSurfaceVariant,
            )
        }
        AppSlider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChange(sliderValue) },
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
        )
    }
}

@Composable
fun SettingsBanner(
    icon: ImageVector,
    title: String,
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: (() -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    trailingDescription: String? = null,
) {
    AppBanner(
        text = text,
        containerColor = containerColor,
        contentColor = contentColor,
        variant = AppBannerVariant.Card,
        title = title,
        icon = icon,
        onClick = onClick,
        action = if (trailingIcon != null) {
            {
                Icon(trailingIcon, contentDescription = trailingDescription, tint = contentColor, modifier = Modifier.size(18.dp))
            }
        } else {
            null
        },
    )
}

private val listPillShape = RoundedCornerShape(AppRadius.pill)

@Composable
fun ListCard(content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    AppCard(
        containerColor = colors.background,
        borderColor = colors.outline.copy(alpha = AppCardDefaults.BORDER_ALPHA),
        modifier = Modifier.fillMaxWidth(),
        content = content,
    )
}

@Composable
fun ListHead(title: String, count: String? = null) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .heightIn(min = 56.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.onSurface,
        )
        if (count != null) {
            Text(count, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
    AppHorizontalDivider(tone = AppDividerTone.Subtle)
}

@Composable
fun ListSearchField(query: String, onQuery: (String) -> Unit, placeholder: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = AppSpacing.md)
            .clip(listPillShape)
            .border(1.5.dp, colors.outline.copy(alpha = 0.4f), listPillShape)
            .background(colors.surface)
            .padding(horizontal = AppSpacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                singleLine = true,
                textStyle = LocalSettingsTypography.current.inputStyle(MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface)),
                cursorBrush = SolidColor(colors.primary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                    innerTextField()
                },
            )
        }
    }
}

@Composable
fun ListItemRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        content = content,
    )
}

@Composable
fun RowScope.ListRowBody(title: String, sub: String? = null) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (sub != null) {
            Text(
                sub,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun IntroIconBadge(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
fun ModuleIntroDialog(
    icon: ImageVector,
    name: String,
    description: String,
    dismissLabel: String,
    onDismiss: () -> Unit,
    permissionHint: String? = null,
) {
    AppDialog(
        onDismissRequest = onDismiss,
        properties = AppDialogDefaults.properties,
        icon = { IntroIconBadge(icon) },
        title = {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                if (permissionHint != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = permissionHint,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
        confirmButton = {
            AppButton(
                text = dismissLabel,
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Text,
                onClick = onDismiss,
            )
        },
    )
}

@Composable
fun ListEmpty(text: String) {
    AppEmptyState(text = text, modifier = Modifier.fillMaxWidth())
}
