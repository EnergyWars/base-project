package com.wafflehq.base.ui.settings

import com.wafflehq.base.data.features.FeatureFilesRepository
import com.wafflehq.base.data.model.ThemeMode
import com.wafflehq.base.data.settings.SettingsRepository
import com.wafflehq.base.testutil.FakeAssetContext
import com.wafflehq.base.testutil.FakePreferencesDataStore
import com.wafflehq.base.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(files: Map<String, String> = emptyMap()): SettingsViewModel =
        SettingsViewModel(
            repository = SettingsRepository(FakePreferencesDataStore()),
            featureFilesRepository = FeatureFilesRepository(FakeAssetContext.withFiles(files))
        )

    @Test
    fun `theme mode starts as system`() = runTest {
        assertEquals(ThemeMode.SYSTEM, viewModel().themeMode.first())
    }

    @Test
    fun `selecting a theme mode updates the exposed state`() = runTest {
        val viewModel = viewModel()

        viewModel.onThemeModeSelected(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, viewModel.themeMode.first { it == ThemeMode.DARK })
    }

    @Test
    fun `feature file count reflects the available files`() = runTest {
        val viewModel = viewModel(mapOf("a.md" to "# A", "b.md" to "# B"))

        assertEquals(2, viewModel.featureFilesCount.value)
    }

    @Test
    fun `feature file count is zero without files`() = runTest {
        assertEquals(0, viewModel().featureFilesCount.value)
    }
}
