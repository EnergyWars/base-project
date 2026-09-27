package com.wafflehq.lib.folders.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.folders.FolderAppearanceKey
import com.wafflehq.lib.folders.FolderTreeRow
import com.wafflehq.lib.folders.R
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppFab
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.button.AppIconButtonVariant
import com.wafflehq.lib.uicore.components.AppCard
import com.wafflehq.lib.uicore.components.AppStatusPill
import com.wafflehq.lib.uicore.menu.AppDropdownMenu
import com.wafflehq.lib.uicore.menu.AppDropdownMenuItem
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.theme.AppSpacing
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

data class FolderBreadcrumbItem(
    val label: String,
    val onClick: (() -> Unit)? = null
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FolderBreadcrumb(
    items: List<FolderBreadcrumbItem>,
    modifier: Modifier = Modifier
) {
    if (items.size <= 1) return
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = FolderListDefaults.cardHorizontalPadding, vertical = AppSpacing.sm)
            .testTag(FolderTestTags.BREADCRUMB),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        horizontalArrangement = Arrangement.Start
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = FolderListDefaults.crumbSeparatorAlpha
                    )
                )
            }
            val isCurrent = index == items.lastIndex
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
                    item.onClick != null -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .testTag(FolderTestTags.breadcrumbItem(index))
                    .clip(RoundedCornerShape(AppRadius.pill))
                    .background(if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .then(
                        if (!isCurrent && item.onClick != null) {
                            Modifier.clickable(onClick = item.onClick)
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
            )
        }
    }
}

@Composable
fun FolderCard(
    name: String,
    countText: String,
    lastActivityAt: LocalDateTime,
    isDropTarget: Boolean,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onPositioned: (LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier,
    renameLabel: String = stringResource(R.string.folder_rename),
    isExpanded: Boolean = false,
    onToggleExpand: (() -> Unit)? = null,
    reorderHandleModifier: Modifier? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    folderId: Long? = null,
    appearanceScope: String? = LocalFolderAppearanceScope.current,
    onDisposeBounds: (() -> Unit)? = null,
    extraMenuItems: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit = {}
) {
    var menuOpen by remember { mutableStateOf(false) }
    var appearanceOpen by remember { mutableStateOf(false) }
    val currentOnDisposeBounds by rememberUpdatedState(onDisposeBounds)
    DisposableEffect(Unit) {
        onDispose { currentOnDisposeBounds?.invoke() }
    }
    val appearanceKey = if (folderId != null && appearanceScope != null) {
        FolderAppearanceKey(appearanceScope, folderId)
    } else {
        null
    }
    val appearance = rememberFolderAppearance(appearanceKey)
    val canEditAppearance = rememberCanEditFolderAppearance(appearanceKey)
    val colors = MaterialTheme.colorScheme
    val tint = appearance.colorArgb?.let { Color(it) } ?: iconTint
    val defaultIcon = icon ?: if (isExpanded || isDropTarget) Icons.Default.FolderOpen else Icons.Default.Folder
    val displayIcon = FolderIconCatalog.resolve(appearance.iconKey) ?: defaultIcon
    val containerColor by animateColorAsState(
        targetValue = if (isDropTarget) {
            colors.primaryContainer
        } else {
            lerp(colors.surface, tint, FolderListDefaults.tintBlend(isExpanded))
        },
        label = "folderContainerColor"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isDropTarget -> colors.primary
            isExpanded -> tint
            else -> tint.copy(alpha = FolderListDefaults.tintBorderAlpha)
        },
        label = "folderBorderColor"
    )
    val borderWidth by animateDpAsState(
        targetValue = FolderListDefaults.borderWidth(isDropTarget, isExpanded),
        label = "folderBorderWidth"
    )
    val reorderLabel = stringResource(R.string.folder_reorder_handle)
    AppCard(
        onClick = onOpen,
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned(onPositioned)
            .then(reorderHandleModifier ?: Modifier)
            .semantics { if (reorderHandleModifier != null) onLongClick(label = reorderLabel, action = null) }
            .testTag(FolderTestTags.FOLDER_CARD),
        borderColor = borderColor,
        borderWidth = borderWidth,
        containerColor = containerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = FolderListDefaults.cardHorizontalPadding,
                    vertical = FolderListDefaults.cardVerticalPadding
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(FolderListDefaults.iconTouchSize)
                    .clip(RoundedCornerShape(AppRadius.card))
                    .then(
                        if (canEditAppearance) {
                            Modifier.clickable(
                                onClickLabel = stringResource(R.string.folder_appearance_change),
                                onClick = { appearanceOpen = true }
                            )
                        } else {
                            Modifier
                        }
                    )
                    .testTag(FolderAppearanceTestTags.ICON_TILE),
                contentAlignment = Alignment.Center
            ) {
                FolderIconTile(
                    accent = tint,
                    icon = displayIcon,
                    size = FolderListDefaults.tileSize,
                    glyphSize = FolderListDefaults.tileGlyphSize
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FolderListDefaults.cardContentSpacing)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(AppSpacing.xs))
                    AppStatusPill(
                        text = countText,
                        containerColor = tint.copy(alpha = FolderListDefaults.tileAlpha),
                        contentColor = colors.onSurface
                    )
                    Spacer(Modifier.height(AppSpacing.xs))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant.copy(alpha = FolderListDefaults.metaAlpha),
                            modifier = Modifier.size(FolderListDefaults.metaIconSize)
                        )
                        Text(
                            text = folderLastActivityLabel(lastActivityAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant.copy(alpha = FolderListDefaults.metaAlpha),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag(FolderTestTags.FOLDER_LAST_ACTIVITY)
                        )
                    }
                }
                if (onToggleExpand != null) {
                    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "folderExpandRotation")
                    AppIconButton(
                        icon = Icons.Default.ExpandMore,
                        contentDescription = stringResource(
                            if (isExpanded) R.string.folder_collapse else R.string.folder_expand
                        ),
                        role = if (isExpanded) AppButtonRole.Primary else AppButtonRole.Neutral,
                        onClick = onToggleExpand,
                        modifier = Modifier
                            .size(FolderListDefaults.iconButtonSize)
                            .testTag(FolderTestTags.FOLDER_EXPAND_BUTTON),
                        variant = if (isExpanded) AppIconButtonVariant.Tonal else AppIconButtonVariant.Standard,
                        iconRotation = rotation,
                    )
                }
                Box {
                    AppIconButton(
                        icon = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.folder_options),
                        role = AppButtonRole.Neutral,
                        onClick = { menuOpen = true },
                        modifier = Modifier
                            .size(FolderListDefaults.iconButtonSize)
                            .testTag(FolderTestTags.FOLDER_MENU_BUTTON)
                    )
                    FolderOptionsMenuContent(
                        expanded = menuOpen,
                        onDismiss = { menuOpen = false },
                        renameLabel = renameLabel,
                        onRename = onRename,
                        onDelete = onDelete,
                        onChangeAppearance = if (canEditAppearance) ({ appearanceOpen = true }) else null,
                        extraMenuItems = extraMenuItems
                    )
                }
            }
        }
    }
    if (appearanceOpen) {
        FolderAppearanceEditorDialog(
            key = appearanceKey,
            defaultIcon = defaultIcon,
            defaultColor = iconTint,
            onDismiss = { appearanceOpen = false }
        )
    }
}

@Composable
fun <T : Any> FolderEntryCard(
    dragState: FolderDragState<T>?,
    item: T,
    dragDescription: String,
    onDrop: ((item: T, targetFolderId: Long?) -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dragEnabled: Boolean = true,
    isDragging: Boolean = dragState?.draggedItem == item,
    entryKey: Any? = null,
    onReorder: ((item: T, targetKey: Any) -> Unit)? = null,
    reorderHandleModifier: Modifier? = null,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    leadingIcon: ImageVector? = null,
    leadingEmoji: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    menuItems: (@Composable ColumnScope.(dismiss: () -> Unit) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val isReorderTarget = entryKey != null && !isDragging && dragState?.isReorderTarget(entryKey) == true
    DisposableEffect(dragState, entryKey) {
        onDispose { if (dragState != null && entryKey != null) dragState.unregisterEntryBounds(entryKey) }
    }
    val colors = MaterialTheme.colorScheme
    val containerColor by animateColorAsState(
        targetValue = lerp(colors.surface, colors.primaryContainer, if (isReorderTarget) 1f else 0f),
        label = "entryContainerColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isReorderTarget) {
            colors.primary
        } else {
            colors.outline.copy(alpha = FolderListDefaults.cardBorderAlpha)
        },
        label = "entryBorderColor"
    )
    val ownDragModifier = if (reorderHandleModifier == null && dragState != null && onDrop != null) {
        Modifier.folderLongPressDrag(
            state = dragState,
            item = item,
            onDrop = onDrop,
            enabled = dragEnabled,
            onReorder = onReorder
        )
    } else {
        Modifier
    }
    val isDraggable = reorderHandleModifier != null || (dragState != null && onDrop != null && dragEnabled)
    AppCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isDragging && reorderHandleModifier == null) FolderListDefaults.draggingAlpha else 1f)
            .then(
                if (dragState != null && entryKey != null) {
                    Modifier.onGloballyPositioned { dragState.registerEntryBounds(entryKey, it) }
                } else {
                    Modifier
                }
            )
            .then(reorderHandleModifier ?: ownDragModifier)
            .semantics { if (isDraggable) onLongClick(label = dragDescription, action = null) }
            .testTag(FolderTestTags.ENTRY_CARD),
        borderColor = borderColor,
        borderWidth = if (isReorderTarget) FolderListDefaults.dropTargetBorderWidth else FolderListDefaults.cardBorderWidth,
        containerColor = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = FolderListDefaults.cardHorizontalPadding,
                    vertical = FolderListDefaults.cardVerticalPadding
                ),
            verticalAlignment = verticalAlignment,
            horizontalArrangement = Arrangement.spacedBy(FolderListDefaults.cardContentSpacing)
        ) {
            if (leadingIcon != null || leadingEmoji != null) {
                FolderIconTile(
                    accent = accentColor,
                    icon = leadingIcon,
                    emoji = leadingEmoji,
                    size = FolderListDefaults.entryTileSize,
                    glyphSize = FolderListDefaults.entryTileGlyphSize
                )
            }
            Column(modifier = Modifier.weight(1f), content = content)
            if (menuItems != null) {
                Box {
                    AppIconButton(
                        icon = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.folder_entry_options),
                        role = AppButtonRole.Neutral,
                        onClick = { menuOpen = true },
                        modifier = Modifier
                            .size(FolderListDefaults.iconButtonSize)
                            .testTag(FolderTestTags.ENTRY_MENU_BUTTON)
                    )
                    AppDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        menuItems { menuOpen = false }
                    }
                }
            }
        }
    }
}

fun <F, E> LazyListScope.folderTreeItems(
    rows: List<FolderTreeRow<F, E>>,
    folderKey: (F) -> Any,
    entryKey: (E) -> Any,
    folderContent: @Composable (row: FolderTreeRow.FolderRow<F>, modifier: Modifier) -> Unit,
    entryContent: @Composable (entry: E, modifier: Modifier) -> Unit,
    frameColor: @Composable (F) -> Color? = { null }
) {
    itemsIndexed(
        rows,
        key = { _, row ->
            when (row) {
                is FolderTreeRow.FolderRow -> "folder-${folderKey(row.folder)}"
                is FolderTreeRow.EntryRow -> "entry-${entryKey(row.entry)}"
            }
        }
    ) { index, row ->
        val levels = folderFrameLevels(rows, index).mapIndexed { levelIndex, level ->
            level.copy(color = rows.ancestorFolderAt(index, levelIndex)?.let { frameColor(it) })
        }
        val frame = Modifier.folderFlatFrame(levels, MaterialTheme.colorScheme.primary)
        when (row) {
            is FolderTreeRow.FolderRow -> folderContent(row, frame)
            is FolderTreeRow.EntryRow -> entryContent(row.entry, frame)
        }
    }
}

private fun <F, E> List<FolderTreeRow<F, E>>.ancestorFolderAt(index: Int, ancestorDepth: Int): F? {
    for (candidate in index - 1 downTo 0) {
        val row = this[candidate]
        if (row is FolderTreeRow.FolderRow && row.depth == ancestorDepth) return row.folder
    }
    return null
}

@Composable
fun FolderOptionsAction(
    onRename: () -> Unit,
    onDelete: () -> Unit,
    renameLabel: String = stringResource(R.string.folder_rename),
    folderId: Long? = null,
    appearanceScope: String? = LocalFolderAppearanceScope.current,
    defaultIcon: ImageVector = Icons.Default.Folder,
    defaultColor: Color = MaterialTheme.colorScheme.primary,
    extraMenuItems: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit = {}
) {
    var menuOpen by remember { mutableStateOf(false) }
    var appearanceOpen by remember { mutableStateOf(false) }
    val appearanceKey = if (folderId != null && appearanceScope != null) {
        FolderAppearanceKey(appearanceScope, folderId)
    } else {
        null
    }
    val canEditAppearance = rememberCanEditFolderAppearance(appearanceKey)
    Box {
        AppIconButton(
            icon = Icons.Default.MoreVert,
            contentDescription = stringResource(R.string.folder_options),
            role = AppButtonRole.Neutral,
            onClick = { menuOpen = true },
        )
        FolderOptionsMenuContent(
            expanded = menuOpen,
            onDismiss = { menuOpen = false },
            renameLabel = renameLabel,
            onRename = onRename,
            onDelete = onDelete,
            onChangeAppearance = if (canEditAppearance) ({ appearanceOpen = true }) else null,
            extraMenuItems = extraMenuItems
        )
    }
    if (appearanceOpen) {
        FolderAppearanceEditorDialog(
            key = appearanceKey,
            defaultIcon = defaultIcon,
            defaultColor = defaultColor,
            onDismiss = { appearanceOpen = false }
        )
    }
}

@Composable
private fun FolderOptionsMenuContent(
    expanded: Boolean,
    onDismiss: () -> Unit,
    renameLabel: String,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onChangeAppearance: (() -> Unit)?,
    extraMenuItems: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit
) {
    AppDropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        AppDropdownMenuItem(
            text = { Text(renameLabel) },
            onClick = {
                onDismiss()
                onRename()
            }
        )
        if (onChangeAppearance != null) {
            AppDropdownMenuItem(
                text = { Text(stringResource(R.string.folder_appearance_change)) },
                onClick = {
                    onDismiss()
                    onChangeAppearance()
                }
            )
        }
        extraMenuItems(onDismiss)
        AppDropdownMenuItem(
            text = { Text(stringResource(R.string.folder_delete)) },
            onClick = {
                onDismiss()
                onDelete()
            }
        )
    }
}

@Composable
fun FolderActionFabs(
    onAddItem: () -> Unit,
    addItemDescription: String,
    onAddFolder: (() -> Unit)?,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    addItemIcon: ImageVector = Icons.Default.Add
) {
    Column(horizontalAlignment = Alignment.End) {
        if (onAddFolder != null) {
            AppFab(
                icon = Icons.Default.CreateNewFolder,
                contentDescription = stringResource(R.string.folder_new),
                containerColor = containerColor,
                contentColor = contentColor,
                onClick = onAddFolder,
                small = true,
            )
            Spacer(Modifier.height(AppSpacing.lg))
        }
        AppFab(
            icon = addItemIcon,
            contentDescription = addItemDescription,
            containerColor = containerColor,
            contentColor = contentColor,
            onClick = onAddItem,
        )
    }
}

@Composable
fun folderLastActivityLabel(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(Locale.getDefault())
    return stringResource(R.string.folder_last_edited, dateTime.format(formatter))
}
