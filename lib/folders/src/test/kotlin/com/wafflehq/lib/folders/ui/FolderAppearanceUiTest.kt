package com.wafflehq.lib.folders.ui

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.folders.FolderAppearance
import com.wafflehq.lib.folders.FolderAppearanceKey
import com.wafflehq.lib.folders.FolderAppearanceStore
import com.wafflehq.lib.folders.R
import com.wafflehq.lib.uicore.R as UiCoreR
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDateTime

private class FakeAppearanceStore(
    initial: Map<FolderAppearanceKey, FolderAppearance> = emptyMap()
) : FolderAppearanceStore {
    private val state = MutableStateFlow(initial)
    override val appearances: StateFlow<Map<FolderAppearanceKey, FolderAppearance>> = state
    val saved = mutableListOf<Pair<FolderAppearanceKey, FolderAppearance>>()

    override fun save(key: FolderAppearanceKey, appearance: FolderAppearance) {
        saved += key to appearance
        state.value = if (appearance.isDefault) state.value - key else state.value + (key to appearance)
    }
}

private class FakeColorPicker(private val picked: Color) : FolderColorPicker {
    var shownWith: Color? = null

    @Composable
    override fun Content(
        title: String,
        initialColor: Color,
        onColorSelected: (Color) -> Unit,
        onDismiss: () -> Unit
    ) {
        shownWith = initialColor
        Column {
            Text(title)
            Text("pick", modifier = Modifier.testTag("fake_pick").clickable { onColorSelected(picked) })
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FolderAppearanceUiTest {

    @get:Rule
    val rule = createComposeRule()

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private fun str(id: Int) = context.getString(id)
    private val lastActivity = LocalDateTime.of(2026, 5, 20, 14, 30)
    private val key = FolderAppearanceKey("notes", 7L)

    @Test
    fun catalog_hasUniqueKeysAndEveryLabelResolves() {
        val keys = FolderIconCatalog.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
        FolderIconCatalog.entries.forEach { assertTrue(str(it.labelRes).isNotBlank()) }
    }

    @Test
    fun catalog_keepsLegacyRecipeKeys() {
        listOf(
            "breakfast", "main_course", "soup", "salad", "snack", "dessert", "baking", "drink",
            "sauce", "grill", "restaurant", "fastfood", "cookie", "bakery", "seafood", "hot_beverage"
        ).forEach { assertNotNull(it, FolderIconCatalog.resolve(it)) }
    }

    @Test
    fun catalog_resolveReturnsNullForMissingOrUnknownKeys() {
        assertNull(FolderIconCatalog.resolve(null))
        assertNull(FolderIconCatalog.resolve("does_not_exist"))
        assertNotNull(FolderIconCatalog.resolve("folder"))
    }

    @Test
    fun folderAppearance_isDefaultOnlyWithoutIconAndColor() {
        assertTrue(FolderAppearance().isDefault)
        assertFalse(FolderAppearance(iconKey = "star").isDefault)
        assertFalse(FolderAppearance(colorArgb = 1).isDefault)
    }

    @Test
    fun rememberFolderAppearance_readsFromStoreAndFollowsUpdates() {
        val store = FakeAppearanceStore(mapOf(key to FolderAppearance(iconKey = "star")))
        var seen: FolderAppearance? = null
        rule.setContent {
            CompositionLocalProvider(LocalFolderAppearanceStore provides store) {
                seen = rememberFolderAppearance(key)
            }
        }

        rule.runOnIdle { assertEquals(FolderAppearance(iconKey = "star"), seen) }

        store.save(key, FolderAppearance(iconKey = "book", colorArgb = 5))
        rule.runOnIdle { assertEquals(FolderAppearance(iconKey = "book", colorArgb = 5), seen) }
    }

    @Test
    fun rememberFolderAppearance_withoutStoreOrKeyIsDefault() {
        var withoutStore: FolderAppearance? = null
        var withoutKey: FolderAppearance? = null
        val store = FakeAppearanceStore(mapOf(key to FolderAppearance(iconKey = "star")))
        rule.setContent {
            withoutStore = rememberFolderAppearance(key)
            CompositionLocalProvider(LocalFolderAppearanceStore provides store) {
                withoutKey = rememberFolderAppearance(null)
            }
        }

        rule.runOnIdle {
            assertTrue(withoutStore!!.isDefault)
            assertTrue(withoutKey!!.isDefault)
        }
    }

    @Test
    fun rememberFolderTint_prefersStoredColorOverDefault() {
        val stored = Color(0xFF123456)
        val store = FakeAppearanceStore(mapOf(key to FolderAppearance(colorArgb = stored.toArgb())))
        var tint: Color? = null
        var fallback: Color? = null
        rule.setContent {
            CompositionLocalProvider(LocalFolderAppearanceStore provides store) {
                tint = rememberFolderTint(key, Color.Red)
                fallback = rememberFolderTint(FolderAppearanceKey("notes", 99L), Color.Red)
            }
        }

        rule.runOnIdle {
            assertEquals(stored, tint)
            assertEquals(Color.Red, fallback)
        }
    }

    @Test
    fun dialog_selectingAnIconAndSavingReturnsTheDraft() {
        var saved: FolderAppearance? = null
        rule.setContent {
            FolderAppearanceDialog(
                current = FolderAppearance(),
                defaultIcon = Icons.Default.Folder,
                defaultColor = Color.Blue,
                onSave = { saved = it },
                onDismiss = {}
            )
        }

        rule.onNodeWithContentDescription(str(R.string.folder_icon_star)).performClick()
        rule.onNodeWithTag(FolderAppearanceTestTags.DIALOG_SAVE_BUTTON).performClick()

        assertEquals(FolderAppearance(iconKey = "star"), saved)
    }

    @Test
    fun dialog_resetIsDisabledForDefaultsAndClearsTheDraftOtherwise() {
        var saved: FolderAppearance? = null
        rule.setContent {
            FolderAppearanceDialog(
                current = FolderAppearance(iconKey = "star", colorArgb = 3),
                defaultIcon = Icons.Default.Folder,
                defaultColor = Color.Blue,
                onSave = { saved = it },
                onDismiss = {}
            )
        }

        rule.onNodeWithTag(FolderAppearanceTestTags.DIALOG_RESET_BUTTON).assertIsEnabled().performClick()
        rule.onNodeWithTag(FolderAppearanceTestTags.DIALOG_RESET_BUTTON).assertIsNotEnabled()
        rule.onNodeWithTag(FolderAppearanceTestTags.DIALOG_SAVE_BUTTON).performClick()

        assertEquals(FolderAppearance(), saved)
    }

    @Test
    fun dialog_cancelDismissesWithoutSaving() {
        var saved = false
        var dismissed = false
        rule.setContent {
            FolderAppearanceDialog(
                current = FolderAppearance(),
                defaultIcon = Icons.Default.Folder,
                defaultColor = Color.Blue,
                onSave = { saved = true },
                onDismiss = { dismissed = true }
            )
        }

        rule.onNodeWithText(str(UiCoreR.string.uicore_cancel)).performClick()

        assertTrue(dismissed)
        assertFalse(saved)
    }

    @Test
    fun dialog_hidesColorButtonWithoutAColorPicker() {
        rule.setContent {
            FolderAppearanceDialog(
                current = FolderAppearance(),
                defaultIcon = Icons.Default.Folder,
                defaultColor = Color.Blue,
                onSave = {},
                onDismiss = {}
            )
        }

        rule.onAllNodesWithTag(FolderAppearanceTestTags.DIALOG_COLOR_BUTTON).assertCountEquals(0)
        rule.onNodeWithTag(FolderAppearanceTestTags.DIALOG_PREVIEW).assertIsDisplayed()
    }

    @Test
    fun dialog_colorButtonOpensThePickerWithCurrentColorAndAppliesTheChoice() {
        val picker = FakeColorPicker(Color(0xFF00FF00))
        var saved: FolderAppearance? = null
        rule.setContent {
            CompositionLocalProvider(LocalFolderColorPicker provides picker) {
                FolderAppearanceDialog(
                    current = FolderAppearance(),
                    defaultIcon = Icons.Default.Folder,
                    defaultColor = Color.Blue,
                    onSave = { saved = it },
                    onDismiss = {}
                )
            }
        }

        rule.onNodeWithTag(FolderAppearanceTestTags.DIALOG_COLOR_BUTTON).performClick()
        rule.onNodeWithText(str(R.string.folder_appearance_color_title)).assertIsDisplayed()
        assertEquals(Color.Blue, picker.shownWith)
        rule.onNodeWithTag("fake_pick").performClick()
        rule.onNodeWithTag(FolderAppearanceTestTags.DIALOG_SAVE_BUTTON).performClick()

        assertEquals(FolderAppearance(colorArgb = Color(0xFF00FF00).toArgb()), saved)
    }

    @Test
    fun folderCard_iconTapOpensDialogAndSavesForItsScopeAndId() {
        val store = FakeAppearanceStore()
        rule.setContent {
            CompositionLocalProvider(LocalFolderAppearanceStore provides store) {
                FolderCard(
                    name = "Ideen",
                    countText = "0",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {},
                    folderId = 7L,
                    appearanceScope = "notes"
                )
            }
        }

        rule.onNodeWithTag(FolderAppearanceTestTags.ICON_TILE, useUnmergedTree = true).performClick()
        rule.onNodeWithContentDescription(str(R.string.folder_icon_star)).performClick()
        rule.onNodeWithTag(FolderAppearanceTestTags.DIALOG_SAVE_BUTTON).performClick()

        rule.runOnIdle {
            assertEquals(listOf(key to FolderAppearance(iconKey = "star")), store.saved)
        }
        rule.onAllNodesWithTag(FolderAppearanceTestTags.DIALOG_SAVE_BUTTON).assertCountEquals(0)
    }

    @Test
    fun folderCard_menuOffersChangeAppearanceWhenEditable() {
        val store = FakeAppearanceStore()
        rule.setContent {
            CompositionLocalProvider(LocalFolderAppearanceStore provides store) {
                FolderCard(
                    name = "Ideen",
                    countText = "0",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {},
                    folderId = 7L,
                    appearanceScope = "notes"
                )
            }
        }

        rule.onNodeWithTag(FolderTestTags.FOLDER_MENU_BUTTON).performClick()
        rule.onNodeWithText(str(R.string.folder_appearance_change)).performClick()

        rule.onNodeWithText(str(R.string.folder_appearance_title)).assertIsDisplayed()
    }

    @Test
    fun folderCard_withoutStoreOrIdentityHasNoAppearanceEntryPoints() {
        rule.setContent {
            Column {
                FolderCard(
                    name = "OhneStore",
                    countText = "0",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {},
                    folderId = 1L,
                    appearanceScope = "notes"
                )
                CompositionLocalProvider(LocalFolderAppearanceStore provides FakeAppearanceStore()) {
                    FolderCard(
                        name = "OhneId",
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
        }

        val menuButtons = rule.onAllNodesWithTag(FolderTestTags.FOLDER_MENU_BUTTON)
        menuButtons[0].performClick()
        rule.onAllNodesWithText(str(R.string.folder_appearance_change)).assertCountEquals(0)
    }

    @Test
    fun folderCard_usesScopeFromCompositionLocalWhenNotGivenExplicitly() {
        val store = FakeAppearanceStore()
        rule.setContent {
            CompositionLocalProvider(
                LocalFolderAppearanceStore provides store,
                LocalFolderAppearanceScope provides "notes"
            ) {
                FolderCard(
                    name = "Ideen",
                    countText = "0",
                    lastActivityAt = lastActivity,
                    isDropTarget = false,
                    onOpen = {},
                    onRename = {},
                    onDelete = {},
                    onPositioned = {},
                    folderId = 7L
                )
            }
        }

        rule.onNodeWithTag(FolderAppearanceTestTags.ICON_TILE, useUnmergedTree = true).performClick()
        rule.onNodeWithTag(FolderAppearanceTestTags.DIALOG_SAVE_BUTTON).performClick()

        rule.runOnIdle { assertEquals(listOf(key to FolderAppearance()), store.saved) }
    }

    @Test
    fun folderOptionsAction_offersAppearanceWhenKeyAndStoreExist() {
        val store = FakeAppearanceStore()
        rule.setContent {
            CompositionLocalProvider(LocalFolderAppearanceStore provides store) {
                FolderOptionsAction(
                    onRename = {},
                    onDelete = {},
                    folderId = 7L,
                    appearanceScope = "notes"
                )
            }
        }

        rule.onNodeWithContentDescription(str(R.string.folder_options)).performClick()
        rule.onNodeWithText(str(R.string.folder_appearance_change)).assertIsDisplayed().performClick()
        rule.onNodeWithText(str(R.string.folder_appearance_title)).assertIsDisplayed()
    }

    @Test
    fun folderOptionsAction_hidesAppearanceWithoutFolderId() {
        val store = FakeAppearanceStore()
        rule.setContent {
            CompositionLocalProvider(LocalFolderAppearanceStore provides store) {
                FolderOptionsAction(onRename = {}, onDelete = {}, appearanceScope = "notes")
            }
        }

        rule.onNodeWithContentDescription(str(R.string.folder_options)).performClick()
        rule.onAllNodesWithText(str(R.string.folder_appearance_change)).assertCountEquals(0)
    }
}
