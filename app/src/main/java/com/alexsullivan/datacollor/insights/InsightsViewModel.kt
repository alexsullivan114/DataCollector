package com.alexsullivan.datacollor.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableManager
import com.alexsullivan.datacollor.database.entities.TrackableEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class InsightsViewModel(
    private val trackableId: String,
    private val trackableManager: TrackableManager
) : ViewModel() {
    private val _uiFlow = MutableStateFlow<UiState?>(null)
    val uiFlow = _uiFlow.asStateFlow()

    init {
        viewModelScope.launch {
            _uiFlow.emit(getBooleanUiState(trackableId))
        }
    }

    private suspend fun getBooleanUiState(id: String): UiState.BooleanUiState {
        val trackable = trackableManager.getTrackable(id)
            ?: throw IllegalStateException("Can't find trackable with id $id")
        val entities = trackableManager.getBooleanEntities(id).sortedBy { it.date }
        val totalCount = entities.count { it.executed }

        val thisYear = LocalDateTime.now().year
        val yearStartCount = entities.count {
            val entityYear = it.date.year
            (entityYear == thisYear) && it.executed
        }

        return UiState.BooleanUiState(trackable.title, totalCount, yearStartCount)
    }

    private suspend fun getTrackableEntitiesForTrackable(trackable: Trackable): List<TrackableEntity> {
        return trackableManager.getTrackableEntities(trackable.id)
    }

    sealed class UiState {
        data class BooleanUiState(
            val trackableTitle: String,
            val totalCount: Int,
            val yearStartCount: Int
        ) : UiState()
    }
}
