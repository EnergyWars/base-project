package com.wafflehq.base.ui.library.demos

import android.app.Application
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.uicore.R as UiCoreR
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
class UiCoreFeedbackDemoTest : LibraryDemoTest() {

    private fun showDemo(snackbar: SnackbarHostState = SnackbarHostState()) {
        show { UiCoreFeedbackDemo(snackbarHostState = snackbar) }
    }

    @Test
    fun emptyStateActionStartsTheCounter() {
        showDemo()
        node(DemoTags.section("uicore_feedback")).assertExists()
        rule.onNodeWithText(string(R.string.libex_uicore_empty_text)).assertExists()

        click(UiCoreFeedbackTags.EMPTY_ACTION)

        assertTagText(UiCoreFeedbackTags.COUNT, "1")
        assertTagText(UiCoreFeedbackTags.DOUBLED, "2")
        assertTagCount(UiCoreFeedbackTags.EMPTY_ACTION, 0)
    }

    @Test
    fun stepperDrivesTheStatTiles() {
        showDemo()

        stepUp(UiCoreFeedbackTags.STEPPER)
        stepUp(UiCoreFeedbackTags.STEPPER)

        assertTagText(UiCoreFeedbackTags.COUNT, "2")
        assertTagText(UiCoreFeedbackTags.DOUBLED, "4")

        stepDown(UiCoreFeedbackTags.STEPPER)
        stepDown(UiCoreFeedbackTags.STEPPER)

        assertTagCount(UiCoreFeedbackTags.COUNT, 0)
        rule.onNodeWithText(string(R.string.libex_uicore_empty_text)).assertExists()
    }

    @Test
    fun filterChipTogglesItsState() {
        showDemo()
        assertTagText(UiCoreFeedbackTags.FILTER_STATE, string(R.string.libex_value_yes))

        click(UiCoreFeedbackTags.FILTER_ONE)

        assertTagText(UiCoreFeedbackTags.FILTER_STATE, string(R.string.libex_value_no))
    }

    @Test
    fun sliderDrivesThePercentLabel() {
        showDemo()
        assertTagText(UiCoreFeedbackTags.PERCENT, string(R.string.libex_value_percent, 40))

        setSlider(UiCoreFeedbackTags.SLIDER, 0.75f)

        assertTagText(UiCoreFeedbackTags.PERCENT, string(R.string.libex_value_percent, 75))
    }

    @Test
    fun deleteDialogConfirmsAndReportsTheResult() {
        showDemo()

        click(UiCoreFeedbackTags.DELETE)
        rule.onNodeWithText(string(R.string.libex_uicore_dialog_delete_title)).assertExists()
        rule.onNodeWithText(string(UiCoreR.string.uicore_delete)).performClick()

        waitForTagText(UiCoreFeedbackTags.DIALOG_RESULT, string(R.string.libex_uicore_dialog_deleted))
    }

    @Test
    fun deleteDialogCancelLeavesNoResult() {
        showDemo()

        click(UiCoreFeedbackTags.DELETE)
        rule.onNodeWithText(string(UiCoreR.string.uicore_cancel)).performClick()

        assertTagCount(UiCoreFeedbackTags.DIALOG_RESULT, 0)
    }

    @Test
    fun renameDialogReportsTheConfirmedName() {
        showDemo()

        click(UiCoreFeedbackTags.RENAME)
        rule.onNodeWithText(string(UiCoreR.string.uicore_ok)).performClick()

        waitForTagText(
            UiCoreFeedbackTags.DIALOG_RESULT,
            string(R.string.libex_uicore_dialog_renamed, string(R.string.libex_uicore_dialog_rename_initial)),
        )
    }

    @Test
    fun errorDialogShowsTheMessageAndCanBeClosed() {
        showDemo()

        click(UiCoreFeedbackTags.ERROR)
        rule.onNodeWithText(string(R.string.libex_uicore_dialog_error_title)).assertExists()
        rule.onNodeWithText(string(R.string.libex_action_close)).performClick()

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasText(string(R.string.libex_uicore_dialog_error_title))).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun snackbarButtonShowsTheMessage() {
        val snackbar = SnackbarHostState()
        showDemo(snackbar)

        click(UiCoreFeedbackTags.SNACKBAR)

        rule.waitUntil(5_000) { snackbar.currentSnackbarData != null }
        assertEquals(string(R.string.libex_uicore_snackbar_message), snackbar.currentSnackbarData?.visuals?.message)
    }

    @Test
    fun logicComputesPercentAndDetail() {
        assertEquals(0, UiCoreFeedbackLogic.percent(-1f))
        assertEquals(50, UiCoreFeedbackLogic.percent(0.5f))
        assertEquals(100, UiCoreFeedbackLogic.percent(3f))
        assertEquals(8, UiCoreFeedbackLogic.doubled(4))
        val detail = UiCoreFeedbackLogic.errorDetail("boom")
        assertTrue(detail.contains("IllegalStateException"))
        assertTrue(detail.contains("Cause: IllegalArgumentException"))
    }
}
