package com.wafflehq.lib.pdf.ui

import android.app.Application
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.pdf.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class PdfSaveFeedbackTest {

    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    private suspend fun awaitSnackbarMessage(snackbarHostState: SnackbarHostState): String {
        withTimeout(5_000) {
            while (snackbarHostState.currentSnackbarData == null) {
                delay(10)
            }
        }
        return snackbarHostState.currentSnackbarData?.visuals?.message.orEmpty()
    }

    @Test
    fun `shows success snackbar when save succeeds`() = runBlocking {
        val file = File(context.cacheDir, "test.pdf").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val target = File(context.cacheDir, "saved.pdf")
        val uri = Uri.fromFile(target)
        val snackbarHostState = SnackbarHostState()

        val job = launch { savePdfWithFeedback(context, file, uri, snackbarHostState) }

        assertEquals(context.getString(R.string.pdf_export_save_success), awaitSnackbarMessage(snackbarHostState))
        job.cancel()
    }

    @Test
    fun `shows error snackbar when source file is missing`() = runBlocking {
        val file = File(context.cacheDir, "missing.pdf")
        val target = File(context.cacheDir, "saved2.pdf")
        val uri = Uri.fromFile(target)
        val snackbarHostState = SnackbarHostState()

        val job = launch { savePdfWithFeedback(context, file, uri, snackbarHostState) }

        assertEquals(context.getString(R.string.pdf_export_save_error), awaitSnackbarMessage(snackbarHostState))
        job.cancel()
    }
}
