package com.wafflehq.lib.settings.onboarding.ui

import com.wafflehq.lib.uicore.R as UiCoreR
import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.onboarding.ModuleOnboardingContent
import com.wafflehq.lib.settings.onboarding.ModuleOnboardingPage
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
class ModuleOnboardingScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private fun str(id: Int) = context.getString(id)
    private fun str(id: Int, vararg args: Any) = context.getString(id, *args)

    private val content = ModuleOnboardingContent(
        icon = Icons.Filled.Home,
        nameRes = R.string.appsettings_title,
        pages = listOf(
            ModuleOnboardingPage(R.string.appsettings_section_colors, R.string.appsettings_section_colors_desc),
            ModuleOnboardingPage(R.string.appsettings_section_encryption, R.string.appsettings_section_encryption_desc),
            ModuleOnboardingPage(R.string.appsettings_section_legal, R.string.appsettings_section_legal_desc),
        ),
        permissionRes = R.string.appsettings_onboarding_activate_module,
    )

    @Test
    fun `renders the first page of the given content`() {
        rule.setContent { ModuleOnboardingScreen(content = content, onBack = {}) }

        rule.onNodeWithText(str(R.string.appsettings_section_colors)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.appsettings_section_colors_desc)).assertExists()
        rule.onNodeWithText(str(R.string.appsettings_onboarding_page_progress, 1, 3)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.appsettings_onboarding_activate_module)).assertExists()
    }

    @Test
    fun `no back button on the first page`() {
        rule.setContent { ModuleOnboardingScreen(content = content, onBack = {}) }

        rule.onNodeWithText(str(UiCoreR.string.uicore_back)).assertDoesNotExist()
    }

    @Test
    fun `pages forward and back through the content`() {
        rule.setContent { ModuleOnboardingScreen(content = content, onBack = {}) }

        rule.onNodeWithText(str(R.string.appsettings_onboarding_next)).performClick()

        rule.onNodeWithText(str(R.string.appsettings_section_encryption)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.appsettings_onboarding_page_progress, 2, 3)).assertIsDisplayed()

        rule.onNodeWithText(str(UiCoreR.string.uicore_back)).performClick()

        rule.onNodeWithText(str(R.string.appsettings_section_colors)).assertIsDisplayed()
    }

    @Test
    fun `the last page finishes instead of paging on`() {
        var back = 0
        rule.setContent { ModuleOnboardingScreen(content = content, onBack = { back++ }) }

        rule.onNodeWithText(str(R.string.appsettings_onboarding_next)).performClick()
        rule.onNodeWithText(str(R.string.appsettings_onboarding_next)).performClick()

        rule.onNodeWithText(str(R.string.appsettings_section_legal)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.appsettings_onboarding_finish)).performClick()

        rule.runOnIdle { assertEquals(1, back) }
    }

    @Test
    fun `a single-page module shows the finish action right away`() {
        val single = content.copy(pages = content.pages.take(1))
        rule.setContent { ModuleOnboardingScreen(content = single, onBack = {}) }

        rule.onNodeWithText(str(R.string.appsettings_onboarding_finish)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.appsettings_onboarding_page_progress, 1, 1)).assertIsDisplayed()
    }

    @Test
    fun `skip leaves the tutorial from the first page`() {
        var back = 0
        rule.setContent { ModuleOnboardingScreen(content = content, onBack = { back++ }) }

        rule.onNodeWithTag(OnboardingTestTags.SKIP).performClick()

        rule.runOnIdle { assertEquals(1, back) }
    }

    @Test
    fun `skip is hidden on the last page`() {
        rule.setContent { ModuleOnboardingScreen(content = content, onBack = {}) }

        rule.onNodeWithText(str(R.string.appsettings_onboarding_next)).performClick()
        rule.onNodeWithText(str(R.string.appsettings_onboarding_next)).performClick()

        rule.onNodeWithTag(OnboardingTestTags.SKIP).assertDoesNotExist()
    }
}
