package com.wafflehq.base.ui.library.demos

import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.navigation.settings.EditorScaffoldTestTags
import com.wafflehq.lib.navigation.shell.MAX_NAV_SHORTCUTS
import com.wafflehq.lib.navigation.shell.NavShortcutMode
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
class NavigationDemoTest : LibraryDemoTest() {

    @Test
    fun rendersSettingsBlocksAndTheDrawerPreview() {
        show { NavigationDemo() }

        node(DemoTags.section("navigation")).assertExists()
        rule.onNodeWithText(string(R.string.libex_navigation_switch_title)).assertExists()
        rule.onNodeWithText(string(R.string.libex_navigation_row_profile)).assertExists()
        rule.onNodeWithText(string(R.string.libex_navigation_drawer_title)).assertExists()
    }

    @Test
    fun navRowReportsTheOpenedEntry() {
        show { NavigationDemo() }

        clickText(string(R.string.libex_navigation_row_privacy))

        assertTagText(NavigationTags.OPENED, string(R.string.libex_navigation_row_privacy))
    }

    @Test
    fun densityDropdownSelectsAnotherOption() {
        show { NavigationDemo() }
        val normal = string(R.string.libex_navigation_density_normal)
        val relaxed = string(R.string.libex_navigation_density_relaxed)

        clickText(normal)
        rule.onNodeWithText(relaxed).performClick()

        rule.onAllNodesWithText(relaxed).assertCountEquals(1)
        rule.onAllNodesWithText(normal).assertCountEquals(0)
    }

    @Test
    fun recentShortcutsListTheLatestVisitFirst() {
        show { NavigationDemo() }
        assertTagText(NavigationTags.SHORTCUTS, string(R.string.libex_navigation_shortcuts_empty))

        click(NavigationTags.target(DemoNavTarget.CALENDAR))
        click(NavigationTags.target(DemoNavTarget.NOTES))

        assertTagText(
            NavigationTags.SHORTCUTS,
            string(
                R.string.libex_navigation_shortcuts_result,
                MAX_NAV_SHORTCUTS,
                string(R.string.libex_navigation_target_notes) + ", " + string(R.string.libex_navigation_target_calendar),
            ),
        )
    }

    @Test
    fun customModeTogglesEntriesIndependentlyOfTheVisitHistory() {
        show { NavigationDemo() }
        click(NavigationTags.target(DemoNavTarget.NOTES))

        click(NavigationTags.mode(NavShortcutMode.CUSTOM))
        assertTagText(NavigationTags.SHORTCUTS, string(R.string.libex_navigation_shortcuts_empty))

        click(NavigationTags.target(DemoNavTarget.WEATHER))
        assertTagText(
            NavigationTags.SHORTCUTS,
            string(R.string.libex_navigation_shortcuts_result, MAX_NAV_SHORTCUTS, string(R.string.libex_navigation_target_weather)),
        )

        click(NavigationTags.target(DemoNavTarget.WEATHER))
        assertTagText(NavigationTags.SHORTCUTS, string(R.string.libex_navigation_shortcuts_empty))
    }

    @Test
    fun editorScaffoldSavesTheTypedText() {
        show { NavigationDemo() }

        click(NavigationTags.EDITOR_OPEN)
        node(NavigationTags.EDITOR_FIELD).performTextInput("hello")
        node(EditorScaffoldTestTags.SAVE_BUTTON).performClick()

        waitForTag(NavigationTags.EDITOR_RESULT)
        assertTagText(NavigationTags.EDITOR_RESULT, string(R.string.libex_navigation_editor_saved, "hello"))
        assertTagCount(NavigationTags.EDITOR_FIELD, 0)
    }

    @Test
    fun editorScaffoldDoesNotSaveWithoutText() {
        show { NavigationDemo() }

        click(NavigationTags.EDITOR_OPEN)
        node(EditorScaffoldTestTags.SAVE_BUTTON).performClick()

        assertTagCount(NavigationTags.EDITOR_RESULT, 0)
        node(NavigationTags.EDITOR_FIELD).assertExists()
    }

    @Test
    fun visitMovesTheIdToTheFrontWithoutDuplicates() {
        assertEquals(listOf("b", "a"), NavigationDemoLogic.visit(listOf("a", "b"), "b"))
        assertEquals(listOf("a", "b"), NavigationDemoLogic.visit(listOf("b"), "a"))
        assertEquals(listOf("a"), NavigationDemoLogic.visit(listOf("a"), "a"))
    }

    @Test
    fun customToggleRespectsTheShortcutLimit() {
        var custom = emptyList<String>()
        listOf("a", "b", "c", "d", "e").forEach { custom = NavigationDemoLogic.toggleCustom(custom, it) }

        assertEquals(MAX_NAV_SHORTCUTS, custom.size)
        assertEquals(listOf("a", "b", "c", "d"), custom)
        assertEquals(listOf("b", "c", "d"), NavigationDemoLogic.toggleCustom(custom, "a"))
    }

    @Test
    fun shortcutsIgnoreUnknownIds() {
        val resolved = NavigationDemoLogic.shortcuts(
            mode = NavShortcutMode.RECENT,
            recent = listOf("notes", "ghost", "calendar"),
            custom = emptyList(),
        )

        assertEquals(listOf("notes", "calendar"), resolved)
    }

    @Test
    fun pinnedItemsFormTheFirstSection() {
        val sections = NavigationDemoLogic.sections(
            label = { "L$it" },
            pinned = setOf("notes"),
            selectedId = "todo",
            pinnedLabel = "Pinned",
            onSelect = {},
            onPinToggle = {},
        )

        assertEquals("Pinned", sections.first().label)
        assertTrue(sections.first().pinned)
        assertEquals(listOf("notes"), sections.first().items.map { it.id })
        assertEquals(DemoNavTarget.entries.size, sections.filterNot { it.pinned }.sumOf { it.items.size })
        assertTrue(sections.flatMap { it.items }.first { it.id == "todo" }.selected)
    }

    @Test
    fun withoutPinsThereAreOnlyTheGroupSections() {
        val sections = NavigationDemoLogic.sections({ "L$it" }, emptySet(), null, "Pinned", {}, {})

        assertEquals(2, sections.size)
        assertTrue(sections.none { it.pinned })
    }
}
