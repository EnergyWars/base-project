package com.wafflehq.uikit.modules

import androidx.compose.ui.graphics.vector.ImageVector
import com.wafflehq.uikit.modules.permissions.ModulePermissionSpec
import kotlinx.coroutines.flow.Flow

data class ModuleNavigationEntry(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val navigationRoute: String = route,
)

data class ModuleSettingsEntry(
    val icon: ImageVector,
    val labelRes: Int,
    val descriptionRes: Int,
    val permissionHintRes: Int? = null,
    val configRoute: String? = null,
    val onboardingId: String? = null,
)

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

    suspend fun setEnabled(enabled: Boolean)

    suspend fun onPermissionResult(granted: Boolean) {
        if (!granted) setEnabled(false)
    }
}
