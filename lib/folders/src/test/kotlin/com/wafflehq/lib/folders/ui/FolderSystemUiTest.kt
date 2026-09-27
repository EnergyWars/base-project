package com.wafflehq.lib.folders.ui

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.folders.R
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.folders.FolderDeletionAction
import com.wafflehq.lib.folders.FolderTreeRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FolderSystemUiTest {

    @get:Rule
    val rule = createComposeRule()

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private fun str(id: Int) = context.getString(id)
    private val lastActivity = LocalDateTime.of(2026, 5, 20, 14, 30)

    @Test
    fun folderCard_showsNameCountAndLastActivity() {
        rule.setContent {
            FolderCard(
                name = "Backen",
                countText = "3 Einträge",
                lastActivityAt = lastActivity,
                isDropTarget = false,
                onOpen = {},
                onRename = {},
                onDelete = {},
                onPositioned = {}
            )
        }

        rule.onNodeWithText("Backen").assertIsDisplayed()
        rule.onNodeWithText("3 Einträge").assertIsDisplayed()
        rule.onNodeWithTag(FolderTestTags.FOLDER_LAST_ACTIVITY, useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithTag(FolderTestTags.FOLDER_MENU_BUTTON).assertIsDisplayed()
        rule.onAllNodesWithTag(FolderTestTags.FOLDER_EXPAND_BUTTON).assertCountEquals(0)
        rule.onAllNodesWithContentDescription(str(R.string.folder_reorder_handle)).assertCountEquals(0)
        rule.onNodeWithTag(FolderAppearanceTestTags.ICON_TILE, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun folderCard_disposeBounds_firesOnlyWhenRemovedNotOnRecomposition() {
        var disposeCount = 0
        var visible by mutableStateOf(true)
        var recomposeTrigger by mutableStateOf(0)
        rule.setContent {
            if (visible) {
                FolderCard(
                    name = "Backen $recomposeTrigger",
                    countText = "0",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {},
                    onDisposeBounds = { disposeCount++ }
                )
            }
        }

        rule.runOnIdle { recomposeTrigger++ }
        rule.runOnIdle { recomposeTrigger++ }
        rule.runOnIdle { assertEquals(0, disposeCount) }

        rule.runOnIdle { visible = false }
        rule.runOnIdle { assertEquals(1, disposeCount) }
    }

    @Test
    fun folderCard_openInvokesCallback() {
        var opened = false
        rule.setContent {
            FolderCard(
                name = "Backen",
                countText = "0",
                lastActivityAt = lastActivity,
                isDropTarget = false,
                onOpen = { opened = true },
                onRename = {},
                onDelete = {},
                onPositioned = {}
            )
        }

        rule.onNodeWithTag(FolderTestTags.FOLDER_CARD).performClick()

        assertTrue(opened)
    }

    @Test
    fun folderCard_menuContainsRenameExtraAndDeleteInOrder() {
        var renamed = false
        var deleted = false
        var extra = false
        rule.setContent {
            FolderCard(
                name = "Backen",
                countText = "0",
                lastActivityAt = lastActivity,
                isDropTarget = false,
                onOpen = {},
                onRename = { renamed = true },
                onDelete = { deleted = true },
                onPositioned = {},
                extraMenuItems = { dismiss ->
                    DropdownMenuItem(text = { Text("Extra") }, onClick = { dismiss(); extra = true })
                }
            )
        }

        rule.onNodeWithTag(FolderTestTags.FOLDER_MENU_BUTTON).performClick()
        rule.onNodeWithText(str(R.string.folder_rename)).assertIsDisplayed()
        rule.onNodeWithText("Extra").assertIsDisplayed()
        rule.onNodeWithText(str(R.string.folder_delete)).assertIsDisplayed()

        rule.onNodeWithText("Extra").performClick()
        assertTrue(extra)

        rule.onNodeWithTag(FolderTestTags.FOLDER_MENU_BUTTON).performClick()
        rule.onNodeWithText(str(R.string.folder_rename)).performClick()
        assertTrue(renamed)

        rule.onNodeWithTag(FolderTestTags.FOLDER_MENU_BUTTON).performClick()
        rule.onNodeWithText(str(R.string.folder_delete)).performClick()
        assertTrue(deleted)
    }

    @Test
    fun folderCard_customRenameLabelIsUsed() {
        rule.setContent {
            FolderCard(
                name = "Haushalt",
                countText = "0",
                lastActivityAt = lastActivity,
                isDropTarget = false,
                onOpen = {},
                onRename = {},
                onDelete = {},
                onPositioned = {},
                renameLabel = "Kategorie bearbeiten"
            )
        }

        rule.onNodeWithTag(FolderTestTags.FOLDER_MENU_BUTTON).performClick()

        rule.onNodeWithText("Kategorie bearbeiten").assertIsDisplayed()
    }

    @Test
    fun folderCard_expandButtonTogglesAndDescribesState() {
        var toggles = 0
        rule.setContent {
            FolderCard(
                name = "Backen",
                countText = "0",
                lastActivityAt = lastActivity,
                isDropTarget = false,
                onOpen = {},
                onRename = {},
                onDelete = {},
                onPositioned = {},
                isExpanded = true,
                onToggleExpand = { toggles++ }
            )
        }

        rule.onNodeWithContentDescription(str(R.string.folder_collapse)).assertIsDisplayed()
        rule.onNodeWithTag(FolderTestTags.FOLDER_EXPAND_BUTTON).performClick()

        assertEquals(1, toggles)
    }

    @Test
    fun folderCard_neverShowsADragIconAndMarksLongPressWhenModifierGiven() {
        rule.setContent {
            Column {
                FolderCard(
                    name = "Ziehbar",
                    countText = "0",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {},
                    reorderHandleModifier = Modifier
                )
                FolderCard(
                    name = "Fest",
                    countText = "0",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {}
                )
            }
        }

        rule.onAllNodesWithContentDescription(str(R.string.folder_reorder_handle)).assertCountEquals(0)
        rule.onAllNodesWithTag(FolderDragDefaults.HANDLE_TAG, useUnmergedTree = true).assertCountEquals(0)
        val cards = rule.onAllNodesWithTag(FolderTestTags.FOLDER_CARD)
        cards[0].assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))
        cards[1].assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnLongClick))
    }

    @Test
    fun folderCard_dropTargetAndExpandedStatesRenderNameAndCount() {
        rule.setContent {
            Column {
                FolderCard(
                    name = "Ziel",
                    countText = "2 Einträge",
                    lastActivityAt = lastActivity,
                    isDropTarget = true,
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {}
                )
                FolderCard(
                    name = "Offen",
                    countText = "5 Einträge",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    isExpanded = true,
                    onToggleExpand = {},
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {}
                )
            }
        }

        rule.onNodeWithText("Ziel").assertIsDisplayed()
        rule.onNodeWithText("2 Einträge").assertIsDisplayed()
        rule.onNodeWithText("Offen").assertIsDisplayed()
        rule.onNodeWithText("5 Einträge").assertIsDisplayed()
        rule.onNodeWithContentDescription(str(R.string.folder_collapse)).assertIsDisplayed()
    }

    @Test
    fun folderDeleteDialog_showsTitleMessageAndBothOptions() {
        rule.setContent { FolderDeleteDialog(onConfirm = {}, onDismiss = {}) }

        rule.onNodeWithText(str(R.string.folder_delete_confirm_title)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.folder_delete_choice_message)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.folder_delete_option_move_up)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.folder_delete_option_delete_all)).assertIsDisplayed()
    }

    @Test
    fun folderNameDialog_showsGivenTitleAndInitialName() {
        rule.setContent {
            FolderNameDialog(title = "Ordner anlegen", initialName = "Backen", onConfirm = {}, onDismiss = {})
        }

        rule.onNodeWithText("Ordner anlegen").assertIsDisplayed()
        rule.onNodeWithText("Backen").assertIsDisplayed()
    }

    @Test
    fun folderCard_customIconOverridesDefaultFolderIcon() {
        rule.setContent {
            FolderCard(
                name = "Backen",
                countText = "0",
                lastActivityAt = lastActivity,
                isDropTarget = false,
                onOpen = {},
                onRename = {},
                onDelete = {},
                onPositioned = {},
                icon = Icons.Default.Cake
            )
        }

        rule.onNodeWithText("Backen").assertIsDisplayed()
    }

    @Test
    fun folderEntryCard_hasNoDragIconButIsLongPressDraggableAndKeepsClickAndMenu() {
        var clicked = false
        var deleted = false
        rule.setContent {
            val dragState = rememberFolderDragState<String>()
            FolderEntryCard(
                dragState = dragState,
                item = "note",
                dragDescription = "drag",
                onDrop = { _, _ -> },
                onClick = { clicked = true },
                menuItems = { dismiss ->
                    DropdownMenuItem(text = { Text("Löschen") }, onClick = { dismiss(); deleted = true })
                }
            ) {
                Text("Inhalt")
            }
        }

        rule.onNodeWithText("Inhalt").assertIsDisplayed()
        rule.onAllNodesWithTag(FolderDragDefaults.HANDLE_TAG, useUnmergedTree = true).assertCountEquals(0)
        rule.onAllNodesWithContentDescription("drag").assertCountEquals(0)
        rule.onNodeWithTag(FolderTestTags.ENTRY_CARD)
            .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))
            .performClick()
        assertTrue(clicked)

        rule.onNodeWithTag(FolderTestTags.ENTRY_MENU_BUTTON).performClick()
        rule.onNodeWithText("Löschen").performClick()
        assertTrue(deleted)
    }

    @Test
    fun folderEntryCard_withDragDisabled_hasNoLongPressSemantics() {
        rule.setContent {
            val dragState = rememberFolderDragState<String>()
            FolderEntryCard(
                dragState = dragState,
                item = "note",
                dragDescription = "drag",
                onDrop = { _, _ -> },
                onClick = {},
                dragEnabled = false
            ) {
                Text("Inhalt")
            }
        }

        rule.onNodeWithTag(FolderTestTags.ENTRY_CARD)
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnLongClick))
    }

    @Test
    fun folderEntryCard_withoutDragStateAndMenu_hidesHandleAndMenu() {
        rule.setContent {
            FolderEntryCard<String>(
                dragState = null,
                item = "note",
                dragDescription = "drag",
                onDrop = { _, _ -> },
                onClick = {}
            ) {
                Text("Inhalt")
            }
        }

        rule.onAllNodesWithTag(FolderDragDefaults.HANDLE_TAG, useUnmergedTree = true).assertCountEquals(0)
        rule.onAllNodesWithTag(FolderTestTags.ENTRY_MENU_BUTTON).assertCountEquals(0)
    }

    @Test
    fun folderTreeItems_rendersFoldersAndEntriesWithIndent() {
        val rows: List<FolderTreeRow<String, String>> = listOf(
            FolderTreeRow.FolderRow("Ordner A", depth = 0, isExpanded = true),
            FolderTreeRow.EntryRow("Eintrag 1", depth = 1),
            FolderTreeRow.FolderRow("Ordner B", depth = 0, isExpanded = false)
        )
        rule.setContent {
            androidx.compose.foundation.lazy.LazyColumn {
                folderTreeItems(
                    rows = rows,
                    folderKey = { it },
                    entryKey = { it },
                    folderContent = { row, modifier -> Text("F:${row.folder}:${row.isExpanded}", modifier = modifier) },
                    entryContent = { entry, modifier -> Text("E:$entry", modifier = modifier) }
                )
            }
        }

        rule.onNodeWithText("F:Ordner A:true").assertIsDisplayed()
        rule.onNodeWithText("E:Eintrag 1").assertIsDisplayed()
        rule.onNodeWithText("F:Ordner B:false").assertIsDisplayed()
    }

    @Test
    fun folderTreeItems_indentsNestedRowsByDepth() {
        val rows: List<FolderTreeRow<String, String>> = listOf(
            FolderTreeRow.FolderRow("Ordner A", depth = 0, isExpanded = true),
            FolderTreeRow.FolderRow("Ordner B", depth = 1, isExpanded = true),
            FolderTreeRow.EntryRow("Eintrag 1", depth = 2)
        )
        rule.setContent {
            androidx.compose.foundation.lazy.LazyColumn {
                folderTreeItems(
                    rows = rows,
                    folderKey = { it },
                    entryKey = { it },
                    folderContent = { row, modifier -> Text("F:${row.folder}", modifier = modifier) },
                    entryContent = { entry, modifier -> Text("E:$entry", modifier = modifier) }
                )
            }
        }

        rule.onNodeWithText("F:Ordner A").assertLeftPositionInRootIsEqualTo(0.dp)
        rule.onNodeWithText("F:Ordner B").assertLeftPositionInRootIsEqualTo(FolderListDefaults.nestedFrameStep)
        rule.onNodeWithText("E:Eintrag 1").assertLeftPositionInRootIsEqualTo(FolderListDefaults.nestedFrameStep * 2)
    }

    @Test
    fun folderNestedFrame_wrapsContentWithBorderInsetOnAllSides() {
        rule.setContent {
            Box(Modifier.folderNestedFrame(Color.Red).size(width = 120.dp, height = 40.dp))
        }

        rule.onNodeWithTag(FolderTestTags.NESTED_FRAME)
            .assertWidthIsEqualTo(120.dp + FolderListDefaults.nestedFrameInset * 2)
            .assertHeightIsEqualTo(40.dp + FolderListDefaults.nestedFrameInset * 2)
            .assertLeftPositionInRootIsEqualTo(FolderListDefaults.nestedFrameIndent)
    }

    @Test
    fun folderFrameLevels_marksFirstAndLastRowOfEveryOpenGroup() {
        val rows: List<FolderTreeRow<String, String>> = listOf(
            FolderTreeRow.FolderRow("A", depth = 0, isExpanded = true),
            FolderTreeRow.FolderRow("B", depth = 1, isExpanded = true),
            FolderTreeRow.EntryRow("b1", depth = 2),
            FolderTreeRow.EntryRow("b2", depth = 2),
            FolderTreeRow.EntryRow("a1", depth = 1),
            FolderTreeRow.FolderRow("C", depth = 0, isExpanded = false)
        )

        assertEquals(emptyList<FolderFrameLevel>(), folderFrameLevels(rows, 0))
        assertEquals(listOf(FolderFrameLevel(isFirst = true, isLast = false)), folderFrameLevels(rows, 1))
        assertEquals(
            listOf(
                FolderFrameLevel(isFirst = false, isLast = false),
                FolderFrameLevel(isFirst = true, isLast = false)
            ),
            folderFrameLevels(rows, 2)
        )
        assertEquals(
            listOf(
                FolderFrameLevel(isFirst = false, isLast = false),
                FolderFrameLevel(isFirst = false, isLast = true)
            ),
            folderFrameLevels(rows, 3)
        )
        assertEquals(listOf(FolderFrameLevel(isFirst = false, isLast = true)), folderFrameLevels(rows, 4))
        assertEquals(emptyList<FolderFrameLevel>(), folderFrameLevels(rows, 5))
    }

    @Test
    fun folderFrameLevels_singleChildIsFirstAndLast() {
        val rows: List<FolderTreeRow<String, String>> = listOf(
            FolderTreeRow.FolderRow("A", depth = 0, isExpanded = true),
            FolderTreeRow.EntryRow("only", depth = 1)
        )

        assertEquals(listOf(FolderFrameLevel(isFirst = true, isLast = true)), folderFrameLevels(rows, 1))
    }

    @Test
    fun folderFlatFrame_padsRowsByNestingAndFrameEdges() {
        rule.setContent {
            Column {
                Box(
                    Modifier
                        .folderFlatFrame(listOf(FolderFrameLevel(isFirst = true, isLast = true)), Color.Red)
                        .testTag("single")
                        .size(width = 100.dp, height = 30.dp)
                )
                Box(
                    Modifier
                        .folderFlatFrame(listOf(FolderFrameLevel(isFirst = false, isLast = false)), Color.Red)
                        .testTag("middle")
                        .size(width = 100.dp, height = 30.dp)
                )
                Box(
                    Modifier
                        .folderFlatFrame(emptyList(), Color.Red)
                        .testTag("root")
                        .size(width = 100.dp, height = 30.dp)
                )
            }
        }

        rule.onNodeWithTag("single")
            .assertWidthIsEqualTo(100.dp)
            .assertHeightIsEqualTo(30.dp)
            .assertLeftPositionInRootIsEqualTo(FolderListDefaults.nestedFrameStep)
        rule.onNodeWithTag("middle").assertLeftPositionInRootIsEqualTo(FolderListDefaults.nestedFrameStep)
        rule.onNodeWithTag("root").assertLeftPositionInRootIsEqualTo(0.dp)
    }

    @Test
    fun folderCard_expandedAndCollapsedRenderWithDefaultAndCustomIcon() {
        var expanded by mutableStateOf(false)
        rule.setContent {
            Column {
                FolderCard(
                    name = "Backen",
                    countText = "0",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {},
                    isExpanded = expanded,
                    onToggleExpand = { expanded = !expanded }
                )
                FolderCard(
                    name = "Kuchen",
                    countText = "0",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {},
                    isExpanded = expanded,
                    icon = Icons.Default.Cake
                )
            }
        }

        rule.onNodeWithText("Backen").assertIsDisplayed()
        rule.onNodeWithContentDescription(str(R.string.folder_expand)).performClick()
        rule.onNodeWithContentDescription(str(R.string.folder_collapse)).assertIsDisplayed()
        rule.onNodeWithText("Kuchen").assertIsDisplayed()
    }

    @Test
    fun folderNameDialog_disablesSaveForBlankNameAndTrimsInput() {
        var confirmed: String? = null
        rule.setContent {
            FolderNameDialog(
                title = "Neuer Ordner",
                initialName = "",
                onConfirm = { confirmed = it },
                onDismiss = {}
            )
        }

        rule.onNodeWithText(str(UiCoreR.string.uicore_save)).assertIsNotEnabled()
        rule.onNodeWithText(str(R.string.folder_name_label)).performTextInput("  Backen  ")
        rule.onNodeWithText(str(UiCoreR.string.uicore_save)).assertIsEnabled().performClick()

        assertEquals("Backen", confirmed)
    }

    @Test
    fun folderDeleteDialog_defaultsToDeleteContentsAndSupportsMoveUp() {
        var action: FolderDeletionAction? = null
        rule.setContent {
            FolderDeleteDialog(onConfirm = { action = it }, onDismiss = {})
        }

        rule.onNodeWithText(str(R.string.folder_delete_option_delete_all)).assertIsSelected()
        rule.onNodeWithText(str(R.string.folder_delete_option_move_up)).assertIsNotSelected()
        rule.onNodeWithText(str(UiCoreR.string.uicore_delete)).performClick()
        assertEquals(FolderDeletionAction.DELETE_CONTENTS, action)

        rule.onNodeWithText(str(R.string.folder_delete_option_move_up)).performClick()
        rule.onNodeWithText(str(UiCoreR.string.uicore_delete)).performClick()
        assertEquals(FolderDeletionAction.MOVE_CONTENTS_UP, action)
    }

    @Test
    fun folderActionFabs_showsFolderFabOnlyWhenCallbackGiven() {
        var added = false
        var folderAdded = false
        rule.setContent {
            FolderActionFabs(
                onAddItem = { added = true },
                addItemDescription = "Eintrag hinzufügen",
                onAddFolder = { folderAdded = true }
            )
        }

        rule.onNodeWithContentDescription("Eintrag hinzufügen").performClick()
        rule.onNodeWithContentDescription(str(R.string.folder_new)).performClick()

        assertTrue(added)
        assertTrue(folderAdded)
    }

    @Test
    fun folderActionFabs_usesCustomItemIconAndStillTriggersCallback() {
        var added = false
        rule.setContent {
            FolderActionFabs(
                onAddItem = { added = true },
                addItemDescription = "Kiste anlegen",
                onAddFolder = null,
                addItemIcon = Icons.Filled.Inventory2
            )
        }

        rule.onNodeWithContentDescription("Kiste anlegen").assertIsDisplayed().performClick()

        assertTrue(added)
    }

    @Test
    fun folderActionFabs_hidesFolderFabWithoutCallback() {
        rule.setContent {
            FolderActionFabs(onAddItem = {}, addItemDescription = "Eintrag hinzufügen", onAddFolder = null)
        }

        rule.onNodeWithContentDescription("Eintrag hinzufügen").assertIsDisplayed()
        rule.onAllNodesWithContentDescription(str(R.string.folder_new)).assertCountEquals(0)
    }

    @Test
    fun folderOptionsAction_offersRenameAndDelete() {
        var renamed = false
        var deleted = false
        rule.setContent {
            FolderOptionsAction(onRename = { renamed = true }, onDelete = { deleted = true })
        }

        rule.onNodeWithContentDescription(str(R.string.folder_options)).performClick()
        rule.onNodeWithText(str(R.string.folder_rename)).performClick()
        rule.onNodeWithContentDescription(str(R.string.folder_options)).performClick()
        rule.onNodeWithText(str(R.string.folder_delete)).performClick()

        assertTrue(renamed)
        assertTrue(deleted)
    }

    @Test
    fun folderBreadcrumb_rendersAllItemsAndOnlyAncestorsAreClickable() {
        var jumpedToRoot = false
        var jumpedToSprachen = false
        rule.setContent {
            FolderBreadcrumb(
                items = listOf(
                    FolderBreadcrumbItem("Lernkarten") { jumpedToRoot = true },
                    FolderBreadcrumbItem("Sprachen") { jumpedToSprachen = true },
                    FolderBreadcrumbItem("Latein")
                )
            )
        }

        rule.onNodeWithTag(FolderTestTags.breadcrumbItem(0)).assertIsDisplayed()
        rule.onNodeWithTag(FolderTestTags.breadcrumbItem(1)).assertIsDisplayed()
        rule.onNodeWithTag(FolderTestTags.breadcrumbItem(2)).assertIsDisplayed()

        rule.onNodeWithTag(FolderTestTags.breadcrumbItem(0)).performClick()
        assertTrue(jumpedToRoot)
        rule.onNodeWithTag(FolderTestTags.breadcrumbItem(1)).performClick()
        assertTrue(jumpedToSprachen)
    }

    @Test
    fun folderBreadcrumb_wrapsOntoMultipleLinesInsteadOfScrolling() {
        rule.setContent {
            Box(modifier = Modifier.width(200.dp)) {
                FolderBreadcrumb(
                    items = listOf(
                        FolderBreadcrumbItem("Lernkarten") {},
                        FolderBreadcrumbItem("Naturwissenschaften") {},
                        FolderBreadcrumbItem("Biologie") {},
                        FolderBreadcrumbItem("Zellbiologie") {},
                        FolderBreadcrumbItem("Mitochondrien")
                    )
                )
            }
        }

        rule.onNodeWithTag(FolderTestTags.breadcrumbItem(0)).assertIsDisplayed()
        rule.onNodeWithTag(FolderTestTags.breadcrumbItem(4)).assertIsDisplayed()
    }

    @Test
    fun folderInsertionLine_visibleTrue_isDisplayed() {
        rule.setContent {
            FolderInsertionLine(visible = true)
        }

        rule.onNodeWithTag(FolderTestTags.INSERTION_LINE, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun folderInsertionLine_visibleFalse_isNotRendered() {
        rule.setContent {
            FolderInsertionLine(visible = false)
        }

        rule.onAllNodesWithTag(FolderTestTags.INSERTION_LINE, useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun folderBreadcrumb_hidesItselfWithSingleItem() {
        rule.setContent {
            FolderBreadcrumb(items = listOf(FolderBreadcrumbItem("Lernkarten")))
        }

        rule.onAllNodesWithTag(FolderTestTags.BREADCRUMB).assertCountEquals(0)
    }
}
