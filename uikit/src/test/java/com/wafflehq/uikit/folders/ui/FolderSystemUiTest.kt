package com.wafflehq.uikit.folders.ui

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.uikit.R
import com.wafflehq.uikit.folders.FolderDeletionAction
import com.wafflehq.uikit.folders.FolderTreeRow
import com.wafflehq.uikit.theme.AppTheme
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
            AppTheme {
                FolderCard(
                    name = "Backen",
                    countText = "3 Einträge",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {},
                )
            }
        }

        rule.onNodeWithText("Backen").assertIsDisplayed()
        rule.onNodeWithText("3 Einträge").assertIsDisplayed()
        rule.onNodeWithTag(FolderTestTags.FOLDER_LAST_ACTIVITY, useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithTag(FolderTestTags.FOLDER_MENU_BUTTON).assertIsDisplayed()
        rule.onAllNodesWithTag(FolderTestTags.FOLDER_EXPAND_BUTTON).assertCountEquals(0)
        rule.onAllNodesWithTag(FolderTestTags.FOLDER_REORDER_HANDLE, useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun folderCard_openInvokesCallback() {
        var opened = false
        rule.setContent {
            AppTheme {
                FolderCard(
                    name = "Backen",
                    countText = "0",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    onOpen = { opened = true },
                    onRename = {},
                    onDelete = {},
                    onPositioned = {},
                )
            }
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
            AppTheme {
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
                    },
                )
            }
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
            AppTheme {
                FolderCard(
                    name = "Haushalt",
                    countText = "0",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {},
                    renameLabel = "Kategorie bearbeiten",
                )
            }
        }

        rule.onNodeWithTag(FolderTestTags.FOLDER_MENU_BUTTON).performClick()

        rule.onNodeWithText("Kategorie bearbeiten").assertIsDisplayed()
    }

    @Test
    fun folderCard_expandButtonTogglesAndDescribesState() {
        var toggles = 0
        rule.setContent {
            AppTheme {
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
                    onToggleExpand = { toggles++ },
                )
            }
        }

        rule.onNodeWithContentDescription(str(R.string.folder_collapse)).assertIsDisplayed()
        rule.onNodeWithTag(FolderTestTags.FOLDER_EXPAND_BUTTON).performClick()

        assertEquals(1, toggles)
    }

    @Test
    fun folderCard_showsReorderHandleOnlyWhenModifierGiven() {
        rule.setContent {
            AppTheme {
                FolderCard(
                    name = "Backen",
                    countText = "0",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {},
                    reorderHandleModifier = Modifier,
                )
            }
        }

        rule.onNodeWithTag(FolderTestTags.FOLDER_REORDER_HANDLE, useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithContentDescription(str(R.string.folder_reorder_handle)).assertIsDisplayed()
    }

    @Test
    fun folderCard_customIconOverridesDefaultFolderIcon() {
        rule.setContent {
            AppTheme {
                FolderCard(
                    name = "Backen",
                    countText = "0",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {},
                    icon = Icons.Default.Cake,
                )
            }
        }

        rule.onNodeWithText("Backen").assertIsDisplayed()
    }

    @Test
    fun folderIconPickerDialog_selectingOptionInvokesOnSelect() {
        var selected: String? = null
        val options = listOf(
            FolderIconOption(key = "folder", icon = Icons.Default.Folder, label = "Ordner"),
            FolderIconOption(key = "cake", icon = Icons.Default.Cake, label = "Backen"),
        )
        rule.setContent {
            AppTheme {
                FolderIconPickerDialog(
                    options = options,
                    selectedKey = "folder",
                    onSelect = { selected = it },
                    onDismiss = {},
                )
            }
        }

        rule.onNodeWithContentDescription("Backen").performClick()

        assertEquals("cake", selected)
    }

    @Test
    fun folderIconPickerDialog_closeButtonInvokesOnDismiss() {
        var dismissed = false
        rule.setContent {
            AppTheme {
                FolderIconPickerDialog(
                    options = listOf(FolderIconOption(key = "folder", icon = Icons.Default.Folder, label = "Ordner")),
                    selectedKey = null,
                    onSelect = {},
                    onDismiss = { dismissed = true },
                )
            }
        }

        rule.onNodeWithText(str(R.string.folder_icon_picker_close)).performClick()

        assertTrue(dismissed)
    }

    @Test
    fun folderEntryCard_showsDragHandleContentAndMenu() {
        var clicked = false
        var deleted = false
        rule.setContent {
            AppTheme {
                val dragState = rememberFolderDragState<String>()
                FolderEntryCard(
                    dragState = dragState,
                    item = "note",
                    dragDescription = "drag",
                    onDrop = { _, _ -> },
                    onClick = { clicked = true },
                    menuItems = { dismiss ->
                        DropdownMenuItem(text = { Text("Löschen") }, onClick = { dismiss(); deleted = true })
                    },
                ) {
                    Text("Inhalt")
                }
            }
        }

        rule.onNodeWithText("Inhalt").assertIsDisplayed()
        rule.onNodeWithTag(FolderDragDefaults.HANDLE_TAG, useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithContentDescription("drag").assertIsDisplayed()
        rule.onNodeWithTag(FolderTestTags.ENTRY_CARD).performClick()
        assertTrue(clicked)

        rule.onNodeWithTag(FolderTestTags.ENTRY_MENU_BUTTON).performClick()
        rule.onNodeWithText("Löschen").performClick()
        assertTrue(deleted)
    }

    @Test
    fun folderEntryCard_withoutDragStateAndMenu_hidesHandleAndMenu() {
        rule.setContent {
            AppTheme {
                FolderEntryCard<String>(
                    dragState = null,
                    item = "note",
                    dragDescription = "drag",
                    onDrop = { _, _ -> },
                    onClick = {},
                ) {
                    Text("Inhalt")
                }
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
            FolderTreeRow.FolderRow("Ordner B", depth = 0, isExpanded = false),
        )
        rule.setContent {
            AppTheme {
                androidx.compose.foundation.lazy.LazyColumn {
                    folderTreeItems(
                        rows = rows,
                        folderKey = { it },
                        entryKey = { it },
                        folderContent = { row, modifier -> Text("F:${row.folder}:${row.isExpanded}", modifier = modifier) },
                        entryContent = { entry, modifier -> Text("E:$entry", modifier = modifier) },
                    )
                }
            }
        }

        rule.onNodeWithText("F:Ordner A:true").assertIsDisplayed()
        rule.onNodeWithText("E:Eintrag 1").assertIsDisplayed()
        rule.onNodeWithText("F:Ordner B:false").assertIsDisplayed()
    }

    @Test
    fun folderNameDialog_disablesSaveForBlankNameAndTrimsInput() {
        var confirmed: String? = null
        rule.setContent {
            AppTheme {
                FolderNameDialog(
                    title = "Neuer Ordner",
                    initialName = "",
                    onConfirm = { confirmed = it },
                    onDismiss = {},
                )
            }
        }

        rule.onNodeWithText(str(R.string.folder_save)).assertIsNotEnabled()
        rule.onNodeWithText(str(R.string.folder_name_label)).performTextInput("  Backen  ")
        rule.onNodeWithText(str(R.string.folder_save)).assertIsEnabled().performClick()

        assertEquals("Backen", confirmed)
    }

    @Test
    fun folderDeleteDialog_defaultsToMoveUpAndSupportsDeleteContents() {
        var action: FolderDeletionAction? = null
        rule.setContent {
            AppTheme {
                FolderDeleteDialog(onConfirm = { action = it }, onDismiss = {})
            }
        }

        rule.onNodeWithText(str(R.string.folder_delete)).performClick()
        assertEquals(FolderDeletionAction.MOVE_CONTENTS_UP, action)

        rule.onNodeWithText(str(R.string.folder_delete_option_delete_all)).performClick()
        rule.onNodeWithText(str(R.string.folder_delete)).performClick()
        assertEquals(FolderDeletionAction.DELETE_CONTENTS, action)
    }

    @Test
    fun folderActionFabs_showsFolderFabOnlyWhenCallbackGiven() {
        var added = false
        var folderAdded = false
        rule.setContent {
            AppTheme {
                FolderActionFabs(
                    onAddItem = { added = true },
                    addItemDescription = "Eintrag hinzufügen",
                    onAddFolder = { folderAdded = true },
                )
            }
        }

        rule.onNodeWithContentDescription("Eintrag hinzufügen").performClick()
        rule.onNodeWithContentDescription(str(R.string.folder_new)).performClick()

        assertTrue(added)
        assertTrue(folderAdded)
    }

    @Test
    fun folderActionFabs_hidesFolderFabWithoutCallback() {
        rule.setContent {
            AppTheme {
                FolderActionFabs(onAddItem = {}, addItemDescription = "Eintrag hinzufügen", onAddFolder = null)
            }
        }

        rule.onNodeWithContentDescription("Eintrag hinzufügen").assertIsDisplayed()
        rule.onAllNodesWithContentDescription(str(R.string.folder_new)).assertCountEquals(0)
    }

    @Test
    fun folderOptionsAction_offersRenameAndDelete() {
        var renamed = false
        var deleted = false
        rule.setContent {
            AppTheme {
                FolderOptionsAction(onRename = { renamed = true }, onDelete = { deleted = true })
            }
        }

        rule.onNodeWithContentDescription(str(R.string.folder_options)).performClick()
        rule.onNodeWithText(str(R.string.folder_rename)).performClick()
        rule.onNodeWithContentDescription(str(R.string.folder_options)).performClick()
        rule.onNodeWithText(str(R.string.folder_delete)).performClick()

        assertTrue(renamed)
        assertTrue(deleted)
    }
}
