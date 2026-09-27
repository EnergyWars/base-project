package com.wafflehq.lib.navigation.settings

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsUiTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun scaffold_showsTitleBackButtonAndActions() {
        var back = 0
        rule.setContent {
            SettingsScaffold(title = "Display", onBack = { back++ }, backDescription = "Go back", actions = { Text("Act") }) {
                Text("Body")
            }
        }
        rule.onNodeWithText("Display").assertIsDisplayed()
        rule.onNodeWithText("Act").assertIsDisplayed()
        rule.onNodeWithText("Body").assertIsDisplayed()
        rule.onNodeWithContentDescription("Go back").performClick()
        assertEquals(1, back)
    }

    @Test
    fun scaffold_truncatesLongTitleInsteadOfOverflowing() {
        val longTitle = "This is an extremely long page title that would never fit into a single top bar line"
        rule.setContent {
            SettingsScaffold(title = longTitle, onBack = null, backDescription = "Go back") { Text("Body") }
        }

        val layoutResults = mutableListOf<TextLayoutResult>()
        rule.onNodeWithText(longTitle).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layoutResults) }
        val layoutResult = layoutResults.first()
        assertEquals(1, layoutResult.lineCount)
        assertTrue(layoutResult.isLineEllipsized(0))
    }

    @Test
    fun scaffold_withoutBackHasNoBackButton() {
        rule.setContent {
            SettingsScaffold(title = "Root", onBack = null, backDescription = "Go back") { Text("Body") }
        }
        rule.onNodeWithContentDescription("Go back").assertDoesNotExist()
    }

    @Test
    fun navRow_showsTitleSubtitleAndInvokesClick() {
        var clicks = 0
        rule.setContent {
            SettingsNavRow(title = "Modules", subtitle = "Turn things on", onClick = { clicks++ }, highlighted = true)
        }
        rule.onNodeWithText("Modules").assertIsDisplayed()
        rule.onNodeWithText("Turn things on").assertIsDisplayed()
        rule.onNodeWithText("Modules").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun navRow_disabledDoesNotClick() {
        var clicks = 0
        rule.setContent {
            SettingsNavRow(title = "Off", onClick = { clicks++ }, enabled = false)
        }
        rule.onNodeWithText("Off").performClick()
        assertEquals(0, clicks)
    }

    @Test
    fun switchRow_togglesOnRowAndSwitchClick() {
        var checked by mutableStateOf(false)
        rule.setContent {
            SettingsSwitchRow(title = "Quick input", subtitle = "Sub", checked = checked, onCheckedChange = { checked = it })
        }
        rule.onNodeWithText("Quick input").performClick()
        assertTrue(checked)
        rule.onNodeWithText("Quick input").performClick()
        assertFalse(checked)
    }

    @Test
    fun switchRow_disabledDoesNotToggle() {
        var checked = false
        rule.setContent {
            SettingsSwitchRow(title = "Locked", checked = false, onCheckedChange = { checked = it }, enabled = false)
        }
        rule.onNodeWithText("Locked").performClick()
        assertFalse(checked)
    }

    @Test
    fun dropdown_opensAndSelectsOption() {
        var selected = 0
        rule.setContent {
            SettingsDropdownField(
                label = "Format",
                value = "German",
                options = listOf("German", "International"),
                selectedIndex = selected,
                onSelect = { selected = it }
            )
        }
        rule.onNodeWithText("German").performClick()
        rule.onNodeWithText("International").performClick()
        assertEquals(1, selected)
    }

    @Test
    fun dropdown_marksOnlyTheCurrentOptionAsSelected() {
        rule.setContent {
            SettingsDropdownField(
                label = "Format",
                value = "German",
                options = listOf("German", "International"),
                selectedIndex = 1,
                onSelect = {}
            )
        }
        rule.onNodeWithText("German").performClick()
        rule.onNodeWithText("International").assertIsSelected()
        rule.onAllNodesWithText("German").assertCountEquals(2)
    }

    @Test
    fun dropdown_longValueDoesNotSqueezeLabelIntoSingleLetterLines() {
        rule.setContent {
            Box(modifier = Modifier.width(300.dp)) {
                SettingsDropdownField(
                    label = "Input mode",
                    value = "Quick entry (numeric keypad, e.g. 1630 → 16:30)",
                    options = listOf("Quick entry (numeric keypad, e.g. 1630 → 16:30)", "Free text"),
                    selectedIndex = 0,
                    onSelect = {}
                )
            }
        }
        val labelWidth = rule.onNodeWithText("Input mode").fetchSemanticsNode().size.width
        val containerWidth = with(rule.density) { 300.dp.roundToPx() }
        assertTrue(labelWidth >= containerWidth * 0.3f)
    }

    @Test
    fun dropdown_longValueKeepsChevronInsideValueField() {
        rule.setContent {
            Box(modifier = Modifier.width(300.dp)) {
                SettingsDropdownField(
                    label = "Input mode",
                    value = "Quick entry (numeric keypad, e.g. 1630 → 16:30)",
                    options = listOf("Quick entry (numeric keypad, e.g. 1630 → 16:30)", "Free text"),
                    selectedIndex = 0,
                    onSelect = {}
                )
            }
        }
        val valueNode = rule.onNodeWithText("Quick entry (numeric keypad, e.g. 1630 → 16:30)").fetchSemanticsNode()
        val containerWidth = with(rule.density) { 300.dp.roundToPx() }
        assertTrue(valueNode.size.width <= containerWidth * 0.6f)
    }

    @Test
    fun dropdown_shortValueStillOpensAndShowsLabelOnOneLine() {
        rule.setContent {
            Box(modifier = Modifier.width(300.dp)) {
                SettingsDropdownField(
                    label = "Format",
                    value = "German",
                    options = listOf("German", "International"),
                    selectedIndex = 0,
                    onSelect = {}
                )
            }
        }
        rule.onNodeWithText("Format").assertIsDisplayed()
        rule.onNodeWithText("German").assertIsDisplayed()
    }

    @Test
    fun collapsibleGroup_expandsOnHeaderClick() {
        rule.setContent {
            CollapsibleSettingsGroup(label = "Details") { Text("Hidden body") }
        }
        rule.onNodeWithText("Hidden body").assertDoesNotExist()
        rule.onNodeWithText("Details").performClick()
        rule.onNodeWithText("Hidden body").assertIsDisplayed()
    }

    @Test
    fun collapsibleGroup_initiallyExpandedShowsContent() {
        rule.setContent {
            CollapsibleSettingsGroup(label = "Details", initiallyExpanded = true) { Text("Visible body") }
        }
        rule.onNodeWithText("Visible body").assertIsDisplayed()
    }

    @Test
    fun group_showsLabelWithProvidedTypography() {
        rule.setContent {
            CompositionLocalProvider(LocalSettingsTypography provides SettingsTypography(labelFontFamily = FontFamily.Serif)) {
                SettingsGroup(label = "Group label") { Text("Inside") }
            }
        }
        rule.onNodeWithText("Group label").assertIsDisplayed()
        rule.onNodeWithText("Inside").assertIsDisplayed()
    }

    @Test
    fun sectionLabel_andDividers_render() {
        rule.setContent {
            SettingsSurfaceList {
                SettingsSectionLabel("Section")
                SettingsRowDivider()
                SettingsGroupDivider()
                SettingsRowScaffold(title = "Row", subtitle = "Sub", trailing = { Text("Trail") })
            }
        }
        rule.onNodeWithText("Section").assertIsDisplayed()
        rule.onNodeWithText("Row").assertIsDisplayed()
        rule.onNodeWithText("Trail").assertIsDisplayed()
    }

    @Test
    fun sliderControl_showsLabelAndFormattedValue() {
        rule.setContent {
            SettingsSliderControl(
                label = "Size",
                valueText = { "${it.toInt()} px" },
                value = 14f,
                onValueChange = {},
                valueRange = 10f..20f
            )
        }
        rule.onNodeWithText("Size").assertIsDisplayed()
        rule.onNodeWithText("14 px").assertIsDisplayed()
    }

    @Test
    fun banner_invokesClickAndShowsTrailingIcon() {
        var clicked = false
        rule.setContent {
            SettingsBanner(
                icon = Icons.Default.Info,
                title = "Banner",
                text = "Body",
                containerColor = Color.Yellow,
                contentColor = Color.Black,
                onClick = { clicked = true },
                trailingIcon = Icons.Default.Info,
                trailingDescription = "More"
            )
        }
        rule.onNodeWithContentDescription("More").assertIsDisplayed()
        rule.onNodeWithText("Banner").performClick()
        assertTrue(clicked)
    }

    @Test
    fun listComponents_renderHeadSearchRowsAndEmpty() {
        var query by mutableStateOf("")
        rule.setContent {
            ListCard {
                ListHead(title = "Entries", count = "3")
                ListSearchField(query = query, onQuery = { query = it }, placeholder = "Search")
                ListItemRow { ListRowBody(title = "Item", sub = "Detail") }
                ListEmpty(text = "Nothing here")
            }
        }
        rule.onNodeWithText("Entries").assertIsDisplayed()
        rule.onNodeWithText("3").assertIsDisplayed()
        rule.onNodeWithText("Item").assertIsDisplayed()
        rule.onNodeWithText("Detail").assertIsDisplayed()
        rule.onNodeWithText("Nothing here").assertIsDisplayed()
        rule.onNodeWithText("Search").performTextInput("abc")
        assertEquals("abc", query)
    }

    @Test
    fun moduleIntroDialog_showsContentAndDismisses() {
        var dismissed = false
        rule.setContent {
            ModuleIntroDialog(
                icon = Icons.Default.Info,
                name = "Weight",
                description = "Track weight",
                permissionHint = "No permissions",
                dismissLabel = "Close",
                onDismiss = { dismissed = true }
            )
        }
        rule.onNodeWithText("Weight").assertIsDisplayed()
        rule.onNodeWithText("Track weight").assertIsDisplayed()
        rule.onNodeWithText("No permissions").assertIsDisplayed()
        rule.onNodeWithText("Close").performClick()
        assertTrue(dismissed)
    }

    @Test
    fun switchRow_switchReflectsCheckedState() {
        rule.setContent {
            SettingsSwitchRow(title = "On row", checked = true, onCheckedChange = {})
            SettingsSwitchRow(title = "Off row", checked = false, onCheckedChange = {})
        }
        rule.onAllNodesWithText("On row").fetchSemanticsNodes().let { assertEquals(1, it.size) }
        rule.onNodeWithText("On row").assertExists()
        rule.onNodeWithText("Off row").assertExists()
    }
}
