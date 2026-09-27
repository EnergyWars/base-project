package com.wafflehq.lib.pdf.ui

import com.wafflehq.lib.uicore.R as UiCoreR
import android.app.Application
import android.graphics.pdf.PdfDocument
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.pdf.R
import com.wafflehq.lib.pdf.ShadowPdfDocument
import com.wafflehq.lib.pdf.ShadowPdfRenderer
import com.wafflehq.lib.pdf.ShadowPdfRendererPage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(
    application = Application::class,
    sdk = [34],
    shadows = [ShadowPdfDocument::class, ShadowPdfRenderer::class, ShadowPdfRendererPage::class]
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfPreviewDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val calls = mutableListOf<String>()

    private fun str(id: Int, vararg args: Any) = context.getString(id, *args)

    private fun createPdf(name: String): File {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(200, 300, 1).create())
        document.finishPage(page)
        val file = File(context.cacheDir, name)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun setContent(file: File) {
        rule.setContent {
            PdfPreviewDialog(
                file = file,
                onDismiss = { calls += "dismiss" },
                onShare = { calls += "share" }
            )
        }
        rule.waitForIdle()
    }

    private fun awaitContentDescription(label: String) {
        rule.waitUntil(5_000) {
            rule.onAllNodesWithContentDescription(label).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `renders the first page of a valid file`() {
        setContent(createPdf("preview.pdf"))

        awaitContentDescription(str(R.string.pdf_page_label, 1))

        rule.onNodeWithText(str(R.string.pdf_preview_title)).assertExists()
        rule.onNodeWithText(str(R.string.pdf_preview_error)).assertDoesNotExist()
    }

    @Test
    fun `shows the error hint when the file cannot be opened`() {
        setContent(File(context.cacheDir, "missing.pdf"))
        rule.waitUntil(5_000) {
            rule.onAllNodesWithText(str(R.string.pdf_preview_error)).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithText(str(R.string.pdf_preview_error)).assertExists()
    }

    @Test
    fun `share and close buttons trigger their callbacks`() {
        setContent(createPdf("preview-actions.pdf"))

        rule.onNodeWithContentDescription(str(R.string.pdf_preview_share)).performClick()
        rule.onNodeWithContentDescription(str(UiCoreR.string.uicore_back)).performClick()

        assertEquals(listOf("share", "dismiss"), calls)
    }
}
