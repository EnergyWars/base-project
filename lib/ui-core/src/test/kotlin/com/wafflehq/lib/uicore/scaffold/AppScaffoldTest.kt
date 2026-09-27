package com.wafflehq.lib.uicore.scaffold

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppScaffoldTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `renders title actions content fab snackbar and bottom bar`() {
        rule.setContent {
            MaterialTheme {
                AppScaffold(
                    title = "Weight",
                    onBack = null,
                    actions = { Text("action", Modifier.testTag("action")) },
                    bottomBar = { Text("bottom", Modifier.testTag("bottom")) },
                    snackbarHost = { Text("snack", Modifier.testTag("snack")) },
                    floatingActionButton = { Text("fab", Modifier.testTag("fab")) },
                ) { padding ->
                    Box(Modifier.fillMaxSize().testTag("content")) { Text("body $padding") }
                }
            }
        }

        rule.onNodeWithTag(AppScaffoldTestTags.TITLE).assertTextEquals("Weight")
        listOf("action", "bottom", "snack", "fab", "content").forEach { rule.onNodeWithTag(it).assertIsDisplayed() }
    }

    @Test
    fun `back button is shown and invokes onBack`() {
        var backs = 0
        rule.setContent {
            MaterialTheme { AppScaffold(title = "T", onBack = { backs++ }, content = {}) }
        }

        rule.onNodeWithTag(AppScaffoldTestTags.BACK).performClick()

        assertTrue(backs == 1)
    }

    @Test
    fun `back button is absent without onBack`() {
        rule.setContent {
            MaterialTheme { AppScaffold(title = "T", onBack = null, content = {}) }
        }

        rule.onNodeWithTag(AppScaffoldTestTags.BACK).assertDoesNotExist()
    }

    @Test
    fun `back button carries the default description`() {
        rule.setContent {
            MaterialTheme { AppScaffold(title = "T", onBack = {}, content = {}) }
        }

        rule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun `custom title content replaces the text title`() {
        rule.setContent {
            MaterialTheme {
                AppScaffold(titleContent = { Text("custom", Modifier.testTag("custom")) }, onBack = null, content = {})
            }
        }

        rule.onNodeWithTag("custom").assertIsDisplayed()
        rule.onNodeWithTag(AppScaffoldTestTags.TITLE).assertDoesNotExist()
    }

    @Test
    fun `top bar overload renders the given bar without the standard title bar`() {
        rule.setContent {
            MaterialTheme {
                AppScaffold(topBar = { Text("own bar", Modifier.testTag("own")) }, content = {})
            }
        }

        rule.onNodeWithTag("own").assertIsDisplayed()
        rule.onNodeWithTag(AppScaffoldTestTags.TOP_BAR).assertDoesNotExist()
    }

    @Test
    fun `standalone top bar renders navigation title and actions`() {
        rule.setContent {
            MaterialTheme {
                AppTopBar(
                    title = { Text("Standalone", Modifier.testTag("title")) },
                    navigation = { Text("nav", Modifier.testTag("nav")) },
                    actions = { Text("act", Modifier.testTag("act")) },
                )
            }
        }

        listOf("title", "nav", "act", AppScaffoldTestTags.TOP_BAR).forEach { rule.onNodeWithTag(it).assertIsDisplayed() }
    }
}
