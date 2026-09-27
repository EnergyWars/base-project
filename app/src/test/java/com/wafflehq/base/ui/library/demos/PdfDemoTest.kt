package com.wafflehq.base.ui.library.demos

import android.app.Application
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.base.ui.library.testutil.ShadowPdfDocument
import com.wafflehq.lib.pdf.R as PdfR
import com.wafflehq.lib.uicore.R as UiCoreR
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(
    application = Application::class,
    sdk = [34],
    qualifiers = "w411dp-h1800dp-xxhdpi",
    shadows = [ShadowPdfDocument::class],
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfDemoTest : LibraryDemoTest() {

    private val defaultFileName get() = PdfDemoLogic.fileName(string(R.string.libex_pdf_default_title))

    private fun resultPrefix(pages: Int): String =
        string(R.string.libex_pdf_result, defaultFileName, pages, 1L).substringBeforeLast(" · ")

    private fun showDemo(snackbar: SnackbarHostState = SnackbarHostState()) {
        show { PdfDemo(snackbarHostState = snackbar, workDispatcher = Dispatchers.Unconfined) }
    }

    @Test
    fun showsTheFileNameForTheDefaultTitle() {
        showDemo()

        assertTagText(PdfTags.FILE_NAME, defaultFileName)
    }

    @Test
    fun fileNameFollowsTheTitleField() {
        showDemo()

        replaceText(PdfTags.TITLE, "Report 1")

        assertTagText(PdfTags.FILE_NAME, PdfDemoLogic.fileName("Report 1"))
    }

    @Test
    fun generateReportsFileNameAndPageCount() {
        showDemo()

        click(PdfTags.GENERATE)
        waitForTag(PdfTags.RESULT)

        assertTagText(PdfTags.RESULT, resultPrefix(pages = 2))
    }

    @Test
    fun pageStepperChangesThePageCount() {
        showDemo()

        stepUp(PdfTags.PAGES)
        click(PdfTags.GENERATE)
        waitForTag(PdfTags.RESULT)

        assertTagText(PdfTags.RESULT, resultPrefix(pages = 3))
    }

    @Test
    fun optionsDialogOffersShareAndCancel() {
        showDemo()
        click(PdfTags.GENERATE)
        waitForTag(PdfTags.RESULT)

        click(PdfTags.OPTIONS)

        rule.onNodeWithText(defaultFileName).assertExists()
        rule.onNodeWithText(string(PdfR.string.pdf_export_share)).assertExists()
        rule.onNodeWithText(string(PdfR.string.pdf_export_preview)).assertExists()

        rule.onNodeWithText(string(UiCoreR.string.uicore_cancel)).performClick()

        rule.waitUntil(5_000) { rule.onAllNodesWithText(string(PdfR.string.pdf_export_share)).fetchSemanticsNodes().isEmpty() }
    }

    @Test
    fun sharingWithoutFileProviderReportsAnUnavailableMessage() {
        val snackbar = SnackbarHostState()
        showDemo(snackbar)
        click(PdfTags.GENERATE)
        waitForTag(PdfTags.RESULT)
        click(PdfTags.OPTIONS)

        rule.onNodeWithText(string(PdfR.string.pdf_export_share)).performClick()

        rule.waitUntil(5_000) { snackbar.currentSnackbarData != null }
        assertEquals(string(R.string.libex_pdf_share_unavailable), snackbar.currentSnackbarData?.visuals?.message)
    }

    @Test
    fun buildWritesAFileWithTheRequestedPages() {
        val result = PdfDemoLogic.build(
            context = context,
            title = "Logic",
            subtitle = "Sub",
            body = "Body text that is long enough to wrap around the page width at least once when rendered.",
            pageHeading = { "Page $it" },
            pages = 3,
            withWatermark = true,
        )

        assertEquals(3, result.pageCount)
        assertTrue(result.file.exists())
        assertEquals(PdfDemoLogic.fileName("Logic"), result.file.name)
        assertEquals(result.file.length(), result.sizeBytes)
    }

    @Test
    fun buildClampsThePageCount() {
        val result = PdfDemoLogic.build(context, "Many", "Sub", "Body", { "Page $it" }, pages = 99, withWatermark = false)

        assertEquals(PdfDemoLogic.MAX_PAGES, result.pageCount)
    }

    @Test
    fun sizeIsRoundedUpToWholeKibibytes() {
        assertEquals(0L, PdfDemoLogic.sizeInKib(0L))
        assertEquals(1L, PdfDemoLogic.sizeInKib(1L))
        assertEquals(1L, PdfDemoLogic.sizeInKib(1024L))
        assertEquals(2L, PdfDemoLogic.sizeInKib(1025L))
    }

    @Test
    fun fileNamesDropUnsafeCharacters() {
        assertEquals("libex - AB.pdf", PdfDemoLogic.fileName("A/B"))
        assertEquals("libex.pdf", PdfDemoLogic.fileName("   "))
    }
}
