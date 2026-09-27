package com.wafflehq.lib.pdf.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.pdf.PdfPageSource
import com.wafflehq.lib.pdf.R
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfPagesViewTest {

    @get:Rule
    val rule = createComposeRule()

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val requestedWidths = mutableListOf<Int>()

    private fun str(id: Int, vararg args: Any) = context.getString(id, *args)

    private fun source(pageCount: Int = 2): PdfPageSource {
        val source = mock<PdfPageSource> { on { this.pageCount } doReturn pageCount }
        source.stub {
            onBlocking { renderPageToWidth(any(), any()) } doAnswer { invocation ->
                requestedWidths += invocation.getArgument<Int>(1)
                Bitmap.createBitmap(20, 30, Bitmap.Config.ARGB_8888)
            }
        }
        return source
    }

    private fun viewportWidth() = rule.onNodeWithTag(PdfViewerTestTags.PAGES).fetchSemanticsNode().size.width

    @Test
    fun `shows the rendered first page`() {
        rule.setContent { PdfPagesView(source = source()) }
        rule.waitForIdle()

        rule.onNodeWithContentDescription(str(R.string.pdf_page_label, 1)).assertIsDisplayed()
    }

    @Test
    fun `renders pages at the viewport width when unzoomed`() {
        rule.setContent { PdfPagesView(source = source()) }
        rule.waitForIdle()

        assertTrue(requestedWidths.isNotEmpty())
        assertTrue(requestedWidths.all { it == viewportWidth() })
    }

    @Test
    fun `re-renders pages at a higher resolution after zooming in`() {
        rule.setContent { PdfPagesView(source = source()) }
        rule.waitForIdle()
        requestedWidths.clear()

        rule.onNodeWithTag(PdfViewerTestTags.PAGES).performTouchInput { doubleClick(center) }
        rule.mainClock.advanceTimeBy(1_000L)
        rule.waitForIdle()

        val expected = (viewportWidth() * PdfZoomState.renderZoomFor(PdfZoomState.DEFAULT_DOUBLE_TAP_ZOOM)).roundToInt()
        assertTrue(requestedWidths.contains(expected))
    }

    @Test
    fun `keeps showing the page when a higher resolution render fails`() {
        val source = mock<PdfPageSource> { on { pageCount } doReturn 1 }
        var calls = 0
        source.stub {
            onBlocking { renderPageToWidth(any(), any()) } doAnswer {
                calls++
                if (calls == 1) Bitmap.createBitmap(20, 30, Bitmap.Config.ARGB_8888) else throw OutOfMemoryError()
            }
        }
        rule.setContent { PdfPagesView(source = source) }
        rule.waitForIdle()

        rule.onNodeWithTag(PdfViewerTestTags.PAGES).performTouchInput { doubleClick(center) }
        rule.mainClock.advanceTimeBy(1_000L)
        rule.waitForIdle()

        assertTrue(calls > 1)
        rule.onNodeWithContentDescription(str(R.string.pdf_page_label, 1)).assertIsDisplayed()
    }

    @Test
    fun `shows a placeholder while a page is failing to render`() {
        val source = mock<PdfPageSource> { on { pageCount } doReturn 1 }
        source.stub {
            onBlocking { renderPageToWidth(any(), any()) } doThrow IllegalStateException("broken")
        }
        rule.setContent { PdfPagesView(source = source) }
        rule.waitForIdle()

        rule.onNodeWithContentDescription(str(R.string.pdf_page_label, 1)).assertDoesNotExist()
    }
}
