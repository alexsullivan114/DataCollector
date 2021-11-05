package com.alexsullivan.datacollor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(
    private val trackableManager: TrackableManager
): ViewModel() {
    private val _itemsFlow = MutableStateFlow(emptyList<Trackable>())
    private val _triggerUpdateWidgetsFlow = Channel<Unit>()
    val itemsFlow = _itemsFlow.asStateFlow()
    val triggerUpdateWidgetFlow = _triggerUpdateWidgetsFlow.receiveAsFlow()

    init {
        viewModelScope.launch {
            println("We be initing from view model")
            trackableManager.init()
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
        println("Added")
        viewModelScope.launch {
            val uuid = UUID.randomUUID().toString()
            val trackable = Trackable(uuid, title, true)
            trackableManager.addTrackable(trackable)
        }
    }

    fun trackableDeleted(trackable: Trackable) {
        println("Deleted")
        viewModelScope.launch {
            trackableManager.deleteTrackable(trackable)
            trackableManager.deleteTrackableEntities(trackable.id)
        }
    }
}
