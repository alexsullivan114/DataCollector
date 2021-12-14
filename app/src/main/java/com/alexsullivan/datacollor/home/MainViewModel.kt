package com.alexsullivan.datacollor.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.BackupTrackablesUseCase
import com.alexsullivan.datacollor.QLPreferences
import com.alexsullivan.datacollor.TrackableSerializer
import com.alexsullivan.datacollor.UpdateTrackablesUseCase
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val trackableManager: TrackableManager,
    private val updateTrackablesUseCase: UpdateTrackablesUseCase,
    private val backupTrackablesUseCase: BackupTrackablesUseCase,
    private val prefs: QLPreferences
): ViewModel() {
    private val _itemsFlow = MutableStateFlow(emptyList<Trackable>())
    private val _triggerUpdateWidgetsFlow = Channel<Unit>()
    private val _triggerPeriodicWorkFlow = Channel<Unit>()
    val itemsFlow = _itemsFlow.asStateFlow()
    val triggerUpdateWidgetFlow = _triggerUpdateWidgetsFlow.receiveAsFlow()
    val triggerPeriodicWorkFlow = _triggerPeriodicWorkFlow.receiveAsFlow()

    init {
        viewModelScope.launch {
            trackableManager.getTrackablesFlow()
                .distinctUntilChanged()
                .collect {
                    _itemsFlow.emit(it)
                    _triggerUpdateWidgetsFlow.send(Unit)
                }
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

    fun signedInToGoogle() {
        viewModelScope.launch {
            if (prefs.backupFileId == null) {
                val backedUpId = backupTrackablesUseCase.fetchExistingDriveFileId()
                if (backedUpId != null) {
                    val contents =
                        backupTrackablesUseCase.fetchExistingDriveFileContents(backedUpId)
                    val trackableEntityMap = TrackableSerializer.deserialize(contents)
                    trackableEntityMap.forEach { (trackable, entities) ->
                        // TODO: What if there's an existing trackable with this title already?
                        trackableManager.addTrackable(trackable)
                        entities.forEach { trackableManager.saveEntity(it) }
                    }
                }
                val driveFileId = backedUpId ?: backupTrackablesUseCase.createDriveFile()
                prefs.backupFileId = driveFileId
            }
            _triggerPeriodicWorkFlow.send(Unit)
        }
    }
}
