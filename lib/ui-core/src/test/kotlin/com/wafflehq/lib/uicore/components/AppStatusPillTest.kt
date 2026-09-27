package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
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
class AppStatusPillTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `pill renders its text`() {
        rule.setContent {
            MaterialTheme {
                AppStatusPill(text = "3 Einträge", containerColor = Color.LightGray, contentColor = Color.Black)
            }
        }
        rule.onNodeWithText("3 Einträge").assertIsDisplayed()
    }

    @Test
    fun `pill with leading icon still renders its text`() {
        rule.setContent {
            MaterialTheme {
                AppStatusPill(
                    text = "Fällig",
                    containerColor = Color.LightGray,
                    contentColor = Color.Black,
                    icon = Icons.Default.DateRange
                )
            }
        }
        rule.onNodeWithText("Fällig").assertIsDisplayed()
    }

    @Test
    fun `long text is limited to a single line without crashing`() {
        rule.setContent {
            MaterialTheme {
                AppStatusPill(
                    text = "Ein sehr langer Text, der in einer einzigen Zeile abgeschnitten werden muss",
                    containerColor = Color.LightGray,
                    contentColor = Color.Black
                )
            }
        }
        rule.onNodeWithText("Ein sehr langer Text, der in einer einzigen Zeile abgeschnitten werden muss").assertExists()
    }

    @Test
    fun `icon size default is 14dp`() {
        assertEquals(14.dp, AppStatusPillDefaults.iconSize)
    }
}
