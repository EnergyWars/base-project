package com.wafflehq.lib.media

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
class MediaFileManagerTest {

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val manager get() = MediaFileManager(context)

    private companion object {
        val RICH_EXIF_VALUES = mapOf(
            ExifInterface.TAG_DATETIME_ORIGINAL to "2020:01:01 10:00:00",
            ExifInterface.TAG_DATETIME to "2020:01:01 10:00:00",
            ExifInterface.TAG_GPS_LATITUDE to "52/1,30/1,0/1",
            ExifInterface.TAG_GPS_LATITUDE_REF to "N",
            ExifInterface.TAG_GPS_DEST_LATITUDE to "48/1,10/1,0/1",
            ExifInterface.TAG_GPS_DEST_LATITUDE_REF to "N",
            ExifInterface.TAG_GPS_IMG_DIRECTION to "90/1",
            ExifInterface.TAG_GPS_IMG_DIRECTION_REF to "T",
            ExifInterface.TAG_GPS_AREA_INFORMATION to "HomeTown",
            ExifInterface.TAG_MAKER_NOTE to "secret-maker-note",
            ExifInterface.TAG_BODY_SERIAL_NUMBER to "SN-123456",
            ExifInterface.TAG_CAMERA_OWNER_NAME to "Jane Doe",
            ExifInterface.TAG_MAKE to "TestCam",
            ExifInterface.TAG_MODEL to "TestModel",
            ExifInterface.TAG_SOFTWARE to "TestSoftware",
            ExifInterface.TAG_ARTIST to "Jane Doe",
            ExifInterface.TAG_USER_COMMENT to "private comment"
        )
        val RICH_EXIF_TAGS = RICH_EXIF_VALUES.keys.toList()
    }

    private fun createJpegWithGpsExif(name: String, width: Int, height: Int): File {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val file = File(context.cacheDir, name)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        bitmap.recycle()
        val exif = ExifInterface(file.absolutePath)
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, "52/1,30/1,0/1")
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N")
        exif.setAttribute(ExifInterface.TAG_MAKE, "TestCam")
        exif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
        exif.saveAttributes()
        return file
    }

    private fun createJpegWithRichExif(name: String, width: Int, height: Int): File {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val file = File(context.cacheDir, name)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        bitmap.recycle()
        val exif = ExifInterface(file.absolutePath)
        RICH_EXIF_TAGS.forEach { tag -> exif.setAttribute(tag, RICH_EXIF_VALUES.getValue(tag)) }
        exif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
        exif.saveAttributes()
        return file
    }

    @Test
    fun `saveImage downsamples oversized images to the max dimension`() {
        val source = createJpegWithGpsExif("oversized.jpg", 5000, 5000)

        val savedPath = manager.saveImage(Uri.fromFile(source))

        assertNotNull(savedPath)
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(savedPath, bounds)
        assertTrue(bounds.outWidth <= 4096)
        assertTrue(bounds.outHeight <= 4096)
    }

    @Test
    fun `saveFinanceReceipt strips GPS EXIF data but keeps orientation`() {
        val source = createJpegWithGpsExif("receipt.jpg", 200, 200)

        val savedPath = manager.saveFinanceReceipt(Uri.fromFile(source))

        assertNotNull(savedPath)
        val exif = ExifInterface(savedPath!!)
        assertNull(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE))
        assertNull(exif.getAttribute(ExifInterface.TAG_MAKE))
        assertTrue(
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) ==
                ExifInterface.ORIENTATION_ROTATE_90
        )
    }

    @Test
    fun `saveVocabularyImage strips GPS EXIF data`() {
        val source = createJpegWithGpsExif("vocab.jpg", 200, 200)

        val savedPath = manager.saveVocabularyImage(Uri.fromFile(source))

        assertNotNull(savedPath)
        val exif = ExifInterface(savedPath!!)
        assertNull(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE))
    }

    @Test
    fun `saveVoiceMemo copies audio content into the voice memo directory`() {
        val source = File(context.cacheDir, "memo_source.m4a")
        source.writeBytes(byteArrayOf(1, 2, 3, 4, 5))

        val savedPath = manager.saveVoiceMemo(Uri.fromFile(source))

        assertNotNull(savedPath)
        val savedFile = File(savedPath!!)
        assertTrue(savedFile.exists())
        assertTrue(savedFile.canonicalPath.startsWith(manager.voiceMemoDir.canonicalPath))
        assertTrue(savedFile.readBytes().contentEquals(byteArrayOf(1, 2, 3, 4, 5)))
    }

    @Test
    fun `deleteFile removes a saved voice memo`() {
        val source = File(context.cacheDir, "memo_to_delete.m4a")
        source.writeBytes(byteArrayOf(9))
        val savedPath = manager.saveVoiceMemo(Uri.fromFile(source))!!

        manager.deleteFile(savedPath)

        assertTrue(!File(savedPath).exists())
    }

    @Test
    fun `deleteFile ignores paths outside the allowed directories`() {
        val outsideFile = File(context.cacheDir, "outside.m4a")
        outsideFile.writeBytes(byteArrayOf(9))

        manager.deleteFile(outsideFile.absolutePath)

        assertTrue(outsideFile.exists())
    }

    @Test
    fun `hardenImageFile strips GPS EXIF and downsamples a file written by an external app`() {
        val file = manager.newFinanceReceiptFile()
        val bitmap = Bitmap.createBitmap(5000, 5000, Bitmap.Config.ARGB_8888)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        bitmap.recycle()
        val exif = ExifInterface(file.absolutePath)
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, "52/1,30/1,0/1")
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N")
        exif.saveAttributes()

        manager.hardenImageFile(file.absolutePath)

        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(file.absolutePath, bounds)
        assertTrue(bounds.outWidth <= 4096)
        assertNull(ExifInterface(file.absolutePath).getAttribute(ExifInterface.TAG_GPS_LATITUDE))
    }

    @Test
    fun `saveVocabularyImageBytes stores valid bytes and strips GPS EXIF data`() {
        val source = createJpegWithGpsExif("vocab_bytes.jpg", 200, 200)

        val savedPath = manager.saveVocabularyImageBytes(source.readBytes())

        assertNotNull(savedPath)
        assertTrue(File(savedPath!!).exists())
        assertNull(ExifInterface(savedPath).getAttribute(ExifInterface.TAG_GPS_LATITUDE))
    }

    @Test
    fun `saveVocabularyImageBytes rejects payloads above the image size limit`() {
        val savedPath = manager.saveVocabularyImageBytes(ByteArray(20 * 1024 * 1024 + 1))

        assertNull(savedPath)
    }

    @Test
    fun `saveAudio copies audio content and defaults to the m4a extension`() {
        val source = File(context.cacheDir, "audio_source.bin")
        source.writeBytes(byteArrayOf(4, 5, 6))

        val savedPath = manager.saveAudio(Uri.fromFile(source))

        assertNotNull(savedPath)
        assertTrue(savedPath!!.endsWith(".m4a"))
        assertTrue(File(savedPath).readBytes().contentEquals(byteArrayOf(4, 5, 6)))
    }

    @Test
    fun `saveAudio returns null when the source cannot be read`() {
        val missing = File(context.cacheDir, "missing_audio.m4a")

        assertNull(manager.saveAudio(Uri.fromFile(missing)))
    }

    @Test
    fun `saveVoiceMemo returns null when the source cannot be read`() {
        val missing = File(context.cacheDir, "missing_memo.m4a")

        assertNull(manager.saveVoiceMemo(Uri.fromFile(missing)))
    }

    @Test
    fun `saveImage returns null when the source cannot be read`() {
        val missing = File(context.cacheDir, "missing_image.jpg")

        assertNull(manager.saveImage(Uri.fromFile(missing)))
    }

    @Test
    fun `saveFinanceReceipt returns null when the source cannot be read`() {
        val missing = File(context.cacheDir, "missing_receipt.jpg")

        assertNull(manager.saveFinanceReceipt(Uri.fromFile(missing)))
    }

    @Test
    fun `saveVocabularyImage returns null when the source cannot be read`() {
        val missing = File(context.cacheDir, "missing_vocab.jpg")

        assertNull(manager.saveVocabularyImage(Uri.fromFile(missing)))
    }

    @Test
    fun `newFinanceReceiptFile creates unique jpg files inside the receipts directory`() {
        val first = manager.newFinanceReceiptFile()
        val second = manager.newFinanceReceiptFile()

        assertTrue(first.name.endsWith(".jpg"))
        assertTrue(first.canonicalPath.startsWith(manager.financeReceiptsDir.canonicalPath))
        assertTrue(first.name != second.name)
    }

    @Test
    fun `hardenImageFile deletes a managed file that is not an image and reports failure`() {
        val file = manager.newFinanceReceiptFile()
        file.writeBytes(byteArrayOf(1, 2, 3))

        val result = manager.hardenImageFile(file.absolutePath)

        assertFalse(result)
        assertFalse(file.exists())
    }

    @Test
    fun `hardenImageFile leaves files outside the managed directories untouched`() {
        val file = File(context.cacheDir, "not_an_image.jpg")
        file.writeBytes(byteArrayOf(1, 2, 3))

        val result = manager.hardenImageFile(file.absolutePath)

        assertFalse(result)
        assertTrue(file.exists())
    }

    @Test
    fun `hardenImageFile reports failure for a missing file`() {
        val missing = File(manager.financeReceiptsDir, "gone.jpg")

        assertFalse(manager.hardenImageFile(missing.absolutePath))
    }

    @Test
    fun `hardenImageFile returns true for a valid managed image`() {
        val file = manager.newFinanceReceiptFile()
        file.outputStream().use { out ->
            val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            bitmap.recycle()
        }

        assertTrue(manager.hardenImageFile(file.absolutePath))
        assertTrue(file.exists())
    }

    @Test
    fun `saveImage removes every EXIF tag outside of the orientation for small images`() {
        val source = createJpegWithRichExif("rich.jpg", 100, 100)

        val savedPath = manager.saveImage(Uri.fromFile(source))

        assertNotNull(savedPath)
        val exif = ExifInterface(savedPath!!)
        RICH_EXIF_TAGS.forEach { tag -> assertNull("tag $tag survived", exif.getAttribute(tag)) }
        assertEquals(ExifInterface.ORIENTATION_ROTATE_90, exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0))
    }

    @Test
    fun `saveFinanceReceipt removes every EXIF tag outside of the orientation for oversized images`() {
        val source = createJpegWithRichExif("rich_big.jpg", 4200, 300)

        val savedPath = manager.saveFinanceReceipt(Uri.fromFile(source))

        assertNotNull(savedPath)
        val exif = ExifInterface(savedPath!!)
        RICH_EXIF_TAGS.forEach { tag -> assertNull("tag $tag survived", exif.getAttribute(tag)) }
        assertEquals(ExifInterface.ORIENTATION_ROTATE_90, exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0))
    }

    @Test
    fun `saveRecipePhoto leaves no orientation tag when the source is not rotated`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val source = File(context.cacheDir, "plain.jpg")
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        bitmap.recycle()
        val exif = ExifInterface(source.absolutePath)
        exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, "2020:01:01 10:00:00")
        exif.saveAttributes()

        val savedPath = manager.saveRecipePhoto(Uri.fromFile(source))

        assertNotNull(savedPath)
        val saved = ExifInterface(savedPath!!)
        assertNull(saved.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL))
        val orientation = saved.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        assertTrue(orientation == ExifInterface.ORIENTATION_UNDEFINED || orientation == ExifInterface.ORIENTATION_NORMAL)
    }

    @Test
    fun `saveImage returns null and leaves no file when the content is not a decodable image`() {
        val source = File(context.cacheDir, "garbage.jpg").apply { writeBytes(ByteArray(2048) { it.toByte() }) }
        val imagesDir = File(context.filesDir, "fakecall/images")

        val savedPath = manager.saveImage(Uri.fromFile(source))

        assertNull(savedPath)
        assertTrue(imagesDir.listFiles().orEmpty().isEmpty())
    }

    @Test
    fun `saveFinanceReceipt returns null when the content is not a decodable image`() {
        val source = File(context.cacheDir, "garbage_receipt.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        assertNull(manager.saveFinanceReceipt(Uri.fromFile(source)))
        assertTrue(manager.financeReceiptsDir.listFiles().orEmpty().isEmpty())
    }

    @Test
    fun `saveRecipePhoto returns null when the content is not a decodable image`() {
        val source = File(context.cacheDir, "garbage_recipe.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        assertNull(manager.saveRecipePhoto(Uri.fromFile(source)))
        assertTrue(manager.recipePhotosDir.listFiles().orEmpty().isEmpty())
    }

    @Test
    fun `saveVocabularyImage returns null when the content is not a decodable image`() {
        val source = File(context.cacheDir, "garbage_vocab.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        assertNull(manager.saveVocabularyImage(Uri.fromFile(source)))
    }

    @Test
    fun `saveVocabularyImageBytes returns null and leaves no file for arbitrary bytes`() {
        val vocabularyImages = File(context.filesDir, "vocabulary/images")

        val savedPath = manager.saveVocabularyImageBytes(byteArrayOf(9, 8, 7, 6, 5))

        assertNull(savedPath)
        assertTrue(vocabularyImages.listFiles().orEmpty().isEmpty())
    }

    @Test
    fun `saveVocabularyImageBytes removes every EXIF tag outside of the orientation`() {
        val source = createJpegWithRichExif("vocab_rich.jpg", 120, 80)

        val savedPath = manager.saveVocabularyImageBytes(source.readBytes())

        assertNotNull(savedPath)
        val exif = ExifInterface(savedPath!!)
        RICH_EXIF_TAGS.forEach { tag -> assertNull("tag $tag survived", exif.getAttribute(tag)) }
        assertEquals(ExifInterface.ORIENTATION_ROTATE_90, exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0))
    }

    @Test
    fun `reencoding leaves no temporary files behind`() {
        val source = createJpegWithRichExif("no_tmp.jpg", 100, 100)

        val savedPath = manager.saveImage(Uri.fromFile(source))

        assertNotNull(savedPath)
        val names = File(savedPath!!).parentFile!!.listFiles().orEmpty().map { it.name }
        assertTrue(names.none { it.endsWith(".tmp") })
    }

    @Test
    fun `saveRecipePhoto strips GPS EXIF data but keeps orientation inside the recipe photo directory`() {
        val source = createJpegWithGpsExif("recipe.jpg", 200, 200)

        val savedPath = manager.saveRecipePhoto(Uri.fromFile(source))

        assertNotNull(savedPath)
        assertTrue(File(savedPath!!).canonicalPath.startsWith(manager.recipePhotosDir.canonicalPath))
        val exif = ExifInterface(savedPath)
        assertNull(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE))
        assertNull(exif.getAttribute(ExifInterface.TAG_MAKE))
        assertEquals(ExifInterface.ORIENTATION_ROTATE_90, exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0))
    }

    @Test
    fun `saveRecipePhoto downsamples oversized images to the max dimension`() {
        val source = createJpegWithGpsExif("recipe_big.jpg", 5000, 5000)

        val savedPath = manager.saveRecipePhoto(Uri.fromFile(source))

        assertNotNull(savedPath)
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(savedPath, bounds)
        assertTrue(bounds.outWidth <= 4096)
        assertTrue(bounds.outHeight <= 4096)
    }

    @Test
    fun `saveRecipePhoto returns null when the source cannot be read`() {
        val missing = File(context.cacheDir, "missing_recipe.jpg")

        assertNull(manager.saveRecipePhoto(Uri.fromFile(missing)))
    }

    @Test
    fun `newRecipePhotoFile creates unique jpg files inside the recipe photo directory`() {
        val first = manager.newRecipePhotoFile()
        val second = manager.newRecipePhotoFile()

        assertTrue(first.name.endsWith(".jpg"))
        assertTrue(first.canonicalPath.startsWith(manager.recipePhotosDir.canonicalPath))
        assertTrue(first.name != second.name)
    }

    @Test
    fun `copyRecipePhoto duplicates a recipe photo into a new file`() {
        val original = File(manager.recipePhotosDir, "original.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        val copyPath = manager.copyRecipePhoto(original.absolutePath)

        assertNotNull(copyPath)
        assertTrue(copyPath != original.absolutePath)
        assertTrue(File(copyPath!!).readBytes().contentEquals(byteArrayOf(1, 2, 3)))
    }

    @Test
    fun `copyRecipePhoto refuses files outside the recipe photo directory`() {
        val outside = File(context.cacheDir, "outside.jpg").apply { writeBytes(byteArrayOf(1)) }

        assertNull(manager.copyRecipePhoto(outside.absolutePath))
    }

    @Test
    fun `copyRecipePhoto returns null for a missing recipe photo`() {
        assertNull(manager.copyRecipePhoto(File(manager.recipePhotosDir, "gone.jpg").absolutePath))
    }

    @Test
    fun `deleteFile removes a saved recipe photo`() {
        val file = File(manager.recipePhotosDir, "to_delete.jpg").apply { writeBytes(byteArrayOf(1)) }

        manager.deleteFile(file.absolutePath)

        assertTrue(!file.exists())
    }
}
