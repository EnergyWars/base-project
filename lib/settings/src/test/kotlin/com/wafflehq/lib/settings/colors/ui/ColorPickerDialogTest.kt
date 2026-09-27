package com.wafflehq.lib.settings.colors.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.uicore.R as UiCoreR
import org.junit.Assert.assertEquals
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
class ColorPickerDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private fun str(id: Int) = context.getString(id)

    private val initial = Color(0xFF0E3D6E)

    @Test
    fun `both tabs are offered and the palette is shown first`() {
        rule.setContent {
            ColorPickerDialog(title = "Farbe", initialColor = initial, onColorSelected = {}, onDismiss = {})
        }

        rule.onNodeWithText(str(R.string.appsettings_color_picker_tab_palette)).assertExists()
        rule.onNodeWithText(str(R.string.appsettings_color_picker_tab_custom)).assertExists()
        rule.onNodeWithText(str(R.string.appsettings_color_picker_mode_hex)).assertDoesNotExist()
    }

    @Test
    fun `saving without any change emits the initial color unchanged`() {
        var selected: Color? = null
        rule.setContent {
            ColorPickerDialog(title = "Farbe", initialColor = initial, onColorSelected = { selected = it }, onDismiss = {})
        }

        rule.onNodeWithText(str(UiCoreR.string.uicore_save)).performClick()

        assertEquals(initial, selected)
    }

    @Test
    fun `a hex value typed in the custom tab is emitted on save`() {
        var selected: Color? = null
        rule.setContent {
            ColorPickerDialog(title = "Farbe", initialColor = initial, onColorSelected = { selected = it }, onDismiss = {})
        }

        rule.onNodeWithText(str(R.string.appsettings_color_picker_tab_custom)).performClick()
        rule.onNodeWithText(str(R.string.appsettings_color_picker_mode_hex)).performClick()
        rule.onNodeWithText(str(R.string.appsettings_color_picker_hex_label)).performTextReplacement("#FF224466")
        rule.onNodeWithText(str(UiCoreR.string.uicore_save)).performClick()

        assertEquals(Color(0xFF224466), selected)
    }

    @Test
    fun `an rgb triple typed in the custom tab is emitted on save`() {
        var selected: Color? = null
        rule.setContent {
            ColorPickerDialog(title = "Farbe", initialColor = initial, onColorSelected = { selected = it }, onDismiss = {})
        }

        rule.onNodeWithText(str(R.string.appsettings_color_picker_tab_custom)).performClick()
        rule.onNodeWithText(str(R.string.appsettings_color_picker_mode_rgb)).performClick()
        rule.onNodeWithText(str(R.string.appsettings_color_picker_rgb_r)).performTextReplacement("34")
        rule.onNodeWithText(str(R.string.appsettings_color_picker_rgb_g)).performTextReplacement("68")
        rule.onNodeWithText(str(R.string.appsettings_color_picker_rgb_b)).performTextReplacement("102")
        rule.onNodeWithText(str(UiCoreR.string.uicore_save)).performClick()

        assertEquals(Color(0xFF224466), selected)
    }

    @Test
    fun `cancel dismisses without emitting a color`() {
        var dismissed = false
        var selected: Color? = null
        rule.setContent {
            ColorPickerDialog(
                title = "Farbe",
                initialColor = initial,
                onColorSelected = { selected = it },
                onDismiss = { dismissed = true }
            )
        }

        rule.onNodeWithText(str(UiCoreR.string.uicore_cancel)).performClick()

        assertTrue(dismissed)
        assertNull(selected)
    }
}
