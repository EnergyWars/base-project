package com.wafflehq.lib.settings.colors

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

class ColorSafetyGuard(
    private val store: ColorOverrideStore,
    private val clock: () -> Long = System::currentTimeMillis
) {
    suspend fun run() {
        store.pendingConfirmationFlow().collectLatest { pending ->
            if (pending == null) return@collectLatest
            while (true) {
                val remaining = pending.deadlineMillis - clock()
                if (remaining <= 0L) {
                    store.revertIfExpired()
                    break
                }
                delay(remaining)
            }
        }
    }
}
