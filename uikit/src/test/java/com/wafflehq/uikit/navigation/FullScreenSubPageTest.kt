package com.wafflehq.uikit.navigation

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
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
    fun defaultLocalStartsAtZero() {
        var observed = -1
        rule.setContent { observed = LocalFullScreenSubPageDepth.current.intValue }

        rule.runOnIdle { assertEquals(0, observed) }
    }

    @Test
    fun togglingFullScreenSubPage_keepsCachedRootPositionInSyncWithLayoutChain() {
        var fullScreen by mutableStateOf(false)
        var coordinates: LayoutCoordinates? = null
        rule.setContent {
            val contentPadding = appContentPadding(fullScreen, PaddingValues(top = 50.dp))
            Column(
                Modifier
                    .padding(contentPadding)
                    .consumeWindowInsets(contentPadding),
            ) {
                Box(Modifier.size(10.dp).onGloballyPositioned { coordinates = it })
            }
        }
        rule.waitForIdle()
        val before = coordinates!!
        assertEquals(before.boundsInRoot().topLeft, before.positionInRoot())

        fullScreen = true
        rule.waitForIdle()
        val after = coordinates!!
        assertEquals(0f, after.positionInRoot().y)
        assertEquals(after.boundsInRoot().topLeft, after.positionInRoot())

        fullScreen = false
        rule.waitForIdle()
        val restored = coordinates!!
        assertEquals(restored.boundsInRoot().topLeft, restored.positionInRoot())
    }
}
