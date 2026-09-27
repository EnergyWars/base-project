package com.wafflehq.lib.backupcore

import java.time.Instant
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupRetentionPolicyTest {

    private val now = Instant.parse("2026-08-30T12:00:00Z")

    private fun candidate(id: String, daysAgo: Long, isManual: Boolean = false) =
        BackupRetentionCandidate(id = id, createdAt = now.minus(daysAgo, ChronoUnit.DAYS), isManual = isManual)

    @Test
    fun `disabled policy never deletes anything`() {
        val candidates = (1..20).map { candidate("auto-$it", daysAgo = it.toLong()) }

        val result = BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = false,
            mode = BackupRetentionMode.COUNT,
            maxCount = 1,
            maxAgeDays = 1,
            now = now
        )

        assertEquals(emptySet<String>(), result)
    }

    @Test
    fun `count mode keeps the newest maxCount automatic backups`() {
        val candidates = (1..15).map { candidate("auto-$it", daysAgo = it.toLong()) }

        val result = BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = true,
            mode = BackupRetentionMode.COUNT,
            maxCount = 10,
            maxAgeDays = 30,
            now = now
        )

        assertEquals((11..15).map { "auto-$it" }.toSet(), result)
    }

    @Test
    fun `count mode never deletes manual backups`() {
        val candidates = listOf(candidate("manual-old", daysAgo = 100, isManual = true)) +
            (1..12).map { candidate("auto-$it", daysAgo = it.toLong()) }

        val result = BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = true,
            mode = BackupRetentionMode.COUNT,
            maxCount = 10,
            maxAgeDays = 30,
            now = now
        )

        assertEquals(false, "manual-old" in result)
        assertEquals((11..12).map { "auto-$it" }.toSet(), result)
    }

    @Test
    fun `count mode does not let manual backups occupy slots in the kept window`() {
        val candidates = (1..5).map { candidate("manual-$it", daysAgo = it.toLong(), isManual = true) } +
            (1..10).map { candidate("auto-$it", daysAgo = it.toLong() + 5) }

        val result = BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = true,
            mode = BackupRetentionMode.COUNT,
            maxCount = 10,
            maxAgeDays = 30,
            now = now
        )

        assertEquals(emptySet<String>(), result)
    }

    @Test
    fun `age mode deletes automatic backups older than maxAgeDays`() {
        val candidates = listOf(
            candidate("recent", daysAgo = 5),
            candidate("boundary", daysAgo = 30),
            candidate("old", daysAgo = 31)
        )

        val result = BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = true,
            mode = BackupRetentionMode.AGE,
            maxCount = 10,
            maxAgeDays = 30,
            now = now
        )

        assertEquals(setOf("old"), result)
    }

    @Test
    fun `age mode never deletes manual backups regardless of age`() {
        val candidates = listOf(candidate("manual-ancient", daysAgo = 400, isManual = true))

        val result = BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = true,
            mode = BackupRetentionMode.AGE,
            maxCount = 10,
            maxAgeDays = 30,
            now = now
        )

        assertEquals(emptySet<String>(), result)
    }

    @Test
    fun `fromOrdinal falls back to COUNT for unknown or missing values`() {
        assertEquals(BackupRetentionMode.COUNT, BackupRetentionMode.fromOrdinal(null))
        assertEquals(BackupRetentionMode.COUNT, BackupRetentionMode.fromOrdinal(99))
        assertEquals(BackupRetentionMode.AGE, BackupRetentionMode.fromOrdinal(BackupRetentionMode.AGE.ordinal))
        assertEquals(BackupRetentionMode.GENERATIONAL, BackupRetentionMode.fromOrdinal(BackupRetentionMode.GENERATIONAL.ordinal))
    }

    @Test
    fun `generational mode keeps one backup per day for the last keepDaily days`() {
        val candidates = (0..9).map { candidate("daily-$it", daysAgo = it.toLong()) }

        val result = BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = true,
            mode = BackupRetentionMode.GENERATIONAL,
            maxCount = 10,
            maxAgeDays = 30,
            now = now,
            keepDaily = 7,
            keepWeekly = 0,
            keepMonthly = 0
        )

        assertEquals(setOf("daily-7", "daily-8", "daily-9"), result)
    }

    @Test
    fun `generational mode keeps one backup per ISO week for the last keepWeekly weeks`() {
        val candidates = listOf(0L, 7L, 14L, 21L, 28L, 35L).map { candidate("w-$it", daysAgo = it) }

        val result = BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = true,
            mode = BackupRetentionMode.GENERATIONAL,
            maxCount = 10,
            maxAgeDays = 30,
            now = now,
            keepDaily = 0,
            keepWeekly = 4,
            keepMonthly = 0
        )

        assertEquals(setOf("w-28", "w-35"), result)
    }

    @Test
    fun `generational mode keeps one backup per calendar month for the last keepMonthly months`() {
        val candidates = (0..7).map { candidate("m-$it", daysAgo = it * 32L) }

        val result = BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = true,
            mode = BackupRetentionMode.GENERATIONAL,
            maxCount = 10,
            maxAgeDays = 30,
            now = now,
            keepDaily = 0,
            keepWeekly = 0,
            keepMonthly = 6
        )

        assertEquals(setOf("m-6", "m-7"), result)
    }

    @Test
    fun `generational mode never deletes manual backups`() {
        val candidates = listOf(candidate("manual-old", daysAgo = 400, isManual = true)) +
            (0..1).map { candidate("auto-$it", daysAgo = it.toLong()) }

        val result = BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = true,
            mode = BackupRetentionMode.GENERATIONAL,
            maxCount = 10,
            maxAgeDays = 30,
            now = now
        )

        assertEquals(false, "manual-old" in result)
    }

    @Test
    fun `generational mode respects custom generation sizes`() {
        val candidates = (0..4).map { candidate("d-$it", daysAgo = it.toLong()) }

        val result = BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = true,
            mode = BackupRetentionMode.GENERATIONAL,
            maxCount = 10,
            maxAgeDays = 30,
            now = now,
            keepDaily = 2,
            keepWeekly = 0,
            keepMonthly = 0
        )

        assertEquals(setOf("d-2", "d-3", "d-4"), result)
    }

    private fun at(id: String, instant: String, isManual: Boolean = false) =
        BackupRetentionCandidate(id = id, createdAt = Instant.parse(instant), isManual = isManual)

    private fun generational(
        candidates: List<BackupRetentionCandidate>,
        keepDaily: Int,
        keepWeekly: Int,
        keepMonthly: Int
    ) = BackupRetentionPolicy.idsToDelete(
        candidates = candidates,
        enabled = true,
        mode = BackupRetentionMode.GENERATIONAL,
        maxCount = 10,
        maxAgeDays = 30,
        now = now,
        keepDaily = keepDaily,
        keepWeekly = keepWeekly,
        keepMonthly = keepMonthly
    )

    private fun count(candidates: List<BackupRetentionCandidate>, maxCount: Int) =
        BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = true,
            mode = BackupRetentionMode.COUNT,
            maxCount = maxCount,
            maxAgeDays = 30,
            now = now
        )

    private fun age(candidates: List<BackupRetentionCandidate>, maxAgeDays: Int) =
        BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = true,
            mode = BackupRetentionMode.AGE,
            maxCount = 10,
            maxAgeDays = maxAgeDays,
            now = now
        )

    @Test
    fun `default generation sizes are 7 daily 4 weekly 6 monthly`() {
        assertEquals(7, BackupRetentionPolicy.DEFAULT_KEEP_DAILY)
        assertEquals(4, BackupRetentionPolicy.DEFAULT_KEEP_WEEKLY)
        assertEquals(6, BackupRetentionPolicy.DEFAULT_KEEP_MONTHLY)
    }

    @Test
    fun `generational mode uses default generation sizes when none are given`() {
        val candidates = (0..9).map { candidate("d-$it", daysAgo = it.toLong()) }

        val result = BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = true,
            mode = BackupRetentionMode.GENERATIONAL,
            maxCount = 10,
            maxAgeDays = 30,
            now = now
        )

        assertEquals(setOf("d-8", "d-9"), result)
    }

    @Test
    fun `no candidates yields nothing to delete in every mode`() {
        BackupRetentionMode.entries.forEach { mode ->
            val result = BackupRetentionPolicy.idsToDelete(
                candidates = emptyList(),
                enabled = true,
                mode = mode,
                maxCount = 1,
                maxAgeDays = 1,
                now = now
            )

            assertEquals(emptySet<String>(), result)
        }
    }

    @Test
    fun `count mode deletes nothing when there are exactly maxCount automatic backups`() {
        val candidates = (1..10).map { candidate("auto-$it", daysAgo = it.toLong()) }

        assertEquals(emptySet<String>(), count(candidates, maxCount = 10))
    }

    @Test
    fun `count mode deletes exactly one backup when there are maxCount plus one`() {
        val candidates = (1..11).map { candidate("auto-$it", daysAgo = it.toLong()) }

        assertEquals(setOf("auto-11"), count(candidates, maxCount = 10))
    }

    @Test
    fun `count mode deletes nothing when maxCount exceeds the number of backups`() {
        val candidates = (1..3).map { candidate("auto-$it", daysAgo = it.toLong()) }

        assertEquals(emptySet<String>(), count(candidates, maxCount = 100))
    }

    @Test
    fun `count mode with maxCount zero deletes all automatic backups but no manual ones`() {
        val candidates = listOf(candidate("manual", daysAgo = 1, isManual = true)) +
            (1..3).map { candidate("auto-$it", daysAgo = it.toLong()) }

        assertEquals(setOf("auto-1", "auto-2", "auto-3"), count(candidates, maxCount = 0))
    }

    @Test
    fun `count mode treats a negative maxCount like zero`() {
        val candidates = (1..3).map { candidate("auto-$it", daysAgo = it.toLong()) }

        assertEquals(setOf("auto-1", "auto-2", "auto-3"), count(candidates, maxCount = -5))
    }

    @Test
    fun `count mode sorts by creation time and not by input order`() {
        val candidates = listOf(
            candidate("middle", daysAgo = 5),
            candidate("oldest", daysAgo = 9),
            candidate("newest", daysAgo = 1)
        )

        assertEquals(setOf("middle", "oldest"), count(candidates, maxCount = 1))
    }

    @Test
    fun `age mode keeps a backup created exactly at the cutoff`() {
        val cutoff = now.minus(30, ChronoUnit.DAYS)
        val candidates = listOf(
            BackupRetentionCandidate("at-cutoff", cutoff, isManual = false),
            BackupRetentionCandidate("just-before", cutoff.minusSeconds(1), isManual = false),
            BackupRetentionCandidate("just-after", cutoff.plusSeconds(1), isManual = false)
        )

        assertEquals(setOf("just-before"), age(candidates, maxAgeDays = 30))
    }

    @Test
    fun `age mode with maxAgeDays zero deletes everything older than now`() {
        val candidates = listOf(
            BackupRetentionCandidate("now", now, isManual = false),
            BackupRetentionCandidate("past", now.minusSeconds(1), isManual = false)
        )

        assertEquals(setOf("past"), age(candidates, maxAgeDays = 0))
    }

    @Test
    fun `age mode treats a negative maxAgeDays like zero`() {
        val candidates = listOf(
            BackupRetentionCandidate("now", now, isManual = false),
            BackupRetentionCandidate("past", now.minusSeconds(1), isManual = false),
            BackupRetentionCandidate("future", now.plusSeconds(60), isManual = false)
        )

        assertEquals(setOf("past"), age(candidates, maxAgeDays = -10))
    }

    @Test
    fun `age mode never deletes backups dated in the future`() {
        val candidates = listOf(BackupRetentionCandidate("future", now.plus(5, ChronoUnit.DAYS), isManual = false))

        assertEquals(emptySet<String>(), age(candidates, maxAgeDays = 1))
    }

    @Test
    fun `age mode with a huge maxAgeDays deletes nothing`() {
        val candidates = listOf(candidate("ancient", daysAgo = 36500))

        assertEquals(emptySet<String>(), age(candidates, maxAgeDays = Int.MAX_VALUE))
    }

    @Test
    fun `age mode ignores maxCount`() {
        val candidates = (1..5).map { candidate("auto-$it", daysAgo = it.toLong()) }

        val result = BackupRetentionPolicy.idsToDelete(
            candidates = candidates,
            enabled = true,
            mode = BackupRetentionMode.AGE,
            maxCount = 1,
            maxAgeDays = 30,
            now = now
        )

        assertEquals(emptySet<String>(), result)
    }

    @Test
    fun `generational mode keeps only the newest backup of a day`() {
        val candidates = listOf(
            at("newest", "2026-08-30T10:00:00Z"),
            at("same-day", "2026-08-30T08:00:00Z"),
            at("previous-day", "2026-08-29T23:00:00Z")
        )

        assertEquals(setOf("same-day"), generational(candidates, keepDaily = 2, keepWeekly = 0, keepMonthly = 0))
    }

    @Test
    fun `generational mode buckets days in UTC`() {
        val candidates = listOf(
            at("before-midnight", "2026-08-29T23:59:59Z"),
            at("after-midnight", "2026-08-30T00:00:00Z")
        )

        assertEquals(emptySet<String>(), generational(candidates, keepDaily = 2, keepWeekly = 0, keepMonthly = 0))
        assertEquals(
            setOf("before-midnight"),
            generational(candidates, keepDaily = 1, keepWeekly = 0, keepMonthly = 0)
        )
    }

    @Test
    fun `generational mode separates ISO weeks between Sunday and Monday`() {
        val candidates = listOf(
            at("monday", "2026-08-31T12:00:00Z"),
            at("sunday", "2026-08-30T12:00:00Z")
        )

        assertEquals(emptySet<String>(), generational(candidates, keepDaily = 0, keepWeekly = 2, keepMonthly = 0))
    }

    @Test
    fun `generational mode uses the ISO week based year across the new year`() {
        val candidates = listOf(
            at("thursday-new-year", "2026-01-01T12:00:00Z"),
            at("monday-same-iso-week", "2025-12-29T12:00:00Z"),
            at("previous-iso-week", "2025-12-28T12:00:00Z")
        )

        assertEquals(
            setOf("monday-same-iso-week"),
            generational(candidates, keepDaily = 0, keepWeekly = 2, keepMonthly = 0)
        )
        assertEquals(
            setOf("monday-same-iso-week", "previous-iso-week"),
            generational(candidates, keepDaily = 0, keepWeekly = 1, keepMonthly = 0)
        )
    }

    @Test
    fun `generational mode separates equal months of different years`() {
        val candidates = listOf(
            at("august-2026", "2026-08-15T12:00:00Z"),
            at("august-2025", "2025-08-15T12:00:00Z")
        )

        assertEquals(emptySet<String>(), generational(candidates, keepDaily = 0, keepWeekly = 0, keepMonthly = 2))
        assertEquals(
            setOf("august-2025"),
            generational(candidates, keepDaily = 0, keepWeekly = 0, keepMonthly = 1)
        )
    }

    @Test
    fun `generational mode keeps the union of all generations`() {
        val candidates = listOf(
            at("today", "2026-08-30T10:00:00Z"),
            at("today-earlier", "2026-08-30T06:00:00Z"),
            at("july", "2026-07-15T12:00:00Z"),
            at("june", "2026-06-10T12:00:00Z")
        )

        assertEquals(
            setOf("today-earlier", "june"),
            generational(candidates, keepDaily = 1, keepWeekly = 0, keepMonthly = 2)
        )
    }

    @Test
    fun `generational mode with all generation sizes zero deletes every automatic backup`() {
        val candidates = listOf(candidate("manual", daysAgo = 1, isManual = true)) +
            (0..2).map { candidate("auto-$it", daysAgo = it.toLong()) }

        assertEquals(
            setOf("auto-0", "auto-1", "auto-2"),
            generational(candidates, keepDaily = 0, keepWeekly = 0, keepMonthly = 0)
        )
    }

    @Test
    fun `generational mode treats negative generation sizes like zero`() {
        val candidates = (0..2).map { candidate("auto-$it", daysAgo = it.toLong()) }

        assertEquals(
            setOf("auto-0", "auto-1", "auto-2"),
            generational(candidates, keepDaily = -1, keepWeekly = -1, keepMonthly = -1)
        )
    }

    @Test
    fun `generational mode does not count manual backups as generation slots`() {
        val candidates = listOf(
            candidate("manual-newest", daysAgo = 0, isManual = true),
            candidate("auto-1", daysAgo = 1),
            candidate("auto-2", daysAgo = 2)
        )

        assertEquals(
            setOf("auto-2"),
            generational(candidates, keepDaily = 1, keepWeekly = 0, keepMonthly = 0)
        )
    }

    @Test
    fun `fromOrdinal falls back to COUNT for negative and out of range ordinals`() {
        assertEquals(BackupRetentionMode.COUNT, BackupRetentionMode.fromOrdinal(-1))
        assertEquals(BackupRetentionMode.COUNT, BackupRetentionMode.fromOrdinal(BackupRetentionMode.entries.size))
        assertEquals(BackupRetentionMode.COUNT, BackupRetentionMode.fromOrdinal(0))
    }

    @Test
    fun `persisted ordinals of the retention modes stay stable`() {
        assertEquals(0, BackupRetentionMode.COUNT.ordinal)
        assertEquals(1, BackupRetentionMode.AGE.ordinal)
        assertEquals(2, BackupRetentionMode.GENERATIONAL.ordinal)
    }
}
