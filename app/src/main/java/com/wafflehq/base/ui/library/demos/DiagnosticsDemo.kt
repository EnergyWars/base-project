package com.wafflehq.base.ui.library.demos

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.wafflehq.base.R
import com.wafflehq.lib.diagnostics.DiagnosticLogEntry
import com.wafflehq.lib.diagnostics.DiagnosticLogFormatter
import com.wafflehq.lib.diagnostics.DiagnosticLogSink
import com.wafflehq.lib.diagnostics.DiagnosticLogger
import com.wafflehq.lib.diagnostics.FreezeDetector
import com.wafflehq.lib.diagnostics.FreezeTransition
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppSlider
import com.wafflehq.lib.uicore.theme.AppSpacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

internal class InMemoryDiagnosticSink(
    val entries: SnapshotStateList<DiagnosticLogEntry> = mutableStateListOf(),
) : DiagnosticLogSink {

    override suspend fun log(entry: DiagnosticLogEntry) {
        entries.add(entry)
    }
}

internal object DiagnosticsDemoLogic {

    const val TAG = "Demo"
    const val LOGCAT_TAG = "LibraryExamples"
    const val FROZEN_NOW_MS = 100_000L
    const val MAX_BLOCK_MS = 6_000f
    const val BLOCK_STEPS = 11
    const val MAIN_THREAD_STACK = "at android.os.Looper.loop(Looper.java)"

    fun formatted(entries: List<DiagnosticLogEntry>, placeholder: String): String =
        DiagnosticLogFormatter.format(entries, placeholder)

    fun evaluate(detector: FreezeDetector, blockedMs: Float): FreezeTransition =
        detector.evaluate(lastHeartbeatMs = FROZEN_NOW_MS - blockedMs.toLong(), nowMs = FROZEN_NOW_MS)

    @StringRes
    fun transitionLabel(transition: FreezeTransition): Int = when (transition) {
        FreezeTransition.NONE -> R.string.libex_diagnostics_transition_none
        FreezeTransition.STARTED -> R.string.libex_diagnostics_transition_started
        FreezeTransition.RESOLVED -> R.string.libex_diagnostics_transition_resolved
    }
}

internal object DiagnosticsTags {
    const val LOG = "libex_diagnostics_log"
    const val DEBUG = "libex_diagnostics_debug"
    const val WARN = "libex_diagnostics_warn"
    const val ERROR = "libex_diagnostics_error"
    const val CLEAR = "libex_diagnostics_clear"
    const val SLIDER = "libex_diagnostics_slider"
    const val FREEZE_STATE = "libex_diagnostics_freeze_state"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DiagnosticsDemo(sink: InMemoryDiagnosticSink = remember { InMemoryDiagnosticSink() }) {
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Unconfined) }
    DisposableEffect(scope) { onDispose { scope.cancel() } }
    val logger = remember(sink) {
        DiagnosticLogger(sink = sink, logcatTag = DiagnosticsDemoLogic.LOGCAT_TAG, scope = scope)
    }
    val detector = remember { FreezeDetector() }
    var blockedMs by remember { mutableFloatStateOf(0f) }
    var transition by remember { mutableStateOf(FreezeTransition.NONE) }
    val empty = stringResource(R.string.libex_diagnostics_empty)
    val debugMessage = stringResource(R.string.libex_diagnostics_message_debug)
    val warnMessage = stringResource(R.string.libex_diagnostics_message_warn)
    val errorMessage = stringResource(R.string.libex_diagnostics_message_error)
    val freezeMessage = stringResource(R.string.libex_diagnostics_message_freeze)
    val formatted = DiagnosticsDemoLogic.formatted(sink.entries.toList(), empty)

    DemoSection(
        id = "diagnostics",
        titleRes = R.string.libex_diagnostics_title,
        descriptionRes = R.string.libex_diagnostics_desc,
        moduleRes = R.string.libex_module_diagnostics,
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            AppButton(
                text = stringResource(R.string.libex_diagnostics_log_debug),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Tonal,
                onClick = { logger.d(DiagnosticsDemoLogic.TAG, debugMessage) },
                modifier = Modifier.testTag(DiagnosticsTags.DEBUG),
            )
            AppButton(
                text = stringResource(R.string.libex_diagnostics_log_warn),
                role = AppButtonRole.Warning,
                variant = AppButtonVariant.Tonal,
                onClick = { logger.w(DiagnosticsDemoLogic.TAG, warnMessage) },
                modifier = Modifier.testTag(DiagnosticsTags.WARN),
            )
            AppButton(
                text = stringResource(R.string.libex_diagnostics_log_error),
                role = AppButtonRole.Error,
                variant = AppButtonVariant.Tonal,
                onClick = { logger.e(DiagnosticsDemoLogic.TAG, errorMessage, IllegalStateException(errorMessage)) },
                modifier = Modifier.testTag(DiagnosticsTags.ERROR),
            )
            AppButton(
                text = stringResource(R.string.libex_action_clear),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                enabled = sink.entries.isNotEmpty(),
                onClick = { sink.entries.clear() },
                modifier = Modifier.testTag(DiagnosticsTags.CLEAR),
            )
        }
        Text(
            text = formatted,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = LOG_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().testTag(DiagnosticsTags.LOG),
        )
        AppHorizontalDivider()
        DemoLabelText(stringResource(R.string.libex_diagnostics_freeze_heading))
        DemoKeyValue(
            label = stringResource(R.string.libex_diagnostics_blocked),
            value = stringResource(R.string.libex_diagnostics_blocked_value, blockedMs.toInt()),
        )
        AppSlider(
            value = blockedMs,
            onValueChange = {
                blockedMs = it
                transition = DiagnosticsDemoLogic.evaluate(detector, it)
                if (transition == FreezeTransition.STARTED) {
                    logger.freeze(DiagnosticsDemoLogic.TAG, freezeMessage, DiagnosticsDemoLogic.MAIN_THREAD_STACK)
                }
            },
            valueRange = 0f..DiagnosticsDemoLogic.MAX_BLOCK_MS,
            steps = DiagnosticsDemoLogic.BLOCK_STEPS,
            modifier = Modifier.testTag(DiagnosticsTags.SLIDER),
        )
        DemoBodyText(
            text = stringResource(
                R.string.libex_diagnostics_freeze_state,
                stringResource(if (detector.isFrozen) R.string.libex_value_yes else R.string.libex_value_no),
                stringResource(DiagnosticsDemoLogic.transitionLabel(transition)),
                FreezeDetector.DEFAULT_THRESHOLD_MS.toInt(),
            ),
            modifier = Modifier.testTag(DiagnosticsTags.FREEZE_STATE),
        )
    }
}

private const val LOG_MAX_LINES = 12
