package com.wafflehq.lib.entrylock

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class EntryLockState(
    val sessionUnlocked: Boolean = false,
    val selectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val temporarilyUnlockedIds: Set<Long> = emptySet()
)

class EntryLockController(
    private val scope: CoroutineScope,
    private val repository: EntryLockRepository,
    private val session: EntryLockSession? = null
) {
    private val _sessionUnlocked = MutableStateFlow(false)
    private val _selectionMode = MutableStateFlow(false)
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _temporarilyUnlockedIds = MutableStateFlow<Set<Long>>(emptySet())

    val state: Flow<EntryLockState> = combine(
        _sessionUnlocked, _selectionMode, _selectedIds, _temporarilyUnlockedIds
    ) { sessionUnlocked, selectionMode, selectedIds, temporarilyUnlockedIds ->
        EntryLockState(sessionUnlocked, selectionMode, selectedIds, temporarilyUnlockedIds)
    }

    init {
        session?.let { lockSession ->
            scope.launch { lockSession.relockRequests.collect { onSessionRelock() } }
        }
    }

    fun onSessionUnlockConfirmed() {
        _sessionUnlocked.value = true
        session?.onSessionUnlocked()
    }

    fun onSessionRelock() {
        _sessionUnlocked.value = false
        _temporarilyUnlockedIds.value = emptySet()
    }

    fun onLockAllEntries() {
        _sessionUnlocked.value = false
        _temporarilyUnlockedIds.value = emptySet()
        scope.launch { repository.setAllLocked(true) }
    }

    fun onUnlockAllEntriesConfirmed() {
        _temporarilyUnlockedIds.value = emptySet()
        scope.launch { repository.setAllLocked(false) }
    }

    fun onEnterSelectionMode() {
        _selectionMode.value = true
    }

    fun onLongPressEntry(id: Long) {
        _selectionMode.value = true
        _selectedIds.value = setOf(id)
    }

    fun onToggleSelected(id: Long) {
        val current = _selectedIds.value
        _selectedIds.value = if (id in current) current - id else current + id
    }

    fun onSelectAll(ids: Set<Long>) {
        _selectedIds.value = ids
    }

    fun onExitSelectionMode() {
        _selectionMode.value = false
        _selectedIds.value = emptySet()
    }

    fun onBulkLockSelected() {
        val ids = _selectedIds.value
        if (ids.isEmpty()) return
        scope.launch { repository.setLocked(ids, true) }
        _temporarilyUnlockedIds.value = _temporarilyUnlockedIds.value - ids
        onExitSelectionMode()
    }

    fun onBulkUnlockSelectedConfirmed() {
        val ids = _selectedIds.value
        if (ids.isEmpty()) return
        scope.launch { repository.setLocked(ids, false) }
        onExitSelectionMode()
    }

    fun onTemporaryUnlockConfirmed(id: Long) {
        _temporarilyUnlockedIds.value = _temporarilyUnlockedIds.value + id
    }

    fun onRelockEntry(id: Long) {
        _temporarilyUnlockedIds.value = _temporarilyUnlockedIds.value - id
    }
}

fun isEntryRevealed(isLocked: Boolean, entryId: Long, sessionUnlocked: Boolean, temporarilyUnlockedIds: Set<Long>): Boolean =
    !isLocked || sessionUnlocked || entryId in temporarilyUnlockedIds

data class EntryRevealState(val isRevealed: Boolean, val isLockedNow: Boolean, val isTempRevealed: Boolean)

fun entryRevealState(isLocked: Boolean, sessionUnlocked: Boolean, temporarilyUnlocked: Boolean): EntryRevealState {
    val isRevealed = !isLocked || sessionUnlocked || temporarilyUnlocked
    return EntryRevealState(
        isRevealed = isRevealed,
        isLockedNow = !isRevealed,
        isTempRevealed = isLocked && !sessionUnlocked && temporarilyUnlocked
    )
}
