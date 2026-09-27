package com.wafflehq.lib.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class ModuleGroupingTest {

    private class FakeModule(
        override val id: String,
        override val settingsOrder: Int,
        override val category: ModuleCategory
    ) : FeatureModule {
        override val navigation: ModuleNavigationEntry = ModuleNavigationEntry(id, 1, Icons.Default.Home)
        override val isEnabled: Flow<Boolean> = MutableStateFlow(true)
        override suspend fun setEnabled(enabled: Boolean) = Unit
    }

    @Test
    fun groupsModulesByCategoryInEnumOrder() {
        val period = FakeModule("period", 0, ModuleCategory.HEALTH_CYCLE)
        val todo = FakeModule("todo", 1, ModuleCategory.SCHEDULE_ORGANIZATION)
        val weight = FakeModule("weight", 2, ModuleCategory.HEALTH_CYCLE)
        val toolbox = FakeModule("toolbox", 3, ModuleCategory.TOOLS)

        val grouped = listOf(toolbox, todo, period, weight).groupedByCategory { it.category }

        assertEquals(
            listOf(ModuleCategory.HEALTH_CYCLE, ModuleCategory.SCHEDULE_ORGANIZATION, ModuleCategory.TOOLS),
            grouped.keys.toList()
        )
        assertEquals(listOf(period, weight), grouped[ModuleCategory.HEALTH_CYCLE])
        assertEquals(listOf(todo), grouped[ModuleCategory.SCHEDULE_ORGANIZATION])
        assertEquals(listOf(toolbox), grouped[ModuleCategory.TOOLS])
    }

    @Test
    fun emptyListProducesEmptyMap() {
        assertEquals(emptyMap<ModuleCategory, List<FeatureModule>>(), emptyList<FeatureModule>().groupedByCategory { it.category })
    }

    @Test
    fun categoriesWithoutModulesAreOmitted() {
        val journal = FakeModule("tagebuch", 0, ModuleCategory.JOURNAL)
        val grouped = listOf(journal).groupedByCategory { it.category }
        assertEquals(setOf(ModuleCategory.JOURNAL), grouped.keys)
    }
}
