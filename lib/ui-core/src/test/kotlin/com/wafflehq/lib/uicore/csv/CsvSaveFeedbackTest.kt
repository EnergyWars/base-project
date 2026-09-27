package com.wafflehq.lib.uicore.csv

import android.app.Application
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.uicore.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class CsvSaveFeedbackTest {

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
    fun `writeCsvToUri writes the content and reports success`() {
        val target = File(context.cacheDir, "written.csv")

        val success = writeCsvToUri(context, "a,b\n1,2\n", Uri.fromFile(target))

        assertTrue(success)
        assertEquals("a,b\n1,2\n", target.readText())
    }

    @Test
    fun `writeCsvToUri reports failure when the destination directory is missing`() {
        val target = File(context.cacheDir, "missing_dir/written.csv")

        assertFalse(writeCsvToUri(context, "content", Uri.fromFile(target)))
    }

    @Test
    fun `shows success snackbar when save succeeds`() = runBlocking {
        val target = File(context.cacheDir, "saved.csv")
        val snackbarHostState = SnackbarHostState()

        val job = launch { saveCsvWithFeedback(context, "Date,Taken\n2026-01-01,1\n", Uri.fromFile(target), snackbarHostState) }

        assertEquals(context.getString(R.string.uicore_csv_export_save_success), awaitSnackbarMessage(snackbarHostState))
        job.cancel()
        assertEquals("Date,Taken\n2026-01-01,1\n", target.readText())
    }

    @Test
    fun `shows error snackbar when destination directory is missing`() = runBlocking {
        val target = File(context.cacheDir, "missing_dir/saved.csv")
        val snackbarHostState = SnackbarHostState()

        val job = launch { saveCsvWithFeedback(context, "content", Uri.fromFile(target), snackbarHostState) }

        assertEquals(context.getString(R.string.uicore_csv_export_save_error), awaitSnackbarMessage(snackbarHostState))
        job.cancel()
    }
}
