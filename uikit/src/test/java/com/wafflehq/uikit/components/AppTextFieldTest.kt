package com.wafflehq.uikit.components

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.wafflehq.uikit.theme.AppRole
import com.wafflehq.uikit.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppTextFieldTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun typingUpdatesValueViaOnValueChange() {
        rule.setContent {
            var value by remember { mutableStateOf("") }
            AppTheme {
                AppTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = "Name",
                    role = AppRole.Primary,
                )
            }
        }
        rule.onNodeWithText("Name").performTextInput("hello")
        rule.onNodeWithText("hello").assertExists()
    }

    @Test
    fun disabledFieldIsNotEnabled() {
        rule.setContent {
            AppTheme {
                AppTextField(
                    value = "",
                    onValueChange = {},
                    label = "Disabled field",
                    role = AppRole.Neutral,
                    enabled = false,
                )
            }
        }
        rule.onNodeWithText("Disabled field").assertIsNotEnabled()
    }

    @Test
    fun errorStateRendersWithoutCrashing() {
        rule.setContent {
            AppTheme {
                AppTextField(
                    value = "bad",
                    onValueChange = {},
                    label = "Error field",
                    role = AppRole.Error,
                    isError = true,
                )
            }
        }
        rule.onNodeWithText("bad").assertExists()
    }
}
