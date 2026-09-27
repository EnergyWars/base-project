package com.wafflehq.lib.settings.colors.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.wafflehq.lib.settings.colors.ColorRamp
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
class SimplifiedColorSettingsListTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `every ramp gets exactly one row`() {
        rule.setContent {
            SimplifiedColorSettingsList(
                baseRampOverrides = emptyMap(),
                onEditRamp = {},
                onResetRamp = {}
            )
        }

        for (ramp in ColorRamp.entries) {
            rule.onNodeWithTag(SimplifiedColorRowTestTag.row(ramp)).assertExists()
        }
    }

    @Test
    fun `tapping a row invokes onEditRamp with that exact ramp`() {
        var edited: ColorRamp? = null
        rule.setContent {
            SimplifiedColorSettingsList(
                baseRampOverrides = emptyMap(),
                onEditRamp = { edited = it },
                onResetRamp = {}
            )
        }

        rule.onNodeWithTag(SimplifiedColorRowTestTag.row(ColorRamp.GARNET)).performClick()

        assertEquals(ColorRamp.GARNET, edited)
    }

    @Test
    fun `a ramp without an override has no reset button`() {
        rule.setContent {
            SimplifiedColorSettingsList(
                baseRampOverrides = emptyMap(),
                onEditRamp = {},
                onResetRamp = {}
            )
        }

        rule.onNodeWithTag(SimplifiedColorRowTestTag.reset(ColorRamp.SAPPHIRE)).assertDoesNotExist()
    }

    @Test
    fun `a ramp with an override shows a reset button that invokes onResetRamp for that ramp only`() {
        var reset: ColorRamp? = null
        rule.setContent {
            SimplifiedColorSettingsList(
                baseRampOverrides = mapOf(ColorRamp.SAPPHIRE to Color(0xFF224466)),
                onEditRamp = {},
                onResetRamp = { reset = it }
            )
        }

        rule.onNodeWithTag(SimplifiedColorRowTestTag.reset(ColorRamp.SAPPHIRE)).performClick()

        assertEquals(ColorRamp.SAPPHIRE, reset)
    }

    @Test
    fun `tapping the reset button does not also trigger onEditRamp for that row`() {
        var edited: ColorRamp? = null
        rule.setContent {
            SimplifiedColorSettingsList(
                baseRampOverrides = mapOf(ColorRamp.SAPPHIRE to Color(0xFF224466)),
                onEditRamp = { edited = it },
                onResetRamp = {}
            )
        }

        rule.onNodeWithTag(SimplifiedColorRowTestTag.reset(ColorRamp.SAPPHIRE)).performClick()

        assertNull(edited)
    }
}
