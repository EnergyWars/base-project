package com.wafflehq.uikit.folders.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.uikit.R
import com.wafflehq.uikit.components.AppDialog
import com.wafflehq.uikit.components.AppDialogConfirmButton
import com.wafflehq.uikit.components.AppDialogDismissButton
import com.wafflehq.uikit.folders.FolderDeletionAction
import com.wafflehq.uikit.folders.FolderNames
import com.wafflehq.uikit.folders.FolderTreeRow
import com.wafflehq.uikit.theme.AppRadius
import com.wafflehq.uikit.theme.AppRole
import com.wafflehq.uikit.theme.AppSpacing
import com.wafflehq.uikit.theme.AppTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object FolderListDefaults {
    val itemSpacing: Dp = AppSpacing.md
    val contentPadding: PaddingValues = PaddingValues(AppSpacing.lg)
    val indentPerDepth: Dp = AppSpacing.lg
    val cardBorderWidth: Dp = 1.dp
    val dropTargetBorderWidth: Dp = 2.dp
    val cardBorderAlpha: Float = 0.4f
    val draggingAlpha: Float = 0.3f
    val cardHorizontalPadding: Dp = AppSpacing.lg
    val cardVerticalPadding: Dp = AppSpacing.md
    val cardContentSpacing: Dp = AppSpacing.md
    val iconButtonSize: Dp = 36.dp

    fun depthIndent(depth: Int): Dp = indentPerDepth * depth.coerceAtLeast(0)
}

object FolderTestTags {
    const val FOLDER_CARD = "folder_card"
    const val FOLDER_MENU_BUTTON = "folder_menu_button"
    const val FOLDER_EXPAND_BUTTON = "folder_expand_button"
    const val FOLDER_REORDER_HANDLE = "folder_reorder_handle"
    const val FOLDER_LAST_ACTIVITY = "folder_last_activity"
    const val ENTRY_CARD = "folder_entry_card"
    const val ENTRY_MENU_BUTTON = "folder_entry_menu_button"
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
    icon: ImageVector = Icons.Default.Folder,
    iconTint: Color = AppTheme.colors.primary.accent,
    extraMenuItems: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit = {},
) {
    var menuOpen by remember { mutableStateOf(false) }
    val borderColor = if (isDropTarget) {
        AppTheme.colors.primary.accent
    } else {
        AppTheme.colors.outline.copy(alpha = FolderListDefaults.cardBorderAlpha)
    }
    val containerColor = if (isDropTarget) {
        AppTheme.colors.primary.container
    } else {
        AppTheme.colors.surface
    }
    Card(
        onClick = onOpen,
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned(onPositioned)
            .testTag(FolderTestTags.FOLDER_CARD),
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            if (isDropTarget) FolderListDefaults.dropTargetBorderWidth else FolderListDefaults.cardBorderWidth,
            borderColor,
        ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = FolderListDefaults.cardHorizontalPadding,
                    vertical = FolderListDefaults.cardVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FolderListDefaults.cardContentSpacing),
        ) {
            if (reorderHandleModifier != null) {
                Icon(
                    Icons.Default.DragIndicator,
                    contentDescription = stringResource(R.string.folder_reorder_handle),
                    tint = AppTheme.colors.onSurfaceVariant,
                    modifier = reorderHandleModifier.testTag(FolderTestTags.FOLDER_REORDER_HANDLE),
                )
            }
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = countText,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant,
                )
                Text(
                    text = folderLastActivityLabel(lastActivityAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.onSurfaceVariant,
                    modifier = Modifier.testTag(FolderTestTags.FOLDER_LAST_ACTIVITY),
                )
            }
            if (onToggleExpand != null) {
                val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "folderExpandRotation")
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier
                        .size(FolderListDefaults.iconButtonSize)
                        .testTag(FolderTestTags.FOLDER_EXPAND_BUTTON),
                ) {
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = stringResource(
                            if (isExpanded) R.string.folder_collapse else R.string.folder_expand,
                        ),
                        modifier = Modifier.rotate(rotation),
                    )
                }
            }
            Box {
                IconButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier
                        .size(FolderListDefaults.iconButtonSize)
                        .testTag(FolderTestTags.FOLDER_MENU_BUTTON),
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.folder_options))
                }
                FolderOptionsMenuContent(
                    expanded = menuOpen,
                    onDismiss = { menuOpen = false },
                    renameLabel = renameLabel,
                    onRename = onRename,
                    onDelete = onDelete,
                    extraMenuItems = extraMenuItems,
                )
            }
        }
    }
}

@Composable
fun <T : Any> FolderEntryCard(
    dragState: FolderDragState<T>?,
    item: T,
    dragDescription: String,
    onDrop: (item: T, targetFolderId: Long?) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dragEnabled: Boolean = true,
    isDragging: Boolean = dragState?.draggedItem == item,
    entryKey: Any? = null,
    onReorder: ((item: T, targetKey: Any) -> Unit)? = null,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    menuItems: (@Composable ColumnScope.(dismiss: () -> Unit) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val isReorderTarget = entryKey != null && !isDragging && dragState?.isReorderTarget(entryKey) == true
    DisposableEffect(dragState, entryKey) {
        onDispose { if (dragState != null && entryKey != null) dragState.unregisterEntryBounds(entryKey) }
    }
    val borderColor = if (isReorderTarget) {
        AppTheme.colors.primary.accent
    } else {
        AppTheme.colors.outline.copy(alpha = FolderListDefaults.cardBorderAlpha)
    }
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isDragging) FolderListDefaults.draggingAlpha else 1f)
            .then(
                if (dragState != null && entryKey != null) {
                    Modifier.onGloballyPositioned { dragState.registerEntryBounds(entryKey, it) }
                } else {
                    Modifier
                },
            )
            .testTag(FolderTestTags.ENTRY_CARD),
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            if (isReorderTarget) FolderListDefaults.dropTargetBorderWidth else FolderListDefaults.cardBorderWidth,
            borderColor,
        ),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = FolderListDefaults.cardHorizontalPadding,
                    vertical = FolderListDefaults.cardVerticalPadding,
                ),
            verticalAlignment = verticalAlignment,
            horizontalArrangement = Arrangement.spacedBy(FolderListDefaults.cardContentSpacing),
        ) {
            if (dragState != null) {
                FolderDragHandle(
                    state = dragState,
                    item = item,
                    contentDescription = dragDescription,
                    onDrop = onDrop,
                    enabled = dragEnabled,
                    onReorder = onReorder,
                )
            }
            Column(modifier = Modifier.weight(1f), content = content)
            if (menuItems != null) {
                Box {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier
                            .size(FolderListDefaults.iconButtonSize)
                            .testTag(FolderTestTags.ENTRY_MENU_BUTTON),
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.folder_entry_options))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
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
) {
    items(
        rows,
        key = { row ->
            when (row) {
                is FolderTreeRow.FolderRow -> "folder-${folderKey(row.folder)}"
                is FolderTreeRow.EntryRow -> "entry-${entryKey(row.entry)}"
            }
        },
    ) { row ->
        val indent = Modifier.padding(start = FolderListDefaults.depthIndent(row.depth()))
        when (row) {
            is FolderTreeRow.FolderRow -> folderContent(row, indent)
            is FolderTreeRow.EntryRow -> entryContent(row.entry, indent)
        }
    }
}

private fun FolderTreeRow<*, *>.depth(): Int = when (this) {
    is FolderTreeRow.FolderRow -> depth
    is FolderTreeRow.EntryRow -> depth
}

@Composable
fun FolderOptionsAction(
    onRename: () -> Unit,
    onDelete: () -> Unit,
    renameLabel: String = stringResource(R.string.folder_rename),
    extraMenuItems: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit = {},
) {
    var menuOpen by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { menuOpen = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.folder_options))
        }
        FolderOptionsMenuContent(
            expanded = menuOpen,
            onDismiss = { menuOpen = false },
            renameLabel = renameLabel,
            onRename = onRename,
            onDelete = onDelete,
            extraMenuItems = extraMenuItems,
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
    extraMenuItems: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(renameLabel) },
            onClick = {
                onDismiss()
                onRename()
            },
        )
        extraMenuItems(onDismiss)
        DropdownMenuItem(
            text = { Text(stringResource(R.string.folder_delete)) },
            onClick = {
                onDismiss()
                onDelete()
            },
        )
    }
}

@Composable
private fun rememberFolderGuardedDismiss(hasChanges: Boolean, onDismiss: () -> Unit): () -> Unit {
    var showConfirm by remember { mutableStateOf(false) }
    if (showConfirm) {
        AppDialog(
            onDismissRequest = { showConfirm = false },
            title = stringResource(R.string.folder_discard_changes_title),
            text = stringResource(R.string.folder_discard_changes_message),
            confirmText = stringResource(R.string.folder_discard_changes_confirm),
            confirmRole = AppRole.Error,
            onConfirm = { showConfirm = false; onDismiss() },
            dismissText = stringResource(R.string.folder_cancel),
            onDismiss = { showConfirm = false },
        )
    }
    return remember(hasChanges, onDismiss) { { if (hasChanges) showConfirm = true else onDismiss() } }
}

@Composable
fun FolderNameDialog(
    title: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    label: String = stringResource(R.string.folder_name_label),
    confirmLabel: String = stringResource(R.string.folder_save),
    extraContent: @Composable ColumnScope.(markChanged: () -> Unit) -> Unit = {},
) {
    var name by remember { mutableStateOf(initialName) }
    var hasChanges by remember { mutableStateOf(false) }
    val guardedDismiss = rememberFolderGuardedDismiss(hasChanges = hasChanges, onDismiss = onDismiss)
    val normalized = FolderNames.normalize(name)
    AppDialog(
        onDismissRequest = guardedDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; hasChanges = true },
                    label = { Text(label) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppRadius.textField),
                )
                extraContent { hasChanges = true }
            }
        },
        confirmButton = {
            AppDialogConfirmButton(text = confirmLabel, onClick = { normalized?.let(onConfirm) }, enabled = normalized != null)
        },
        dismissButton = {
            AppDialogDismissButton(text = stringResource(R.string.folder_cancel), onClick = onDismiss)
        },
    )
}

@Composable
fun FolderDeleteDialog(
    onConfirm: (FolderDeletionAction) -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.folder_delete_confirm_title),
) {
    var selected by remember { mutableStateOf(FolderDeletionAction.MOVE_CONTENTS_UP) }
    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(stringResource(R.string.folder_delete_choice_message))
                Spacer(Modifier.height(AppSpacing.sm))
                FolderDeleteOptionRow(
                    selected = selected == FolderDeletionAction.MOVE_CONTENTS_UP,
                    title = stringResource(R.string.folder_delete_option_move_up),
                    subtitle = stringResource(R.string.folder_delete_option_move_up_hint),
                    onClick = { selected = FolderDeletionAction.MOVE_CONTENTS_UP },
                )
                FolderDeleteOptionRow(
                    selected = selected == FolderDeletionAction.DELETE_CONTENTS,
                    title = stringResource(R.string.folder_delete_option_delete_all),
                    subtitle = stringResource(R.string.folder_delete_option_delete_all_hint),
                    onClick = { selected = FolderDeletionAction.DELETE_CONTENTS },
                )
            }
        },
        confirmButton = {
            AppDialogConfirmButton(text = stringResource(R.string.folder_delete), onClick = { onConfirm(selected) }, role = AppRole.Error)
        },
        dismissButton = {
            AppDialogDismissButton(text = stringResource(R.string.folder_cancel), onClick = onDismiss)
        },
    )
}

@Composable
private fun FolderDeleteOptionRow(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = AppSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.padding(start = 4.dp, top = AppSpacing.md)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun FolderActionFabs(
    onAddItem: () -> Unit,
    addItemDescription: String,
    onAddFolder: (() -> Unit)?,
    containerColor: Color = AppTheme.colors.primary.accent,
    contentColor: Color = contentColorFor(containerColor),
) {
    Column(horizontalAlignment = Alignment.End) {
        if (onAddFolder != null) {
            SmallFloatingActionButton(onClick = onAddFolder) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = stringResource(R.string.folder_new))
            }
            Spacer(Modifier.height(AppSpacing.lg))
        }
        FloatingActionButton(
            onClick = onAddItem,
            containerColor = containerColor,
            contentColor = contentColor,
        ) {
            Icon(Icons.Default.Add, contentDescription = addItemDescription)
        }
    }
}

data class FolderIconOption(
    val key: String,
    val icon: ImageVector,
    val label: String,
)

/**
 * Per-folder icon selection is opt-in: only pass a non-empty [options] list from a module that
 * wants it. Modules that don't wire this in never show the menu item at all.
 */
@Composable
fun FolderIconPickerDialog(
    options: List<FolderIconOption>,
    selectedKey: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.folder_icon_picker_title),
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(56.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.height(280.dp),
            ) {
                items(options, key = { it.key }) { option ->
                    val isSelected = option.key == selectedKey
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(AppRadius.card))
                            .then(
                                if (isSelected) {
                                    Modifier.background(AppTheme.colors.primary.container)
                                } else {
                                    Modifier
                                },
                            )
                            .selectable(selected = isSelected, onClick = { onSelect(option.key) }),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            option.icon,
                            contentDescription = option.label,
                            tint = if (isSelected) {
                                AppTheme.colors.primary.onContainer
                            } else {
                                AppTheme.colors.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            AppDialogConfirmButton(text = stringResource(R.string.folder_icon_picker_close), onClick = onDismiss)
        },
    )
}

@Composable
fun folderLastActivityLabel(dateTime: LocalDateTime): String {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(locale)
    return stringResource(R.string.folder_last_edited, dateTime.format(formatter))
}
