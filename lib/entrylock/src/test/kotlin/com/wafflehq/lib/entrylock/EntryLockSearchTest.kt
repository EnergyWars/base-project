package com.wafflehq.lib.entrylock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EntryLockSearchTest {

    private data class Item(val id: Long, val locked: Boolean)

    private val open = Item(1, false)
    private val locked = Item(2, true)
    private val otherLocked = Item(3, true)
    private val all = listOf(open, locked, otherLocked)

    private fun List<Item>.reveal(state: EntryLockState) = filterRevealed(state, { it.locked }, { it.id })

    @Test
    fun emptyListStaysEmpty() {
        assertTrue(emptyList<Item>().reveal(EntryLockState()).isEmpty())
    }

    @Test
    fun unlockedEntriesAreAlwaysKept() {
        assertEquals(listOf(open), listOf(open).reveal(EntryLockState()))
    }

    @Test
    fun lockedEntriesAreRemovedWithoutUnlock() {
        assertEquals(listOf(open), all.reveal(EntryLockState()))
    }

    @Test
    fun sessionUnlockKeepsAllEntries() {
        assertEquals(all, all.reveal(EntryLockState(sessionUnlocked = true)))
    }

    @Test
    fun temporaryUnlockKeepsOnlyMatchingLockedEntry() {
        assertEquals(
            listOf(open, locked),
            all.reveal(EntryLockState(temporarilyUnlockedIds = setOf(locked.id)))
        )
    }

    @Test
    fun unrelatedTemporaryUnlockDoesNotRevealOtherEntries() {
        assertEquals(
            listOf(open),
            all.reveal(EntryLockState(temporarilyUnlockedIds = setOf(99L)))
        )
    }

    @Test
    fun selectionModeAloneDoesNotRevealLockedEntries() {
        assertEquals(
            listOf(open),
            all.reveal(EntryLockState(selectionMode = true, selectedIds = setOf(locked.id)))
        )
    }

    @Test
    fun orderOfRemainingEntriesIsPreserved() {
        val ordered = listOf(otherLocked, open, locked)
        assertEquals(
            listOf(otherLocked, open),
            ordered.reveal(EntryLockState(temporarilyUnlockedIds = setOf(otherLocked.id)))
        )
    }

    @Test
    fun flowFiltersWithCurrentLockState() = runTest {
        val lockState = MutableStateFlow(EntryLockState())
        val filtered = flowOf(all).filterRevealed(lockState, { it.locked }, { it.id })

        assertEquals(listOf(open), filtered.first())

        lockState.value = EntryLockState(temporarilyUnlockedIds = setOf(locked.id))
        assertEquals(listOf(open, locked), filtered.first())

        lockState.value = EntryLockState(sessionUnlocked = true)
        assertEquals(all, filtered.first())

        lockState.value = EntryLockState()
        assertEquals(listOf(open), filtered.first())
    }
}
