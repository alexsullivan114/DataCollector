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
    private val _backupLoadingFlow = MutableStateFlow(false)
    val backupLoadingFlow = _backupLoadingFlow.asStateFlow()

    fun backupToDriveClicked() = viewModelScope.launch {
        _backupLoadingFlow.value = true
        prefs.backupFileId?.let { id ->
            backupTrackablesUseCase.uploadToDrive(id)
        }
        _backupLoadingFlow.value = false
    }
}
