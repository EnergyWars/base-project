package com.wafflehq.lib.uicore.csv

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvFormulaGuardTest {

    @Test
    fun neutralize_prefixesEveryTriggerCharacter() {
        listOf("=A1", "+A1", "-A1", "@A1", "\tA1", "\rA1").forEach { value ->
            assertEquals("'$value", CsvFormulaGuard.neutralize(value))
        }
    }

    @Test
    fun neutralize_keepsOrdinaryText() {
        assertEquals("hello", CsvFormulaGuard.neutralize("hello"))
        assertEquals("", CsvFormulaGuard.neutralize(""))
        assertEquals("a=b", CsvFormulaGuard.neutralize("a=b"))
    }

    @Test
    fun neutralize_keepsPlainNumbers() {
        listOf("-5", "+5", "-5.25", "-5,25", "12").forEach { value ->
            assertEquals(value, CsvFormulaGuard.neutralize(value))
        }
    }

    @Test
    fun neutralize_prefixesNumberLikeFormulas() {
        assertEquals("'-1+1", CsvFormulaGuard.neutralize("-1+1"))
        assertEquals("'-", CsvFormulaGuard.neutralize("-"))
    }

    @Test
    fun restore_isTheInverseOfNeutralize() {
        listOf("=A1", "+cmd", "-abc", "@SUM(1)", "\tx", "-1+1").forEach { value ->
            assertEquals(value, CsvFormulaGuard.restore(CsvFormulaGuard.neutralize(value)))
        }
    }

    @Test
    fun restore_leavesOrdinaryApostrophesAlone() {
        assertEquals("'hello", CsvFormulaGuard.restore("'hello"))
        assertEquals("'", CsvFormulaGuard.restore("'"))
        assertEquals("plain", CsvFormulaGuard.restore("plain"))
        assertEquals("'5", CsvFormulaGuard.restore("'5"))
    }
}
