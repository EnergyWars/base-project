package com.wafflehq.lib.settings.legal.ui

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.settings.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LegalTextScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val sections = listOf(
        LegalSection(R.string.appsettings_legal_data_deletion, R.string.appsettings_data_deletion_action),
        LegalSection(R.string.appsettings_data_deletion_confirm_title, R.string.appsettings_data_deletion_retry)
    )

    @Test
    fun `every passed section is rendered with its title and body`() {
        rule.setContent {
            LegalTextScreen(title = "Legal", onBack = {}, sections = sections)
        }

        sections.forEach { section ->
            rule.onNodeWithTag(LegalTextScreenTestTags.section(section.titleRes)).assertExists()
            rule.onNodeWithText(context.getString(section.titleRes)).assertExists()
            rule.onNodeWithText(context.getString(section.bodyRes)).assertExists()
        }
    }

    @Test
    fun `an empty section list still renders the screen title`() {
        rule.setContent {
            LegalTextScreen(title = "Imprint", onBack = {}, sections = emptyList())
        }

        rule.onNodeWithTag(LegalTextScreenTestTags.CONTENT).assertExists()
        rule.onNodeWithText("Imprint").assertExists()
    }
}
