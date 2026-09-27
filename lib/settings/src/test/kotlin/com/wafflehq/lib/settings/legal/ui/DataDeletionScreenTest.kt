package com.wafflehq.lib.settings.legal.ui

import android.app.Application
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.wafflehq.lib.settings.legal.DataDeletionController
import com.wafflehq.lib.settings.legal.access.DataResetter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.junit.After
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
class DataDeletionScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun setContent(
        resetter: DataResetter = DataResetter { },
        cloudBackupHint: String? = "Cloud backups stay",
        cloudSyncAction: DataDeletionCloudSyncAction? = DataDeletionCloudSyncAction("Cloud sync") {}
    ) {
        val controller = DataDeletionController(resetter, scope)
        rule.setContent {
            DataDeletionScreen(
                controller = controller,
                explanation = "Everything local is removed",
                onBack = {},
                cloudBackupHint = cloudBackupHint,
                cloudSyncAction = cloudSyncAction
            )
        }
    }

    private fun awaitTag(tag: String) {
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `idle shows the app texts and both actions`() {
        setContent()

        rule.onNodeWithTag(DataDeletionTestTags.EXPLANATION).assertExists()
        rule.onNodeWithTag(DataDeletionTestTags.CLOUD_BACKUP_HINT).assertExists()
        rule.onNodeWithTag(DataDeletionTestTags.CLOUD_SYNC_ACTION).assertExists()
        rule.onNodeWithTag(DataDeletionTestTags.DELETE_ACTION).assertExists()
    }

    @Test
    fun `without a cloud hint and action only the deletion action remains`() {
        setContent(cloudBackupHint = null, cloudSyncAction = null)

        rule.onNodeWithTag(DataDeletionTestTags.CLOUD_BACKUP_HINT).assertDoesNotExist()
        rule.onNodeWithTag(DataDeletionTestTags.CLOUD_SYNC_ACTION).assertDoesNotExist()
        rule.onNodeWithTag(DataDeletionTestTags.DELETE_ACTION).assertExists()
    }

    @Test
    fun `deleting only starts after the confirmation`() {
        var calls = 0
        setContent(resetter = DataResetter { calls++ })

        rule.onNodeWithTag(DataDeletionTestTags.DELETE_ACTION).performClick()
        assertEquals(0, calls)

        rule.onNodeWithTag(DataDeletionTestTags.CONFIRM_ACTION).performClick()

        assertEquals(1, calls)
        awaitTag(DataDeletionTestTags.DONE)
    }

    @Test
    fun `a failing reset offers a retry`() {
        var calls = 0
        setContent(resetter = DataResetter { calls++; throw IllegalStateException("boom") })

        rule.onNodeWithTag(DataDeletionTestTags.DELETE_ACTION).performClick()
        rule.onNodeWithTag(DataDeletionTestTags.CONFIRM_ACTION).performClick()

        awaitTag(DataDeletionTestTags.ERROR)
        rule.onNodeWithTag(DataDeletionTestTags.RETRY_ACTION).performClick()
        rule.onNodeWithTag(DataDeletionTestTags.CONFIRM_ACTION).performClick()

        assertEquals(2, calls)
        awaitTag(DataDeletionTestTags.ERROR)
    }
}
