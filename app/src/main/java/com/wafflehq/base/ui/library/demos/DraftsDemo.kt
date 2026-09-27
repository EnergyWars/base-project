package com.wafflehq.base.ui.library.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.lib.drafts.DRAFT_AUTOSAVE_INTERVAL_MS
import com.wafflehq.lib.drafts.DraftAutosaveEffect
import com.wafflehq.lib.drafts.DraftRepository
import com.wafflehq.lib.drafts.StoredDraft
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.time.shortTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

internal class InMemoryDraftRepository(
    private val clock: () -> Long = System::currentTimeMillis,
) : DraftRepository<String> {

    private val state = MutableStateFlow<StoredDraft<String>?>(null)

    val current: StoredDraft<String>? get() = state.value

    override fun observe(): Flow<StoredDraft<String>?> = state

    override suspend fun save(entryId: Long?, payload: String) {
        state.value = StoredDraft(entryId, payload, clock())
    }

    override suspend fun clear() {
        state.value = null
    }
}

internal object DraftsDemoLogic {

    const val DEMO_INTERVAL_MS = 2_000L

    fun shouldSave(text: String, stored: StoredDraft<String>?): Boolean =
        text.isNotBlank() && text != stored?.payload
}

private const val MILLIS_PER_SECOND = 1_000L

internal object DraftsTags {
    const val INPUT = "libex_drafts_input"
    const val SWITCH = "libex_drafts_switch"
    const val SAVE = "libex_drafts_save"
    const val RESTORE = "libex_drafts_restore"
    const val DISCARD = "libex_drafts_discard"
    const val STATUS = "libex_drafts_status"
}

@Composable
internal fun DraftsDemo(
    intervalMs: Long = DraftsDemoLogic.DEMO_INTERVAL_MS,
    repository: InMemoryDraftRepository = remember { InMemoryDraftRepository() },
) {
    var text by remember { mutableStateOf("") }
    var autosave by remember { mutableStateOf(true) }
    val stored by repository.observe().collectAsState(initial = null)
    val latestText by rememberUpdatedState(text)
    val scope = rememberCoroutineScope()
    val timeFormatter = remember { shortTimeFormatter() }

    DraftAutosaveEffect(enabled = autosave, intervalMs = intervalMs) {
        if (DraftsDemoLogic.shouldSave(latestText, repository.current)) {
            repository.save(entryId = null, payload = latestText)
        }
    }

    DemoSection(
        id = "drafts",
        titleRes = R.string.libex_drafts_title,
        descriptionRes = R.string.libex_drafts_desc,
        moduleRes = R.string.libex_module_drafts,
    ) {
        AppTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.libex_drafts_field_label)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth().testTag(DraftsTags.INPUT),
        )
        DemoSwitchRow(
            label = stringResource(R.string.libex_drafts_autosave),
            checked = autosave,
            onCheckedChange = { autosave = it },
            tag = DraftsTags.SWITCH,
        )
        DemoMetaText(
            text = stringResource(
                R.string.libex_drafts_interval,
                intervalMs / MILLIS_PER_SECOND,
                DRAFT_AUTOSAVE_INTERVAL_MS / MILLIS_PER_SECOND,
            )
        )
        val draft = stored
        DemoMetaText(
            text = if (draft == null) {
                stringResource(R.string.libex_drafts_none)
            } else {
                stringResource(
                    R.string.libex_drafts_saved,
                    Instant.ofEpochMilli(draft.updatedAtEpochMs).atZone(ZoneId.systemDefault()).format(timeFormatter),
                    draft.payload.length,
                )
            },
            modifier = Modifier.testTag(DraftsTags.STATUS),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            AppButton(
                text = stringResource(R.string.libex_drafts_save),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Tonal,
                onClick = { scope.launch { repository.save(entryId = null, payload = text) } },
                modifier = Modifier.testTag(DraftsTags.SAVE),
            )
            AppButton(
                text = stringResource(R.string.libex_drafts_restore),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Outlined,
                enabled = draft != null,
                onClick = { draft?.let { text = it.payload } },
                modifier = Modifier.testTag(DraftsTags.RESTORE),
            )
            AppButton(
                text = stringResource(R.string.libex_drafts_discard),
                role = AppButtonRole.Error,
                variant = AppButtonVariant.Text,
                enabled = draft != null,
                onClick = { scope.launch { repository.clear() } },
                modifier = Modifier.testTag(DraftsTags.DISCARD),
            )
        }
    }
}
