package com.wafflehq.lib.settings.colors

import android.app.Application
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class ColorTokenThemeTest {

    @get:Rule
    val rule = createComposeRule()

    private val registry = TestColorTokens.registry

    @Test
    fun `resolves every token of the registry`() {
        var resolved: Map<ColorTokenId, Color>? = null

        rule.setContent {
            ColorTokenTheme(registry, emptyMap(), emptyMap(), isDark = false) { resolved = it }
        }

        rule.runOnIdle {
            assertEquals(registry.all.size, resolved?.size)
            assertEquals(Sapphire40, resolved?.get(TestColorTokens.alphaAccent))
        }
    }

    @Test
    fun `dark mode resolves the dark defaults`() {
        var resolved: Map<ColorTokenId, Color>? = null

        rule.setContent {
            ColorTokenTheme(registry, emptyMap(), emptyMap(), isDark = true) { resolved = it }
        }

        rule.runOnIdle { assertEquals(Sapphire80, resolved?.get(TestColorTokens.alphaAccent)) }
    }

    @Test
    fun `an override wins over the default`() {
        val overrides = mapOf(TestColorTokens.alphaAccent to ColorValue.Custom(Color(0xFF123456).toArgb()))
        var custom: Color? = null

        rule.setContent {
            ColorTokenTheme(registry, overrides, emptyMap(), isDark = false) {
                custom = colorToken(TestColorTokens.alphaAccent)
            }
        }

        rule.runOnIdle { assertEquals(Color(0xFF123456), custom) }
    }

    @Test
    fun `a base ramp override moves a ramp-derived default`() {
        val seed = Color(0xFF204080)
        var shifted: Color? = null

        rule.setContent {
            ColorTokenTheme(registry, emptyMap(), mapOf(ColorRamp.SAPPHIRE to seed), isDark = false) {
                shifted = colorToken(TestColorTokens.alphaAccent)
            }
        }

        rule.runOnIdle {
            assertEquals(ColorRampTable.generatedRamp(seed).getValue(40), shifted)
        }
    }

    @Test
    fun `colorToken falls back to the token default outside a provided theme`() {
        var fallback: Color? = null

        rule.setContent {
            CompositionLocalProvider(LocalColorTokenRegistry provides registry) {
                fallback = colorToken(TestColorTokens.betaAccent)
            }
        }

        rule.runOnIdle { assertEquals(Garnet40, fallback) }
    }

    @Test
    fun `colorToken reports an unknown token instead of returning a wrong color`() {
        assertThrows(IllegalStateException::class.java) {
            rule.setContent {
                CompositionLocalProvider(LocalColorTokenRegistry provides registry) {
                    colorToken(ColorTokenId("nope.missing"))
                }
            }
            rule.waitForIdle()
        }
    }

    @Test
    fun `registry local defaults to null`() {
        var current: ColorTokenRegistry? = registry

        rule.setContent { current = LocalColorTokenRegistry.current }

        rule.runOnIdle { assertNull(current) }
    }

    @Test
    fun `rememberMappedColorScheme applies the mapping`() {
        val mapping = MaterialRoleMapping(
            primary = TestColorTokens.alphaAccent,
            onPrimary = TestColorTokens.alphaOnAccent,
            primaryContainer = TestColorTokens.alphaAccent,
            onPrimaryContainer = TestColorTokens.alphaOnAccent,
            secondary = TestColorTokens.betaAccent,
            onSecondary = TestColorTokens.alphaOnAccent,
            secondaryContainer = TestColorTokens.betaAccent,
            onSecondaryContainer = TestColorTokens.alphaOnAccent,
            tertiary = TestColorTokens.betaAccent,
            onTertiary = TestColorTokens.alphaOnAccent,
            tertiaryContainer = TestColorTokens.betaAccent,
            onTertiaryContainer = TestColorTokens.alphaOnAccent,
            error = TestColorTokens.betaAccent,
            onError = TestColorTokens.alphaOnAccent,
            errorContainer = TestColorTokens.betaAccent,
            onErrorContainer = TestColorTokens.alphaOnAccent,
            background = TestColorTokens.alphaAccent,
            onBackground = TestColorTokens.alphaOnAccent,
            outline = TestColorTokens.betaAccent
        )
        var primary: Color? = null

        rule.setContent {
            ColorTokenTheme(registry, emptyMap(), emptyMap(), isDark = false) { resolved ->
                primary = rememberMappedColorScheme(lightColorScheme(), resolved, mapping).primary
            }
        }

        rule.runOnIdle { assertEquals(Sapphire40, primary) }
    }
}
