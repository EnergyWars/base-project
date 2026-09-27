package com.wafflehq.lib.maintenance

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MaintenanceWorkSchedulerTest {

    private class FakeTask(
        override val id: String,
        private val enabled: Boolean,
        private val enabledThrows: Boolean = false
    ) : MaintenanceTask {
        override suspend fun isEnabled(): Boolean {
            if (enabledThrows) error("isEnabled failed")
            return enabled
        }

        override suspend fun run() = Unit
    }

    private val workManager = mock<WorkManager>()

    private val config = MaintenanceScheduleConfig(
        uniqueWorkName = "unique_name",
        tag = "the_tag",
        legacyUniqueWorkNames = listOf("legacy_one", "legacy_two"),
        legacyTags = listOf("legacy_tag")
    )

    private fun scheduler(
        vararg tasks: MaintenanceTask,
        config: MaintenanceScheduleConfig = this.config,
        provider: () -> WorkManager = { workManager }
    ) = MaintenanceWorkScheduler(provider, tasks.toSet(), ListenableWorker::class.java, config)

    @Test
    fun refreshEnqueuesUniquePeriodicWorkWithKeepPolicyWhenATaskIsEnabled() = runTest {
        scheduler(FakeTask("a", enabled = true)).refresh()

        val requestCaptor = argumentCaptor<PeriodicWorkRequest>()
        verify(workManager).enqueueUniquePeriodicWork(
            eq("unique_name"),
            eq(ExistingPeriodicWorkPolicy.KEEP),
            requestCaptor.capture()
        )
        assertTrue(requestCaptor.firstValue.tags.contains("the_tag"))
    }

    @Test
    fun refreshCancelsLegacyWorkBeforeEnqueueing() = runTest {
        scheduler(FakeTask("a", enabled = true)).refresh()

        verify(workManager).cancelUniqueWork("legacy_one")
        verify(workManager).cancelUniqueWork("legacy_two")
        verify(workManager).cancelAllWorkByTag("legacy_tag")
    }

    @Test
    fun refreshCancelsPeriodicWorkWhenNoTaskIsEnabled() = runTest {
        scheduler(FakeTask("a", enabled = false), FakeTask("b", enabled = false)).refresh()

        verify(workManager).cancelUniqueWork("legacy_one")
        verify(workManager).cancelUniqueWork("legacy_two")
        verify(workManager).cancelAllWorkByTag("legacy_tag")
        verify(workManager).cancelUniqueWork("unique_name")
        verifyNoMoreInteractions(workManager)
    }

    @Test
    fun refreshCancelsPeriodicWorkWhenThereAreNoTasks() = runTest {
        scheduler().refresh()

        verify(workManager).cancelUniqueWork("unique_name")
    }

    @Test
    fun refreshTreatsFailingIsEnabledAsDisabled() = runTest {
        scheduler(FakeTask("a", enabled = true, enabledThrows = true)).refresh()

        verify(workManager).cancelUniqueWork("unique_name")
        verify(workManager).cancelUniqueWork("legacy_one")
    }

    @Test
    fun refreshEnqueuesWhenOnlyOneOfSeveralTasksIsEnabled() = runTest {
        scheduler(
            FakeTask("a", enabled = false),
            FakeTask("b", enabled = true),
            FakeTask("c", enabled = true, enabledThrows = true)
        ).refresh()

        verify(workManager).enqueueUniquePeriodicWork(
            eq("unique_name"),
            eq(ExistingPeriodicWorkPolicy.KEEP),
            argumentCaptor<PeriodicWorkRequest>().capture()
        )
    }

    @Test
    fun refreshWithoutLegacyEntriesOnlyTouchesTheUniqueWork() = runTest {
        val plainConfig = MaintenanceScheduleConfig(uniqueWorkName = "unique_name", tag = "the_tag")

        scheduler(FakeTask("a", enabled = false), config = plainConfig).refresh()

        verify(workManager).cancelUniqueWork("unique_name")
        verifyNoMoreInteractions(workManager)
    }

    @Test
    fun cancelCancelsOnlyTheUniqueWork() {
        scheduler(FakeTask("a", enabled = true)).cancel()

        verify(workManager).cancelUniqueWork("unique_name")
        verifyNoMoreInteractions(workManager)
    }

    @Test
    fun workManagerIsResolvedOnEveryCallInsteadOfCached() = runTest {
        var resolved = 0
        val scheduler = scheduler(FakeTask("a", enabled = false)) {
            resolved++
            workManager
        }

        scheduler.cancel()
        scheduler.cancel()

        assertEquals(2, resolved)
    }

    @Test
    fun buildRequestCarriesTheConfiguredTag() {
        val request = scheduler(FakeTask("a", enabled = true)).buildRequest()

        assertTrue(request.tags.contains("the_tag"))
        assertTrue(request.tags.contains(ListenableWorker::class.java.name))
    }
}
