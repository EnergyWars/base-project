package com.wafflehq.base.ui.library.demos

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.lib.backupcore.BackupRetentionCandidate
import com.wafflehq.lib.backupcore.BackupRetentionMode
import com.wafflehq.lib.backupcore.BackupRetentionPolicy
import com.wafflehq.lib.backupcore.BackupVersionTooNewException
import com.wafflehq.lib.backupcore.BackupVersionValidator
import com.wafflehq.lib.uicore.components.AppBanner
import com.wafflehq.lib.uicore.components.AppBannerRole
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppPillSelector
import com.wafflehq.lib.uicore.theme.AppSpacing
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

internal object BackupCoreDemoLogic {

    val AGE_DAYS: List<Long> = listOf(0, 1, 2, 3, 5, 8, 12, 19, 27, 40, 60, 90)
    const val MANUAL_INDEX = 4

    fun candidates(now: Instant): List<BackupRetentionCandidate> = AGE_DAYS.mapIndexed { index, days ->
        BackupRetentionCandidate(
            id = "backup-${index + 1}",
            createdAt = now.minus(days, ChronoUnit.DAYS),
            isManual = index == MANUAL_INDEX,
        )
    }

    fun idsToDelete(
        candidates: List<BackupRetentionCandidate>,
        enabled: Boolean,
        mode: BackupRetentionMode,
        maxCount: Int,
        maxAgeDays: Int,
        now: Instant,
    ): Set<String> = BackupRetentionPolicy.idsToDelete(
        candidates = candidates,
        enabled = enabled,
        mode = mode,
        maxCount = maxCount,
        maxAgeDays = maxAgeDays,
        now = now,
    )

    fun ageInDays(candidate: BackupRetentionCandidate, now: Instant): Long =
        Duration.between(candidate.createdAt, now).toDays()

    fun versionTooNew(backupVersion: Int, currentVersion: Int): BackupVersionTooNewException? =
        runCatching { BackupVersionValidator.validate(backupVersion, currentVersion) }
            .exceptionOrNull() as? BackupVersionTooNewException

    @StringRes
    fun modeLabel(mode: BackupRetentionMode): Int = when (mode) {
        BackupRetentionMode.COUNT -> R.string.libex_backupcore_mode_count
        BackupRetentionMode.AGE -> R.string.libex_backupcore_mode_age
        BackupRetentionMode.GENERATIONAL -> R.string.libex_backupcore_mode_generational
    }
}

internal object BackupCoreTags {
    const val ENABLED = "libex_backupcore_enabled"
    const val MAX_COUNT = "libex_backupcore_max_count"
    const val MAX_AGE = "libex_backupcore_max_age"
    const val SUMMARY = "libex_backupcore_summary"
    const val BACKUP_VERSION = "libex_backupcore_backup_version"
    const val APP_VERSION = "libex_backupcore_app_version"
    const val VERSION_RESULT = "libex_backupcore_version_result"
    fun mode(mode: BackupRetentionMode) = "libex_backupcore_mode_${mode.name}"
    fun candidate(id: String) = "libex_backupcore_candidate_$id"
    fun decision(id: String) = "libex_backupcore_decision_$id"
}

@Composable
internal fun BackupCoreDemo(now: Instant = remember { Instant.now() }) {
    var enabled by remember { mutableStateOf(true) }
    var mode by remember { mutableStateOf(BackupRetentionMode.COUNT) }
    var maxCount by remember { mutableIntStateOf(5) }
    var maxAgeDays by remember { mutableIntStateOf(14) }
    var backupVersion by remember { mutableIntStateOf(4) }
    var appVersion by remember { mutableIntStateOf(3) }
    val candidates = remember(now) { BackupCoreDemoLogic.candidates(now) }
    val toDelete = BackupCoreDemoLogic.idsToDelete(candidates, enabled, mode, maxCount, maxAgeDays, now)
    val tooNew = BackupCoreDemoLogic.versionTooNew(backupVersion, appVersion)

    DemoSection(
        id = "backupcore",
        titleRes = R.string.libex_backupcore_title,
        descriptionRes = R.string.libex_backupcore_desc,
        moduleRes = R.string.libex_module_backupcore,
    ) {
        DemoSwitchRow(
            label = stringResource(R.string.libex_backupcore_enabled),
            checked = enabled,
            onCheckedChange = { enabled = it },
            tag = BackupCoreTags.ENABLED,
        )
        AppPillSelector(
            options = BackupRetentionMode.entries,
            selected = mode,
            onSelect = { mode = it },
            label = { stringResource(BackupCoreDemoLogic.modeLabel(it)) },
            modifier = Modifier.fillMaxWidth(),
            testTag = { BackupCoreTags.mode(it) },
        )
        DemoStepperRow(
            label = stringResource(R.string.libex_backupcore_max_count),
            value = maxCount,
            range = 1..BackupCoreDemoLogic.AGE_DAYS.size,
            step = 1,
            onValueChange = { maxCount = it },
            tag = BackupCoreTags.MAX_COUNT,
        )
        DemoStepperRow(
            label = stringResource(R.string.libex_backupcore_max_age),
            value = maxAgeDays,
            range = 1..90,
            step = 1,
            onValueChange = { maxAgeDays = it },
            tag = BackupCoreTags.MAX_AGE,
        )
        candidates.forEach { candidate ->
            val delete = candidate.id in toDelete
            Row(
                modifier = Modifier.fillMaxWidth().testTag(BackupCoreTags.candidate(candidate.id)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = stringResource(
                        R.string.libex_backupcore_candidate,
                        candidate.id,
                        BackupCoreDemoLogic.ageInDays(candidate, now),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                if (candidate.isManual) {
                    DemoStatusPill(stringResource(R.string.libex_backupcore_manual), DemoTone.Info)
                }
                DemoStatusPill(
                    text = stringResource(if (delete) R.string.libex_backupcore_delete else R.string.libex_backupcore_keep),
                    tone = if (delete) DemoTone.Error else DemoTone.Success,
                    modifier = Modifier.testTag(BackupCoreTags.decision(candidate.id)),
                )
            }
        }
        DemoBodyText(
            text = stringResource(R.string.libex_backupcore_summary, toDelete.size, candidates.size - toDelete.size),
            modifier = Modifier.testTag(BackupCoreTags.SUMMARY),
        )
        AppHorizontalDivider()
        DemoLabelText(stringResource(R.string.libex_backupcore_version_heading))
        DemoStepperRow(
            label = stringResource(R.string.libex_backupcore_backup_version),
            value = backupVersion,
            range = 1..10,
            step = 1,
            onValueChange = { backupVersion = it },
            tag = BackupCoreTags.BACKUP_VERSION,
        )
        DemoStepperRow(
            label = stringResource(R.string.libex_backupcore_app_version),
            value = appVersion,
            range = 1..10,
            step = 1,
            onValueChange = { appVersion = it },
            tag = BackupCoreTags.APP_VERSION,
        )
        AppBanner(
            text = if (tooNew == null) {
                stringResource(R.string.libex_backupcore_version_ok)
            } else {
                stringResource(R.string.libex_backupcore_version_too_new, tooNew.backupVersionCode, tooNew.currentVersionCode)
            },
            role = if (tooNew == null) AppBannerRole.Secondary else AppBannerRole.Error,
            modifier = Modifier.fillMaxWidth().testTag(BackupCoreTags.VERSION_RESULT),
        )
    }
}
