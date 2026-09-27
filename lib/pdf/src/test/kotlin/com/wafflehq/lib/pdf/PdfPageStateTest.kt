package com.wafflehq.lib.pdf

import android.content.Context
import android.graphics.pdf.PdfDocument
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], shadows = [ShadowPdfDocument::class])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfPageStateTest {

    @Test
    fun `initial state starts on page one at top margin`() {
        val state = PdfPageState(PdfDocument())

        assertEquals(1, state.pageNumber)
        assertEquals(PdfExportUtils.MARGIN, state.y)
        assertSame(state.page.canvas, state.canvas)
        state.finish()
    }

    @Test
    fun `ensureSpace keeps page when content fits`() {
        val state = PdfPageState(PdfDocument())
        val page = state.page

        state.ensureSpace(10f)

        assertEquals(1, state.pageNumber)
        assertSame(page, state.page)
        state.finish()
    }

    @Test
    fun `ensureSpace starts new page when content would overflow`() {
        val state = PdfPageState(PdfDocument())
        val firstPage = state.page
        state.y = PdfExportUtils.PAGE_HEIGHT - PdfExportUtils.MARGIN - 5f

        state.ensureSpace(10f)

        assertEquals(2, state.pageNumber)
        assertNotSame(firstPage, state.page)
        assertEquals(PdfExportUtils.MARGIN, state.y)
        state.finish()
    }

    @Test
    fun `custom page geometry is respected`() {
        val state = PdfPageState(PdfDocument(), pageWidth = 200, pageHeight = 100, margin = 10f)

        assertEquals(200, state.page.info.pageWidth)
        assertEquals(100, state.page.info.pageHeight)
        assertEquals(10f, state.y)
        state.y = 85f
        state.ensureSpace(10f)

        assertEquals(2, state.pageNumber)
        assertEquals(200, state.page.info.pageWidth)
        assertEquals(100, state.page.info.pageHeight)
        state.finish()
    }

    @Test
    fun `finished document contains all pages`() {
        val document = PdfDocument()
        val state = PdfPageState(document)
        state.newPage()
        state.newPage()
        state.finish()

        assertEquals(3, document.pages.size)
    }

    @Test
    fun `page geometry is exposed`() {
        val state = PdfPageState(PdfDocument())

        assertEquals(PdfExportUtils.PAGE_WIDTH, state.pageWidth)
        assertEquals(PdfExportUtils.PAGE_HEIGHT, state.pageHeight)
        assertEquals(PdfExportUtils.MARGIN, state.margin)
        assertNull(state.watermarkText)
        state.finish()
    }

    @Test
    fun `watermark is drawn on every finished page`() {
        val document = PdfDocument()
        val state = PdfPageState(document, watermarkText = WATERMARK)
        state.newPage()
        state.finish()

        assertEquals(2, PdfTestSupport.finishedPageCount(document))
        for (index in 0..1) {
            val bitmap = PdfTestSupport.pageBitmap(document, index)
            assertTrue(PdfTestSupport.hasWatermarkPixels(bitmap, state.pageWidth, state.pageHeight, state.margin))
        }
    }

    @Test
    fun `pages stay blank without watermark text`() {
        val document = PdfDocument()
        val state = PdfPageState(document)
        state.newPage()
        state.finish()

        assertEquals(2, PdfTestSupport.finishedPageCount(document))
        for (index in 0..1) {
            val bitmap = PdfTestSupport.pageBitmap(document, index)
            assertFalse(PdfTestSupport.hasWatermarkPixels(bitmap, state.pageWidth, state.pageHeight, state.margin))
        }
    }

    @Test
    fun `forContext uses the watermark string resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val state = PdfPageState.forContext(context, PdfDocument())

        assertEquals(context.getString(R.string.pdf_watermark), state.watermarkText)
        assertEquals(PdfExportUtils.PAGE_WIDTH, state.pageWidth)
        state.finish()
    }

    @Test
    fun `forContext respects custom page geometry`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val state = PdfPageState.forContext(context, PdfDocument(), pageWidth = 200, pageHeight = 100, margin = 10f)

        assertEquals(200, state.pageWidth)
        assertEquals(100, state.pageHeight)
        assertEquals(10f, state.margin)
        assertEquals(10f, state.y)
        state.finish()
    }

    private companion object {
        const val WATERMARK = "Made with AllInOneCalendar"
    }
}
