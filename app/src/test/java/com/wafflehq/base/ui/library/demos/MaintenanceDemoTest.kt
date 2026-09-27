package com.wafflehq.base.ui.library.demos

import android.app.Application
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.maintenance.MaintenanceScheduleConfig
import com.wafflehq.lib.maintenance.MaintenanceTaskRunner
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.ZoneOffset
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MaintenanceDemoTest : LibraryDemoTest() {

    private val now = ZonedDateTime.of(2026, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC)

    private companion object {
        const val FAILURE_MESSAGE = "failure"
    }

    @Test
    fun announcesThatTheSchedulerWouldEnqueueWork() {
        show { MaintenanceDemo(now = now) }

        waitForTagText(MaintenanceTags.ANY_ENABLED, string(R.string.libex_maintenance_scheduler_active))
    }

    @Test
    fun runAllReportsEveryTaskOutcome() {
        show { MaintenanceDemo(now = now) }

        click(MaintenanceTags.RUN_ALL)
        waitForTag(MaintenanceTags.outcome("cleanup"))

        assertTagText(MaintenanceTags.outcome("cleanup"), string(R.string.libex_maintenance_outcome_ok))
        assertTagText(MaintenanceTags.outcome("sync"), string(R.string.libex_maintenance_outcome_ok))
        assertTagText(
            MaintenanceTags.outcome(MaintenanceDemoLogic.FAILING_TASK_ID),
            string(R.string.libex_maintenance_outcome_failed, string(R.string.libex_maintenance_failure_message)),
        )
        assertTagText(
            MaintenanceTags.outcome(MaintenanceDemoLogic.DORMANT_TASK_ID),
            string(R.string.libex_maintenance_outcome_skipped),
        )
    }

    @Test
    fun executorReportsTheFailingTask() {
        show { MaintenanceDemo(now = now) }

        click(MaintenanceTags.RUN_EXECUTOR)
        waitForTag(MaintenanceTags.EXECUTOR_RESULT)

        assertTagText(
            MaintenanceTags.EXECUTOR_RESULT,
            string(R.string.libex_maintenance_executor_failures, 1, MaintenanceDemoLogic.FAILING_TASK_ID),
        )
    }

    @Test
    fun nextFireDelayFollowsTheConfiguredTime() {
        show { MaintenanceDemo(now = now) }

        assertTagText(MaintenanceTags.DELAY, string(R.string.libex_maintenance_delay_value, 17L, 30L))

        stepUp(MaintenanceTags.FIRE_HOUR)

        assertTagText(MaintenanceTags.DELAY, string(R.string.libex_maintenance_delay_value, 18L, 30L))
    }

    @Test
    fun intervalStepperUpdatesTheScheduleConfiguration() {
        show { MaintenanceDemo(now = now) }
        assertTagText(
            MaintenanceTags.CONFIG,
            string(R.string.libex_maintenance_config, MaintenanceDemoLogic.WORK_NAME, 15L, 5L),
        )

        stepUp(MaintenanceTags.INTERVAL)
        stepUp(MaintenanceTags.FLEX)

        assertTagText(
            MaintenanceTags.CONFIG,
            string(R.string.libex_maintenance_config, MaintenanceDemoLogic.WORK_NAME, 30L, 10L),
        )
    }

    @Test
    fun runnerOrdersTasksAndSkipsDisabledOnes() = runTest {
        val runner = MaintenanceTaskRunner(MaintenanceDemoLogic.tasks(FAILURE_MESSAGE))

        val outcomes = runner.runAll()

        assertEquals(listOf("cleanup", "sync", "broken", "dormant"), outcomes.map { it.taskId })
        assertTrue(outcomes[0].ran && outcomes[0].error == null)
        assertTrue(outcomes[2].ran && outcomes[2].error != null)
        assertFalse(outcomes[3].ran)
        assertTrue(runner.anyEnabled())
    }

    @Test
    fun scheduleConfigCarriesTheGivenValues() {
        val config = MaintenanceDemoLogic.scheduleConfig(intervalMinutes = 45, flexMinutes = 10)

        assertEquals(MaintenanceDemoLogic.WORK_NAME, config.uniqueWorkName)
        assertEquals(45L, config.intervalMinutes)
        assertEquals(10L, config.flexMinutes)
        assertTrue(config.legacyTags.isEmpty())
        assertEquals(15L, MaintenanceScheduleConfig.DEFAULT_INTERVAL_MINUTES)
    }

    @Test
    fun hoursAndMinutesSplitTheDelay() {
        assertEquals(17L to 30L, MaintenanceDemoLogic.hoursAndMinutes(63_000_000L))
        assertEquals(0L to 0L, MaintenanceDemoLogic.hoursAndMinutes(-5L))
        assertEquals(0L to 59L, MaintenanceDemoLogic.hoursAndMinutes(59 * 60_000L + 30_000L))
    }

    @Test
    fun sectionIsRendered() {
        show { MaintenanceDemo(now = now) }

        node(DemoTags.section("maintenance")).assertExists()
        assertNotNull(MaintenanceDemoLogic.tasks(FAILURE_MESSAGE).firstOrNull { it.id == "sync" })
        assertNull(MaintenanceDemoLogic.tasks(FAILURE_MESSAGE).firstOrNull { it.id == "missing" })
    }
}
