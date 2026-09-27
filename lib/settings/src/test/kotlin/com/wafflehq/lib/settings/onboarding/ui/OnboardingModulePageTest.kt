package com.wafflehq.lib.settings.onboarding.ui

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.settings.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OnboardingModulePageTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private fun str(id: Int) = context.getString(id)

    @Test
    fun `shows the module data it was given`() {
        rule.setContent {
            OnboardingModulePage(
                icon = Icons.Filled.Home,
                name = "Weight tracker",
                description = "Track your weight",
                enabled = false,
                onEnabledChange = {},
                onNext = {},
                onSkip = {},
                permissionHint = "Needs body sensors",
                progressLabel = "Module 3 of 11"
            )
        }

        rule.onNodeWithText("Weight tracker").assertIsDisplayed()
        rule.onNodeWithText("Track your weight").assertIsDisplayed()
        rule.onNodeWithText("Needs body sensors").assertIsDisplayed()
        rule.onNodeWithText("Module 3 of 11").assertIsDisplayed()
        rule.onNodeWithText(str(R.string.appsettings_onboarding_activate_module)).assertIsDisplayed()
    }

    @Test
    fun `leaves out the optional hint and progress label`() {
        rule.setContent {
            OnboardingModulePage(
                icon = Icons.Filled.Home,
                name = "Notes",
                description = "Write notes",
                enabled = false,
                onEnabledChange = {},
                onNext = {},
                onSkip = {}
            )
        }

        rule.onNodeWithText("Notes").assertIsDisplayed()
        rule.onNodeWithText("Needs body sensors").assertDoesNotExist()
    }

    @Test
    fun `toggling the switch row reports the new state`() {
        var toggled: Boolean? = null
        rule.setContent {
            OnboardingModulePage(
                icon = Icons.Filled.Home,
                name = "Notes",
                description = "Write notes",
                enabled = false,
                onEnabledChange = { toggled = it },
                onNext = {},
                onSkip = {}
            )
        }

        rule.onNodeWithText(str(R.string.appsettings_onboarding_activate_module)).performClick()

        rule.runOnIdle { assertEquals(true, toggled) }
    }

    @Test
    fun `the two actions are wired separately`() {
        var next = 0
        var skip = 0
        rule.setContent {
            OnboardingModulePage(
                icon = Icons.Filled.Home,
                name = "Notes",
                description = "Write notes",
                enabled = true,
                onEnabledChange = {},
                onNext = { next++ },
                onSkip = { skip++ }
            )
        }

        rule.onNodeWithText(str(R.string.appsettings_onboarding_next)).performClick()
        rule.onNodeWithText(str(R.string.appsettings_onboarding_skip)).performClick()

        rule.runOnIdle {
            assertEquals(1, next)
            assertEquals(1, skip)
        }
    }
}
