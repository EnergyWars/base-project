package com.wafflehq.base.ui.library.demos

import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.settings.colors.ColorRamp
import com.wafflehq.lib.settings.colors.ColorRampTable
import com.wafflehq.lib.settings.colors.labelRes
import com.wafflehq.lib.settings.colors.ui.ColorContrastBadgeTestTag
import com.wafflehq.lib.settings.colors.ui.PaletteSwatchGridTestTag
import com.wafflehq.lib.settings.core.LibrarySettingsSections
import com.wafflehq.lib.settings.encryption.PasswordReminderInterval
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsDemoTest : LibraryDemoTest() {

    private fun ramp(ramp: ColorRamp) = string(ramp.labelRes)

    @Test
    fun startsWithTheBaseSapphireSwatch() {
        show { SettingsDemo() }

        node(DemoTags.section("settings")).assertExists()
        assertTagText(
            SettingsTags.SWATCH_SELECTED,
            string(R.string.libex_settings_swatch_selected, ramp(ColorRamp.SAPPHIRE), ColorRampTable.BASE_STEP),
        )
        assertTagCount(ColorContrastBadgeTestTag.ROOT, 2)
    }

    @Test
    fun selectingASwatchUpdatesTheLabel() {
        show { SettingsDemo() }

        click(PaletteSwatchGridTestTag.swatch(ColorRamp.EMERALD, 60))

        assertTagText(
            SettingsTags.SWATCH_SELECTED,
            string(R.string.libex_settings_swatch_selected, ramp(ColorRamp.EMERALD), 60),
        )
    }

    @Test
    fun reminderDropdownStoresTheChosenInterval() {
        show { SettingsDemo() }
        assertTagText(SettingsTags.REMINDER_STORED, string(R.string.libex_settings_reminder_stored, 3, "WEEKLY"))

        clickText(string(R.string.libex_settings_reminder_weekly))
        rule.onNodeWithText(string(R.string.libex_settings_reminder_daily)).performClick()

        assertTagText(SettingsTags.REMINDER_STORED, string(R.string.libex_settings_reminder_stored, 2, "DAILY"))
    }

    @Test
    fun librarySectionsOpenWithTheirId() {
        show { SettingsDemo() }

        click("libex_settings_section_${LibrarySettingsSections.ENCRYPTION}")

        assertTagText(SettingsTags.SECTION_OPENED, LibrarySettingsSections.ENCRYPTION)
    }

    @Test
    fun onboardingCollectsTheEnabledModules() {
        show { SettingsDemo() }

        click(SettingsTags.ONBOARDING_OPEN)
        rule.onNodeWithText(string(R.string.libex_modules_weather)).assertExists()
        rule.onNodeWithText(string(R.string.libex_settings_onboarding_next)).performClick()
        rule.onNodeWithText(string(R.string.libex_modules_journal)).assertExists()
        rule.onNodeWithText(string(R.string.libex_settings_onboarding_skip)).performClick()

        waitForTag(SettingsTags.ONBOARDING_RESULT)
        assertTagText(SettingsTags.ONBOARDING_RESULT, string(R.string.libex_settings_onboarding_result, 0, 2))
    }

    @Test
    fun onboardingProgressListsThePageOfTheModule() {
        show { SettingsDemo() }

        click(SettingsTags.ONBOARDING_OPEN)

        rule.onNodeWithText(string(R.string.libex_settings_onboarding_progress, 1, 2)).assertExists()
        rule.onAllNodesWithText(string(R.string.libex_settings_onboarding_progress, 2, 2)).assertCountEquals(0)
    }

    @Test
    fun everyReminderIntervalHasALabel() {
        val labels = PasswordReminderInterval.entries.map { SettingsDemoLogic.reminderLabelRes(it) }

        assertEquals(PasswordReminderInterval.entries.size, labels.toSet().size)
    }

    @Test
    fun reminderIndexRoundTripsAndFallsBackToNever() {
        assertEquals(PasswordReminderInterval.MONTHLY, SettingsDemoLogic.reminderFromIndex(PasswordReminderInterval.MONTHLY.ordinal))
        assertEquals(PasswordReminderInterval.NEVER, SettingsDemoLogic.reminderFromIndex(99))
    }

    @Test
    fun defaultSwatchIsTheBaseStepOfSapphire() {
        assertEquals(ColorRamp.SAPPHIRE to ColorRampTable.BASE_STEP, SettingsDemoLogic.DEFAULT_SWATCH)
        assertTrue(ColorRampTable.swatch(ColorRamp.SAPPHIRE, ColorRampTable.BASE_STEP) == ColorRampTable.baseSeed(ColorRamp.SAPPHIRE))
    }
}
