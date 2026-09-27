package com.wafflehq.base.ui.library.demos

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.uicore.button.LocalAppButtonWarningColors
import com.wafflehq.lib.uicore.components.AppCard
import com.wafflehq.lib.uicore.components.AppStatusPill
import com.wafflehq.lib.uicore.components.AppStepper
import com.wafflehq.lib.uicore.components.AppSwitch
import com.wafflehq.lib.uicore.theme.AppSpacing

internal object DemoTags {
    fun section(id: String) = "libex_section_$id"
}

@Composable
internal fun DemoSection(
    id: String,
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    @StringRes moduleRes: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCard(modifier = modifier.fillMaxWidth().testTag(DemoTags.section(id))) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                AppStatusPill(
                    text = stringResource(moduleRes),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = stringResource(descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

@Composable
internal fun DemoBodyText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Composable
internal fun DemoMetaText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
internal fun DemoLabelText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

@Composable
internal fun DemoKeyValue(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
internal fun DemoSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    tag: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        AppSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = if (tag != null) Modifier.testTag(tag) else Modifier,
        )
    }
}

internal enum class DemoTone { Success, Error, Warning, Neutral, Info }

@Composable
internal fun DemoStatusPill(text: String, tone: DemoTone, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val warning = LocalAppButtonWarningColors.current
    val container = when (tone) {
        DemoTone.Success -> scheme.secondaryContainer
        DemoTone.Error -> scheme.errorContainer
        DemoTone.Warning -> warning.warningContainer
        DemoTone.Neutral -> scheme.surfaceVariant
        DemoTone.Info -> scheme.primaryContainer
    }
    val content = when (tone) {
        DemoTone.Success -> scheme.secondary
        DemoTone.Error -> scheme.error
        DemoTone.Warning -> warning.warning
        DemoTone.Neutral -> scheme.onSurfaceVariant
        DemoTone.Info -> scheme.primary
    }
    AppStatusPill(text = text, containerColor = container, contentColor = content, modifier = modifier)
}

@Composable
internal fun DemoStepperRow(
    label: String,
    value: Int,
    range: IntRange,
    step: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tag: String? = null,
    valueText: String = value.toString(),
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        AppStepper(
            value = value,
            onValueChange = onValueChange,
            range = range,
            step = step,
            valueText = valueText,
            modifier = if (tag != null) Modifier.testTag(tag) else Modifier,
        )
    }
}
