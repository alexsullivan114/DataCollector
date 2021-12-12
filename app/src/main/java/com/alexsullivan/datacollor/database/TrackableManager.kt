package com.alexsullivan.datacollor.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*

class TrackableManager(private val database: TrackableEntityDatabase) {
    suspend fun update() {
        val enabledTrackables = database.trackableEntityDao().getEnabledTrackables()
        val trackableEntities = database.trackableEntityDao().getTrackableEntities(midnight())
        enabledTrackables.forEach { trackable ->
            if (!trackableEntities.any { it.trackableId ==  trackable.id}) {
                val entity = TrackableEntity(trackable.id, false, midnight())
                saveEntity(entity)
            }
        }
    }

    suspend fun toggle(trackable: Trackable) {
        val entity = database.trackableEntityDao().getTrackableEntity(midnight(), trackable.id)
        withContext(Dispatchers.IO) {
            val updatedEntity = entity.copy(executed = !entity.executed)
            database.trackableEntityDao().saveEntity(updatedEntity)
        }
    }

    suspend fun getEnabledTrackables(): List<Trackable> {
        return database.trackableEntityDao().getEnabledTrackables()
    }

    suspend fun addTrackable(trackable: Trackable) {
        database.trackableEntityDao().saveTrackable(trackable)
    }

    suspend fun saveEntity(trackableEntity: TrackableEntity) {
        database.trackableEntityDao().saveEntity(trackableEntity)
    }

    suspend fun deleteTrackable(trackable: Trackable) {
        database.trackableEntityDao().deleteTrackable(trackable)
    }

    suspend fun deleteTrackableEntities(id: String) {
        database.trackableEntityDao().deleteTrackableEntities(id)
    }

    fun getTrackablesFlow(): Flow<List<Trackable>> {
        return database.trackableEntityDao().getTrackablesFlow()
    }

    suspend fun toggleTrackableEnabled(trackable: Trackable, enabled: Boolean) {
        val dao = database.trackableEntityDao()
        val updatedTrackable = trackable.copy(enabled = enabled)
        dao.saveTrackable(updatedTrackable)
    }

    suspend fun getTodaysTrackableEntities(): List<TrackableEntity> {
        return database.trackableEntityDao().getTrackableEntities(midnight())
    }

    suspend fun getTrackableById(id: String): Trackable? {
        return database.trackableEntityDao().getTrackableById(id)
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
