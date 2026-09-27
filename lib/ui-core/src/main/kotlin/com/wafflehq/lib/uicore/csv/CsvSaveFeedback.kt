package com.wafflehq.lib.uicore.csv

import android.content.Context
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import com.wafflehq.lib.uicore.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

fun writeCsvToUri(context: Context, content: String, uri: Uri): Boolean = try {
    context.contentResolver.openOutputStream(uri)?.use { out -> out.write(content.toByteArray()) } != null
} catch (e: IOException) {
    false
}

suspend fun saveCsvWithFeedback(context: Context, content: String, uri: Uri, snackbarHostState: SnackbarHostState) {
    val success = withContext(Dispatchers.IO) { writeCsvToUri(context, content, uri) }
    snackbarHostState.showSnackbar(
        context.getString(
            if (success) R.string.uicore_csv_export_save_success else R.string.uicore_csv_export_save_error
        )
    )
}
