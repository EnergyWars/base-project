package com.wafflehq.lib.uicore.render

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ImageFileScalerTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun existingFile(): File = folder.newFile("image.jpg")

    private fun decodeJpeg(bytes: ByteArray): Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    @Test
    fun `readImageBytes returns null for a missing file`() {
        assertNull(ImageFileScaler.readImageBytes(File("/does/not/exist.jpg"), scale = true))
    }

    @Test
    fun `scaleDown keeps bitmaps within bounds unchanged`() {
        val bitmap = Bitmap.createBitmap(100, 50, Bitmap.Config.ARGB_8888)
        val result = ImageFileScaler.scaleDown(bitmap, 200)
        assertSame(bitmap, result)
    }

    @Test
    fun `scaleDown shrinks oversized bitmaps preserving aspect ratio`() {
        val bitmap = Bitmap.createBitmap(1000, 500, Bitmap.Config.ARGB_8888)
        val result = ImageFileScaler.scaleDown(bitmap, 200)
        assertEquals(200, result.width)
        assertEquals(100, result.height)
    }

    @Test
    fun `readImageBytes uses the supplied decoder and requests the target dimension`() {
        var requested = -1
        val bytes = ImageFileScaler.readImageBytes(existingFile(), scale = true) { _, maxDimension ->
            requested = maxDimension
            Bitmap.createBitmap(100, 40, Bitmap.Config.ARGB_8888)
        }
        assertEquals(ImageFileScaler.MAX_DIMENSION_SCALED, requested)
        assertNotNull(bytes)
        val decoded = decodeJpeg(bytes!!)
        assertEquals(100, decoded.width)
        assertEquals(40, decoded.height)
    }

    @Test
    fun `readImageBytes requests the original size limit when not scaling`() {
        var requested = -1
        ImageFileScaler.readImageBytes(existingFile(), scale = false) { _, maxDimension ->
            requested = maxDimension
            Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        }
        assertEquals(4096, requested)
    }

    @Test
    fun `readImageBytes scales oversized decoder output down`() {
        val bytes = ImageFileScaler.readImageBytes(existingFile(), scale = true) { _, _ ->
            Bitmap.createBitmap(960, 480, Bitmap.Config.ARGB_8888)
        }
        val decoded = decodeJpeg(bytes!!)
        assertEquals(480, decoded.width)
        assertEquals(240, decoded.height)
    }

    @Test
    fun `readImageBytes returns null when the decoder yields nothing`() {
        assertNull(ImageFileScaler.readImageBytes(existingFile(), scale = true) { _, _ -> null })
    }

    @Test
    fun `readImageBytes returns null for an undecodable file with the default decoder`() {
        assertNull(ImageFileScaler.readImageBytes(existingFile(), scale = true))
    }
}
