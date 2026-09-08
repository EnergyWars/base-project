package com.wafflehq.uikit.navigation

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
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
class FullScreenSubPageEffectTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun incrementsDepthWhileComposedAndDecrementsOnDisposal() {
        val depth = mutableIntStateOf(0)
        var visible by mutableStateOf(true)
        rule.setContent {
            CompositionLocalProvider(LocalFullScreenSubPageDepth provides depth) {
                if (visible) {
                    FullScreenSubPageEffect()
                }
            }
        }
        rule.waitForIdle()
        assertEquals(1, depth.intValue)

        visible = false
        rule.waitForIdle()
        assertEquals(0, depth.intValue)
    }

    @Test
    fun nestedEffectsAccumulateDepth() {
        val depth = mutableIntStateOf(0)
        var showSecond by mutableStateOf(true)
        rule.setContent {
            CompositionLocalProvider(LocalFullScreenSubPageDepth provides depth) {
                FullScreenSubPageEffect()
                if (showSecond) {
                    FullScreenSubPageEffect()
                }
            }
        }
        rule.waitForIdle()
        assertEquals(2, depth.intValue)

        showSecond = false
        rule.waitForIdle()
        assertEquals(1, depth.intValue)
    }
}
