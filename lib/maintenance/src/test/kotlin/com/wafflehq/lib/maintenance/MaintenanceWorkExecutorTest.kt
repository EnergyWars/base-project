package com.wafflehq.lib.maintenance

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.concurrent.CancellationException

class MaintenanceWorkExecutorTest {

    private class FakeTask(
        override val id: String,
        override val order: Int = MaintenanceTask.DEFAULT_ORDER,
        private val enabled: Boolean = true,
        private val failWith: Throwable? = null,
        private val log: MutableList<String>
    ) : MaintenanceTask {
        override suspend fun isEnabled(): Boolean = enabled

        override suspend fun run() {
            log += id
            failWith?.let { throw it }
        }
    }

    private val ran = mutableListOf<String>()
    private val failures = mutableListOf<Pair<String, Throwable>>()

    private fun executor(vararg tasks: MaintenanceTask) =
        MaintenanceWorkExecutor(tasks.toSet()) { id, error -> failures += id to error }

    @Test
    fun runsEnabledTasksInOrder() = runTest {
        executor(
            FakeTask("b", order = 20, log = ran),
            FakeTask("a", order = 10, log = ran)
        ).execute()

        assertEquals(listOf("a", "b"), ran)
        assertTrue(failures.isEmpty())
    }

    @Test
    fun skipsDisabledTasksWithoutReportingFailure() = runTest {
        executor(
            FakeTask("off", enabled = false, log = ran),
            FakeTask("on", log = ran)
        ).execute()

        assertEquals(listOf("on"), ran)
        assertTrue(failures.isEmpty())
    }

    @Test
    fun reportsFailingTaskAndStillRunsTheOthers() = runTest {
        val boom = IllegalStateException("boom")

        executor(
            FakeTask("first", order = 1, failWith = boom, log = ran),
            FakeTask("second", order = 2, log = ran)
        ).execute()

        assertEquals(listOf("first", "second"), ran)
        assertEquals(1, failures.size)
        assertEquals("first", failures.single().first)
        assertSame(boom, failures.single().second)
    }

    @Test
    fun rethrowsCancellationInsteadOfReportingIt() = runTest {
        val cancellation = CancellationException("cancelled")

        try {
            executor(FakeTask("cancelling", failWith = cancellation, log = ran)).execute()
            fail("expected CancellationException")
        } catch (e: CancellationException) {
            assertSame(cancellation, e)
        }

        assertTrue(failures.isEmpty())
    }

    @Test
    fun rethrowsCancellationAfterAllTasksRan() = runTest {
        val cancellation = CancellationException("cancelled")

        try {
            executor(
                FakeTask("cancelling", order = 1, failWith = cancellation, log = ran),
                FakeTask("later", order = 2, log = ran)
            ).execute()
            fail("expected CancellationException")
        } catch (e: CancellationException) {
            assertSame(cancellation, e)
        }

        assertEquals(listOf("cancelling", "later"), ran)
    }

    @Test
    fun withoutTasksNothingHappens() = runTest {
        executor().execute()

        assertTrue(ran.isEmpty())
        assertTrue(failures.isEmpty())
    }
}
