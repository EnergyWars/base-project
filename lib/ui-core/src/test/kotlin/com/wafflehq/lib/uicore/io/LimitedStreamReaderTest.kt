package com.wafflehq.lib.uicore.io

import java.io.ByteArrayInputStream
import java.io.IOException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LimitedStreamReaderTest {

    @Test
    fun readBytesLimited_underLimit_returnsAllBytes() {
        val data = ByteArray(100) { it.toByte() }

        val result = ByteArrayInputStream(data).readBytesLimited(1_000)

        assertArrayEquals(data, result)
    }

    @Test
    fun readBytesLimited_exactlyAtLimit_returnsAllBytes() {
        val data = ByteArray(100) { it.toByte() }

        val result = ByteArrayInputStream(data).readBytesLimited(100)

        assertArrayEquals(data, result)
    }

    @Test
    fun readBytesLimited_overLimit_throwsDefaultException() {
        val data = ByteArray(101)

        assertThrows(IOException::class.java) {
            ByteArrayInputStream(data).readBytesLimited(100)
        }
    }

    @Test
    fun readBytesLimited_overLimit_throwsCustomException() {
        class CustomException : Exception()
        val data = ByteArray(101)

        assertThrows(CustomException::class.java) {
            ByteArrayInputStream(data).readBytesLimited(100) { CustomException() }
        }
    }

    @Test
    fun readBytesLimited_emptyStream_returnsEmptyArray() {
        val result = ByteArrayInputStream(ByteArray(0)).readBytesLimited(100)

        assertArrayEquals(ByteArray(0), result)
    }

    @Test
    fun readBytesLimited_largerThanBufferSize_readsAcrossMultipleChunks() {
        val data = ByteArray(DEFAULT_BUFFER_SIZE * 3 + 17) { (it % 256).toByte() }

        val result = ByteArrayInputStream(data).readBytesLimited(data.size.toLong())

        assertArrayEquals(data, result)
    }
}
