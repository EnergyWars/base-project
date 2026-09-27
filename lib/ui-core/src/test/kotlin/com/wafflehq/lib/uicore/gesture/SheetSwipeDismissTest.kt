package com.wafflehq.lib.uicore.gesture

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
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
class SheetSwipeDismissTest {

    @get:Rule
    val rule = createComposeRule()

    private var dismissals = 0

    private fun setContent() {
        rule.setContent {
            Box(
                Modifier
                    .size(400.dp)
                    .testTag(TAG)
                    .dismissSheetOnDownwardSwipe { dismissals++ }
            )
        }
    }

    @Test
    fun `a long downward swipe dismisses the sheet`() {
        setContent()

        rule.onNodeWithTag(TAG).performTouchInput { swipeDown(startY = top, endY = bottom, durationMillis = 300L) }

        assertEquals(1, dismissals)
    }

    @Test
    fun `a short downward swipe does not dismiss the sheet`() {
        setContent()

        rule.onNodeWithTag(TAG).performTouchInput {
            swipeDown(startY = centerY, endY = centerY + 60f, durationMillis = 300L)
        }

        assertEquals(0, dismissals)
    }

    @Test
    fun `an upward swipe does not dismiss the sheet`() {
        setContent()

        rule.onNodeWithTag(TAG).performTouchInput { swipeUp(startY = bottom, endY = top, durationMillis = 300L) }

        assertEquals(0, dismissals)
    }

    private companion object {
        const val TAG = "swipe_dismiss_target"
    }
}
