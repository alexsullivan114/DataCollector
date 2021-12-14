package com.alexsullivan.datacollor.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*

class TrackableManager(database: TrackableEntityDatabase) {
    private val booleanDao = database.trackableBooleanDao()
    private val numberDao = database.trackableNumberDao()
    private val trackableDao = database.trackableDao()

    suspend fun update() {
        val enabledTrackables = trackableDao.getEnabled()
        val booleanEntities = booleanDao.getEntities(midnight())
        val numberEntities = numberDao.getEntities(midnight())
        enabledTrackables.forEach { trackable ->
            when (trackable.type) {
                TrackableType.BOOLEAN -> {
                    if (!booleanEntities.any { it.trackableId ==  trackable.id}) {
                        val entity = BooleanTrackableEntity(trackable.id, false, midnight())
                        saveEntity(TrackableEntity.Boolean(entity))
                    }
                }
                TrackableType.NUMBER -> {
                    if (!numberEntities.any { it.trackableId ==  trackable.id}) {
                        val entity = NumberTrackableEntity(trackable.id, 0, midnight())
                        saveEntity(TrackableEntity.Number(entity))
                    }
                }
            }
        }
    }

    suspend fun toggle(trackable: Trackable) {
        val entity = booleanDao.getEntity(midnight(), trackable.id)
        withContext(Dispatchers.IO) {
            val updatedEntity = entity.copy(executed = !entity.executed)
            booleanDao.save(updatedEntity)
        }
    }

    suspend fun updateCount(trackable: Trackable, increment: Boolean) {
        val entity = numberDao.getEntity(midnight(), trackable.id)
        withContext(Dispatchers.IO) {
            val newCount = if (increment) entity.count + 1 else entity.count - 1
            val updatedEntity = entity.copy(count = newCount)
            numberDao.save(updatedEntity)
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
        when (entity) {
            is TrackableEntity.Boolean -> booleanDao.save(entity.booleanEntity)
            is TrackableEntity.Number -> numberDao.save(entity.numberEntity)
        }
    }

    suspend fun deleteTrackable(trackable: Trackable) {
        trackableDao.deleteTrackable(trackable)
    }

    suspend fun deleteTrackableEntities(id: String) {
        booleanDao.delete(id)
        numberDao.delete(id)
    }

    fun getTrackablesFlow(): Flow<List<Trackable>> {
        return trackableDao.getTrackablesFlow()
    }

    suspend fun toggleTrackableEnabled(trackable: Trackable, enabled: Boolean) {
        val updatedTrackable = trackable.copy(enabled = enabled)
        trackableDao.saveTrackable(updatedTrackable)
    }

    suspend fun getTrackableEntities(): List<TrackableEntity> {
        val booleanEntities = booleanDao.getEntities().map { TrackableEntity.Boolean(it) }
        val numberEntities = numberDao.getEntities().map { TrackableEntity.Number(it) }
        return booleanEntities + numberEntities
    }

    suspend fun getTodaysTrackableEntities(): List<TrackableEntity> {
        val booleanEntities = booleanDao.getEntities(midnight()).map { TrackableEntity.Boolean(it) }
        val numberEntities = numberDao.getEntities(midnight()).map { TrackableEntity.Number(it) }
        return booleanEntities + numberEntities
    }

    private fun midnight(): Date {
        val calendar: Calendar = GregorianCalendar()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.time
    }
}
