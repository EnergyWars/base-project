package com.wafflehq.lib.uicore.io

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportFileNamesTest {

    private val isoTimestamp = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z"

    @Test
    fun jsonExportFileName_hasPrefixIsoTimestampAndJsonExtension() {
        val name = jsonExportFileName("allinonecalendar_recipes")

        assertTrue(name, Regex("allinonecalendar_recipes_$isoTimestamp\\.json").matches(name))
    }

    @Test
    fun jsonExportFileName_differentPrefixesProduceDifferentNames() {
        assertNotEquals(
            jsonExportFileName("allinonecalendar_recipes"),
            jsonExportFileName("allinonecalendar_hobbys")
        )
    }

    @Test
    fun csvExportFileName_hasPrefixIsoTimestampAndCsvExtension() {
        val name = csvExportFileName("allinonecalendar_weight")

        assertTrue(name, Regex("allinonecalendar_weight_$isoTimestamp\\.csv").matches(name))
    }

    @Test
    fun csvExportFileName_differentPrefixesProduceDifferentNames() {
        assertNotEquals(
            csvExportFileName("allinonecalendar_weight"),
            csvExportFileName("allinonecalendar_steps")
        )
    }

    @Test
    fun exportFileNames_keepPrefixVerbatim() {
        assertTrue(jsonExportFileName("a b").startsWith("a b_"))
        assertTrue(csvExportFileName("").startsWith("_"))
    }
}
