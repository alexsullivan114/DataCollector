package com.alexsullivan.datacollor.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*

class TrackableManager(private val database: TrackableEntityDatabase) {
    // TODO: Why do we need state? Can't we just reference the database?
    val state = mutableMapOf<Trackable, Boolean>()
    private var operatingDate: Date = midnight()

    suspend fun init() {
        withContext(Dispatchers.IO) {
            println("We be initing")
            val dao = database.trackableEntityDao()
            val enabledTrackables = dao.getEnabledTrackables()
            enabledTrackables.forEach { state[it] = false }
            val trackableEntities = dao.getTrackableEntities(midnight())
            trackableEntities.forEach {
                val trackable = dao.getTrackableById(it.trackableId)
                if (trackable != null) {
                    state[trackable] = it.executed
                }
            }
        }
    }

    suspend fun update() {
        if (midnight() != operatingDate) {
            state.clear()
            val enabledTrackable = database.trackableEntityDao().getEnabledTrackables()
            enabledTrackable.forEach { trackable ->
                state[trackable] = false
            }
        }
    }

    suspend fun toggle(trackable: Trackable) {
        state[trackable] = !state.getValue(trackable)
        withContext(Dispatchers.IO) {
            val trackableEntity = TrackableEntity(trackable.id, state.getValue(trackable), midnight())
            database.trackableEntityDao().saveEntity(trackableEntity)
        }
    }

    suspend fun getEnabledTrackables(): List<Trackable> {
        return database.trackableEntityDao().getEnabledTrackables()
    }

    suspend fun addTrackable(trackable: Trackable) {
        database.trackableEntityDao().saveTrackable(trackable)
    }

    suspend fun deleteTrackable(trackable: Trackable) {
        // TODO: I think we need to remove it from the state map as well...
        // UPDATE: I think since init gets called so much the state map is constantly rebuilt so
        // we're all right.
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

    private fun midnight(): Date {
        val calendar: Calendar = GregorianCalendar()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.time
    }
}
