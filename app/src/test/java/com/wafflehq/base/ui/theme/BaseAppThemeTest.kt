package com.wafflehq.base.ui.theme

import android.app.Application
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.wafflehq.base.data.model.ThemeMode
import com.wafflehq.base.domain.colortheme.tokens.GlobalColorTokens
import com.wafflehq.base.domain.colortheme.tokens.SuccessColorTokens
import com.wafflehq.base.testutil.FakePreferencesDataStore
import com.wafflehq.lib.settings.colors.ColorOverrideStore
import com.wafflehq.lib.settings.colors.ColorRamp
import com.wafflehq.lib.settings.colors.ColorValue
import com.wafflehq.lib.settings.colors.Emerald40
import com.wafflehq.lib.settings.colors.Emerald80
import com.wafflehq.lib.settings.colors.LocalIsDarkTheme
import com.wafflehq.lib.settings.colors.Sapphire40
import com.wafflehq.lib.settings.colors.Sapphire80
import com.wafflehq.lib.uicore.button.AppButtonWarningColors
import com.wafflehq.lib.uicore.button.LocalAppButtonWarningColors
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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
class BaseAppThemeTest {

    @get:Rule
    val rule = createComposeRule()

    private var colors: AppColors? = null
    private var scheme: ColorScheme? = null
    private var dark: Boolean? = null
    private var warning: AppButtonWarningColors? = null
    private var primaryToken: Color? = null
    private var successToken: Color? = null

    @Composable
    private fun capture() {
        colors = AppTheme.colors
        scheme = MaterialTheme.colorScheme
        dark = LocalIsDarkTheme.current
    }

    @Test
    fun `light mode provides light role colors and material scheme`() {
        rule.setContent { BaseAppTheme(themeMode = ThemeMode.LIGHT) { capture() } }
        rule.waitForIdle()

        assertFalse(dark!!)
        assertEquals(Sapphire40, colors!!.primary.accent)
        assertEquals(Emerald40, colors!!.success.accent)
        assertEquals(Sapphire40, scheme!!.primary)
        assertEquals(colors!!.background, scheme!!.background)
    }

    @Test
    fun `dark mode provides dark role colors`() {
        rule.setContent { BaseAppTheme(themeMode = ThemeMode.DARK) { capture() } }
        rule.waitForIdle()

        assertTrue(dark!!)
        assertEquals(Sapphire80, colors!!.primary.accent)
        assertEquals(Emerald80, colors!!.success.accent)
    }

    @Test
    fun `system mode follows the system setting`() {
        rule.setContent { BaseAppTheme(themeMode = ThemeMode.SYSTEM) { capture() } }
        rule.waitForIdle()

        assertFalse(dark!!)
        assertEquals(Sapphire40, colors!!.primary.accent)
    }

    @Test
    fun `color overrides replace the resolved token for the active mode only`() {
        val custom = ColorValue.Custom(0xFF123456.toInt())
        rule.setContent {
            BaseAppTheme(
                themeMode = ThemeMode.LIGHT,
                colorOverridesLight = mapOf(GlobalColorTokens.primary to custom),
                colorOverridesDark = mapOf(GlobalColorTokens.primary to ColorValue.Custom(0xFF654321.toInt()))
            ) { capture() }
        }
        rule.waitForIdle()

        assertEquals(Color(0xFF123456), colors!!.primary.accent)
        assertEquals(Color(0xFF123456), scheme!!.primary)
    }

    @Test
    fun `base ramp overrides recolor ramp based defaults`() {
        rule.setContent {
            BaseAppTheme(
                themeMode = ThemeMode.LIGHT,
                baseRampOverrides = mapOf(ColorRamp.SAPPHIRE to Color(0xFF8B1A1A))
            ) { capture() }
        }
        rule.waitForIdle()

        assertNotEquals(Sapphire40, colors!!.primary.accent)
        assertEquals(Emerald40, colors!!.success.accent)
    }

    @Test
    fun `button warning colors come from the warning tokens`() {
        rule.setContent {
            BaseAppTheme(themeMode = ThemeMode.LIGHT) {
                warning = LocalAppButtonWarningColors.current
                capture()
            }
        }
        rule.waitForIdle()

        assertEquals(colors!!.warning.accent, warning!!.warning)
        assertEquals(colors!!.warning.onAccent, warning!!.onWarning)
        assertEquals(colors!!.warning.container, warning!!.warningContainer)
        assertEquals(colors!!.warning.onContainer, warning!!.onWarningContainer)
    }

    @Test
    fun `color token accessor resolves inside the theme`() {
        rule.setContent {
            BaseAppTheme(themeMode = ThemeMode.DARK) {
                primaryToken = colorToken(GlobalColorTokens.primary)
                successToken = colorToken(SuccessColorTokens.success)
            }
        }
        rule.waitForIdle()

        assertEquals(Sapphire80, primaryToken)
        assertEquals(Emerald80, successToken)
    }

    @Test
    fun `color token accessor falls back to the catalog defaults outside the theme`() {
        rule.setContent {
            primaryToken = colorToken(GlobalColorTokens.primary)
            successToken = colorToken(SuccessColorTokens.success)
        }
        rule.waitForIdle()

        assertEquals(Sapphire40, primaryToken)
        assertEquals(Emerald40, successToken)
    }

    @Test
    fun `app colors fall back to light defaults outside the theme`() {
        rule.setContent { colors = AppTheme.colors }
        rule.waitForIdle()

        assertEquals(Sapphire40, colors!!.primary.accent)
        assertEquals(Emerald40, colors!!.success.accent)
    }

    @Test
    fun `themed content applies stored overrides and the stored theme mode`() {
        val store = ColorOverrideStore(FakePreferencesDataStore())
        runBlocking {
            store.createTheme("Test")
            store.confirmPending()
            store.setOverride(GlobalColorTokens.primary, isDark = true, value = ColorValue.Custom(0xFF123456.toInt()))
            store.confirmPending()
        }
        rule.setContent {
            AppThemedContent(
                colorPrefs = store,
                themeModeFlow = flowOf(ThemeMode.DARK),
                initialThemeMode = ThemeMode.LIGHT
            ) { capture() }
        }
        rule.waitUntil(5_000) { colors?.primary?.accent == Color(0xFF123456) }

        assertTrue(dark!!)
        assertEquals(Color(0xFF123456), scheme!!.primary)
    }

    @Test
    fun `themed content starts with the initial theme mode`() {
        val store = ColorOverrideStore(FakePreferencesDataStore())
        rule.setContent {
            AppThemedContent(
                colorPrefs = store,
                themeModeFlow = emptyFlow(),
                initialThemeMode = ThemeMode.DARK
            ) { capture() }
        }
        rule.waitForIdle()

        assertTrue(dark!!)
        assertEquals(Sapphire80, colors!!.primary.accent)
    }
}
