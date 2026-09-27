package com.wafflehq.lib.uicore.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.theme.AppSpacing

@Immutable
class AppPillTab(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val testTag: String? = null,
    val popup: (@Composable () -> Unit)? = null
)

@Immutable
class AppPillTabColors(
    val trackContainer: Color,
    val trackBorder: Color,
    val selectedContainer: Color,
    val selectedContent: Color,
    val selectedBorder: Color,
    val unselectedContainer: Color,
    val unselectedContent: Color,
    val unselectedBorder: Color,
    val disabledContent: Color
)

object AppPillTabDefaults {
    val trackBorderWidth: Dp = 1.dp
    val trackPadding: Dp = AppSpacing.xs
    val tabMinHeight: Dp = 40.dp
    const val DISABLED_CONTENT_ALPHA = 0.38f

    @Composable
    fun colors(
        trackContainer: Color = Color.Transparent,
        trackBorder: Color = MaterialTheme.colorScheme.outline,
        selectedContainer: Color = MaterialTheme.colorScheme.primary,
        selectedContent: Color = MaterialTheme.colorScheme.onPrimary,
        selectedBorder: Color = Color.Transparent,
        unselectedContainer: Color = Color.Transparent,
        unselectedContent: Color = MaterialTheme.colorScheme.onSurface,
        unselectedBorder: Color = Color.Transparent,
        disabledContent: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
    ): AppPillTabColors = AppPillTabColors(
        trackContainer = trackContainer,
        trackBorder = trackBorder,
        selectedContainer = selectedContainer,
        selectedContent = selectedContent,
        selectedBorder = selectedBorder,
        unselectedContainer = unselectedContainer,
        unselectedContent = unselectedContent,
        unselectedBorder = unselectedBorder,
        disabledContent = disabledContent
    )
}

@Composable
fun AppPillTabRow(
    tabs: List<AppPillTab>,
    modifier: Modifier = Modifier,
    colors: AppPillTabColors = AppPillTabDefaults.colors()
) {
    val trackShape = RoundedCornerShape(AppRadius.pill)
    Row(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .clip(trackShape)
            .background(colors.trackContainer)
            .border(AppPillTabDefaults.trackBorderWidth, colors.trackBorder, trackShape)
            .padding(AppPillTabDefaults.trackPadding)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            Box(modifier = Modifier.weight(1f)) {
                AppPillTabItem(tab = tab, colors = colors)
                tab.popup?.invoke()
            }
        }
    }
}

@Composable
fun <T> AppPillSelector(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    colors: AppPillTabColors = AppPillTabDefaults.colors(),
    enabled: Boolean = true,
    testTag: (T) -> String? = { null }
) {
    val tabs = options.map { option ->
        AppPillTab(
            label = label(option),
            selected = option == selected,
            onClick = { onSelect(option) },
            enabled = enabled,
            testTag = testTag(option)
        )
    }
    AppPillTabRow(tabs = tabs, modifier = modifier, colors = colors)
}

@Composable
private fun AppPillTabItem(tab: AppPillTab, colors: AppPillTabColors) {
    val shape = RoundedCornerShape(AppRadius.pill)
    val container by animateColorAsState(
        targetValue = if (tab.selected) colors.selectedContainer else colors.unselectedContainer,
        label = "pillTabContainer"
    )
    val content by animateColorAsState(
        targetValue = when {
            !tab.enabled -> colors.disabledContent
            tab.selected -> colors.selectedContent
            else -> colors.unselectedContent
        },
        label = "pillTabContent"
    )
    val border = if (tab.selected) colors.selectedBorder else colors.unselectedBorder
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AppPillTabDefaults.tabMinHeight)
            .clip(shape)
            .background(container)
            .border(AppPillTabDefaults.trackBorderWidth, border, shape)
            .selectable(
                selected = tab.selected,
                enabled = tab.enabled,
                role = Role.RadioButton,
                onClick = tab.onClick
            )
            .then(if (tab.testTag != null) Modifier.testTag(tab.testTag) else Modifier)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (tab.selected) FontWeight.Bold else FontWeight.Medium,
            color = content,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
