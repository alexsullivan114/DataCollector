package com.alexsullivan.datacollor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val trackableManager: TrackableManager
): ViewModel() {
    private val _itemsFlow = MutableStateFlow(emptyList<Trackable>())
    private val _triggerUpdateWidgetsFlow = Channel<Unit>()
    val itemsFlow = _itemsFlow.asStateFlow()
    val triggerUpdateWidgetFlow = _triggerUpdateWidgetsFlow.receiveAsFlow()

    init {
        viewModelScope.launch {
            trackableManager.init()
            val trackables = trackableManager.getAllTrackables()
            print(trackables)
            trackableManager.getTrackablesFlow()
                .distinctUntilChanged()
                .collect {
                    _itemsFlow.emit(it)
                    _triggerUpdateWidgetsFlow.send(Unit)
                }
        }
    }

    fun trackableToggled(trackable: Trackable, checked: Boolean) {
        viewModelScope.launch {
            trackableManager.toggleTrackableEnabled(trackable, checked)
        }
    }

    fun trackableAdded(title: String) {
        viewModelScope.launch {
            val trackable = Trackable(0, title, true)
            trackableManager.addTrackable(trackable)
        }
    }
}
