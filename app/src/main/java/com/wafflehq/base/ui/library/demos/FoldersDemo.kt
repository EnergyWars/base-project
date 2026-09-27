package com.wafflehq.base.ui.library.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.lib.folders.FolderDeletionAction
import com.wafflehq.lib.folders.FolderDrop
import com.wafflehq.lib.folders.FolderTreeRow
import com.wafflehq.lib.folders.buildFolderTreeRows
import com.wafflehq.lib.folders.collectFolderSubtreeIds
import com.wafflehq.lib.folders.folderAncestryChain
import com.wafflehq.lib.folders.ui.FolderBreadcrumb
import com.wafflehq.lib.folders.ui.FolderBreadcrumbItem
import com.wafflehq.lib.folders.ui.FolderCard
import com.wafflehq.lib.folders.ui.FolderDeleteDialog
import com.wafflehq.lib.folders.ui.FolderDragContainer
import com.wafflehq.lib.folders.ui.FolderDragState
import com.wafflehq.lib.folders.ui.FolderEntryCard
import com.wafflehq.lib.folders.ui.FolderEntryTitle
import com.wafflehq.lib.folders.ui.FolderListDefaults
import com.wafflehq.lib.folders.ui.FolderMetaLine
import com.wafflehq.lib.folders.ui.FolderNameDialog
import com.wafflehq.lib.folders.ui.rememberFolderDragState
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.menu.AppDropdownMenuItem
import java.time.LocalDateTime

internal data class DemoFolder(val id: Long, val parentId: Long?, val name: String)

internal data class DemoEntry(val id: Long, val folderId: Long?, val title: String)

internal class FoldersDemoModel(initialFolders: List<DemoFolder>, initialEntries: List<DemoEntry>) {

    val folders = mutableStateListOf<DemoFolder>().apply { addAll(initialFolders) }
    val entries = mutableStateListOf<DemoEntry>().apply { addAll(initialEntries) }

    var expandedIds by mutableStateOf(emptySet<Long>())
        private set

    var selectedFolderId by mutableStateOf<Long?>(null)
        private set

    private var nextId = (initialFolders.map { it.id } + initialEntries.map { it.id }).maxOrNull()?.plus(1) ?: 1L

    fun rows(): List<FolderTreeRow<DemoFolder, DemoEntry>> = buildFolderTreeRows(
        folders = folders.toList(),
        entries = entries.toList(),
        rootParentId = null,
        expandedFolderIds = expandedIds,
        folderId = { it.id },
        folderParentId = { it.parentId },
        folderName = { it.name },
        entryFolderId = { it.folderId },
        entryOrder = compareBy<DemoEntry> { it.title },
    )

    fun unfiledEntries(): List<DemoEntry> = entries.filter { it.folderId == null }.sortedBy { it.title }

    fun folderName(id: Long?): String? = folders.firstOrNull { it.id == id }?.name

    fun entryCount(folderId: Long): Int {
        val subtree = folders.collectFolderSubtreeIds(folderId, { it.id }, { it.parentId })
        return entries.count { it.folderId in subtree }
    }

    fun breadcrumb(): List<DemoFolder> = folders.folderAncestryChain(selectedFolderId, { it.id }, { it.parentId })

    fun toggleExpanded(id: Long) {
        expandedIds = if (id in expandedIds) expandedIds - id else expandedIds + id
        selectedFolderId = id
    }

    fun select(id: Long?) {
        selectedFolderId = id
    }

    fun addFolder(name: String, parentId: Long?): DemoFolder {
        val folder = DemoFolder(nextId++, parentId, name)
        folders.add(folder)
        if (parentId != null) expandedIds = expandedIds + parentId
        return folder
    }

    fun rename(id: Long, name: String) {
        val index = folders.indexOfFirst { it.id == id }
        if (index >= 0) folders[index] = folders[index].copy(name = name)
    }

    fun delete(id: Long, action: FolderDeletionAction) {
        val folder = folders.firstOrNull { it.id == id } ?: return
        val removed: Set<Long> = when (action) {
            FolderDeletionAction.MOVE_CONTENTS_UP -> {
                folders.indices.forEach { index ->
                    if (folders[index].parentId == id) folders[index] = folders[index].copy(parentId = folder.parentId)
                }
                entries.indices.forEach { index ->
                    if (entries[index].folderId == id) entries[index] = entries[index].copy(folderId = folder.parentId)
                }
                setOf(id)
            }
            FolderDeletionAction.DELETE_CONTENTS -> {
                val subtree = folders.collectFolderSubtreeIds(id, { it.id }, { it.parentId })
                entries.removeAll { it.folderId in subtree }
                subtree
            }
        }
        folders.removeAll { it.id in removed }
        expandedIds = expandedIds - removed
        if (selectedFolderId in removed) selectedFolderId = null
    }

    fun moveEntry(entryId: Long, targetFolderId: Long?) {
        val index = entries.indexOfFirst { it.id == entryId }
        if (index < 0) return
        if (!FolderDrop.shouldMove(entries[index].folderId, targetFolderId, allowUnfile = false)) return
        entries[index] = entries[index].copy(folderId = targetFolderId)
        if (targetFolderId != null) expandedIds = expandedIds + targetFolderId
    }

    fun removeEntry(id: Long) {
        entries.removeAll { it.id == id }
    }

    companion object {
        const val RECIPES = 1L
        const val SOUPS = 2L
        const val NOTES = 3L
        const val ARCHIVE = 4L
        const val TOMATO_SOUP = 10L
        const val PASTA = 11L
        const val MEETING = 12L
        const val IDEAS = 13L

        fun sample(text: (Int) -> String): FoldersDemoModel = FoldersDemoModel(
            initialFolders = listOf(
                DemoFolder(RECIPES, null, text(R.string.libex_folders_sample_recipes)),
                DemoFolder(SOUPS, RECIPES, text(R.string.libex_folders_sample_soups)),
                DemoFolder(NOTES, null, text(R.string.libex_folders_sample_notes)),
                DemoFolder(ARCHIVE, null, text(R.string.libex_folders_sample_archive)),
            ),
            initialEntries = listOf(
                DemoEntry(TOMATO_SOUP, SOUPS, text(R.string.libex_folders_sample_tomato_soup)),
                DemoEntry(PASTA, RECIPES, text(R.string.libex_folders_sample_pasta)),
                DemoEntry(MEETING, NOTES, text(R.string.libex_folders_sample_meeting)),
                DemoEntry(IDEAS, null, text(R.string.libex_folders_sample_ideas)),
            ),
        )
    }
}

internal object FoldersDemoLogic {
    val LAST_ACTIVITY: LocalDateTime = LocalDateTime.of(2026, 1, 15, 9, 30)
}

internal object FoldersTags {
    const val NEW_FOLDER = "libex_folders_new"
}

@Composable
internal fun rememberFoldersDemoModel(): FoldersDemoModel {
    val context = LocalContext.current
    return remember { FoldersDemoModel.sample { context.getString(it) } }
}

@Composable
internal fun FoldersDemo(model: FoldersDemoModel = rememberFoldersDemoModel()) {
    val dragState = rememberFolderDragState<DemoEntry>()
    var showCreate by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<DemoFolder?>(null) }
    var deleteTarget by remember { mutableStateOf<DemoFolder?>(null) }
    val rows = model.rows()
    val rootLabel = stringResource(R.string.libex_folders_root)
    val removeLabel = stringResource(R.string.libex_folders_remove_entry)
    val dragHint = stringResource(R.string.libex_folders_drag_hint)
    val unfiledLabel = stringResource(R.string.libex_folders_unfiled)
    val chain = model.breadcrumb()

    DemoSection(
        id = "folders",
        titleRes = R.string.libex_folders_title,
        descriptionRes = R.string.libex_folders_desc,
        moduleRes = R.string.libex_module_folders,
    ) {
        AppButton(
            text = stringResource(R.string.libex_folders_new),
            role = AppButtonRole.Primary,
            variant = AppButtonVariant.Tonal,
            leadingIcon = Icons.Filled.CreateNewFolder,
            onClick = { showCreate = true },
            modifier = Modifier.testTag(FoldersTags.NEW_FOLDER),
        )
        FolderDragContainer(state = dragState, dragLabel = { it.title }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(FolderListDefaults.itemSpacing),
            ) {
                FolderBreadcrumb(
                    items = buildList {
                        add(FolderBreadcrumbItem(rootLabel) { model.select(null) })
                        chain.forEach { folder -> add(FolderBreadcrumbItem(folder.name) { model.select(folder.id) }) }
                    },
                )
                rows.forEach { row ->
                    when (row) {
                        is FolderTreeRow.FolderRow -> {
                            val folder = row.folder
                            FolderCard(
                                name = folder.name,
                                countText = stringResource(R.string.libex_folders_count, model.entryCount(folder.id)),
                                lastActivityAt = FoldersDemoLogic.LAST_ACTIVITY,
                                isDropTarget = dragState.isDropTarget(folder.id),
                                onOpen = { model.toggleExpanded(folder.id) },
                                onRename = { renameTarget = folder },
                                onDelete = { deleteTarget = folder },
                                onPositioned = { dragState.registerFolderBounds(folder.id, it) },
                                modifier = Modifier.padding(start = FolderListDefaults.depthIndent(row.depth)),
                                isExpanded = row.isExpanded,
                                onToggleExpand = { model.toggleExpanded(folder.id) },
                                folderId = folder.id,
                                onDisposeBounds = { dragState.unregisterFolderBounds(folder.id) },
                            )
                        }
                        is FolderTreeRow.EntryRow -> DemoEntryCard(
                            entry = row.entry,
                            depth = row.depth,
                            model = model,
                            dragState = dragState,
                            dragHint = dragHint,
                            removeLabel = removeLabel,
                            folderLabel = model.folderName(row.entry.folderId) ?: unfiledLabel,
                        )
                    }
                }
                model.unfiledEntries().forEach { entry ->
                    DemoEntryCard(
                        entry = entry,
                        depth = 0,
                        model = model,
                        dragState = dragState,
                        dragHint = dragHint,
                        removeLabel = removeLabel,
                        folderLabel = unfiledLabel,
                    )
                }
            }
        }
    }

    if (showCreate) {
        FolderNameDialog(
            title = stringResource(R.string.libex_folders_new_title),
            initialName = "",
            onConfirm = { name ->
                model.addFolder(name, model.selectedFolderId)
                showCreate = false
            },
            onDismiss = { showCreate = false },
        )
    }
    renameTarget?.let { target ->
        FolderNameDialog(
            title = stringResource(R.string.libex_folders_rename_title),
            initialName = target.name,
            onConfirm = { name ->
                model.rename(target.id, name)
                renameTarget = null
            },
            onDismiss = { renameTarget = null },
        )
    }
    deleteTarget?.let { target ->
        FolderDeleteDialog(
            onConfirm = { action ->
                model.delete(target.id, action)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null },
        )
    }
}

@Composable
private fun DemoEntryCard(
    entry: DemoEntry,
    depth: Int,
    model: FoldersDemoModel,
    dragState: FolderDragState<DemoEntry>,
    dragHint: String,
    removeLabel: String,
    folderLabel: String,
) {
    FolderEntryCard(
        dragState = dragState,
        item = entry,
        dragDescription = dragHint,
        onDrop = { item, target -> model.moveEntry(item.id, target) },
        onClick = { model.select(entry.folderId) },
        modifier = Modifier.padding(start = FolderListDefaults.depthIndent(depth)),
        leadingIcon = Icons.Filled.Description,
        menuItems = { dismiss ->
            AppDropdownMenuItem(
                text = { Text(removeLabel) },
                onClick = {
                    dismiss()
                    model.removeEntry(entry.id)
                },
            )
        },
    ) {
        FolderEntryTitle(entry.title)
        FolderMetaLine(text = folderLabel, icon = Icons.Filled.Folder)
    }
}
