package com.wafflehq.lib.folders.ui

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FolderEntryPartsTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun folderEntryTitle_rendersText() {
        rule.setContent { MaterialTheme { FolderEntryTitle("Spaghetti") } }

        rule.onNodeWithText("Spaghetti").assertIsDisplayed()
    }

    @Test
    fun folderMetaLine_rendersTextWithAndWithoutIcon() {
        rule.setContent {
            MaterialTheme {
                FolderMetaLine(text = "Mit Icon", icon = Icons.Default.Schedule)
                FolderMetaLine(text = "Ohne Icon")
            }
        }

        rule.onNodeWithText("Mit Icon").assertIsDisplayed()
        rule.onNodeWithText("Ohne Icon").assertIsDisplayed()
    }

    @Test
    fun folderMetaFlow_rendersAllChildren() {
        rule.setContent {
            MaterialTheme {
                FolderMetaFlow {
                    Text("A")
                    Text("B")
                    Text("C")
                }
            }
        }

        rule.onNodeWithText("A").assertIsDisplayed()
        rule.onNodeWithText("B").assertIsDisplayed()
        rule.onNodeWithText("C").assertIsDisplayed()
    }

    @Test
    fun folderTagRow_rendersEveryTag() {
        rule.setContent { MaterialTheme { FolderTagRow(tags = listOf("vegan", "schnell", "Pasta")) } }

        rule.onNodeWithText("vegan").assertIsDisplayed()
        rule.onNodeWithText("schnell").assertIsDisplayed()
        rule.onNodeWithText("Pasta").assertIsDisplayed()
    }

    @Test
    fun folderTagRow_withoutTagsRendersNothing() {
        rule.setContent { MaterialTheme { FolderTagRow(tags = emptyList()) } }

        rule.onAllNodesWithText("vegan").assertCountEquals(0)
    }

    @Test
    fun folderIconTile_rendersEmojiOrIcon() {
        rule.setContent {
            MaterialTheme {
                FolderIconTile(accent = Color.Red, emoji = "🎸")
                FolderIconTile(accent = Color.Blue, icon = Icons.Default.Restaurant)
                FolderIconTile(accent = Color.Green)
            }
        }

        rule.onNodeWithText("🎸").assertIsDisplayed()
    }

    @Test
    fun folderEntryCard_withLeadingEmojiShowsItNextToContent() {
        rule.setContent {
            MaterialTheme {
                FolderEntryCard<String>(
                    dragState = null,
                    item = "x",
                    dragDescription = "drag",
                    onClick = {},
                    leadingEmoji = "🎸"
                ) { FolderEntryTitle("Gitarre") }
            }
        }

        rule.onNodeWithText("🎸").assertIsDisplayed()
        rule.onNodeWithText("Gitarre").assertIsDisplayed()
        rule.onNodeWithTag(FolderTestTags.ENTRY_CARD).assertIsDisplayed()
    }

    @Test
    fun folderEntryCard_withLeadingIconAndAccentRendersContent() {
        rule.setContent {
            MaterialTheme {
                FolderEntryCard<String>(
                    dragState = null,
                    item = "x",
                    dragDescription = "drag",
                    onClick = {},
                    leadingIcon = Icons.Default.Restaurant,
                    accentColor = Color.Magenta
                ) { FolderEntryTitle("Rezept") }
            }
        }

        rule.onNodeWithText("Rezept").assertIsDisplayed()
    }
}
