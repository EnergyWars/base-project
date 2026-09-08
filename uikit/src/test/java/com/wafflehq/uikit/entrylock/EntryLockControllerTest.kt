package com.wafflehq.uikit.entrylock

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EntryLockControllerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeEntryLockRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeEntryLockRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun state_startsWithDefaults() = runTest {
        val controller = EntryLockController(this, repository)
        var state = EntryLockState()
        val job = launch { controller.state.collect { state = it } }
        advanceUntilIdle()

        assertEquals(EntryLockState(), state)
        job.cancel()
    }

    @Test
    fun onSessionUnlockConfirmed_setsSessionUnlockedTrue() = runTest {
        val controller = EntryLockController(this, repository)
        var state = EntryLockState()
        val job = launch { controller.state.collect { state = it } }
        advanceUntilIdle()

        controller.onSessionUnlockConfirmed()
        advanceUntilIdle()

        assertTrue(state.sessionUnlocked)
        job.cancel()
    }

    @Test
    fun onLockAllEntries_resetsSessionAndTemporaryUnlocks_andCallsRepository() = runTest {
        val controller = EntryLockController(this, repository)
        var state = EntryLockState()
        val job = launch { controller.state.collect { state = it } }
        advanceUntilIdle()
        controller.onSessionUnlockConfirmed()
        controller.onTemporaryUnlockConfirmed(1L)
        advanceUntilIdle()

        controller.onLockAllEntries()
        advanceUntilIdle()

        assertTrue(!state.sessionUnlocked)
        assertTrue(state.temporarilyUnlockedIds.isEmpty())
        assertEquals(listOf(true), repository.setAllLockedCalls)
        job.cancel()
    }

    @Test
    fun onUnlockAllEntriesConfirmed_clearsTemporaryUnlocks_andCallsRepository() = runTest {
        val controller = EntryLockController(this, repository)
        var state = EntryLockState()
        val job = launch { controller.state.collect { state = it } }
        advanceUntilIdle()
        controller.onTemporaryUnlockConfirmed(1L)
        advanceUntilIdle()

        controller.onUnlockAllEntriesConfirmed()
        advanceUntilIdle()

        assertTrue(state.temporarilyUnlockedIds.isEmpty())
        assertEquals(listOf(false), repository.setAllLockedCalls)
        job.cancel()
    }

    @Test
    fun onLongPressEntry_enablesSelectionMode_andSelectsOnlyThatId() = runTest {
        val controller = EntryLockController(this, repository)
        var state = EntryLockState()
        val job = launch { controller.state.collect { state = it } }
        advanceUntilIdle()

        controller.onLongPressEntry(42L)
        advanceUntilIdle()

        assertTrue(state.selectionMode)
        assertEquals(setOf(42L), state.selectedIds)
        job.cancel()
    }

    @Test
    fun onToggleSelected_addsThenRemovesId() = runTest {
        val controller = EntryLockController(this, repository)
        var state = EntryLockState()
        val job = launch { controller.state.collect { state = it } }
        advanceUntilIdle()

        controller.onToggleSelected(1L)
        advanceUntilIdle()
        assertEquals(setOf(1L), state.selectedIds)

        controller.onToggleSelected(2L)
        advanceUntilIdle()
        assertEquals(setOf(1L, 2L), state.selectedIds)

        controller.onToggleSelected(1L)
        advanceUntilIdle()
        assertEquals(setOf(2L), state.selectedIds)
        job.cancel()
    }

    @Test
    fun onSelectAll_replacesSelectionWithGivenIds() = runTest {
        val controller = EntryLockController(this, repository)
        var state = EntryLockState()
        val job = launch { controller.state.collect { state = it } }
        advanceUntilIdle()

        controller.onSelectAll(setOf(1L, 2L, 3L))
        advanceUntilIdle()

        assertEquals(setOf(1L, 2L, 3L), state.selectedIds)
        job.cancel()
    }

    @Test
    fun onExitSelectionMode_clearsModeAndSelection() = runTest {
        val controller = EntryLockController(this, repository)
        var state = EntryLockState()
        val job = launch { controller.state.collect { state = it } }
        advanceUntilIdle()
        controller.onLongPressEntry(5L)
        advanceUntilIdle()

        controller.onExitSelectionMode()
        advanceUntilIdle()

        assertTrue(!state.selectionMode)
        assertTrue(state.selectedIds.isEmpty())
        job.cancel()
    }

    @Test
    fun onBulkLockSelected_callsRepositoryWithSelectedIdsAndTrue_andExitsSelectionMode() = runTest {
        val controller = EntryLockController(this, repository)
        var state = EntryLockState()
        val job = launch { controller.state.collect { state = it } }
        advanceUntilIdle()
        controller.onToggleSelected(1L)
        controller.onToggleSelected(2L)
        advanceUntilIdle()

        controller.onBulkLockSelected()
        advanceUntilIdle()

        assertEquals(listOf(setOf(1L, 2L) to true), repository.setLockedCalls)
        assertTrue(!state.selectionMode)
        assertTrue(state.selectedIds.isEmpty())
        job.cancel()
    }

    @Test
    fun onBulkLockSelected_withEmptySelection_doesNotCallRepository() = runTest {
        val controller = EntryLockController(this, repository)
        controller.onBulkLockSelected()
        advanceUntilIdle()
        assertTrue(repository.setLockedCalls.isEmpty())
    }

    @Test
    fun onBulkLockSelected_removesLockedIdsFromTemporaryUnlockSet() = runTest {
        val controller = EntryLockController(this, repository)
        var state = EntryLockState()
        val job = launch { controller.state.collect { state = it } }
        advanceUntilIdle()
        controller.onTemporaryUnlockConfirmed(1L)
        controller.onToggleSelected(1L)
        advanceUntilIdle()

        controller.onBulkLockSelected()
        advanceUntilIdle()

        assertTrue(1L !in state.temporarilyUnlockedIds)
        job.cancel()
    }

    @Test
    fun onBulkUnlockSelectedConfirmed_callsRepositoryWithSelectedIdsAndFalse_andExitsSelectionMode() = runTest {
        val controller = EntryLockController(this, repository)
        var state = EntryLockState()
        val job = launch { controller.state.collect { state = it } }
        advanceUntilIdle()
        controller.onToggleSelected(7L)
        advanceUntilIdle()

        controller.onBulkUnlockSelectedConfirmed()
        advanceUntilIdle()

        assertEquals(listOf(setOf(7L) to false), repository.setLockedCalls)
        assertTrue(!state.selectionMode)
        job.cancel()
    }

    @Test
    fun onBulkUnlockSelectedConfirmed_withEmptySelection_doesNotCallRepository() = runTest {
        val controller = EntryLockController(this, repository)
        controller.onBulkUnlockSelectedConfirmed()
        advanceUntilIdle()
        assertTrue(repository.setLockedCalls.isEmpty())
    }

    @Test
    fun onTemporaryUnlockConfirmed_addsIdToTemporarilyUnlockedIds() = runTest {
        val controller = EntryLockController(this, repository)
        var state = EntryLockState()
        val job = launch { controller.state.collect { state = it } }
        advanceUntilIdle()

        controller.onTemporaryUnlockConfirmed(3L)
        advanceUntilIdle()

        assertEquals(setOf(3L), state.temporarilyUnlockedIds)
        job.cancel()
    }

    @Test
    fun onRelockEntry_removesIdFromTemporarilyUnlockedIds() = runTest {
        val controller = EntryLockController(this, repository)
        var state = EntryLockState()
        val job = launch { controller.state.collect { state = it } }
        advanceUntilIdle()
        controller.onTemporaryUnlockConfirmed(1L)
        controller.onTemporaryUnlockConfirmed(2L)
        advanceUntilIdle()

        controller.onRelockEntry(1L)
        advanceUntilIdle()

        assertEquals(setOf(2L), state.temporarilyUnlockedIds)
        job.cancel()
    }

    @Test
    fun isEntryRevealed_unlockedEntry_isAlwaysRevealed() {
        assertTrue(isEntryRevealed(isLocked = false, entryId = 1L, sessionUnlocked = false, temporarilyUnlockedIds = emptySet()))
    }

    @Test
    fun isEntryRevealed_lockedEntry_withoutUnlock_isNotRevealed() {
        assertTrue(!isEntryRevealed(isLocked = true, entryId = 1L, sessionUnlocked = false, temporarilyUnlockedIds = emptySet()))
    }

    @Test
    fun isEntryRevealed_lockedEntry_withSessionUnlock_isRevealed() {
        assertTrue(isEntryRevealed(isLocked = true, entryId = 1L, sessionUnlocked = true, temporarilyUnlockedIds = emptySet()))
    }

    @Test
    fun isEntryRevealed_lockedEntry_withMatchingTemporaryUnlock_isRevealed() {
        assertTrue(isEntryRevealed(isLocked = true, entryId = 1L, sessionUnlocked = false, temporarilyUnlockedIds = setOf(1L)))
    }

    @Test
    fun isEntryRevealed_lockedEntry_withUnrelatedTemporaryUnlock_isNotRevealed() {
        assertTrue(!isEntryRevealed(isLocked = true, entryId = 1L, sessionUnlocked = false, temporarilyUnlockedIds = setOf(2L)))
    }

    @Test
    fun entryRevealState_lockedNotUnlocked_isNotRevealedAndNotTempRevealed() {
        val result = entryRevealState(isLocked = true, sessionUnlocked = false, temporarilyUnlocked = false)
        assertTrue(!result.isRevealed)
        assertTrue(result.isLockedNow)
        assertTrue(!result.isTempRevealed)
    }

    @Test
    fun entryRevealState_lockedWithTemporaryUnlock_isRevealedAndTempRevealed() {
        val result = entryRevealState(isLocked = true, sessionUnlocked = false, temporarilyUnlocked = true)
        assertTrue(result.isRevealed)
        assertTrue(!result.isLockedNow)
        assertTrue(result.isTempRevealed)
    }

    @Test
    fun entryRevealState_sessionUnlocked_isRevealedButNotTempRevealed() {
        val result = entryRevealState(isLocked = true, sessionUnlocked = true, temporarilyUnlocked = true)
        assertTrue(result.isRevealed)
        assertTrue(!result.isTempRevealed)
    }

    @Test
    fun entryRevealState_notLocked_isRevealed() {
        val result = entryRevealState(isLocked = false, sessionUnlocked = false, temporarilyUnlocked = false)
        assertTrue(result.isRevealed)
        assertTrue(!result.isLockedNow)
        assertTrue(!result.isTempRevealed)
    }
}

private class FakeEntryLockRepository : EntryLockRepository {
    val setAllLockedCalls = mutableListOf<Boolean>()
    val setLockedCalls = mutableListOf<Pair<Set<Long>, Boolean>>()

    override suspend fun setAllLocked(locked: Boolean) {
        setAllLockedCalls += locked
    }

    override suspend fun setLocked(ids: Set<Long>, locked: Boolean) {
        setLockedCalls += ids to locked
    }
}
