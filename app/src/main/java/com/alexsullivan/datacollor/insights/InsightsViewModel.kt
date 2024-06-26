package com.alexsullivan.datacollor.insights

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.database.GetBooleanEntitiesUseCase
import com.alexsullivan.datacollor.database.GetNumberEntitiesUseCase
import com.alexsullivan.datacollor.database.GetRatingEntitiesUseCase
import com.alexsullivan.datacollor.database.GetTimeEntitiesUseCase
import com.alexsullivan.datacollor.database.GetTrackableUsCase
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableType
import com.alexsullivan.datacollor.database.entities.BooleanTrackableEntity
import com.alexsullivan.datacollor.database.entities.NumberTrackableEntity
import com.alexsullivan.datacollor.database.entities.Rating
import com.alexsullivan.datacollor.insights.InsightsViewModel.UiState.BooleanUiState
import com.alexsullivan.datacollor.insights.InsightsViewModel.UiState.NumericUiState
import com.alexsullivan.datacollor.insights.InsightsViewModel.UiState.RatingUiState
import com.alexsullivan.datacollor.insights.ratings.MonthRating
import com.alexsullivan.datacollor.insights.ratings.MonthRatingGridDay
import com.alexsullivan.datacollor.routing.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.Year
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getTrackable: GetTrackableUsCase,
    private val getRatingEntities: GetRatingEntitiesUseCase,
    private val getNumberEntities: GetNumberEntitiesUseCase,
    private val getBooleanEntities: GetBooleanEntitiesUseCase,
    private val getTimeEntities: GetTimeEntitiesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiFlow = MutableStateFlow<UiState?>(null)
    val uiFlow = _uiFlow.asStateFlow()

    init {
        val trackableId = savedStateHandle.get<String>(Screen.Insights.trackableIdKey)!!
        viewModelScope.launch {
            val trackable = getTrackable(trackableId)
                ?: throw IllegalStateException("Can't find trackable with id $trackableId")
            val state = when (trackable.type) {
                TrackableType.BOOLEAN -> getBooleanUiState(trackable, trackableId)
                TrackableType.NUMBER -> getNumericUiState(trackable, trackableId)
                TrackableType.RATING -> getRatingUiState(trackable, trackableId)
                TrackableType.TIME -> getTimeUiState(trackable)
            }
            _uiFlow.emit(state)
        }
    }

    private suspend fun getRatingUiState(trackable: Trackable, trackableId: String): RatingUiState {
        val entities = getRatingEntities(trackableId).sortedBy { it.date }
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
            dateRatings = datePairs,
            monthRatings = buildMonthRatingGridData(datePairs)
        )
    }

    private fun buildMonthRatingGridData(datePairs: List<Pair<LocalDate, Rating>>): List<MonthRating> {
        val dataGroupedByMonth = datePairs.sortedByDescending { it.first }.groupBy { (date, _) ->
            date.month to date.year
        }
        return dataGroupedByMonth.map { (key, dateRatings) ->
            val (month, year) = key
            val title = "${
                month.toString().lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            } $year"
            val gridDays = dateRatings.map { (date, rating) ->
                MonthRatingGridDay(date, rating)
            }.toMutableList()
            val gridDaysSet = gridDays.map { it.date }.toSet()
            for (i in 1..month.length(Year.of(year).isLeap)) {
                val date = LocalDate.of(year, month, i)
                if (!gridDaysSet.contains(date)) {
                    if (i < gridDays.size) {
                        gridDays.add(i, MonthRatingGridDay(date, null))
                    } else {
                        gridDays.add(MonthRatingGridDay(date, null))
                    }
                }
            }

            MonthRating(title, gridDays)
        }
    }

    private suspend fun getNumericUiState(trackable: Trackable, trackableId: String): NumericUiState {
        val entities = getNumberEntities(trackableId).sortedBy { it.date }
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

    private suspend fun getBooleanUiState(trackable: Trackable, trackableId: String): BooleanUiState {
        val entities = getBooleanEntities(trackableId).sortedBy { it.date }

        val totalCount = getTotalCount(entities)
        val yearStartCount = getYearStartCount(entities)
        val perWeekCount = getPerWeekCount(entities)
        val dates = getToggledDates(entities)
        return BooleanUiState(trackable.title, totalCount, yearStartCount, perWeekCount, dates)
    }

    private suspend fun getTimeUiState(trackable: Trackable): UiState.TimeUiState {
        val timeTrackables = getTimeEntities(trackable.id)
        val timeDatePairs = timeTrackables.filter { it.time != null }.map { it.date to it.time!! }
        val times = timeTrackables.mapNotNull { it.time }
        val averageTime = times.sumOf { it.toSecondOfDay() } / times.size
        return UiState.TimeUiState(
            trackable.title,
            timeDatePairs,
            LocalTime.ofSecondOfDay(averageTime.toLong())
        )
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
            val dateRatings: List<Pair<LocalDate, Rating>>,
            val monthRatings: List<MonthRating>
        ) : UiState()

        data class TimeUiState(
            val trackableTitle: String,
            val toggledTimes: List<Pair<LocalDate, LocalTime>>,
            val averageToggledTime: LocalTime
        ) : UiState()
    }
}
