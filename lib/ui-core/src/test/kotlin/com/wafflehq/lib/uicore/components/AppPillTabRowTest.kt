package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
class AppPillTabRowTest {

    @get:Rule
    val rule = createComposeRule()

    private enum class Mode { DAY, WEEK, MONTH, YEAR }

    @Test
    fun `all tab labels are rendered`() {
        rule.setContent {
            MaterialTheme {
                AppPillSelector(
                    options = Mode.entries,
                    selected = Mode.MONTH,
                    onSelect = {},
                    label = { it.name }
                )
            }
        }
        Mode.entries.forEach { rule.onNodeWithText(it.name).assertIsDisplayed() }
    }

    @Test
    fun `only the selected option is marked as selected`() {
        rule.setContent {
            MaterialTheme {
                AppPillSelector(
                    options = Mode.entries,
                    selected = Mode.WEEK,
                    onSelect = {},
                    label = { it.name }
                )
            }
        }
        rule.onNodeWithText("WEEK").assertIsSelected()
        rule.onNodeWithText("DAY").assertIsNotSelected()
        rule.onNodeWithText("MONTH").assertIsNotSelected()
        rule.onNodeWithText("YEAR").assertIsNotSelected()
    }

    @Test
    fun `clicking an option reports it`() {
        var picked: Mode? = null
        rule.setContent {
            MaterialTheme {
                AppPillSelector(
                    options = Mode.entries,
                    selected = Mode.DAY,
                    onSelect = { picked = it },
                    label = { it.name }
                )
            }
        }
        rule.onNodeWithText("YEAR").performClick()
        assertEquals(Mode.YEAR, picked)
    }

    @Test
    fun `selection follows state changes`() {
        rule.setContent {
            MaterialTheme {
                var current by remember { mutableStateOf(Mode.DAY) }
                AppPillSelector(
                    options = Mode.entries,
                    selected = current,
                    onSelect = { current = it },
                    label = { it.name }
                )
            }
        }
        rule.onNodeWithText("DAY").assertIsSelected()
        rule.onNodeWithText("MONTH").performClick()
        rule.onNodeWithText("MONTH").assertIsSelected()
        rule.onNodeWithText("DAY").assertIsNotSelected()
    }

    @Test
    fun `disabled selector does not report clicks`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppPillSelector(
                    options = Mode.entries,
                    selected = Mode.DAY,
                    onSelect = { clicks++ },
                    label = { it.name },
                    enabled = false
                )
            }
        }
        rule.onNodeWithText("WEEK").assertIsNotEnabled()
        rule.onNodeWithText("WEEK").performClick()
        assertEquals(0, clicks)
    }

    @Test
    fun `enabled selector exposes enabled tabs`() {
        rule.setContent {
            MaterialTheme {
                AppPillSelector(
                    options = Mode.entries,
                    selected = Mode.DAY,
                    onSelect = {},
                    label = { it.name }
                )
            }
        }
        rule.onNodeWithText("WEEK").assertIsEnabled()
    }

    @Test
    fun `test tags are applied per option`() {
        rule.setContent {
            MaterialTheme {
                AppPillSelector(
                    options = Mode.entries,
                    selected = Mode.DAY,
                    onSelect = {},
                    label = { it.name },
                    testTag = { "mode_${it.name}" }
                )
            }
        }
        rule.onNodeWithTag("mode_MONTH").assertIsDisplayed()
    }

    @Test
    fun `tabs row supports individually disabled tabs`() {
        var clicked = false
        rule.setContent {
            MaterialTheme {
                AppPillTabRow(
                    tabs = listOf(
                        AppPillTab("One", selected = true, onClick = {}),
                        AppPillTab("Two", selected = false, onClick = { clicked = true }, enabled = false)
                    )
                )
            }
        }
        rule.onNodeWithText("Two").assertIsNotEnabled()
        rule.onNodeWithText("Two").performClick()
        assertFalse(clicked)
        rule.onNodeWithText("One").assertIsEnabled()
    }

    @Test
    fun `tab popup is rendered next to its tab`() {
        rule.setContent {
            MaterialTheme {
                AppPillTabRow(
                    tabs = listOf(
                        AppPillTab("One", selected = true, onClick = {}),
                        AppPillTab("More", selected = false, onClick = {}, popup = { Text("Popup content") })
                    )
                )
            }
        }
        rule.onNodeWithText("Popup content").assertIsDisplayed()
    }

    @Test
    fun `empty tab list renders without crashing`() {
        rule.setContent {
            MaterialTheme {
                AppPillTabRow(tabs = emptyList())
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `single tab renders and is selectable`() {
        var clicked = false
        rule.setContent {
            MaterialTheme {
                AppPillTabRow(tabs = listOf(AppPillTab("Only", selected = false, onClick = { clicked = true })))
            }
        }
        rule.onNodeWithText("Only").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `tabs share the available width equally`() {
        rule.setContent {
            MaterialTheme {
                AppPillTabRow(
                    tabs = listOf(
                        AppPillTab("A", selected = true, onClick = {}, testTag = "a"),
                        AppPillTab("A much longer label", selected = false, onClick = {}, testTag = "b")
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        val a = rule.onNodeWithTag("a").fetchSemanticsNode().boundsInRoot.width
        val b = rule.onNodeWithTag("b").fetchSemanticsNode().boundsInRoot.width
        assertEquals(a, b, 1f)
    }

    @Test
    fun `wrapping row sizes all tabs to the widest one`() {
        rule.setContent {
            MaterialTheme {
                AppPillTabRow(
                    tabs = listOf(
                        AppPillTab("A", selected = true, onClick = {}, testTag = "a"),
                        AppPillTab("Considerably longer", selected = false, onClick = {}, testTag = "b")
                    )
                )
            }
        }
        val a = rule.onNodeWithTag("a").fetchSemanticsNode().boundsInRoot.width
        val b = rule.onNodeWithTag("b").fetchSemanticsNode().boundsInRoot.width
        assertEquals(a, b, 1f)
    }

    @Test
    fun `default colors follow the material color scheme`() {
        val scheme = lightColorScheme(
            primary = Color(0xFF0E3D6E),
            onPrimary = Color.White,
            onSurface = Color(0xFF191C22),
            outline = Color(0xFF8B95A8)
        )
        var colors: AppPillTabColors? = null
        rule.setContent {
            MaterialTheme(colorScheme = scheme) {
                colors = AppPillTabDefaults.colors()
            }
        }
        val resolved = colors!!
        assertEquals(scheme.primary, resolved.selectedContainer)
        assertEquals(scheme.onPrimary, resolved.selectedContent)
        assertEquals(scheme.onSurface, resolved.unselectedContent)
        assertEquals(scheme.outline, resolved.trackBorder)
        assertEquals(Color.Transparent, resolved.trackContainer)
        assertEquals(Color.Transparent, resolved.unselectedContainer)
        assertEquals(Color.Transparent, resolved.selectedBorder)
        assertEquals(Color.Transparent, resolved.unselectedBorder)
        assertEquals(scheme.onSurface.copy(alpha = AppPillTabDefaults.DISABLED_CONTENT_ALPHA), resolved.disabledContent)
    }

    @Test
    fun `custom colors override the defaults`() {
        var colors: AppPillTabColors? = null
        rule.setContent {
            MaterialTheme {
                colors = AppPillTabDefaults.colors(
                    selectedContainer = Color.Red,
                    selectedContent = Color.Green,
                    trackBorder = Color.Blue
                )
            }
        }
        val resolved = colors!!
        assertEquals(Color.Red, resolved.selectedContainer)
        assertEquals(Color.Green, resolved.selectedContent)
        assertEquals(Color.Blue, resolved.trackBorder)
    }

    @Test
    fun `long labels stay on a single line without crashing`() {
        rule.setContent {
            MaterialTheme {
                AppPillTabRow(
                    tabs = listOf(
                        AppPillTab("An extremely long label that cannot possibly fit", selected = true, onClick = {}),
                        AppPillTab("Short", selected = false, onClick = {})
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        rule.onNodeWithText("Short").assertIsDisplayed()
    }
}
