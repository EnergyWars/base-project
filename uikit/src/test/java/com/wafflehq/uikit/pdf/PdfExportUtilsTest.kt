package com.wafflehq.uikit.pdf

import android.content.Context
import android.graphics.pdf.PdfDocument
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], shadows = [ShadowPdfDocument::class])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfExportUtilsTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `writeToCache writes into the cache directory and closes the document`() {
        val document = PdfDocument()
        document.finishPage(PdfExportUtils.startPage(document, 1))

        val file = PdfExportUtils.writeToCache(context, document, "export-test.pdf")

        assertEquals(context.cacheDir, file.parentFile)
        assertEquals("export-test.pdf", file.name)
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
        assertThrows(IllegalStateException::class.java) { PdfExportUtils.startPage(document, 2) }
    }

    @Test
    fun `fileProviderAuthority derives from package name`() {
        assertEquals("${context.packageName}.fileprovider", PdfExportUtils.fileProviderAuthority(context))
    }
}
