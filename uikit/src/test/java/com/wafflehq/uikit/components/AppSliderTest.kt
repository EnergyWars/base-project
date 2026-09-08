package com.wafflehq.uikit.components

import android.app.Application
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performSemanticsAction
import com.wafflehq.uikit.theme.AppTheme
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
class AppSliderTest {

    @get:Rule
    val rule = createComposeRule()

    private val hasProgressAction = SemanticsMatcher("has SetProgress action") {
        it.config.getOrNull(SemanticsActions.SetProgress) != null
    }

    private val hasProgressBarInfo = SemanticsMatcher("has ProgressBarRangeInfo") {
        it.config.getOrNull(SemanticsProperties.ProgressBarRangeInfo) != null
    }

    @Test
    fun draggingSliderInvokesOnValueChange() {
        var value = 0.2f
        rule.setContent {
            AppTheme {
                AppSlider(value = value, onValueChange = { value = it })
            }
        }
        rule.onNode(hasProgressAction).performSemanticsAction(SemanticsActions.SetProgress) { it(0.8f) }
        assertEquals(0.8f, value, 0.001f)
    }

    @Test
    fun disabledSliderIsNotEnabled() {
        rule.setContent {
            AppTheme {
                AppSlider(value = 0.3f, onValueChange = {}, enabled = false)
            }
        }
        rule.onNode(hasProgressBarInfo).assertIsNotEnabled()
    }

    @Test
    fun steppedSliderRendersWithoutCrashing() {
        rule.setContent {
            AppTheme {
                AppSlider(value = 0.5f, onValueChange = {}, steps = 3)
            }
        }
    }
}
