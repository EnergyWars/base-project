package com.wafflehq.lib.uicore.render

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File

object ImageFileScaler {
    const val MAX_DIMENSION_SCALED = 480
    private const val MAX_DIMENSION_ORIGINAL = 4096
    private const val JPEG_QUALITY_SCALED = 70
    private const val JPEG_QUALITY_ORIGINAL = 92

    fun readImageBytes(
        file: File,
        scale: Boolean,
        decode: (File, Int) -> Bitmap? = ::decodeSampled
    ): ByteArray? {
        if (!file.exists()) return null
        val targetMaxDimension = if (scale) MAX_DIMENSION_SCALED else MAX_DIMENSION_ORIGINAL
        val quality = if (scale) JPEG_QUALITY_SCALED else JPEG_QUALITY_ORIGINAL
        val bitmap = decode(file, targetMaxDimension) ?: return null
        return try {
            val fitted = scaleDown(bitmap, targetMaxDimension)
            try {
                val output = ByteArrayOutputStream()
                fitted.compress(Bitmap.CompressFormat.JPEG, quality, output)
                output.toByteArray()
            } finally {
                if (fitted !== bitmap) fitted.recycle()
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun decodeSampled(file: File, targetMaxDimension: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val options = BitmapFactory.Options().apply {
            inSampleSize = computeInSampleSize(bounds.outWidth, bounds.outHeight, targetMaxDimension)
        }
        return BitmapFactory.decodeFile(file.absolutePath, options)
    }

    private fun computeInSampleSize(width: Int, height: Int, targetMaxDimension: Int): Int {
        var sampleSize = 1
        while (width / sampleSize > targetMaxDimension * 2 || height / sampleSize > targetMaxDimension * 2) {
            sampleSize *= 2
        }
        return sampleSize
    }

    fun scaleDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap
        val ratio = minOf(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
        val targetWidth = (width * ratio).toInt().coerceAtLeast(1)
        val targetHeight = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
