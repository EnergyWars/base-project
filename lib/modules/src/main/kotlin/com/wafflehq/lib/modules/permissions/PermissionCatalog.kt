package com.wafflehq.lib.modules.permissions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

sealed interface PermissionRequirement {

    val declaredPermissions: List<String>

    data class Runtime(
        val permissions: List<String>,
        val requireAll: Boolean = true
    ) : PermissionRequirement {
        override val declaredPermissions: List<String> get() = permissions
    }

    data class Special(
        val permissions: List<String> = emptyList(),
        val isGranted: (Context) -> Boolean,
        val settingsIntent: ((Context) -> Intent)? = null
    ) : PermissionRequirement {
        override val declaredPermissions: List<String> get() = permissions
    }
}

data class PermissionDefinition(
    val id: String,
    @param:StringRes val titleRes: Int,
    val requirement: PermissionRequirement,
    val manifestPermissions: List<String> = requirement.declaredPermissions,
    val minSdk: Int = 0,
    val maxSdk: Int = Int.MAX_VALUE,
    val deviceSupported: Boolean = true,
    @param:StringRes val grantedLabelRes: Int? = null,
    @param:StringRes val deniedLabelRes: Int? = null,
    val neutralStatus: Boolean = false
) {
    fun isAvailable(sdkInt: Int = Build.VERSION.SDK_INT): Boolean =
        deviceSupported && sdkInt >= minSdk && sdkInt <= maxSdk
}

data class PermissionUsage(
    val permissionId: String,
    @param:StringRes val ownerLabelRes: Int,
    @param:StringRes val purposeRes: Int,
    val optional: Boolean = false,
    val isActive: Flow<Boolean> = flowOf(true)
)

data class PermissionUsageState(
    @param:StringRes val ownerLabelRes: Int,
    @param:StringRes val purposeRes: Int,
    val optional: Boolean,
    val active: Boolean
)

data class PermissionEntry(
    val definition: PermissionDefinition,
    val granted: Boolean,
    val usages: List<PermissionUsageState>
) {
    val isUsed: Boolean get() = usages.any { it.active }

    val isRequired: Boolean get() = usages.any { it.active && !it.optional }
}

class PermissionCatalog(
    definitions: Set<PermissionDefinition>,
    private val usages: Set<PermissionUsage>,
    private val sdkInt: Int = Build.VERSION.SDK_INT
) {
    val available: List<PermissionDefinition> = definitions.filter { it.isAvailable(sdkInt) }

    val usageStates: Flow<Map<String, List<PermissionUsageState>>> = states()

    fun entries(
        context: Context,
        usageStates: Map<String, List<PermissionUsageState>>
    ): List<PermissionEntry> = available.map { definition ->
        PermissionEntry(
            definition = definition,
            granted = definition.isGranted(context),
            usages = usageStates[definition.id].orEmpty()
        )
    }

    fun settingsIntent(definition: PermissionDefinition, context: Context): Intent? =
        (definition.requirement as? PermissionRequirement.Special)?.settingsIntent?.invoke(context)

    private fun states(): Flow<Map<String, List<PermissionUsageState>>> {
        val ordered = usages.toList()
        if (ordered.isEmpty()) return flowOf(emptyMap())
        return combine(ordered.map { it.isActive }) { active ->
            ordered.mapIndexed { index, usage ->
                usage.permissionId to PermissionUsageState(
                    ownerLabelRes = usage.ownerLabelRes,
                    purposeRes = usage.purposeRes,
                    optional = usage.optional,
                    active = active[index]
                )
            }.groupBy({ it.first }, { it.second })
        }
    }
}

fun PermissionDefinition.isGranted(context: Context): Boolean = when (val target = requirement) {
    is PermissionRequirement.Special -> target.isGranted(context)
    is PermissionRequirement.Runtime -> {
        val checks = target.permissions.map { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        when {
            checks.isEmpty() -> true
            target.requireAll -> checks.all { it }
            else -> checks.any { it }
        }
    }
}
