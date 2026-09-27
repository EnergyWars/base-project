package com.wafflehq.lib.pdf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfRenderSizeTest {

    @Test
    fun `fitWidth scales to the requested width and keeps the aspect ratio`() {
        val size = PdfRenderSize.fitWidth(pageWidth = 595, pageHeight = 842, targetWidthPx = 1190)

        assertEquals(1190, size.width)
        assertEquals(1684, size.height)
    }

    @Test
    fun `fitWidth can render smaller than the page`() {
        val size = PdfRenderSize.fitWidth(pageWidth = 600, pageHeight = 800, targetWidthPx = 300)

        assertEquals(300, size.width)
        assertEquals(400, size.height)
    }

    @Test
    fun `fitWidth caps the pixel budget and keeps the aspect ratio`() {
        val size = PdfRenderSize.fitWidth(pageWidth = 595, pageHeight = 842, targetWidthPx = 20_000)

        assertTrue(size.width.toLong() * size.height <= PdfRenderSize.MAX_PIXELS)
        assertEquals(595.0 / 842.0, size.width.toDouble() / size.height, 0.01)
    }

    @Test
    fun `fitWidth caps the longest side for tall pages`() {
        val size = PdfRenderSize.fitWidth(pageWidth = 100, pageHeight = 2000, targetWidthPx = 10_000)

        assertTrue(size.width <= PdfRenderSize.MAX_DIMENSION_PX)
        assertTrue(size.height <= PdfRenderSize.MAX_DIMENSION_PX)
        assertEquals(100.0 / 2000.0, size.width.toDouble() / size.height, 0.01)
    }

    @Test
    fun `fitWidth caps the longest side for wide pages`() {
        val size = PdfRenderSize.fitWidth(pageWidth = 3000, pageHeight = 100, targetWidthPx = 100_000)

        assertEquals(PdfRenderSize.MAX_DIMENSION_PX, size.width)
        assertTrue(size.height >= 1)
    }

    @Test
    fun `fitWidth never returns a zero sized bitmap`() {
        val size = PdfRenderSize.fitWidth(pageWidth = 1, pageHeight = 1000, targetWidthPx = 1)

        assertTrue(size.width >= 1)
        assertTrue(size.height >= 1)
    }

    @Test
    fun `fitWidth tolerates degenerate input`() {
        val size = PdfRenderSize.fitWidth(pageWidth = 0, pageHeight = 0, targetWidthPx = 0)

        assertEquals(1, size.width)
        assertEquals(1, size.height)
    }
}
