package com.wafflehq.uikit.maintenance

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MaintenanceTaskRunnerTest {

    private class FakeTask(
        override val id: String,
        override val order: Int = MaintenanceTask.DEFAULT_ORDER,
        private val enabled: Boolean = true,
        private val failWith: Throwable? = null,
        private val enabledThrows: Boolean = false,
        private val log: MutableList<String>
    ) : MaintenanceTask {
        var runCount = 0
            private set

        override suspend fun isEnabled(): Boolean {
            if (enabledThrows) error("isEnabled failed")
            return enabled
        }

        override suspend fun run() {
            runCount++
            log += id
            failWith?.let { throw it }
        }
    }

    @Test
    fun runAll_runsOnlyEnabledTasks() = runTest {
        val log = mutableListOf<String>()
        val enabled = FakeTask("a", enabled = true, log = log)
        val disabled = FakeTask("b", enabled = false, log = log)

        val outcomes = MaintenanceTaskRunner(setOf(enabled, disabled)).runAll()

        assertEquals(1, enabled.runCount)
        assertEquals(0, disabled.runCount)
        assertEquals(listOf("a"), log)
        assertTrue(outcomes.first { it.taskId == "a" }.ran)
        assertFalse(outcomes.first { it.taskId == "b" }.ran)
    }

    @Test
    fun runAll_respectsOrderThenId() = runTest {
        val log = mutableListOf<String>()
        val tasks = setOf(
            FakeTask("z", order = 10, log = log),
            FakeTask("m", order = 90, log = log),
            FakeTask("a", order = 10, log = log),
            FakeTask("k", order = 20, log = log),
        )

        MaintenanceTaskRunner(tasks).runAll()

        assertEquals(listOf("a", "z", "k", "m"), log)
    }

    @Test
    fun runAll_continuesAfterFailingTaskAndReportsError() = runTest {
        val log = mutableListOf<String>()
        val failing = FakeTask("fail", order = 1, failWith = IllegalStateException("boom"), log = log)
        val next = FakeTask("next", order = 2, log = log)

        val outcomes = MaintenanceTaskRunner(setOf(failing, next)).runAll()

        assertEquals(listOf("fail", "next"), log)
        assertNotNull(outcomes.first { it.taskId == "fail" }.error)
        assertNull(outcomes.first { it.taskId == "next" }.error)
    }

    @Test
    fun runAll_treatsThrowingIsEnabledAsDisabled() = runTest {
        val log = mutableListOf<String>()
        val broken = FakeTask("broken", enabledThrows = true, log = log)
        val healthy = FakeTask("healthy", log = log)

        val outcomes = MaintenanceTaskRunner(setOf(broken, healthy)).runAll()

        assertEquals(0, broken.runCount)
        assertEquals(1, healthy.runCount)
        assertFalse(outcomes.first { it.taskId == "broken" }.ran)
    }

    @Test
    fun anyEnabled_trueWhenAtLeastOneTaskEnabled() = runTest {
        val log = mutableListOf<String>()
        val runner = MaintenanceTaskRunner(
            setOf(FakeTask("off", enabled = false, log = log), FakeTask("on", enabled = true, log = log))
        )

        assertTrue(runner.anyEnabled())
    }

    @Test
    fun anyEnabled_falseWhenAllDisabledOrEmpty() = runTest {
        val log = mutableListOf<String>()

        assertFalse(MaintenanceTaskRunner(setOf(FakeTask("off", enabled = false, log = log))).anyEnabled())
        assertFalse(MaintenanceTaskRunner(emptySet()).anyEnabled())
    }

    @Test
    fun anyEnabled_ignoresTasksWhoseIsEnabledThrows() = runTest {
        val log = mutableListOf<String>()

        assertFalse(MaintenanceTaskRunner(setOf(FakeTask("broken", enabledThrows = true, log = log))).anyEnabled())
    }
}
