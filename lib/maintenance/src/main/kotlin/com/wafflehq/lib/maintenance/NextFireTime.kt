package com.wafflehq.lib.maintenance

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object NextFireTime {

    fun at(
        hour: Int,
        minute: Int,
        zone: ZoneId = ZoneId.systemDefault(),
        now: ZonedDateTime = ZonedDateTime.now(zone)
    ): ZonedDateTime {
        var fireTime = ZonedDateTime.of(now.withZoneSameInstant(zone).toLocalDate(), LocalTime.of(hour, minute), zone)
        if (!fireTime.isAfter(now)) {
            fireTime = fireTime.plusDays(1)
        }
        return fireTime
    }

    fun delayMillis(
        hour: Int,
        minute: Int,
        zone: ZoneId = ZoneId.systemDefault(),
        now: ZonedDateTime = ZonedDateTime.now(zone)
    ): Long = at(hour, minute, zone, now).toInstant().toEpochMilli() - now.toInstant().toEpochMilli()

    fun tomorrowAt(
        hour: Int,
        minute: Int,
        zone: ZoneId = ZoneId.systemDefault(),
        now: ZonedDateTime = ZonedDateTime.now(zone)
    ): ZonedDateTime = ZonedDateTime.of(now.toLocalDate().plusDays(1), LocalTime.of(hour, minute), zone)

    fun tomorrowDelayMillis(
        hour: Int,
        minute: Int,
        zone: ZoneId = ZoneId.systemDefault(),
        now: ZonedDateTime = ZonedDateTime.now(zone)
    ): Long = tomorrowAt(hour, minute, zone, now).toInstant().toEpochMilli() - now.toInstant().toEpochMilli()
}
