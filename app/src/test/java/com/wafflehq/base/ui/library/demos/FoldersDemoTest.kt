package com.wafflehq.base.ui.library.demos

import android.app.Application
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.folders.R as FoldersR
import com.wafflehq.lib.folders.ui.FolderTestTags
import com.wafflehq.lib.uicore.R as UiCoreR
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FoldersDemoTest : LibraryDemoTest() {

    private fun showDemo(): FoldersDemoModel {
        val model = FoldersDemoModel.sample { context.getString(it) }
        show { FoldersDemo(model = remember { model }) }
        return model
    }

    private fun folderCards() = rule.onAllNodesWithTag(FolderTestTags.FOLDER_CARD)

    @Test
    fun rendersRootFoldersAndUnfiledEntries() {
        showDemo()

        node(DemoTags.section("folders")).assertExists()
        assertEquals(3, folderCards().fetchSemanticsNodes().size)
        assertEquals(1, rule.onAllNodesWithTag(FolderTestTags.ENTRY_CARD).fetchSemanticsNodes().size)
        rule.onNodeWithText(string(R.string.libex_folders_sample_ideas)).assertExists()
    }

    @Test
    fun tappingAFolderRevealsItsChildren() {
        showDemo()

        rule.onNodeWithText(string(R.string.libex_folders_sample_recipes)).performClick()

        rule.onNodeWithText(string(R.string.libex_folders_sample_soups)).assertExists()
        rule.onNodeWithText(string(R.string.libex_folders_sample_pasta)).assertExists()
        assertEquals(4, folderCards().fetchSemanticsNodes().size)
    }

    @Test
    fun breadcrumbAppearsForANestedSelection() {
        showDemo()
        rule.onNodeWithText(string(R.string.libex_folders_sample_recipes)).performClick()

        node(FolderTestTags.BREADCRUMB).assertExists()
        node(FolderTestTags.breadcrumbItem(0)).assertTextEquals(string(R.string.libex_folders_root))
        node(FolderTestTags.breadcrumbItem(1)).assertTextEquals(string(R.string.libex_folders_sample_recipes))
    }

    @Test
    fun newFolderDialogAddsAFolderCard() {
        val model = showDemo()

        click(FoldersTags.NEW_FOLDER)
        rule.onNode(hasSetTextAction()).performTextInput("Snacks")
        rule.onNodeWithText(string(UiCoreR.string.uicore_save)).performClick()

        rule.waitUntil(5_000) { folderCards().fetchSemanticsNodes().size == 4 }
        rule.onNodeWithText("Snacks").assertExists()
        assertEquals(5, model.folders.size)
    }

    @Test
    fun deleteDialogRemovesTheFolder() {
        val model = showDemo()

        rule.onAllNodesWithTag(FolderTestTags.FOLDER_MENU_BUTTON)[0].performClick()
        rule.onNodeWithText(string(FoldersR.string.folder_delete)).performClick()
        rule.onNodeWithText(string(UiCoreR.string.uicore_delete)).performClick()

        rule.waitUntil(5_000) { folderCards().fetchSemanticsNodes().size == 2 }
        assertEquals(3, model.folders.size)
    }

    @Test
    fun renameDialogChangesTheFolderName() {
        val model = showDemo()

        rule.onAllNodesWithTag(FolderTestTags.FOLDER_MENU_BUTTON)[0].performClick()
        rule.onNodeWithText(string(FoldersR.string.folder_rename)).performClick()
        rule.onNode(hasSetTextAction()).performTextReplacement("Renamed")
        rule.onNodeWithText(string(UiCoreR.string.uicore_save)).performClick()

        rule.waitUntil(5_000) { model.folderName(FoldersDemoModel.ARCHIVE) == "Renamed" }
        assertEquals("Renamed", model.folderName(FoldersDemoModel.ARCHIVE))
    }
}
