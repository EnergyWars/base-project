package com.wafflehq.base.ui.library.demos

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.lib.charts.ChartPoint
import com.wafflehq.lib.charts.ChartSegment
import com.wafflehq.lib.charts.InteractiveCenteredBarChart
import com.wafflehq.lib.charts.InteractiveMultiLineChart
import com.wafflehq.lib.charts.InteractiveStackedBarChart
import com.wafflehq.lib.charts.LineChartSeries
import com.wafflehq.lib.charts.RadarChart
import com.wafflehq.lib.charts.RadarSeries
import com.wafflehq.lib.uicore.components.AppPillSelector
import com.wafflehq.lib.uicore.components.AppStatTile
import com.wafflehq.lib.uicore.theme.AppSpacing
import java.util.Locale

internal enum class ChartKind(@StringRes val labelRes: Int) {
    LINE(R.string.libex_charts_kind_line),
    STACKED(R.string.libex_charts_kind_stacked),
    CENTERED(R.string.libex_charts_kind_centered),
    RADAR(R.string.libex_charts_kind_radar),
}

internal object ChartsDemoData {

    val mood: List<Float> = listOf(-2f, -1f, 0f, 1.5f, 3f, 2f, 0.5f, -1f, -2.5f, -0.5f, 1f, 2f, 3.5f, 2.5f)

    val trend: List<Float> = mood.mapIndexed { index, _ ->
        val window = mood.subList((index - 1).coerceAtLeast(0), (index + 2).coerceAtMost(mood.size))
        window.average().toFloat()
    }

    val stacked: List<Triple<Float, Float, Float>> = listOf(
        Triple(3f, 2f, 1f), Triple(4f, 1f, 2f), Triple(2f, 3f, 2f), Triple(5f, 2f, 1f), Triple(3f, 3f, 3f),
        Triple(4f, 2f, 2f), Triple(6f, 1f, 1f), Triple(2f, 4f, 2f), Triple(3f, 2f, 4f), Triple(5f, 3f, 2f),
    )

    val balance: List<Float> = listOf(120f, -80f, 45f, 200f, -150f, 30f, -20f, 90f, -60f, 140f, 75f, -110f)

    val radarCurrent: List<Int> = listOf(7, 5, 8, 4, 6)
    val radarTarget: List<Int> = listOf(8, 8, 8, 8, 8)
    const val RADAR_MAX = 10
}

internal data class ChartSummary(val minimum: Float, val maximum: Float, val average: Float)

internal object ChartsDemoLogic {

    fun summary(kind: ChartKind): ChartSummary {
        val values = when (kind) {
            ChartKind.LINE -> ChartsDemoData.mood
            ChartKind.STACKED -> ChartsDemoData.stacked.map { it.first + it.second + it.third }
            ChartKind.CENTERED -> ChartsDemoData.balance
            ChartKind.RADAR -> ChartsDemoData.radarCurrent.map { it.toFloat() }
        }
        return ChartSummary(values.min(), values.max(), values.average().toFloat())
    }

    fun format(value: Float, locale: Locale = Locale.getDefault()): String = String.format(locale, "%.1f", value)
}

internal object ChartsTags {
    fun kind(kind: ChartKind) = "libex_charts_kind_${kind.name}"
    const val MINIMUM = "libex_charts_minimum"
    const val MAXIMUM = "libex_charts_maximum"
    const val AVERAGE = "libex_charts_average"
    const val CHART = "libex_charts_chart"
}

@Composable
internal fun ChartsDemo(initialKind: ChartKind = ChartKind.LINE) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var kind by remember { mutableStateOf(initialKind) }
    val summary = remember(kind) { ChartsDemoLogic.summary(kind) }
    val axisLabels = listOf(
        stringResource(R.string.libex_charts_axis_focus),
        stringResource(R.string.libex_charts_axis_energy),
        stringResource(R.string.libex_charts_axis_sleep),
        stringResource(R.string.libex_charts_axis_mood),
        stringResource(R.string.libex_charts_axis_activity),
    )
    val moodLabel = stringResource(R.string.libex_charts_series_mood)
    val trendLabel = stringResource(R.string.libex_charts_series_trend)

    DemoSection(
        id = "charts",
        titleRes = R.string.libex_charts_title,
        descriptionRes = R.string.libex_charts_desc,
        moduleRes = R.string.libex_module_charts,
    ) {
        AppPillSelector(
            options = ChartKind.entries,
            selected = kind,
            onSelect = { kind = it },
            label = { stringResource(it.labelRes) },
            modifier = Modifier.fillMaxWidth(),
            testTag = { ChartsTags.kind(it) },
        )
        Box(modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT).testTag(ChartsTags.CHART)) {
            when (kind) {
                ChartKind.LINE -> InteractiveMultiLineChart(
                    series = listOf(
                        LineChartSeries(label = moodLabel, color = scheme.primary, values = ChartsDemoData.mood),
                        LineChartSeries(label = trendLabel, color = scheme.tertiary, values = ChartsDemoData.trend),
                    ),
                    pointCount = ChartsDemoData.mood.size,
                    pointLabel = { (it + 1).toString() },
                    tooltipLines = { index ->
                        listOf(
                            context.getString(R.string.libex_charts_tooltip_day, index + 1),
                            "$moodLabel ${ChartsDemoLogic.format(ChartsDemoData.mood[index])}",
                            "$trendLabel ${ChartsDemoLogic.format(ChartsDemoData.trend[index])}",
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    yAxisTickSteps = listOf(1f, 2f, 5f),
                )
                ChartKind.STACKED -> InteractiveStackedBarChart(
                    points = ChartsDemoData.stacked.map { (first, second, third) ->
                        ChartPoint(
                            listOf(
                                ChartSegment(first, scheme.primary),
                                ChartSegment(second, scheme.secondary),
                                ChartSegment(third, scheme.tertiary),
                            )
                        )
                    },
                    pointLabel = { (it + 1).toString() },
                    tooltipLines = { index ->
                        val total = ChartsDemoData.stacked[index].let { it.first + it.second + it.third }
                        listOf(
                            context.getString(R.string.libex_charts_tooltip_day, index + 1),
                            context.getString(R.string.libex_charts_tooltip_total, ChartsDemoLogic.format(total)),
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                ChartKind.CENTERED -> InteractiveCenteredBarChart(
                    values = ChartsDemoData.balance,
                    pointLabel = { (it + 1).toString() },
                    tooltipLines = { index ->
                        listOf(
                            context.getString(R.string.libex_charts_tooltip_day, index + 1),
                            ChartsDemoLogic.format(ChartsDemoData.balance[index]),
                        )
                    },
                    positiveColor = scheme.primary,
                    negativeColor = scheme.error,
                    modifier = Modifier.fillMaxSize(),
                )
                ChartKind.RADAR -> RadarChart(
                    labels = axisLabels,
                    series = listOf(
                        RadarSeries(values = ChartsDemoData.radarCurrent, color = scheme.primary, filled = true),
                        RadarSeries(values = ChartsDemoData.radarTarget, color = scheme.tertiary, filled = false, dashed = true),
                    ),
                    maxValue = ChartsDemoData.RADAR_MAX,
                    gridColor = scheme.outline,
                    labelColor = scheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            AppStatTile(
                value = ChartsDemoLogic.format(summary.minimum),
                label = stringResource(R.string.libex_charts_min),
                modifier = Modifier.weight(1f).testTag(ChartsTags.MINIMUM),
            )
            AppStatTile(
                value = ChartsDemoLogic.format(summary.maximum),
                label = stringResource(R.string.libex_charts_max),
                modifier = Modifier.weight(1f).testTag(ChartsTags.MAXIMUM),
            )
            AppStatTile(
                value = ChartsDemoLogic.format(summary.average),
                label = stringResource(R.string.libex_charts_avg),
                modifier = Modifier.weight(1f).testTag(ChartsTags.AVERAGE),
            )
        }
    }
}

private val CHART_HEIGHT = 220.dp
