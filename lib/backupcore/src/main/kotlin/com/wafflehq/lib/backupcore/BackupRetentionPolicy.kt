package com.wafflehq.lib.backupcore

import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields

enum class BackupRetentionMode {
    COUNT, AGE, GENERATIONAL;

    companion object {
        fun fromOrdinal(value: Int?): BackupRetentionMode =
            entries.getOrNull(value ?: -1) ?: COUNT
    }
}

data class BackupRetentionCandidate(
    val id: String,
    val createdAt: Instant,
    val isManual: Boolean
)

object BackupRetentionPolicy {

    const val DEFAULT_KEEP_DAILY = 7
    const val DEFAULT_KEEP_WEEKLY = 4
    const val DEFAULT_KEEP_MONTHLY = 6

    fun idsToDelete(
        candidates: List<BackupRetentionCandidate>,
        enabled: Boolean,
        mode: BackupRetentionMode,
        maxCount: Int,
        maxAgeDays: Int,
        now: Instant = Instant.now(),
        keepDaily: Int = DEFAULT_KEEP_DAILY,
        keepWeekly: Int = DEFAULT_KEEP_WEEKLY,
        keepMonthly: Int = DEFAULT_KEEP_MONTHLY
    ): Set<String> {
        if (!enabled) return emptySet()
        val prunable = candidates.filterNot { it.isManual }.sortedByDescending { it.createdAt }
        val toDelete = when (mode) {
            BackupRetentionMode.COUNT -> prunable.drop(maxCount.coerceAtLeast(0))
            BackupRetentionMode.AGE -> {
                val cutoff = now.minus(maxAgeDays.toLong().coerceAtLeast(0), ChronoUnit.DAYS)
                prunable.filter { it.createdAt.isBefore(cutoff) }
            }
            BackupRetentionMode.GENERATIONAL -> {
                val keepIds = generationalKeepIds(prunable, keepDaily, keepWeekly, keepMonthly)
                prunable.filterNot { it.id in keepIds }
            }
        }
        return toDelete.map { it.id }.toSet()
    }

    private fun generationalKeepIds(
        prunable: List<BackupRetentionCandidate>,
        keepDaily: Int,
        keepWeekly: Int,
        keepMonthly: Int
    ): Set<String> {
        val weekFields = WeekFields.ISO

        fun keepNewestPerBucket(bucketCount: Int, bucketKey: (Instant) -> Any): Set<String> {
            if (bucketCount <= 0) return emptySet()
            val seenBuckets = HashSet<Any>()
            val keep = mutableSetOf<String>()
            for (candidate in prunable) {
                val key = bucketKey(candidate.createdAt)
                if (seenBuckets.add(key)) {
                    keep += candidate.id
                    if (seenBuckets.size >= bucketCount) break
                }
            }
            return keep
        }

        val daily = keepNewestPerBucket(keepDaily) { it.atZone(ZoneOffset.UTC).toLocalDate() }
        val weekly = keepNewestPerBucket(keepWeekly) {
            val date = it.atZone(ZoneOffset.UTC).toLocalDate()
            date.get(weekFields.weekBasedYear()) to date.get(weekFields.weekOfWeekBasedYear())
        }
        val monthly = keepNewestPerBucket(keepMonthly) {
            val date = it.atZone(ZoneOffset.UTC).toLocalDate()
            date.year to date.monthValue
        }
        return daily + weekly + monthly
    }
}
