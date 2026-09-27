package com.wafflehq.base.ui.features

import androidx.lifecycle.SavedStateHandle
import com.wafflehq.base.data.features.FeatureFilesRepository
import com.wafflehq.base.data.settings.SettingsRepository
import com.wafflehq.base.testutil.FakeAssetContext
import com.wafflehq.base.testutil.FakePreferencesDataStore
import com.wafflehq.base.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FeatureFilesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val files = mapOf("a.md" to "# Alpha", "b.md" to "# Beta")

    private fun listViewModel(): FeatureFilesViewModel = FeatureFilesViewModel(
        settingsRepository = SettingsRepository(FakePreferencesDataStore()),
        featureFilesRepository = FeatureFilesRepository(FakeAssetContext.withFiles(files))
    )

    @Test
    fun `all files are listed unchecked by default`() = runTest {
        val viewModel = listViewModel()

        val items = viewModel.items.first { it.isNotEmpty() }

        assertEquals(2, viewModel.totalFileCount)
        assertEquals(
            listOf(
                FeatureFileItem("a.md", "Alpha", checked = false),
                FeatureFileItem("b.md", "Beta", checked = false)
            ),
            items
        )
    }

    @Test
    fun `checked files are hidden until show hidden is enabled`() = runTest {
        val viewModel = listViewModel()

        viewModel.onCheckedChange("a.md", true)
        val visible = viewModel.items.first { it.size == 1 }
        assertEquals(listOf("b.md"), visible.map { it.fileName })

        viewModel.onShowHiddenChange(true)
        assertTrue(viewModel.showHidden.first { it })
        val all = viewModel.items.first { it.size == 2 }
        assertTrue(all.first { it.fileName == "a.md" }.checked)
        assertFalse(all.first { it.fileName == "b.md" }.checked)
    }

    @Test
    fun `unchecking a file shows it again`() = runTest {
        val viewModel = listViewModel()
        viewModel.onCheckedChange("a.md", true)
        viewModel.items.first { it.size == 1 }

        viewModel.onCheckedChange("a.md", false)

        assertEquals(2, viewModel.items.first { it.size == 2 }.size)
    }

    @Test
    fun `detail view model loads the requested file`() {
        val viewModel = FeatureFileDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("fileName" to "a.md")),
            featureFilesRepository = FeatureFilesRepository(FakeAssetContext.withFiles(files))
        )

        assertEquals("Alpha", viewModel.featureFile?.title)
        assertEquals("# Alpha", viewModel.featureFile?.content)
    }

    @Test
    fun `detail view model yields null for an unknown file`() {
        val viewModel = FeatureFileDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("fileName" to "missing.md")),
            featureFilesRepository = FeatureFilesRepository(FakeAssetContext.withFiles(files))
        )

        assertNull(viewModel.featureFile)
    }

    @Test
    fun `detail view model requires a file name argument`() {
        assertThrows(IllegalStateException::class.java) {
            FeatureFileDetailViewModel(
                savedStateHandle = SavedStateHandle(),
                featureFilesRepository = FeatureFilesRepository(FakeAssetContext.withFiles(files))
            )
        }
    }
}
