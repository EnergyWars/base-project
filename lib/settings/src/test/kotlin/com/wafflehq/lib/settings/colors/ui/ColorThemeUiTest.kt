package com.wafflehq.lib.settings.colors.ui

import com.wafflehq.lib.uicore.R as UiCoreR
import android.app.Application
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.colors.ColorOverrideStore
import com.wafflehq.lib.settings.colors.ColorSettingsController
import com.wafflehq.lib.settings.colors.ColorThemeExportEntry
import com.wafflehq.lib.settings.colors.ColorThemeInfo
import com.wafflehq.lib.settings.colors.ColorThemeData
import com.wafflehq.lib.settings.colors.ColorThemeExportRepository
import com.wafflehq.lib.settings.colors.ColorValue
import com.wafflehq.lib.settings.colors.FakePreferencesDataStore
import com.wafflehq.lib.settings.colors.TestColorTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ColorThemeUiTest {

    @get:Rule
    val rule = createComposeRule()

    private lateinit var store: ColorOverrideStore
    private lateinit var controller: ColorSettingsController
    private val scope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())
    private val context: android.content.Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        store = ColorOverrideStore(FakePreferencesDataStore())
        controller = ColorSettingsController(
            store,
            ColorThemeExportRepository(store, TestColorTokens.registry),
            TestColorTokens.registry,
            scope
        )
    }

    @After
    fun tearDown() = scope.cancel()

    private fun text(res: Int) = context.getString(res)

    private fun showSelector() = rule.setContent { ColorThemeSelector(controller) }

    @Test
    fun `the standard theme shows its name and the read-only hint`() {
        showSelector()

        rule.onNodeWithTag(ColorThemeTestTags.SELECTOR).assertExists()
        rule.onNodeWithText(text(R.string.appsettings_color_theme_standard_name)).assertExists()
        rule.onNodeWithText(text(R.string.appsettings_color_theme_standard_hint)).assertExists()
    }

    @Test
    fun `an active custom theme shows its name and the custom hint`() {
        runBlocking { store.createTheme("Mein Look") }
        showSelector()

        rule.onNodeWithText("Mein Look").assertExists()
        rule.onNodeWithText(text(R.string.appsettings_color_theme_custom_hint)).assertExists()
    }

    @Test
    fun `duplicate rename and delete are only offered for a custom theme`() {
        showSelector()
        rule.onNodeWithTag(ColorThemeTestTags.MENU).performClick()

        rule.onNodeWithTag(ColorThemeTestTags.NEW).assertExists()
        rule.onNodeWithTag(ColorThemeTestTags.DUPLICATE).assertDoesNotExist()
        rule.onNodeWithTag(ColorThemeTestTags.RENAME).assertDoesNotExist()
        rule.onNodeWithTag(ColorThemeTestTags.DELETE).assertDoesNotExist()
    }

    @Test
    fun `the menu of a custom theme offers all actions`() {
        runBlocking { store.createTheme("A") }
        showSelector()
        rule.onNodeWithTag(ColorThemeTestTags.MENU).performClick()

        rule.onNodeWithTag(ColorThemeTestTags.NEW).assertExists()
        rule.onNodeWithTag(ColorThemeTestTags.DUPLICATE).assertExists()
        rule.onNodeWithTag(ColorThemeTestTags.RENAME).assertExists()
        rule.onNodeWithTag(ColorThemeTestTags.DELETE).assertExists()
    }

    @Test
    fun `creating a new theme from the menu names and activates it`() {
        showSelector()
        rule.onNodeWithTag(ColorThemeTestTags.MENU).performClick()
        rule.onNodeWithTag(ColorThemeTestTags.NEW).performClick()

        rule.onNode(hasSetTextAction()).performTextInput("Neon")
        rule.onNodeWithText(text(R.string.appsettings_color_theme_name_create)).performClick()

        rule.waitForIdle()
        val themes = runBlocking { store.themesFlow().first() }
        assertEquals(listOf("Neon"), themes.map { it.name })
        assertEquals(themes.single().id, runBlocking { store.activeThemeIdFlow().first() })
    }

    @Test
    fun `duplicating copies the colors of the active theme`() {
        runBlocking {
            store.createTheme("Basis")
            store.setOverride(TestColorTokens.alphaAccent, false, ColorValue.Custom(9))
        }
        showSelector()
        rule.onNodeWithTag(ColorThemeTestTags.MENU).performClick()
        rule.onNodeWithTag(ColorThemeTestTags.DUPLICATE).performClick()

        rule.onNodeWithText(text(R.string.appsettings_color_theme_name_create)).performClick()

        rule.waitForIdle()
        assertEquals(ColorValue.Custom(9), runBlocking { store.overridesFlow(false).first() }[TestColorTokens.alphaAccent])
        assertEquals(2, runBlocking { store.themesFlow().first() }.size)
    }

    @Test
    fun `renaming changes the theme name`() {
        runBlocking { store.createTheme("Alt") }
        showSelector()
        rule.onNodeWithTag(ColorThemeTestTags.MENU).performClick()
        rule.onNodeWithTag(ColorThemeTestTags.RENAME).performClick()

        rule.onNode(hasSetTextAction()).performTextReplacement("Neu")
        rule.onNodeWithText(text(UiCoreR.string.uicore_save)).performClick()

        rule.waitForIdle()
        assertEquals("Neu", runBlocking { store.themesFlow().first() }.single().name)
    }

    @Test
    fun `deleting asks for confirmation and falls back to the standard theme`() {
        runBlocking { store.createTheme("Weg") }
        showSelector()
        rule.onNodeWithTag(ColorThemeTestTags.MENU).performClick()
        rule.onNodeWithTag(ColorThemeTestTags.DELETE).performClick()
        rule.onNodeWithText(text(R.string.appsettings_color_theme_delete_confirm_title)).assertExists()

        rule.onNodeWithText(text(R.string.appsettings_color_theme_delete)).performClick()

        rule.waitForIdle()
        assertTrue(runBlocking { store.themesFlow().first() }.isEmpty())
        assertNull(runBlocking { store.activeThemeIdFlow().first() })
    }

    @Test
    fun `switching between the standard theme and a custom one works from the list`() {
        val info = runBlocking { store.createTheme("Eins") }
        showSelector()
        rule.onNodeWithTag(ColorThemeTestTags.SELECTOR).performClick()

        rule.onNodeWithTag(ColorThemeTestTags.STANDARD_ENTRY).performClick()
        rule.waitForIdle()
        assertNull(runBlocking { store.activeThemeIdFlow().first() })

        rule.onNodeWithTag(ColorThemeTestTags.SELECTOR).performClick()
        rule.onNodeWithTag(ColorThemeTestTags.entry(info.id)).performClick()
        rule.waitForIdle()
        assertEquals(info.id, runBlocking { store.activeThemeIdFlow().first() })
    }

    @Test
    fun `no name dialog is shown while nobody asked for one`() {
        rule.setContent { ColorThemeNameRequestHost(controller) }

        rule.onNodeWithText(text(R.string.appsettings_color_theme_name_required_title)).assertDoesNotExist()
    }

    @Test
    fun `an edit in the standard theme opens the name dialog and applies after naming`() {
        rule.setContent { ColorThemeNameRequestHost(controller) }

        controller.setOverride(TestColorTokens.alphaAccent, false, ColorValue.Custom(4))
        rule.waitForIdle()
        rule.onNodeWithText(text(R.string.appsettings_color_theme_name_required_title)).assertExists()
        rule.onNodeWithText(text(R.string.appsettings_color_theme_name_required_message)).assertExists()

        rule.onNode(hasSetTextAction()).performTextInput("Eigenes")
        rule.onNodeWithText(text(R.string.appsettings_color_theme_name_create)).performClick()

        rule.waitForIdle()
        assertEquals("Eigenes", runBlocking { store.themesFlow().first() }.single().name)
        assertEquals(ColorValue.Custom(4), runBlocking { store.overridesFlow(false).first() }[TestColorTokens.alphaAccent])
    }

    @Test
    fun `cancelling the name dialog leaves the standard theme untouched`() {
        rule.setContent { ColorThemeNameRequestHost(controller) }
        controller.setOverride(TestColorTokens.alphaAccent, false, ColorValue.Custom(4))
        rule.waitForIdle()

        rule.onNodeWithText(context.getString(com.wafflehq.lib.uicore.R.string.uicore_cancel)).performClick()

        rule.waitForIdle()
        assertTrue(runBlocking { store.themesFlow().first() }.isEmpty())
        rule.onNodeWithText(text(R.string.appsettings_color_theme_name_required_title)).assertDoesNotExist()
    }

    @Test
    fun `the export dialog preselects the given themes and returns the chosen ids`() {
        val themes = listOf(ColorThemeInfo("a", "Alpha"), ColorThemeInfo("b", "Beta"))
        var confirmed: Set<String>? = null
        rule.setContent {
            ColorThemeExportDialog(themes, initiallySelected = setOf("a"), onConfirm = { confirmed = it }, onDismiss = {})
        }

        rule.onNodeWithTag(ColorThemeTestTags.exportEntry("b")).performClick()
        rule.onNodeWithTag(ColorThemeTestTags.EXPORT_CONFIRM).assertIsEnabled().performClick()

        assertEquals(setOf("a", "b"), confirmed)
    }

    @Test
    fun `the export dialog cannot be confirmed without a selection`() {
        rule.setContent {
            ColorThemeExportDialog(
                listOf(ColorThemeInfo("a", "Alpha")),
                initiallySelected = emptySet(),
                onConfirm = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithTag(ColorThemeTestTags.EXPORT_CONFIRM).assertIsNotEnabled()
    }

    @Test
    fun `the import dialog lists every theme of the file and imports the selected ones`() {
        val data = ColorThemeData(overridesLight = mapOf(TestColorTokens.alphaAccent.value to "C|-16776961"))
        runBlocking {
            controller.loadImportCandidates(
                """{"schemaVersion":3,"themes":[
                    {"name":"Eins","overridesLight":{"alpha.accent":"C|-16776961"}},
                    {"name":"Zwei","overridesLight":{"alpha.accent":"C|-65536"}}]}""".byteInputStream(),
                "Theme"
            )
            controller.importCandidates.first { it.size == 2 }
        }
        rule.setContent { ColorThemeImportDialog(controller = controller, onDismiss = {}) }
        rule.onNodeWithText("Eins").assertExists()
        rule.onNodeWithText("Zwei").assertExists()

        rule.onNodeWithTag(ColorThemeTestTags.importEntry(1)).performClick()
        rule.onNodeWithTag(ColorThemeTestTags.IMPORT_CONFIRM).performClick()

        rule.waitForIdle()
        assertEquals(listOf("Eins"), runBlocking { store.themesFlow().first() }.map { it.name })
        assertEquals(data, runBlocking { store.themeData(store.themesFlow().first().single().id) })
    }

    @Test
    fun `an entry constructed for import exposes its token count`() {
        assertEquals(1, ColorThemeExportEntry("x", ColorThemeData(overridesDark = mapOf("a" to "C|1"))).tokenCount)
    }
}
