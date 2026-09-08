package com.wafflehq.uikit.drafts

import kotlinx.coroutines.flow.Flow

data class StoredDraft<T>(
    val entryId: Long?,
    val payload: T,
    val updatedAtEpochMs: Long,
)

interface DraftRepository<T> {
    fun observe(): Flow<StoredDraft<T>?>
    suspend fun save(entryId: Long?, payload: T)
    suspend fun clear()
}
