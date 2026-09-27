package com.wafflehq.lib.navigation.shell

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
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
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
class AppContentPaddingTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun fullScreenSubPage_usesZeroPadding() {
        val inner = PaddingValues(top = 48.dp, bottom = 16.dp)
        val result = appContentPadding(isFullScreenSubPage = true, innerPadding = inner)
        assertEquals(0.dp, result.calculateTopPadding())
        assertEquals(0.dp, result.calculateBottomPadding())
        assertEquals(0.dp, result.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(0.dp, result.calculateRightPadding(LayoutDirection.Ltr))
    }

    @Test
    fun regularPage_keepsScaffoldPadding() {
        val inner = PaddingValues(top = 48.dp, bottom = 16.dp)
        assertEquals(inner, appContentPadding(isFullScreenSubPage = false, innerPadding = inner))
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
                    .consumeWindowInsets(contentPadding)
            ) {
                Box(Modifier.size(10.dp).onGloballyPositioned { coordinates = it })
            }
        }
        rule.waitForIdle()
        val before = checkNotNull(coordinates)
        assertEquals(before.boundsInRoot().topLeft, before.positionInRoot())

        fullScreen = true
        rule.waitForIdle()
        val after = checkNotNull(coordinates)
        assertEquals(0f, after.positionInRoot().y)
        assertEquals(after.boundsInRoot().topLeft, after.positionInRoot())

        fullScreen = false
        rule.waitForIdle()
        val restored = checkNotNull(coordinates)
        assertEquals(restored.boundsInRoot().topLeft, restored.positionInRoot())
    }
}
