package com.alexsullivan.datacollor.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableManager
import com.alexsullivan.datacollor.database.TrackableType
import com.alexsullivan.datacollor.database.entities.BooleanTrackableEntity
import com.alexsullivan.datacollor.database.entities.TrackableEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class InsightsViewModel(
    private val trackableId: String,
    private val trackableManager: TrackableManager
) : ViewModel() {
    private val _uiFlow = MutableStateFlow<UiState?>(null)
    val uiFlow = _uiFlow.asStateFlow()

    init {
        viewModelScope.launch {
            val trackable = trackableManager.getTrackable(trackableId)
                ?: throw IllegalStateException("Can't find trackable with id $trackableId")
            val state = when (trackable.type) {
                TrackableType.BOOLEAN -> getBooleanUiState(trackable)
                TrackableType.NUMBER -> getNumericUiState(trackable)
                TrackableType.RATING -> getRatingUiState(trackable)
            }
            _uiFlow.emit(state)
        }
    }

    private suspend fun getRatingUiState(trackable: Trackable): UiState.RatingUiState {
        TODO()
    }

    private suspend fun getNumericUiState(trackable: Trackable): UiState.NumericUiState {
        val entities = trackableManager.getNumberEntities(trackableId).sortedBy { it.date }
        val datePairs = entities.map { it.date.toLocalDate() to it.count }
        return UiState.NumericUiState(datePairs)
    }

    private suspend fun getBooleanUiState(trackable: Trackable): UiState.BooleanUiState {
        val entities = trackableManager.getBooleanEntities(trackableId).sortedBy { it.date }

        val totalCount = getTotalCount(entities)
        val yearStartCount = getYearStartCount(entities)
        val perWeekCount = getPerWeekCount(entities)
        val dates = getToggledDates(entities)
        return UiState.BooleanUiState(trackable.title, totalCount, yearStartCount, perWeekCount, dates)
    }

    private fun getPerWeekCount(entities: List<BooleanTrackableEntity>): Float {
        if (entities.isEmpty()) {
            return 0f
        }
        val sortedEntities = entities.sortedBy { it.date }
        val executedCount = sortedEntities.count { it.executed }
        val weeksBetween = ChronoUnit.WEEKS.between(sortedEntities.first().date, OffsetDateTime.now())
        return executedCount.toFloat() / weeksBetween
    }

    private fun getToggledDates(entities: List<BooleanTrackableEntity>): List<LocalDate> {
        return entities.filter { it.executed }.map { it.date.toLocalDate() }
    }

    private fun getTotalCount(entities: List<BooleanTrackableEntity>): Int {
        return entities.count { it.executed }
    }

    private fun getYearStartCount(entities: List<BooleanTrackableEntity>): Int {
        val thisYear = LocalDateTime.now().year
        return entities.count {
            val entityYear = it.date.year
            (entityYear == thisYear) && it.executed
        }
    }

    private suspend fun getTrackableEntitiesForTrackable(trackable: Trackable): List<TrackableEntity> {
        return trackableManager.getTrackableEntities(trackable.id)
    }

    sealed class UiState {
        data class BooleanUiState(
            val trackableTitle: String,
            val totalCount: Int,
            val yearStartCount: Int,
            val perWeekCount: Float,
            val daysToggled: List<LocalDate>
        ) : UiState()

        data class NumericUiState(
            val dateCounts: List<Pair<LocalDate, Int>>
        ) : UiState()

        object RatingUiState : UiState()
    }
}
