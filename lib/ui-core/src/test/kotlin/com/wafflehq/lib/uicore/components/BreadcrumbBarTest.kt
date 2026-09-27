package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class BreadcrumbBarTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun rendersAllLabelsAndInvokesClick() {
        var clicked = 0
        rule.setContent {
            BreadcrumbBar(
                items = listOf(
                    BreadcrumbItem("Root", onClick = { clicked++ }),
                    BreadcrumbItem("Child")
                )
            )
        }

        rule.onNodeWithText("Root").assertIsDisplayed().assertHasClickAction().performClick()
        rule.onNodeWithText("Child").assertIsDisplayed().assertHasNoClickAction()

        assertEquals(1, clicked)
    }

    @Test
    fun singleItemHasNoSeparator() {
        rule.setContent { BreadcrumbBar(items = listOf(BreadcrumbItem("Only"))) }

        rule.onNodeWithText("Only").assertIsDisplayed()
    }

    @Test
    fun wrapsOntoMultipleLinesInsteadOfScrolling() {
        rule.setContent {
            Box(modifier = Modifier.width(200.dp)) {
                BreadcrumbBar(
                    items = listOf(
                        BreadcrumbItem("Notizen"),
                        BreadcrumbItem("Projekt Alpha"),
                        BreadcrumbItem("Unterordner"),
                        BreadcrumbItem("Sehr langer Name")
                    )
                )
            }
        }

        rule.onNodeWithText("Notizen").assertIsDisplayed()
        rule.onNodeWithText("Sehr langer Name").assertIsDisplayed()
    }
}
