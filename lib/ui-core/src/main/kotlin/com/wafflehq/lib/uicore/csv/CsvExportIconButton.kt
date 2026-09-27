package com.wafflehq.lib.uicore.csv

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.uicore.R
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppIconButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CsvExportIconButton(
    fileName: String,
    snackbarHostState: SnackbarHostState,
    csvContent: () -> String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val errorMessage = stringResource(R.string.uicore_csv_export_error)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val content = csvContent()
        scope.launch {
            val success = withContext(Dispatchers.IO) { writeCsvToUri(context, content, uri) }
            if (!success) snackbarHostState.showSnackbar(errorMessage)
        }
    }
    AppIconButton(
        icon = Icons.Filled.TableChart,
        contentDescription = stringResource(R.string.uicore_csv_export_action),
        role = AppButtonRole.Neutral,
        onClick = { launcher.launch(fileName) }
    )
}
