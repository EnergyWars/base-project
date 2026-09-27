package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.foundation.shape.RoundedCornerShape
import com.wafflehq.lib.uicore.theme.AppRadius
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppBottomSheetTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `sheet shows its content`() {
        rule.setContent {
            MaterialTheme {
                AppBottomSheet(
                    onDismissRequest = {},
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                ) { Text("Sheet content") }
            }
        }

        rule.onNodeWithText("Sheet content").assertIsDisplayed()
    }

    @Test
    fun `sheet without a drag handle still shows its content`() {
        rule.setContent {
            MaterialTheme {
                AppBottomSheet(
                    onDismissRequest = {},
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    dragHandle = null,
                ) { Text("No handle") }
            }
        }

        rule.onNodeWithText("No handle").assertIsDisplayed()
    }

    @Test
    fun `defaults round only the top corners with the sheet radius`() {
        assertEquals(
            RoundedCornerShape(topStart = AppRadius.sheet, topEnd = AppRadius.sheet),
            AppBottomSheetDefaults.shape,
        )
    }
}
