package com.wafflehq.lib.uicore.serialization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EnumCodecTest {

    private enum class Sample { ALPHA, BETA }

    @Test
    fun enumFromNameOrDefault_knownName_returnsMatch() {
        assertEquals(Sample.BETA, enumFromNameOrDefault("BETA", Sample.ALPHA))
    }

    @Test
    fun enumFromNameOrDefault_unknownName_returnsDefault() {
        assertEquals(Sample.ALPHA, enumFromNameOrDefault("GAMMA", Sample.ALPHA))
    }

    @Test
    fun enumFromNameOrDefault_nullName_returnsDefault() {
        assertEquals(Sample.ALPHA, enumFromNameOrDefault(null, Sample.ALPHA))
    }

    @Test
    fun enumFromNameOrDefault_caseSensitive_treatsMismatchAsUnknown() {
        assertEquals(Sample.ALPHA, enumFromNameOrDefault("beta", Sample.ALPHA))
    }

    @Test
    fun enumFromNameOrNull_knownName_returnsMatch() {
        assertEquals(Sample.BETA, enumFromNameOrNull<Sample>("BETA"))
    }

    @Test
    fun enumFromNameOrNull_unknownName_returnsNull() {
        assertNull(enumFromNameOrNull<Sample>("GAMMA"))
    }

    @Test
    fun enumFromNameOrNull_nullName_returnsNull() {
        assertNull(enumFromNameOrNull<Sample>(null))
    }
}
