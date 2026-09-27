package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppSectionCardTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `renders title and content`() {
        rule.setContent {
            MaterialTheme {
                AppSectionCard(title = "Ingredients", icon = Icons.Default.Inbox) { Text("Flour") }
            }
        }

        rule.onNodeWithText("Ingredients").assertIsDisplayed()
        rule.onNodeWithText("Flour").assertIsDisplayed()
    }

    @Test
    fun `renders content without a title`() {
        rule.setContent { MaterialTheme { AppSectionCard { Text("Only body") } } }

        rule.onNodeWithText("Only body").assertIsDisplayed()
    }

    @Test
    fun `color overload renders content`() {
        rule.setContent {
            MaterialTheme {
                AppSectionCard(
                    title = "Colored",
                    containerColor = androidx.compose.ui.graphics.Color.LightGray,
                    borderColor = androidx.compose.ui.graphics.Color.DarkGray,
                ) { Text("Body") }
            }
        }

        rule.onNodeWithText("Colored").assertIsDisplayed()
        rule.onNodeWithText("Body").assertIsDisplayed()
    }
}
