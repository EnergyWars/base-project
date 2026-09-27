package com.wafflehq.lib.pdf

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.IntentCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SharedFileShareTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        FileProviderTestReset.clear()
    }

    private fun startedChooser(): Intent {
        val started = shadowOf(context as Application).nextStartedActivity
        assertNotNull(started)
        assertEquals(Intent.ACTION_CHOOSER, started.action)
        return started
    }

    private fun innerIntent(chooser: Intent): Intent =
        requireNotNull(IntentCompat.getParcelableExtra(chooser, Intent.EXTRA_INTENT, Intent::class.java))

    @Test
    fun `shared directory name is shared`() {
        assertEquals("shared", SharedFileShare.SHARED_CACHE_DIR_NAME)
    }

    @Test
    fun `fileProviderAuthority derives from the package name`() {
        assertEquals("${context.packageName}.fileprovider", SharedFileShare.fileProviderAuthority(context))
    }

    @Test
    fun `sharedCacheDir creates the shared subdirectory of the cache`() {
        val dir = SharedFileShare.sharedCacheDir(context)

        assertEquals(File(context.cacheDir, "shared"), dir)
        assertTrue(dir.isDirectory)
    }

    @Test
    fun `sharedCacheDir is idempotent when the directory already exists`() {
        val first = SharedFileShare.sharedCacheDir(context)
        val second = SharedFileShare.sharedCacheDir(context)

        assertEquals(first, second)
        assertTrue(second.isDirectory)
    }

    @Test
    fun `writeText writes utf8 content into the shared directory`() {
        val file = SharedFileShare.writeText(context, "notes.txt", "Größe ä ö ü")

        assertEquals(File(context.cacheDir, "shared"), file.parentFile)
        assertEquals("notes.txt", file.name)
        assertEquals("Größe ä ö ü", file.readText(Charsets.UTF_8))
    }

    @Test
    fun `writeText overwrites an existing file`() {
        SharedFileShare.writeText(context, "overwrite.txt", "first")

        val file = SharedFileShare.writeText(context, "overwrite.txt", "second")

        assertEquals("second", file.readText())
    }

    @Test
    fun `writeText removes files of earlier exports from the shared directory`() {
        val previous = SharedFileShare.writeText(context, "event.ics", "old")
        val other = SharedFileShare.writeText(context, "health_report.txt", "old")

        val current = SharedFileShare.writeText(context, "notes.txt", "new")

        assertFalse(previous.exists())
        assertFalse(other.exists())
        assertEquals(listOf(current), SharedFileShare.sharedCacheDir(context).listFiles().orEmpty().toList())
    }

    @Test
    fun `writeText replaces a previous export under the same name`() {
        SharedFileShare.writeText(context, "event.ics", "old")

        val current = SharedFileShare.writeText(context, "event.ics", "new")

        assertEquals("new", current.readText())
        assertEquals(1, SharedFileShare.sharedCacheDir(context).listFiles().orEmpty().size)
    }

    @Test
    fun `newSharedFile clears the shared directory but keeps the directory itself`() {
        val dir = SharedFileShare.sharedCacheDir(context)
        File(dir, "stale.pdf").writeText("stale")
        File(dir, "nested").apply { mkdirs() }.resolve("inner.txt").writeText("stale")

        val file = SharedFileShare.newSharedFile(context, "fresh.png")

        assertTrue(dir.isDirectory)
        assertTrue(dir.listFiles().orEmpty().isEmpty())
        assertEquals(dir, file.parentFile)
        assertEquals("fresh.png", file.name)
        assertFalse(file.exists())
    }

    @Test
    fun `newSharedFile creates the shared directory when it does not exist yet`() {
        File(context.cacheDir, "shared").deleteRecursively()

        val file = SharedFileShare.newSharedFile(context, "first.ics")

        assertTrue(file.parentFile!!.isDirectory)
    }

    @Test
    fun `newSharedFile leaves files outside the shared directory untouched`() {
        val outside = File(context.cacheDir, "temp_audio_keep.m4a").apply { writeText("keep") }

        SharedFileShare.newSharedFile(context, "fresh.ics")

        assertTrue(outside.exists())
    }

    @Test
    fun `uriFor resolves a shared file through the FileProvider`() {
        val file = SharedFileShare.writeText(context, "resolve.txt", "x")

        val uri = SharedFileShare.uriFor(context, file)

        assertEquals("content", uri.scheme)
        assertEquals(SharedFileShare.fileProviderAuthority(context), uri.authority)
        assertTrue(uri.toString().endsWith("resolve.txt"))
    }

    @Test
    fun `shareFile opens a chooser wrapping a send intent for the file`() {
        val file = SharedFileShare.writeText(context, "share.csv", "a,b")

        SharedFileShare.shareFile(context, file, "text/csv", "Share CSV")

        val chooser = startedChooser()
        assertEquals("Share CSV", chooser.getCharSequenceExtra(Intent.EXTRA_TITLE))
        assertTrue(chooser.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        val send = innerIntent(chooser)
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("text/csv", send.type)
        assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        val stream: Uri? = IntentCompat.getParcelableExtra(send, Intent.EXTRA_STREAM, Uri::class.java)
        assertEquals(SharedFileShare.uriFor(context, file), stream)
    }

    @Test
    fun `shareUri forwards the given uri and mime type`() {
        val uri = Uri.parse("content://example.authority/doc")

        SharedFileShare.shareUri(context, uri, "image/png", "Share image")

        val send = innerIntent(startedChooser())
        assertEquals("image/png", send.type)
        assertEquals(uri, IntentCompat.getParcelableExtra(send, Intent.EXTRA_STREAM, Uri::class.java))
        assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `shareText sends plain text with subject and body`() {
        SharedFileShare.shareText(context, "Subject", "Body text", "Share text")

        val chooser = startedChooser()
        assertEquals("Share text", chooser.getCharSequenceExtra(Intent.EXTRA_TITLE))
        assertTrue(chooser.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        val send = innerIntent(chooser)
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("text/plain", send.type)
        assertEquals("Subject", send.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals("Body text", send.getStringExtra(Intent.EXTRA_TEXT))
        assertFalse(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `shareText without subject omits the subject extra`() {
        SharedFileShare.shareText(context, null, "Body only", "Share text")

        val send = innerIntent(startedChooser())
        assertFalse(send.hasExtra(Intent.EXTRA_SUBJECT))
        assertNull(send.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals("Body only", send.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun `shareCsv writes the content and shares it as csv`() {
        SharedFileShare.shareCsv(context, "export.csv", "Date,Taken\n2026-01-01,1\n", "Share CSV")

        val chooser = startedChooser()
        val send = innerIntent(chooser)
        assertEquals(SharedFileShare.CSV_MIME_TYPE, send.type)
        assertEquals("Share CSV", chooser.getStringExtra(Intent.EXTRA_TITLE))
        assertEquals(
            "Date,Taken\n2026-01-01,1\n",
            File(SharedFileShare.sharedCacheDir(context), "export.csv").readText()
        )
    }

    @Test
    fun `shareBitmapPng writes a decodable png and shares it`() {
        val bitmap = Bitmap.createBitmap(4, 3, Bitmap.Config.ARGB_8888)

        SharedFileShare.shareBitmapPng(context, bitmap, "qr.png", "Share QR")

        val chooser = startedChooser()
        val send = innerIntent(chooser)
        assertEquals(SharedFileShare.PNG_MIME_TYPE, send.type)
        assertEquals("Share QR", chooser.getStringExtra(Intent.EXTRA_TITLE))
        val file = File(SharedFileShare.sharedCacheDir(context), "qr.png")
        assertTrue(file.length() > 0)
        val decoded = BitmapFactory.decodeFile(file.path)
        assertEquals(4, decoded.width)
        assertEquals(3, decoded.height)
    }

    @Test
    fun `shareBitmapPng replaces files of earlier exports`() {
        val earlier = SharedFileShare.writeText(context, "old.txt", "old")

        SharedFileShare.shareBitmapPng(context, Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888), "qr.png", "t")

        assertFalse(earlier.exists())
    }
}
