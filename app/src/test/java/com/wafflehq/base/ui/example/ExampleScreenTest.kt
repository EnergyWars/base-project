package com.wafflehq.base.ui.example

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.base.R
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
class ExampleScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private var menuClicks = 0

    @Test
    fun `example screen shows its title and the lorem sections`() {
        rule.setContent {
            BaseAppTheme(themeMode = ThemeMode.LIGHT) {
                ExampleScreen(
                    titleRes = R.string.example_2_title,
                    onOpenMenu = { menuClicks++ },
                    onNavigateHome = {},
                    onOpenSettings = {}
                )
            }
        }

        rule.onNodeWithTag(AppScaffoldTestTags.TITLE).assertTextEquals("Example 2")
        rule.onNodeWithText("Lorem ipsum").assertIsDisplayed()
        rule.onNodeWithText("Dolor sit amet").assertIsDisplayed()
        rule.onNodeWithText("Consetetur").assertIsDisplayed()
    }

    @Test
    fun `menu button opens the menu`() {
        rule.setContent {
            BaseAppTheme(themeMode = ThemeMode.LIGHT) {
                ExampleScreen(
                    titleRes = R.string.example_1_title,
                    onOpenMenu = { menuClicks++ },
                    onNavigateHome = {},
                    onOpenSettings = {}
                )
            }
        }

        rule.onNodeWithTag(AppScaffoldTestTags.BACK).performClick()

        assertEquals(1, menuClicks)
    }
}
