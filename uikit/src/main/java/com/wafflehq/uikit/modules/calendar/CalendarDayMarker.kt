package com.wafflehq.uikit.modules.calendar

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class CalendarDayMarker(
    val sourceId: String,
    val icon: ImageVector? = null,
    val colorTokenId: String? = null,
    val label: String? = null,
)

fun interface CalendarDayMarkerSource {
    fun markers(from: LocalDate, to: LocalDate): Flow<Map<LocalDate, List<CalendarDayMarker>>>
}
