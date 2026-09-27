package com.wafflehq.lib.astronomy

import java.time.LocalDate
import kotlin.math.abs

enum class MoonPhase(val symbol: String) {
    NEW_MOON("🌑"),
    FIRST_QUARTER("🌓"),
    FULL_MOON("🌕"),
    LAST_QUARTER("🌗")
}

object MoonPhaseCalculator {

    private const val REFERENCE_EPOCH_DAY = 10962L
    private const val SYNODIC_MONTH = 29.530588853

    private val PHASE_FRACTIONS = mapOf(
        MoonPhase.NEW_MOON to 0.0,
        MoonPhase.FIRST_QUARTER to 0.25,
        MoonPhase.FULL_MOON to 0.5,
        MoonPhase.LAST_QUARTER to 0.75
    )

    fun getPhaseForRange(from: LocalDate, to: LocalDate): Map<LocalDate, MoonPhase> {
        val result = mutableMapOf<LocalDate, MoonPhase>()
        var d = from
        while (!d.isAfter(to)) {
            getMajorPhase(d)?.let { result[d] = it }
            d = d.plusDays(1)
        }
        return result
    }

    fun nearestPhase(date: LocalDate): MoonPhase {
        val phase = continuousPhase(date)
        return PHASE_FRACTIONS.entries.minBy { circularDist(phase, it.value) }.key
    }

    internal fun getMajorPhase(date: LocalDate): MoonPhase? {
        val phase = continuousPhase(date)
        val prevPhase = continuousPhase(date.minusDays(1))
        val nextPhase = continuousPhase(date.plusDays(1))

        return PHASE_FRACTIONS.entries
            .firstOrNull { (_, f) ->
                val dist = circularDist(phase, f)
                val prevDist = circularDist(prevPhase, f)
                val nextDist = circularDist(nextPhase, f)
                dist < prevDist && dist <= nextDist
            }
            ?.key
    }

    internal fun continuousPhase(date: LocalDate): Double {
        val days = (date.toEpochDay() - REFERENCE_EPOCH_DAY).toDouble()
        return days.mod(SYNODIC_MONTH) / SYNODIC_MONTH
    }

    private fun circularDist(a: Double, b: Double): Double {
        val d = abs(a - b)
        return minOf(d, 1.0 - d)
    }
}
