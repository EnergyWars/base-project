package com.wafflehq.lib.maintenance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MaintenanceScheduleConfigTest {

    @Test
    fun defaultsMatchWorkManagerMinimumPeriodicWindow() {
        val config = MaintenanceScheduleConfig(uniqueWorkName = "unique", tag = "tag")

        assertEquals(15L, config.intervalMinutes)
        assertEquals(5L, config.flexMinutes)
        assertEquals(MaintenanceScheduleConfig.DEFAULT_INTERVAL_MINUTES, config.intervalMinutes)
        assertEquals(MaintenanceScheduleConfig.DEFAULT_FLEX_MINUTES, config.flexMinutes)
    }

    @Test
    fun legacyListsDefaultToEmpty() {
        val config = MaintenanceScheduleConfig(uniqueWorkName = "unique", tag = "tag")

        assertTrue(config.legacyUniqueWorkNames.isEmpty())
        assertTrue(config.legacyTags.isEmpty())
    }

    @Test
    fun customValuesArePreserved() {
        val config = MaintenanceScheduleConfig(
            uniqueWorkName = "unique",
            tag = "tag",
            intervalMinutes = 30L,
            flexMinutes = 10L,
            legacyUniqueWorkNames = listOf("a"),
            legacyTags = listOf("b")
        )

        assertEquals(30L, config.intervalMinutes)
        assertEquals(10L, config.flexMinutes)
        assertEquals(listOf("a"), config.legacyUniqueWorkNames)
        assertEquals(listOf("b"), config.legacyTags)
    }
}
