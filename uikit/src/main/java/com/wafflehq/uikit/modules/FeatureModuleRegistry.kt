package com.wafflehq.uikit.modules

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FeatureModuleRegistry(modules: Collection<FeatureModule>) {

    val all: List<FeatureModule> = modules.sortedBy { it.settingsOrder }

    val byId: Map<String, FeatureModule> = all.associateBy { it.id }

    init {
        require(byId.size == all.size) { "Duplicate feature module ids: ${all.map { it.id }}" }
    }

    fun module(id: String): FeatureModule =
        byId[id] ?: throw IllegalArgumentException("Unknown feature module: $id")

    fun isEnabled(id: String): Flow<Boolean> = module(id).isEnabled

    fun enabledIds(): Flow<Set<String>> =
        if (all.isEmpty()) flowOf(emptySet())
        else combine(all.map { module -> module.isEnabled.map { enabled -> module.id to enabled } }) { pairs ->
            pairs.filter { it.second }.map { it.first }.toSet()
        }

    fun enabledModules(): Flow<List<FeatureModule>> =
        enabledIds().map { ids -> all.filter { it.id in ids } }

    fun navigationEntries(): Flow<List<ModuleNavigationEntry>> =
        enabledModules().map { modules ->
            modules.sortedBy { it.navigationOrder }.mapNotNull { it.navigation }
        }

    fun modulesForColorCategory(categoryKey: String): List<FeatureModule> =
        all.filter { categoryKey in it.colorCategoryKeys }

    fun isColorCategoryVisible(categoryKey: String, alwaysVisible: Set<String>): Flow<Boolean> {
        if (categoryKey in alwaysVisible) return flowOf(true)
        val owners = modulesForColorCategory(categoryKey)
        if (owners.isEmpty()) return flowOf(false)
        return combine(owners.map { it.isEnabled }) { states -> states.any { it } }
    }
}
