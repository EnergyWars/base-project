package com.wafflehq.uikit.pdf

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfExportUtils {

    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842
    const val MARGIN = 40f

    fun startPage(
        document: PdfDocument,
        number: Int,
        pageWidth: Int = PAGE_WIDTH,
        pageHeight: Int = PAGE_HEIGHT,
    ): PdfDocument.Page =
        document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, number).create())

    fun fileProviderAuthority(context: Context): String = "${context.packageName}.fileprovider"

    fun writeToCache(context: Context, document: PdfDocument, fileName: String): File {
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun sharePdf(
        context: Context,
        file: File,
        shareTitle: String,
        authority: String = fileProviderAuthority(context),
    ) {
        val uri = FileProvider.getUriForFile(context, authority, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, shareTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
