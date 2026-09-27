package com.wafflehq.base.ui.library.demos

import android.app.Application
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.drafts.StoredDraft
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftsDemoTest : LibraryDemoTest() {

    @Test
    fun startsWithoutADraft() {
        show { DraftsDemo(repository = InMemoryDraftRepository()) }

        assertTagText(DraftsTags.STATUS, string(R.string.libex_drafts_none))
        node(DraftsTags.RESTORE).assertIsNotEnabled()
        node(DraftsTags.DISCARD).assertIsNotEnabled()
    }

    @Test
    fun saveNowStoresTheTypedText() {
        val repository = InMemoryDraftRepository(clock = { 1_000L })
        show { DraftsDemo(repository = repository) }

        node(DraftsTags.INPUT).performScrollTo().performTextInput("waffle")
        click(DraftsTags.SAVE)
        rule.waitUntil(5_000) { repository.current != null }

        assertEquals("waffle", repository.current?.payload)
        node(DraftsTags.RESTORE).assertIsEnabled()
        node(DraftsTags.DISCARD).assertIsEnabled()
    }

    @Test
    fun autosaveStoresTheTextAfterTheInterval() {
        val repository = InMemoryDraftRepository(clock = { 2_000L })
        show { DraftsDemo(intervalMs = 50L, repository = repository) }

        node(DraftsTags.INPUT).performScrollTo().performTextInput("autosaved")
        rule.waitUntil(5_000) { repository.current?.payload == "autosaved" }

        assertEquals("autosaved", repository.current?.payload)
    }

    @Test
    fun disablingAutosaveKeepsTheRepositoryEmpty() {
        val repository = InMemoryDraftRepository()
        show { DraftsDemo(intervalMs = 20L, repository = repository) }

        click(DraftsTags.SWITCH)
        node(DraftsTags.INPUT).performScrollTo().performTextInput("quiet")
        rule.mainClock.autoAdvance = false
        rule.mainClock.advanceTimeBy(500L)
        rule.waitForIdle()

        assertNull(repository.current)
    }

    @Test
    fun discardClearsTheStoredDraft() {
        val repository = InMemoryDraftRepository()
        show { DraftsDemo(repository = repository) }
        node(DraftsTags.INPUT).performScrollTo().performTextInput("gone")
        click(DraftsTags.SAVE)
        rule.waitUntil(5_000) { repository.current != null }

        click(DraftsTags.DISCARD)
        rule.waitUntil(5_000) { repository.current == null }

        assertTagText(DraftsTags.STATUS, string(R.string.libex_drafts_none))
    }

    @Test
    fun restoreCopiesTheDraftBackIntoTheField() {
        val repository = InMemoryDraftRepository()
        show { DraftsDemo(repository = repository) }
        node(DraftsTags.INPUT).performScrollTo().performTextInput("first")
        click(DraftsTags.SAVE)
        rule.waitUntil(5_000) { repository.current != null }
        replaceText(DraftsTags.INPUT, "changed")

        click(DraftsTags.RESTORE)

        rule.waitUntil(5_000) { hasFieldText("first") }
        assertTrue(hasFieldText("first"))
    }

    @Test
    fun repositoryEmitsTheSavedDraft() = runTest {
        val repository = InMemoryDraftRepository(clock = { 77L })

        repository.save(entryId = 5L, payload = "abc")

        val stored: StoredDraft<String>? = repository.observe().first()
        assertEquals(5L, stored?.entryId)
        assertEquals("abc", stored?.payload)
        assertEquals(77L, stored?.updatedAtEpochMs)
        repository.clear()
        assertNull(repository.current)
    }

    @Test
    fun shouldSaveOnlyForNewNonBlankText() {
        val stored = StoredDraft<String>(null, "same", 1L)

        assertFalse(DraftsDemoLogic.shouldSave("", null))
        assertFalse(DraftsDemoLogic.shouldSave("   ", null))
        assertFalse(DraftsDemoLogic.shouldSave("same", stored))
        assertTrue(DraftsDemoLogic.shouldSave("other", stored))
        assertTrue(DraftsDemoLogic.shouldSave("first", null))
    }

    private fun hasFieldText(text: String): Boolean =
        rule.onAllNodes(hasTestTag(DraftsTags.INPUT) and hasText(text))
            .fetchSemanticsNodes().isNotEmpty()
}
