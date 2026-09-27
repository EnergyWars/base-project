package com.wafflehq.lib.pdf.edit

import android.app.Application
import android.graphics.pdf.PdfDocument
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.pdf.FileProviderTestReset
import com.wafflehq.lib.pdf.ShadowPdfDocument
import com.wafflehq.lib.pdf.ShadowPdfRenderer
import com.wafflehq.lib.pdf.ShadowPdfRendererPage
import com.wafflehq.lib.pdf.SharedFileShare
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(
    application = Application::class,
    sdk = [34],
    shadows = [ShadowPdfDocument::class, ShadowPdfRenderer::class, ShadowPdfRendererPage::class]
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfSourcePoolTest {

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val pool = PdfSourcePool()

    @Before
    fun setUp() {
        FileProviderTestReset.clear()
    }

    @After
    fun tearDown() {
        pool.close()
    }

    private fun createPdf(name: String, pageCount: Int): File {
        val document = PdfDocument()
        repeat(pageCount) { index ->
            val page = document.startPage(PdfDocument.PageInfo.Builder(200, 300, index + 1).create())
            document.finishPage(page)
        }
        val file = File(SharedFileShare.sharedCacheDir(context), name)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    @Test
    fun `pageCount reads the number of pages of a valid document`() = runTest {
        assertEquals(3, pool.pageCount(createPdf("three.pdf", 3)))
    }

    @Test
    fun `pageCount is null for a file that is not a pdf`() = runTest {
        val broken = File(SharedFileShare.sharedCacheDir(context), "broken.pdf").apply { writeText("nope") }

        assertNull(pool.pageCount(broken))
    }

    @Test
    fun `pageCount is null for a missing file`() = runTest {
        assertNull(pool.pageCount(File(SharedFileShare.sharedCacheDir(context), "missing.pdf")))
    }

    @Test
    fun `renderPage scales the page by the requested factor`() = runTest {
        val file = createPdf("two.pdf", 2)

        val bitmap = pool.renderPage(file, 1, scale = 2)

        assertNotNull(bitmap)
        assertEquals(400, bitmap!!.width)
        assertEquals(600, bitmap.height)
    }

    @Test
    fun `renderPage returns null for indices outside the document`() = runTest {
        val file = createPdf("two.pdf", 2)

        assertNull(pool.renderPage(file, 2, scale = 1))
        assertNull(pool.renderPage(file, -1, scale = 1))
    }

    @Test
    fun `renderPage returns null for an unreadable document`() = runTest {
        val broken = File(SharedFileShare.sharedCacheDir(context), "broken.pdf").apply { writeText("nope") }

        assertNull(pool.renderPage(broken, 0, scale = 1))
    }

    @Test
    fun `the same file can be queried repeatedly through one cached source`() = runTest {
        val file = createPdf("two.pdf", 2)

        assertEquals(2, pool.pageCount(file))
        assertNotNull(pool.renderPage(file, 0, scale = 1))
        assertNotNull(pool.renderPage(file, 1, scale = 1))
        assertEquals(2, pool.pageCount(file))
    }

    @Test
    fun `a closed pool refuses further access and close is idempotent`() = runTest {
        val file = createPdf("two.pdf", 2)
        assertEquals(2, pool.pageCount(file))

        pool.close()
        pool.close()

        assertNull(pool.pageCount(file))
        assertNull(pool.renderPage(file, 0, scale = 1))
    }
}
