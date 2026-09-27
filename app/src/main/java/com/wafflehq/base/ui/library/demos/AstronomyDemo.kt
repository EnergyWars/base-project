package com.wafflehq.base.ui.library.demos

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.wafflehq.base.R
import com.wafflehq.lib.astronomy.MoonPhase
import com.wafflehq.lib.astronomy.MoonPhaseCalculator
import com.wafflehq.lib.astronomy.SunCalculator
import com.wafflehq.lib.astronomy.SunTimes
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.components.AppLabelChip
import com.wafflehq.lib.uicore.components.AppPillSelector
import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.time.TimeFormats
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal enum class AstronomyLocation(
    val latitude: Double,
    val longitude: Double,
    val zoneId: String,
    @StringRes val labelRes: Int,
) {
    BERLIN(52.52, 13.405, "Europe/Berlin", R.string.libex_astronomy_location_berlin),
    SYDNEY(-33.87, 151.21, "Australia/Sydney", R.string.libex_astronomy_location_sydney),
    TROMSOE(69.65, 18.96, "Europe/Oslo", R.string.libex_astronomy_location_tromsoe),
}

internal data class AstronomySnapshot(
    val date: LocalDate,
    val location: AstronomyLocation,
    val sunTimes: SunTimes,
    val moonPhase: MoonPhase,
    val upcomingPhases: List<Pair<LocalDate, MoonPhase>>,
)

internal object AstronomyDemoLogic {

    const val UPCOMING_DAYS = 30L

    fun snapshot(date: LocalDate, location: AstronomyLocation): AstronomySnapshot {
        val sunTimes = SunCalculator.calculate(
            date = date,
            latDeg = location.latitude,
            lonDeg = location.longitude,
            zoneId = ZoneId.of(location.zoneId),
        )
        val upcoming = MoonPhaseCalculator
            .getPhaseForRange(date, date.plusDays(UPCOMING_DAYS - 1))
            .entries
            .sortedBy { it.key }
            .map { it.key to it.value }
        return AstronomySnapshot(
            date = date,
            location = location,
            sunTimes = sunTimes,
            moonPhase = MoonPhaseCalculator.nearestPhase(date),
            upcomingPhases = upcoming,
        )
    }

    @StringRes
    fun moonPhaseLabel(phase: MoonPhase): Int = when (phase) {
        MoonPhase.NEW_MOON -> R.string.libex_astronomy_phase_new
        MoonPhase.FIRST_QUARTER -> R.string.libex_astronomy_phase_first_quarter
        MoonPhase.FULL_MOON -> R.string.libex_astronomy_phase_full
        MoonPhase.LAST_QUARTER -> R.string.libex_astronomy_phase_last_quarter
    }
}

internal object AstronomyTags {
    const val DATE = "libex_astronomy_date"
    const val NEXT = "libex_astronomy_next"
    const val PREVIOUS = "libex_astronomy_previous"
    const val SUNRISE = "libex_astronomy_sunrise"
    const val SUNSET = "libex_astronomy_sunset"
    const val MOON = "libex_astronomy_moon"
    fun location(location: AstronomyLocation) = "libex_astronomy_location_${location.name}"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AstronomyDemo(initialDate: LocalDate = LocalDate.now()) {
    var date by remember { mutableStateOf(initialDate) }
    var location by remember { mutableStateOf(AstronomyLocation.BERLIN) }
    val snapshot = remember(date, location) { AstronomyDemoLogic.snapshot(date, location) }
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val shortFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT) }
    val noTime = stringResource(R.string.libex_astronomy_no_time)

    DemoSection(
        id = "astronomy",
        titleRes = R.string.libex_astronomy_title,
        descriptionRes = R.string.libex_astronomy_desc,
        moduleRes = R.string.libex_module_astronomy,
    ) {
        AppPillSelector(
            options = AstronomyLocation.entries,
            selected = location,
            onSelect = { location = it },
            label = { stringResource(it.labelRes) },
            modifier = Modifier.fillMaxWidth(),
            testTag = { AstronomyTags.location(it) },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            AppIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.libex_action_previous),
                role = AppButtonRole.Neutral,
                onClick = { date = date.minusDays(1) },
                modifier = Modifier.testTag(AstronomyTags.PREVIOUS),
            )
            Text(
                text = date.format(dateFormatter),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).testTag(AstronomyTags.DATE),
            )
            AppIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.libex_action_next),
                role = AppButtonRole.Neutral,
                onClick = { date = date.plusDays(1) },
                modifier = Modifier.testTag(AstronomyTags.NEXT),
            )
            AppButton(
                text = stringResource(R.string.libex_action_today),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Tonal,
                onClick = { date = LocalDate.now() },
            )
        }
        val sun = snapshot.sunTimes
        DemoKeyValue(
            label = stringResource(R.string.libex_astronomy_morning_blue),
            value = sun.morningBlueStart?.format(TimeFormats.HOUR_MINUTE) ?: noTime,
        )
        DemoKeyValue(
            label = stringResource(R.string.libex_astronomy_sunrise),
            value = sun.sunrise?.format(TimeFormats.HOUR_MINUTE) ?: noTime,
            modifier = Modifier.testTag(AstronomyTags.SUNRISE),
        )
        DemoKeyValue(
            label = stringResource(R.string.libex_astronomy_morning_golden_end),
            value = sun.morningGoldenEnd?.format(TimeFormats.HOUR_MINUTE) ?: noTime,
        )
        DemoKeyValue(
            label = stringResource(R.string.libex_astronomy_evening_golden_start),
            value = sun.eveningGoldenStart?.format(TimeFormats.HOUR_MINUTE) ?: noTime,
        )
        DemoKeyValue(
            label = stringResource(R.string.libex_astronomy_sunset),
            value = sun.sunset?.format(TimeFormats.HOUR_MINUTE) ?: noTime,
            modifier = Modifier.testTag(AstronomyTags.SUNSET),
        )
        DemoKeyValue(
            label = stringResource(R.string.libex_astronomy_evening_blue_end),
            value = sun.eveningBlueEnd?.format(TimeFormats.HOUR_MINUTE) ?: noTime,
        )
        DemoBodyText(
            text = stringResource(
                R.string.libex_astronomy_moon_now,
                snapshot.moonPhase.symbol,
                stringResource(AstronomyDemoLogic.moonPhaseLabel(snapshot.moonPhase)),
            ),
            modifier = Modifier.testTag(AstronomyTags.MOON),
        )
        DemoMetaText(text = stringResource(R.string.libex_astronomy_upcoming, AstronomyDemoLogic.UPCOMING_DAYS.toInt()))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            snapshot.upcomingPhases.forEach { (phaseDate, phase) ->
                AppLabelChip(text = "${phase.symbol} ${phaseDate.format(shortFormatter)}")
            }
        }
    }
}
