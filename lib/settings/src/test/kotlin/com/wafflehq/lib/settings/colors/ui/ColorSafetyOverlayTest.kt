package com.wafflehq.lib.settings.colors.ui

import android.app.Application
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.colors.ColorOverrideStore
import com.wafflehq.lib.settings.colors.ColorSafetyPending
import com.wafflehq.lib.settings.colors.ColorThemeLibrary
import com.wafflehq.lib.settings.colors.ColorValue
import com.wafflehq.lib.settings.colors.FakePreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ColorSafetyOverlayTest {

    @get:Rule
    val rule = createComposeRule()

    private val context: android.content.Context = ApplicationProvider.getApplicationContext()
    private val token = com.wafflehq.lib.settings.colors.ColorTokenId("test.tokenA")

    @Test
    fun `the banner shows the remaining seconds rounded up`() {
        val pending = ColorSafetyPending(ColorThemeLibrary(), deadlineMillis = 12_400L)
        rule.setContent { ColorSafetyBanner(pending, clock = { 0L }, onKeep = {}, onRevert = {}) }

        rule.onNodeWithTag(ColorSafetyTestTags.BANNER).assertExists()
        rule.onNodeWithTag(ColorSafetyTestTags.COUNTDOWN)
            .assertTextEquals(context.getString(R.string.appsettings_color_safety_message, 13))
    }

    @Test
    fun `keep and revert forward to their callbacks`() {
        var keep = 0
        var revert = 0
        val pending = ColorSafetyPending(ColorThemeLibrary(), deadlineMillis = 10_000L)
        rule.setContent { ColorSafetyBanner(pending, clock = { 0L }, onKeep = { keep++ }, onRevert = { revert++ }) }

        rule.onNodeWithTag(ColorSafetyTestTags.KEEP).performClick()
        rule.onNodeWithTag(ColorSafetyTestTags.REVERT).performClick()

        assertEquals(1, keep)
        assertEquals(1, revert)
    }

    @Test
    fun `no banner is shown without an open confirmation window`() {
        val store = ColorOverrideStore(FakePreferencesDataStore())
        rule.setContent { ColorSafetyOverlay(store) }

        rule.onNodeWithTag(ColorSafetyTestTags.BANNER).assertDoesNotExist()
    }

    @Test
    fun `an unconfirmed edit shows the banner and keeping it closes the window`() {
        val store = ColorOverrideStore(FakePreferencesDataStore())
        runBlocking {
            store.createTheme("A")
            store.setOverride(token, false, ColorValue.Custom(1))
        }
        rule.setContent { ColorSafetyOverlay(store) }
        rule.waitForIdle()
        rule.onNodeWithTag(ColorSafetyTestTags.BANNER).assertExists()

        rule.onNodeWithTag(ColorSafetyTestTags.KEEP).performClick()

        rule.waitForIdle()
        rule.onNodeWithTag(ColorSafetyTestTags.BANNER).assertDoesNotExist()
        assertEquals(ColorValue.Custom(1), runBlocking { store.overridesFlow(false).first() }[token])
    }

    @Test
    fun `reverting from the banner restores the previous colors`() {
        val store = ColorOverrideStore(FakePreferencesDataStore())
        runBlocking {
            store.createTheme("A")
            store.confirmPending()
            store.setOverride(token, false, ColorValue.Custom(1))
        }
        rule.setContent { ColorSafetyOverlay(store) }
        rule.waitForIdle()

        rule.onNodeWithTag(ColorSafetyTestTags.REVERT).performClick()

        rule.waitForIdle()
        assertNull(runBlocking { store.overridesFlow(false).first() }[token])
        rule.onNodeWithTag(ColorSafetyTestTags.BANNER).assertDoesNotExist()
    }

    @Test
    fun `an expired window is reverted as soon as the overlay is shown`() {
        var now = 0L
        val store = ColorOverrideStore(FakePreferencesDataStore(), clock = { now }, confirmWindowMillis = 1_000)
        runBlocking {
            store.createTheme("A")
            store.confirmPending()
            store.setOverride(token, false, ColorValue.Custom(1))
        }
        now = 60_000

        rule.setContent { ColorSafetyOverlay(store, clock = { now }) }
        rule.waitForIdle()

        assertNull(runBlocking { store.overridesFlow(false).first() }[token])
    }
}
