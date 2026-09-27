package com.wafflehq.lib.modules.permissions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberModulePermissionRequester(
    onResult: (spec: ModulePermissionSpec, allGranted: Boolean) -> Unit,
): (ModulePermissionSpec) -> Unit {
    var pendingSpec by remember { mutableStateOf<ModulePermissionSpec?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val spec = pendingSpec
        pendingSpec = null
        if (spec != null) {
            onResult(spec, spec.evaluate(results))
        }
    }

    return { spec ->
        if (spec.permissions.isEmpty()) {
            onResult(spec, true)
        } else {
            pendingSpec = spec
            launcher.launch(spec.permissions.toTypedArray())
        }
    }
}
