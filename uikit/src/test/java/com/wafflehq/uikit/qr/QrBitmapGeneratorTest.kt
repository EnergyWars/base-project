package com.wafflehq.uikit.qr

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class QrBitmapGeneratorTest {

    @Test
    fun `generates bitmap of requested size`() {
        val bitmap = generateQrBitmap("hello", 64)

        assertNotNull(bitmap)
        assertEquals(64, bitmap!!.width)
        assertEquals(64, bitmap.height)
        assertEquals(Bitmap.Config.ARGB_8888, bitmap.config)
    }

    @Test
    fun `bitmap contains black and white modules only`() {
        val bitmap = generateQrBitmap("hello", 64)!!
        val pixels = IntArray(64 * 64)
        bitmap.getPixels(pixels, 0, 64, 0, 0, 64, 64)

        assertTrue(pixels.all { it == Color.BLACK || it == Color.WHITE })
        assertTrue(pixels.any { it == Color.BLACK })
        assertTrue(pixels.any { it == Color.WHITE })
    }

    @Test
    fun `returns null for empty content`() {
        assertNull(generateQrBitmap("", 64))
    }
}
