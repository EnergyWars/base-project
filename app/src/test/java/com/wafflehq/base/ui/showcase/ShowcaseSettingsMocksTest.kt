package com.wafflehq.base.ui.showcase

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ShowcaseSettingsMocksTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `top bar shows title and forwards back clicks`() {
        var backs = 0
        rule.setContent {
            MockSettingsTopBar(title = "Display", onBack = { backs++ }, backDescription = "Back")
        }

        rule.onNodeWithText("Display").assertExists()
        rule.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, backs)
    }

    @Test
    fun `list content shows both rows and subtitle`() {
        rule.setContent {
            MockSettingsListContent(
                featuresLabel = "Features",
                displayLabel = "Display",
                displaySubtitle = "Theme, colors",
            )
        }

        rule.onNodeWithText("Features").assertExists()
        rule.onNodeWithText("Display").assertExists()
        rule.onNodeWithText("Theme, colors").assertExists()
    }

    @Test
    fun `list rows with codes report them on double tap`() {
        rule.setContent {
            ElementInspectorHost(enabled = true) {
                MockSettingsListContent(
                    featuresLabel = "Features",
                    displayLabel = "Display",
                    displaySubtitle = "Sub",
                    featuresCode = "23a.1",
                    displayCode = "23a.2",
                )
            }
        }

        rule.onNodeWithText("Features").performTouchInput { doubleClick() }
        rule.onNodeWithText("23a.1").assertExists()
        rule.onNodeWithText("Close").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Display").performTouchInput { doubleClick() }
        rule.onNodeWithText("23a.2").assertExists()
    }

    @Test
    fun `list rows without codes are clickable and stay silent`() {
        rule.setContent {
            ElementInspectorHost(enabled = true) {
                MockSettingsListContent(featuresLabel = "Features", displayLabel = "Display", displaySubtitle = "Sub")
            }
        }

        rule.onNodeWithText("Features").performClick()
        rule.onNodeWithText("Features").performTouchInput { doubleClick() }

        rule.onNodeWithText("Element id").assertDoesNotExist()
    }

    @Test
    fun `display settings show all groups controls and the selected theme`() {
        rule.setContent {
            MockDisplaySettingsContent(themeMode = MockThemeMode.System, onThemeSelected = {})
        }

        rule.onNodeWithText("General").assertExists()
        rule.onNodeWithText("Lorem ipsum").assertExists()
        rule.onNodeWithText("Consectetur").assertExists()
        rule.onNodeWithText("System default").assertExists()
        rule.onNodeWithText("Font size").assertExists()
        rule.onNodeWithText("Contrast").assertExists()
        rule.onNodeWithText("110 %").assertExists()
        rule.onNodeWithText("3 / 5").assertExists()
        rule.onNodeWithText("Dolor sit amet").assertExists()
        rule.onNodeWithText("Adipiscing elit").assertExists()
    }

    @Test
    fun `display settings label follows every theme mode`() {
        var mode by mutableStateOf(MockThemeMode.System)
        rule.setContent {
            MockDisplaySettingsContent(themeMode = mode, onThemeSelected = {})
        }

        rule.onNodeWithText("System default").assertExists()
        mode = MockThemeMode.Light
        rule.waitForIdle()
        rule.onNodeWithText("Light").assertExists()
        mode = MockThemeMode.Dark
        rule.waitForIdle()
        rule.onNodeWithText("Dark").assertExists()
    }

    @Test
    fun `choosing an entry in the theme dropdown reports the mode`() {
        val selected = mutableListOf<MockThemeMode>()
        var mode by mutableStateOf(MockThemeMode.System)
        rule.setContent {
            MockDisplaySettingsContent(themeMode = mode, onThemeSelected = { selected += it; mode = it })
        }

        rule.onNodeWithText("System default").performClick()
        rule.onNodeWithText("Dark").performClick()
        rule.waitForIdle()

        assertEquals(listOf(MockThemeMode.Dark), selected)
        rule.onNodeWithText("Dark").assertExists()
    }

    @Test
    fun `dropdown offers all three modes when opened`() {
        rule.setContent {
            MockDisplaySettingsContent(themeMode = MockThemeMode.Light, onThemeSelected = {})
        }

        rule.onNodeWithText("Light").performClick()
        rule.waitForIdle()

        assertTrue(rule.onAllNodesWithText("Light").fetchSemanticsNodes().size >= 2)
        rule.onNodeWithText("System default").assertExists()
        rule.onNodeWithText("Dark").assertExists()
    }

    @Test
    fun `group codes report on double tap`() {
        rule.setContent {
            ElementInspectorHost(enabled = true) {
                MockDisplaySettingsContent(
                    themeMode = MockThemeMode.System,
                    onThemeSelected = {},
                    groupCodes = listOf("24a.1", "24a.2", "24a.3"),
                )
            }
        }

        rule.onNodeWithText("Lorem ipsum").performTouchInput { doubleClick() }

        rule.onNodeWithText("24a.2").assertExists()
    }

    @Test
    fun `switch rows toggle their state`() {
        rule.setContent {
            MockDisplaySettingsContent(themeMode = MockThemeMode.System, onThemeSelected = {})
        }

        val switches = rule.onAllNodes(isToggleable())
        switches[0].assertIsOn()
        switches[1].assertIsOff()

        switches[0].performClick()
        switches[1].performClick()
        rule.waitForIdle()

        switches[0].assertIsOff()
        switches[1].assertIsOn()
    }

    @Test
    fun `side nav drawer renders title section label and items`() {
        rule.setContent {
            MockSideNavDrawer(
                title = "App",
                sections = listOf(
                    MockNavSection(
                        label = "Pages",
                        items = listOf(
                            MockNavItem("Home", Icons.Outlined.Home, selected = true, onClick = {}),
                            MockNavItem("Settings", Icons.Outlined.Settings, selected = false, onClick = {}),
                        ),
                    ),
                    MockNavSection(
                        items = listOf(MockNavItem("Unlabelled", Icons.Outlined.Home, selected = false, onClick = {})),
                    ),
                ),
            )
        }

        rule.onNodeWithText("App").assertExists()
        rule.onNodeWithText("Pages").assertExists()
        rule.onNodeWithText("Home").assertExists()
        rule.onNodeWithText("Settings").assertExists()
        rule.onNodeWithText("Unlabelled").assertExists()
    }

    @Test
    fun `side nav drawer forwards item clicks`() {
        val clicked = mutableListOf<String>()
        rule.setContent {
            MockSideNavDrawer(
                title = "App",
                sections = listOf(
                    MockNavSection(
                        items = listOf(
                            MockNavItem("Home", Icons.Outlined.Home, selected = true, onClick = { clicked += "Home" }),
                            MockNavItem("Settings", Icons.Outlined.Settings, selected = false, onClick = { clicked += "Settings" }),
                        ),
                    ),
                ),
            )
        }

        rule.onNodeWithText("Settings").performClick()

        assertEquals(listOf("Settings"), clicked)
    }

    @Test
    fun `side nav drawer items with inspect codes report them on double tap`() {
        rule.setContent {
            ElementInspectorHost(enabled = true) {
                MockSideNavDrawer(
                    title = "App",
                    sections = listOf(
                        MockNavSection(
                            items = listOf(
                                MockNavItem("Home", Icons.Outlined.Home, selected = true, onClick = {}, inspectCode = "22g.1"),
                                MockNavItem("Settings", Icons.Outlined.Settings, selected = false, onClick = {}),
                            ),
                        ),
                    ),
                )
            }
        }

        rule.onNodeWithText("Home").performTouchInput { doubleClick() }

        rule.onNodeWithText("22g.1").assertExists()
    }
}
