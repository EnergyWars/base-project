package com.wafflehq.lib.folders.ui

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.folders.FolderAppearance
import com.wafflehq.lib.folders.FolderAppearanceKey
import com.wafflehq.lib.folders.FolderAppearanceStore
import com.wafflehq.lib.folders.FolderTreeRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private data class FrameFolder(val id: Long, val parentId: Long?, val sortOrder: Int)
private data class FrameEntry(val id: Long, val folderId: Long?, val sortOrder: Int)

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FolderNestedFrameTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setLevel(
        folders: List<FrameFolder>,
        entries: List<FrameEntry>,
        expanded: Set<Long>,
        expandedContent: (@Composable ColumnScope.(FrameFolder) -> Unit)? = null
    ) {
        rule.setContent {
            FolderDragLevel(
                folders = folders,
                entries = entries,
                parentId = null,
                expandedFolderIds = expanded,
                folderId = { it.id },
                folderParentId = { it.parentId },
                folderSortOrder = { it.sortOrder },
                entryFolderId = { it.folderId },
                entrySortOrder = { it.sortOrder },
                entryId = { it.id },
                onReorderLevel = { _, _, _ -> },
                onMoveEntry = { _, _ -> },
                expandedContent = expandedContent,
                folderContent = { folder, _, _, _ ->
                    Box(Modifier.fillMaxWidth().height(60.dp).testTag("folder_${folder.id}")) { Text("F${folder.id}") }
                },
                entryContent = { entry, _, _ ->
                    Box(Modifier.fillMaxWidth().height(40.dp).testTag("entry_${entry.id}")) { Text("E${entry.id}") }
                }
            )
        }
    }

    @Test
    fun expandedFolderWithEntries_wrapsItsContentInOneFrameBelowTheHeader() {
        setLevel(
            folders = listOf(FrameFolder(1L, null, 0)),
            entries = listOf(FrameEntry(10L, 1L, 0), FrameEntry(11L, 1L, 1)),
            expanded = setOf(1L)
        )

        rule.onAllNodesWithTag(FolderTestTags.NESTED_FRAME).assertCountEquals(1)
        val frame = rule.onNodeWithTag(FolderTestTags.NESTED_FRAME).getBoundsInRoot()
        val header = rule.onNodeWithTag("folder_1").getBoundsInRoot()
        val first = rule.onNodeWithTag("entry_10").getBoundsInRoot()
        val last = rule.onNodeWithTag("entry_11").getBoundsInRoot()
        assertTrue(frame.top >= header.bottom)
        assertTrue(frame.left < first.left)
        assertTrue(frame.right > first.right)
        assertTrue(frame.top < first.top)
        assertTrue(frame.bottom > last.bottom)
    }

    @Test
    fun collapsedFolder_hasNoFrame() {
        setLevel(
            folders = listOf(FrameFolder(1L, null, 0)),
            entries = listOf(FrameEntry(10L, 1L, 0)),
            expanded = emptySet()
        )

        rule.onAllNodesWithTag(FolderTestTags.NESTED_FRAME).assertCountEquals(0)
    }

    @Test
    fun expandedEmptyFolderWithoutExtraContent_hasNoFrame() {
        setLevel(
            folders = listOf(FrameFolder(1L, null, 0)),
            entries = emptyList(),
            expanded = setOf(1L)
        )

        rule.onAllNodesWithTag(FolderTestTags.NESTED_FRAME).assertCountEquals(0)
    }

    @Test
    fun expandedEmptyFolderWithExtraContent_framesTheExtraContent() {
        setLevel(
            folders = listOf(FrameFolder(1L, null, 0)),
            entries = emptyList(),
            expanded = setOf(1L),
            expandedContent = { folder -> Text("extra ${folder.id}") }
        )

        rule.onAllNodesWithTag(FolderTestTags.NESTED_FRAME).assertCountEquals(1)
        rule.onNodeWithText("extra 1").assertIsDisplayed()
    }

    @Test
    fun nestedExpandedFolders_getOneFramePerLevel() {
        setLevel(
            folders = listOf(FrameFolder(1L, null, 0), FrameFolder(2L, 1L, 0)),
            entries = listOf(FrameEntry(10L, 2L, 0)),
            expanded = setOf(1L, 2L)
        )

        rule.onAllNodesWithTag(FolderTestTags.NESTED_FRAME).assertCountEquals(2)
    }

    @Test
    fun folderTreeItems_resolvesFrameColorFromTheOwningAncestorFolders() {
        val rows: List<FolderTreeRow<String, String>> = listOf(
            FolderTreeRow.FolderRow("A", depth = 0, isExpanded = true),
            FolderTreeRow.FolderRow("B", depth = 1, isExpanded = true),
            FolderTreeRow.EntryRow("e", depth = 2)
        )
        val requested = mutableListOf<String>()
        rule.setContent {
            LazyColumn {
                folderTreeItems(
                    rows = rows,
                    folderKey = { it },
                    entryKey = { it },
                    folderContent = { row, modifier -> Text("F:${row.folder}", modifier = modifier) },
                    entryContent = { entry, modifier -> Text("E:$entry", modifier = modifier) },
                    frameColor = { folder ->
                        requested += folder
                        Color.Green
                    }
                )
            }
        }

        rule.onNodeWithText("E:e").assertIsDisplayed()
        rule.runOnIdle {
            assertTrue(requested.contains("A"))
            assertTrue(requested.contains("B"))
            assertEquals(setOf("A", "B"), requested.toSet())
        }
    }

    @Test
    fun appearanceScopeFromFolderDragLevel_isProvidedToTheFolderContent() {
        val store = object : FolderAppearanceStore {
            override val appearances: StateFlow<Map<FolderAppearanceKey, FolderAppearance>> = MutableStateFlow(
                mapOf(FolderAppearanceKey("notes", 1L) to FolderAppearance(colorArgb = Color.Red.toArgb()))
            )

            override fun save(key: FolderAppearanceKey, appearance: FolderAppearance) = Unit
        }
        var scopeSeen: String? = null
        rule.setContent {
            CompositionLocalProvider(LocalFolderAppearanceStore provides store) {
                FolderDragLevel(
                    folders = listOf(FrameFolder(1L, null, 0)),
                    entries = emptyList<FrameEntry>(),
                    parentId = null,
                    expandedFolderIds = emptySet(),
                    folderId = { it.id },
                    folderParentId = { it.parentId },
                    folderSortOrder = { it.sortOrder },
                    entryFolderId = { it.folderId },
                    entrySortOrder = { it.sortOrder },
                    entryId = { it.id },
                    onReorderLevel = { _, _, _ -> },
                    onMoveEntry = { _, _ -> },
                    appearanceScope = "notes",
                    folderContent = { _, _, _, _ ->
                        scopeSeen = LocalFolderAppearanceScope.current
                        Text("content")
                    },
                    entryContent = { _, _, _ -> }
                )
            }
        }

        rule.runOnIdle { assertEquals("notes", scopeSeen) }
    }
}
