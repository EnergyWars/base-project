package com.wafflehq.lib.database.state

object DatabaseKeyRecoveryState {
    @Volatile
    var keyUnavailable: Boolean = false
}
