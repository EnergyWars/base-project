package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun BlockingProgressOverlay() {
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(AppDialogDefaults.containerColor, AppDialogDefaults.shape),
            contentAlignment = Alignment.Center
        ) {
            AppCircularProgress()
        }
    }
}
