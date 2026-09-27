package com.wafflehq.lib.uicore.components

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun SecureScreenEffect() {
    val activity = LocalContext.current as? Activity
    DisposableEffect(activity) {
        val currentActivity = activity
        currentActivity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            currentActivity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
