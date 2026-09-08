package com.wafflehq.uikit.pdf

import android.graphics.pdf.PdfDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
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
    fun `watermarkText is taken verbatim from the caller-supplied parameter`() {
        val state = PdfPageState(PdfDocument(), watermarkText = "Custom watermark")

        assertEquals("Custom watermark", state.watermarkText)
        state.finish()
    }
}
