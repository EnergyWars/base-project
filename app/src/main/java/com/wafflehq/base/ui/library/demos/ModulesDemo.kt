package com.wafflehq.base.ui.library.demos

import android.Manifest
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.lib.modules.FeatureModule
import com.wafflehq.lib.modules.FeatureModuleRegistry
import com.wafflehq.lib.modules.ModuleCategory
import com.wafflehq.lib.modules.ModuleNavigationEntry
import com.wafflehq.lib.modules.ModuleSettingsEntry
import com.wafflehq.lib.modules.calendar.CalendarDayMarker
import com.wafflehq.lib.modules.calendar.singleMarkerPerDate
import com.wafflehq.lib.modules.calendar.toggledCalendarDayMarkers
import com.wafflehq.lib.modules.groupedByCategory
import com.wafflehq.lib.modules.permissions.ModulePermissionSpec
import com.wafflehq.lib.modules.permissions.PermissionDefinition
import com.wafflehq.lib.modules.permissions.PermissionRegistryCheck
import com.wafflehq.lib.modules.permissions.PermissionRequirement
import com.wafflehq.lib.modules.permissions.PermissionUsage
import com.wafflehq.lib.modules.permissions.evaluate
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppBanner
import com.wafflehq.lib.uicore.components.AppBannerRole
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppLabelChip
import com.wafflehq.lib.uicore.theme.AppSpacing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.LocalDate

internal class DemoFeatureModule(
    override val id: String,
    override val settingsOrder: Int,
    override val category: ModuleCategory,
    icon: ImageVector,
    @StringRes labelRes: Int,
    @StringRes descriptionRes: Int,
    initiallyEnabled: Boolean,
) : FeatureModule {

    private val state = MutableStateFlow(initiallyEnabled)

    override val isEnabled: Flow<Boolean> = state

    override val navigation = ModuleNavigationEntry(route = id, labelRes = labelRes, icon = icon, category = category)

    override val settings = ModuleSettingsEntry(icon = icon, labelRes = labelRes, descriptionRes = descriptionRes)

    override suspend fun setEnabled(enabled: Boolean) {
        state.value = enabled
    }
}

internal object ModulesDemoLogic {

    const val WEATHER = "weather"
    const val JOURNAL = "journal"
    const val HEALTH = "health"

    fun modules(): List<FeatureModule> = listOf(
        DemoFeatureModule(
            WEATHER, 10, ModuleCategory.TOOLS, Icons.Filled.WbSunny,
            R.string.libex_modules_weather, R.string.libex_modules_weather_desc, initiallyEnabled = true,
        ),
        DemoFeatureModule(
            JOURNAL, 20, ModuleCategory.JOURNAL, Icons.Filled.Book,
            R.string.libex_modules_journal, R.string.libex_modules_journal_desc, initiallyEnabled = false,
        ),
        DemoFeatureModule(
            HEALTH, 30, ModuleCategory.HEALTH_CYCLE, Icons.Filled.Favorite,
            R.string.libex_modules_health, R.string.libex_modules_health_desc, initiallyEnabled = false,
        ),
    )

    fun duplicateIdsRejected(): Boolean {
        val duplicate = modules().first()
        return runCatching { FeatureModuleRegistry(listOf(duplicate, duplicate)) }
            .exceptionOrNull() is IllegalArgumentException
    }

    fun permissionSpec(requireAll: Boolean) = ModulePermissionSpec(
        permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
        rationaleTitleRes = R.string.libex_modules_rationale_title,
        rationaleTextRes = R.string.libex_modules_rationale_text,
        hintRes = R.string.libex_modules_rationale_hint,
        requireAll = requireAll,
    )

    fun canStart(cameraGranted: Boolean, microphoneGranted: Boolean, requireAll: Boolean): Boolean =
        permissionSpec(requireAll).evaluate(
            mapOf(
                Manifest.permission.CAMERA to cameraGranted,
                Manifest.permission.RECORD_AUDIO to microphoneGranted,
            )
        )

    fun registryCheck(catalogCoversMicrophone: Boolean): PermissionRegistryCheck.Result {
        val camera = PermissionDefinition(
            id = "camera",
            titleRes = R.string.libex_modules_perm_camera,
            requirement = PermissionRequirement.Runtime(listOf(Manifest.permission.CAMERA)),
        )
        val microphone = PermissionDefinition(
            id = "microphone",
            titleRes = R.string.libex_modules_perm_microphone,
            requirement = PermissionRequirement.Runtime(listOf(Manifest.permission.RECORD_AUDIO)),
        )
        val definitions = if (catalogCoversMicrophone) setOf(camera, microphone) else setOf(camera)
        val usages = definitions.map {
            PermissionUsage(
                permissionId = it.id,
                ownerLabelRes = R.string.libex_modules_perm_owner,
                purposeRes = R.string.libex_modules_perm_purpose,
            )
        }.toSet()
        return PermissionRegistryCheck.verify(
            manifestPermissions = setOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            definitions = definitions,
            usages = usages,
        )
    }

    fun markerDates(today: LocalDate): List<LocalDate> =
        listOf(today, today.plusDays(2), today.plusDays(2), today.plusDays(40))

    @StringRes
    fun categoryLabel(category: ModuleCategory): Int = when (category) {
        ModuleCategory.HEALTH_CYCLE -> R.string.libex_modules_category_health
        ModuleCategory.SCHEDULE_ORGANIZATION -> R.string.libex_modules_category_schedule
        ModuleCategory.JOURNAL -> R.string.libex_modules_category_journal
        ModuleCategory.TOOLS -> R.string.libex_modules_category_tools
        ModuleCategory.SCHOOL_LEARNING -> R.string.libex_modules_category_school
    }
}

internal object ModulesTags {
    const val ENABLED_IDS = "libex_modules_enabled_ids"
    const val DUPLICATE = "libex_modules_duplicate"
    const val DUPLICATE_RESULT = "libex_modules_duplicate_result"
    const val CAN_START = "libex_modules_can_start"
    const val CAMERA = "libex_modules_camera"
    const val MICROPHONE = "libex_modules_microphone"
    const val REQUIRE_ALL = "libex_modules_require_all"
    const val CATALOG = "libex_modules_catalog"
    const val REGISTRY_STATUS = "libex_modules_registry_status"
    const val MARKERS = "libex_modules_markers"
    const val MARKERS_SWITCH = "libex_modules_markers_switch"
    fun toggle(id: String) = "libex_modules_toggle_$id"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ModulesDemo(today: LocalDate = LocalDate.now()) {
    val scope = rememberCoroutineScope()
    val registry = remember { FeatureModuleRegistry(ModulesDemoLogic.modules()) }
    val enabledIds by registry.enabledIds().collectAsState(initial = emptySet())
    val navigation by registry.navigationEntries().collectAsState(initial = emptyList())
    var duplicateRejected by remember { mutableStateOf<Boolean?>(null) }
    var cameraGranted by remember { mutableStateOf(true) }
    var microphoneGranted by remember { mutableStateOf(false) }
    var requireAll by remember { mutableStateOf(false) }
    var catalogCoversMicrophone by remember { mutableStateOf(false) }
    var markersEnabled by remember { mutableStateOf(true) }
    val markersEnabledFlow = remember { MutableStateFlow(true) }
    val markers = remember(today) {
        toggledCalendarDayMarkers(markersEnabledFlow) {
            singleMarkerPerDate(
                entries = flowOf(ModulesDemoLogic.markerDates(today)),
                from = today,
                to = today.plusDays(MARKER_RANGE_DAYS),
                marker = CalendarDayMarker(sourceId = "libex"),
                dateOf = { it },
            )
        }
    }
    val markerMap by markers.collectAsState(initial = emptyMap())
    val registryCheck = remember(catalogCoversMicrophone) { ModulesDemoLogic.registryCheck(catalogCoversMicrophone) }
    val canStart = ModulesDemoLogic.canStart(cameraGranted, microphoneGranted, requireAll)

    DemoSection(
        id = "modules",
        titleRes = R.string.libex_modules_title,
        descriptionRes = R.string.libex_modules_desc,
        moduleRes = R.string.libex_module_modules,
    ) {
        registry.all.forEach { module ->
            val enabled by module.isEnabled.collectAsState(initial = false)
            val entry = module.settings
            DemoSwitchRow(
                label = if (entry != null) stringResource(entry.labelRes) else module.id,
                checked = enabled,
                onCheckedChange = { scope.launch { module.setEnabled(it) } },
                tag = ModulesTags.toggle(module.id),
            )
        }
        DemoBodyText(
            text = stringResource(R.string.libex_modules_enabled_ids, enabledIds.sorted().joinToString()),
            modifier = Modifier.testTag(ModulesTags.ENABLED_IDS),
        )
        navigation.groupedByCategory { it.category }.forEach { (category, entries) ->
            DemoMetaText(stringResource(ModulesDemoLogic.categoryLabel(category)))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                entries.forEach { AppLabelChip(text = stringResource(it.labelRes), leadingIcon = it.icon) }
            }
        }
        AppButton(
            text = stringResource(R.string.libex_modules_register_duplicate),
            role = AppButtonRole.Primary,
            variant = AppButtonVariant.Outlined,
            onClick = { duplicateRejected = ModulesDemoLogic.duplicateIdsRejected() },
            modifier = Modifier.testTag(ModulesTags.DUPLICATE),
        )
        duplicateRejected?.let { rejected ->
            AppBanner(
                text = stringResource(
                    if (rejected) R.string.libex_modules_duplicate_rejected else R.string.libex_modules_duplicate_accepted
                ),
                role = if (rejected) AppBannerRole.Secondary else AppBannerRole.Error,
                modifier = Modifier.fillMaxWidth().testTag(ModulesTags.DUPLICATE_RESULT),
            )
        }
        AppHorizontalDivider()
        DemoLabelText(stringResource(R.string.libex_modules_permissions_heading))
        DemoSwitchRow(
            label = stringResource(R.string.libex_modules_perm_camera),
            checked = cameraGranted,
            onCheckedChange = { cameraGranted = it },
            tag = ModulesTags.CAMERA,
        )
        DemoSwitchRow(
            label = stringResource(R.string.libex_modules_perm_microphone),
            checked = microphoneGranted,
            onCheckedChange = { microphoneGranted = it },
            tag = ModulesTags.MICROPHONE,
        )
        DemoSwitchRow(
            label = stringResource(R.string.libex_modules_require_all),
            checked = requireAll,
            onCheckedChange = { requireAll = it },
            tag = ModulesTags.REQUIRE_ALL,
        )
        DemoStatusPill(
            text = stringResource(if (canStart) R.string.libex_modules_can_start else R.string.libex_modules_cannot_start),
            tone = if (canStart) DemoTone.Success else DemoTone.Warning,
            modifier = Modifier.testTag(ModulesTags.CAN_START),
        )
        DemoSwitchRow(
            label = stringResource(R.string.libex_modules_catalog_covers),
            checked = catalogCoversMicrophone,
            onCheckedChange = { catalogCoversMicrophone = it },
            tag = ModulesTags.CATALOG,
        )
        AppBanner(
            text = if (registryCheck.isConsistent) {
                stringResource(R.string.libex_modules_registry_ok)
            } else {
                stringResource(R.string.libex_modules_registry_issue, registryCheck.notInCatalog.sorted().joinToString())
            },
            role = if (registryCheck.isConsistent) AppBannerRole.Secondary else AppBannerRole.Warning,
            modifier = Modifier.fillMaxWidth().testTag(ModulesTags.REGISTRY_STATUS),
        )
        AppHorizontalDivider()
        DemoSwitchRow(
            label = stringResource(R.string.libex_modules_markers_enabled),
            checked = markersEnabled,
            onCheckedChange = {
                markersEnabled = it
                markersEnabledFlow.value = it
            },
            tag = ModulesTags.MARKERS_SWITCH,
        )
        DemoBodyText(
            text = stringResource(R.string.libex_modules_markers_count, markerMap.size),
            modifier = Modifier.testTag(ModulesTags.MARKERS),
        )
    }
}

private const val MARKER_RANGE_DAYS = 30L
