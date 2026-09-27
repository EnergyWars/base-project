package com.wafflehq.base.ui.library

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.demos.DemoTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LibraryExamplesScreenTest : LibraryDemoTest() {

    private var menuOpened = 0
    private var homeOpened = 0
    private var settingsOpened = 0

    private fun showScreen() {
        rule.setContent {
            MaterialTheme {
                LibraryExamplesScreen(
                    onOpenMenu = { menuOpened++ },
                    onNavigateHome = { homeOpened++ },
                    onOpenSettings = { settingsOpened++ },
                )
            }
        }
    }

    @Test
    fun showsTheTitleLeadAndTheFirstDemo() {
        showScreen()

        rule.onNodeWithText(string(R.string.libex_title)).assertIsDisplayed()
        rule.onNodeWithText(string(R.string.libex_lead)).assertIsDisplayed()
        node(DemoTags.section("astronomy")).assertExists()
        node(LibraryExamplesTags.SEARCH).assertExists()
    }

    @Test
    fun headerActionsInvokeTheCallbacks() {
        showScreen()

        rule.onNodeWithContentDescription(string(R.string.cd_menu)).performClick()
        rule.onNodeWithContentDescription(string(R.string.cd_home)).performClick()
        rule.onNodeWithContentDescription(string(R.string.cd_open_settings)).performClick()

        assertEquals(1, menuOpened)
        assertEquals(1, homeOpened)
        assertEquals(1, settingsOpened)
    }

    @Test
    fun searchNarrowsTheListToMatchingModules() {
        showScreen()

        replaceText(LibraryExamplesTags.SEARCH, "QR")

        node(DemoTags.section("qr")).assertExists()
        node(DemoTags.section("astronomy")).assertDoesNotExist()
    }

    @Test
    fun searchWithoutMatchShowsTheEmptyState() {
        showScreen()

        replaceText(LibraryExamplesTags.SEARCH, "zzzzzz")

        node(LibraryExamplesTags.EMPTY).assertExists()
        node(DemoTags.section("qr")).assertDoesNotExist()
    }

    @Test
    fun everyDemoComposesInOneScrollableColumn() {
        val snackbar = SnackbarHostState()
        show { LibraryDemos.all.forEach { it.content(snackbar) } }

        LibraryDemos.all.forEach { entry -> node(DemoTags.section(entry.id)).assertExists() }
    }

    @Test
    fun demoEntriesAreUniqueAndComplete() {
        val ids = LibraryDemos.all.map { it.id }
        val titles = LibraryDemos.all.map { it.titleRes }

        assertEquals(24, ids.size)
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(titles.size, titles.toSet().size)
    }

    @Test
    fun everyLibraryModuleHasAtLeastOneDemo() {
        val modules = listOf(
            "astronomy", "backupcore", "charts", "database", "diagnostics", "drafts", "entrylock", "folders",
            "maintenance", "media", "modules", "navigation", "notifications", "pdf", "prefsbackup", "qr",
            "quickpicker", "settings", "textarea",
        )
        val ids = LibraryDemos.all.map { it.id }

        assertTrue(modules.all { it in ids })
        assertTrue(ids.any { it.startsWith("uicore_") })
    }

    @Test
    fun filterMatchesTitlesCaseInsensitively() {
        val entries = LibraryDemos.all.take(3)
        val titles = listOf("Alpha module", "Beta module", "Gamma")

        assertEquals(entries, LibraryDemos.filter(entries, titles, "  "))
        assertEquals(entries.take(2), LibraryDemos.filter(entries, titles, "MODULE"))
        assertEquals(listOf(entries[2]), LibraryDemos.filter(entries, titles, "gam"))
        assertTrue(LibraryDemos.filter(entries, titles, "zzz").isEmpty())
    }
}
