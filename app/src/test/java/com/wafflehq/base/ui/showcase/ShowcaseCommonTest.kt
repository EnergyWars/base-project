package com.wafflehq.base.ui.showcase

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppColors
import com.wafflehq.base.ui.theme.AppRole
import com.wafflehq.base.ui.theme.AppTheme
import com.wafflehq.base.ui.theme.LocalAppColors
import com.wafflehq.base.ui.theme.RoleColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
class ShowcaseCommonTest {

    @get:Rule
    val rule = createComposeRule()

    private fun distinctRole(seed: Int): RoleColors = RoleColors(
        accent = Color(seed, 0, 0),
        onAccent = Color(0, seed, 0),
        container = Color(0, 0, seed),
        onContainer = Color(seed, seed, 0),
        tonalBorder = Color(0, seed, seed),
    )

    private fun distinctColors(): AppColors = AppColors(
        primary = distinctRole(10),
        secondary = distinctRole(20),
        tertiary = distinctRole(30),
        success = distinctRole(40),
        warning = distinctRole(50),
        error = distinctRole(60),
        neutral = distinctRole(70),
        background = Color.Black,
        onBackground = Color.White,
        surface = Color.Black,
        onSurface = Color.White,
        surfaceVariant = Color.Gray,
        onSurfaceVariant = Color.LightGray,
        surface3 = Color.DarkGray,
        outline = Color.Gray,
    )

    @Test
    fun `hex formats opaque colors as upper case six digit hex`() {
        assertEquals("#1A2B3C", Color(0xFF1A2B3C).hex())
        assertEquals("#000000", Color.Black.hex())
        assertEquals("#FFFFFF", Color.White.hex())
        assertEquals("#FF0000", Color.Red.hex())
    }

    @Test
    fun `hex ignores the alpha channel`() {
        assertEquals("#1A2B3C", Color(0x801A2B3C).hex())
        assertEquals("#1A2B3C", Color(0x001A2B3C).hex())
    }

    @Test
    fun `hex keeps leading zeros`() {
        assertEquals("#00000F", Color(0xFF00000F).hex())
        assertEquals("#010203", Color(0xFF010203).hex())
    }

    @Test
    fun `role maps every app role to its own color set`() {
        val custom = distinctColors()
        val resolved = mutableMapOf<AppRole, RoleColors>()
        rule.setContent {
            CompositionLocalProvider(LocalAppColors provides custom) {
                AppRole.entries.forEach { resolved[it] = role(it) }
            }
        }
        rule.waitForIdle()

        assertEquals(custom.primary, resolved.getValue(AppRole.Primary))
        assertEquals(custom.secondary, resolved.getValue(AppRole.Secondary))
        assertEquals(custom.tertiary, resolved.getValue(AppRole.Tertiary))
        assertEquals(custom.success, resolved.getValue(AppRole.Success))
        assertEquals(custom.warning, resolved.getValue(AppRole.Warning))
        assertEquals(custom.error, resolved.getValue(AppRole.Error))
        assertEquals(custom.neutral, resolved.getValue(AppRole.Neutral))
        assertEquals(AppRole.entries.size, resolved.values.toSet().size)
    }

    @Test
    fun `role follows the current theme colors`() {
        var resolved: RoleColors? = null
        var defaultPrimary: RoleColors? = null
        rule.setContent {
            defaultPrimary = AppTheme.colors.primary
            resolved = role(AppRole.Primary)
        }
        rule.waitForIdle()

        assertEquals(defaultPrimary, resolved)
        assertNotEquals(distinctRole(10), resolved)
    }

    @Test
    fun `mix color interpolates between base and accent`() {
        val start = mixColor(Color.Black, Color.White, 0f)
        val end = mixColor(Color.Black, Color.White, 1f)
        val middle = mixColor(Color.Black, Color.White, 0.5f)

        assertEquals(0f, start.red, 0.01f)
        assertEquals(1f, end.red, 0.01f)
        assertTrue(middle.red > 0.05f && middle.red < 0.95f)
        assertEquals(middle.red, middle.green, 0.01f)
        assertEquals(middle.green, middle.blue, 0.01f)
    }

    @Test
    fun `section renders title description and content`() {
        rule.setContent {
            Section(R.string.sc_s1_title, R.string.sc_s1_desc) {
                Text("content", modifier = Modifier.testTag("content"))
            }
        }

        rule.onNodeWithText("1 · Typography").assertExists()
        rule.onNodeWithTag("content").assertExists()
    }

    @Test
    fun `panel and helpers render their content`() {
        rule.setContent {
            Panel {
                Subhead("SUB")
                WrapRow { Text("wrapped") }
                Divider()
            }
        }

        rule.onNodeWithText("SUB").assertExists()
        rule.onNodeWithText("wrapped").assertExists()
    }

    @Test
    fun `showcase button renders every variant in enabled and disabled state`() {
        rule.setContent {
            BtnVariant.entries.forEach { variant ->
                AppRole.entries.forEach { role ->
                    ShowcaseButton("${variant.name}-${role.name}-on", role, variant)
                    ShowcaseButton("${variant.name}-${role.name}-off", role, variant, enabled = false)
                }
            }
        }

        BtnVariant.entries.forEach { variant ->
            AppRole.entries.forEach { role ->
                rule.onNodeWithText("${variant.name}-${role.name}-on").assertExists()
                rule.onNodeWithText("${variant.name}-${role.name}-off").assertExists()
            }
        }
    }

    @Test
    fun `showcase button with inspect code reports it on double tap`() {
        rule.setContent {
            ElementInspectorHost(enabled = true) {
                ShowcaseButton("Tap", AppRole.Primary, BtnVariant.Filled, inspectCode = "6a.1")
            }
        }

        rule.onNodeWithText("Tap").performTouchInput { doubleClick() }

        rule.onNodeWithText("6a.1").assertExists()
    }

    @Test
    fun `showcase button without inspect code stays silent on double tap`() {
        rule.setContent {
            ElementInspectorHost(enabled = true) {
                ShowcaseButton("Tap", AppRole.Primary, BtnVariant.Filled)
            }
        }

        rule.onNodeWithText("Tap").performTouchInput { doubleClick() }

        rule.onNodeWithText("Element id").assertDoesNotExist()
    }

    @Test
    fun `settings mock shows caption and reports its group code`() {
        rule.setContent {
            ElementInspectorHost(enabled = true) {
                SettingsMock(R.string.sc_set_list_cap, groupCode = "23a") {
                    Text("body", modifier = Modifier.testTag("body"))
                }
            }
        }

        rule.onNodeWithTag("body").performTouchInput { doubleClick() }

        rule.onNodeWithText("23a").assertExists()
    }

    @Test
    fun `settings mock without group code shows no inspector dialog`() {
        rule.setContent {
            ElementInspectorHost(enabled = true) {
                SettingsMock(R.string.sc_set_list_cap) {
                    Text("body", modifier = Modifier.testTag("body"))
                }
            }
        }

        rule.onNodeWithTag("body").performTouchInput { doubleClick() }

        rule.onNodeWithText("Element id").assertDoesNotExist()
    }

    @Test
    fun `lede shows eyebrow title and description`() {
        rule.setContent { ShowcaseLede() }

        rule.onNodeWithText("Complete token & component set").assertExists()
        rule.onNodeWithText("WaffleHQ · Design system v2.1 · Showcase").assertExists()
    }

    @Test
    fun `theme toggle reports the chosen mode`() {
        var dark by mutableStateOf(false)
        val events = mutableListOf<Boolean>()
        rule.setContent {
            ShowcaseThemeToggle(dark = dark, onToggle = { events += it; dark = it })
        }

        rule.onNodeWithText("Dark").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Light").performClick()
        rule.waitForIdle()

        assertEquals(listOf(true, false), events)
    }

    @Test
    fun `theme toggle shows download action`() {
        rule.setContent { ShowcaseThemeToggle(dark = true, onToggle = {}) }

        rule.onNodeWithText("Download theme").assertExists()
    }
}
