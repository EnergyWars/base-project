package com.wafflehq.lib.uicore.csv

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
import kotlinx.coroutines.launch

@Composable
fun rememberCsvSaveLauncher(snackbarHostState: SnackbarHostState): (fileName: String, content: String) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingContent by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val content = pendingContent
        pendingContent = null
        if (content != null && uri != null) {
            scope.launch { saveCsvWithFeedback(context, content, uri, snackbarHostState) }
        }
    }
    return { fileName, content ->
        pendingContent = content
        launcher.launch(fileName)
    }
}
