package com.wafflehq.lib.settings.colors.ui

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ColorTokenInfoButtonTest {

    @get:Rule
    val rule = createComposeRule()

    private val description = "Colors the default event dot."
    private val testTag = "info_button_test_tag"

    private fun setContent() {
        rule.setContent {
            ColorTokenInfoButton(description = description, testTag = testTag)
        }
    }

    @Test
    fun descriptionIsHiddenUntilTapped() {
        setContent()

        rule.onNodeWithText(description).assertDoesNotExist()
    }

    @Test
    fun tappingInfoButton_revealsDescriptionBubble() {
        setContent()

        rule.onNodeWithTag(testTag).performClick()

        rule.onNodeWithText(description).assertIsDisplayed()
    }

    @Test
    fun tappingBubble_dismissesIt() {
        setContent()

        rule.onNodeWithTag(testTag).performClick()
        rule.onNodeWithText(description).assertIsDisplayed()

        rule.onNodeWithText(description).performClick()

        rule.onNodeWithText(description).assertDoesNotExist()
    }
}
