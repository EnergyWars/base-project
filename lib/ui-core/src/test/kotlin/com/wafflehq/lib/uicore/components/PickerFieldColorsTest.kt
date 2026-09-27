package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.material3.TextFieldColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
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
class PickerFieldColorsTest {

    @get:Rule
    val rule = createComposeRule()

    private val alphaTolerance = 1f / 255f

    @Test
    fun emptyFieldGetsPalerBorderAndLabelThanFilledField() {
        var emptyBorderAlpha = 0f
        var filledBorderAlpha = 0f
        var emptyLabelAlpha = 0f
        var filledLabelAlpha = 0f
        rule.setContent {
            val empty = disabledPickerFieldColors(isEmpty = true, borderColor = Color.Black, labelColor = Color.Black)
            val filled = disabledPickerFieldColors(isEmpty = false, borderColor = Color.Black, labelColor = Color.Black)
            emptyBorderAlpha = empty.disabledIndicatorColor.alpha
            filledBorderAlpha = filled.disabledIndicatorColor.alpha
            emptyLabelAlpha = empty.disabledLabelColor.alpha
            filledLabelAlpha = filled.disabledLabelColor.alpha
        }

        assertEquals(1f, filledBorderAlpha)
        assertEquals(1f, filledLabelAlpha)
        assertEquals(0.5f, emptyBorderAlpha, alphaTolerance)
        assertEquals(0.5f, emptyLabelAlpha, alphaTolerance)
        assertTrue(emptyBorderAlpha < filledBorderAlpha)
    }

    @Test
    fun preservesPartialAlphaOfCustomBorderColorWhenNotEmpty() {
        var alpha = 0f
        rule.setContent {
            alpha = disabledPickerFieldColors(
                isEmpty = false,
                borderColor = Color.Black.copy(alpha = 0.5f)
            ).disabledIndicatorColor.alpha
        }

        assertEquals(0.5f, alpha, alphaTolerance)
    }

    @Test
    fun scalesDownAlreadyTransparentBorderColorWhenEmpty() {
        var alpha = 0f
        rule.setContent {
            alpha = disabledPickerFieldColors(
                isEmpty = true,
                borderColor = Color.Black.copy(alpha = 0.5f)
            ).disabledIndicatorColor.alpha
        }

        assertEquals(0.25f, alpha, alphaTolerance)
    }

    @Test
    fun emptyAwareTextFieldColorsFadesBorderAndLabelWhenEmpty() {
        var emptyBorderAlpha = 0f
        var emptyLabelAlpha = 0f
        rule.setContent {
            val empty = emptyAwareTextFieldColors(isEmpty = true)
            emptyBorderAlpha = empty.unfocusedIndicatorColor.alpha
            emptyLabelAlpha = empty.unfocusedLabelColor.alpha
        }

        assertEquals(0.5f, emptyBorderAlpha, alphaTolerance)
        assertEquals(0.5f, emptyLabelAlpha, alphaTolerance)
    }

    @Test
    fun emptyAwareTextFieldColorsKeepsDefaultsWhenFilled() {
        var filledColors: TextFieldColors? = null
        var defaultColors: TextFieldColors? = null
        rule.setContent {
            filledColors = emptyAwareTextFieldColors(isEmpty = false)
            defaultColors = AppTextFieldDefaults.colors()
        }

        val default = defaultColors!!
        val filled = filledColors!!
        assertEquals(default.unfocusedIndicatorColor, filled.unfocusedIndicatorColor)
        assertEquals(default.unfocusedLabelColor, filled.unfocusedLabelColor)
    }
}
