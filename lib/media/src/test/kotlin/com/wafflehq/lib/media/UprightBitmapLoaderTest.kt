package com.wafflehq.lib.media

import android.app.Application
import android.graphics.Bitmap
import androidx.exifinterface.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UprightBitmapLoaderTest {

    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    private fun createJpeg(name: String, width: Int, height: Int, orientation: Int? = null): File {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val file = File(context.cacheDir, name)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        bitmap.recycle()
        if (orientation != null) {
            val exif = ExifInterface(file.absolutePath)
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
            exif.saveAttributes()
        }
        return file
    }

    @Test
    fun `load returns the bitmap unchanged for normal orientation`() {
        val file = createJpeg("normal.jpg", 200, 100, ExifInterface.ORIENTATION_NORMAL)

        val bitmap = UprightBitmapLoader.load(file.absolutePath, 1000)

        assertNotNull(bitmap)
        assertEquals(200, bitmap!!.width)
        assertEquals(100, bitmap.height)
    }

    @Test
    fun `load returns the bitmap unchanged when there is no orientation tag`() {
        val file = createJpeg("plain.jpg", 200, 100)

        val bitmap = UprightBitmapLoader.load(file.absolutePath, 1000)

        assertEquals(200, bitmap!!.width)
        assertEquals(100, bitmap.height)
    }

    @Test
    fun `load rotates a rotate-90 image so that width and height swap`() {
        val file = createJpeg("rot90.jpg", 200, 100, ExifInterface.ORIENTATION_ROTATE_90)

        val bitmap = UprightBitmapLoader.load(file.absolutePath, 1000)

        assertEquals(100, bitmap!!.width)
        assertEquals(200, bitmap.height)
    }

    @Test
    fun `load rotates a rotate-270 image so that width and height swap`() {
        val file = createJpeg("rot270.jpg", 200, 100, ExifInterface.ORIENTATION_ROTATE_270)

        val bitmap = UprightBitmapLoader.load(file.absolutePath, 1000)

        assertEquals(100, bitmap!!.width)
        assertEquals(200, bitmap.height)
    }

    @Test
    fun `load keeps dimensions for a rotate-180 image`() {
        val file = createJpeg("rot180.jpg", 200, 100, ExifInterface.ORIENTATION_ROTATE_180)

        val bitmap = UprightBitmapLoader.load(file.absolutePath, 1000)

        assertEquals(200, bitmap!!.width)
        assertEquals(100, bitmap.height)
    }

    @Test
    fun `load keeps dimensions for flipped images`() {
        val horizontal = createJpeg("flip_h.jpg", 200, 100, ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
        val vertical = createJpeg("flip_v.jpg", 200, 100, ExifInterface.ORIENTATION_FLIP_VERTICAL)

        val horizontalBitmap = UprightBitmapLoader.load(horizontal.absolutePath, 1000)
        val verticalBitmap = UprightBitmapLoader.load(vertical.absolutePath, 1000)

        assertEquals(200, horizontalBitmap!!.width)
        assertEquals(200, verticalBitmap!!.width)
    }

    @Test
    fun `load swaps dimensions for transposed and transversed images`() {
        val transpose = createJpeg("transpose.jpg", 200, 100, ExifInterface.ORIENTATION_TRANSPOSE)
        val transverse = createJpeg("transverse.jpg", 200, 100, ExifInterface.ORIENTATION_TRANSVERSE)

        val transposeBitmap = UprightBitmapLoader.load(transpose.absolutePath, 1000)
        val transverseBitmap = UprightBitmapLoader.load(transverse.absolutePath, 1000)

        assertEquals(100, transposeBitmap!!.width)
        assertEquals(100, transverseBitmap!!.width)
    }

    @Test
    fun `load downsamples images above the max dimension`() {
        val file = createJpeg("big.jpg", 4000, 2000)

        val bitmap = UprightBitmapLoader.load(file.absolutePath, 1000)

        assertNotNull(bitmap)
        assertTrue(bitmap!!.width <= 2000)
        assertTrue(bitmap.width >= 1000)
    }

    @Test
    fun `load returns null for a missing file`() {
        assertNull(UprightBitmapLoader.load(File(context.cacheDir, "missing.jpg").absolutePath, 1000))
    }

    @Test
    fun `load returns null for a file that is not an image`() {
        val file = File(context.cacheDir, "broken.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        assertNull(UprightBitmapLoader.load(file.absolutePath, 1000))
    }

    @Test
    fun `load returns null for a non-positive max dimension`() {
        val file = createJpeg("zero.jpg", 10, 10)

        assertNull(UprightBitmapLoader.load(file.absolutePath, 0))
    }

    @Test
    fun `sampleSizeFor returns 1 for small images and powers of two for large ones`() {
        assertEquals(1, UprightBitmapLoader.sampleSizeFor(800, 600, 1000))
        assertEquals(1, UprightBitmapLoader.sampleSizeFor(1999, 1000, 1000))
        assertEquals(2, UprightBitmapLoader.sampleSizeFor(2000, 1000, 1000))
        assertEquals(4, UprightBitmapLoader.sampleSizeFor(4000, 3000, 1000))
    }

    @Test
    fun `orientationMatrix is null for normal and undefined orientations`() {
        assertNull(UprightBitmapLoader.orientationMatrix(ExifInterface.ORIENTATION_NORMAL))
        assertNull(UprightBitmapLoader.orientationMatrix(ExifInterface.ORIENTATION_UNDEFINED))
    }
}
