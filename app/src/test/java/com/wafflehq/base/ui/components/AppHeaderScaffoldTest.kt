package com.wafflehq.base.ui.components

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.base.data.model.ThemeMode
import com.wafflehq.base.ui.theme.BaseAppTheme
import com.wafflehq.lib.uicore.scaffold.AppScaffoldTestTags
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
class AppHeaderScaffoldTest {

    @get:Rule
    val rule = createComposeRule()

    private var menuClicks = 0
    private var homeClicks = 0
    private var settingsClicks = 0

    private fun show(activeItem: HeaderItem = HeaderItem.Home) {
        rule.setContent {
            BaseAppTheme(themeMode = ThemeMode.LIGHT) {
                AppHeaderScaffold(
                    title = "Title",
                    activeItem = activeItem,
                    onOpenMenu = { menuClicks++ },
                    onNavigateHome = { homeClicks++ },
                    onOpenSettings = { settingsClicks++ }
                ) {
                    Text("Body")
                }
            }
        }
    }

    @Test
    fun `title and content are displayed`() {
        show()

        rule.onNodeWithTag(AppScaffoldTestTags.TITLE).assertTextEquals("Title")
        rule.onNodeWithText("Body").assertIsDisplayed()
    }

    @Test
    fun `menu button invokes the menu callback`() {
        show()

        rule.onNodeWithTag(AppScaffoldTestTags.BACK).performClick()

        assertEquals(1, menuClicks)
        assertEquals(0, homeClicks)
        assertEquals(0, settingsClicks)
    }

    @Test
    fun `home button invokes the home callback`() {
        show(HeaderItem.Settings)

        rule.onNodeWithContentDescription("Home").performClick()

        assertEquals(1, homeClicks)
    }

    @Test
    fun `settings button invokes the settings callback`() {
        show(HeaderItem.None)

        rule.onNodeWithContentDescription("Open settings").performClick()

        assertEquals(1, settingsClicks)
    }
}
