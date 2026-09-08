package com.wafflehq.uikit.maintenance

class MaintenanceTaskRunner(private val tasks: Set<MaintenanceTask>) {

    data class Outcome(val taskId: String, val ran: Boolean, val error: Throwable? = null)

    suspend fun anyEnabled(): Boolean = tasks.any { runCatching { it.isEnabled() }.getOrDefault(false) }

    suspend fun runAll(): List<Outcome> = orderedTasks().map { task ->
        val enabled = runCatching { task.isEnabled() }.getOrDefault(false)
        if (!enabled) return@map Outcome(task.id, ran = false)
        val error = runCatching { task.run() }.exceptionOrNull()
        Outcome(task.id, ran = true, error = error)
    }

    private fun orderedTasks(): List<MaintenanceTask> = tasks.sortedWith(compareBy({ it.order }, { it.id }))
}
