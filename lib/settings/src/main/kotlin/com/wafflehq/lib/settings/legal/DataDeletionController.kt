package com.wafflehq.lib.settings.legal

import com.wafflehq.lib.settings.legal.access.DataResetter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DataDeletionState { IDLE, DELETING, DONE, ERROR }

class DataDeletionController(
    private val resetter: DataResetter,
    private val scope: CoroutineScope
) {

    private val _state = MutableStateFlow(DataDeletionState.IDLE)
    val state: StateFlow<DataDeletionState> = _state.asStateFlow()

    fun deleteAllData() {
        if (_state.value == DataDeletionState.DELETING) return
        scope.launch {
            _state.value = DataDeletionState.DELETING
            runCatching { resetter.deleteAllData() }
                .onSuccess { _state.value = DataDeletionState.DONE }
                .onFailure { _state.value = DataDeletionState.ERROR }
        }
    }
}
