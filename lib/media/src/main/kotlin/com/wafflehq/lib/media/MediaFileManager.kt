package com.wafflehq.lib.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

class MediaFileManager(private val context: Context) {

    private val imagesDir = File(context.filesDir, MediaDirs.FAKE_CALL_IMAGES)
    private val audioDir = File(context.filesDir, MediaDirs.FAKE_CALL_AUDIO)
    private val vocabularyImagesDir = File(context.filesDir, MediaDirs.VOCABULARY_IMAGES)
    val financeReceiptsDir = File(context.filesDir, MediaDirs.FINANCE_RECEIPTS)
    val recipePhotosDir = File(context.filesDir, MediaDirs.RECIPE_PHOTOS)
    val voiceMemoDir = File(context.filesDir, MediaDirs.VOICE_MEMO)

    private val allowedDirs = listOf(imagesDir, audioDir, vocabularyImagesDir, financeReceiptsDir, recipePhotosDir, voiceMemoDir)

    init {
        imagesDir.mkdirs()
        audioDir.mkdirs()
        vocabularyImagesDir.mkdirs()
        financeReceiptsDir.mkdirs()
        recipePhotosDir.mkdirs()
        voiceMemoDir.mkdirs()
    }

    fun saveVocabularyImage(uri: Uri): String? = saveImageFromUri(uri, vocabularyImagesDir)

    fun saveVocabularyImageBytes(bytes: ByteArray): String? {
        if (bytes.size > MAX_IMAGE_BYTES) return null
        val file = File(vocabularyImagesDir, "${UUID.randomUUID()}.jpg")
        return try {
            file.outputStream().use { it.write(bytes) }
            if (hardenSavedImage(file)) file.absolutePath else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save vocabulary image bytes", e)
            file.delete()
            null
        }
    }

    fun saveImage(uri: Uri): String? = saveImageFromUri(uri, imagesDir)

    fun saveFinanceReceipt(uri: Uri): String? = saveImageFromUri(uri, financeReceiptsDir)

    fun saveRecipePhoto(uri: Uri): String? = saveImageFromUri(uri, recipePhotosDir)

    fun copyRecipePhoto(path: String): String? {
        val target = File(recipePhotosDir, "${UUID.randomUUID()}.jpg")
        return try {
            val source = File(path).canonicalFile
            val isInsideRecipeDir = source.path.startsWith(recipePhotosDir.canonicalPath + File.separator)
            if (isInsideRecipeDir && source.isFile) {
                source.copyTo(target)
                target.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy recipe photo", e)
            target.delete()
            null
        }
    }

    private fun saveImageFromUri(uri: Uri, targetDir: File): String? {
        val file = File(targetDir, "${UUID.randomUUID()}.jpg")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    copyLimited(input, output, MAX_IMAGE_BYTES)
                }
            }
            if (hardenSavedImage(file)) file.absolutePath else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save image", e)
            file.delete()
            null
        }
    }

    private fun hardenSavedImage(file: File): Boolean {
        val hardened = try {
            reencodeImage(file)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to re-encode image", e)
            false
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory while re-encoding image", e)
            false
        }
        if (!hardened) file.delete()
        return hardened
    }

    private fun reencodeImage(file: File): Boolean {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false
        val orientation = readOrientation(file)
        val options = BitmapFactory.Options().apply {
            inSampleSize = computeInSampleSize(bounds.outWidth, bounds.outHeight, MAX_IMAGE_DIMENSION)
        }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return false
        val temp = File(file.parentFile, "${file.name}.tmp")
        try {
            val compressed = try {
                temp.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
            } finally {
                bitmap.recycle()
            }
            if (!compressed) return false
            if (orientation != ExifInterface.ORIENTATION_NORMAL && orientation != ExifInterface.ORIENTATION_UNDEFINED) {
                val exif = ExifInterface(temp.absolutePath)
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
                exif.saveAttributes()
            }
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE)
            return true
        } finally {
            temp.delete()
        }
    }

    private fun readOrientation(file: File): Int = try {
        ExifInterface(file.absolutePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )
    } catch (e: IOException) {
        ExifInterface.ORIENTATION_NORMAL
    }

    private fun computeInSampleSize(width: Int, height: Int, targetMaxDimension: Int): Int {
        var sampleSize = 1
        while (width / sampleSize > targetMaxDimension || height / sampleSize > targetMaxDimension) {
            sampleSize *= 2
        }
        return sampleSize
    }

    fun newFinanceReceiptFile(): File = File(financeReceiptsDir, "${UUID.randomUUID()}.jpg")

    fun newRecipePhotoFile(): File = File(recipePhotosDir, "${UUID.randomUUID()}.jpg")

    fun hardenImageFile(path: String): Boolean {
        val file = try {
            File(path).canonicalFile
        } catch (e: IOException) {
            return false
        }
        if (!isInsideAllowedDir(file)) return false
        return hardenSavedImage(file)
    }

    fun saveAudio(uri: Uri): String? {
        val extension = getFileExtension(uri) ?: "m4a"
        val file = File(audioDir, "${UUID.randomUUID()}.$extension")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    copyLimited(input, output, MAX_AUDIO_BYTES)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save audio", e)
            file.delete()
            null
        }
    }

    fun saveVoiceMemo(uri: Uri): String? {
        val extension = getFileExtension(uri) ?: "m4a"
        val file = File(voiceMemoDir, "${UUID.randomUUID()}.$extension")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    copyLimited(input, output, MAX_AUDIO_BYTES)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save voice memo", e)
            file.delete()
            null
        }
    }

    fun deleteFile(path: String) {
        try {
            val target = File(path).canonicalFile
            if (isInsideAllowedDir(target)) {
                target.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete file", e)
        }
    }

    private fun isInsideAllowedDir(target: File): Boolean = allowedDirs.any { dir ->
        target.path == dir.canonicalPath || target.path.startsWith(dir.canonicalPath + File.separator)
    }

    private fun copyLimited(input: InputStream, output: OutputStream, maxBytes: Long) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            if (total > maxBytes) throw IOException("File exceeds size limit of $maxBytes bytes")
            output.write(buffer, 0, read)
        }
    }

    private fun getFileExtension(uri: Uri): String? = try {
        val mimeType = context.contentResolver.getType(uri) ?: return null
        when {
            mimeType.contains("audio/mp4") -> "m4a"
            mimeType.contains("audio/mpeg") -> "mp3"
            mimeType.contains("audio/wav") -> "wav"
            mimeType.contains("audio/ogg") -> "ogg"
            mimeType.contains("audio/webm") -> "webm"
            else -> "m4a"
        }
    } catch (e: Exception) {
        null
    }

    companion object {
        private const val TAG = "MediaFileManager"
        private const val MAX_IMAGE_BYTES = 20L * 1024 * 1024
        private const val MAX_AUDIO_BYTES = 50L * 1024 * 1024
        private const val MAX_IMAGE_DIMENSION = 4096
        private const val JPEG_QUALITY = 92
    }
}
