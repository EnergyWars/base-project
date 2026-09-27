package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppBarSurfaceTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `renders its content`() {
        rule.setContent { MaterialTheme { AppBarSurface { Text("Bar content") } } }

        rule.onNodeWithText("Bar content").assertIsDisplayed()
    }

    @Test
    fun `elevation token is the shared bar elevation`() {
        assertEquals(3.dp, AppBarSurfaceDefaults.tonalElevation)
    }
}
