package com.wafflehq.uikit.folders

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class FolderActivityTest {

    private val zone = ZoneOffset.UTC

    @Test
    fun resolve_withoutActivity_usesCreationTime() {
        val created = LocalDateTime.of(2026, 3, 1, 10, 30).toInstant(zone).toEpochMilli()

        val result = FolderActivity.resolve(null, created, zone)

        assertEquals(LocalDateTime.of(2026, 3, 1, 10, 30), result)
    }

    @Test
    fun resolve_withNewerActivity_usesActivityTime() {
        val created = LocalDateTime.of(2026, 3, 1, 10, 30).toInstant(zone).toEpochMilli()
        val activity = LocalDateTime.of(2026, 4, 2, 8, 0).toInstant(zone).toEpochMilli()

        val result = FolderActivity.resolve(activity, created, zone)

        assertEquals(LocalDateTime.of(2026, 4, 2, 8, 0), result)
    }

    @Test
    fun resolve_withOlderActivityThanCreation_usesCreationTime() {
        val created = LocalDateTime.of(2026, 3, 1, 10, 30).toInstant(zone).toEpochMilli()
        val activity = LocalDateTime.of(2025, 1, 1, 0, 0).toInstant(zone).toEpochMilli()

        val result = FolderActivity.resolve(activity, created, zone)

        assertEquals(LocalDateTime.of(2026, 3, 1, 10, 30), result)
    }

    @Test
    fun resolveFromEpochDay_withoutActivity_usesCreationTime() {
        val created = LocalDateTime.of(2026, 3, 1, 10, 30).toInstant(zone).toEpochMilli()

        val result = FolderActivity.resolveFromEpochDay(null, created, zone)

        assertEquals(LocalDateTime.of(2026, 3, 1, 10, 30), result)
    }

    @Test
    fun resolveFromEpochDay_withNewerDay_usesStartOfThatDay() {
        val created = LocalDateTime.of(2026, 3, 1, 10, 30).toInstant(zone).toEpochMilli()
        val day = LocalDate.of(2026, 5, 20).toEpochDay()

        val result = FolderActivity.resolveFromEpochDay(day, created, zone)

        assertEquals(LocalDateTime.of(2026, 5, 20, 0, 0), result)
    }

    @Test
    fun resolveFromEpochDay_withOlderDay_usesCreationTime() {
        val created = LocalDateTime.of(2026, 3, 1, 10, 30).toInstant(zone).toEpochMilli()
        val day = LocalDate.of(2026, 3, 1).toEpochDay()

        val result = FolderActivity.resolveFromEpochDay(day, created, zone)

        assertEquals(LocalDateTime.of(2026, 3, 1, 10, 30), result)
    }
}
