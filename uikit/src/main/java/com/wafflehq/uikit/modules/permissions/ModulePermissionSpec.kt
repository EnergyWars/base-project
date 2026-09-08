package com.wafflehq.uikit.modules.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

data class ModulePermissionSpec(
    val permissions: List<String>,
    val rationaleTitleRes: Int,
    val rationaleTextRes: Int,
    val hintRes: Int,
    val requireAll: Boolean = false,
)

fun ModulePermissionSpec.isGranted(context: Context): Boolean {
    if (permissions.isEmpty()) return true
    val checks = permissions.map {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    return if (requireAll) checks.all { it } else checks.any { it }
}

fun ModulePermissionSpec.evaluate(results: Map<String, Boolean>): Boolean {
    if (permissions.isEmpty()) return true
    return if (requireAll) permissions.all { results[it] == true } else permissions.any { results[it] == true }
}
