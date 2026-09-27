package com.wafflehq.lib.settings.backup

import org.junit.Assert.assertEquals
import org.junit.Test

class FailedBackupAttemptsTest {

    private fun attempt(id: String) = FailedBackupAttempt(id = id, timestampIso = "2026-01-01T00:00:00Z", reason = "network error")

    @Test
    fun `withNewFailure prepends the new attempt`() {
        val current = listOf(attempt("old"))

        val updated = FailedBackupAttempts.withNewFailure(current, attempt("new"))

        assertEquals(listOf("new", "old"), updated.map { it.id })
    }

    @Test
    fun `withNewFailure caps the list at MAX_ENTRIES`() {
        val current = (1..FailedBackupAttempts.MAX_ENTRIES).map { attempt("old-$it") }

        val updated = FailedBackupAttempts.withNewFailure(current, attempt("new"))

        assertEquals(FailedBackupAttempts.MAX_ENTRIES, updated.size)
        assertEquals("new", updated.first().id)
        assertEquals(false, updated.any { it.id == "old-${FailedBackupAttempts.MAX_ENTRIES}" })
    }

    @Test
    fun `withRemoved drops only the matching id`() {
        val current = listOf(attempt("a"), attempt("b"), attempt("c"))

        val updated = FailedBackupAttempts.withRemoved(current, "b")

        assertEquals(listOf("a", "c"), updated.map { it.id })
    }

    @Test
    fun `withRemoved on unknown id is a no-op`() {
        val current = listOf(attempt("a"))

        val updated = FailedBackupAttempts.withRemoved(current, "does-not-exist")

        assertEquals(current, updated)
    }
}
