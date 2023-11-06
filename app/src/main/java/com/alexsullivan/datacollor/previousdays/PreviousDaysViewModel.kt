package com.alexsullivan.datacollor.previousdays

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.database.TrackableManager
import com.alexsullivan.datacollor.database.entities.TrackableEntity
import com.alexsullivan.datacollor.utils.today
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class PreviousDaysViewModel @Inject constructor(
    private val trackableManager: TrackableManager,
): ViewModel() {

    private var date: LocalDate = today()
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
            is TrackableEntity.Time -> TODO("Add support for time")
        }
    }

    fun dateSelected(dateTimestamp: Long) {
        date = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(dateTimestamp),
            ZoneId.of("UTC")
        ).toLocalDate()
    }

    fun nextDayPressed() {
        date = date.plusDays(1)
    }

    fun previousDayPressed() {
        date = date.minusDays(1)
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

    fun getDate(): LocalDate {
       return date
    }

    private fun dateString(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
    }

    private suspend fun displayableEntitiesForDateFlow(date: LocalDate): Flow<List<DisplayableTrackableEntity>> {
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

    private fun shouldDisableNext(date: LocalDate): Boolean {
        return date.isEqual(LocalDate.now())
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
