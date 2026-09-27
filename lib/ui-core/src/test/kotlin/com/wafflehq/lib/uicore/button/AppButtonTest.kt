package com.wafflehq.lib.uicore.button

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
        val clicked = mutableMapOf<AppButtonVariant, Boolean>()
        rule.setContent {
            MaterialTheme {
                Column {
                    AppButtonVariant.entries.forEach { variant ->
                        AppButton(
                            text = "Go $variant",
                            role = AppButtonRole.Primary,
                            variant = variant,
                            onClick = { clicked[variant] = true },
                        )
                    }
                }
            }
        }
        AppButtonVariant.entries.forEach { variant ->
            rule.onNodeWithText("Go $variant").performClick()
            assertTrue("variant $variant should invoke onClick", clicked[variant] == true)
        }
    }

    @Test
    fun `disabled button is not enabled`() {
        rule.setContent {
            MaterialTheme {
                AppButton(text = "Disabled", role = AppButtonRole.Error, onClick = {}, enabled = false)
            }
        }
        rule.onNodeWithText("Disabled").assertIsNotEnabled()
    }

    @Test
    fun `renders for every role`() {
        rule.setContent {
            MaterialTheme {
                Column {
                    AppButtonRole.entries.forEach { role ->
                        AppButton(text = "Role $role", role = role, onClick = {})
                    }
                }
            }
        }
        AppButtonRole.entries.forEach { role ->
            rule.onNodeWithText("Role $role").assertExists()
        }
    }

    @Test
    fun `trailing icon variant still renders text`() {
        rule.setContent {
            MaterialTheme {
                AppButton(
                    text = "Sort",
                    role = AppButtonRole.Primary,
                    onClick = {},
                    trailingIcon = Icons.Filled.Add,
                )
            }
        }
        rule.onNodeWithText("Sort").assertIsDisplayed()
    }

    @Test
    fun `custom color overload renders text and invokes onClick`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppButton(
                    text = "Custom",
                    containerColor = androidx.compose.ui.graphics.Color.Magenta,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    onClick = { clicks++ },
                )
            }
        }
        rule.onNodeWithText("Custom").performClick()
        assertTrue(clicks == 1)
    }

    @Test
    fun `custom content padding and single line still render text and invoke onClick`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppButton(
                    text = "Compact",
                    role = AppButtonRole.Primary,
                    onClick = { clicks++ },
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    singleLine = true,
                )
            }
        }
        rule.onNodeWithText("Compact").assertIsDisplayed().performClick()
        assertTrue(clicks == 1)
    }

    @Test
    fun `unobscured click on an Error role button still invokes onClick`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppButton(text = "Delete", role = AppButtonRole.Error, onClick = { clicks++ })
            }
        }
        rule.onNodeWithText("Delete").performClick()
        assertTrue(clicks == 1)
    }

    @Test
    fun `leading icon variant still renders text`() {
        rule.setContent {
            MaterialTheme {
                AppButton(
                    text = "Save",
                    role = AppButtonRole.Primary,
                    onClick = {},
                    leadingIcon = Icons.Filled.Add,
                )
            }
        }
        rule.onNodeWithText("Save").assertIsDisplayed()
    }

    @Test
    fun `slot overload renders free content and invokes onClick for every variant`() {
        val clicked = mutableMapOf<AppButtonVariant, Boolean>()
        rule.setContent {
            MaterialTheme {
                Column {
                    AppButtonVariant.entries.forEach { variant ->
                        AppButton(
                            role = AppButtonRole.Primary,
                            variant = variant,
                            onClick = { clicked[variant] = true },
                        ) {
                            Column { Text("Slot $variant"); Text("second line") }
                        }
                    }
                }
            }
        }
        AppButtonVariant.entries.forEach { variant ->
            rule.onNodeWithText("Slot $variant").performClick()
            assertTrue("variant $variant should invoke onClick", clicked[variant] == true)
        }
    }

    @Test
    fun `disabled slot button is not enabled`() {
        rule.setContent {
            MaterialTheme {
                AppButton(role = AppButtonRole.Primary, onClick = {}, enabled = false) { Text("Slot off") }
            }
        }
        rule.onNodeWithText("Slot off").assertIsNotEnabled()
    }

    @Test
    fun `custom color slot overload renders content and invokes onClick`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppButton(
                    containerColor = androidx.compose.ui.graphics.Color.Magenta,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    onClick = { clicks++ },
                ) { Text("Slot custom") }
            }
        }
        rule.onNodeWithText("Slot custom").performClick()
        assertTrue(clicks == 1)
    }
}
