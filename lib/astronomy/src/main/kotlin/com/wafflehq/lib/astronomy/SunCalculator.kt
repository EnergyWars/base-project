package com.wafflehq.lib.astronomy

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.*

data class SunTimes(
    val morningBlueStart: LocalTime?,
    val sunrise: LocalTime?,
    val morningGoldenEnd: LocalTime?,
    val eveningGoldenStart: LocalTime?,
    val sunset: LocalTime?,
    val eveningBlueEnd: LocalTime?
) {
    fun withVisibleSegments(
        showMorningGolden: Boolean,
        showEveningGolden: Boolean,
        showMorningBlue: Boolean,
        showEveningBlue: Boolean
    ): SunTimes? {
        val filtered = copy(
            morningBlueStart = if (showMorningBlue) morningBlueStart else null,
            morningGoldenEnd = if (showMorningGolden) morningGoldenEnd else null,
            eveningGoldenStart = if (showEveningGolden) eveningGoldenStart else null,
            eveningBlueEnd = if (showEveningBlue) eveningBlueEnd else null
        )
        val hasVisibleSegment = filtered.morningBlueStart != null ||
            filtered.morningGoldenEnd != null ||
            filtered.eveningGoldenStart != null ||
            filtered.eveningBlueEnd != null
        return if (hasVisibleSegment) filtered else null
    }
}

object SunCalculator {

    private const val ZENITH_SUNRISE_SUNSET = 90.833
    private const val ZENITH_GOLDEN_HOUR = 84.0
    private const val ZENITH_CIVIL_TWILIGHT = 96.0

    fun calculate(
        date: LocalDate,
        latDeg: Double,
        lonDeg: Double,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): SunTimes {
        val utcOffsetMinutes = zoneId.rules
            .getOffset(date.atTime(LocalTime.NOON).atZone(zoneId).toInstant())
            .totalSeconds / 60
        val jd = julianDay(date)
        return SunTimes(
            morningBlueStart = calcTime(jd, latDeg, lonDeg, ZENITH_CIVIL_TWILIGHT, true, utcOffsetMinutes),
            sunrise = calcTime(jd, latDeg, lonDeg, ZENITH_SUNRISE_SUNSET, true, utcOffsetMinutes),
            morningGoldenEnd = calcTime(jd, latDeg, lonDeg, ZENITH_GOLDEN_HOUR, true, utcOffsetMinutes),
            eveningGoldenStart = calcTime(jd, latDeg, lonDeg, ZENITH_GOLDEN_HOUR, false, utcOffsetMinutes),
            sunset = calcTime(jd, latDeg, lonDeg, ZENITH_SUNRISE_SUNSET, false, utcOffsetMinutes),
            eveningBlueEnd = calcTime(jd, latDeg, lonDeg, ZENITH_CIVIL_TWILIGHT, false, utcOffsetMinutes)
        )
    }

    internal fun julianDay(date: LocalDate): Double {
        val a = (14 - date.monthValue) / 12
        val y = date.year + 4800 - a
        val m = date.monthValue + 12 * a - 3
        return (date.dayOfMonth + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045).toDouble()
    }

    internal fun calcTime(
        jd: Double,
        latDeg: Double,
        lonDeg: Double,
        zenithDeg: Double,
        isSunrise: Boolean,
        utcOffsetMinutes: Int
    ): LocalTime? {
        val jc = (jd - 2451545.0) / 36525.0
        val l0 = (280.46646 + jc * (36000.76983 + jc * 0.0003032)).mod(360.0)
        val m = 357.52911 + jc * (35999.05029 - 0.0001537 * jc)
        val mRad = Math.toRadians(m)
        val c = sin(mRad) * (1.914602 - jc * (0.004817 + 0.000014 * jc)) +
                sin(2.0 * mRad) * (0.019993 - 0.000101 * jc) +
                sin(3.0 * mRad) * 0.000289
        val sunTrueLon = l0 + c
        val omega = 125.04 - 1934.136 * jc
        val lambda = sunTrueLon - 0.00569 - 0.00478 * sin(Math.toRadians(omega))
        val epsilon0 = 23.0 + (26.0 + (21.448 - jc * (46.815 + jc * (0.00059 - jc * 0.001813))) / 60.0) / 60.0
        val epsilon = epsilon0 + 0.00256 * cos(Math.toRadians(omega))
        val declRad = asin(sin(Math.toRadians(epsilon)) * sin(Math.toRadians(lambda)))
        val e = 0.016708634 - jc * (0.000042037 + 0.0000001267 * jc)
        val yFactor = tan(Math.toRadians(epsilon / 2.0)).pow(2)
        val l0Rad = Math.toRadians(l0)
        val eqtime = (yFactor * sin(2.0 * l0Rad)
                - 2.0 * e * sin(mRad)
                + 4.0 * e * yFactor * sin(mRad) * cos(2.0 * l0Rad)
                - 0.5 * yFactor.pow(2) * sin(4.0 * l0Rad)
                - 1.25 * e.pow(2) * sin(2.0 * mRad)) * (180.0 / PI * 4.0)
        val latRad = Math.toRadians(latDeg)
        val cosHA = cos(Math.toRadians(zenithDeg)) / (cos(latRad) * cos(declRad)) -
                tan(latRad) * tan(declRad)
        if (cosHA < -1.0 || cosHA > 1.0) return null
        val ha = Math.toDegrees(acos(cosHA))
        val solarNoonUtc = 720.0 - 4.0 * lonDeg - eqtime
        val utcMinutes = if (isSunrise) solarNoonUtc - 4.0 * ha else solarNoonUtc + 4.0 * ha
        val localMinutes = utcMinutes + utcOffsetMinutes
        if (localMinutes < 0.0 || localMinutes >= 1440.0) return null
        val totalSeconds = (localMinutes * 60.0).roundToInt().toLong().coerceIn(0L, 86399L)
        return LocalTime.ofSecondOfDay(totalSeconds)
    }
}
