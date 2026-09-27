package com.wafflehq.lib.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

private const val MAX_RENDER_DIMENSION_PX = 4096
private const val MAX_PDF_FILE_BYTES = 100L * 1024 * 1024

class PdfPageSource private constructor(
    private val pfd: ParcelFileDescriptor,
    private val renderer: PdfRenderer
) {
    private val mutex = Mutex()
    val pageCount: Int get() = renderer.pageCount

    suspend fun renderPage(index: Int, scale: Int = 2): Bitmap = withContext(Dispatchers.IO) {
        mutex.withLock {
            renderer.openPage(index).use { page ->
                val width = (page.width * scale).coerceIn(1, MAX_RENDER_DIMENSION_PX)
                val height = (page.height * scale).coerceIn(1, MAX_RENDER_DIMENSION_PX)
                renderInto(page, width, height)
            }
        }
    }

    suspend fun renderPageToWidth(index: Int, targetWidthPx: Int): Bitmap = withContext(Dispatchers.IO) {
        mutex.withLock {
            renderer.openPage(index).use { page ->
                val size = PdfRenderSize.fitWidth(page.width, page.height, targetWidthPx)
                renderInto(page, size.width, size.height)
            }
        }
    }

    private fun renderInto(page: PdfRenderer.Page, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    }

    fun close() {
        renderer.close()
        pfd.close()
    }

    companion object {
        fun open(file: File): PdfPageSource {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            return openFromDescriptor(pfd)
        }

        fun open(context: Context, uri: Uri): PdfPageSource? = try {
            if (uri.scheme != "content") {
                null
            } else {
                val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
                if (pfd.statSize > MAX_PDF_FILE_BYTES) {
                    pfd.close()
                    null
                } else {
                    openFromDescriptor(pfd)
                }
            }
        } catch (e: Exception) {
            null
        }

        private fun openFromDescriptor(pfd: ParcelFileDescriptor): PdfPageSource {
            return try {
                PdfPageSource(pfd, PdfRenderer(pfd))
            } catch (e: Exception) {
                pfd.close()
                throw e
            }
        }
    }
}
