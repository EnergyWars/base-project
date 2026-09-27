package com.wafflehq.base.ui.settings

import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.base.data.features.FeatureFilesRepository
import com.wafflehq.base.data.model.ThemeMode
import com.wafflehq.base.data.settings.SettingsRepository
import com.wafflehq.base.testutil.FakeAssetContext
import com.wafflehq.base.testutil.FakePreferencesDataStore
import com.wafflehq.base.ui.theme.BaseAppTheme
import com.wafflehq.lib.navigation.settings.SettingsHomePageTestTags
import com.wafflehq.lib.uicore.scaffold.AppScaffoldTestTags
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w400dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val calls = mutableListOf<String>()

    private fun viewModel(files: Map<String, String>, repository: SettingsRepository = SettingsRepository(FakePreferencesDataStore())) =
        SettingsViewModel(repository, FeatureFilesRepository(FakeAssetContext.withFiles(files)))

    private fun showSettings(files: Map<String, String>) {
        val viewModel = viewModel(files)
        rule.setContent {
            BaseAppTheme(themeMode = ThemeMode.LIGHT) {
                SettingsScreen(
                    onBack = { calls += "back" },
                    onOpenDisplay = { calls += "display" },
                    onOpenFeatureFiles = { calls += "features" },
                    onOpenColors = { calls += "colors" },
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun `settings home lists display and colors and hides features without files`() {
        showSettings(emptyMap())
        rule.waitForIdle()

        rule.onNodeWithTag(AppScaffoldTestTags.TITLE).assertTextEquals("Settings")
        rule.onNodeWithTag(SettingsHomePageTestTags.entry("Display")).assertIsDisplayed()
        rule.onNodeWithTag(SettingsHomePageTestTags.entry("Colors")).assertIsDisplayed()
        rule.onAllNodesWithTag(SettingsHomePageTestTags.entry("Features")).assertCountEquals(0)
    }

    @Test
    fun `settings home shows the features entry with the file count`() {
        showSettings(mapOf("a.md" to "# A", "b.md" to "# B"))

        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag(SettingsHomePageTestTags.entry("Features")).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("2 entries").assertIsDisplayed()
    }

    @Test
    fun `entries and back button invoke their callbacks`() {
        showSettings(mapOf("a.md" to "# A"))
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag(SettingsHomePageTestTags.entry("Features")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag(SettingsHomePageTestTags.entry("Display")).performClick()
        rule.onNodeWithTag(SettingsHomePageTestTags.entry("Colors")).performClick()
        rule.onNodeWithTag(SettingsHomePageTestTags.entry("Features")).performClick()
        rule.onNodeWithTag(AppScaffoldTestTags.BACK).performClick()

        assertEquals(listOf("display", "colors", "features", "back"), calls)
    }

    @Test
    fun `display settings shows the current theme mode`() {
        val repository = SettingsRepository(FakePreferencesDataStore())
        val viewModel = viewModel(emptyMap(), repository)
        rule.setContent {
            BaseAppTheme(themeMode = ThemeMode.LIGHT) {
                DisplaySettingsScreen(onBack = { calls += "back" }, viewModel = viewModel)
            }
        }
        rule.waitForIdle()

        rule.onNodeWithTag(AppScaffoldTestTags.TITLE).assertTextEquals("Display")
        rule.onNodeWithText("System default").assertIsDisplayed()

        runBlocking { repository.setThemeMode(ThemeMode.DARK) }
        rule.waitUntil(5_000) { textExists("Dark") }
        rule.onNodeWithText("Dark").assertIsDisplayed()

        rule.onNodeWithTag(AppScaffoldTestTags.BACK).performClick()
        assertEquals(listOf("back"), calls)
    }

    private fun textExists(text: String): Boolean =
        rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
}
