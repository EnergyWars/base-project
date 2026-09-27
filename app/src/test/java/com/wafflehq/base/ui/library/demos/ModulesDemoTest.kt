package com.wafflehq.base.ui.library.demos

import android.Manifest
import android.app.Application
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.modules.FeatureModuleRegistry
import com.wafflehq.lib.modules.ModuleCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ModulesDemoTest : LibraryDemoTest() {

    private val today = LocalDate.of(2026, 3, 10)

    @Test
    fun startsWithOnlyTheWeatherModuleEnabled() {
        show { ModulesDemo(today = today) }

        waitForTagText(ModulesTags.ENABLED_IDS, ModulesDemoLogic.WEATHER)
        assertNoTagText(ModulesTags.ENABLED_IDS, ModulesDemoLogic.JOURNAL)
    }

    @Test
    fun togglingAModuleUpdatesTheEnabledIds() {
        show { ModulesDemo(today = today) }

        click(ModulesTags.toggle(ModulesDemoLogic.JOURNAL))

        waitForTagText(ModulesTags.ENABLED_IDS, ModulesDemoLogic.JOURNAL)
        assertTagText(ModulesTags.ENABLED_IDS, ModulesDemoLogic.WEATHER)
    }

    @Test
    fun duplicateIdsAreRejectedByTheRegistry() {
        show { ModulesDemo(today = today) }

        click(ModulesTags.DUPLICATE)

        assertTagText(ModulesTags.DUPLICATE_RESULT, string(R.string.libex_modules_duplicate_rejected))
    }

    @Test
    fun permissionRulesFollowTheRequireAllSwitch() {
        show { ModulesDemo(today = today) }
        assertTagText(ModulesTags.CAN_START, string(R.string.libex_modules_can_start))

        click(ModulesTags.REQUIRE_ALL)

        assertTagText(ModulesTags.CAN_START, string(R.string.libex_modules_cannot_start))

        click(ModulesTags.MICROPHONE)

        assertTagText(ModulesTags.CAN_START, string(R.string.libex_modules_can_start))
    }

    @Test
    fun registryCheckIsConsistentOnceTheCatalogCoversTheMicrophone() {
        show { ModulesDemo(today = today) }
        assertTagText(
            ModulesTags.REGISTRY_STATUS,
            string(R.string.libex_modules_registry_issue, Manifest.permission.RECORD_AUDIO),
        )

        click(ModulesTags.CATALOG)

        assertTagText(ModulesTags.REGISTRY_STATUS, string(R.string.libex_modules_registry_ok))
    }

    @Test
    fun dayMarkersFollowTheEnabledSwitch() {
        show { ModulesDemo(today = today) }
        waitForTagText(ModulesTags.MARKERS, string(R.string.libex_modules_markers_count, 2))

        click(ModulesTags.MARKERS_SWITCH)

        waitForTagText(ModulesTags.MARKERS, string(R.string.libex_modules_markers_count, 0))
    }

    @Test
    fun registryOrdersModulesAndGroupsNavigationByCategory() = runTest {
        val registry = FeatureModuleRegistry(ModulesDemoLogic.modules())

        assertEquals(
            listOf(ModulesDemoLogic.WEATHER, ModulesDemoLogic.JOURNAL, ModulesDemoLogic.HEALTH),
            registry.all.map { it.id },
        )
        assertEquals(setOf(ModulesDemoLogic.WEATHER), registry.enabledIds().first())
        registry.module(ModulesDemoLogic.HEALTH).setEnabled(true)
        val navigation = registry.navigationEntries().first()
        assertEquals(
            listOf(ModuleCategory.TOOLS, ModuleCategory.HEALTH_CYCLE),
            navigation.map { it.category },
        )
    }

    @Test
    fun duplicateRegistrationFailsFast() {
        assertTrue(ModulesDemoLogic.duplicateIdsRejected())
    }

    @Test
    fun permissionEvaluationHonoursRequireAll() {
        assertTrue(ModulesDemoLogic.canStart(cameraGranted = true, microphoneGranted = false, requireAll = false))
        assertFalse(ModulesDemoLogic.canStart(cameraGranted = true, microphoneGranted = false, requireAll = true))
        assertFalse(ModulesDemoLogic.canStart(cameraGranted = false, microphoneGranted = false, requireAll = false))
        assertTrue(ModulesDemoLogic.canStart(cameraGranted = true, microphoneGranted = true, requireAll = true))
    }

    @Test
    fun registryCheckReportsMissingCatalogEntry() {
        val incomplete = ModulesDemoLogic.registryCheck(catalogCoversMicrophone = false)
        val complete = ModulesDemoLogic.registryCheck(catalogCoversMicrophone = true)

        assertFalse(incomplete.isConsistent)
        assertEquals(setOf(Manifest.permission.RECORD_AUDIO), incomplete.notInCatalog)
        assertTrue(complete.isConsistent)
    }

    @Test
    fun markerDatesContainDuplicatesAndAnOutlier() {
        val dates = ModulesDemoLogic.markerDates(today)

        assertEquals(4, dates.size)
        assertEquals(3, dates.toSet().size)
    }
}
