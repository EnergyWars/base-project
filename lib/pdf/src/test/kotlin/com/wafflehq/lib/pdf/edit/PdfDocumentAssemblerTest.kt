package com.wafflehq.lib.pdf.edit

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.pdf.FileProviderTestReset
import com.wafflehq.lib.pdf.PdfPageSource
import com.wafflehq.lib.pdf.ShadowPdfDocument
import com.wafflehq.lib.pdf.ShadowPdfRenderer
import com.wafflehq.lib.pdf.ShadowPdfRendererPage
import com.wafflehq.lib.pdf.SharedFileShare
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
class PdfDocumentAssemblerTest {

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val requestedDimensions = mutableListOf<Int>()
    private val loadedFiles = mutableListOf<File>()

    private val redLoader = PdfImageLoader { file, maxDimension ->
        loadedFiles += file
        requestedDimensions += maxDimension
        solidBitmap(sizeFor(file), Color.RED)
    }

    private val landscapeSize = 200 to 100
    private val portraitSize = 100 to 200

    private fun sizeFor(file: File): Pair<Int, Int> =
        if (file.name.startsWith("landscape")) landscapeSize else portraitSize

    private fun solidBitmap(size: Pair<Int, Int>, color: Int): Bitmap =
        Bitmap.createBitmap(size.first, size.second, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }

    private val pool = PdfSourcePool()

    private fun assembler(loader: PdfImageLoader) = PdfDocumentAssembler(loader, pool)

    @After
    fun tearDown() {
        pool.close()
    }

    @Before
    fun setUp() {
        FileProviderTestReset.clear()
        requestedDimensions.clear()
        loadedFiles.clear()
    }

    private fun output(name: String = "out.pdf") = File(SharedFileShare.sharedCacheDir(context), name)

    private fun imagePage(id: Long, name: String = "portrait-$id.jpg", rotation: Int = 0) =
        PdfEditPage(id, PdfPageContent.Image(File(name)), rotation)

    private fun createSourcePdf(name: String, sizes: List<Pair<Int, Int>>): File {
        val document = PdfDocument()
        sizes.forEachIndexed { index, size ->
            val page = document.startPage(PdfDocument.PageInfo.Builder(size.first, size.second, index + 1).create())
            page.canvas.drawColor(if (index == 0) Color.GREEN else Color.BLUE)
            document.finishPage(page)
        }
        val file = File(SharedFileShare.sharedCacheDir(context), name)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private suspend fun renderPage(file: File, index: Int): Bitmap {
        val source = PdfPageSource.open(file)
        try {
            return source.renderPage(index, scale = 1)
        } finally {
            source.close()
        }
    }

    private fun isRed(pixel: Int) = Color.red(pixel) > 200 && Color.green(pixel) < 60 && Color.blue(pixel) < 60

    @Test
    fun `assemble without pages reports NoPages and writes nothing`() = runTest {
        val file = output()

        val result = assembler(redLoader).assemble(emptyList(), PdfBuildOptions(), file)

        assertEquals(PdfAssembleResult.NoPages, result)
        assertFalse(file.exists())
    }

    @Test
    fun `assemble creates one A4 portrait page per portrait image`() = runTest {
        val file = output()

        val result = assembler(redLoader).assemble(
            listOf(imagePage(1), imagePage(2)),
            PdfBuildOptions(PdfPageSizeMode.A4),
            file
        )

        assertEquals(PdfAssembleResult.Success(2), result)
        val source = PdfPageSource.open(file)
        try {
            assertEquals(2, source.pageCount)
        } finally {
            source.close()
        }
        val page = renderPage(file, 0)
        assertEquals(595, page.width)
        assertEquals(842, page.height)
        assertTrue(isRed(page.getPixel(297, 421)))
    }

    @Test
    fun `assemble centers a landscape image on a landscape A4 page and leaves the margins white`() = runTest {
        val file = output()

        assembler(redLoader).assemble(
            listOf(imagePage(1, name = "landscape-1.jpg")),
            PdfBuildOptions(PdfPageSizeMode.A4),
            file
        )

        val page = renderPage(file, 0)
        assertEquals(842, page.width)
        assertEquals(595, page.height)
        assertTrue(isRed(page.getPixel(421, 297)))
    }

    @Test
    fun `assemble swaps page orientation when a landscape image is rotated by 90 degrees`() = runTest {
        val file = output()

        assembler(redLoader).assemble(
            listOf(imagePage(1, name = "landscape-1.jpg", rotation = 90)),
            PdfBuildOptions(PdfPageSizeMode.A4),
            file
        )

        val page = renderPage(file, 0)
        assertEquals(595, page.width)
        assertEquals(842, page.height)
    }

    @Test
    fun `assemble in match content mode keeps the image aspect ratio`() = runTest {
        val file = output()

        assembler(redLoader).assemble(
            listOf(imagePage(1, name = "landscape-1.jpg")),
            PdfBuildOptions(PdfPageSizeMode.MATCH_CONTENT),
            file
        )

        val page = renderPage(file, 0)
        assertEquals(842, page.width)
        assertEquals(421, page.height)
        assertTrue(isRed(page.getPixel(5, 5)))
    }

    @Test
    fun `assemble passes the quality dependent maximum image side to the loader`() = runTest {
        assembler(redLoader).assemble(
            listOf(imagePage(1), imagePage(2), imagePage(3)),
            PdfBuildOptions(quality = PdfImageQuality.LOW),
            output()
        )

        assertEquals(listOf(1240, 1240, 1240), requestedDimensions)
    }

    @Test
    fun `assemble turns yellowed paper white for enhanced pages only`() = runTest {
        val file = output()
        val yellowedLoader = PdfImageLoader { _, _ -> solidBitmap(portraitSize, Color.rgb(222, 200, 140)) }

        assembler(yellowedLoader).assemble(
            listOf(imagePage(1).copy(enhanceContrast = true), imagePage(2)),
            PdfBuildOptions(PdfPageSizeMode.A4),
            file
        )

        val enhanced = renderPage(file, 0).getPixel(297, 421)
        val untouched = renderPage(file, 1).getPixel(297, 421)
        assertTrue(Color.red(enhanced) > 240 && Color.green(enhanced) > 240 && Color.blue(enhanced) > 240)
        assertTrue(Color.blue(untouched) < 200)
    }

    @Test
    fun `assemble enhances the contrast of source pdf pages`() = runTest {
        val file = output()
        val source = createSourcePdf("contrast-source.pdf", listOf(200 to 300))

        assembler(redLoader).assemble(
            listOf(PdfEditPage(1, PdfPageContent.PdfPage(source, 0), enhanceContrast = true)),
            PdfBuildOptions(),
            file
        )

        val pixel = renderPage(file, 0).getPixel(100, 150)
        assertTrue(Color.red(pixel) > 240 && Color.green(pixel) > 240 && Color.blue(pixel) > 240)
    }

    @Test
    fun `assemble keeps page order`() = runTest {
        val file = output()

        assembler(redLoader).assemble(
            listOf(
                imagePage(1, name = "landscape-a.jpg"),
                imagePage(2, name = "portrait-b.jpg")
            ),
            PdfBuildOptions(PdfPageSizeMode.A4),
            file
        )

        assertEquals(listOf("landscape-a.jpg", "portrait-b.jpg"), loadedFiles.map { it.name })
        assertEquals(842, renderPage(file, 0).width)
        assertEquals(595, renderPage(file, 1).width)
    }

    @Test
    fun `assemble copies pages of a source pdf in the requested order with their original size`() = runTest {
        val source = createSourcePdf("source.pdf", listOf(200 to 300, 400 to 200))
        val file = output()

        val result = assembler(redLoader).assemble(
            listOf(
                PdfEditPage(1, PdfPageContent.PdfPage(source, 1)),
                PdfEditPage(2, PdfPageContent.PdfPage(source, 0))
            ),
            PdfBuildOptions(quality = PdfImageQuality.MEDIUM),
            file
        )

        assertEquals(PdfAssembleResult.Success(2), result)
        val first = renderPage(file, 0)
        val second = renderPage(file, 1)
        assertEquals(400, first.width)
        assertEquals(200, first.height)
        assertEquals(200, second.width)
        assertEquals(300, second.height)
        assertTrue(Color.blue(first.getPixel(10, 10)) > 200)
        assertTrue(Color.green(second.getPixel(10, 10)) > 200)
    }

    @Test
    fun `assemble rotates source pdf pages and swaps their size`() = runTest {
        val source = createSourcePdf("source.pdf", listOf(200 to 300))
        val file = output()

        assembler(redLoader).assemble(
            listOf(PdfEditPage(1, PdfPageContent.PdfPage(source, 0), rotationDegrees = 90)),
            PdfBuildOptions(),
            file
        )

        val page = renderPage(file, 0)
        assertEquals(300, page.width)
        assertEquals(200, page.height)
    }

    @Test
    fun `assemble mixes images and pdf pages in one document`() = runTest {
        val source = createSourcePdf("source.pdf", listOf(200 to 300))
        val file = output()

        val result = assembler(redLoader).assemble(
            listOf(imagePage(1), PdfEditPage(2, PdfPageContent.PdfPage(source, 0))),
            PdfBuildOptions(),
            file
        )

        assertEquals(PdfAssembleResult.Success(2), result)
    }

    @Test
    fun `assemble reports the page whose image cannot be loaded and writes nothing`() = runTest {
        val file = output()
        val loader = PdfImageLoader { _, _ -> null }

        val result = assembler(loader).assemble(listOf(imagePage(7)), PdfBuildOptions(), file)

        assertEquals(PdfAssembleResult.SourceUnreadable(7), result)
        assertFalse(file.exists())
    }

    @Test
    fun `assemble reports a pdf page index outside the source document`() = runTest {
        val source = createSourcePdf("source.pdf", listOf(200 to 300))

        val result = assembler(redLoader).assemble(
            listOf(PdfEditPage(5, PdfPageContent.PdfPage(source, 3))),
            PdfBuildOptions(),
            output()
        )

        assertEquals(PdfAssembleResult.SourceUnreadable(5), result)
    }

    @Test
    fun `assemble reports a source pdf that is not a valid document`() = runTest {
        val broken = File(SharedFileShare.sharedCacheDir(context), "broken.pdf").apply { writeText("not a pdf") }

        val result = assembler(redLoader).assemble(
            listOf(PdfEditPage(9, PdfPageContent.PdfPage(broken, 0))),
            PdfBuildOptions(),
            output()
        )

        assertEquals(PdfAssembleResult.SourceUnreadable(9), result)
    }

    @Test
    fun `assemble reports WriteFailed when the output cannot be created`() = runTest {
        val directory = File(SharedFileShare.sharedCacheDir(context), "blocked").apply { mkdirs() }

        val result = assembler(redLoader).assemble(listOf(imagePage(1)), PdfBuildOptions(), directory)

        assertEquals(PdfAssembleResult.WriteFailed, result)
    }
}
