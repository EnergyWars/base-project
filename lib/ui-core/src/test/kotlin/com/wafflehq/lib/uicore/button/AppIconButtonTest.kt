package com.wafflehq.lib.uicore.button

import android.app.Application
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
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
    fun `every variant invokes onClick`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                Column {
                    AppIconButtonVariant.entries.forEach { variant ->
                        AppIconButton(
                            icon = Icons.Filled.Delete,
                            contentDescription = "delete $variant",
                            role = AppButtonRole.Error,
                            variant = variant,
                            onClick = { clicks++ },
                        )
                    }
                }
            }
        }
        AppIconButtonVariant.entries.forEach { variant ->
            rule.onNodeWithContentDescription("delete $variant").performClick()
        }
        assertEquals(AppIconButtonVariant.entries.size, clicks)
    }

    @Test
    fun `disabled icon button is not enabled`() {
        rule.setContent {
            MaterialTheme {
                AppIconButton(
                    icon = Icons.Filled.Delete,
                    contentDescription = "delete",
                    role = AppButtonRole.Error,
                    onClick = {},
                    enabled = false,
                )
            }
        }
        rule.onNodeWithContentDescription("delete").assertIsNotEnabled()
    }

    @Test
    fun `renders for every role`() {
        rule.setContent {
            MaterialTheme {
                Column {
                    AppButtonRole.entries.forEach { role ->
                        AppIconButton(
                            icon = Icons.Filled.Delete,
                            contentDescription = "role $role",
                            role = role,
                            onClick = {},
                        )
                    }
                }
            }
        }
        assertEquals(
            AppButtonRole.entries.size,
            rule.onAllNodesWithContentDescription("role", substring = true).fetchSemanticsNodes().size,
        )
    }

    @Test
    fun `custom shape and size still invoke onClick`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppIconButton(
                    icon = Icons.Filled.Delete,
                    contentDescription = "call action",
                    role = AppButtonRole.Error,
                    variant = AppIconButtonVariant.Filled,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp),
                    onClick = { clicks++ },
                )
            }
        }
        rule.onNodeWithContentDescription("call action").assertIsDisplayed().performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `a supplied interaction source records the press`() {
        val source = MutableInteractionSource()
        val interactions = mutableListOf<Interaction>()
        rule.setContent {
            MaterialTheme {
                LaunchedEffect(source) { source.interactions.collect { interactions.add(it) } }
                AppIconButton(
                    icon = Icons.Filled.Delete,
                    contentDescription = "repeating",
                    role = AppButtonRole.Neutral,
                    onClick = {},
                    interactionSource = source,
                )
            }
        }
        rule.onNodeWithContentDescription("repeating").performClick()
        rule.waitForIdle()
        assertEquals(true, interactions.any { it is PressInteraction.Press })
    }
}
