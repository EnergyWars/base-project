package com.wafflehq.lib.pdf.ui

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.pdf.R
import com.wafflehq.lib.uicore.R as UiCoreR
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
class PdfExportChoiceDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val calls = mutableListOf<String>()

    private fun setContent(
        message: String? = null,
        onOpenInReader: (() -> Unit)? = null,
        extraActions: @Composable () -> Unit = {}
    ) {
        rule.setContent {
            PdfExportChoiceDialog(
                title = "Export title",
                message = message,
                onDismissRequest = { calls += "dismiss" },
                onShare = { calls += "share" },
                onSave = { calls += "save" },
                onPreview = { calls += "preview" },
                onOpenInReader = onOpenInReader,
                extraActions = extraActions
            )
        }
    }

    private fun str(id: Int) = context.getString(id)

    @Test
    fun `title and base actions are displayed`() {
        setContent()

        rule.onNodeWithText("Export title").assertExists()
        rule.onNodeWithText(str(R.string.pdf_export_preview)).assertExists()
        rule.onNodeWithText(str(R.string.pdf_export_share)).assertExists()
        rule.onNodeWithText(str(R.string.pdf_export_save)).assertExists()
        rule.onNodeWithText(str(UiCoreR.string.uicore_cancel)).assertExists()
    }

    @Test
    fun `open in reader action is hidden without a callback`() {
        setContent()

        rule.onNodeWithText(str(R.string.pdf_export_open_in_reader)).assertDoesNotExist()
    }

    @Test
    fun `open in reader action is shown and triggers its callback`() {
        setContent(onOpenInReader = { calls += "reader" })

        rule.onNodeWithText(str(R.string.pdf_export_open_in_reader)).performClick()

        assertEquals(listOf("reader"), calls)
    }

    @Test
    fun `message is shown only when provided`() {
        setContent(message = "Some message")

        rule.onNodeWithText("Some message").assertExists()
    }

    @Test
    fun `extra actions are rendered`() {
        setContent(extraActions = { Text("Extra action") })

        rule.onNodeWithText("Extra action").assertExists()
    }

    @Test
    fun `each action triggers only its own callback`() {
        setContent()

        rule.onNodeWithText(str(R.string.pdf_export_preview)).performClick()
        rule.onNodeWithText(str(R.string.pdf_export_share)).performClick()
        rule.onNodeWithText(str(R.string.pdf_export_save)).performClick()
        rule.onNodeWithText(str(UiCoreR.string.uicore_cancel)).performClick()

        assertEquals(listOf("preview", "share", "save", "dismiss"), calls)
    }
}
