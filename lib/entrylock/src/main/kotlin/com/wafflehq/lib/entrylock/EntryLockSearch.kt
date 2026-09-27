package com.wafflehq.lib.entrylock

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

fun <T> List<T>.filterRevealed(
    lockState: EntryLockState,
    isLocked: (T) -> Boolean,
    entryId: (T) -> Long
): List<T> = filter { isEntryRevealed(isLocked(it), entryId(it), lockState.sessionUnlocked, lockState.temporarilyUnlockedIds) }

fun <T> Flow<List<T>>.filterRevealed(
    lockState: Flow<EntryLockState>,
    isLocked: (T) -> Boolean,
    entryId: (T) -> Long
): Flow<List<T>> = combine(lockState) { entries, state -> entries.filterRevealed(state, isLocked, entryId) }
