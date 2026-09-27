package com.wafflehq.lib.pdf

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object PdfExportUtils {

    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842
    const val MARGIN = 40f
    const val MIME_TYPE = "application/pdf"

    fun startPage(
        document: PdfDocument,
        number: Int,
        pageWidth: Int = PAGE_WIDTH,
        pageHeight: Int = PAGE_HEIGHT
    ): PdfDocument.Page =
        document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, number).create())

    fun fileProviderAuthority(context: Context): String = SharedFileShare.fileProviderAuthority(context)

    fun watermarkText(context: Context): String = context.getString(R.string.pdf_watermark)

    fun writeToCache(context: Context, document: PdfDocument, fileName: String): File {
        val file = SharedFileShare.newSharedFile(context, fileName)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun sharePdf(
        context: Context,
        file: File,
        shareTitle: String,
        authority: String = fileProviderAuthority(context)
    ) {
        SharedFileShare.shareFile(context, file, MIME_TYPE, shareTitle, authority)
    }

    fun sharePdfUri(context: Context, uri: Uri, shareTitle: String) {
        SharedFileShare.shareUri(context, uri, MIME_TYPE, shareTitle)
    }

    fun savePdfToUri(context: Context, file: File, uri: Uri): Boolean = try {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            file.inputStream().use { input -> input.copyTo(out) }
        } != null
    } catch (e: Exception) {
        false
    }

    fun savePdfToUri(context: Context, sourceUri: Uri, destinationUri: Uri): Boolean = try {
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                input.copyTo(output)
            }
        } != null
    } catch (e: Exception) {
        false
    }

    fun queryDisplayName(context: Context, uri: Uri): String? = try {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
    } catch (e: Exception) {
        null
    }

    fun createPdfPrintAdapter(context: Context, uri: Uri, jobName: String): PrintDocumentAdapter =
        object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                callback.onLayoutFinished(buildPdfPrintDocumentInfo(jobName), true)
            }

            override fun onWrite(
                pages: Array<out PageRange>,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                if (writePdfBytesTo(context, uri, destination)) {
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } else {
                    callback.onWriteFailed(null)
                }
            }
        }

    fun buildPdfPrintDocumentInfo(jobName: String): PrintDocumentInfo =
        PrintDocumentInfo.Builder(jobName)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .build()

    fun writePdfBytesTo(context: Context, uri: Uri, destination: ParcelFileDescriptor): Boolean = try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destination.fileDescriptor).use { output -> input.copyTo(output) }
        } != null
    } catch (e: Exception) {
        false
    }

    fun printPdf(context: Context, uri: Uri, jobName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        printManager.print(jobName, createPdfPrintAdapter(context, uri, jobName), PrintAttributes.Builder().build())
    }
}
