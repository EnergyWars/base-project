package com.wafflehq.lib.pdf

import android.app.Application
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(
    application = Application::class,
    sdk = [34],
    shadows = [ShadowPdfDocument::class, ShadowPdfRenderer::class, ShadowPdfRendererPage::class]
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfPageSourceTest {

    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Before
    fun setUp() {
        FileProviderTestReset.clear()
    }

    private fun createPdf(name: String, pageCount: Int = 1): File {
        val document = PdfDocument()
        repeat(pageCount) { index ->
            val page = document.startPage(PdfDocument.PageInfo.Builder(200, 300, index + 1).create())
            document.finishPage(page)
        }
        val sharedDir = SharedFileShare.sharedCacheDir(context)
        val file = File(sharedDir, name)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    @Test
    fun `open reads page count from a valid file`() {
        val file = createPdf("valid.pdf", pageCount = 3)

        val source = PdfPageSource.open(file)
        try {
            assertEquals(3, source.pageCount)
        } finally {
            source.close()
        }
    }

    @Test
    fun `renderPage produces a non-empty bitmap`() = runTest {
        val file = createPdf("render.pdf")

        val source = PdfPageSource.open(file)
        try {
            val bitmap = source.renderPage(0)
            assertTrue(bitmap.width > 0)
            assertTrue(bitmap.height > 0)
        } finally {
            source.close()
        }
    }

    @Test
    fun `renderPageToWidth renders at the requested width and keeps the aspect ratio`() = runTest {
        val file = createPdf("width.pdf")

        val source = PdfPageSource.open(file)
        try {
            val bitmap = source.renderPageToWidth(0, 600)
            assertEquals(600, bitmap.width)
            assertEquals(900, bitmap.height)
        } finally {
            source.close()
        }
    }

    @Test
    fun `renderPageToWidth stays within the pixel budget for extreme targets`() = runTest {
        val file = createPdf("budget.pdf")

        val source = PdfPageSource.open(file)
        try {
            val bitmap = source.renderPageToWidth(0, 100_000)
            assertTrue(bitmap.width.toLong() * bitmap.height <= PdfRenderSize.MAX_PIXELS)
            assertTrue(bitmap.width <= PdfRenderSize.MAX_DIMENSION_PX)
            assertTrue(bitmap.height <= PdfRenderSize.MAX_DIMENSION_PX)
        } finally {
            source.close()
        }
    }

    @Test
    fun `open throws for a corrupt file`() {
        val file = File(context.cacheDir, "corrupt.pdf").apply { writeBytes(byteArrayOf(1, 2, 3, 4)) }

        assertThrows(Exception::class.java) { PdfPageSource.open(file) }
    }

    @Test
    fun `open with content uri reads the same document`() {
        val file = createPdf("content.pdf", pageCount = 2)
        val uri = FileProvider.getUriForFile(context, PdfExportUtils.fileProviderAuthority(context), file)

        val source = PdfPageSource.open(context, uri)
        try {
            assertEquals(2, source?.pageCount)
        } finally {
            source?.close()
        }
    }

    @Test
    fun `open with content uri returns null for an unresolvable uri`() {
        val uri = android.net.Uri.parse("content://${PdfExportUtils.fileProviderAuthority(context)}/does-not-exist.pdf")

        val source = PdfPageSource.open(context, uri)

        assertNull(source)
    }
}
