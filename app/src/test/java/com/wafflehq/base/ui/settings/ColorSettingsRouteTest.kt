package com.wafflehq.base.ui.settings

import com.wafflehq.base.data.model.ThemeMode
import com.wafflehq.base.domain.colortheme.ColorTokenCatalog
import com.wafflehq.base.domain.colortheme.ColorTokenCategory
import com.wafflehq.base.testutil.FakePreferencesDataStore
import com.wafflehq.base.testutil.MainDispatcherRule
import com.wafflehq.lib.settings.colors.ColorOverrideStore
import com.wafflehq.lib.settings.colors.ColorThemeExportRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ColorSettingsRouteTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `all category keys match the catalog categories`() {
        assertEquals(ColorTokenCategory.entries.map { it.name }.toSet(), AllColorCategoryKeys)
        assertEquals(ColorTokenCatalog.registry.categories.map { it.key }.toSet(), AllColorCategoryKeys)
    }

    @Test
    fun `export file name for a single theme contains the theme name`() {
        val name = colorExportFileName("Ocean")

        assertTrue(name, name.startsWith("baseapp_theme_Ocean_"))
        assertTrue(name, name.endsWith(".json"))
    }

    @Test
    fun `export file name without a theme uses the plural prefix`() {
        val name = colorExportFileName(null)

        assertTrue(name, name.startsWith("baseapp_themes_"))
        assertTrue(name, name.endsWith(".json"))
    }

    @Test
    fun `theme mode labels are distinct string resources`() {
        val labels = ThemeMode.entries.map { it.labelRes }

        assertEquals(ThemeMode.entries.size, labels.toSet().size)
        assertTrue(labels.all { it != 0 })
        assertNotEquals(labels[0], labels[1])
    }

    @Test
    fun `color settings view model exposes a controller without overrides`() {
        val store = ColorOverrideStore(FakePreferencesDataStore())
        val registry = ColorTokenCatalog.registry
        val viewModel = ColorSettingsViewModel(store, ColorThemeExportRepository(store, registry), registry)

        assertEquals(emptyMap<Any, Any>(), viewModel.controller.overrides(isDark = false).value)
        assertEquals(emptyMap<Any, Any>(), viewModel.controller.baseRampOverrides.value)
        assertEquals(emptyList<Any>(), viewModel.controller.themes.value)
    }
}
