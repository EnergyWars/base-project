package com.wafflehq.lib.maintenance

data class MaintenanceScheduleConfig(
    val uniqueWorkName: String,
    val tag: String,
    val intervalMinutes: Long = DEFAULT_INTERVAL_MINUTES,
    val flexMinutes: Long = DEFAULT_FLEX_MINUTES,
    val legacyUniqueWorkNames: List<String> = emptyList(),
    val legacyTags: List<String> = emptyList()
) {
    companion object {
        const val DEFAULT_INTERVAL_MINUTES = 15L
        const val DEFAULT_FLEX_MINUTES = 5L
    }
}
