package com.wafflehq.lib.settings.legal.ui

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class ManualOssEntryListTest {

    @get:Rule
    val rule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val withoutText = ManualOssEntry("Play Services", "Play Services Terms of Service")
    private val withText = ManualOssEntry(
        name = "Geist",
        license = "SIL Open Font License 1.1",
        licenseTextRes = R.string.appsettings_data_deletion_confirm_message
    )

    private fun setContent() {
        rule.setContent { ManualOssEntryList(entries = listOf(withoutText, withText)) }
    }

    @Test
    fun `every manual entry is rendered with name and license`() {
        setContent()

        rule.onNodeWithTag(OpenSourceLicensesTestTags.manualEntry(withoutText.name)).assertExists()
        rule.onNodeWithTag(OpenSourceLicensesTestTags.manualEntry(withText.name)).assertExists()
        rule.onNodeWithText(withoutText.license).assertExists()
        rule.onNodeWithText(withText.license).assertExists()
        rule.onNodeWithText(context.getString(R.string.appsettings_legal_open_source_licenses_intro)).assertExists()
        rule.onNodeWithText(
            context.getString(R.string.appsettings_legal_open_source_licenses_generated_section)
        ).assertExists()
    }

    @Test
    fun `an entry with a license text toggles its full text`() {
        setContent()

        rule.onNodeWithTag(OpenSourceLicensesTestTags.manualEntryLicenseText(withText.name), useUnmergedTree = true).assertDoesNotExist()

        rule.onNodeWithTag(OpenSourceLicensesTestTags.manualEntry(withText.name)).performClick()
        rule.onNodeWithTag(OpenSourceLicensesTestTags.manualEntryLicenseText(withText.name), useUnmergedTree = true).assertExists()

        rule.onNodeWithTag(OpenSourceLicensesTestTags.manualEntry(withText.name)).performClick()
        rule.onNodeWithTag(OpenSourceLicensesTestTags.manualEntryLicenseText(withText.name), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `an entry without a license text offers no toggle`() {
        setContent()

        rule.onNodeWithTag(OpenSourceLicensesTestTags.manualEntry(withoutText.name)).performClick()

        rule.onNodeWithTag(OpenSourceLicensesTestTags.manualEntryLicenseText(withoutText.name), useUnmergedTree = true).assertDoesNotExist()
    }
}
