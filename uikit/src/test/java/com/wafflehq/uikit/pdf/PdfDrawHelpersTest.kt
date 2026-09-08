package com.wafflehq.uikit.pdf

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], shadows = [ShadowPdfDocument::class])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfDrawHelpersTest {

    @Test
    fun `truncatePdfText returns text unchanged when it fits`() {
        val paint = PdfPaints.body()

        assertEquals("short", truncatePdfText("short", paint, 10_000f))
    }

    @Test
    fun `truncatePdfText shortens text and appends ellipsis when too wide`() {
        val paint = PdfPaints.body()
        val text = "a".repeat(200)
        val maxWidth = paint.measureText("a".repeat(20))

        val result = truncatePdfText(text, paint, maxWidth)

        assertTrue(result.endsWith("…"))
        assertTrue(result.length < text.length)
        assertTrue(paint.measureText(result) <= maxWidth)
    }

    @Test
    fun `truncatePdfText yields only ellipsis when nothing fits`() {
        val paint = PdfPaints.body()

        assertEquals("…", truncatePdfText("abc", paint, 0f))
    }

    @Test
    fun `drawTitleBlock advances cursor below title and subtitle`() {
        val state = PdfPageState(PdfDocument())
        val start = state.y

        state.drawTitleBlock("Title", "Subtitle")

        val expected = start + PdfPaints.title().textSize + 6f + PdfPaints.subtitle().textSize + 14f
        assertEquals(expected, state.y, 0.001f)
        state.finish()
    }

    @Test
    fun `drawSectionHeading advances cursor and breaks page when needed`() {
        val state = PdfPageState(PdfDocument())
        state.y = PdfExportUtils.PAGE_HEIGHT - PdfExportUtils.MARGIN - 1f

        state.drawSectionHeading("Heading")

        assertEquals(2, state.pageNumber)
        assertEquals(PdfExportUtils.MARGIN + PdfPaints.heading().textSize + 6f, state.y, 0.001f)
        state.finish()
    }

    @Test
    fun `drawDivider keeps cursor position`() {
        val state = PdfPageState(PdfDocument())
        val before = state.y

        state.drawDivider()

        assertEquals(before, state.y, 0f)
        state.finish()
    }

    @Test
    fun `paints use expected colours and styles`() {
        assertEquals(PdfColors.SUBTLE, PdfPaints.subtitle().color)
        assertEquals(PdfColors.SUBTLE, PdfPaints.subtleBody().color)
        assertEquals(PdfColors.GRID, PdfPaints.divider().color)
        assertEquals(0.5f, PdfPaints.divider().strokeWidth)
        assertTrue(PdfPaints.title().typeface.isBold)
        assertTrue(PdfPaints.heading().typeface.isBold)
        assertEquals(Paint.ANTI_ALIAS_FLAG, PdfPaints.body().flags and Paint.ANTI_ALIAS_FLAG)
    }

    @Test
    fun `wrapPdfText keeps text that fits on a single line`() {
        val paint = PdfPaints.body()

        assertEquals(listOf("hello world"), wrapPdfText("hello world", paint, 10_000f))
    }

    @Test
    fun `wrapPdfText wraps at word boundaries`() {
        val paint = PdfPaints.body()
        val text = "alpha beta gamma delta epsilon"
        val maxWidth = paint.measureText("alpha beta")

        val lines = wrapPdfText(text, paint, maxWidth)

        assertTrue(lines.size >= 2)
        assertEquals(text, lines.joinToString(" "))
        lines.forEach { line -> assertTrue(paint.measureText(line) <= maxWidth) }
    }

    @Test
    fun `wrapPdfText hard splits an over-long word`() {
        val paint = PdfPaints.body()
        val text = "a".repeat(100)
        val maxWidth = paint.measureText("a".repeat(10))

        val lines = wrapPdfText(text, paint, maxWidth)

        assertTrue(lines.size >= 2)
        assertEquals(text, lines.joinToString(""))
        lines.forEach { line -> assertTrue(paint.measureText(line) <= maxWidth) }
    }

    @Test
    fun `wrapPdfText returns a single empty line for empty text`() {
        assertEquals(listOf(""), wrapPdfText("", PdfPaints.body(), 100f))
    }

    @Test
    fun `wrapPdfText preserves paragraph breaks`() {
        assertEquals(listOf("one", "", "two"), wrapPdfText("one\n\ntwo", PdfPaints.body(), 10_000f))
    }

    @Test
    fun `drawWrappedText advances cursor by line count`() {
        val state = PdfPageState(PdfDocument())
        val paint = PdfPaints.body()
        val start = state.y

        state.drawWrappedText("one\ntwo\nthree", paint)

        assertEquals(start + 3 * (paint.textSize + 2f), state.y, 0.001f)
        state.finish()
    }

    @Test
    fun `drawWrappedText uses custom line spacing`() {
        val state = PdfPageState(PdfDocument())
        val paint = PdfPaints.body()
        val start = state.y

        state.drawWrappedText("single", paint, lineSpacing = 5f)

        assertEquals(start + paint.textSize + 5f, state.y, 0.001f)
        state.finish()
    }

    @Test
    fun `drawWrappedText breaks page when near the bottom`() {
        val state = PdfPageState(PdfDocument())
        val paint = PdfPaints.body()
        state.y = PdfExportUtils.PAGE_HEIGHT - PdfExportUtils.MARGIN - 1f

        state.drawWrappedText("overflow", paint)

        assertEquals(2, state.pageNumber)
        assertEquals(PdfExportUtils.MARGIN + paint.textSize + 2f, state.y, 0.001f)
        state.finish()
    }
}
