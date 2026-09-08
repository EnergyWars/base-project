package com.wafflehq.uikit.components

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.uikit.theme.AppRole
import com.wafflehq.uikit.theme.AppTheme
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
class AppButtonTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `every variant renders its text and invokes onClick`() {
        val clicked = mutableMapOf<ButtonVariant, Boolean>()
        rule.setContent {
            AppTheme {
                Column {
                    ButtonVariant.entries.forEach { variant ->
                        AppButton(
                            text = "Go $variant",
                            role = AppRole.Primary,
                            variant = variant,
                            onClick = { clicked[variant] = true },
                        )
                    }
                }
            }
        }
        ButtonVariant.entries.forEach { variant ->
            rule.onNodeWithText("Go $variant").performClick()
            assertTrue("variant $variant should invoke onClick", clicked[variant] == true)
        }
    }

    @Test
    fun `disabled button is not enabled`() {
        rule.setContent {
            AppTheme {
                AppButton(text = "Disabled", role = AppRole.Error, onClick = {}, enabled = false)
            }
        }
        rule.onNodeWithText("Disabled").assertIsNotEnabled()
    }

    @Test
    fun `renders for every role`() {
        rule.setContent {
            AppTheme {
                Column {
                    AppRole.entries.forEach { role ->
                        AppButton(text = "Role $role", role = role, onClick = {})
                    }
                }
            }
        }
        AppRole.entries.forEach { role ->
            rule.onNodeWithText("Role $role").assertExists()
        }
    }
}
