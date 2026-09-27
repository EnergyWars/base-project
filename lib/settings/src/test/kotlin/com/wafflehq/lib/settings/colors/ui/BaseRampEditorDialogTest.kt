package com.wafflehq.lib.settings.colors.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.uicore.R as UiCoreR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
class BaseRampEditorDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private fun str(id: Int) = context.getString(id)

    private val defaultSeed = Color(0xFF0E3D6E)

    @Test
    fun `preview strip is shown for the current seed`() {
        rule.setContent {
            BaseRampEditorDialog(
                title = "Saphir",
                currentValue = null,
                defaultSeed = defaultSeed,
                onValueSelected = {},
                onReset = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithTag(BaseRampEditorTestTag.PREVIEW_STRIP).assertExists()
    }

    @Test
    fun `contrast badge against the automatic text color is shown for the current seed`() {
        rule.setContent {
            BaseRampEditorDialog(
                title = "Saphir",
                currentValue = null,
                defaultSeed = defaultSeed,
                onValueSelected = {},
                onReset = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithTag(ColorContrastBadgeTestTag.ROOT).assertExists()
    }

    @Test
    fun `tapping cancel dismisses without emitting any value`() {
        var dismissed = false
        var selected: Color? = null
        rule.setContent {
            BaseRampEditorDialog(
                title = "Saphir",
                currentValue = null,
                defaultSeed = defaultSeed,
                onValueSelected = { selected = it },
                onReset = {},
                onDismiss = { dismissed = true }
            )
        }

        rule.onNodeWithText(str(UiCoreR.string.uicore_cancel)).performClick()

        assertTrue(dismissed)
        assertNull(selected)
    }

    @Test
    fun `saving without picking a new value and no prior override resets to default`() {
        var resetCalled = false
        var selected: Color? = null
        rule.setContent {
            BaseRampEditorDialog(
                title = "Saphir",
                currentValue = null,
                defaultSeed = defaultSeed,
                onValueSelected = { selected = it },
                onReset = { resetCalled = true },
                onDismiss = {}
            )
        }

        rule.onNodeWithTag(BaseRampEditorTestTag.SAVE_BUTTON).performClick()

        assertTrue(resetCalled)
        assertNull(selected)
    }

    @Test
    fun `picking a custom hex color and saving emits exactly that color`() {
        var selected: Color? = null
        rule.setContent {
            BaseRampEditorDialog(
                title = "Saphir",
                currentValue = null,
                defaultSeed = defaultSeed,
                onValueSelected = { selected = it },
                onReset = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithText(str(R.string.appsettings_color_picker_tab_custom)).performClick()
        rule.onNodeWithText(str(R.string.appsettings_color_picker_mode_hex)).performClick()
        rule.onNodeWithText(str(R.string.appsettings_color_picker_hex_label)).performTextReplacement("#FF224466")
        rule.onNodeWithTag(BaseRampEditorTestTag.SAVE_BUTTON).performClick()

        assertEquals(Color(0xFF224466), selected)
    }

    @Test
    fun `tapping the before swatch after picking a new value reverts the pending pick`() {
        var selected: Color? = null
        var resetCalled = false
        val currentValue = Color(0xFF111111)
        rule.setContent {
            BaseRampEditorDialog(
                title = "Saphir",
                currentValue = currentValue,
                defaultSeed = defaultSeed,
                onValueSelected = { selected = it },
                onReset = { resetCalled = true },
                onDismiss = {}
            )
        }

        rule.onNodeWithText(str(R.string.appsettings_color_picker_tab_custom)).performClick()
        rule.onNodeWithText(str(R.string.appsettings_color_picker_mode_hex)).performClick()
        rule.onNodeWithText(str(R.string.appsettings_color_picker_hex_label)).performTextReplacement("#FF224466")
        rule.onNodeWithTag(BaseRampEditorTestTag.BEFORE_SWATCH).performClick()
        rule.onNodeWithTag(BaseRampEditorTestTag.SAVE_BUTTON).performClick()

        assertEquals(currentValue, selected)
        assertFalse(resetCalled)
    }
}
