package com.wafflehq.lib.navigation.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wafflehq.lib.navigation.R
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.components.AppBanner
import com.wafflehq.lib.uicore.components.AppBannerRole
import com.wafflehq.lib.uicore.components.AppBannerVariant
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppCheckbox
import com.wafflehq.lib.uicore.components.AppDividerTone

private const val DRAWER_WIDTH_FRACTION = 0.88f
private const val DRAWER_MAX_WIDTH_DP = 440
private const val PINNED_TILES_PER_ROW = 4
private const val TILE_CONTAINER_ALPHA = 0.16f
private const val BORDER_ALPHA = 0.4f
private const val DISABLED_ALPHA = 0.38f
private val IconTileSize = 40.dp
private val HeaderTileSize = 44.dp
private val PinnedTileHeight = 76.dp
private val RowMinHeight = 56.dp
private val PinBadgeSize = 14.dp

@Immutable
data class AppNavFooterAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)

@Composable
fun AppSideNavDrawer(
    title: String,
    sections: List<AppNavSection>,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    headerIcon: ImageVector? = null,
    searchPlaceholder: String = "",
    pinnedLabel: String = "",
    closeDescription: String? = null,
    onClose: (() -> Unit)? = null,
    footerItem: AppNavItem? = null,
    footerAction: AppNavFooterAction? = null,
    metaTextStyle: TextStyle = MaterialTheme.typography.labelMedium,
    colors: AppNavColors = appNavColors(),
    sectionOverrides: List<String>? = null,
    onSectionOverridesChange: ((List<String>) -> Unit)? = null,
    showHidden: Boolean = false,
    onShowHiddenChange: ((Boolean) -> Unit)? = null
) {
    var query by rememberSaveable { mutableStateOf("") }
    var localOverrides by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val overrides = sectionOverrides ?: localOverrides
    val searching = query.isNotBlank()
    val anyHidden = remember(sections) { hasHiddenNavItems(sections) }
    val shownSections = remember(sections, showHidden) { withoutHiddenNavItems(sections, showHidden) }
    val visibleSections = remember(shownSections, query) { filterNavSections(shownSections, query) }
    val pinnedSection = if (searching) null else visibleSections.firstOrNull { it.pinned }
    val listSections = visibleSections.filterNot { it.pinned }
    val pinHintVisible = !searching && pinnedSection == null && pinnedLabel.isNotEmpty() &&
        shownSections.any { section -> section.items.any { it.onPinToggle != null } }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(DRAWER_WIDTH_FRACTION)
            .widthIn(max = DRAWER_MAX_WIDTH_DP.dp)
            .testTag(AppNavigationTestTags.DRAWER),
        color = colors.drawerBackground,
        shape = RoundedCornerShape(topEnd = AppRadius.sheet, bottomEnd = AppRadius.sheet)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.systemBars.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start)
                )
        ) {
            DrawerHeader(
                title = title,
                subtitle = subtitle,
                headerIcon = headerIcon,
                closeDescription = closeDescription,
                onClose = onClose,
                metaTextStyle = metaTextStyle,
                colors = colors
            )
            DrawerSearchField(
                query = query,
                placeholder = searchPlaceholder,
                onQueryChange = { query = it }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.md)
            ) {
                if (pinnedSection != null) {
                    PinnedTiles(section = pinnedSection, metaTextStyle = metaTextStyle, colors = colors)
                } else if (pinHintVisible) {
                    PinnedHint(label = pinnedLabel, metaTextStyle = metaTextStyle, colors = colors)
                }
                if (searching && listSections.isEmpty()) {
                    Text(
                        text = stringResource(R.string.navigation_search_no_results),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.drawerSectionLabel,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.xl)
                    )
                }
                listSections.forEach { section ->
                    DrawerSection(
                        section = section,
                        expanded = searching || isSectionExpanded(section, overrides),
                        collapsible = !searching,
                        onToggle = {
                            val updated = toggledOverrides(section, overrides)
                            if (onSectionOverridesChange != null) onSectionOverridesChange(updated) else localOverrides = updated
                        },
                        colors = colors
                    )
                }
                if (anyHidden && onShowHiddenChange != null) {
                    ShowHiddenRow(checked = showHidden, onCheckedChange = onShowHiddenChange, colors = colors)
                }
                Spacer(Modifier.height(AppSpacing.sm))
            }
            if (footerItem != null || footerAction != null) {
                DrawerFooter(item = footerItem, action = footerAction, colors = colors)
            }
        }
    }
}

@Composable
private fun DrawerHeader(
    title: String,
    subtitle: String?,
    headerIcon: ImageVector?,
    closeDescription: String?,
    onClose: (() -> Unit)?,
    metaTextStyle: TextStyle,
    colors: AppNavColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AppSpacing.lg, end = AppSpacing.sm, top = AppSpacing.lg, bottom = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        if (headerIcon != null) {
            Box(
                modifier = Modifier
                    .size(HeaderTileSize)
                    .clip(RoundedCornerShape(AppRadius.textField))
                    .background(colors.selectedTile),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = headerIcon,
                    contentDescription = null,
                    tint = colors.selectedTileIcon,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.drawerTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = metaTextStyle,
                    color = colors.drawerSectionLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (onClose != null) {
            AppIconButton(
                icon = Icons.Outlined.Close,
                contentDescription = closeDescription,
                role = AppButtonRole.Neutral,
                onClick = onClose,
                modifier = Modifier.testTag(AppNavigationTestTags.DRAWER_CLOSE)
            )
        }
    }
}

@Composable
private fun DrawerSearchField(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit
) {
    AppTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        shape = RoundedCornerShape(AppRadius.pill),
        placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                AppIconButton(
                    icon = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.navigation_search_clear),
                    role = AppButtonRole.Neutral,
                    onClick = { onQueryChange("") }
                )
            }
        } else {
            null
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg)
            .testTag(AppNavigationTestTags.DRAWER_SEARCH)
    )
}

@Composable
private fun PinnedTiles(section: AppNavSection, metaTextStyle: TextStyle, colors: AppNavColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        SectionCaption(text = section.label.orEmpty(), metaTextStyle = metaTextStyle, colors = colors)
        section.items.chunked(PINNED_TILES_PER_ROW).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                rowItems.forEach { item ->
                    PinnedTile(item = item, colors = colors, modifier = Modifier.weight(1f))
                }
                repeat(PINNED_TILES_PER_ROW - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        AppHorizontalDivider(
            modifier = Modifier.padding(top = AppSpacing.sm),
            tone = AppDividerTone.Subtle
        )
    }
}

@Composable
private fun PinnedHint(label: String, metaTextStyle: TextStyle, colors: AppNavColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        SectionCaption(text = label, metaTextStyle = metaTextStyle, colors = colors)
        AppBanner(
            text = stringResource(R.string.navigation_pin_hint),
            role = AppBannerRole.Neutral,
            variant = AppBannerVariant.Card,
            icon = Icons.Outlined.PushPin,
            modifier = Modifier.testTag(AppNavigationTestTags.DRAWER_PIN_HINT)
        )
        AppHorizontalDivider(
            modifier = Modifier.padding(top = AppSpacing.sm),
            tone = AppDividerTone.Subtle
        )
    }
}

@Composable
private fun SectionCaption(text: String, metaTextStyle: TextStyle, colors: AppNavColors) {
    Text(
        text = text.uppercase(),
        style = metaTextStyle.copy(letterSpacing = CAPTION_LETTER_SPACING),
        color = colors.drawerSectionLabel,
        modifier = Modifier.padding(horizontal = AppSpacing.xs)
    )
}

@Composable
private fun PinnedTile(item: AppNavItem, colors: AppNavColors, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(AppRadius.button)
    val haptics = LocalHapticFeedback.current
    val accent = colors.accentAt(item.accentIndex) ?: colors.drawerUnselectedIcon
    val borderColor = if (item.selected) colors.selectedPill else MaterialTheme.colorScheme.outline.copy(alpha = BORDER_ALPHA)
    val borderWidth = if (item.selected) 2.dp else 1.dp
    Box(
        modifier = modifier
            .height(PinnedTileHeight)
            .alpha(if (item.enabled) 1f else DISABLED_ALPHA)
            .clip(shape)
            .background(if (item.selected) colors.drawerSelectedContainer else colors.drawerUnselectedContainer)
            .border(borderWidth, borderColor, shape)
            .combinedClickable(
                enabled = item.enabled,
                role = Role.Button,
                onLongClickLabel = pinActionLabel(item),
                onLongClick = pinLongClick(item, haptics),
                onClick = item.onClick
            )
            .semantics { selected = item.selected }
            .padding(horizontal = AppSpacing.xs)
            .testTag(AppNavigationTestTags.drawerPinnedItem(item.label))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (item.selected) colors.drawerSelectedIcon else accent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(AppSpacing.xs))
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (item.selected) FontWeight.SemiBold else null,
                color = if (item.selected) colors.drawerSelectedLabel else colors.drawerUnselectedLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Filled.PushPin,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = AppSpacing.xs)
                .size(PinBadgeSize)
        )
    }
}

@Composable
private fun DrawerSection(
    section: AppNavSection,
    expanded: Boolean,
    collapsible: Boolean,
    onToggle: () -> Unit,
    colors: AppNavColors
) {
    val label = section.label
    if (label == null) {
        Column(modifier = Modifier.padding(top = AppSpacing.sm)) {
            section.items.forEach { item -> DrawerNavRow(item = item, colors = colors) }
        }
        return
    }
    val toggleLabel = stringResource(
        if (expanded) R.string.navigation_section_collapse else R.string.navigation_section_expand,
        label
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = RowMinHeight)
                .clickable(enabled = collapsible, onClickLabel = toggleLabel, onClick = onToggle)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                .testTag(AppNavigationTestTags.drawerSection(label)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = colors.drawerTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = section.items.size.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.drawerSectionLabel
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.drawerTitle,
                modifier = Modifier.size(24.dp)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                section.items.forEach { item -> DrawerNavRow(item = item, colors = colors) }
                Spacer(Modifier.height(AppSpacing.sm))
            }
        }
        AppHorizontalDivider(tone = AppDividerTone.Subtle)
    }
}

@Composable
private fun DrawerFooter(item: AppNavItem?, action: AppNavFooterAction?, colors: AppNavColors) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AppHorizontalDivider(tone = AppDividerTone.Subtle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item != null) {
                DrawerNavRow(item = item, colors = colors, modifier = Modifier.weight(1f))
            } else {
                Spacer(Modifier.weight(1f))
            }
            if (action != null) {
                AppIconButton(
                    icon = action.icon,
                    contentDescription = action.contentDescription,
                    role = AppButtonRole.Neutral,
                    onClick = action.onClick,
                    modifier = Modifier.testTag(AppNavigationTestTags.DRAWER_FOOTER_ACTION)
                )
            }
        }
    }
}

@Composable
private fun DrawerNavRow(item: AppNavItem, colors: AppNavColors, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(AppRadius.pill)
    val haptics = LocalHapticFeedback.current
    val accent = colors.accentAt(item.accentIndex) ?: colors.drawerUnselectedIcon
    val tileColor = if (item.selected) colors.selectedTile else accent.copy(alpha = TILE_CONTAINER_ALPHA)
    val tileIconColor = if (item.selected) colors.selectedTileIcon else accent
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .alpha(if (item.enabled && !item.hidden) 1f else DISABLED_ALPHA)
            .clip(shape)
            .background(if (item.selected) colors.drawerSelectedContainer else colors.drawerUnselectedContainer)
            .combinedClickable(
                enabled = item.enabled,
                role = Role.Button,
                onLongClickLabel = pinActionLabel(item),
                onLongClick = pinLongClick(item, haptics),
                onClick = item.onClick
            )
            .semantics { selected = item.selected }
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs)
            .testTag(AppNavigationTestTags.drawerItem(item.label)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)
    ) {
        NavIconTile(icon = item.icon, tileColor = tileColor, iconColor = tileIconColor)
        NavRowLabel(item = item, colors = colors)
        if (item.onPinToggle != null) {
            AppIconButton(
                icon = if (item.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = pinActionLabel(item),
                role = if (item.pinned) AppButtonRole.Primary else AppButtonRole.Neutral,
                onClick = item.onPinToggle,
                enabled = item.enabled,
                modifier = Modifier.testTag(AppNavigationTestTags.drawerPinToggle(item.label))
            )
        }
        if (item.onHideToggle != null) {
            AppIconButton(
                icon = if (item.hidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                contentDescription = stringResource(
                    if (item.hidden) R.string.navigation_unhide_item else R.string.navigation_hide_item,
                    item.label
                ),
                role = AppButtonRole.Neutral,
                onClick = item.onHideToggle,
                modifier = Modifier.testTag(AppNavigationTestTags.drawerHideToggle(item.label))
            )
        }
    }
}

@Composable
private fun ShowHiddenRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit, colors: AppNavColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(horizontal = AppSpacing.md)
            .testTag(AppNavigationTestTags.DRAWER_SHOW_HIDDEN),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        AppCheckbox(checked = checked, onCheckedChange = null)
        Text(
            text = stringResource(R.string.navigation_show_hidden),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.drawerSectionLabel
        )
    }
}

@Composable
private fun RowScope.NavRowLabel(item: AppNavItem, colors: AppNavColors) {
    Text(
        text = item.label,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = if (item.selected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (item.selected) colors.drawerSelectedLabel else colors.drawerUnselectedLabel,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f)
    )
}

@Composable
private fun NavIconTile(icon: ImageVector, tileColor: Color, iconColor: Color) {
    Box(
        modifier = Modifier
            .size(IconTileSize)
            .clip(RoundedCornerShape(AppRadius.textField))
            .background(tileColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

private fun pinLongClick(item: AppNavItem, haptics: HapticFeedback): () -> Unit {
    val toggle = item.onPinToggle ?: return {}
    return {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        toggle()
    }
}

@Composable
private fun pinActionLabel(item: AppNavItem): String? =
    if (item.onPinToggle == null) {
        null
    } else {
        stringResource(if (item.pinned) R.string.navigation_unpin_item else R.string.navigation_pin_item, item.label)
    }

internal fun isSectionExpanded(section: AppNavSection, overrides: List<String>): Boolean {
    val key = section.id
    return when {
        "$EXPANDED_PREFIX$key" in overrides -> true
        "$COLLAPSED_PREFIX$key" in overrides -> false
        else -> section.items.any { it.selected }
    }
}

internal fun toggledOverrides(section: AppNavSection, overrides: List<String>): List<String> {
    val key = section.id
    val expanded = isSectionExpanded(section, overrides)
    val prefix = if (expanded) COLLAPSED_PREFIX else EXPANDED_PREFIX
    return overrides.filterNot { it.drop(1) == key } + "$prefix$key"
}

private const val EXPANDED_PREFIX = "+"
private const val COLLAPSED_PREFIX = "-"
private val CAPTION_LETTER_SPACING = 1.2.sp
