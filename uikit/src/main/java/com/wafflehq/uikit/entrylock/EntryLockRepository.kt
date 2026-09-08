package com.wafflehq.uikit.entrylock

interface EntryLockRepository {
    suspend fun setAllLocked(locked: Boolean)
    suspend fun setLocked(ids: Set<Long>, locked: Boolean)
}
