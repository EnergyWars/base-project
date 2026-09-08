package com.wafflehq.uikit.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfWatermarkTest {

    private fun whiteBitmap(): Bitmap =
        Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }

    @Test
    fun `draw paints the text into the bottom right corner`() {
        val bitmap = whiteBitmap()

        PdfWatermark.draw(Canvas(bitmap), TEXT, WIDTH, HEIGHT, MARGIN)

        assertTrue(PdfTestSupport.hasWatermarkPixels(bitmap, WIDTH, HEIGHT, MARGIN))
    }

    @Test
    fun `draw leaves the top left region untouched`() {
        val bitmap = whiteBitmap()

        PdfWatermark.draw(Canvas(bitmap), TEXT, WIDTH, HEIGHT, MARGIN)

        assertFalse(PdfTestSupport.hasNonWhitePixel(bitmap, 0, 0, WIDTH / 2, HEIGHT / 2))
    }

    @Test
    fun `draw keeps the text inside the right margin`() {
        val bitmap = whiteBitmap()

        PdfWatermark.draw(Canvas(bitmap), TEXT, WIDTH, HEIGHT, MARGIN)

        assertFalse(PdfTestSupport.hasNonWhitePixel(bitmap, WIDTH - MARGIN.toInt() + 2, 0, WIDTH, HEIGHT))
    }

    @Test
    fun `draw with empty text is a no-op`() {
        val bitmap = whiteBitmap()

        PdfWatermark.draw(Canvas(bitmap), "", WIDTH, HEIGHT, MARGIN)

        assertFalse(PdfTestSupport.hasNonWhitePixel(bitmap, 0, 0, WIDTH, HEIGHT))
    }

    private companion object {
        const val WIDTH = 300
        const val HEIGHT = 200
        const val MARGIN = 20f
        const val TEXT = "Sample watermark"
    }
}
