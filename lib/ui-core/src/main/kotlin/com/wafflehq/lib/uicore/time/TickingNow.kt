package com.wafflehq.lib.uicore.time

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import java.time.Clock
import java.time.Instant

@Composable
fun rememberTickingNow(
    intervalMillis: Long,
    clock: Clock = Clock.systemDefaultZone(),
    enabled: Boolean = true
): State<Instant> = produceState(initialValue = clock.instant(), intervalMillis, clock, enabled) {
    if (!enabled) return@produceState
    value = clock.instant()
    while (true) {
        delay(intervalMillis)
        value = clock.instant()
    }
}
