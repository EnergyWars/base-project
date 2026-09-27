package com.wafflehq.lib.folders.ui

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private data class RFolder(val id: Long, val parentId: Long?, val sortOrder: Int)
private data class REntry(val id: Long, val folderId: Long?, val sortOrder: Int)

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FolderDragLevelRealCardsTest {

    @get:Rule
    val rule = createComposeRule()

    private val lastActivity = LocalDateTime.of(2026, 5, 20, 14, 30)

    @Test
    fun draggingLastEntryIntoCollapsedFolder_withRealCards_invokesOnMoveEntry() {
        var moved: Pair<Long, Long?>? = null
        rule.setContent {
            FolderDragLevel(
                folders = listOf(RFolder(1L, null, 3)),
                entries = listOf(REntry(10L, null, 0), REntry(11L, null, 1), REntry(12L, null, 2)),
                parentId = null,
                expandedFolderIds = emptySet(),
                folderId = { it.id },
                folderParentId = { it.parentId },
                folderSortOrder = { it.sortOrder },
                entryFolderId = { it.folderId },
                entrySortOrder = { it.sortOrder },
                entryId = { it.id },
                onReorderLevel = { _, _, _ -> },
                onMoveEntry = { entry, target -> moved = entry.id to target },
                onMoveFolder = { _, _ -> },
                canNestFolder = { _, _ -> true },
                folderContent = { folder, isExpanded, isNestTarget, dragHandleModifier ->
                    FolderCard(
                        name = "F${folder.id}",
                        countText = "0",
                        lastActivityAt = lastActivity,
                        isDropTarget = isNestTarget,
                        onOpen = {},
                        onRename = {},
                        onDelete = {},
                        onPositioned = {},
                        isExpanded = isExpanded,
                        reorderHandleModifier = dragHandleModifier
                    )
                },
                entryContent = { entry, isDragging, dragHandleModifier ->
                    FolderEntryCard(
                        dragState = null,
                        item = entry,
                        dragDescription = "drag",
                        onClick = {},
                        isDragging = isDragging,
                        reorderHandleModifier = dragHandleModifier
                    ) {
                        Text("E${entry.id}")
                    }
                }
            )
        }

        val cards = rule.onAllNodesWithTag(FolderTestTags.ENTRY_CARD, useUnmergedTree = true)
        val lastIndex = cards.fetchSemanticsNodes().size - 1
        val draggedHandle = cards[lastIndex]
        val handleBounds = draggedHandle.getBoundsInRoot()
        val folderCardBounds = rule.onNodeWithTag(FolderTestTags.FOLDER_CARD, useUnmergedTree = true).getBoundsInRoot()
        val deltaY = (folderCardBounds.top + folderCardBounds.bottom) / 2f - (handleBounds.top + handleBounds.bottom) / 2f
        val deltaYPx = with(rule.density) { deltaY.toPx() }

        draggedHandle.performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, deltaYPx))
            up()
        }

        rule.runOnIdle {
            assertEquals(12L to 1L, moved)
        }
    }
}
