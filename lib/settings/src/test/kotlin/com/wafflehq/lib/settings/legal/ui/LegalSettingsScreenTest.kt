package com.wafflehq.lib.settings.legal.ui

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.navigation.settings.SettingsHomeEntry
import com.wafflehq.lib.navigation.settings.SettingsHomePageTestTags
import com.wafflehq.lib.settings.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LegalSettingsScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun entries(clicked: MutableList<String>) = listOf(
        SettingsHomeEntry(title = "Privacy", subtitle = "How data is handled") { clicked += "Privacy" },
        SettingsHomeEntry(title = "Delete data") { clicked += "Delete data" }
    )

    @Test
    fun `the hub renders exactly the entries it was given`() {
        val clicked = mutableListOf<String>()
        rule.setContent { LegalSettingsScreen(entries = entries(clicked), onBack = {}) }

        rule.onNodeWithTag(SettingsHomePageTestTags.entry("Privacy")).assertExists()
        rule.onNodeWithTag(SettingsHomePageTestTags.entry("Delete data")).assertExists()
        rule.onNodeWithTag(SettingsHomePageTestTags.entry("Imprint")).assertDoesNotExist()
    }

    @Test
    fun `tapping an entry invokes exactly its callback`() {
        val clicked = mutableListOf<String>()
        rule.setContent { LegalSettingsScreen(entries = entries(clicked), onBack = {}) }

        rule.onNodeWithTag(SettingsHomePageTestTags.entry("Delete data")).performClick()

        assertEquals(listOf("Delete data"), clicked)
    }

    @Test
    fun `the default title comes from the library`() {
        rule.setContent { LegalSettingsScreen(entries = emptyList(), onBack = {}) }

        rule.onNodeWithText(context.getString(R.string.appsettings_legal_title)).assertExists()
    }
}
