package com.wafflehq.base.data.settings

import com.wafflehq.base.data.model.ThemeMode
import com.wafflehq.base.testutil.FakePreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {

    private val repository = SettingsRepository(FakePreferencesDataStore())

    @Test
    fun `defaults are system theme and no feature file state`() = runTest {
        assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
        assertEquals(emptySet<String>(), repository.checkedFeatureFiles.first())
        assertFalse(repository.showHiddenFeatureFiles.first())
    }

    @Test
    fun `theme mode is persisted`() = runTest {
        repository.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, repository.themeMode.first())
        repository.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, repository.themeMode.first())
    }

    @Test
    fun `feature files can be checked and unchecked`() = runTest {
        repository.setFeatureFileChecked("a.md", true)
        repository.setFeatureFileChecked("b.md", true)
        assertEquals(setOf("a.md", "b.md"), repository.checkedFeatureFiles.first())

        repository.setFeatureFileChecked("a.md", false)
        assertEquals(setOf("b.md"), repository.checkedFeatureFiles.first())
    }

    @Test
    fun `checking the same file twice keeps a single entry`() = runTest {
        repository.setFeatureFileChecked("a.md", true)
        repository.setFeatureFileChecked("a.md", true)
        assertEquals(setOf("a.md"), repository.checkedFeatureFiles.first())
    }

    @Test
    fun `unchecking an unknown file is a no-op`() = runTest {
        repository.setFeatureFileChecked("missing.md", false)
        assertEquals(emptySet<String>(), repository.checkedFeatureFiles.first())
    }

    @Test
    fun `show hidden flag is persisted`() = runTest {
        repository.setShowHiddenFeatureFiles(true)
        assertTrue(repository.showHiddenFeatureFiles.first())
        repository.setShowHiddenFeatureFiles(false)
        assertFalse(repository.showHiddenFeatureFiles.first())
    }
}
