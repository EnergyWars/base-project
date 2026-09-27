package com.wafflehq.lib.pdf.edit

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.wafflehq.lib.pdf.PdfExportUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

sealed interface PdfAssembleResult {
    data class Success(val pageCount: Int) : PdfAssembleResult
    data object NoPages : PdfAssembleResult
    data class SourceUnreadable(val pageId: Long) : PdfAssembleResult
    data object WriteFailed : PdfAssembleResult
}

class PdfDocumentAssembler(
    private val imageLoader: PdfImageLoader,
    private val sourcePool: PdfSourcePool
) {

    suspend fun assemble(pages: List<PdfEditPage>, options: PdfBuildOptions, output: File): PdfAssembleResult =
        withContext(Dispatchers.IO) {
            if (pages.isEmpty()) return@withContext PdfAssembleResult.NoPages
            val document = PdfDocument()
            try {
                for ((index, page) in pages.withIndex()) {
                    ensureActive()
                    val loaded = loadBitmap(page, options)
                        ?: return@withContext PdfAssembleResult.SourceUnreadable(page.id)
                    val upright = PdfBitmapRotation.rotate(loaded, page.rotationDegrees, recycleSource = true)
                    try {
                        drawPage(document, index + 1, upright, page.content, options)
                    } finally {
                        upright.recycle()
                    }
                }
                FileOutputStream(output).use { document.writeTo(it) }
                PdfAssembleResult.Success(pages.size)
            } catch (e: IOException) {
                output.delete()
                PdfAssembleResult.WriteFailed
            } finally {
                document.close()
            }
        }

    private suspend fun loadBitmap(page: PdfEditPage, options: PdfBuildOptions): Bitmap? = try {
        val loaded = when (val content = page.content) {
            is PdfPageContent.Image -> imageLoader.load(content.file, options.quality.maxImageSidePx)
            is PdfPageContent.PdfPage ->
                sourcePool.renderPage(content.file, content.pageIndex, options.quality.pdfRenderScale)
        }
        if (loaded != null && page.enhanceContrast) PdfContrastEnhancer.enhance(loaded, recycleSource = true) else loaded
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    } catch (e: OutOfMemoryError) {
        null
    }

    private fun drawPage(
        document: PdfDocument,
        pageNumber: Int,
        bitmap: Bitmap,
        content: PdfPageContent,
        options: PdfBuildOptions
    ) {
        val pageSize = when (content) {
            is PdfPageContent.Image -> PdfPageGeometry.imagePageSize(bitmap.width, bitmap.height, options.pageSizeMode)
            is PdfPageContent.PdfPage -> PdfPageGeometry.renderedPageSize(
                bitmap.width, bitmap.height, options.quality.pdfRenderScale
            )
        }
        val target = when (content) {
            is PdfPageContent.Image -> PdfPageGeometry.fitInside(bitmap.width, bitmap.height, pageSize)
            is PdfPageContent.PdfPage -> PdfFitRect(0f, 0f, pageSize.width.toFloat(), pageSize.height.toFloat())
        }
        val page = PdfExportUtils.startPage(document, pageNumber, pageSize.width, pageSize.height)
        page.canvas.drawBitmap(
            bitmap,
            null,
            RectF(target.left, target.top, target.right, target.bottom),
            Paint(Paint.FILTER_BITMAP_FLAG)
        )
        document.finishPage(page)
    }
}
