package com.wafflehq.uikit.navigation

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.uikit.theme.AppTheme
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
class AppNavigationShellTest {

    @get:Rule
    val rule = createComposeRule()

    private fun item(label: String, selected: Boolean = false, enabled: Boolean = true, onClick: () -> Unit = {}) =
        AppNavItem(label = label, icon = Icons.Default.Home, selected = selected, enabled = enabled, onClick = onClick)

    @Test
    fun topBar_rendersAllItemsAndInvokesClick() {
        var clicked = ""
        rule.setContent {
            AppTheme {
                AppTopNavBar(
                    items = listOf(
                        item("Menu", onClick = { clicked = "Menu" }),
                        item("Calendar", selected = true, onClick = { clicked = "Calendar" }),
                        item("Settings", onClick = { clicked = "Settings" }),
                    ),
                )
            }
        }
        rule.onNodeWithTag(AppNavigationTestTags.TOP_BAR).assertIsDisplayed()
        rule.onNodeWithText("Menu").assertIsDisplayed()
        rule.onNodeWithText("Calendar").assertIsDisplayed()
        rule.onNodeWithText("Settings").assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.topBarTab("Settings")).performClick()
        assertEquals("Settings", clicked)
    }

    @Test
    fun topBar_disabledItemDoesNotInvokeClick() {
        var clicked = false
        rule.setContent {
            AppTheme {
                AppTopNavBar(items = listOf(item("Menu", enabled = false, onClick = { clicked = true })))
            }
        }
        rule.onNodeWithTag(AppNavigationTestTags.topBarTab("Menu")).assertIsNotEnabled()
        rule.onNodeWithTag(AppNavigationTestTags.topBarTab("Menu")).performClick()
        assertFalse(clicked)
    }

    @Test
    fun drawer_rendersTitleSectionsAndItems() {
        var selected = ""
        rule.setContent {
            AppTheme {
                AppSideNavDrawer(
                    title = "My app",
                    sections = listOf(
                        AppNavSection(
                            label = "Pages",
                            items = listOf(
                                item("Calendar", selected = true, onClick = { selected = "Calendar" }),
                                item("Weight", onClick = { selected = "Weight" }),
                            ),
                        ),
                        AppNavSection(items = listOf(item("Unlabeled"))),
                    ),
                )
            }
        }
        rule.onNodeWithTag(AppNavigationTestTags.DRAWER).assertIsDisplayed()
        rule.onNodeWithText("My app").assertIsDisplayed()
        rule.onNodeWithText("Pages").assertIsDisplayed()
        rule.onNodeWithText("Unlabeled").assertIsDisplayed()
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Calendar")).assertIsSelected()
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Weight")).assertIsNotSelected()
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Weight")).performClick()
        assertEquals("Weight", selected)
    }

    @Test
    fun scaffold_rendersTopBarAndContentWithDrawer() {
        rule.setContent {
            AppTheme {
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                AppNavigationScaffold(
                    drawerState = drawerState,
                    drawerContent = { Text("Drawer body") },
                    topBar = { Text("Top bar") },
                ) { padding ->
                    Text("Content ${padding.calculateTopPadding().value >= 0f}")
                }
            }
        }
        rule.onNodeWithText("Top bar").assertIsDisplayed()
        rule.onNodeWithText("Content true").assertIsDisplayed()
    }

    @Test
    fun appNavColors_fallsBackToAppThemeWhenNoLocalProvided() {
        var resolved: AppNavColors? = null
        var provided: AppNavColors? = null
        val custom = stubAppNavColors(Color.Red)
        rule.setContent {
            AppTheme {
                resolved = appNavColors()
                CompositionLocalProvider(LocalAppNavColors provides custom) {
                    provided = appNavColors()
                }
            }
        }
        assertNotNull(resolved)
        assertTrue(resolved !== custom)
        assertEquals(custom, provided)
    }

    private fun stubAppNavColors(color: Color) = AppNavColors(
        topBarBackground = color,
        topBarDivider = color,
        topBarSelectedPillBackground = color,
        topBarSelectedIcon = color,
        topBarSelectedLabel = color,
        topBarUnselectedIcon = color,
        topBarUnselectedLabel = color,
        drawerBackground = color,
        drawerTitle = color,
        drawerSectionLabel = color,
        selectedPill = color,
        drawerSelectedContainer = color,
        drawerUnselectedContainer = color,
        drawerSelectedIcon = color,
        drawerSelectedLabel = color,
        drawerUnselectedIcon = color,
        drawerUnselectedLabel = color,
    )

    @Test
    fun navItemDefaultsToEnabled() {
        val navItem = AppNavItem(label = "x", icon = Icons.Default.Settings, selected = false) {}
        assertTrue(navItem.enabled)
    }
}
