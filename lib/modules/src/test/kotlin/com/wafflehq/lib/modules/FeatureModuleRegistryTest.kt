package com.wafflehq.lib.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureModuleRegistryTest {

    private class FakeModule(
        override val id: String,
        override val settingsOrder: Int,
        override val navigationOrder: Int = settingsOrder,
        enabled: Boolean = false,
        override val navigation: ModuleNavigationEntry? = ModuleNavigationEntry(id, 1, Icons.Default.Home),
        override val colorCategoryKeys: Set<String> = emptySet(),
        override val category: ModuleCategory = ModuleCategory.TOOLS
    ) : FeatureModule {
        val state = MutableStateFlow(enabled)
        override val isEnabled: Flow<Boolean> = state
        override suspend fun setEnabled(enabled: Boolean) { state.value = enabled }
    }

    @Test
    fun allIsSortedBySettingsOrderAndIndexedById() {
        val registry = FeatureModuleRegistry(setOf(FakeModule("b", 2), FakeModule("a", 1)))
        assertEquals(listOf("a", "b"), registry.all.map { it.id })
        assertEquals("b", registry.module("b").id)
        assertEquals(setOf("a", "b"), registry.byId.keys)
    }

    @Test(expected = IllegalArgumentException::class)
    fun duplicateIdsAreRejected() {
        FeatureModuleRegistry(listOf(FakeModule("a", 1), FakeModule("a", 2)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unknownModuleThrows() {
        FeatureModuleRegistry(emptyList()).module("nope")
    }

    @Test
    fun enabledIdsReflectsToggles() = runTest {
        val a = FakeModule("a", 1, enabled = true)
        val b = FakeModule("b", 2)
        val registry = FeatureModuleRegistry(listOf(a, b))
        assertEquals(setOf("a"), registry.enabledIds().first())
        b.setEnabled(true)
        assertEquals(setOf("a", "b"), registry.enabledIds().first())
        a.setEnabled(false)
        assertEquals(setOf("b"), registry.enabledIds().first())
        assertTrue(registry.isEnabled("b").first())
        assertFalse(registry.isEnabled("a").first())
    }

    @Test
    fun emptyRegistryEmitsEmptySets() = runTest {
        val registry = FeatureModuleRegistry(emptyList())
        assertEquals(emptySet<String>(), registry.enabledIds().first())
        assertEquals(emptyList<ModuleNavigationEntry>(), registry.navigationEntries().first())
    }

    @Test
    fun navigationEntriesUseNavigationOrderAndSkipModulesWithoutEntry() = runTest {
        val first = FakeModule("first", settingsOrder = 3, navigationOrder = 1, enabled = true)
        val second = FakeModule("second", settingsOrder = 1, navigationOrder = 2, enabled = true)
        val hidden = FakeModule("hidden", settingsOrder = 2, enabled = true, navigation = null)
        val disabled = FakeModule("disabled", settingsOrder = 0, navigationOrder = 0)
        val registry = FeatureModuleRegistry(listOf(first, second, hidden, disabled))
        assertEquals(listOf("first", "second"), registry.navigationEntries().first().map { it.route })
        assertEquals(listOf("disabled", "second", "hidden", "first"), registry.all.map { it.id })
    }

    @Test
    fun navigationEntriesPassThroughNavigationRoute() = runTest {
        val entry = ModuleNavigationEntry("dream?editId={editId}", 1, Icons.Default.Home, navigationRoute = "dream")
        val module = FakeModule("dream", 1, enabled = true, navigation = entry)
        val registry = FeatureModuleRegistry(listOf(module))
        val entries = registry.navigationEntries().first()
        assertEquals(listOf("dream"), entries.map { it.navigationRoute })
        assertEquals(listOf("dream?editId={editId}"), entries.map { it.route })
        assertEquals("plain", ModuleNavigationEntry("plain", 1, Icons.Default.Home).navigationRoute)
    }

    @Test
    fun navigationEntriesCarryModuleCategory() = runTest {
        val module = FakeModule("weight", 1, enabled = true, category = ModuleCategory.HEALTH_CYCLE)
        val registry = FeatureModuleRegistry(listOf(module))
        assertEquals(listOf(ModuleCategory.HEALTH_CYCLE), registry.navigationEntries().first().map { it.category })
    }

    @Test
    fun colorCategoryVisibilityFollowsOwningModules() = runTest {
        val weight = FakeModule("weight", 1, enabled = false, colorCategoryKeys = setOf("WEIGHT"))
        val registry = FeatureModuleRegistry(listOf(weight))
        assertTrue(registry.isColorCategoryVisible("GLOBAL", setOf("GLOBAL")).first())
        assertFalse(registry.isColorCategoryVisible("WEIGHT", setOf("GLOBAL")).first())
        weight.setEnabled(true)
        assertTrue(registry.isColorCategoryVisible("WEIGHT", setOf("GLOBAL")).first())
        assertFalse(registry.isColorCategoryVisible("UNOWNED", setOf("GLOBAL")).first())
        assertEquals(listOf(weight), registry.modulesForColorCategory("WEIGHT"))
    }

    @Test
    fun defaultPermissionResultDisablesModuleWhenDenied() = runTest {
        val module = FakeModule("steps", 1, enabled = true)
        module.onPermissionResult(granted = true)
        assertTrue(module.state.value)
        module.onPermissionResult(granted = false)
        assertFalse(module.state.value)
    }

    @Test
    fun interfaceDefaultsAreNeutral() {
        val module = FakeModule("x", 5)
        assertNull(module.settings)
        assertNull(module.permissionSpec)
        assertTrue(module.hiddenWhenRestricted)
        assertEquals(5, module.navigationOrder)
        assertEquals(emptySet<String>(), module.colorCategoryKeys)
        assertEquals(ModuleCategory.TOOLS, module.category)
        assertEquals(emptyList<Int>(), module.dataConnections)
    }
}
