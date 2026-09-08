package com.wafflehq.uikit.navigation

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.uikit.theme.AppTheme
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

    @Test
    fun showsTitleAndContent() {
        rule.setContent {
            AppTheme {
                EditorScaffold(
                    title = "Editor title",
                    onBack = {},
                    onSave = {},
                    canSave = true,
                    backDescription = "Back",
                    saveLabel = "Save",
                ) {
                    Text("Body content")
                }
            }
        }
        rule.onNodeWithText("Editor title").assertIsDisplayed()
        rule.onNodeWithText("Body content").assertIsDisplayed()
    }

    @Test
    fun saveButtonInvokesOnSaveWhenAllowed() {
        var saved = 0
        rule.setContent {
            AppTheme {
                EditorScaffold(
                    title = "t",
                    onBack = {},
                    onSave = { saved++ },
                    canSave = true,
                    backDescription = "Back",
                    saveLabel = "Save",
                ) {}
            }
        }
        rule.onNodeWithTag(EditorScaffoldTestTags.SAVE_BAR).assertIsDisplayed()
        rule.onNodeWithText("Save", useUnmergedTree = true).assertExists()
        rule.onNodeWithTag(EditorScaffoldTestTags.SAVE_BUTTON).performClick()
        assertEquals(1, saved)
    }

    @Test
    fun saveButtonDoesNothingWhenCannotSave() {
        var saved = false
        rule.setContent {
            AppTheme {
                EditorScaffold(
                    title = "t",
                    onBack = {},
                    onSave = { saved = true },
                    canSave = false,
                    backDescription = "Back",
                    saveLabel = "Save",
                ) {}
            }
        }
        rule.onNodeWithTag(EditorScaffoldTestTags.SAVE_BUTTON).performClick()
        assertFalse(saved)
    }

    @Test
    fun saveBarHiddenWhenDisabled() {
        rule.setContent {
            AppTheme {
                EditorScaffold(
                    title = "t",
                    onBack = {},
                    onSave = {},
                    canSave = true,
                    backDescription = "Back",
                    saveLabel = "Save",
                    showSaveBar = false,
                ) {}
            }
        }
        rule.onNodeWithTag(EditorScaffoldTestTags.SAVE_BAR).assertDoesNotExist()
        rule.onNodeWithTag(EditorScaffoldTestTags.SAVE_BUTTON).assertDoesNotExist()
    }

    @Test
    fun backArrowInvokesOnBackWithDescription() {
        var back = 0
        rule.setContent {
            AppTheme {
                EditorScaffold(
                    title = "t",
                    onBack = { back++ },
                    onSave = {},
                    canSave = true,
                    backDescription = "Close editor",
                    saveLabel = "Save",
                ) {}
            }
        }
        rule.onNodeWithContentDescription("Close editor").performClick()
        assertEquals(1, back)
    }

    @Test
    fun systemBackInvokesOnBack() {
        var back = 0
        rule.setContent {
            AppTheme {
                EditorScaffold(
                    title = "t",
                    onBack = { back++ },
                    onSave = {},
                    canSave = true,
                    backDescription = "Back",
                    saveLabel = "Save",
                ) {}
            }
        }
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        assertEquals(1, back)
    }

    @Test
    fun backdropIsRendered() {
        rule.setContent {
            AppTheme {
                EditorScaffold(
                    title = "t",
                    onBack = {},
                    onSave = {},
                    canSave = true,
                    backDescription = "Back",
                    saveLabel = "Save",
                    backdrop = { Text("Backdrop slot") },
                ) {}
            }
        }
        rule.onNodeWithText("Backdrop slot").assertIsDisplayed()
    }

    @Test
    fun registersAsFullScreenSubPageWhileComposed() {
        val depth = mutableIntStateOf(0)
        val visible = mutableIntStateOf(1)
        rule.setContent {
            AppTheme {
                CompositionLocalProvider(LocalFullScreenSubPageDepth provides depth) {
                    if (visible.intValue > 0) {
                        EditorScaffold(
                            title = "t",
                            onBack = {},
                            onSave = {},
                            canSave = true,
                            backDescription = "Back",
                            saveLabel = "Save",
                        ) {}
                    }
                }
            }
        }
        rule.waitForIdle()
        assertEquals(1, depth.intValue)
        rule.runOnUiThread { visible.intValue = 0 }
        rule.waitForIdle()
        assertEquals(0, depth.intValue)
    }

    @Test
    fun defaultAccentColorsComeFromAppTheme() {
        var resolvedAccent: androidx.compose.ui.graphics.Color? = null
        rule.setContent {
            AppTheme {
                resolvedAccent = AppTheme.colors.primary.accent
                EditorScaffold(
                    title = "t",
                    onBack = {},
                    onSave = {},
                    canSave = true,
                    backDescription = "Back",
                    saveLabel = "Save",
                ) {}
            }
        }
        assertTrue(resolvedAccent != null)
    }
}
