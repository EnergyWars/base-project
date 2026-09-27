package com.wafflehq.lib.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object SharedFileShare {

    private const val PNG_QUALITY = 100

    const val SHARED_CACHE_DIR_NAME = "shared"
    const val TEXT_MIME_TYPE = "text/plain"
    const val CSV_MIME_TYPE = "text/csv"
    const val PNG_MIME_TYPE = "image/png"

    fun fileProviderAuthority(context: Context): String = "${context.packageName}.fileprovider"

    fun sharedCacheDir(context: Context): File =
        File(context.cacheDir, SHARED_CACHE_DIR_NAME).apply { mkdirs() }

    fun newSharedFile(context: Context, fileName: String): File {
        val dir = sharedCacheDir(context)
        dir.listFiles()?.forEach { it.deleteRecursively() }
        return File(dir, fileName)
    }

    fun writeText(context: Context, fileName: String, content: String): File {
        val file = newSharedFile(context, fileName)
        file.writeText(content, Charsets.UTF_8)
        return file
    }

    fun shareCsv(context: Context, fileName: String, content: String, chooserTitle: String) {
        val file = writeText(context, fileName, content)
        shareFile(context, file, CSV_MIME_TYPE, chooserTitle)
    }

    fun shareBitmapPng(context: Context, bitmap: Bitmap, fileName: String, chooserTitle: String) {
        val file = newSharedFile(context, fileName)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
        shareFile(context, file, PNG_MIME_TYPE, chooserTitle)
    }

    fun uriFor(context: Context, file: File, authority: String = fileProviderAuthority(context)): Uri =
        FileProvider.getUriForFile(context, authority, file)

    fun shareFile(
        context: Context,
        file: File,
        mimeType: String,
        chooserTitle: String,
        authority: String = fileProviderAuthority(context)
    ) {
        shareUri(context, uriFor(context, file, authority), mimeType, chooserTitle)
    }

    fun shareUri(context: Context, uri: Uri, mimeType: String, chooserTitle: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooserFor(intent, chooserTitle))
    }

    fun shareText(context: Context, subject: String?, text: String, chooserTitle: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = TEXT_MIME_TYPE
            if (subject != null) putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(chooserFor(intent, chooserTitle))
    }

    private fun chooserFor(intent: Intent, chooserTitle: String): Intent =
        Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
}
