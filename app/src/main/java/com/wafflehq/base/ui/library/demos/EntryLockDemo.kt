package com.wafflehq.base.ui.library.demos

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.lib.entrylock.AuthResult
import com.wafflehq.lib.entrylock.BiometricEntryAuthenticator
import com.wafflehq.lib.entrylock.EntryLockController
import com.wafflehq.lib.entrylock.EntryLockRepository
import com.wafflehq.lib.entrylock.EntryLockSession
import com.wafflehq.lib.entrylock.EntryLockState
import com.wafflehq.lib.entrylock.GuardedEntryAuthenticator
import com.wafflehq.lib.entrylock.entryRevealState
import com.wafflehq.lib.entrylock.ui.EntryLockOverflowMenu
import com.wafflehq.lib.entrylock.ui.EntryLockStatusIcon
import com.wafflehq.lib.entrylock.ui.EntrySelectionTopBarActions
import com.wafflehq.lib.entrylock.ui.LockedEntryRow
import com.wafflehq.lib.entrylock.ui.entryAuthLauncher
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.components.AppCheckbox
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.time.TimeFormats
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal data class DemoLockEntry(
    val id: Long,
    @StringRes val titleRes: Int,
    val locked: Boolean,
)

internal sealed interface EntryLockAction {
    data class Reveal(val entryId: Long) : EntryLockAction
    data object UnlockAll : EntryLockAction
    data object SessionUnlock : EntryLockAction
    data object BulkUnlock : EntryLockAction
}

internal class DemoEntryLockRepository(
    private val entries: SnapshotStateList<DemoLockEntry>,
) : EntryLockRepository {

    override suspend fun setAllLocked(locked: Boolean) {
        entries.indices.forEach { index -> entries[index] = entries[index].copy(locked = locked) }
    }

    override suspend fun setLocked(ids: Set<Long>, locked: Boolean) {
        entries.indices.forEach { index ->
            if (entries[index].id in ids) entries[index] = entries[index].copy(locked = locked)
        }
    }
}

internal object EntryLockDemoLogic {

    const val SESSION_MILLIS = 30_000L

    fun initialEntries(): List<DemoLockEntry> = listOf(
        DemoLockEntry(1L, R.string.libex_entrylock_entry_one, locked = true),
        DemoLockEntry(2L, R.string.libex_entrylock_entry_two, locked = true),
        DemoLockEntry(3L, R.string.libex_entrylock_entry_three, locked = false),
    )

    fun applyConfirmed(action: EntryLockAction, controller: EntryLockController) {
        when (action) {
            is EntryLockAction.Reveal -> controller.onTemporaryUnlockConfirmed(action.entryId)
            EntryLockAction.UnlockAll -> controller.onUnlockAllEntriesConfirmed()
            EntryLockAction.SessionUnlock -> controller.onSessionUnlockConfirmed()
            EntryLockAction.BulkUnlock -> controller.onBulkUnlockSelectedConfirmed()
        }
    }

    @StringRes
    fun authResultLabel(result: AuthResult): Int = when (result) {
        AuthResult.SUCCESS -> R.string.libex_entrylock_result_success
        AuthResult.FAILED -> R.string.libex_entrylock_result_failed
        AuthResult.CANCELLED -> R.string.libex_entrylock_result_cancelled
        AuthResult.NOT_AVAILABLE -> R.string.libex_entrylock_result_unavailable
    }
}

internal object EntryLockTags {
    const val MENU = "libex_entrylock_menu"
    const val SIMULATED = "libex_entrylock_simulated"
    const val DEVICE_TEST = "libex_entrylock_device_test"
    const val AUTH_RESULT = "libex_entrylock_auth_result"
    const val SESSION = "libex_entrylock_session"
    const val CANCEL_SELECTION = "libex_entrylock_cancel_selection"
    fun entry(id: Long) = "libex_entrylock_entry_$id"
    fun checkbox(id: Long) = "libex_entrylock_checkbox_$id"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun EntryLockDemo(baseDate: LocalDate = LocalDate.now()) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val entries = remember { mutableStateListOf<DemoLockEntry>().apply { addAll(EntryLockDemoLogic.initialEntries()) } }
    val repository = remember { DemoEntryLockRepository(entries) }
    val session = remember { EntryLockSession(scope = scope, sessionDurationMillis = EntryLockDemoLogic.SESSION_MILLIS) }
    val controller = remember { EntryLockController(scope, repository, session) }
    val lockState by controller.state.collectAsState(initial = EntryLockState())
    val deviceAuthenticator = remember { GuardedEntryAuthenticator(BiometricEntryAuthenticator(context), session) }
    var simulated by remember { mutableStateOf(true) }
    var menuOpen by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<EntryLockAction?>(null) }
    var lastResult by remember { mutableStateOf<AuthResult?>(null) }
    val authTitle = stringResource(R.string.libex_entrylock_auth_title)
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    fun onAuthResult(result: AuthResult) {
        lastResult = result
        val action = pending
        pending = null
        if (result == AuthResult.SUCCESS && action != null) EntryLockDemoLogic.applyConfirmed(action, controller)
    }

    val launchDeviceAuth = entryAuthLauncher(
        authenticate = { activity, title -> deviceAuthenticator.authenticate(activity, title) },
        onResult = { onAuthResult(it) },
    )

    fun request(action: EntryLockAction) {
        pending = action
        if (simulated) onAuthResult(AuthResult.SUCCESS) else launchDeviceAuth(authTitle)
    }

    DemoSection(
        id = "entrylock",
        titleRes = R.string.libex_entrylock_title,
        descriptionRes = R.string.libex_entrylock_desc,
        moduleRes = R.string.libex_module_entrylock,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            DemoStatusPill(
                text = stringResource(
                    if (lockState.sessionUnlocked) R.string.libex_entrylock_session_open else R.string.libex_entrylock_session_locked
                ),
                tone = if (lockState.sessionUnlocked) DemoTone.Success else DemoTone.Neutral,
                modifier = Modifier.weight(1f).testTag(EntryLockTags.SESSION),
            )
            if (lockState.selectionMode) {
                EntrySelectionTopBarActions(
                    selectedCount = lockState.selectedIds.size,
                    onSelectAll = { controller.onSelectAll(entries.map { it.id }.toSet()) },
                    onLockSelected = controller::onBulkLockSelected,
                    onUnlockSelected = { request(EntryLockAction.BulkUnlock) },
                    contentColor = MaterialTheme.colorScheme.primary,
                )
                AppButton(
                    text = stringResource(R.string.libex_action_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = controller::onExitSelectionMode,
                    modifier = Modifier.testTag(EntryLockTags.CANCEL_SELECTION),
                )
            }
            Box {
                AppIconButton(
                    icon = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.libex_action_more),
                    role = AppButtonRole.Neutral,
                    onClick = { menuOpen = true },
                    modifier = Modifier.testTag(EntryLockTags.MENU),
                )
                EntryLockOverflowMenu(
                    expanded = menuOpen,
                    onDismiss = { menuOpen = false },
                    hasLockedEntries = entries.any { it.locked },
                    sessionUnlocked = lockState.sessionUnlocked,
                    onSelect = controller::onEnterSelectionMode,
                    onLockAll = controller::onLockAllEntries,
                    onUnlockAll = { request(EntryLockAction.UnlockAll) },
                    onSessionUnlock = { request(EntryLockAction.SessionUnlock) },
                )
            }
        }
        entries.forEachIndexed { index, entry ->
            val reveal = entryRevealState(
                isLocked = entry.locked,
                sessionUnlocked = lockState.sessionUnlocked,
                temporarilyUnlocked = entry.id in lockState.temporarilyUnlockedIds,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            if (lockState.selectionMode) {
                                controller.onToggleSelected(entry.id)
                            } else if (reveal.isLockedNow) {
                                request(EntryLockAction.Reveal(entry.id))
                            }
                        },
                        onLongClick = { controller.onLongPressEntry(entry.id) },
                    )
                    .testTag(EntryLockTags.entry(entry.id)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                if (lockState.selectionMode) {
                    AppCheckbox(
                        checked = entry.id in lockState.selectedIds,
                        onCheckedChange = { controller.onToggleSelected(entry.id) },
                        modifier = Modifier.testTag(EntryLockTags.checkbox(entry.id)),
                    )
                }
                if (reveal.isRevealed) {
                    Text(
                        text = stringResource(entry.titleRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    LockedEntryRow(
                        dateText = baseDate.minusDays(index.toLong()).format(dateFormatter),
                        timeText = LocalTime.of(MORNING_HOUR + index, 0).format(TimeFormats.HOUR_MINUTE),
                        modifier = Modifier.weight(1f),
                    )
                }
                EntryLockStatusIcon(
                    isLocked = entry.locked,
                    isTempRevealed = reveal.isTempRevealed,
                    selectionMode = lockState.selectionMode,
                    onRelock = { controller.onRelockEntry(entry.id) },
                )
            }
        }
        AppHorizontalDivider()
        DemoSwitchRow(
            label = stringResource(R.string.libex_entrylock_simulated),
            checked = simulated,
            onCheckedChange = { simulated = it },
            tag = EntryLockTags.SIMULATED,
        )
        AppButton(
            text = stringResource(R.string.libex_entrylock_device_test),
            role = AppButtonRole.Warning,
            variant = AppButtonVariant.Tonal,
            onClick = {
                pending = null
                launchDeviceAuth(authTitle)
            },
            modifier = Modifier.testTag(EntryLockTags.DEVICE_TEST),
        )
        lastResult?.let {
            DemoMetaText(
                text = stringResource(R.string.libex_entrylock_last_result, stringResource(EntryLockDemoLogic.authResultLabel(it))),
                modifier = Modifier.testTag(EntryLockTags.AUTH_RESULT),
            )
        }
    }
}

private const val MORNING_HOUR = 8
