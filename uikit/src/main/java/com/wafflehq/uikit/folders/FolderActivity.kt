package com.wafflehq.uikit.folders

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object FolderActivity {
    fun resolve(
        lastActivityEpochMs: Long?,
        createdAtEpochMs: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): LocalDateTime {
        val effective = lastActivityEpochMs?.coerceAtLeast(createdAtEpochMs) ?: createdAtEpochMs
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(effective), zoneId)
    }

    fun resolveFromEpochDay(
        lastActivityEpochDay: Long?,
        createdAtEpochMs: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): LocalDateTime {
        val created = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAtEpochMs), zoneId)
        val activity = lastActivityEpochDay?.let { LocalDate.ofEpochDay(it).atStartOfDay() } ?: return created
        return maxOf(activity, created)
    }
}
