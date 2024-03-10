package com.alexsullivan.datacollor.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.QLPreferences
import com.alexsullivan.datacollor.database.TrackableManager
import com.alexsullivan.datacollor.database.daos.WeatherDao
import com.alexsullivan.datacollor.drive.BackupTrackablesUseCase
import com.alexsullivan.datacollor.serialization.TrackableDeserializer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefs: QLPreferences,
    private val backupTrackablesUseCase: BackupTrackablesUseCase,
    private val trackableManager: TrackableManager,
    private val weatherDao: WeatherDao,
) : ViewModel() {
    private val _triggerUpdateWidgetsFlow = Channel<Unit>()
    private val _triggerPeriodicWorkFlow = Channel<Unit>()
    val triggerUpdateWidgetFlow = _triggerUpdateWidgetsFlow.receiveAsFlow()
    val triggerPeriodicWorkFlow = _triggerPeriodicWorkFlow.receiveAsFlow()

    init {
        viewModelScope.launch {
            trackableManager.getTrackablesFlow()
                .distinctUntilChanged()
                .collect {
                    _triggerUpdateWidgetsFlow.send(Unit)
                }
        }
    }

    fun signedInToGoogle() {
        viewModelScope.launch {
            if (prefs.backupFileId == null) {
                val backedUpId = backupTrackablesUseCase.fetchExistingDriveFileId()
                if (backedUpId != null) {
                    val contents =
                        backupTrackablesUseCase.fetchExistingDriveFileContents(backedUpId)
                    // TODO: This should be a new use case.
                    val lifetimeData = TrackableDeserializer.deserialize(contents)
                    lifetimeData.trackables.forEach { trackableManager.addTrackable(it) }
                    lifetimeData.days.map { it.trackedEntities }.flatten()
                        .forEach { trackableManager.saveEntity(it) }
                    lifetimeData.days.forEach {
                        it.weatherEntity?.let { weather ->
                            weatherDao.saveWeather(
                                weather
                            )
                        }
                    }
                }
                val driveFileId = backedUpId ?: backupTrackablesUseCase.createDriveFile()
                prefs.backupFileId = driveFileId
            }
            _triggerPeriodicWorkFlow.send(Unit)
        }
    }
}
