package com.alexsullivan.datacollor.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableManager
import com.alexsullivan.datacollor.database.TrackableType
import com.alexsullivan.datacollor.database.entities.BooleanTrackableEntity
import com.alexsullivan.datacollor.database.entities.NumberTrackableEntity
import com.alexsullivan.datacollor.database.entities.Rating
import com.alexsullivan.datacollor.insights.InsightsViewModel.UiState.BooleanUiState
import com.alexsullivan.datacollor.insights.InsightsViewModel.UiState.NumericUiState
import com.alexsullivan.datacollor.insights.InsightsViewModel.UiState.RatingUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.floor

class InsightsViewModel @AssistedInject constructor(
    @Assisted private val trackableId: String,
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
                TrackableType.TIME -> TODO("Add time insights")
            }
            _uiFlow.emit(state)
        }
    }

    private suspend fun getRatingUiState(trackable: Trackable): RatingUiState {
        val entities = trackableManager.getRatingEntities(trackableId).sortedBy { it.date }
        val datePairs = entities.map { it.date to it.rating }
        val dayAverages = entities.groupBy { it.date.dayOfWeek }.mapValues { (dayOfWeek, entities) ->
            val averageEntitiesRating = (entities.sumOf { it.rating.value }) / entities.size.toFloat()
            averageEntitiesRating
        }
        val maxRatedDay = dayAverages.maxByOrNull { it.value }!!.key
        val minRatedDay = dayAverages.minByOrNull { it.value }!!.key
        val averageDayValue = (entities.sumOf { it.rating.value }) / entities.size.toFloat()
        val averageDayBound = Rating.fromValue(floor(averageDayValue).toInt()) to Rating.fromValue(
            ceil(averageDayValue).toInt()
        )
        return RatingUiState(
            trackableTitle = trackable.title,
            lowestRatedDay = minRatedDay,
            highestRatedDay = maxRatedDay,
            averageRatingBound = averageDayBound,
            dateRatings = datePairs
        )
    }

    private suspend fun getNumericUiState(trackable: Trackable): NumericUiState {
        val entities = trackableManager.getNumberEntities(trackableId).sortedBy { it.date }
        val datePairs = entities.map { it.date to it.count }
        val totalCount = entities.sumOf { it.count }
        val yearStartCount = getNumericYearStartCount(entities)
        val perWeekCount = getNumericPerWeekCount(entities)
        return NumericUiState(trackable.title, totalCount, yearStartCount, perWeekCount, datePairs)
    }

    private fun getNumericPerWeekCount(entities: List<NumberTrackableEntity>): Float {
        if (entities.isEmpty()) {
            return 0f
        }
        val sortedEntities = entities.sortedBy { it.date }
        val totalCount = entities.sumOf { it.count }
        val weeksBetween = ChronoUnit.WEEKS.between(sortedEntities.first().date, OffsetDateTime.now())
        return totalCount.toFloat() / weeksBetween
    }

    private fun getNumericYearStartCount(entities: List<NumberTrackableEntity>): Int {
        val thisYear = LocalDateTime.now().year
        return entities.sumOf {
            val entityYear = it.date.year
            if (entityYear == thisYear) {
                it.count
            } else {
                0
            }
        }
    }

    private suspend fun getBooleanUiState(trackable: Trackable): BooleanUiState {
        val entities = trackableManager.getBooleanEntities(trackableId).sortedBy { it.date }

        val totalCount = getTotalCount(entities)
        val yearStartCount = getYearStartCount(entities)
        val perWeekCount = getPerWeekCount(entities)
        val dates = getToggledDates(entities)
        return BooleanUiState(trackable.title, totalCount, yearStartCount, perWeekCount, dates)
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
        return entities.filter { it.executed }.map { it.date }
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

    sealed class UiState {
        data class BooleanUiState(
            val trackableTitle: String,
            val totalCount: Int,
            val yearStartCount: Int,
            val perWeekCount: Float,
            val daysToggled: List<LocalDate>
        ) : UiState()

        data class NumericUiState(
            val trackableTitle: String,
            val totalCount: Int,
            val yearStartCount: Int,
            val perWeekCount: Float,
            val dateCounts: List<Pair<LocalDate, Int>>
        ) : UiState()

        data class RatingUiState(
            val trackableTitle: String,
            val lowestRatedDay: DayOfWeek,
            val highestRatedDay: DayOfWeek,
            val averageRatingBound: Pair<Rating, Rating>,
            val dateRatings: List<Pair<LocalDate, Rating>>
        ) : UiState()
    }

    companion object {
        fun provideFactory(
            assistedFactory: InsightsViewModelFactory,
            trackableId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(trackableId) as T
            }
        }
    }
}

@AssistedFactory
interface InsightsViewModelFactory {
    fun create(trackableId: String): InsightsViewModel
}
