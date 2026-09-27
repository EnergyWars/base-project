package com.wafflehq.lib.pdf.edit

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfBitmapRotationTest {

    private fun twoPixelBitmap(): Bitmap =
        Bitmap.createBitmap(2, 1, Bitmap.Config.ARGB_8888).apply {
            setPixel(0, 0, Color.RED)
            setPixel(1, 0, Color.BLUE)
        }

    @Test
    fun `zero rotation returns the same bitmap`() {
        val bitmap = twoPixelBitmap()

        assertSame(bitmap, PdfBitmapRotation.rotate(bitmap, 0))
        assertSame(bitmap, PdfBitmapRotation.rotate(bitmap, 360))
    }

    @Test
    fun `rotating by 90 degrees swaps dimensions and moves pixels clockwise`() {
        val rotated = PdfBitmapRotation.rotate(twoPixelBitmap(), 90)

        assertEquals(1, rotated.width)
        assertEquals(2, rotated.height)
        assertEquals(Color.RED, rotated.getPixel(0, 0))
        assertEquals(Color.BLUE, rotated.getPixel(0, 1))
    }

    @Test
    fun `rotating by 270 degrees moves pixels counter clockwise`() {
        val rotated = PdfBitmapRotation.rotate(twoPixelBitmap(), 270)

        assertEquals(1, rotated.width)
        assertEquals(2, rotated.height)
        assertEquals(Color.BLUE, rotated.getPixel(0, 0))
        assertEquals(Color.RED, rotated.getPixel(0, 1))
    }

    @Test
    fun `rotating by 180 degrees keeps dimensions and mirrors the pixels`() {
        val rotated = PdfBitmapRotation.rotate(twoPixelBitmap(), 180)

        assertEquals(2, rotated.width)
        assertEquals(1, rotated.height)
        assertEquals(Color.BLUE, rotated.getPixel(0, 0))
        assertEquals(Color.RED, rotated.getPixel(1, 0))
    }

    @Test
    fun `recycleSource releases the original when a new bitmap was created`() {
        val bitmap = twoPixelBitmap()

        val rotated = PdfBitmapRotation.rotate(bitmap, 90, recycleSource = true)

        assertNotSame(bitmap, rotated)
        assertTrue(bitmap.isRecycled)
        assertFalse(rotated.isRecycled)
    }

    @Test
    fun `recycleSource keeps the bitmap alive when no rotation is needed`() {
        val bitmap = twoPixelBitmap()

        val result = PdfBitmapRotation.rotate(bitmap, 0, recycleSource = true)

        assertSame(bitmap, result)
        assertFalse(bitmap.isRecycled)
    }
}
