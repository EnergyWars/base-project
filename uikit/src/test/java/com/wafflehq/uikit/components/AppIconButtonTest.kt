package com.wafflehq.uikit.components

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
class AppIconButtonTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun allVariantsRenderAndFireClick() {
        val clicked = mutableMapOf<IconButtonVariant, Boolean>()
        rule.setContent {
            AppTheme {
                Column {
                    IconButtonVariant.entries.forEach { variant ->
                        AppIconButton(
                            icon = Icons.Outlined.Settings,
                            contentDescription = "cd-$variant",
                            role = AppRole.Warning,
                            variant = variant,
                            onClick = { clicked[variant] = true },
                        )
                    }
                }
            }
        }
        IconButtonVariant.entries.forEach { variant ->
            rule.onNodeWithContentDescription("cd-$variant").performClick()
            assertTrue("variant $variant should fire onClick", clicked[variant] == true)
        }
    }

    @Test
    fun disabledIconButtonIsNotEnabled() {
        rule.setContent {
            AppTheme {
                AppIconButton(
                    icon = Icons.Outlined.Settings,
                    contentDescription = "disabled-icon",
                    role = AppRole.Primary,
                    onClick = {},
                    enabled = false,
                )
            }
        }
        rule.onNodeWithContentDescription("disabled-icon").assertIsNotEnabled()
    }
}
