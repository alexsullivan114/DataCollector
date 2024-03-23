package com.alexsullivan.datacollor.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.QLPreferences
import com.alexsullivan.datacollor.drive.BackupTrackablesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupTrackablesUseCase: BackupTrackablesUseCase,
    private val prefs: QLPreferences
) : ViewModel() {
    private val _viewStateFlow =
        MutableStateFlow(SettingsViewState(backupLoading = false, useAdvancedAiEnabled = false))
    val viewStateFlow = _viewStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            _viewStateFlow.emit(_viewStateFlow.value.copy(useAdvancedAiEnabled = prefs.useGpt4))
        }
    }

    fun backupToDriveClicked() = viewModelScope.launch {
        _viewStateFlow.value = _viewStateFlow.value.copy(backupLoading = true)
        prefs.backupFileId?.let { id ->
            backupTrackablesUseCase.uploadToDrive(id)
        }
        _viewStateFlow.value = _viewStateFlow.value.copy(backupLoading = false)
    }

    fun toggleGpt4(toggled: Boolean) = viewModelScope.launch {
        prefs.useGpt4 = toggled
        _viewStateFlow.emit(_viewStateFlow.value.copy(useAdvancedAiEnabled = toggled))
    }
}
