package com.wafflehq.lib.maintenance

interface MaintenanceTask {
    val id: String
    val order: Int get() = DEFAULT_ORDER

    suspend fun isEnabled(): Boolean

    suspend fun run()

    companion object {
        const val DEFAULT_ORDER = 100
    }
}
