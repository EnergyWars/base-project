package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
class FullScreenSubPageTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun effectIncrementsDepthWhileComposedAndDecrementsOnDispose() {
        val depth = mutableIntStateOf(0)
        var show by mutableStateOf(true)
        rule.setContent {
            CompositionLocalProvider(LocalFullScreenSubPageDepth provides depth) {
                if (show) FullScreenSubPageEffect()
            }
        }

        rule.runOnIdle { assertEquals(1, depth.intValue) }

        show = false

        rule.runOnIdle { assertEquals(0, depth.intValue) }
    }

    @Test
    fun nestedEffectsAccumulate() {
        val depth = mutableIntStateOf(0)
        rule.setContent {
            CompositionLocalProvider(LocalFullScreenSubPageDepth provides depth) {
                FullScreenSubPageEffect()
                FullScreenSubPageEffect()
            }
        }

        rule.runOnIdle { assertEquals(2, depth.intValue) }
    }

    @Test
    fun defaultLocalStartsAtZero() {
        var observed = -1
        rule.setContent { observed = LocalFullScreenSubPageDepth.current.intValue }

        rule.runOnIdle { assertEquals(0, observed) }
    }
}
