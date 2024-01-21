package com.alexsullivan.datacollor.database

import com.alexsullivan.datacollor.database.entities.BooleanTrackableEntity
import com.alexsullivan.datacollor.database.entities.NumberTrackableEntity
import com.alexsullivan.datacollor.database.entities.Rating
import com.alexsullivan.datacollor.database.entities.RatingTrackableEntity
import com.alexsullivan.datacollor.database.entities.TimeTrackableEntity
import com.alexsullivan.datacollor.database.entities.TrackableEntity
import com.alexsullivan.datacollor.utils.today
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.max

// TODO: Predictably this has become super overloaded. I feel like it should really only be used
// to power the widget; everything else should go through use cases and stuff. Right now my game
// plan is to start creating use cases for some of this and try to avoid using it going forward,
// so there might be duplicated functionality between here and some use cases - prefer the use cases
class TrackableManager(database: TrackableEntityDatabase) {
    private val booleanDao = database.trackableBooleanDao()
    private val numberDao = database.trackableNumberDao()
    private val ratingDao = database.trackableRatingDao()
    private val timeDao = database.trackableTimeDao()
    private val trackableDao = database.trackableDao()

    suspend fun update() {
        val enabledTrackables = trackableDao.getEnabled()
        val booleanEntities = booleanDao.getEntities(today())
        val numberEntities = numberDao.getEntities(today())
        val ratingEntities = ratingDao.getEntities(today())
        val timeEntities = timeDao.getEntities(today())
        enabledTrackables.forEach { trackable ->
            when (trackable.type) {
                TrackableType.BOOLEAN -> {
                    if (!booleanEntities.any { it.trackableId == trackable.id}) {
                        val entity = BooleanTrackableEntity(trackable.id, false, today())
                        saveEntity(TrackableEntity.Boolean(entity))
                    }
                }
                TrackableType.NUMBER -> {
                    if (!numberEntities.any { it.trackableId == trackable.id}) {
                        val entity = NumberTrackableEntity(trackable.id, 0, today())
                        saveEntity(TrackableEntity.Number(entity))
                    }
                }
                TrackableType.RATING -> {
                    if (!ratingEntities.any { it.trackableId == trackable.id }) {
                        val entity = RatingTrackableEntity(trackable.id, Rating.MEDIOCRE, today())
                        saveEntity(TrackableEntity.Rating(entity))
                    }
                }
                TrackableType.TIME -> {
                    if (!timeEntities.any { it.trackableId == trackable.id }) {
                        val entity = TimeTrackableEntity(trackable.id, null, today())
                        saveEntity(TrackableEntity.Time(entity))
                    }
                }
            }
        }
    }

    suspend fun toggle(trackable: Trackable, date: LocalDate = today()) {
        val entity = booleanDao.getEntity(date, trackable.id)
        withContext(Dispatchers.IO) {
            val updatedEntity = entity.copy(executed = !entity.executed)
            booleanDao.save(updatedEntity)
        }
    }

    suspend fun updateCount(trackable: Trackable, increment: Boolean, date: LocalDate = today()) {
        val entity = numberDao.getEntity(date, trackable.id)
        withContext(Dispatchers.IO) {
            val newCount = if (increment) entity.count + 1 else max(0, entity.count - 1)
            val updatedEntity = entity.copy(count = newCount)
            numberDao.save(updatedEntity)
        }
    }

    suspend fun updateRating(trackable: Trackable, increment: Boolean, date: LocalDate = today()) {
        val entity = ratingDao.getEntity(date, trackable.id)
        withContext(Dispatchers.IO) {
            val newRating = if (increment) entity.rating.increment() else entity.rating.decrement()
            val updatedEntity = entity.copy(rating = newRating)
            ratingDao.save(updatedEntity)
        }
    }

    suspend fun updateTime(trackable: Trackable, date: LocalDate = today(), time: LocalTime = LocalTime.now()) {
       val entity = timeDao.getEntity(date, trackable.id)
        withContext(Dispatchers.IO) {
            val updatedEntity = entity.copy(time = time)
            timeDao.save(updatedEntity)
        }
    }

    suspend fun getEnabledTrackables(): List<Trackable> {
        return trackableDao.getEnabled()
    }

    suspend fun getTrackables(): List<Trackable> {
        return trackableDao.getTrackables()
    }

    suspend fun addTrackable(trackable: Trackable) {
        trackableDao.saveTrackable(trackable)
    }

    suspend fun saveEntity(entity: TrackableEntity) {
        return when (entity) {
            is TrackableEntity.Boolean -> booleanDao.save(entity.booleanEntity)
            is TrackableEntity.Number -> numberDao.save(entity.numberEntity)
            is TrackableEntity.Rating -> ratingDao.save(entity.ratingEntity)
            is TrackableEntity.Time -> timeDao.save(entity.timeEntity)
        }
    }

    suspend fun deleteTrackable(trackable: Trackable) {
        trackableDao.deleteTrackable(trackable)
    }

    suspend fun deleteTrackableEntities(id: String) {
        booleanDao.deleteAllForTrackable(id)
        numberDao.deleteAllForTrackable(id)
        ratingDao.deleteAllForTrackable(id)
    }

    fun getTrackablesFlow(): Flow<List<Trackable>> {
        return trackableDao.getTrackablesFlow()
    }

    // TODO: This should be replaced by a usecase
    fun getTrackableEntitiesByDateFlow(date: LocalDate): Flow<List<TrackableEntity>> {
        val booleansFlow = booleanDao.getEntitiesFlow(date).map { entitiesList ->
            entitiesList.map { TrackableEntity.Boolean(it) }
        }
        val numbersFlow = numberDao.getEntitiesFlow(date).map { entitiesList ->
            entitiesList.map { TrackableEntity.Number(it) }
        }
        val ratingsFlow = ratingDao.getEntitiesFlow(date).map { entitiesList ->
            entitiesList.map { TrackableEntity.Rating(it) }
        }
        val timesFlow = timeDao.getEntitiesFlow(date).map { entitiesList ->
            entitiesList.map { TrackableEntity.Time(it) }
        }

        return combine(booleansFlow, numbersFlow, ratingsFlow, timesFlow) { a, b, c, d ->
            a + b + c + d
        }
    }

    suspend fun toggleTrackableEnabled(trackable: Trackable, enabled: Boolean) {
        val updatedTrackable = trackable.copy(enabled = enabled)
        trackableDao.saveTrackable(updatedTrackable)
    }

    // TODO: This should be replaced by a use case
    suspend fun getTodaysTrackableEntities(): List<TrackableEntity> {
        val booleanEntities = booleanDao.getEntities(today()).map { TrackableEntity.Boolean(it) }
        val numberEntities = numberDao.getEntities(today()).map { TrackableEntity.Number(it) }
        val ratingEntities = ratingDao.getEntities(today()).map { TrackableEntity.Rating(it) }
        val timeEntities = timeDao.getEntities(today()).map { TrackableEntity.Time(it) }
        return booleanEntities + numberEntities + ratingEntities + timeEntities
    }
}
