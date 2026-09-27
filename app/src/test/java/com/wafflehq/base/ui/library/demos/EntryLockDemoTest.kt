package com.wafflehq.base.ui.library.demos

import android.app.Application
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.entrylock.AuthResult
import com.wafflehq.lib.entrylock.EntryLockController
import com.wafflehq.lib.entrylock.R as EntryLockR
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import androidx.compose.runtime.mutableStateListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EntryLockDemoTest : LibraryDemoTest() {

    private val baseDate = LocalDate.of(2026, 5, 4)

    private fun title(id: Int) = string(id)

    @Test
    fun lockedEntriesHideTheirTitles() {
        show { EntryLockDemo(baseDate = baseDate) }

        assertNoTagText(EntryLockTags.entry(1L), title(R.string.libex_entrylock_entry_one))
        assertNoTagText(EntryLockTags.entry(2L), title(R.string.libex_entrylock_entry_two))
        assertTagText(EntryLockTags.entry(3L), title(R.string.libex_entrylock_entry_three))
        assertTagText(EntryLockTags.SESSION, string(R.string.libex_entrylock_session_locked))
    }

    @Test
    fun tappingALockedEntryRevealsItAfterSimulatedAuthentication() {
        show { EntryLockDemo(baseDate = baseDate) }

        click(EntryLockTags.entry(1L))

        waitForTagText(EntryLockTags.entry(1L), title(R.string.libex_entrylock_entry_one))
        assertNoTagText(EntryLockTags.entry(2L), title(R.string.libex_entrylock_entry_two))
        assertTagText(EntryLockTags.AUTH_RESULT, string(R.string.libex_entrylock_result_success))
    }

    @Test
    fun sessionUnlockFromTheMenuRevealsEverything() {
        show { EntryLockDemo(baseDate = baseDate) }

        click(EntryLockTags.MENU)
        rule.onNodeWithText(string(EntryLockR.string.entry_lock_menu_session_unlock)).performClick()

        waitForTagText(EntryLockTags.SESSION, string(R.string.libex_entrylock_session_open))
        assertTagText(EntryLockTags.entry(1L), title(R.string.libex_entrylock_entry_one))
        assertTagText(EntryLockTags.entry(2L), title(R.string.libex_entrylock_entry_two))
    }

    @Test
    fun lockAllRelocksAnUnlockedEntry() {
        show { EntryLockDemo(baseDate = baseDate) }

        click(EntryLockTags.MENU)
        rule.onNodeWithText(string(EntryLockR.string.entry_lock_menu_lock_all)).performClick()

        waitUntilHidden(EntryLockTags.entry(3L), title(R.string.libex_entrylock_entry_three))
    }

    @Test
    fun selectionModeCanLockTheSelectedEntry() {
        show { EntryLockDemo(baseDate = baseDate) }

        click(EntryLockTags.MENU)
        rule.onNodeWithText(string(EntryLockR.string.entry_lock_menu_select)).performClick()
        waitForTag(EntryLockTags.checkbox(3L))
        click(EntryLockTags.checkbox(3L))
        rule.onNode(hasContentDescription(string(EntryLockR.string.entry_lock_bulk_lock))).performClick()

        waitUntilHidden(EntryLockTags.entry(3L), title(R.string.libex_entrylock_entry_three))
        assertTagCount(EntryLockTags.checkbox(3L), 0)
    }

    @Test
    fun cancelLeavesTheSelectionMode() {
        show { EntryLockDemo(baseDate = baseDate) }
        click(EntryLockTags.MENU)
        rule.onNodeWithText(string(EntryLockR.string.entry_lock_menu_select)).performClick()
        waitForTag(EntryLockTags.CANCEL_SELECTION)

        click(EntryLockTags.CANCEL_SELECTION)

        rule.waitUntil(5_000) { tagCount(EntryLockTags.checkbox(1L)) == 0 }
    }

    @Test
    fun deviceAuthenticationIsUnavailableWithoutAFragmentActivity() {
        show { EntryLockDemo(baseDate = baseDate) }

        click(EntryLockTags.DEVICE_TEST)

        waitForTagText(EntryLockTags.AUTH_RESULT, string(R.string.libex_entrylock_result_unavailable))
    }

    @Test
    fun repositoryUpdatesTheBackingEntries() = runTest {
        val entries = mutableStateListOf<DemoLockEntry>().apply { addAll(EntryLockDemoLogic.initialEntries()) }
        val repository = DemoEntryLockRepository(entries)

        repository.setLocked(setOf(3L), true)
        assertTrue(entries.all { it.locked })

        repository.setAllLocked(false)
        assertTrue(entries.none { it.locked })
    }

    @Test
    fun confirmedActionsDriveTheController() = runTest {
        val entries = mutableStateListOf<DemoLockEntry>().apply { addAll(EntryLockDemoLogic.initialEntries()) }
        val controller = EntryLockController(this, DemoEntryLockRepository(entries))

        EntryLockDemoLogic.applyConfirmed(EntryLockAction.Reveal(2L), controller)
        assertEquals(setOf(2L), controller.state.first().temporarilyUnlockedIds)

        EntryLockDemoLogic.applyConfirmed(EntryLockAction.SessionUnlock, controller)
        assertTrue(controller.state.first().sessionUnlocked)

        EntryLockDemoLogic.applyConfirmed(EntryLockAction.UnlockAll, controller)
        runCurrent()
        assertTrue(controller.state.first().temporarilyUnlockedIds.isEmpty())
        assertFalse(entries.any { it.locked })
    }

    @Test
    fun everyAuthResultHasALabel() {
        val labels = AuthResult.entries.map { EntryLockDemoLogic.authResultLabel(it) }

        assertEquals(AuthResult.entries.size, labels.toSet().size)
    }

    private fun waitUntilHidden(tag: String, text: String) {
        rule.waitUntil(5_000) { rule.onAllNodes(tagWithText(tag, text)).fetchSemanticsNodes().isEmpty() }
    }
}
