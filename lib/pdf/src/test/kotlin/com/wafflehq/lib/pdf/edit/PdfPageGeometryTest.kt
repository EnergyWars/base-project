package com.wafflehq.lib.pdf.edit

import org.junit.Assert.assertEquals
import org.junit.Test

class PdfPageGeometryTest {

    @Test
    fun `A4 mode uses a portrait page for portrait and square images`() {
        assertEquals(PdfPageSize(595, 842), PdfPageGeometry.imagePageSize(300, 400, PdfPageSizeMode.A4))
        assertEquals(PdfPageSize(595, 842), PdfPageGeometry.imagePageSize(400, 400, PdfPageSizeMode.A4))
    }

    @Test
    fun `A4 mode uses a landscape page for landscape images`() {
        assertEquals(PdfPageSize(842, 595), PdfPageGeometry.imagePageSize(400, 300, PdfPageSizeMode.A4))
    }

    @Test
    fun `match content keeps the image aspect ratio with a long side of 842`() {
        assertEquals(PdfPageSize(421, 842), PdfPageGeometry.imagePageSize(500, 1000, PdfPageSizeMode.MATCH_CONTENT))
        assertEquals(PdfPageSize(842, 421), PdfPageGeometry.imagePageSize(1000, 500, PdfPageSizeMode.MATCH_CONTENT))
        assertEquals(PdfPageSize(842, 842), PdfPageGeometry.imagePageSize(700, 700, PdfPageSizeMode.MATCH_CONTENT))
    }

    @Test
    fun `match content never produces an empty side for extreme aspect ratios`() {
        val size = PdfPageGeometry.imagePageSize(1, 100_000, PdfPageSizeMode.MATCH_CONTENT)

        assertEquals(PdfPageSize(1, 842), size)
    }

    @Test
    fun `renderedPageSize divides by the render scale and stays positive`() {
        assertEquals(PdfPageSize(200, 300), PdfPageGeometry.renderedPageSize(600, 900, 3))
        assertEquals(PdfPageSize(600, 900), PdfPageGeometry.renderedPageSize(600, 900, 0))
        assertEquals(PdfPageSize(1, 1), PdfPageGeometry.renderedPageSize(1, 1, 4))
    }

    @Test
    fun `fitInside centers a wide image inside a tall page`() {
        val rect = PdfPageGeometry.fitInside(200, 100, PdfPageSize(595, 842))

        assertEquals(0f, rect.left, 0.01f)
        assertEquals(595f, rect.right, 0.01f)
        assertEquals(297.5f, rect.height, 0.01f)
        assertEquals((842f - 297.5f) / 2f, rect.top, 0.01f)
    }

    @Test
    fun `fitInside centers a tall image inside a wide page`() {
        val rect = PdfPageGeometry.fitInside(100, 200, PdfPageSize(842, 595))

        assertEquals(0f, rect.top, 0.01f)
        assertEquals(595f, rect.bottom, 0.01f)
        assertEquals(297.5f, rect.width, 0.01f)
        assertEquals((842f - 297.5f) / 2f, rect.left, 0.01f)
    }

    @Test
    fun `fitInside scales small images up to the page`() {
        val rect = PdfPageGeometry.fitInside(10, 10, PdfPageSize(595, 842))

        assertEquals(595f, rect.width, 0.01f)
        assertEquals(595f, rect.height, 0.01f)
    }
}
