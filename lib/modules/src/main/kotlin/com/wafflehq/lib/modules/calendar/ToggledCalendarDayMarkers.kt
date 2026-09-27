package com.wafflehq.lib.modules.calendar

import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
fun toggledCalendarDayMarkers(
    enabled: Flow<Boolean>,
    markersWhenEnabled: () -> Flow<Map<LocalDate, List<CalendarDayMarker>>>,
): Flow<Map<LocalDate, List<CalendarDayMarker>>> =
    enabled.flatMapLatest { isEnabled -> if (isEnabled) markersWhenEnabled() else flowOf(emptyMap()) }

fun <T> singleMarkerPerDate(
    entries: Flow<List<T>>,
    from: LocalDate,
    to: LocalDate,
    marker: CalendarDayMarker,
    dateOf: (T) -> LocalDate,
): Flow<Map<LocalDate, List<CalendarDayMarker>>> =
    entries.map { list ->
        list.map(dateOf)
            .filter { !it.isBefore(from) && !it.isAfter(to) }
            .distinct()
            .associateWith { listOf(marker) }
    }
