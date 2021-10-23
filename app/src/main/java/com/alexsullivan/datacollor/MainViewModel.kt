package com.alexsullivan.datacollor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel(
    private val trackableManager: TrackableManager
): ViewModel() {
    private val _itemsFlow = MutableStateFlow(emptyList<Trackable>())
    val itemsFlow = _itemsFlow.asStateFlow()

    init {
        viewModelScope.launch {
            trackableManager.init()
            trackableManager.getTrackablesFlow().collect(_itemsFlow::emit)
        }
    }

    fun trackableToggled(trackable: Trackable, checked: Boolean) {
        viewModelScope.launch {
            trackableManager.toggleTrackableEnabled(trackable, checked)
        }
    }
}
