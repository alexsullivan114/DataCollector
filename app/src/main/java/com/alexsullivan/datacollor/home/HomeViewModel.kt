package com.alexsullivan.datacollor.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.UpdateTrackablesUseCase
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableManager
import com.alexsullivan.datacollor.utils.ExportUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val trackableManager: TrackableManager,
    private val updateTrackablesUseCase: UpdateTrackablesUseCase,
    private val exportUtil: ExportUtil
): ViewModel() {
    private val _itemsFlow = MutableStateFlow(emptyList<Trackable>())
    val itemsFlow = _itemsFlow.asStateFlow()

    init {
        viewModelScope.launch {
            trackableManager.getTrackablesFlow()
                .distinctUntilChanged()
                .collect(_itemsFlow::emit)
        }
    }

    fun trackableToggled(trackable: Trackable, checked: Boolean) = viewModelScope.launch {
        trackableManager.toggleTrackableEnabled(trackable, checked)
    }

    fun trackableAdded(trackable: Trackable) = viewModelScope.launch {
        updateTrackablesUseCase.addTrackable(trackable)
    }

    fun trackableDeleted(trackable: Trackable) = viewModelScope.launch {
        updateTrackablesUseCase.deleteTrackable(trackable)
    }

    fun export() {
        viewModelScope.launch { exportUtil.export() }
    }
}
