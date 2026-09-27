package com.wafflehq.base.ui.showcase

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.wafflehq.base.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ShowcaseSectionsTest {

    @get:Rule
    val rule = createComposeRule()

    private data class SectionCase(val name: String, val titleRes: Int, val content: @Composable () -> Unit)

    private val cases = listOf(
        SectionCase("01", R.string.sc_s1_title) { Section01Typography() },
        SectionCase("02", R.string.sc_s2_title) { Section02Weights() },
        SectionCase("03", R.string.sc_s3_title) { Section03Ramps() },
        SectionCase("04", R.string.sc_s4_title) { Section04Surfaces() },
        SectionCase("05", R.string.sc_s5_title) { Section05Roles() },
        SectionCase("06", R.string.sc_s6_title) { Section06Buttons() },
        SectionCase("07", R.string.sc_s7_title) { Section07Fab() },
        SectionCase("08", R.string.sc_s8_title) { Section08IconButtons() },
        SectionCase("09", R.string.sc_s9_title) { Section09Chips() },
        SectionCase("10", R.string.sc_s10_title) { Section10TextFields() },
        SectionCase("11", R.string.sc_s11_title) { Section11Cards() },
        SectionCase("12", R.string.sc_s12_title) { Section12List() },
        SectionCase("13", R.string.sc_s13_title) { Section13Selection() },
        SectionCase("14", R.string.sc_s14_title) { Section14Segmented() },
        SectionCase("16", R.string.sc_s16_title) { Section16Badges() },
        SectionCase("17", R.string.sc_s17_title) { Section17Banners() },
        SectionCase("18", R.string.sc_s18_title) { Section18SnackbarDialog() },
        SectionCase("19", R.string.sc_s19_title) { Section19Icons() },
        SectionCase("20", R.string.sc_s20_title) { Section20Dividers() },
        SectionCase("21", R.string.sc_s21_title) { Section21Spacing() },
        SectionCase("22", R.string.sc_s22_title) { Section22AppHeader() },
        SectionCase("23", R.string.sc_s23_title) { Section23SettingsList() },
        SectionCase("24", R.string.sc_s24_title) { Section24SettingsDetail() },
        SectionCase("25", R.string.sc_s25_title) { Section25FilterList() },
        SectionCase("26", R.string.sc_s26_title) { Section26DndList() },
        SectionCase("27", R.string.sc_s27_title) { Section27DeleteList() },
        SectionCase("28", R.string.sc_s28_title) { Section28PlainList() },
        SectionCase("29", R.string.sc_s29_title) { Section29GroupedList() },
        SectionCase("30", R.string.sc_s30_title) { Section30AccordionList() },
        SectionCase("31", R.string.sc_s31_title) { Section31ControlList() },
        SectionCase("32", R.string.sc_s32_title) { Section32ContainerBoxes() },
        SectionCase("33", R.string.sc_s33_title) { Section33ComboList() },
    )

    @Composable
    private fun Host(enabled: Boolean, content: @Composable () -> Unit) {
        ElementInspectorHost(enabled = enabled) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) { content() }
        }
    }

    @Test
    fun `every section renders its title`() {
        val titles = mutableMapOf<String, String>()
        rule.setContent {
            Host(enabled = true) {
                cases.forEach { case ->
                    titles[case.name] = stringResource(case.titleRes)
                    case.content()
                }
            }
        }
        rule.waitForIdle()

        cases.forEach { case ->
            val title = titles.getValue(case.name)
            assertTrue("section ${case.name} did not render", rule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty())
        }
    }

    @Test
    fun `slider and progress section renders with the animation clock paused`() {
        rule.mainClock.autoAdvance = false
        var title = ""
        rule.setContent {
            title = stringResource(R.string.sc_s15_title)
            Host(enabled = true) { Section15SliderProgress() }
        }
        rule.mainClock.advanceTimeByFrame()

        assertTrue(rule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun `settings list section stays silent while the host is disabled`() {
        rule.setContent {
            Host(enabled = false) { Section23SettingsList() }
        }

        rule.onNodeWithText("Features").performTouchInput { doubleClick() }

        rule.onNodeWithText("23a.1").assertDoesNotExist()
    }

    @Test
    fun `settings list section reports its ids when the host is enabled`() {
        rule.setContent {
            Host(enabled = true) { Section23SettingsList() }
        }

        rule.onNodeWithText("Features").performTouchInput { doubleClick() }

        rule.onNodeWithText("23a.1").assertExists()
    }

    @Test
    fun `settings detail section changes its theme selection`() {
        rule.setContent {
            Host(enabled = false) { Section24SettingsDetail() }
        }

        rule.onNodeWithText("System default").performClick()
        rule.onNodeWithText("Dark").performClick()
        rule.waitForIdle()

        assertEquals(1, rule.onAllNodesWithText("Dark").fetchSemanticsNodes().size)
    }

    @Test
    fun `dialog section shows the live dialog after the trigger is clicked`() {
        rule.setContent {
            Host(enabled = false) { Section18SnackbarDialog() }
        }
        val before = rule.onAllNodesWithText("Delete entry?").fetchSemanticsNodes().size

        rule.onNodeWithText("Open dialog").performClick()
        rule.waitForIdle()

        assertEquals(before + 1, rule.onAllNodesWithText("Delete entry?").fetchSemanticsNodes().size)
    }
}
