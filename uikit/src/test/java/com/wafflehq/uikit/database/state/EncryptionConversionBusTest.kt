package com.wafflehq.uikit.database.state

import com.wafflehq.uikit.database.conversion.ConversionFailureReason
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EncryptionConversionBusTest {

    @Before
    fun setUp() {
        EncryptionConversionBus.update(ConversionUiState.Idle)
    }

    @After
    fun tearDown() {
        EncryptionConversionBus.update(ConversionUiState.Idle)
    }

    @Test
    fun `initial state is idle`() {
        assertEquals(ConversionUiState.Idle, EncryptionConversionBus.state.value)
    }

    @Test
    fun `update sets in progress state`() {
        EncryptionConversionBus.update(ConversionUiState.InProgress)

        assertEquals(ConversionUiState.InProgress, EncryptionConversionBus.state.value)
    }

    @Test
    fun `update sets success awaiting restart state`() {
        EncryptionConversionBus.update(ConversionUiState.SuccessAwaitingRestart)

        assertEquals(ConversionUiState.SuccessAwaitingRestart, EncryptionConversionBus.state.value)
    }

    @Test
    fun `update sets error state with reason`() {
        EncryptionConversionBus.update(ConversionUiState.Error(ConversionFailureReason.IO_ERROR))

        assertEquals(ConversionUiState.Error(ConversionFailureReason.IO_ERROR), EncryptionConversionBus.state.value)
    }

    @Test
    fun `update back to idle resets state`() {
        EncryptionConversionBus.update(ConversionUiState.InProgress)

        EncryptionConversionBus.update(ConversionUiState.Idle)

        assertEquals(ConversionUiState.Idle, EncryptionConversionBus.state.value)
    }
}
