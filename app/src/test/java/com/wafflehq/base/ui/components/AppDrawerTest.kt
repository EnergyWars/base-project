package com.wafflehq.base.ui.components

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.wafflehq.base.data.model.ThemeMode
import com.wafflehq.base.ui.navigation.Routes
import com.wafflehq.base.ui.theme.BaseAppTheme
import com.wafflehq.lib.navigation.shell.AppNavigationTestTags
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
class AppDrawerTest {

    @get:Rule
    val rule = createComposeRule()

    private val selected = mutableListOf<String>()
    private var closed = 0

    private fun show(currentRoute: String?) {
        rule.setContent {
            BaseAppTheme(themeMode = ThemeMode.LIGHT) {
                AppDrawer(
                    currentRoute = currentRoute,
                    onSelect = { selected += it },
                    onClose = { closed++ }
                )
            }
        }
    }

    @Test
    fun `drawer shows all pages when a page is selected`() {
        show(Routes.HOME)

        listOf("Home", "Example 1", "Example 2", "Example 3", "Libraries", "Settings").forEach { label ->
            rule.onNodeWithTag(AppNavigationTestTags.drawerItem(label)).assertIsDisplayed()
        }
    }

    @Test
    fun `selecting a page reports its route`() {
        show(Routes.HOME)

        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Example 2")).performClick()
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Libraries")).performClick()
        rule.onNodeWithTag(AppNavigationTestTags.drawerItem("Settings")).performClick()

        assertEquals(listOf(Routes.EXAMPLE_2, Routes.LIBRARY_EXAMPLES, Routes.SETTINGS), selected)
    }

    @Test
    fun `close button invokes the close callback`() {
        show(Routes.HOME)

        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_CLOSE).performClick()

        assertEquals(1, closed)
    }

    @Test
    fun `search field is present`() {
        show(Routes.HOME)

        rule.onNodeWithTag(AppNavigationTestTags.DRAWER_SEARCH).assertIsDisplayed()
    }
}
