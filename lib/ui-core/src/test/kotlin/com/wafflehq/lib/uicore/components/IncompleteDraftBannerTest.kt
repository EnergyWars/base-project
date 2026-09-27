package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.uicore.R
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
class IncompleteDraftBannerTest {

    @get:Rule
    val rule = createComposeRule()

    private val discardLabel =
        ApplicationProvider.getApplicationContext<Application>().getString(R.string.uicore_draft_banner_discard)

    @Test
    fun messageIsDisplayed() {
        rule.setContent { IncompleteDraftBanner(message = "Unfinished draft", onResume = {}, onDiscard = {}) }

        rule.onNodeWithText("Unfinished draft").assertIsDisplayed()
    }

    @Test
    fun tappingBannerResumesOnly() {
        var resumed = 0
        var discarded = 0
        rule.setContent {
            IncompleteDraftBanner(message = "Unfinished draft", onResume = { resumed++ }, onDiscard = { discarded++ })
        }

        rule.onNodeWithText("Unfinished draft").performClick()

        assertEquals(1, resumed)
        assertEquals(0, discarded)
    }

    @Test
    fun tappingDiscardDiscardsOnly() {
        var resumed = 0
        var discarded = 0
        rule.setContent {
            IncompleteDraftBanner(message = "Unfinished draft", onResume = { resumed++ }, onDiscard = { discarded++ })
        }

        rule.onNodeWithContentDescription(discardLabel).performClick()

        assertEquals(0, resumed)
        assertEquals(1, discarded)
    }
}
