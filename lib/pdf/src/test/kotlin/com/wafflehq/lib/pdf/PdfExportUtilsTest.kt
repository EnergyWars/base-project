package com.wafflehq.lib.pdf

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], shadows = [ShadowPdfDocument::class])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfExportUtilsTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        FileProviderTestReset.clear()
    }

    @Test
    fun `writeToCache writes the finished pages into the shared cache directory`() {
        val document = PdfDocument()
        document.finishPage(PdfExportUtils.startPage(document, 1))

        val file = PdfExportUtils.writeToCache(context, document, "export-test.pdf")

        assertEquals(File(context.cacheDir, "shared"), file.parentFile)
        assertEquals("export-test.pdf", file.name)
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
        assertEquals(1, PdfTestSupport.finishedPageCount(document))
    }

    @Test
    fun `writeToCache removes files of earlier exports from the shared directory`() {
        val previousText = SharedFileShare.writeText(context, "event.ics", "BEGIN:VCALENDAR")
        val previousPdf = PdfDocument().let { old ->
            old.finishPage(PdfExportUtils.startPage(old, 1))
            PdfExportUtils.writeToCache(context, old, "medication_plan.pdf")
        }
        val document = PdfDocument()
        document.finishPage(PdfExportUtils.startPage(document, 1))

        val file = PdfExportUtils.writeToCache(context, document, "health_report.pdf")

        assertFalse(previousText.exists())
        assertFalse(previousPdf.exists())
        assertTrue(file.exists())
        assertEquals(listOf(file), SharedFileShare.sharedCacheDir(context).listFiles().orEmpty().toList())
    }

    @Test
    fun `writeToCache resolves through FileProvider without throwing`() {
        val document = PdfDocument()
        document.finishPage(PdfExportUtils.startPage(document, 1))

        val file = PdfExportUtils.writeToCache(context, document, "shareable-test.pdf")

        val uri = FileProvider.getUriForFile(context, PdfExportUtils.fileProviderAuthority(context), file)
        assertTrue(uri.toString().isNotBlank())
    }

    @Test
    fun `writeToCache closes the document`() {
        val document = PdfDocument()
        document.finishPage(PdfExportUtils.startPage(document, 1))

        PdfExportUtils.writeToCache(context, document, "closed-test.pdf")

        assertThrows(IllegalStateException::class.java) { PdfExportUtils.startPage(document, 2) }
    }

    @Test
    fun `watermarkText returns the string resource`() {
        assertEquals(context.getString(R.string.pdf_watermark), PdfExportUtils.watermarkText(context))
        assertEquals("Made with AllInOneCalendar", PdfExportUtils.watermarkText(context))
    }

    @Test
    fun `fileProviderAuthority derives from package name`() {
        assertEquals("${context.packageName}.fileprovider", PdfExportUtils.fileProviderAuthority(context))
    }

    @Test
    fun `savePdfToUri copies the file into the destination uri`() {
        val source = File(context.cacheDir, "save-source.pdf")
        source.writeBytes(byteArrayOf(1, 2, 3, 4))
        val target = File(context.cacheDir, "save-target.pdf")
        val uri = Uri.fromFile(target)

        val result = PdfExportUtils.savePdfToUri(context, source, uri)

        assertTrue(result)
        assertTrue(target.exists())
        assertEquals(4, target.length())
    }

    @Test
    fun `savePdfToUri returns false when source file is missing`() {
        val missing = File(context.cacheDir, "does-not-exist.pdf")
        val target = File(context.cacheDir, "save-target-2.pdf")
        val uri = Uri.fromFile(target)

        val result = PdfExportUtils.savePdfToUri(context, missing, uri)

        assertTrue(!result)
    }

    @Test
    fun `savePdfToUri copies from a source uri into a destination uri`() {
        val source = File(context.cacheDir, "uri-save-source.pdf")
        source.writeBytes(byteArrayOf(5, 6, 7, 8, 9))
        val target = File(context.cacheDir, "uri-save-target.pdf")

        val result = PdfExportUtils.savePdfToUri(context, Uri.fromFile(source), Uri.fromFile(target))

        assertTrue(result)
        assertEquals(5, target.length())
    }

    @Test
    fun `savePdfToUri from uri returns false when source uri cannot be opened`() {
        val target = File(context.cacheDir, "uri-save-target-2.pdf")

        val result = PdfExportUtils.savePdfToUri(context, Uri.parse("content://missing.authority/doc"), Uri.fromFile(target))

        assertTrue(!result)
    }

    @Test
    fun `queryDisplayName resolves the file name through FileProvider`() {
        val document = PdfDocument()
        document.finishPage(PdfExportUtils.startPage(document, 1))
        val file = PdfExportUtils.writeToCache(context, document, "display-name-test.pdf")
        val uri = FileProvider.getUriForFile(context, PdfExportUtils.fileProviderAuthority(context), file)

        assertEquals("display-name-test.pdf", PdfExportUtils.queryDisplayName(context, uri))
    }

    @Test
    fun `queryDisplayName returns null for an unresolvable uri`() {
        assertEquals(null, PdfExportUtils.queryDisplayName(context, Uri.parse("content://missing.authority/doc")))
    }

    @Test
    fun `sharePdfUri opens a share chooser for the given uri`() {
        val file = File(context.cacheDir, "share-uri-test.pdf")
        file.writeBytes(byteArrayOf(1))

        PdfExportUtils.sharePdfUri(context, Uri.fromFile(file), "Share PDF")

        val started = shadowOf(context as Application).nextStartedActivity
        assertNotNull(started)
        assertEquals(Intent.ACTION_CHOOSER, started.action)
    }

    @Test
    fun `sharePdf resolves the file and opens a share chooser for a pdf`() {
        val document = PdfDocument()
        document.finishPage(PdfExportUtils.startPage(document, 1))
        val file = PdfExportUtils.writeToCache(context, document, "share-file-test.pdf")

        PdfExportUtils.sharePdf(context, file, "Share PDF")

        val started = shadowOf(context as Application).nextStartedActivity
        assertEquals(Intent.ACTION_CHOOSER, started.action)
        val send = IntentCompat.getParcelableExtra(started, Intent.EXTRA_INTENT, Intent::class.java)
        assertEquals(PdfExportUtils.MIME_TYPE, send?.type)
    }

    @Test
    fun `writePdfBytesTo streams the source uri into the destination descriptor`() {
        val source = File(context.cacheDir, "print-source.pdf")
        source.writeBytes(byteArrayOf(1, 2, 3))
        val destination = File(context.cacheDir, "print-destination.pdf")
        destination.createNewFile()
        val descriptor = ParcelFileDescriptor.open(destination, ParcelFileDescriptor.MODE_READ_WRITE)

        val result = try {
            PdfExportUtils.writePdfBytesTo(context, Uri.fromFile(source), descriptor)
        } finally {
            descriptor.close()
        }

        assertTrue(result)
        assertEquals(3, destination.length())
    }

    @Test
    fun `writePdfBytesTo returns false when the source uri cannot be opened`() {
        val destination = File(context.cacheDir, "print-destination-2.pdf")
        destination.createNewFile()
        val descriptor = ParcelFileDescriptor.open(destination, ParcelFileDescriptor.MODE_READ_WRITE)

        val result = try {
            PdfExportUtils.writePdfBytesTo(context, Uri.parse("content://missing.authority/doc"), descriptor)
        } finally {
            descriptor.close()
        }

        assertTrue(!result)
    }

    @Test
    fun `buildPdfPrintDocumentInfo sets the job name`() {
        val info = PdfExportUtils.buildPdfPrintDocumentInfo("my-document.pdf")

        assertEquals("my-document.pdf", info.name)
    }

    @Test
    fun `createPdfPrintAdapter returns a usable adapter instance`() {
        val file = File(context.cacheDir, "adapter-source.pdf")
        file.writeBytes(byteArrayOf(1))

        val adapter = PdfExportUtils.createPdfPrintAdapter(context, Uri.fromFile(file), "adapter-job")

        assertNotNull(adapter)
    }
}
