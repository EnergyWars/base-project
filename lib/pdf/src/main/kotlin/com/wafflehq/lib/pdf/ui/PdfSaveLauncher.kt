package com.wafflehq.lib.pdf.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.wafflehq.lib.pdf.PdfExportUtils
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun rememberPdfSaveLauncher(snackbarHostState: SnackbarHostState): (File) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingFile by remember { mutableStateOf<File?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(PdfExportUtils.MIME_TYPE)
    ) { uri ->
        val file = pendingFile
        pendingFile = null
        if (file != null && uri != null) {
            scope.launch { savePdfWithFeedback(context, file, uri, snackbarHostState) }
        }
    }
    return { file ->
        pendingFile = file
        launcher.launch(file.name)
    }
}
