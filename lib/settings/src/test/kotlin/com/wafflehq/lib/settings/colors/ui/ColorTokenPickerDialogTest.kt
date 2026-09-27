package com.wafflehq.lib.settings.colors.ui

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.colors.ColorTokenId
import com.wafflehq.lib.settings.colors.ColorValue
import com.wafflehq.lib.settings.colors.TestColorTokens
import com.wafflehq.lib.uicore.R as UiCoreR
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ColorTokenPickerDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private fun str(id: Int) = context.getString(id)

    private val registry = TestColorTokens.registry
    private val resolved: Map<ColorTokenId, Color> = registry.all.associate { it.id to it.defaultLight }

    @Test
    fun `contrast badge is shown for a token with a registered onAccent counterpart`() {
        rule.setContent {
            ColorTokenPickerDialog(
                title = "Text auf Akzent",
                token = registry.byId.getValue(TestColorTokens.alphaOnAccent),
                registry = registry,
                resolvedAll = resolved,
                currentValue = ColorValue.Custom(Color.White.toArgb()),
                defaultPreview = Color.White,
                onValueSelected = {},
                onReset = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithTag(ColorContrastBadgeTestTag.ROOT).assertExists()
    }

    @Test
    fun `black text on a white background shown against a real counterpart is classified AAA`() {
        rule.setContent {
            ColorTokenPickerDialog(
                title = "Text auf Akzent",
                token = registry.byId.getValue(TestColorTokens.alphaOnAccent),
                registry = registry,
                resolvedAll = resolved + (TestColorTokens.alphaAccent to Color.White),
                currentValue = ColorValue.Custom(Color.Black.toArgb()),
                defaultPreview = Color.Black,
                onValueSelected = {},
                onReset = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithText(str(R.string.appsettings_color_picker_contrast_level_aaa), substring = true).assertExists()
    }

    @Test
    fun `two nearly identical grays shown against each other are classified as low contrast`() {
        rule.setContent {
            ColorTokenPickerDialog(
                title = "Text auf Akzent",
                token = registry.byId.getValue(TestColorTokens.alphaOnAccent),
                registry = registry,
                resolvedAll = resolved + (TestColorTokens.alphaAccent to Color(0xFF808080)),
                currentValue = ColorValue.Custom(Color(0xFF8A8A8A).toArgb()),
                defaultPreview = Color(0xFF8A8A8A),
                onValueSelected = {},
                onReset = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithText(str(R.string.appsettings_color_picker_contrast_level_low), substring = true).assertExists()
    }

    @Test
    fun `a token without a registered counterpart falls back to the automatic text color badge`() {
        rule.setContent {
            ColorTokenPickerDialog(
                title = "Beta",
                token = registry.byId.getValue(TestColorTokens.betaAccent),
                registry = registry,
                resolvedAll = resolved,
                currentValue = null,
                defaultPreview = Color.White,
                onValueSelected = {},
                onReset = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithTag(ColorContrastBadgeTestTag.ROOT).assertExists()
        rule.onNodeWithText(
            str(R.string.appsettings_color_picker_contrast_against_text).substringBefore('%').trim(),
            substring = true
        ).assertExists()
    }

    @Test
    fun `the short id of the edited token is shown in the title bar`() {
        rule.setContent {
            ColorTokenPickerDialog(
                title = "Beta",
                token = registry.byId.getValue(TestColorTokens.betaAccent),
                registry = registry,
                resolvedAll = resolved,
                currentValue = null,
                defaultPreview = Color.White,
                onValueSelected = {},
                onReset = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithText(registry.shortId(TestColorTokens.betaAccent)).assertExists()
    }

    @Test
    fun `the preview slot gets the edited token and its live color`() {
        var slotToken: ColorTokenId? = null
        var slotLive: Color? = null
        rule.setContent {
            ColorTokenPickerDialog(
                title = "Beta",
                token = registry.byId.getValue(TestColorTokens.betaAccent),
                registry = registry,
                resolvedAll = resolved,
                currentValue = ColorValue.Custom(Color(0xFF224466).toArgb()),
                defaultPreview = Color.White,
                onValueSelected = {},
                onReset = {},
                onDismiss = {},
                preview = { token, live, _ ->
                    slotToken = token.id
                    slotLive = live
                    Text("preview slot")
                }
            )
        }

        rule.onNodeWithText("preview slot").assertExists()
        assertEquals(TestColorTokens.betaAccent, slotToken)
        assertEquals(Color(0xFF224466), slotLive)
    }

    @Test
    fun `no preview slot renders nothing extra and the dialog still works`() {
        var reset = false
        rule.setContent {
            ColorTokenPickerDialog(
                title = "Beta",
                token = registry.byId.getValue(TestColorTokens.betaAccent),
                registry = registry,
                resolvedAll = resolved,
                currentValue = null,
                defaultPreview = Color.White,
                onValueSelected = {},
                onReset = { reset = true },
                onDismiss = {}
            )
        }

        rule.onNodeWithText(str(R.string.appsettings_color_picker_preview_label)).assertExists()
        rule.onNodeWithText(str(UiCoreR.string.uicore_save)).performClick()

        assertEquals(true, reset)
    }
}
