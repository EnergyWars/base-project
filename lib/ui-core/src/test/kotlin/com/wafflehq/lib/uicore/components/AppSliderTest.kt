package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
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
class AppSliderTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun exposesRangeAndStepsInSemantics() {
        rule.setContent {
            AppSlider(value = 6f, onValueChange = {}, valueRange = 0f..10f, steps = 4, modifier = Modifier.testTag("slider"))
        }

        rule.onNodeWithTag("slider").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(6f, 0f..10f, 4))
        )
    }

    @Test
    fun setProgressActionUpdatesValue() {
        var value by mutableFloatStateOf(0f)
        var finished = 0
        rule.setContent {
            AppSlider(
                value = value,
                onValueChange = { value = it },
                onValueChangeFinished = { finished++ },
                valueRange = 0f..100f,
                modifier = Modifier.testTag("slider")
            )
        }

        rule.onNodeWithTag("slider").performSemanticsAction(SemanticsActions.SetProgress) { it(40f) }

        rule.runOnIdle {
            assertEquals(40f, value, 0.001f)
            assertEquals(1, finished)
        }
    }

    @Test
    fun disabledSliderIsNotEnabled() {
        rule.setContent { AppSlider(value = 0.5f, onValueChange = {}, enabled = false, modifier = Modifier.testTag("slider")) }

        rule.onNodeWithTag("slider").assertIsNotEnabled()
    }

    @Test
    fun enabledSliderIsEnabled() {
        rule.setContent { AppSlider(value = 0.5f, onValueChange = {}, modifier = Modifier.testTag("slider")) }

        rule.onNodeWithTag("slider").assertIsEnabled()
    }
}
