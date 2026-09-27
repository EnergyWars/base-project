package com.wafflehq.lib.drafts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

const val DRAFT_AUTOSAVE_INTERVAL_MS = 5_000L

suspend fun runDraftAutosaveLoop(intervalMs: Long = DRAFT_AUTOSAVE_INTERVAL_MS, onTick: suspend () -> Unit) {
    while (coroutineContext.isActive) {
        delay(intervalMs)
        onTick()
    }
}

@Composable
fun DraftAutosaveEffect(
    enabled: Boolean,
    intervalMs: Long = DRAFT_AUTOSAVE_INTERVAL_MS,
    onTick: suspend () -> Unit
) {
    LaunchedEffect(enabled) {
        if (enabled) runDraftAutosaveLoop(intervalMs, onTick)
    }
}
