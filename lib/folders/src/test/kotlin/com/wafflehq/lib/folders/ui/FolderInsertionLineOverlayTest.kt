package com.wafflehq.lib.folders.ui

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FolderInsertionLineOverlayTest {

    @get:Rule
    val rule = createComposeRule()

    private fun assertRowStaysDisplayed(above: Boolean?) {
        rule.setContent {
            Box(Modifier.size(80.dp).testTag("row").folderInsertionLineOverlay(above))
        }
        rule.onNodeWithTag("row").assertIsDisplayed()
    }

    @Test
    fun noInsertion_rowIsDisplayed() = assertRowStaysDisplayed(null)

    @Test
    fun insertionAbove_rowIsDisplayed() = assertRowStaysDisplayed(true)

    @Test
    fun insertionBelow_rowIsDisplayed() = assertRowStaysDisplayed(false)
}
