package com.wafflehq.base.ui.library.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppAssistChip
import com.wafflehq.lib.uicore.components.AppBanner
import com.wafflehq.lib.uicore.components.AppBannerRole
import com.wafflehq.lib.uicore.components.AppCircularProgress
import com.wafflehq.lib.uicore.components.AppEmptyState
import com.wafflehq.lib.uicore.components.AppFilterChip
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppLabelChip
import com.wafflehq.lib.uicore.components.AppLinearProgress
import com.wafflehq.lib.uicore.components.AppSlider
import com.wafflehq.lib.uicore.components.AppStatTile
import com.wafflehq.lib.uicore.components.AppStatTileEmphasis
import com.wafflehq.lib.uicore.components.DeleteConfirmationDialog
import com.wafflehq.lib.uicore.components.ErrorDetailDialog
import com.wafflehq.lib.uicore.components.NameInputDialog
import com.wafflehq.lib.uicore.components.detailText
import com.wafflehq.lib.uicore.theme.AppSpacing
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal object UiCoreFeedbackLogic {

    const val MAX_COUNT = 20

    fun doubled(count: Int): Int = count * 2

    fun percent(progress: Float): Int = (progress.coerceIn(0f, 1f) * PERCENT_FACTOR).roundToInt()

    fun demoError(message: String): Throwable =
        IllegalStateException(message, IllegalArgumentException(message))

    fun errorDetail(message: String): String = demoError(message).detailText(message)

    private const val PERCENT_FACTOR = 100
}

internal object UiCoreFeedbackTags {
    const val COUNT = "libex_uicore_count"
    const val STEPPER = "libex_uicore_stepper"
    const val DOUBLED = "libex_uicore_doubled"
    const val EMPTY_ACTION = "libex_uicore_empty_action"
    const val FILTER_ONE = "libex_uicore_filter_one"
    const val FILTER_STATE = "libex_uicore_filter_state"
    const val SLIDER = "libex_uicore_slider"
    const val PERCENT = "libex_uicore_percent"
    const val DELETE = "libex_uicore_delete"
    const val RENAME = "libex_uicore_rename"
    const val ERROR = "libex_uicore_error"
    const val SNACKBAR = "libex_uicore_snackbar"
    const val DIALOG_RESULT = "libex_uicore_dialog_result"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun UiCoreFeedbackDemo(snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()
    var count by remember { mutableIntStateOf(0) }
    var progress by remember { mutableFloatStateOf(0.4f) }
    var filterOne by remember { mutableStateOf(true) }
    var assistClicks by remember { mutableIntStateOf(0) }
    var showDelete by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var dialogResult by remember { mutableStateOf<String?>(null) }
    val snackbarMessage = stringResource(R.string.libex_uicore_snackbar_message)
    val deletedText = stringResource(R.string.libex_uicore_dialog_deleted)
    val renamedTemplate = stringResource(R.string.libex_uicore_dialog_renamed)
    val errorMessage = stringResource(R.string.libex_uicore_error_message)

    DemoSection(
        id = "uicore_feedback",
        titleRes = R.string.libex_uicore_feedback_title,
        descriptionRes = R.string.libex_uicore_feedback_desc,
        moduleRes = R.string.libex_module_uicore,
    ) {
        AppBanner(
            text = stringResource(R.string.libex_uicore_banner_info),
            role = AppBannerRole.Secondary,
            icon = Icons.Filled.Info,
            modifier = Modifier.fillMaxWidth(),
        )
        AppBanner(
            text = stringResource(R.string.libex_uicore_banner_warning),
            role = AppBannerRole.Warning,
            icon = Icons.Filled.Warning,
            modifier = Modifier.fillMaxWidth(),
        )
        AppBanner(
            text = stringResource(R.string.libex_uicore_banner_error),
            role = AppBannerRole.Error,
            icon = Icons.Filled.ErrorOutline,
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            AppFilterChip(
                selected = filterOne,
                onClick = { filterOne = !filterOne },
                label = { Text(stringResource(R.string.libex_uicore_chip_filter)) },
                modifier = Modifier.testTag(UiCoreFeedbackTags.FILTER_ONE),
            )
            AppAssistChip(
                onClick = { assistClicks++ },
                label = { Text(stringResource(R.string.libex_uicore_chip_assist, assistClicks)) },
            )
            AppLabelChip(text = stringResource(R.string.libex_uicore_chip_label))
            DemoStatusPill(stringResource(R.string.libex_uicore_pill), DemoTone.Info)
        }
        DemoMetaText(
            text = stringResource(if (filterOne) R.string.libex_value_yes else R.string.libex_value_no),
            modifier = Modifier.testTag(UiCoreFeedbackTags.FILTER_STATE),
        )
        AppHorizontalDivider()
        DemoStepperRow(
            label = stringResource(R.string.libex_uicore_counter),
            value = count,
            range = 0..UiCoreFeedbackLogic.MAX_COUNT,
            step = 1,
            onValueChange = { count = it },
            tag = UiCoreFeedbackTags.STEPPER,
        )
        if (count == 0) {
            AppEmptyState(
                text = stringResource(R.string.libex_uicore_empty_text),
                icon = Icons.Filled.Inbox,
                actionLabel = stringResource(R.string.libex_uicore_empty_action),
                onAction = { count = 1 },
                actionModifier = Modifier.testTag(UiCoreFeedbackTags.EMPTY_ACTION),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                AppStatTile(
                    value = count.toString(),
                    label = stringResource(R.string.libex_uicore_stat_count),
                    modifier = Modifier.testTag(UiCoreFeedbackTags.COUNT),
                )
                AppStatTile(
                    value = UiCoreFeedbackLogic.doubled(count).toString(),
                    label = stringResource(R.string.libex_uicore_stat_doubled),
                    emphasis = AppStatTileEmphasis.Prominent,
                    modifier = Modifier.testTag(UiCoreFeedbackTags.DOUBLED),
                )
            }
        }
        AppHorizontalDivider()
        AppSlider(
            value = progress,
            onValueChange = { progress = it },
            modifier = Modifier.testTag(UiCoreFeedbackTags.SLIDER),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            AppLinearProgress(progress = { progress }, modifier = Modifier.weight(1f))
            AppCircularProgress(progress = { progress })
            Text(
                text = stringResource(R.string.libex_value_percent, UiCoreFeedbackLogic.percent(progress)),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag(UiCoreFeedbackTags.PERCENT),
            )
        }
        AppHorizontalDivider()
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            AppButton(
                text = stringResource(R.string.libex_uicore_dialog_delete),
                role = AppButtonRole.Error,
                variant = AppButtonVariant.Tonal,
                onClick = { showDelete = true },
                modifier = Modifier.testTag(UiCoreFeedbackTags.DELETE),
            )
            AppButton(
                text = stringResource(R.string.libex_uicore_dialog_rename),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Tonal,
                onClick = { showRename = true },
                modifier = Modifier.testTag(UiCoreFeedbackTags.RENAME),
            )
            AppButton(
                text = stringResource(R.string.libex_uicore_dialog_error),
                role = AppButtonRole.Warning,
                variant = AppButtonVariant.Outlined,
                onClick = { showError = true },
                modifier = Modifier.testTag(UiCoreFeedbackTags.ERROR),
            )
            AppButton(
                text = stringResource(R.string.libex_uicore_snackbar),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Outlined,
                onClick = { scope.launch { snackbarHostState.showSnackbar(snackbarMessage) } },
                modifier = Modifier.testTag(UiCoreFeedbackTags.SNACKBAR),
            )
        }
        dialogResult?.let {
            DemoBodyText(text = it, modifier = Modifier.testTag(UiCoreFeedbackTags.DIALOG_RESULT))
        }
    }

    if (showDelete) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.libex_uicore_dialog_delete_title),
            message = stringResource(R.string.libex_uicore_dialog_delete_message),
            onConfirm = {
                dialogResult = deletedText
                showDelete = false
            },
            onDismiss = { showDelete = false },
        )
    }
    if (showRename) {
        NameInputDialog(
            title = stringResource(R.string.libex_uicore_dialog_rename_title),
            label = stringResource(R.string.libex_uicore_dialog_rename_label),
            initialName = stringResource(R.string.libex_uicore_dialog_rename_initial),
            onConfirm = {
                dialogResult = renamedTemplate.format(it)
                showRename = false
            },
            onDismiss = { showRename = false },
        )
    }
    if (showError) {
        ErrorDetailDialog(
            title = stringResource(R.string.libex_uicore_dialog_error_title),
            message = errorMessage,
            dismissText = stringResource(R.string.libex_action_close),
            onDismiss = { showError = false },
            icon = Icons.Filled.ErrorOutline,
            detail = UiCoreFeedbackLogic.errorDetail(errorMessage),
        )
    }
}
