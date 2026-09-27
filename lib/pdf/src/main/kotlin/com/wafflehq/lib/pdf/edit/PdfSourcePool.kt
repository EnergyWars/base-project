package com.wafflehq.lib.pdf.edit

import android.graphics.Bitmap
import com.wafflehq.lib.pdf.PdfPageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.File

class PdfSourcePool : Closeable {

    private val lock = Any()
    private val sources = HashMap<File, PdfPageSource>()
    private var closed = false

    suspend fun pageCount(file: File): Int? = source(file)?.pageCount

    suspend fun renderPage(file: File, pageIndex: Int, scale: Int): Bitmap? {
        val source = source(file) ?: return null
        if (pageIndex !in 0 until source.pageCount) return null
        return source.renderPage(pageIndex, scale)
    }

    override fun close() {
        synchronized(lock) {
            closed = true
            sources.values.forEach { it.close() }
            sources.clear()
        }
    }

    private suspend fun source(file: File): PdfPageSource? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            if (closed) return@synchronized null
            sources[file] ?: try {
                PdfPageSource.open(file).also { sources[file] = it }
            } catch (e: Exception) {
                null
            }
        }
    }
}
