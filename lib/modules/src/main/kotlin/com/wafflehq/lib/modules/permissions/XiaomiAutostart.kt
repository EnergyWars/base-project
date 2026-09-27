package com.wafflehq.lib.modules.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

private val XIAOMI_MANUFACTURERS = setOf("xiaomi", "redmi", "poco")

fun isXiaomiDevice(): Boolean = Build.MANUFACTURER.lowercase() in XIAOMI_MANUFACTURERS

fun xiaomiAutostartIntent(context: Context): Intent {
    val autostartIntent = Intent().setClassName(
        "com.miui.securitycenter",
        "com.miui.permcenter.autostart.AutoStartManagementActivity"
    )
    val resolvable = autostartIntent.resolveActivityInfo(context.packageManager, 0) != null
    return if (resolvable) {
        autostartIntent
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }
}
