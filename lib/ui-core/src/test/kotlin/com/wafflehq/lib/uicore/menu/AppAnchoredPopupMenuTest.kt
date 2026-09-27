package com.wafflehq.lib.uicore.menu

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppAnchoredPopupMenuTest {

    @get:Rule
    val rule = createComposeRule()

    private val provider = StartOfAnchorPositionProvider(gapPx = 8)
    private val window = IntSize(1000, 2000)
    private val popup = IntSize(300, 400)

    @Test
    fun `popup opens to the left of the anchor in left-to-right layouts`() {
        val anchor = IntRect(left = 800, top = 1000, right = 900, bottom = 1100)
        val position = provider.calculatePosition(anchor, window, LayoutDirection.Ltr, popup)
        assertEquals(IntOffset(800 - 300 - 8, 1000), position)
    }

    @Test
    fun `popup opens to the right of the anchor in right-to-left layouts`() {
        val anchor = IntRect(left = 100, top = 1000, right = 200, bottom = 1100)
        val position = provider.calculatePosition(anchor, window, LayoutDirection.Rtl, popup)
        assertEquals(IntOffset(200 + 8, 1000), position)
    }

    @Test
    fun `popup is shifted up when it would leave the window at the bottom`() {
        val anchor = IntRect(left = 800, top = 1900, right = 900, bottom = 2000)
        val position = provider.calculatePosition(anchor, window, LayoutDirection.Ltr, popup)
        assertEquals(1600, position.y)
    }

    @Test
    fun `popup never starts above the window`() {
        val anchor = IntRect(left = 800, top = -50, right = 900, bottom = 50)
        val position = provider.calculatePosition(anchor, window, LayoutDirection.Ltr, popup)
        assertEquals(0, position.y)
    }

    @Test
    fun `popup taller than the window is pinned to the top`() {
        val anchor = IntRect(left = 800, top = 500, right = 900, bottom = 600)
        val position = provider.calculatePosition(anchor, window, LayoutDirection.Ltr, IntSize(300, 3000))
        assertEquals(0, position.y)
    }

    @Test
    fun `anchor is always shown and content only while expanded`() {
        rule.setContent {
            MaterialTheme {
                AppAnchoredPopupMenu(
                    expanded = false,
                    onDismissRequest = {},
                    anchor = { Text("Anchor") },
                ) {
                    Text("Content")
                }
            }
        }
        rule.onNodeWithText("Anchor").assertIsDisplayed()
        rule.onNodeWithText("Content").assertDoesNotExist()
    }

    @Test
    fun `expanded menu shows its items and reports clicks`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppAnchoredPopupMenu(
                    expanded = true,
                    onDismissRequest = {},
                    anchor = { Text("Anchor") },
                ) {
                    AppDropdownMenuItem(text = { Text("Content") }, onClick = { clicks++ })
                }
            }
        }
        rule.onNodeWithText("Anchor").assertIsDisplayed()
        rule.onNodeWithText("Content").assertIsDisplayed().performClick()
        assertEquals(1, clicks)
    }
}
