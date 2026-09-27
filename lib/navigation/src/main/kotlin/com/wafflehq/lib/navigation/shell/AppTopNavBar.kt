package com.wafflehq.lib.navigation.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.button.AppIconButtonVariant
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.theme.AppSpacing

data class AppNavAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val selected: Boolean = false
)

private val ShortcutSize = 44.dp
private val ShortcutItemWidth = 52.dp
private val HomeButtonWidth = 108.dp
private val TopActionSize = 48.dp
private val TitleRowMinHeight = 68.dp
private val BottomEdgeThickness = 1.dp
private const val BOTTOM_EDGE_ALPHA = 0.5f

@Composable
fun AppTopNavBar(
    title: String,
    menu: AppNavAction,
    search: AppNavAction,
    settings: AppNavAction,
    home: AppNavItem,
    shortcuts: List<AppNavItem>,
    modifier: Modifier = Modifier,
    statusText: String? = null,
    windowInsets: WindowInsets = WindowInsets.statusBars,
    colors: AppNavColors = appNavColors()
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.topBarBackground)
            .drawBehind {
                val thickness = BottomEdgeThickness.toPx()
                val y = size.height - thickness / 2f
                drawLine(
                    color = colors.topBarDivider.copy(alpha = BOTTOM_EDGE_ALPHA),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = thickness
                )
            }
            .windowInsetsPadding(windowInsets)
            .testTag(AppNavigationTestTags.TOP_BAR)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = TitleRowMinHeight)
                .padding(horizontal = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopBarPlainAction(menu, colors, AppNavigationTestTags.TOP_BAR_MENU)
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.topBarSelectedLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppSpacing.sm)
                    .testTag(AppNavigationTestTags.TOP_BAR_TITLE)
            )
            TopBarPlainAction(search, colors, AppNavigationTestTags.TOP_BAR_SEARCH)
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                Box(modifier = Modifier.padding(horizontal = AppSpacing.sm)) {
                    TopBarCircleButton(
                        icon = settings.icon,
                        label = settings.contentDescription,
                        selected = settings.selected,
                        enabled = settings.enabled,
                        colors = colors,
                        onClick = settings.onClick,
                        modifier = Modifier.testTag(AppNavigationTestTags.TOP_BAR_SETTINGS),
                        size = TopActionSize
                    )
                }
            }
        }
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(
                        start = AppSpacing.md,
                        end = AppSpacing.md,
                        bottom = if (statusText != null) AppSpacing.sm else AppSpacing.md
                    ),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.Top
            ) {
                shortcuts.take(MAX_NAV_SHORTCUTS).forEach { item ->
                    TopBarShortcut(item, colors)
                }
                Spacer(Modifier.weight(1f))
                TopBarHomeButton(home, colors, Modifier.width(HomeButtonWidth).fillMaxHeight())
            }
        }
        if (statusText != null) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = colors.topBarUnselectedLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = AppSpacing.lg, end = AppSpacing.lg, bottom = AppSpacing.md)
                    .testTag(AppNavigationTestTags.TOP_BAR_STATUS)
            )
        }
    }
}

@Composable
private fun TopBarPlainAction(action: AppNavAction, colors: AppNavColors, tag: String) {
    AppIconButton(
        icon = action.icon,
        contentDescription = action.contentDescription,
        containerColor = colors.topBarSelectedLabel,
        contentColor = colors.topBarSelectedLabel,
        onClick = action.onClick,
        enabled = action.enabled,
        modifier = Modifier.size(TopActionSize).testTag(tag)
    )
}

@Composable
private fun TopBarShortcut(item: AppNavItem, colors: AppNavColors) {
    Column(
        modifier = Modifier.width(ShortcutItemWidth),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBarCircleButton(
            icon = item.icon,
            label = item.label,
            selected = item.selected,
            enabled = item.enabled,
            colors = colors,
            onClick = item.onClick,
            modifier = Modifier.testTag(AppNavigationTestTags.topBarShortcut(item.id))
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (item.selected) colors.topBarSelectedLabel else colors.topBarUnselectedLabel,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.xs)
                .testTag(AppNavigationTestTags.topBarShortcutLabel(item.id))
        )
    }
}

@Composable
private fun TopBarHomeButton(item: AppNavItem, colors: AppNavColors, modifier: Modifier) {
    val buttonModifier = modifier.testTag(AppNavigationTestTags.TOP_BAR_HOME)
    val padding = PaddingValues(horizontal = AppSpacing.sm)
    if (item.selected) {
        AppButton(
            text = item.label,
            containerColor = colors.topBarSelectedPillBackground,
            contentColor = colors.topBarSelectedIcon,
            onClick = item.onClick,
            modifier = buttonModifier,
            variant = AppButtonVariant.Filled,
            enabled = item.enabled,
            leadingIcon = item.icon,
            contentPadding = padding,
            singleLine = true
        )
    } else {
        AppButton(
            text = item.label,
            containerColor = colors.topBarUnselectedLabel,
            contentColor = colors.topBarUnselectedLabel,
            onClick = item.onClick,
            modifier = buttonModifier.border(
                width = 1.dp,
                color = colors.topBarDivider,
                shape = RoundedCornerShape(AppRadius.button)
            ),
            variant = AppButtonVariant.Text,
            enabled = item.enabled,
            leadingIcon = item.icon,
            contentPadding = padding,
            singleLine = true
        )
    }
}

@Composable
private fun TopBarCircleButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    enabled: Boolean,
    colors: AppNavColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = ShortcutSize
) {
    val buttonModifier = modifier.size(size)
    if (selected) {
        AppIconButton(
            icon = icon,
            contentDescription = label,
            containerColor = colors.topBarSelectedPillBackground,
            contentColor = colors.topBarSelectedIcon,
            onClick = onClick,
            modifier = buttonModifier,
            variant = AppIconButtonVariant.Filled,
            enabled = enabled,
            shape = CircleShape
        )
    } else {
        AppIconButton(
            icon = icon,
            contentDescription = label,
            containerColor = colors.topBarUnselectedIcon,
            contentColor = colors.topBarUnselectedIcon,
            onClick = onClick,
            modifier = buttonModifier.border(1.dp, colors.topBarDivider, CircleShape),
            variant = AppIconButtonVariant.Standard,
            enabled = enabled,
            shape = CircleShape
        )
    }
}
