package com.wafflehq.lib.navigation.shell

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppNavigationUiTest {

    @get:Rule
    val rule = createComposeRule()

    private fun item(label: String, selected: Boolean = false, enabled: Boolean = true, onClick: () -> Unit = {}) =
        AppNavItem(label = label, icon = Icons.Default.Home, selected = selected, enabled = enabled, onClick = onClick)

    private fun action(label: String, enabled: Boolean = true, selected: Boolean = false, onClick: () -> Unit = {}) =
        AppNavAction(
            icon = Icons.Default.Settings,
            contentDescription = label,
            onClick = onClick,
            enabled = enabled,
            selected = selected
        )

    private fun setTopBar(
        title: String = "App",
        menu: AppNavAction = action("Menu"),
        search: AppNavAction = action("Search"),
        settings: AppNavAction = action("Settings"),
        home: AppNavItem = item("Calendar", selected = true),
        shortcuts: List<AppNavItem> = emptyList(),
        statusText: String? = null
    ) {
        rule.setContent {
            AppTopNavBar(
                title = title,
                menu = menu,
                search = search,
                settings = settings,
                home = home,
                shortcuts = shortcuts,
                statusText = statusText
            )
        }
    }

    @Test
    fun topBar_showsStaticTitleAndAllFixedControls() {
        setTopBar(title = "AllInOneCalendar")
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR).assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_TITLE).assertIsDisplayed()
        rule.onNodeWithText("AllInOneCalendar").assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_MENU).assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_SEARCH).assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_SETTINGS).assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_HOME).assertIsDisplayed()
        rule.onNodeWithText("Calendar").assertIsDisplayed()
    }

    @Test
    fun topBar_fixedControlsInvokeTheirCallbacks() {
        val clicked = mutableListOf<String>()
        setTopBar(
            menu = action("Menu") { clicked += "menu" },
            search = action("Search") { clicked += "search" },
            settings = action("Settings") { clicked += "settings" },
            home = item("Calendar", selected = false) { clicked += "home" }
        )
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_MENU).performClick()
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_SEARCH).performClick()
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_SETTINGS).performClick()
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_HOME).performClick()
        assertEquals(listOf("menu", "search", "settings", "home"), clicked)
    }

    @Test
    fun topBar_disabledControlsDoNotInvokeCallbacks() {
        var clicked = false
        setTopBar(
            menu = action("Menu", enabled = false) { clicked = true },
            search = action("Search", enabled = false) { clicked = true },
            settings = action("Settings", enabled = false) { clicked = true }
        )
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_MENU).assertIsNotEnabled()
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_MENU).performClick()
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_SEARCH).performClick()
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_SETTINGS).performClick()
        assertFalse(clicked)
    }

    @Test
    fun topBar_disabledHomeDoesNotInvokeCallback() {
        var clicked = false
        setTopBar(home = item("Calendar", enabled = false) { clicked = true })
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_HOME).performClick()
        assertFalse(clicked)
    }

    @Test
    fun topBar_rendersShortcutsAndInvokesClick() {
        var clicked = ""
        val shortcuts = listOf("Notes", "Todo", "Weight").map { name ->
            AppNavItem(
                label = name,
                icon = Icons.Default.Home,
                selected = name == "Todo",
                id = name.lowercase(),
                onClick = { clicked = name }
            )
        }
        setTopBar(shortcuts = shortcuts)
        rule.onNodeWithTag(AppNavigationTestTags.topBarShortcut("notes")).assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.topBarShortcut("todo")).assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.topBarShortcut("weight")).performClick()
        assertEquals("Weight", clicked)
    }

    @Test
    fun topBar_showsAtMostFourShortcuts() {
        val shortcuts = (1..7).map { index ->
            AppNavItem(label = "Item$index", icon = Icons.Default.Home, selected = false, id = "id$index", onClick = {})
        }
        setTopBar(shortcuts = shortcuts)
        (1..4).forEach { rule.onNodeWithTag(AppNavigationTestTags.topBarShortcut("id$it")).assertIsDisplayed() }
        rule.onNodeWithTag(AppNavigationTestTags.topBarShortcut("id5")).assertDoesNotExist()
        rule.onNodeWithTag(AppNavigationTestTags.topBarShortcut("id6")).assertDoesNotExist()
        rule.onNodeWithTag(AppNavigationTestTags.topBarShortcut("id7")).assertDoesNotExist()
    }

    @Test
    fun topBar_showsLabelBelowEachShortcut() {
        val shortcuts = listOf("Notes", "Todo").map { name ->
            AppNavItem(label = name, icon = Icons.Default.Home, selected = false, id = name.lowercase(), onClick = {})
        }
        setTopBar(shortcuts = shortcuts)
        rule.onNodeWithTag(AppNavigationTestTags.topBarShortcutLabel("notes")).assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.topBarShortcutLabel("todo")).assertIsDisplayed()
        rule.onNodeWithText("Notes").assertIsDisplayed()
    }

    @Test
    fun topBar_shortcutsAndHomeHaveFixedWidths() {
        val shortcuts = listOf("Notes", "A very long shortcut label").mapIndexed { index, name ->
            AppNavItem(label = name, icon = Icons.Default.Home, selected = false, id = "id$index", onClick = {})
        }
        setTopBar(shortcuts = shortcuts)
        val first = rule.onNodeWithTag(AppNavigationTestTags.topBarShortcutLabel("id0")).fetchSemanticsNode().size.width
        val second = rule.onNodeWithTag(AppNavigationTestTags.topBarShortcutLabel("id1")).fetchSemanticsNode().size.width
        assertEquals(first, second)
        val home = rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_HOME).fetchSemanticsNode().size.width
        val shortcut = rule.onNodeWithTag(AppNavigationTestTags.topBarShortcut("id0")).fetchSemanticsNode().size.width
        assertTrue(home >= 2 * shortcut)
    }

    @Test
    fun topBar_showsStatusTextWhenProvided() {
        setTopBar(statusText = "Last synced: just now")
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_STATUS).assertIsDisplayed()
        rule.onNodeWithText("Last synced: just now").assertIsDisplayed()
    }

    @Test
    fun topBar_hidesStatusTextWhenNull() {
        setTopBar(statusText = null)
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_STATUS).assertDoesNotExist()
    }

    @Test
    fun topBar_withoutShortcutsStillShowsHome() {
        setTopBar(shortcuts = emptyList())
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR_HOME).assertIsDisplayed()
    }

    private fun drawerSections(onClick: (String) -> Unit = {}) = listOf(
        AppNavSection(
            label = "Pinned",
            pinned = true,
            items = listOf(pinnedItem("Notes", onClick = { onClick("Notes") }))
        ),
        AppNavSection(
            label = "Health",
            items = listOf(
                item("Weight", onClick = { onClick("Weight") }),
                item("Steps", onClick = { onClick("Steps") })
            )
        ),
        AppNavSection(
            label = "Organization",
            items = listOf(
                item("Notes", selected = true, onClick = { onClick("Notes") }),
                item("Todos", onClick = { onClick("Todos") })
            )
        )
    )

    private fun pinnedItem(label: String, selected: Boolean = false, onClick: () -> Unit = {}) = AppNavItem(
        label = label,
        icon = Icons.Default.Home,
        selected = selected,
        pinned = true,
        onPinToggle = {},
        accentIndex = 1,
        onClick = onClick
    )

    @Test
    fun drawer_rendersHeaderSearchSectionsAndFooter() {
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                subtitle = "3 modules active",
                headerIcon = Icons.Default.Home,
                searchPlaceholder = "Search modules",
                pinnedLabel = "Pinned",
                closeDescription = "Close",
                onClose = {},
                footerItem = item("Settings"),
                footerAction = AppNavFooterAction(Icons.Default.Settings, "Toggle theme") {},
                sections = drawerSections()
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER).assertIsDisplayed()
        rule.onNodeWithText("My app").assertIsDisplayed()
        rule.onNodeWithText("3 modules active").assertIsDisplayed()
        rule.onNodeWithText("Search modules").assertIsDisplayed()
        rule.onNodeWithText("PINNED").assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_CLOSE).assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.drawerSection("Health")).assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.drawerSection("Organization")).assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Settings")).assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_FOOTER_ACTION).assertIsDisplayed()
    }

    @Test
    fun drawer_closeButtonInvokesOnClose() {
        var closed = 0
        rule.setContent {
            AppSideNavDrawer(title = "My app", sections = drawerSections(), closeDescription = "Close", onClose = { closed++ })
        }
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_CLOSE).performClick()
        assertEquals(1, closed)
    }

    @Test
    fun drawer_withoutOnClose_hasNoCloseButton() {
        rule.setContent { AppSideNavDrawer(title = "My app", sections = drawerSections()) }
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_CLOSE).assertDoesNotExist()
    }

    @Test
    fun drawer_sectionWithSelectedItem_isExpandedAndOthersAreCollapsed() {
        rule.setContent { AppSideNavDrawer(title = "My app", sections = drawerSections()) }
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Notes")).assertIsSelected()
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Todos")).assertIsNotSelected()
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Weight")).assertDoesNotExist()
    }

    @Test
    fun drawer_sectionHeaderTogglesItems() {
        rule.setContent { AppSideNavDrawer(title = "My app", sections = drawerSections()) }
        rule.onNodeWithTag(AppNavigationTestTags.drawerSection("Health")).performClick()
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Weight")).assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.drawerSection("Health")).performClick()
        rule.waitForIdle()
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Weight")).assertDoesNotExist()
    }

    @Test
    fun drawer_selectedSectionCanBeCollapsed() {
        rule.setContent { AppSideNavDrawer(title = "My app", sections = drawerSections()) }
        rule.onNodeWithTag(AppNavigationTestTags.drawerSection("Organization")).performClick()
        rule.waitForIdle()
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Todos")).assertDoesNotExist()
    }

    @Test
    fun drawer_itemClickInvokesCallback() {
        var clicked = ""
        rule.setContent { AppSideNavDrawer(title = "My app", sections = drawerSections { clicked = it }) }
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Todos")).performClick()
        assertEquals("Todos", clicked)
    }

    @Test
    fun drawer_pinnedTileClickInvokesCallback() {
        var clicked = ""
        rule.setContent { AppSideNavDrawer(title = "My app", sections = drawerSections { clicked = it }) }
        rule.onNodeWithTag(AppNavigationTestTags.drawerPinnedItem("Notes")).performClick()
        assertEquals("Notes", clicked)
    }

    @Test
    fun drawer_pinnedTileReflectsSelection() {
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = listOf(
                    AppNavSection(label = "Pinned", pinned = true, items = listOf(pinnedItem("Notes", selected = true), pinnedItem("Todos")))
                )
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerPinnedItem("Notes")).assertIsSelected()
        rule.onNodeWithTag(AppNavigationTestTags.drawerPinnedItem("Todos")).assertIsNotSelected()
    }

    @Test
    fun drawer_moreThanFourPinnedItems_wrapIntoSecondRow() {
        val labels = listOf("A", "B", "C", "D", "E")
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = listOf(AppNavSection(label = "Pinned", pinned = true, items = labels.map { pinnedItem(it) }))
            )
        }
        labels.forEach { rule.onNodeWithTag(AppNavigationTestTags.drawerPinnedItem(it)).assertIsDisplayed() }
    }

    @Test
    fun drawer_longPressOnItem_togglesPinWithoutNavigating() {
        var toggled = 0
        var clicked = false
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = listOf(
                    AppNavSection(
                        label = "Health",
                        items = listOf(
                            AppNavItem(
                                label = "Weight",
                                icon = Icons.Default.Home,
                                selected = true,
                                onPinToggle = { toggled++ },
                                onClick = { clicked = true }
                            )
                        )
                    )
                )
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Weight")).performTouchInput { longClick() }
        assertEquals(1, toggled)
        assertFalse(clicked)
    }

    @Test
    fun drawer_longPressOnPinnedTile_togglesPin() {
        var toggled = 0
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = listOf(
                    AppNavSection(
                        label = "Pinned",
                        pinned = true,
                        items = listOf(
                            AppNavItem(
                                label = "Notes",
                                icon = Icons.Default.Home,
                                selected = false,
                                pinned = true,
                                onPinToggle = { toggled++ },
                                onClick = {}
                            )
                        )
                    )
                )
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerPinnedItem("Notes")).performTouchInput { longClick() }
        assertEquals(1, toggled)
    }

    @Test
    fun drawer_pinButtonOnRow_togglesPinWithoutNavigating() {
        var toggled = 0
        var clicked = false
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = listOf(
                    AppNavSection(
                        label = "Health",
                        items = listOf(
                            AppNavItem(
                                label = "Weight",
                                icon = Icons.Default.Home,
                                selected = true,
                                onPinToggle = { toggled++ },
                                onClick = { clicked = true }
                            )
                        )
                    )
                )
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerPinToggle("Weight")).assertIsDisplayed().performClick()
        assertEquals(1, toggled)
        assertFalse(clicked)
    }

    @Test
    fun drawer_pinButtonIsHiddenWithoutToggle() {
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = listOf(AppNavSection(label = "Health", items = listOf(item("Weight", selected = true))))
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerPinToggle("Weight")).assertDoesNotExist()
    }

    @Test
    fun drawer_pinButtonDescribesPinAndUnpinAction() {
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = listOf(
                    AppNavSection(
                        label = "Health",
                        items = listOf(
                            AppNavItem("Weight", Icons.Default.Home, selected = true, onPinToggle = {}, onClick = {}),
                            AppNavItem("Steps", Icons.Default.Home, selected = true, pinned = true, onPinToggle = {}, onClick = {})
                        )
                    )
                )
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerPinToggle("Weight")).assertContentDescriptionEquals("Pin Weight")
        rule.onNodeWithTag(AppNavigationTestTags.drawerPinToggle("Steps")).assertContentDescriptionEquals("Unpin Steps")
    }

    @Test
    fun drawer_pinHintIsShownOnlyWithoutPinnedItems() {
        val unpinned = listOf(
            AppNavSection(label = "Health", items = listOf(AppNavItem("Weight", Icons.Default.Home, selected = true, onPinToggle = {}, onClick = {})))
        )
        rule.setContent {
            AppSideNavDrawer(title = "My app", pinnedLabel = "Pinned", sections = unpinned)
        }
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_PIN_HINT).assertIsDisplayed()
    }

    @Test
    fun drawer_pinHintIsHiddenWhenSomethingIsPinned() {
        rule.setContent {
            AppSideNavDrawer(title = "My app", pinnedLabel = "Pinned", sections = drawerSections())
        }
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_PIN_HINT).assertDoesNotExist()
    }

    @Test
    fun drawer_longPressWithoutToggle_doesNothing() {
        var clicked = false
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = listOf(AppNavSection(label = "Health", items = listOf(item("Weight", selected = true) { clicked = true })))
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Weight")).performTouchInput { longClick() }
        assertFalse(clicked)
    }

    @Test
    fun drawer_disabledItemDoesNotInvokeClick() {
        var clicked = false
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = listOf(AppNavSection(label = "Health", items = listOf(item("Weight", selected = true, enabled = false) { clicked = true })))
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Weight")).performClick()
        assertFalse(clicked)
    }

    @Test
    fun drawer_unlabeledSectionShowsItemsWithoutHeader() {
        rule.setContent {
            AppSideNavDrawer(title = "My app", sections = listOf(AppNavSection(items = listOf(item("Unlabeled")))))
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Unlabeled")).assertIsDisplayed()
    }

    @Test
    fun drawer_search_filtersItemsAcrossCollapsedSectionsAndHidesPinned() {
        rule.setContent {
            AppSideNavDrawer(title = "My app", pinnedLabel = "Pinned", sections = drawerSections())
        }
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_SEARCH).performTextInput("wei")
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Weight")).assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Steps")).assertDoesNotExist()
        rule.onNodeWithTag(AppNavigationTestTags.drawerSection("Organization")).assertDoesNotExist()
        rule.onNodeWithTag(AppNavigationTestTags.drawerPinnedItem("Notes")).assertDoesNotExist()
    }

    @Test
    fun drawer_search_withoutMatches_showsEmptyMessage() {
        rule.setContent { AppSideNavDrawer(title = "My app", sections = drawerSections()) }
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_SEARCH).performTextInput("zzz")
        rule.onNodeWithText("No matches.").assertIsDisplayed()
    }

    @Test
    fun drawer_clearingSearch_restoresPinnedTiles() {
        rule.setContent { AppSideNavDrawer(title = "My app", sections = drawerSections()) }
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_SEARCH).performTextInput("wei")
        rule.onNodeWithContentDescription("Clear search").performClick()
        rule.onNodeWithTag(AppNavigationTestTags.drawerPinnedItem("Notes")).assertIsDisplayed()
    }

    @Test
    fun drawer_withoutPinnedItems_showsHintWhenPinningIsAvailable() {
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                pinnedLabel = "Pinned",
                sections = listOf(
                    AppNavSection(
                        label = "Health",
                        items = listOf(AppNavItem(label = "Weight", icon = Icons.Default.Home, selected = false, onPinToggle = {}, onClick = {}))
                    )
                )
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_PIN_HINT).assertIsDisplayed()
    }

    @Test
    fun drawer_withoutPinToggles_showsNoHint() {
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                pinnedLabel = "Pinned",
                sections = listOf(AppNavSection(label = "Health", items = listOf(item("Weight"))))
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_PIN_HINT).assertDoesNotExist()
    }

    @Test
    fun drawer_footerActionInvokesCallback() {
        var invoked = 0
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = drawerSections(),
                footerAction = AppNavFooterAction(Icons.Default.Settings, "Toggle theme") { invoked++ }
            )
        }
        rule.onNodeWithContentDescription("Toggle theme").performClick()
        assertEquals(1, invoked)
    }

    @Test
    fun drawer_footerItemInvokesClick() {
        var clicked = false
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = drawerSections(),
                footerItem = item("Settings") { clicked = true }
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Settings")).performClick()
        assertTrue(clicked)
    }

    @Test
    fun sectionExpansion_defaultFollowsSelection() {
        val selected = AppNavSection(label = "A", items = listOf(item("x", selected = true)))
        val other = AppNavSection(label = "B", items = listOf(item("y")))
        assertTrue(isSectionExpanded(selected, emptyList()))
        assertFalse(isSectionExpanded(other, emptyList()))
    }

    @Test
    fun sectionExpansion_userOverridesWinOverSelection() {
        val selected = AppNavSection(label = "A", items = listOf(item("x", selected = true)))
        val other = AppNavSection(label = "B", items = listOf(item("y")))
        assertFalse(isSectionExpanded(selected, listOf("-A")))
        assertTrue(isSectionExpanded(other, listOf("+B")))
    }

    @Test
    fun toggledOverrides_flipsStateAndReplacesPreviousOverride() {
        val other = AppNavSection(label = "B", items = listOf(item("y")))
        val expandedOnce = toggledOverrides(other, emptyList())
        assertEquals(listOf("+B"), expandedOnce)
        val collapsedAgain = toggledOverrides(other, expandedOnce)
        assertEquals(listOf("-B"), collapsedAgain)
    }

    @Test
    fun toggledOverrides_keepsOverridesOfOtherSections() {
        val other = AppNavSection(label = "B", items = listOf(item("y")))
        assertEquals(listOf("+A", "+B"), toggledOverrides(other, listOf("+A")))
    }

    @Test
    fun sectionExpansion_usesSectionIdInsteadOfLabel() {
        val section = AppNavSection(label = "Werkzeuge", id = "TOOLS", items = listOf(item("y")))
        assertTrue(isSectionExpanded(section, listOf("+TOOLS")))
        assertFalse(isSectionExpanded(section, listOf("+Werkzeuge")))
        assertEquals(listOf("+TOOLS"), toggledOverrides(section, emptyList()))
    }

    @Test
    fun drawer_sectionToggleReportsNewOverridesWhenControlled() {
        var reported: List<String>? = null
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = listOf(AppNavSection(label = "Health", id = "HEALTH", items = listOf(item("Weight")))),
                sectionOverrides = emptyList(),
                onSectionOverridesChange = { reported = it }
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerSection("Health")).performClick()
        assertEquals(listOf("+HEALTH"), reported)
    }

    @Test
    fun drawer_controlledOverridesDecideExpansion() {
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = listOf(AppNavSection(label = "Health", id = "HEALTH", items = listOf(item("Weight")))),
                sectionOverrides = listOf("+HEALTH"),
                onSectionOverridesChange = {}
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Weight")).assertIsDisplayed()
    }

    private fun hideableSections(hideWeight: Boolean = true, hideAll: Boolean = false) = listOf(
        AppNavSection(
            label = "Health",
            id = "HEALTH",
            items = listOf(
                AppNavItem("Weight", Icons.Default.Home, selected = true, hidden = hideWeight || hideAll, onHideToggle = {}, onClick = {}),
                AppNavItem("Steps", Icons.Default.Home, selected = false, hidden = hideAll, onHideToggle = {}, onClick = {})
            )
        ),
        AppNavSection(
            label = "Tools",
            id = "TOOLS",
            items = listOf(AppNavItem("Alcohol", Icons.Default.Home, selected = false, hidden = true, onHideToggle = {}, onClick = {}))
        )
    )

    @Test
    fun drawer_hidesHiddenItemsAndEmptiedSectionsByDefault() {
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = hideableSections(),
                onShowHiddenChange = {},
                sectionOverrides = listOf("+HEALTH"),
                onSectionOverridesChange = {}
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Weight")).assertDoesNotExist()
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Steps")).assertExists()
        rule.onNodeWithTag(AppNavigationTestTags.drawerSection("Tools")).assertDoesNotExist()
    }

    @Test
    fun drawer_showHiddenRevealsHiddenItemsAndSections() {
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = hideableSections(),
                showHidden = true,
                onShowHiddenChange = {},
                sectionOverrides = listOf("+HEALTH", "+TOOLS"),
                onSectionOverridesChange = {}
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Weight")).assertExists()
        rule.onNodeWithTag(AppNavigationTestTags.drawerSection("Tools")).assertExists()
    }

    @Test
    fun drawer_showHiddenCheckboxToggleReportsNewValue() {
        var reported: Boolean? = null
        rule.setContent {
            AppSideNavDrawer(title = "My app", sections = hideableSections(), onShowHiddenChange = { reported = it })
        }
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_SHOW_HIDDEN).performClick()
        assertEquals(true, reported)
    }

    @Test
    fun drawer_showHiddenCheckboxIsAbsentWithoutHiddenItems() {
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = hideableSections(hideWeight = false).map { section ->
                    section.copy(items = section.items.map { it.copy(hidden = false) })
                },
                onShowHiddenChange = {}
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_SHOW_HIDDEN).assertDoesNotExist()
    }

    @Test
    fun drawer_showHiddenCheckboxIsAbsentWithoutCallback() {
        rule.setContent {
            AppSideNavDrawer(title = "My app", sections = hideableSections())
        }
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_SHOW_HIDDEN).assertDoesNotExist()
    }

    @Test
    fun drawer_hideButtonDescribesHideAndUnhideAction() {
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = hideableSections(),
                showHidden = true,
                onShowHiddenChange = {},
                sectionOverrides = listOf("+HEALTH"),
                onSectionOverridesChange = {}
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerHideToggle("Steps")).assertContentDescriptionEquals("Hide Steps")
        rule.onNodeWithTag(AppNavigationTestTags.drawerHideToggle("Weight")).assertContentDescriptionEquals("Show Weight again")
    }

    @Test
    fun drawer_hideButtonInvokesToggle() {
        var toggled = false
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = listOf(
                    AppNavSection(
                        label = "Health",
                        id = "HEALTH",
                        items = listOf(AppNavItem("Steps", Icons.Default.Home, selected = true, onHideToggle = { toggled = true }, onClick = {}))
                    )
                )
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerHideToggle("Steps")).performClick()
        assertTrue(toggled)
    }

    @Test
    fun drawer_hideButtonIsAbsentWithoutToggle() {
        rule.setContent {
            AppSideNavDrawer(
                title = "My app",
                sections = listOf(AppNavSection(label = "Health", items = listOf(item("Weight", selected = true))))
            )
        }
        rule.onNodeWithTag(AppNavigationTestTags.drawerHideToggle("Weight")).assertDoesNotExist()
    }

    @Test
    fun scaffold_rendersTopBarAndContentWithDrawer() {
        rule.setContent {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            AppNavigationScaffold(
                drawerState = drawerState,
                drawerContent = { Text("Drawer body") },
                topBar = { Text("Top bar") }
            ) { padding ->
                Text("Content ${padding.calculateTopPadding().value >= 0f}")
            }
        }
        rule.onNodeWithText("Top bar").assertIsDisplayed()
        rule.onNodeWithText("Content true").assertIsDisplayed()
    }

    @Test
    fun appNavColors_fallBackToMaterialThemeWhenNoLocalProvided() {
        var resolved: AppNavColors? = null
        var provided: AppNavColors? = null
        val custom = AppNavColors.fromMaterialThemeStub(Color.Red)
        rule.setContent {
            resolved = appNavColors()
            CompositionLocalProvider(LocalAppNavColors provides custom) {
                provided = appNavColors()
            }
        }
        assertNotNull(resolved)
        assertTrue(resolved !== custom)
        assertEquals(custom, provided)
    }

    private fun AppNavColors.Companion.fromMaterialThemeStub(color: Color) = AppNavColors(
        topBarBackground = color, topBarDivider = color, topBarSelectedPillBackground = color,
        topBarSelectedIcon = color, topBarSelectedLabel = color, topBarUnselectedIcon = color,
        topBarUnselectedLabel = color, drawerBackground = color, drawerTitle = color,
        drawerSectionLabel = color, selectedPill = color, drawerSelectedContainer = color,
        drawerUnselectedContainer = color, drawerSelectedIcon = color, drawerSelectedLabel = color,
        drawerUnselectedIcon = color, drawerUnselectedLabel = color,
        selectedTile = color, selectedTileIcon = color, accents = listOf(color)
    )

    @Test
    fun accentAt_wrapsAroundAndHandlesMissingIndexOrAccents() {
        val palette = AppNavColors.fromMaterialThemeStub(Color.Red).copy(accents = listOf(Color.Red, Color.Blue))
        assertEquals(Color.Red, palette.accentAt(0))
        assertEquals(Color.Blue, palette.accentAt(1))
        assertEquals(Color.Red, palette.accentAt(2))
        assertEquals(Color.Blue, palette.accentAt(-1))
        assertEquals(null, palette.accentAt(null))
        assertEquals(null, palette.copy(accents = emptyList()).accentAt(0))
    }

    @Test
    fun navItemDefaultsToEnabled() {
        val navItem = AppNavItem(label = "x", icon = Icons.Default.Settings, selected = false) {}
        assertTrue(navItem.enabled)
    }
}
