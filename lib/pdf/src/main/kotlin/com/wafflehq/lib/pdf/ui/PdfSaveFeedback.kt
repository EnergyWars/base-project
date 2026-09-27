package com.wafflehq.lib.pdf.ui

import android.content.Context
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import com.wafflehq.lib.pdf.PdfExportUtils
import com.wafflehq.lib.pdf.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun savePdfWithFeedback(context: Context, file: File, uri: Uri, snackbarHostState: SnackbarHostState) {
    val success = withContext(Dispatchers.IO) {
        PdfExportUtils.savePdfToUri(context, file, uri)
    }
    snackbarHostState.showSnackbar(
        context.getString(if (success) R.string.pdf_export_save_success else R.string.pdf_export_save_error)
    )
}
