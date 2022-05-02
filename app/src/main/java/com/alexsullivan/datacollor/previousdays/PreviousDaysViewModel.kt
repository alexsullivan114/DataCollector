package com.alexsullivan.datacollor.previousdays

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.database.TrackableManager
import com.alexsullivan.datacollor.database.entities.TrackableEntity
import com.alexsullivan.datacollor.utils.midnight
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PreviousDaysViewModel(
    private val trackableManager: TrackableManager,
): ViewModel() {

    private var date: Date = midnight()
        set(value) {
            field = value
            viewModelScope.launch {
                val newUiState = _uiFlow.value.copy(
                    date = dateString(value),
                    disableNext = shouldDisableNext(value)
                )
                _uiFlow.emit(newUiState)
                collectDisplayableEntitiesForDate()
            }
        }

    private val _triggerUpdateWidgetsFlow = Channel<Unit>()
    private val _uiFlow = MutableStateFlow(initialUiState())
    val uiFlow = _uiFlow.asStateFlow()
    val triggerUpdateWidgetFlow = _triggerUpdateWidgetsFlow.receiveAsFlow()

    private var displayableEntitiesJob: Job? = null

    init {
        collectDisplayableEntitiesForDate()
    }

    private fun TrackableEntity.toDisplayableEntity(title: String): DisplayableTrackableEntity {
        return when (this) {
            is TrackableEntity.Boolean -> DisplayableTrackableEntity.BooleanEntity(
                title,
                trackableId,
                this.booleanEntity.executed
            )
            is TrackableEntity.Number -> DisplayableTrackableEntity.NumberEntity(
                title,
                trackableId,
                this.numberEntity.count
            )
            is TrackableEntity.Rating -> DisplayableTrackableEntity.RatingEntity(
                title,
                trackableId,
                this.ratingEntity.rating
            )
        }
    }

    fun dateSelected(dateTimestamp: Long) {
        val calendar = Calendar.getInstance()
        calendar.time = Date(dateTimestamp)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, 1)
        date = calendar.time
    }

    fun nextDayPressed() {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DATE, 1)
        date = calendar.time
    }

    fun previousDayPressed() {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DATE, -1)
        date = calendar.time
    }

    fun booleanEntityChanged(entity: DisplayableTrackableEntity.BooleanEntity) =
        viewModelScope.launch {
            val trackables = trackableManager.getTrackables()
            val trackable = trackables.first { it.id == entity.trackableId }
            trackableManager.toggle(trackable, date)
            _triggerUpdateWidgetsFlow.send(Unit)
        }

    fun numberEntityChanged(increment: Boolean, entity: DisplayableTrackableEntity.NumberEntity) =
        viewModelScope.launch {
            val trackables = trackableManager.getTrackables()
            val trackable = trackables.first { it.id == entity.trackableId }
            trackableManager.updateCount(trackable, increment, date)
            _triggerUpdateWidgetsFlow.send(Unit)
        }

    fun ratingEntityChanged(increment: Boolean, entity: DisplayableTrackableEntity.RatingEntity) =
        viewModelScope.launch {
            val trackables = trackableManager.getTrackables()
            val trackable = trackables.first { it.id == entity.trackableId }
            trackableManager.updateRating(trackable, increment, date)
            _triggerUpdateWidgetsFlow.send(Unit)
        }

    fun getDate(): Date {
       return date
    }

    private fun dateString(date: Date): String {
        val formatter = SimpleDateFormat.getDateInstance()
        return formatter.format(date)
    }

    private suspend fun displayableEntitiesForDateFlow(date: Date): Flow<List<DisplayableTrackableEntity>> {
        val trackables = trackableManager.getTrackables()
        return trackableManager.getTrackableEntitiesByDateFlow(date)
            .map { entities ->
                entities.map { entity ->
                    val title = trackables.first { it.id == entity.trackableId }.title
                    entity.toDisplayableEntity(title)
                }.sortedBy { it.title }
            }
    }

    private fun collectDisplayableEntitiesForDate() = viewModelScope.launch {
        val displayableEntitiesFlow = displayableEntitiesForDateFlow(date)
        displayableEntitiesJob?.cancel()
        displayableEntitiesJob = viewModelScope.launch {
            displayableEntitiesFlow.collect {
                val updatedPreviousDaysUi = _uiFlow.value.copy(items = it)
                _uiFlow.emit(updatedPreviousDaysUi)
            }
        }
    }

    private fun shouldDisableNext(date: Date): Boolean {
        val calendar = Calendar.getInstance()
        val today = Date()
        calendar.time = today
        val todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        calendar.time = date
        val dateDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        return dateDayOfYear >= todayDayOfYear
    }

    private fun initialUiState() = UiState(
        date = dateString(date),
        items = emptyList(),
        disableNext = shouldDisableNext(date),
    )

    data class UiState(
        val date: String,
        val items: List<DisplayableTrackableEntity>,
        val disableNext: Boolean,
    )
}
