package com.wafflehq.lib.pdf.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.wafflehq.lib.pdf.R
import com.wafflehq.lib.pdf.SharedFileShare
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.uicore.components.AppDialog
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun rememberCameraPhotoAction(
    newFile: () -> File,
    onCaptured: suspend (File) -> Unit,
    onDiscarded: (File) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentNewFile by rememberUpdatedState(newFile)
    val currentOnCaptured by rememberUpdatedState(onCaptured)
    val currentOnDiscarded by rememberUpdatedState(onDiscarded)
    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }
    var showPermissionDenied by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val path = pendingPath
        pendingPath = null
        if (path != null) {
            val file = File(path)
            if (success) scope.launch { currentOnCaptured(file) } else currentOnDiscarded(file)
        }
    }

    fun launchCamera() {
        val file = currentNewFile()
        pendingPath = file.absolutePath
        cameraLauncher.launch(SharedFileShare.uriFor(context, file))
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera() else showPermissionDenied = true
    }

    if (showPermissionDenied) {
        AppDialog(
            onDismissRequest = { showPermissionDenied = false },
            title = { Text(stringResource(R.string.pdf_camera_permission_denied_title)) },
            text = { Text(stringResource(R.string.pdf_camera_permission_denied_message)) },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.pdf_camera_permission_open_settings),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    onClick = {
                        showPermissionDenied = false
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    }
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = { showPermissionDenied = false }
                )
            }
        )
    }

    return {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) launchCamera() else permissionLauncher.launch(Manifest.permission.CAMERA)
    }
}
