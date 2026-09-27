package com.wafflehq.lib.pdf.edit

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfContrastEnhancerTest {

    private val paper = Color.rgb(222, 200, 140)

    private fun paperBitmap(width: Int = 40, height: Int = 30): Bitmap =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(paper) }

    private fun bitmapWithInkStripes(): Bitmap {
        val bitmap = paperBitmap(120, 120)
        for (x in 0 until 120) {
            if (x % 12 < 3) for (y in 0 until 120) bitmap.setPixel(x, y, Color.rgb(40, 35, 30))
        }
        return bitmap
    }

    @Test
    fun `enhance keeps the size and returns a new bitmap`() {
        val source = paperBitmap(40, 30)

        val result = PdfContrastEnhancer.enhance(source)

        assertNotSame(source, result)
        assertEquals(40, result.width)
        assertEquals(30, result.height)
        assertFalse(source.isRecycled)
    }

    @Test
    fun `enhance turns yellowed paper white`() {
        val result = PdfContrastEnhancer.enhance(paperBitmap())

        assertEquals(Color.WHITE, result.getPixel(10, 10))
        assertEquals(Color.WHITE, result.getPixel(39, 29))
    }

    @Test
    fun `enhance turns ink black and paper white`() {
        val result = PdfContrastEnhancer.enhance(bitmapWithInkStripes())

        assertTrue(Color.red(result.getPixel(1, 60)) < 20)
        assertEquals(Color.WHITE, result.getPixel(7, 60))
    }

    @Test
    fun `enhance recycles the source when asked to`() {
        val source = paperBitmap()

        PdfContrastEnhancer.enhance(source, recycleSource = true)

        assertTrue(source.isRecycled)
    }

    @Test
    fun `enhance does not modify the source pixels`() {
        val source = paperBitmap()

        PdfContrastEnhancer.enhance(source)

        assertEquals(paper, source.getPixel(5, 5))
    }
}
