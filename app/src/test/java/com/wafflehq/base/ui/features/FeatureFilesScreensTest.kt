package com.wafflehq.base.ui.features

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import com.wafflehq.base.data.features.FeatureFilesRepository
import com.wafflehq.base.data.model.ThemeMode
import com.wafflehq.base.data.settings.SettingsRepository
import com.wafflehq.base.testutil.FakeAssetContext
import com.wafflehq.base.testutil.FakePreferencesDataStore
import com.wafflehq.base.ui.theme.BaseAppTheme
import com.wafflehq.lib.uicore.scaffold.AppScaffoldTestTags
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
class FeatureFilesScreensTest {

    @get:Rule
    val rule = createComposeRule()

    private val opened = mutableListOf<String>()
    private var backClicks = 0

    private fun textExists(text: String): Boolean =
        rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    private fun showList(files: Map<String, String>) {
        val viewModel = FeatureFilesViewModel(
            SettingsRepository(FakePreferencesDataStore()),
            FeatureFilesRepository(FakeAssetContext.withFiles(files))
        )
        rule.setContent {
            BaseAppTheme(themeMode = ThemeMode.LIGHT) {
                FeatureFilesListScreen(
                    onBack = { backClicks++ },
                    onOpenFile = { opened += it },
                    viewModel = viewModel
                )
            }
        }
    }

    private fun showDetail(fileName: String, files: Map<String, String>) {
        val viewModel = FeatureFileDetailViewModel(
            SavedStateHandle(mapOf("fileName" to fileName)),
            FeatureFilesRepository(FakeAssetContext.withFiles(files))
        )
        rule.setContent {
            BaseAppTheme(themeMode = ThemeMode.LIGHT) {
                FeatureFileDetailScreen(onBack = { backClicks++ }, viewModel = viewModel)
            }
        }
    }

    @Test
    fun `list shows the empty state without feature files`() {
        showList(emptyMap())

        rule.onNodeWithTag(AppScaffoldTestTags.TITLE).assertTextEquals("Feature list")
        rule.waitUntil(5_000) { textExists("No feature files available.") }
    }

    @Test
    fun `list shows the file titles and opens a file on click`() {
        showList(mapOf("a.md" to "# Alpha", "b.md" to "# Beta"))

        rule.waitUntil(5_000) { textExists("Alpha") && textExists("Beta") }
        rule.onNodeWithText("Beta").performClick()

        assertEquals(listOf("b.md"), opened)
    }

    @Test
    fun `back button invokes the back callback`() {
        showList(mapOf("a.md" to "# Alpha"))

        rule.onNodeWithTag(AppScaffoldTestTags.BACK).performClick()

        assertEquals(1, backClicks)
    }

    @Test
    fun `detail shows the title and the content`() {
        showDetail("a.md", mapOf("a.md" to "# Alpha\nBody text"))

        rule.onNodeWithTag(AppScaffoldTestTags.TITLE).assertTextEquals("Alpha")
        rule.onNodeWithText("# Alpha\nBody text").assertIsDisplayed()
    }

    @Test
    fun `detail falls back to the empty message for an unknown file`() {
        showDetail("missing.md", mapOf("a.md" to "# Alpha"))

        rule.onNodeWithTag(AppScaffoldTestTags.TITLE).assertTextEquals("Feature list")
        rule.onNodeWithText("No feature files available.").assertIsDisplayed()
    }
}
