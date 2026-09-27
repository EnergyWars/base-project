package com.wafflehq.lib.uicore.menu

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
class AppDropdownMenuItemTest {

    @get:Rule
    val rule = createComposeRule()

    private val scheme = lightColorScheme(
        primary = Color(0xFF0000FF),
        onPrimary = Color(0xFFFFFFFF),
        surface = Color(0xFFEEEEEE),
        onSurface = Color(0xFF111111),
        onSurfaceVariant = Color(0xFF444444),
    )

    @Test
    fun `item renders its text and reports clicks`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppDropdownMenuItem(text = { Text("Entry") }, onClick = { clicks++ })
            }
        }
        rule.onNodeWithText("Entry").assertIsDisplayed().performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `selected item is marked as selected and others are not`() {
        rule.setContent {
            MaterialTheme {
                AppDropdownMenuItem(text = { Text("Chosen") }, selected = true, onClick = {})
                AppDropdownMenuItem(text = { Text("Other") }, selected = false, onClick = {})
            }
        }
        rule.onNodeWithText("Chosen").assertIsSelected()
        rule.onNodeWithText("Other").assertIsNotSelected()
    }

    @Test
    fun `disabled item is not enabled and ignores clicks`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppDropdownMenuItem(text = { Text("Off") }, enabled = false, onClick = { clicks++ })
            }
        }
        rule.onNodeWithText("Off").assertIsNotEnabled().performClick()
        assertEquals(0, clicks)
    }

    @Test
    fun `selected container uses the primary role like the pill tabs`() {
        var selected: Color? = null
        var unselected: Color? = null
        rule.setContent {
            MaterialTheme(colorScheme = scheme) {
                selected = AppDropdownMenuItemDefaults.containerColor(true)
                unselected = AppDropdownMenuItemDefaults.containerColor(false)
            }
        }
        assertEquals(scheme.primary, selected)
        assertEquals(Color.Transparent, unselected)
    }

    @Test
    fun `selected colors use onPrimary and unselected colors use surface roles`() {
        var selectedText: Color? = null
        var selectedIcon: Color? = null
        var plainText: Color? = null
        var plainIcon: Color? = null
        rule.setContent {
            MaterialTheme(colorScheme = scheme) {
                val chosen = AppDropdownMenuItemDefaults.colors(selected = true)
                val plain = AppDropdownMenuItemDefaults.colors(selected = false)
                selectedText = chosen.textColor
                selectedIcon = chosen.leadingIconColor
                plainText = plain.textColor
                plainIcon = plain.leadingIconColor
            }
        }
        assertEquals(scheme.onPrimary, selectedText)
        assertEquals(scheme.onPrimary, selectedIcon)
        assertEquals(scheme.onSurface, plainText)
        assertEquals(scheme.onSurfaceVariant, plainIcon)
        assertTrue(selectedText != plainText)
        assertFalse(selectedIcon == plainIcon)
    }
}
