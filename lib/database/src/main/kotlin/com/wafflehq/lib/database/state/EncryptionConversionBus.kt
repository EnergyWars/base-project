package com.wafflehq.lib.database.state

import com.wafflehq.lib.database.conversion.ConversionFailureReason
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ConversionUiState {
    data object Idle : ConversionUiState
    data object InProgress : ConversionUiState
    data object SuccessAwaitingRestart : ConversionUiState
    data class Error(val reason: ConversionFailureReason) : ConversionUiState
}

object EncryptionConversionBus {
    private val _state = MutableStateFlow<ConversionUiState>(ConversionUiState.Idle)
    val state: StateFlow<ConversionUiState> = _state.asStateFlow()

    fun update(newState: ConversionUiState) {
        _state.value = newState
    }
}
