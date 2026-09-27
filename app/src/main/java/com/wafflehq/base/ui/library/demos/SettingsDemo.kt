package com.wafflehq.base.ui.library.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.wafflehq.base.R
import com.wafflehq.lib.navigation.settings.SettingsDropdownField
import com.wafflehq.lib.navigation.settings.SettingsNavRow
import com.wafflehq.lib.navigation.settings.SettingsRowDivider
import com.wafflehq.lib.navigation.settings.SettingsSurfaceList
import com.wafflehq.lib.settings.colors.ColorRamp
import com.wafflehq.lib.settings.colors.ColorRampTable
import com.wafflehq.lib.settings.colors.labelRes
import com.wafflehq.lib.settings.colors.ui.ColorContrastBadge
import com.wafflehq.lib.settings.colors.ui.ColorRampPreviewStrip
import com.wafflehq.lib.settings.colors.ui.PaletteSwatchGrid
import com.wafflehq.lib.settings.core.LibrarySettingsSections
import com.wafflehq.lib.settings.encryption.PasswordReminderInterval
import com.wafflehq.lib.settings.onboarding.OnboardingPage
import com.wafflehq.lib.settings.onboarding.ui.OnboardingFlow
import com.wafflehq.lib.settings.onboarding.ui.OnboardingModulePage
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppDialogDefaults
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.theme.AppSpacing

internal object SettingsDemoLogic {

    val DEFAULT_SWATCH: Pair<ColorRamp, Int> = ColorRamp.SAPPHIRE to ColorRampTable.BASE_STEP

    fun reminderLabelRes(interval: PasswordReminderInterval): Int = when (interval) {
        PasswordReminderInterval.NEVER -> R.string.libex_settings_reminder_never
        PasswordReminderInterval.EVERY_LAUNCH -> R.string.libex_settings_reminder_every_launch
        PasswordReminderInterval.DAILY -> R.string.libex_settings_reminder_daily
        PasswordReminderInterval.WEEKLY -> R.string.libex_settings_reminder_weekly
        PasswordReminderInterval.MONTHLY -> R.string.libex_settings_reminder_monthly
    }

    fun reminderFromIndex(index: Int): PasswordReminderInterval = PasswordReminderInterval.fromOrdinal(index)

    const val ONBOARDING_WEATHER = "weather"
    const val ONBOARDING_JOURNAL = "journal"
}

internal object SettingsTags {
    const val SWATCH_SELECTED = "libex_settings_swatch_selected"
    const val BADGE_HOST = "libex_settings_badge_host"
    const val REMINDER = "libex_settings_reminder"
    const val REMINDER_STORED = "libex_settings_reminder_stored"
    const val SECTION_OPENED = "libex_settings_section_opened"
    const val ONBOARDING_OPEN = "libex_settings_onboarding_open"
    const val ONBOARDING_RESULT = "libex_settings_onboarding_result"
}

@Composable
internal fun SettingsDemo() {
    val context = LocalContext.current
    var swatch by remember { mutableStateOf(SettingsDemoLogic.DEFAULT_SWATCH) }
    var interval by remember { mutableStateOf(PasswordReminderInterval.WEEKLY) }
    var openedSection by remember { mutableStateOf<String?>(null) }
    var showOnboarding by remember { mutableStateOf(false) }
    var onboardingSummary by remember { mutableStateOf<Map<String, Boolean>?>(null) }
    val seed = ColorRampTable.swatch(swatch.first, swatch.second)
    val sections = remember {
        listOf(
            LibrarySettingsSections.colors(order = 10, onOpen = { openedSection = LibrarySettingsSections.COLORS }),
            LibrarySettingsSections.encryption(order = 20, onOpen = { openedSection = LibrarySettingsSections.ENCRYPTION }),
            LibrarySettingsSections.legal(order = 30, onOpen = { openedSection = LibrarySettingsSections.LEGAL }),
        )
    }
    val reminderOptions = PasswordReminderInterval.entries.map { context.getString(SettingsDemoLogic.reminderLabelRes(it)) }

    DemoSection(
        id = "settings",
        titleRes = R.string.libex_settings_title,
        descriptionRes = R.string.libex_settings_desc,
        moduleRes = R.string.libex_module_settings,
    ) {
        DemoLabelText(stringResource(R.string.libex_settings_colors_heading))
        PaletteSwatchGrid(
            onSwatchSelected = { ramp, step -> swatch = ramp to step },
            modifier = Modifier.fillMaxWidth(),
        )
        DemoBodyText(
            text = stringResource(R.string.libex_settings_swatch_selected, stringResource(swatch.first.labelRes), swatch.second),
            modifier = Modifier.testTag(SettingsTags.SWATCH_SELECTED),
        )
        ColorRampPreviewStrip(seed = seed, modifier = Modifier.fillMaxWidth())
        Column(
            modifier = Modifier.fillMaxWidth().testTag(SettingsTags.BADGE_HOST),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            ColorContrastBadge(
                color = seed,
                counterpart = MaterialTheme.colorScheme.surface,
                counterpartLabel = stringResource(R.string.libex_settings_surface),
            )
            ColorContrastBadge(
                color = seed,
                counterpart = MaterialTheme.colorScheme.onSurface,
                counterpartLabel = null,
            )
        }
        AppHorizontalDivider()
        DemoLabelText(stringResource(R.string.libex_settings_reminder_heading))
        SettingsDropdownField(
            label = stringResource(R.string.libex_settings_reminder_label),
            value = reminderOptions[interval.ordinal],
            options = reminderOptions,
            selectedIndex = interval.ordinal,
            onSelect = { interval = SettingsDemoLogic.reminderFromIndex(it) },
        )
        DemoMetaText(
            text = stringResource(R.string.libex_settings_reminder_stored, interval.ordinal, interval.name),
            modifier = Modifier.testTag(SettingsTags.REMINDER_STORED),
        )
        AppHorizontalDivider()
        DemoLabelText(stringResource(R.string.libex_settings_sections_heading))
        SettingsSurfaceList {
            sections.sortedBy { it.order }.forEachIndexed { index, section ->
                if (index > 0) SettingsRowDivider()
                SettingsNavRow(
                    title = stringResource(section.titleRes),
                    subtitle = stringResource(section.descriptionRes),
                    onClick = section.onOpen,
                    modifier = Modifier.testTag("libex_settings_section_${section.id}"),
                )
            }
        }
        openedSection?.let {
            DemoMetaText(
                text = stringResource(R.string.libex_settings_section_opened, it),
                modifier = Modifier.testTag(SettingsTags.SECTION_OPENED),
            )
        }
        AppHorizontalDivider()
        AppButton(
            text = stringResource(R.string.libex_settings_onboarding_open),
            role = AppButtonRole.Primary,
            variant = AppButtonVariant.Tonal,
            onClick = { showOnboarding = true },
            modifier = Modifier.testTag(SettingsTags.ONBOARDING_OPEN),
        )
        onboardingSummary?.let { summary ->
            DemoBodyText(
                text = stringResource(
                    R.string.libex_settings_onboarding_result,
                    summary.count { it.value },
                    summary.size,
                ),
                modifier = Modifier.testTag(SettingsTags.ONBOARDING_RESULT),
            )
        }
    }

    if (showOnboarding) {
        OnboardingPreview(
            onFinish = { result ->
                onboardingSummary = result
                showOnboarding = false
            },
        )
    }
}

@Composable
private fun OnboardingPreview(onFinish: (Map<String, Boolean>) -> Unit) {
    val enabled = remember { mutableStateMapOf(SettingsDemoLogic.ONBOARDING_WEATHER to false, SettingsDemoLogic.ONBOARDING_JOURNAL to false) }
    val next = stringResource(R.string.libex_settings_onboarding_next)
    val skip = stringResource(R.string.libex_settings_onboarding_skip)
    val activate = stringResource(R.string.libex_settings_onboarding_activate)
    val weatherName = stringResource(R.string.libex_modules_weather)
    val weatherDescription = stringResource(R.string.libex_modules_weather_desc)
    val journalName = stringResource(R.string.libex_modules_journal)
    val journalDescription = stringResource(R.string.libex_modules_journal_desc)
    val progressTemplate = R.string.libex_settings_onboarding_progress
    val context = LocalContext.current

    Dialog(onDismissRequest = { onFinish(enabled.toMap()) }, properties = AppDialogDefaults.fullWidthProperties) {
        OnboardingFlow(
            pages = listOf(
                OnboardingPage(SettingsDemoLogic.ONBOARDING_WEATHER) { actions ->
                    OnboardingModulePage(
                        icon = Icons.Filled.WbSunny,
                        name = weatherName,
                        description = weatherDescription,
                        enabled = enabled.getValue(SettingsDemoLogic.ONBOARDING_WEATHER),
                        onEnabledChange = { enabled[SettingsDemoLogic.ONBOARDING_WEATHER] = it },
                        onNext = actions::next,
                        onSkip = actions::finish,
                        progressLabel = context.getString(progressTemplate, actions.pageIndex + 1, actions.pageCount),
                        activateLabel = activate,
                        nextLabel = next,
                        skipLabel = skip,
                    )
                },
                OnboardingPage(SettingsDemoLogic.ONBOARDING_JOURNAL) { actions ->
                    OnboardingModulePage(
                        icon = Icons.Filled.Book,
                        name = journalName,
                        description = journalDescription,
                        enabled = enabled.getValue(SettingsDemoLogic.ONBOARDING_JOURNAL),
                        onEnabledChange = { enabled[SettingsDemoLogic.ONBOARDING_JOURNAL] = it },
                        onNext = actions::next,
                        onSkip = actions::finish,
                        progressLabel = context.getString(progressTemplate, actions.pageIndex + 1, actions.pageCount),
                        activateLabel = activate,
                        nextLabel = next,
                        skipLabel = skip,
                    )
                },
            ),
            onFinish = { onFinish(enabled.toMap()) },
        )
    }
}
