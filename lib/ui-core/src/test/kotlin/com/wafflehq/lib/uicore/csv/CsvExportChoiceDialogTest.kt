package com.wafflehq.lib.uicore.csv

import android.app.Application
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.uicore.R
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
class CsvExportChoiceDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val calls = mutableListOf<String>()

    private fun setContent(message: String? = null) {
        rule.setContent {
            CsvExportChoiceDialog(
                title = "Export title",
                message = message,
                onDismissRequest = { calls += "dismiss" },
                onPreview = { calls += "preview" },
                onShare = { calls += "share" },
                onSave = { calls += "save" }
            )
        }
    }

    @Test
    fun `title and all actions are displayed`() {
        setContent()

        rule.onNodeWithText("Export title").assertExists()
        rule.onNodeWithText(context.getString(R.string.uicore_csv_export_preview)).assertExists()
        rule.onNodeWithText(context.getString(R.string.uicore_csv_export_share)).assertExists()
        rule.onNodeWithText(context.getString(R.string.uicore_csv_export_save)).assertExists()
        rule.onNodeWithText(context.getString(R.string.uicore_cancel)).assertExists()
    }

    @Test
    fun `message is shown only when provided`() {
        setContent(message = "Some message")

        rule.onNodeWithText("Some message").assertExists()
    }

    @Test
    fun `message is absent by default`() {
        setContent()

        rule.onNodeWithText("Some message").assertDoesNotExist()
    }

    @Test
    fun `each action triggers only its own callback`() {
        setContent()

        rule.onNodeWithText(context.getString(R.string.uicore_csv_export_preview)).performClick()
        rule.onNodeWithText(context.getString(R.string.uicore_csv_export_share)).performClick()
        rule.onNodeWithText(context.getString(R.string.uicore_csv_export_save)).performClick()
        rule.onNodeWithText(context.getString(R.string.uicore_cancel)).performClick()

        assertEquals(listOf("preview", "share", "save", "dismiss"), calls)
    }
}
