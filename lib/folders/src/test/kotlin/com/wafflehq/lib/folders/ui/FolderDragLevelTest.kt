package com.wafflehq.lib.folders.ui

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private data class TestFolder(val id: Long, val parentId: Long?, val sortOrder: Int)
private data class TestEntry(val id: Long, val folderId: Long?, val sortOrder: Int)

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FolderDragLevelTest {

    @get:Rule
    val rule = createComposeRule()

    private var movedEntry: Pair<Long, Long?>? = null
    private var movedFolder: Pair<Long, Long?>? = null
    private var reorder: Triple<Long?, Map<Long, Int>, Map<Long, Int>>? = null

    private fun setLevel(
        folders: List<TestFolder>,
        entries: List<TestEntry>,
        expandedFolderIds: Set<Long> = emptySet(),
        allowUnfile: Boolean = true,
        supportsFolderMoves: Boolean = true,
        entryHeight: Dp = 48.dp,
        canNestFolder: (TestFolder, Long?) -> Boolean = { folder, target -> target != folder.id && target != folder.parentId }
    ) {
        rule.setContent {
            FolderDragLevel(
                folders = folders,
                entries = entries,
                parentId = null,
                expandedFolderIds = expandedFolderIds,
                folderId = { it.id },
                folderParentId = { it.parentId },
                folderSortOrder = { it.sortOrder },
                entryFolderId = { it.folderId },
                entrySortOrder = { it.sortOrder },
                entryId = { it.id },
                onReorderLevel = { parentId, folderOrders, entryOrders -> reorder = Triple(parentId, folderOrders, entryOrders) },
                onMoveEntry = { entry, target -> movedEntry = entry.id to target },
                onMoveFolder = if (supportsFolderMoves) {
                    { folder, target -> movedFolder = folder.id to target }
                } else {
                    null
                },
                canNestFolder = canNestFolder,
                allowUnfile = allowUnfile,
                folderContent = { folder, _, _, dragHandleModifier ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .then(dragHandleModifier)
                            .testTag("folder_${folder.id}")
                    ) { Text("F${folder.id}") }
                },
                entryContent = { entry, _, dragHandleModifier ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(entryHeight)
                            .then(dragHandleModifier)
                            .testTag("entry_${entry.id}")
                    ) { Text("E${entry.id}") }
                }
            )
        }
    }

    private fun yAt(tag: String, fraction: Float): Dp {
        val bounds = rule.onNodeWithTag(tag).getBoundsInRoot()
        return bounds.top + (bounds.bottom - bounds.top) * fraction
    }

    private fun dragPx(fromTag: String, toTag: String, toFraction: Float): Float =
        with(rule.density) { (yAt(toTag, toFraction) - yAt(fromTag, 0.5f)).toPx() }

    private fun dragInOneMove(tag: String, deltaPx: Float) {
        rule.onNodeWithTag(tag).performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, deltaPx))
            up()
        }
    }

    @Test
    fun draggingEntryIntoFolderCenter_invokesOnMoveEntry() {
        setLevel(
            folders = listOf(TestFolder(1L, null, 1)),
            entries = listOf(TestEntry(10L, null, 0)),
            canNestFolder = { _, _ -> true }
        )

        dragInOneMove("entry_10", dragPx("entry_10", "folder_1", 0.5f))

        rule.runOnIdle {
            assertEquals(10L to 1L, movedEntry)
        }
    }

    @Test
    fun draggingFolderIntoOtherFolderCenter_invokesOnMoveFolder() {
        setLevel(
            folders = listOf(TestFolder(1L, null, 0), TestFolder(2L, null, 1)),
            entries = emptyList(),
            canNestFolder = { _, _ -> true }
        )

        dragInOneMove("folder_1", dragPx("folder_1", "folder_2", 0.5f))

        rule.runOnIdle {
            assertEquals(1L to 2L, movedFolder)
        }
    }

    @Test
    fun draggingTallEntryStepwiseIntoShortFolderCenter_nestsInsteadOfPushingFolderAway() {
        setLevel(
            folders = listOf(TestFolder(1L, null, 1)),
            entries = listOf(TestEntry(10L, null, 0)),
            entryHeight = 200.dp
        )
        val steps = 4
        val stepPx = dragPx("entry_10", "folder_1", 0.5f) / steps

        rule.onNodeWithTag("entry_10").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            repeat(steps) {
                moveBy(Offset(0f, stepPx))
                advanceEventTime(16)
            }
            up()
        }

        rule.runOnIdle {
            assertEquals(10L to 1L, movedEntry)
            assertNull(reorder)
        }
    }

    @Test
    fun draggingEntryOutOfExpandedFolderBelowRootEntry_movesItToRootLevelAtThatPosition() {
        setLevel(
            folders = listOf(TestFolder(1L, null, 0)),
            entries = listOf(TestEntry(10L, 1L, 0), TestEntry(20L, null, 1)),
            expandedFolderIds = setOf(1L)
        )

        dragInOneMove("entry_10", dragPx("entry_10", "entry_20", 0.75f))

        rule.runOnIdle {
            assertEquals(10L to null, movedEntry)
            assertEquals(Triple(null, mapOf(1L to 0), mapOf(20L to 1, 10L to 2)), reorder)
        }
    }

    @Test
    fun draggingEntryOutOfExpandedFolder_withoutAllowUnfile_keepsItInFolder() {
        setLevel(
            folders = listOf(TestFolder(1L, null, 0)),
            entries = listOf(TestEntry(10L, 1L, 0), TestEntry(20L, null, 1)),
            expandedFolderIds = setOf(1L),
            allowUnfile = false
        )

        dragInOneMove("entry_10", dragPx("entry_10", "entry_20", 0.75f))

        rule.runOnIdle {
            assertNull(movedEntry)
            assertNull(reorder)
        }
    }

    @Test
    fun draggingRootEntryIntoExpandedFolderContent_movesItIntoThatFolderAtThatPosition() {
        setLevel(
            folders = listOf(TestFolder(1L, null, 0)),
            entries = listOf(TestEntry(10L, 1L, 0), TestEntry(20L, null, 1)),
            expandedFolderIds = setOf(1L)
        )

        dragInOneMove("entry_20", dragPx("entry_20", "entry_10", 0.75f))

        rule.runOnIdle {
            assertEquals(20L to 1L, movedEntry)
            assertEquals(Triple(1L, emptyMap<Long, Int>(), mapOf(10L to 0, 20L to 1)), reorder)
        }
    }

    @Test
    fun draggingSubfolderOutOfExpandedFolder_movesItToRootLevel() {
        setLevel(
            folders = listOf(TestFolder(1L, null, 0), TestFolder(2L, 1L, 0)),
            entries = listOf(TestEntry(20L, null, 1)),
            expandedFolderIds = setOf(1L)
        )

        dragInOneMove("folder_2", dragPx("folder_2", "entry_20", 0.75f))

        rule.runOnIdle {
            assertEquals(2L to null, movedFolder)
            assertEquals(Triple(null, mapOf(1L to 0, 2L to 2), mapOf(20L to 1)), reorder)
        }
    }

    @Test
    fun crossLevelDrag_showsInsertionLineInTargetLevel() {
        setLevel(
            folders = listOf(TestFolder(1L, null, 0)),
            entries = listOf(TestEntry(10L, 1L, 0), TestEntry(20L, null, 1)),
            expandedFolderIds = setOf(1L)
        )
        val deltaPx = dragPx("entry_10", "entry_20", 0.75f)

        rule.onNodeWithTag("entry_10").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, deltaPx))
        }

        rule.onAllNodesWithTag(FolderTestTags.INSERTION_LINE, useUnmergedTree = true).assertCountEquals(1)

        rule.onNodeWithTag("entry_10").performTouchInput { up() }
    }

    @Test
    fun withoutOnMoveFolder_draggingFolderOntoOtherFolderCenter_doesNotNest() {
        setLevel(
            folders = listOf(TestFolder(1L, null, 0), TestFolder(2L, null, 1)),
            entries = emptyList(),
            supportsFolderMoves = false,
            canNestFolder = { _, _ -> true }
        )

        dragInOneMove("folder_1", dragPx("folder_1", "folder_2", 0.5f))

        rule.runOnIdle {
            assertNull(movedFolder)
        }
    }
}
