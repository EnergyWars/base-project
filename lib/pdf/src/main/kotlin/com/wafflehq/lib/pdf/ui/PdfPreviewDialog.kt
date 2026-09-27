package com.wafflehq.lib.pdf.ui

import com.wafflehq.lib.uicore.R as UiCoreR
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.wafflehq.lib.pdf.PdfPageSource
import com.wafflehq.lib.pdf.R
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.scaffold.AppScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import com.wafflehq.lib.uicore.components.AppCircularProgress
import com.wafflehq.lib.uicore.components.AppDialogDefaults
import com.wafflehq.lib.uicore.components.AppEmptyState

@Composable
fun PdfPreviewDialog(
    file: File,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    var pageSource by remember(file) { mutableStateOf<PdfPageSource?>(null) }
    var isLoading by remember(file) { mutableStateOf(true) }
    var loadFailed by remember(file) { mutableStateOf(false) }

    LaunchedEffect(file) {
        isLoading = true
        loadFailed = false
        val source = try {
            withContext(Dispatchers.IO) { PdfPageSource.open(file) }
        } catch (e: Exception) {
            null
        }
        pageSource = source
        loadFailed = source == null
        isLoading = false
    }

    DisposableEffect(pageSource) {
        val source = pageSource
        onDispose { source?.close() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = AppDialogDefaults.fullWidthProperties
    ) {
        AppScaffold(
            title = stringResource(R.string.pdf_preview_title),
            onBack = onDismiss,
            backDescription = stringResource(UiCoreR.string.uicore_back),
            actions = {
                AppIconButton(
                    icon = Icons.Default.Share,
                    contentDescription = stringResource(R.string.pdf_preview_share),
                    role = AppButtonRole.Neutral,
                    onClick = onShare,
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                val source = pageSource
                when {
                    isLoading -> AppCircularProgress()
                    loadFailed || source == null -> AppEmptyState(
                        text = stringResource(R.string.pdf_preview_error),
                        icon = Icons.Filled.BrokenImage
                    )
                    else -> PdfPagesView(source = source)
                }
            }
        }
    }
}
