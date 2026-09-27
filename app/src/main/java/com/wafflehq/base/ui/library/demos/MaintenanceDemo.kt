package com.wafflehq.base.ui.library.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.lib.maintenance.MaintenanceScheduleConfig
import com.wafflehq.lib.maintenance.MaintenanceTask
import com.wafflehq.lib.maintenance.MaintenanceTaskRunner
import com.wafflehq.lib.maintenance.MaintenanceWorkExecutor
import com.wafflehq.lib.maintenance.NextFireTime
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.time.TimeFormats
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal class DemoMaintenanceTask(
    override val id: String,
    override val order: Int,
    private val enabled: Boolean,
    private val failure: Throwable? = null,
) : MaintenanceTask {

    override suspend fun isEnabled(): Boolean = enabled

    override suspend fun run() {
        failure?.let { throw it }
    }
}

internal object MaintenanceDemoLogic {

    const val WORK_NAME = "libex_maintenance"
    const val FAILING_TASK_ID = "broken"
    const val DORMANT_TASK_ID = "dormant"

    fun tasks(failureMessage: String): Set<MaintenanceTask> = setOf(
        DemoMaintenanceTask("cleanup", order = 10, enabled = true),
        DemoMaintenanceTask("sync", order = 20, enabled = true),
        DemoMaintenanceTask(FAILING_TASK_ID, order = 30, enabled = true, failure = IllegalStateException(failureMessage)),
        DemoMaintenanceTask(DORMANT_TASK_ID, order = 40, enabled = false),
    )

    fun scheduleConfig(intervalMinutes: Int, flexMinutes: Int) = MaintenanceScheduleConfig(
        uniqueWorkName = WORK_NAME,
        tag = WORK_NAME,
        intervalMinutes = intervalMinutes.toLong(),
        flexMinutes = flexMinutes.toLong(),
    )

    fun hoursAndMinutes(delayMillis: Long): Pair<Long, Long> {
        val duration = Duration.ofMillis(delayMillis.coerceAtLeast(0))
        return duration.toHours() to (duration.toMinutes() % MINUTES_PER_HOUR)
    }

    private const val MINUTES_PER_HOUR = 60L
}

internal object MaintenanceTags {
    const val RUN_ALL = "libex_maintenance_run_all"
    const val RUN_EXECUTOR = "libex_maintenance_run_executor"
    const val ANY_ENABLED = "libex_maintenance_any_enabled"
    const val EXECUTOR_RESULT = "libex_maintenance_executor_result"
    const val NEXT_FIRE = "libex_maintenance_next_fire"
    const val DELAY = "libex_maintenance_delay"
    const val CONFIG = "libex_maintenance_config"
    const val INTERVAL = "libex_maintenance_interval"
    const val FLEX = "libex_maintenance_flex"
    const val FIRE_HOUR = "libex_maintenance_fire_hour"
    const val FIRE_MINUTE = "libex_maintenance_fire_minute"
    fun outcome(taskId: String) = "libex_maintenance_outcome_$taskId"
}

@Composable
internal fun MaintenanceDemo(now: ZonedDateTime = remember { ZonedDateTime.now() }) {
    val scope = rememberCoroutineScope()
    val failureMessage = stringResource(R.string.libex_maintenance_failure_message)
    val tasks = remember(failureMessage) { MaintenanceDemoLogic.tasks(failureMessage) }
    val outcomes = remember { mutableStateListOf<MaintenanceTaskRunner.Outcome>() }
    var anyEnabled by remember { mutableStateOf<Boolean?>(null) }
    var executorFailures by remember { mutableStateOf<List<String>?>(null) }
    var intervalMinutes by remember { mutableIntStateOf(MaintenanceScheduleConfig.DEFAULT_INTERVAL_MINUTES.toInt()) }
    var flexMinutes by remember { mutableIntStateOf(MaintenanceScheduleConfig.DEFAULT_FLEX_MINUTES.toInt()) }
    var fireHour by remember { mutableIntStateOf(3) }
    var fireMinute by remember { mutableIntStateOf(30) }
    val dateTimeFormatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT) }

    LaunchedEffect(tasks) { anyEnabled = MaintenanceTaskRunner(tasks).anyEnabled() }

    val config = MaintenanceDemoLogic.scheduleConfig(intervalMinutes, flexMinutes)
    val nextFire = NextFireTime.at(fireHour, fireMinute, now.zone, now)
    val (delayHours, delayMinutes) = MaintenanceDemoLogic.hoursAndMinutes(
        NextFireTime.delayMillis(fireHour, fireMinute, now.zone, now)
    )

    DemoSection(
        id = "maintenance",
        titleRes = R.string.libex_maintenance_title,
        descriptionRes = R.string.libex_maintenance_desc,
        moduleRes = R.string.libex_module_maintenance,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            AppButton(
                text = stringResource(R.string.libex_maintenance_run_all),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Tonal,
                onClick = {
                    scope.launch {
                        val result = MaintenanceTaskRunner(tasks).runAll()
                        outcomes.clear()
                        outcomes.addAll(result)
                    }
                },
                modifier = Modifier.testTag(MaintenanceTags.RUN_ALL),
            )
            AppButton(
                text = stringResource(R.string.libex_maintenance_run_executor),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Outlined,
                onClick = {
                    scope.launch {
                        val failed = mutableListOf<String>()
                        MaintenanceWorkExecutor(tasks) { taskId, _ -> failed += taskId }.execute()
                        executorFailures = failed
                    }
                },
                modifier = Modifier.testTag(MaintenanceTags.RUN_EXECUTOR),
            )
        }
        val enabledState = anyEnabled
        if (enabledState != null) {
            DemoMetaText(
                text = stringResource(
                    if (enabledState) R.string.libex_maintenance_scheduler_active else R.string.libex_maintenance_scheduler_idle
                ),
                modifier = Modifier.testTag(MaintenanceTags.ANY_ENABLED),
            )
        }
        outcomes.forEach { outcome ->
            Row(
                modifier = Modifier.fillMaxWidth().testTag(MaintenanceTags.outcome(outcome.taskId)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                val error = outcome.error
                when {
                    !outcome.ran -> DemoStatusPill(stringResource(R.string.libex_maintenance_outcome_skipped), DemoTone.Neutral)
                    error == null -> DemoStatusPill(stringResource(R.string.libex_maintenance_outcome_ok), DemoTone.Success)
                    else -> DemoStatusPill(
                        stringResource(R.string.libex_maintenance_outcome_failed, error.message.orEmpty()),
                        DemoTone.Error,
                    )
                }
                Text(
                    text = outcome.taskId,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        executorFailures?.let { failed ->
            DemoBodyText(
                text = stringResource(R.string.libex_maintenance_executor_failures, failed.size, failed.joinToString()),
                modifier = Modifier.testTag(MaintenanceTags.EXECUTOR_RESULT),
            )
        }
        AppHorizontalDivider()
        DemoLabelText(stringResource(R.string.libex_maintenance_schedule_heading))
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            DemoStepperRow(
                label = stringResource(R.string.libex_maintenance_interval),
                value = intervalMinutes,
                range = 15..180,
                step = 15,
                onValueChange = { intervalMinutes = it },
                tag = MaintenanceTags.INTERVAL,
            )
            DemoStepperRow(
                label = stringResource(R.string.libex_maintenance_flex),
                value = flexMinutes,
                range = 5..60,
                step = 5,
                onValueChange = { flexMinutes = it },
                tag = MaintenanceTags.FLEX,
            )
            DemoBodyText(
                text = stringResource(
                    R.string.libex_maintenance_config,
                    config.uniqueWorkName,
                    config.intervalMinutes,
                    config.flexMinutes,
                ),
                modifier = Modifier.testTag(MaintenanceTags.CONFIG),
            )
        }
        AppHorizontalDivider()
        DemoLabelText(stringResource(R.string.libex_maintenance_fire_heading))
        DemoStepperRow(
            label = stringResource(R.string.libex_maintenance_fire_hour),
            value = fireHour,
            range = 0..23,
            step = 1,
            onValueChange = { fireHour = it },
            tag = MaintenanceTags.FIRE_HOUR,
        )
        DemoStepperRow(
            label = stringResource(R.string.libex_maintenance_fire_minute),
            value = fireMinute,
            range = 0..55,
            step = 5,
            onValueChange = { fireMinute = it },
            tag = MaintenanceTags.FIRE_MINUTE,
        )
        DemoKeyValue(
            label = stringResource(R.string.libex_maintenance_next_fire),
            value = nextFire.format(dateTimeFormatter),
            modifier = Modifier.testTag(MaintenanceTags.NEXT_FIRE),
        )
        DemoKeyValue(
            label = stringResource(R.string.libex_maintenance_delay),
            value = stringResource(R.string.libex_maintenance_delay_value, delayHours, delayMinutes),
            modifier = Modifier.testTag(MaintenanceTags.DELAY),
        )
        DemoMetaText(
            text = stringResource(
                R.string.libex_maintenance_tomorrow,
                NextFireTime.tomorrowAt(fireHour, fireMinute, now.zone, now).toLocalTime().format(TimeFormats.HOUR_MINUTE),
            )
        )
    }
}
