package com.wafflehq.uikit.drafts

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DraftRepositoryTest {

    private class InMemoryDraftRepository<T> : DraftRepository<T> {
        private val state = MutableStateFlow<StoredDraft<T>?>(null)
        override fun observe(): Flow<StoredDraft<T>?> = state
        override suspend fun save(entryId: Long?, payload: T) {
            state.value = StoredDraft(entryId, payload, updatedAtEpochMs = 0L)
        }
        override suspend fun clear() {
            state.value = null
        }
    }

    @Test
    fun observeIsNullUntilFirstSave() = runTest {
        val repository = InMemoryDraftRepository<String>()
        assertNull(repository.observe().first())
    }

    @Test
    fun saveStoresPayloadAndEntryId() = runTest {
        val repository = InMemoryDraftRepository<String>()
        repository.save(entryId = 7L, payload = "hello")
        val stored = repository.observe().first()
        assertEquals(7L, stored?.entryId)
        assertEquals("hello", stored?.payload)
    }

    @Test
    fun clearRemovesTheStoredDraft() = runTest {
        val repository = InMemoryDraftRepository<String>()
        repository.save(entryId = null, payload = "draft")
        repository.clear()
        assertNull(repository.observe().first())
    }

    @Test
    fun storedDraftEqualityIsStructural() {
        val a = StoredDraft(entryId = 1L, payload = "x", updatedAtEpochMs = 100L)
        val b = StoredDraft(entryId = 1L, payload = "x", updatedAtEpochMs = 100L)
        assertEquals(a, b)
    }
}
