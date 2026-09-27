package com.wafflehq.lib.maintenance

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MaintenanceWorkScheduler(
    private val workManagerProvider: () -> WorkManager,
    private val tasks: Set<MaintenanceTask>,
    private val workerClass: Class<out ListenableWorker>,
    private val config: MaintenanceScheduleConfig
) {
    private val workManager get() = workManagerProvider()

    suspend fun refresh() {
        config.legacyUniqueWorkNames.forEach { workManager.cancelUniqueWork(it) }
        config.legacyTags.forEach { workManager.cancelAllWorkByTag(it) }
        if (!MaintenanceTaskRunner(tasks).anyEnabled()) {
            cancel()
            return
        }
        workManager.enqueueUniquePeriodicWork(
            config.uniqueWorkName,
            ExistingPeriodicWorkPolicy.KEEP,
            buildRequest()
        )
    }

    fun cancel() {
        workManager.cancelUniqueWork(config.uniqueWorkName)
    }

    internal fun buildRequest(): PeriodicWorkRequest = PeriodicWorkRequest.Builder(
        workerClass,
        config.intervalMinutes, TimeUnit.MINUTES,
        config.flexMinutes, TimeUnit.MINUTES
    ).addTag(config.tag).build()
}
