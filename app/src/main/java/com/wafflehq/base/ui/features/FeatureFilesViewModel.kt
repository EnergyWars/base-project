package com.wafflehq.base.ui.features

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wafflehq.base.data.features.FeatureFilesRepository
import com.wafflehq.base.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeatureFileItem(
    val fileName: String,
    val title: String,
    val checked: Boolean,
)

@HiltViewModel
class FeatureFilesViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    featureFilesRepository: FeatureFilesRepository,
) : ViewModel() {

    private val files = featureFilesRepository.list()
    val totalFileCount: Int = files.size

    val showHidden: StateFlow<Boolean> = settingsRepository.showHiddenFeatureFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val items: StateFlow<List<FeatureFileItem>> = combine(
        settingsRepository.checkedFeatureFiles,
        settingsRepository.showHiddenFeatureFiles,
    ) { checked, showHidden ->
        files
            .map { file -> FeatureFileItem(file.fileName, file.title, file.fileName in checked) }
            .filter { showHidden || !it.checked }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onCheckedChange(fileName: String, checked: Boolean) {
        viewModelScope.launch { settingsRepository.setFeatureFileChecked(fileName, checked) }
    }

    fun onShowHiddenChange(show: Boolean) {
        viewModelScope.launch { settingsRepository.setShowHiddenFeatureFiles(show) }
    }
}

@HiltViewModel
class FeatureFileDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    featureFilesRepository: FeatureFilesRepository,
) : ViewModel() {

    private val fileName: String = checkNotNull(savedStateHandle["fileName"])

    val featureFile = featureFilesRepository.read(fileName)
}
