package com.wafflehq.lib.modules

import androidx.compose.ui.graphics.vector.ImageVector
import com.wafflehq.lib.modules.permissions.ModulePermissionSpec
import kotlinx.coroutines.flow.Flow

data class ModuleNavigationEntry(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val navigationRoute: String = route,
    val category: ModuleCategory = ModuleCategory.TOOLS
)

data class ModuleSettingsEntry(
    val icon: ImageVector,
    val labelRes: Int,
    val descriptionRes: Int,
    val permissionHintRes: Int? = null,
    val configRoute: String? = null,
    val onboardingId: String? = null
)

enum class ModuleCategory {
    HEALTH_CYCLE,
    SCHEDULE_ORGANIZATION,
    JOURNAL,
    TOOLS,
    SCHOOL_LEARNING
}

interface FeatureModule {
    val id: String
    val settingsOrder: Int
    val navigationOrder: Int get() = settingsOrder
    val isEnabled: Flow<Boolean>
    val navigation: ModuleNavigationEntry? get() = null
    val settings: ModuleSettingsEntry? get() = null
    val permissionSpec: ModulePermissionSpec? get() = null
    val colorCategoryKeys: Set<String> get() = emptySet()
    val hiddenWhenRestricted: Boolean get() = true
    val category: ModuleCategory get() = ModuleCategory.TOOLS
    val dataConnections: List<Int> get() = emptyList()

    suspend fun setEnabled(enabled: Boolean)

    suspend fun onPermissionResult(granted: Boolean) {
        if (!granted) setEnabled(false)
    }
}

fun <T> Iterable<T>.groupedByCategory(categoryOf: (T) -> ModuleCategory): Map<ModuleCategory, List<T>> {
    val grouped = groupBy(categoryOf)
    return ModuleCategory.entries.mapNotNull { category ->
        grouped[category]?.let { category to it }
    }.toMap()
}
