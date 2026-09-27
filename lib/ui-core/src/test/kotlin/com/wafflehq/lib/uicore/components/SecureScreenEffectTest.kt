package com.wafflehq.lib.uicore.components

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SecureScreenEffectTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun effectSetsFlagSecureWhileComposedAndClearsOnDispose() {
        var show by mutableStateOf(true)
        rule.setContent {
            if (show) SecureScreenEffect()
        }

        rule.runOnIdle {
            val flags = rule.activity.window.attributes.flags
            assertEquals(
                WindowManager.LayoutParams.FLAG_SECURE,
                flags and WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        show = false

        rule.runOnIdle {
            val flags = rule.activity.window.attributes.flags
            assertEquals(0, flags and WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
