package com.alexsullivan.datacollor.database

import com.alexsullivan.datacollor.database.entities.*
import com.alexsullivan.datacollor.utils.midnight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.max

class TrackableManager(database: TrackableEntityDatabase) {
    private val booleanDao = database.trackableBooleanDao()
    private val numberDao = database.trackableNumberDao()
    private val ratingDao = database.trackableRatingDao()
    private val trackableDao = database.trackableDao()

    suspend fun update() {
        val enabledTrackables = trackableDao.getEnabled()
        val booleanEntities = booleanDao.getEntities(midnight())
        val numberEntities = numberDao.getEntities(midnight())
        val ratingEntities = ratingDao.getEntities(midnight())
        enabledTrackables.forEach { trackable ->
            when (trackable.type) {
                TrackableType.BOOLEAN -> {
                    if (!booleanEntities.any { it.trackableId == trackable.id}) {
                        val entity = BooleanTrackableEntity(trackable.id, false, midnight())
                        saveEntity(TrackableEntity.Boolean(entity))
                    }
                }
                TrackableType.NUMBER -> {
                    if (!numberEntities.any { it.trackableId == trackable.id}) {
                        val entity = NumberTrackableEntity(trackable.id, 0, midnight())
                        saveEntity(TrackableEntity.Number(entity))
                    }
                }
                TrackableType.RATING -> {
                    if (!ratingEntities.any { it.trackableId == trackable.id }) {
                        val entity = RatingTrackableEntity(trackable.id, Rating.MEDIOCRE, midnight())
                        saveEntity(TrackableEntity.Rating(entity))
                    }
                }
            }
        }
    }

    suspend fun toggle(trackable: Trackable, date: Date = midnight()) {
        println("Date: $date")
        println("Trackable: $trackable")
        val entity = booleanDao.getEntity(date, trackable.id)
        withContext(Dispatchers.IO) {
            val updatedEntity = entity.copy(executed = !entity.executed)
            booleanDao.save(updatedEntity)
        }
    }

    suspend fun updateCount(trackable: Trackable, increment: Boolean, date: Date = midnight()) {
        val entity = numberDao.getEntity(date, trackable.id)
        withContext(Dispatchers.IO) {
            val newCount = if (increment) entity.count + 1 else max(0, entity.count - 1)
            val updatedEntity = entity.copy(count = newCount)
            numberDao.save(updatedEntity)
        }
    }

    suspend fun updateRating(trackable: Trackable, increment: Boolean, date: Date = midnight()) {
        val entity = ratingDao.getEntity(date, trackable.id)
        withContext(Dispatchers.IO) {
            val newRating = if (increment) entity.rating.increment() else entity.rating.decrement()
            val updatedEntity = entity.copy(rating = newRating)
            ratingDao.save(updatedEntity)
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
        }
    }

    suspend fun deleteTrackable(trackable: Trackable) {
        trackableDao.deleteTrackable(trackable)
    }

    suspend fun deleteTrackableEntities(id: String) {
        booleanDao.delete(id)
        numberDao.delete(id)
        ratingDao.delete(id)
    }

    fun getTrackablesFlow(): Flow<List<Trackable>> {
        return trackableDao.getTrackablesFlow()
    }

    fun getTrackableEntitiesByDateFlow(date: Date): Flow<List<TrackableEntity>> {
        val booleansFlow = booleanDao.getEntitiesFlow(date).map { entitiesList ->
            entitiesList.map { TrackableEntity.Boolean(it) }
        }
        val numbersFlow = numberDao.getEntitiesFlow(date).map { entitiesList ->
            entitiesList.map { TrackableEntity.Number(it) }
        }
        val ratingsFlow = ratingDao.getEntitiesFlow(date).map { entitiesList ->
            entitiesList.map { TrackableEntity.Rating(it) }
        }


        return combine(booleansFlow, numbersFlow, ratingsFlow) { a, b, c ->
            a + b + c
        }
    }

    suspend fun toggleTrackableEnabled(trackable: Trackable, enabled: Boolean) {
        val updatedTrackable = trackable.copy(enabled = enabled)
        trackableDao.saveTrackable(updatedTrackable)
    }

    suspend fun getTrackableEntities(): List<TrackableEntity> {
        val booleanEntities = booleanDao.getEntities().map { TrackableEntity.Boolean(it) }
        val numberEntities = numberDao.getEntities().map { TrackableEntity.Number(it) }
        val ratingEntities = ratingDao.getEntities().map { TrackableEntity.Rating(it) }
        return booleanEntities + numberEntities + ratingEntities
    }

    suspend fun getTodaysTrackableEntities(): List<TrackableEntity> {
        val booleanEntities = booleanDao.getEntities(midnight()).map { TrackableEntity.Boolean(it) }
        val numberEntities = numberDao.getEntities(midnight()).map { TrackableEntity.Number(it) }
        val ratingEntities = ratingDao.getEntities(midnight()).map { TrackableEntity.Rating(it) }
        return booleanEntities + numberEntities + ratingEntities
    }
}
