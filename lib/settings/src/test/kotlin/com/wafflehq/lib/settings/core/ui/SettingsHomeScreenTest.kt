package com.wafflehq.lib.settings.core.ui

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.core.LibrarySettingsSections
import com.wafflehq.lib.settings.core.SettingsSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
class SettingsHomeScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private fun str(id: Int) = context.getString(id)

    private fun section(
        id: String,
        order: Int,
        titleRes: Int,
        visible: kotlinx.coroutines.flow.Flow<Boolean> = flowOf(true),
        onOpen: () -> Unit = {}
    ) = SettingsSection(
        id = id,
        order = order,
        titleRes = titleRes,
        descriptionRes = R.string.appsettings_section_colors_desc,
        isVisible = visible,
        onOpen = onOpen
    )

    @Test
    fun `renders the registered sections`() {
        rule.setContent {
            SettingsHomeScreen(
                sections = listOf(
                    section("a", 10, R.string.appsettings_section_colors),
                    section("b", 20, R.string.appsettings_section_legal)
                )
            )
        }

        rule.onNodeWithText(str(R.string.appsettings_section_colors)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.appsettings_section_legal)).assertIsDisplayed()
    }

    @Test
    fun `orders sections by their order value, not by list position`() {
        rule.setContent {
            SettingsHomeScreen(
                sections = listOf(
                    section("late", 90, R.string.appsettings_section_legal),
                    section("early", 10, R.string.appsettings_section_colors)
                )
            )
        }

        val positions = listOf(
            R.string.appsettings_section_colors to
                rule.onNodeWithText(str(R.string.appsettings_section_colors)).fetchSemanticsNode().positionInRoot.y,
            R.string.appsettings_section_legal to
                rule.onNodeWithText(str(R.string.appsettings_section_legal)).fetchSemanticsNode().positionInRoot.y
        )
        assertEquals(
            listOf(R.string.appsettings_section_colors, R.string.appsettings_section_legal),
            positions.sortedBy { it.second }.map { it.first }
        )
    }

    @Test
    fun `an invisible section is left out`() {
        rule.setContent {
            SettingsHomeScreen(
                sections = listOf(
                    section("shown", 10, R.string.appsettings_section_colors),
                    section("hidden", 20, R.string.appsettings_section_legal, visible = flowOf(false))
                )
            )
        }

        rule.onNodeWithText(str(R.string.appsettings_section_colors)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.appsettings_section_legal)).assertDoesNotExist()
    }

    @Test
    fun `a section appears when its visibility flow turns on`() {
        val visible = MutableStateFlow(false)
        rule.setContent {
            SettingsHomeScreen(
                sections = listOf(
                    section("shown", 10, R.string.appsettings_section_colors),
                    section("toggling", 20, R.string.appsettings_section_legal, visible = visible)
                )
            )
        }
        rule.onNodeWithText(str(R.string.appsettings_section_legal)).assertDoesNotExist()

        visible.value = true

        rule.onNodeWithText(str(R.string.appsettings_section_legal)).assertIsDisplayed()
    }

    @Test
    fun `tapping a section opens it`() {
        var opened: String? = null
        rule.setContent {
            SettingsHomeScreen(
                sections = listOf(
                    section("a", 10, R.string.appsettings_section_colors) { opened = "a" },
                    section("b", 20, R.string.appsettings_section_legal) { opened = "b" }
                )
            )
        }

        rule.onNodeWithText(str(R.string.appsettings_section_legal)).performClick()

        rule.runOnIdle { assertEquals("b", opened) }
    }

    @Test
    fun `the library sections carry their own default labels`() {
        rule.setContent {
            SettingsHomeScreen(
                sections = listOf(
                    LibrarySettingsSections.colors(order = 10, onOpen = {}),
                    LibrarySettingsSections.encryption(order = 20, onOpen = {}),
                    LibrarySettingsSections.legal(order = 30, onOpen = {})
                )
            )
        }

        rule.onNodeWithText(str(R.string.appsettings_section_colors)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.appsettings_section_encryption)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.appsettings_section_legal)).assertIsDisplayed()
    }

    @Test
    fun `an app can override a library section's wording`() {
        val custom = LibrarySettingsSections.colors(
            order = 10,
            onOpen = {},
            titleRes = R.string.appsettings_section_encryption,
            descriptionRes = R.string.appsettings_section_legal_desc
        )

        rule.setContent { SettingsHomeScreen(sections = listOf(custom)) }

        rule.onNodeWithText(str(R.string.appsettings_section_encryption)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.appsettings_section_colors)).assertDoesNotExist()
        assertEquals(LibrarySettingsSections.COLORS, custom.id)
    }
}
