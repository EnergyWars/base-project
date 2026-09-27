package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Dp
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
class ReservedBottomInsetTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun defaultLocalIsZero() {
        var observed: Dp? = null
        rule.setContent { observed = LocalReservedBottomInset.current }

        rule.runOnIdle { assertEquals(0.dp, observed) }
    }

    @Test
    fun reportsDistanceFromElementTopToRootBottomEdge() {
        var reported = Dp.Unspecified
        rule.setContent {
            Box(modifier = Modifier.height(200.dp)) {
                Column {
                    Spacer(modifier = Modifier.height(150.dp))
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .reportReservedBottomInset { reported = it }
                    )
                }
            }
        }

        rule.runOnIdle { assertEquals(50f, reported.value, 1f) }
    }

    @Test
    fun reportsFullRootHeightWhenElementSitsAtTopEdge() {
        var reported = Dp.Unspecified
        rule.setContent {
            Box(modifier = Modifier.height(200.dp)) {
                Column {
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .reportReservedBottomInset { reported = it }
                    )
                    Spacer(modifier = Modifier.height(150.dp))
                }
            }
        }

        rule.runOnIdle { assertEquals(200f, reported.value, 1f) }
    }

    @Test
    fun updatesReactivelyWhenElementPositionChanges() {
        var spacerHeight by mutableStateOf(150.dp)
        var reported = Dp.Unspecified
        rule.setContent {
            Box(modifier = Modifier.height(200.dp)) {
                Column {
                    Spacer(modifier = Modifier.height(spacerHeight))
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .reportReservedBottomInset { reported = it }
                    )
                }
            }
        }

        rule.runOnIdle { assertEquals(50f, reported.value, 1f) }

        spacerHeight = 100.dp

        rule.runOnIdle { assertEquals(100f, reported.value, 1f) }
    }

    @Test
    fun `reservedBottomInsetPx excludes the safe-drawing inset from the raw distance`() {
        val reserved = reservedBottomInsetPx(rootHeightPx = 2000, topInRootPx = 1500f, safeDrawingBottomPx = 300)

        assertEquals(200f, reserved, 0.1f)
    }

    @Test
    fun `reservedBottomInsetPx matches the raw distance when there is no safe-drawing inset`() {
        val reserved = reservedBottomInsetPx(rootHeightPx = 2000, topInRootPx = 1500f, safeDrawingBottomPx = 0)

        assertEquals(500f, reserved, 0.1f)
    }

    @Test
    fun `reservedBottomInsetPx never goes negative when the element sits within the inset itself`() {
        val reserved = reservedBottomInsetPx(rootHeightPx = 2000, topInRootPx = 1900f, safeDrawingBottomPx = 300)

        assertEquals(0f, reserved, 0.1f)
    }
}
