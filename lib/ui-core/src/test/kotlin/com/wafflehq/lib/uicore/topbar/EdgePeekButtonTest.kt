package com.wafflehq.lib.uicore.topbar

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.uicore.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EdgePeekButtonTest {

    @get:Rule
    val rule = createComposeRule()

    private fun str(id: Int) = ApplicationProvider.getApplicationContext<Application>().getString(id)

    @Test
    fun edgePeekButtonHasHalfHeightAndIsClickable() {
        var clicks = 0
        rule.setContent {
            EdgePeekButton(edge = ScreenEdge.Bottom, onClick = { clicks++ }, icon = Icons.Default.Home, contentDescription = "home")
        }

        rule.onNodeWithContentDescription("home").assertIsDisplayed()
        rule.onNode(hasClickAction())
            .assertWidthIsEqualTo(EdgePeekButtonDiameter)
            .assertHeightIsEqualTo(EdgePeekButtonVisibleHeight)
            .performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun edgePeekButtonHonorsCustomDiameter() {
        val customDiameter = 56.dp
        rule.setContent {
            EdgePeekButton(
                edge = ScreenEdge.Bottom,
                onClick = {},
                icon = Icons.Default.Home,
                contentDescription = "home",
                diameter = customDiameter
            )
        }

        rule.onNode(hasClickAction())
            .assertWidthIsEqualTo(customDiameter)
            .assertHeightIsEqualTo(customDiameter / 2)
    }

    @Test
    fun edgePeekButtonStartEdgeHasHalfWidthAndFullHeight() {
        var clicks = 0
        rule.setContent {
            EdgePeekButton(edge = ScreenEdge.Start, onClick = { clicks++ }, icon = Icons.Default.Home, contentDescription = "home")
        }

        rule.onNodeWithContentDescription("home").assertIsDisplayed()
        rule.onNode(hasClickAction())
            .assertWidthIsEqualTo(EdgePeekButtonVisibleHeight)
            .assertHeightIsEqualTo(EdgePeekButtonDiameter)
            .performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun tabStyleKeepsIdleFootprintOfBubbleStyle() {
        rule.setContent {
            EdgePeekButton(
                edge = ScreenEdge.Start,
                onClick = {},
                icon = Icons.Default.Home,
                contentDescription = "home",
                diameter = 56.dp,
                style = EdgePeekStyle.Tab
            )
        }

        rule.onNode(hasClickAction())
            .assertWidthIsEqualTo(28.dp)
            .assertHeightIsEqualTo(56.dp)
    }

    @Test
    fun longPressInvokesLongClickWithoutClicking() {
        var clicks = 0
        var longClicks = 0
        rule.setContent {
            EdgePeekButton(
                edge = ScreenEdge.Start,
                onClick = { clicks++ },
                icon = Icons.Default.Home,
                contentDescription = "home",
                style = EdgePeekStyle.Tab,
                onLongClick = { longClicks++ },
                onLongClickLabel = "configure"
            )
        }

        rule.onNode(hasClickAction()).performTouchInput { longClick() }

        assertEquals(1, longClicks)
        assertEquals(0, clicks)
    }

    @Test
    fun tapStillWorksWhileLongClickHandlerIsSet() {
        var clicks = 0
        var longClicks = 0
        rule.setContent {
            EdgePeekButton(
                edge = ScreenEdge.Start,
                onClick = { clicks++ },
                icon = Icons.Default.Home,
                contentDescription = "home",
                style = EdgePeekStyle.Tab,
                onLongClick = { longClicks++ }
            )
        }

        rule.onNode(hasClickAction()).performClick()

        assertEquals(1, clicks)
        assertEquals(0, longClicks)
    }

    @Test
    fun topBarRevealButtonUsesLibraryContentDescription() {
        var clicks = 0
        rule.setContent { TopBarRevealButton(onClick = { clicks++ }) }

        rule.onNodeWithContentDescription(str(R.string.uicore_top_bar_expand_cd)).assertIsDisplayed()
        rule.onNode(hasClickAction()).performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun revealButtonPositionsCoverAllAlignments() {
        assertEquals(listOf("LEFT", "CENTER", "RIGHT"), TopBarRevealButtonPosition.entries.map { it.name })
        assertEquals(listOf("Bubble", "Tab"), EdgePeekStyle.entries.map { it.name })
        assertEquals(EdgePeekButtonDiameter / 2, EdgePeekButtonVisibleHeight)
    }
}
