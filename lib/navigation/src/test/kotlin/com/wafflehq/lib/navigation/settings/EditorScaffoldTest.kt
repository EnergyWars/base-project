package com.wafflehq.lib.navigation.settings

import com.wafflehq.lib.uicore.R as UiCoreR
import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.navigation.R
import com.wafflehq.lib.uicore.components.LocalFullScreenSubPageDepth
import com.wafflehq.lib.uicore.components.LocalReservedBottomInset
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
class EditorScaffoldTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun showsTitleAndContent() {
        rule.setContent {
            EditorScaffold(title = "Editor title", onBack = {}, onSave = {}, canSave = true) {
                Text("Body content")
            }
        }
        rule.onNodeWithText("Editor title").assertIsDisplayed()
        rule.onNodeWithText("Body content").assertIsDisplayed()
    }

    @Test
    fun saveButtonUsesDefaultLabelAndInvokesOnSaveWhenAllowed() {
        var saved = 0
        rule.setContent {
            EditorScaffold(title = "t", onBack = {}, onSave = { saved++ }, canSave = true) {}
        }
        rule.onNodeWithTag(EditorScaffoldTestTags.SAVE_BAR).assertIsDisplayed()
        rule.onNodeWithText(context.getString(UiCoreR.string.uicore_save), useUnmergedTree = true).assertExists()
        rule.onNodeWithTag(EditorScaffoldTestTags.SAVE_BUTTON).performClick()
        assertEquals(1, saved)
    }

    @Test
    fun saveButtonDoesNothingWhenCannotSave() {
        var saved = false
        rule.setContent {
            EditorScaffold(title = "t", onBack = {}, onSave = { saved = true }, canSave = false) {}
        }
        rule.onNodeWithTag(EditorScaffoldTestTags.SAVE_BUTTON).performClick()
        assertFalse(saved)
    }

    @Test
    fun customSaveLabelIsShown() {
        rule.setContent {
            EditorScaffold(title = "t", onBack = {}, onSave = {}, canSave = true, saveLabel = "Add drink") {}
        }
        rule.onNodeWithText("Add drink", useUnmergedTree = true).assertExists()
    }

    @Test
    fun saveBarHiddenWhenDisabled() {
        rule.setContent {
            EditorScaffold(title = "t", onBack = {}, onSave = {}, canSave = true, showSaveBar = false) {}
        }
        rule.onNodeWithTag(EditorScaffoldTestTags.SAVE_BAR).assertDoesNotExist()
        rule.onNodeWithTag(EditorScaffoldTestTags.SAVE_BUTTON).assertDoesNotExist()
    }

    @Test
    fun contentReceivesReservedBottomInsetMatchingSaveBarHeight() {
        var capturedInset: Dp? = null
        rule.setContent {
            EditorScaffold(title = "t", onBack = {}, onSave = {}, canSave = true) {
                capturedInset = LocalReservedBottomInset.current
            }
        }
        rule.waitForIdle()
        val barHeightPx = rule.onNodeWithTag(EditorScaffoldTestTags.SAVE_BAR).fetchSemanticsNode().size.height
        val barHeight = with(rule.density) { barHeightPx.toDp() }
        assertEquals(barHeight.value, (capturedInset ?: 0.dp).value, 1f)
    }

    @Test
    fun contentReceivesNoReservedBottomInsetWhenSaveBarHidden() {
        var capturedInset: Dp? = null
        rule.setContent {
            EditorScaffold(title = "t", onBack = {}, onSave = {}, canSave = true, showSaveBar = false) {
                capturedInset = LocalReservedBottomInset.current
            }
        }
        rule.waitForIdle()
        assertEquals(0.dp, capturedInset)
    }

    @Test
    fun backArrowInvokesOnBackWithDefaultDescription() {
        var back = 0
        rule.setContent {
            EditorScaffold(title = "t", onBack = { back++ }, onSave = {}, canSave = true) {}
        }
        rule.onNodeWithContentDescription(context.getString(R.string.navigation_navigate_back)).performClick()
        assertEquals(1, back)
    }

    @Test
    fun backArrowUsesCustomDescription() {
        var back = false
        rule.setContent {
            EditorScaffold(title = "t", onBack = { back = true }, onSave = {}, canSave = true, backDescription = "Close me") {}
        }
        rule.onNodeWithContentDescription("Close me").performClick()
        assertTrue(back)
    }

    @Test
    fun systemBackInvokesOnBack() {
        var back = 0
        rule.setContent {
            EditorScaffold(title = "t", onBack = { back++ }, onSave = {}, canSave = true) {}
        }
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        assertEquals(1, back)
    }

    @Test
    fun actionsAndBackdropAreRendered() {
        rule.setContent {
            EditorScaffold(
                title = "t",
                onBack = {},
                onSave = {},
                canSave = true,
                actions = { Text("Action slot") },
                backdrop = { Text("Backdrop slot") }
            ) {}
        }
        rule.onNodeWithText("Action slot").assertIsDisplayed()
        rule.onNodeWithText("Backdrop slot").assertIsDisplayed()
    }

    @Test
    fun registersAsFullScreenSubPageWhileComposed() {
        val depth = mutableIntStateOf(0)
        val visible = mutableIntStateOf(1)
        rule.setContent {
            CompositionLocalProvider(LocalFullScreenSubPageDepth provides depth) {
                if (visible.intValue > 0) {
                    EditorScaffold(title = "t", onBack = {}, onSave = {}, canSave = true) {}
                }
            }
        }
        rule.waitForIdle()
        assertEquals(1, depth.intValue)
        rule.runOnUiThread { visible.intValue = 0 }
        rule.waitForIdle()
        assertEquals(0, depth.intValue)
    }
}
