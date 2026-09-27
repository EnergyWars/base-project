package com.wafflehq.lib.maintenance

import java.util.concurrent.CancellationException

class MaintenanceWorkExecutor(
    private val tasks: Set<MaintenanceTask>,
    private val onTaskFailure: (taskId: String, error: Throwable) -> Unit
) {
    suspend fun execute() {
        MaintenanceTaskRunner(tasks).runAll().forEach { outcome ->
            val error = outcome.error
            if (error is CancellationException) throw error
            if (error != null) onTaskFailure(outcome.taskId, error)
        }
    }
}
