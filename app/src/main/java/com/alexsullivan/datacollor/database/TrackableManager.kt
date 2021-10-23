package com.alexsullivan.datacollor.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class TrackableManager(private val database: TrackableEntityDatabase) {
    val state = mutableMapOf<Trackable, Boolean>()
    private var operatingDate: Date = midnight()

    suspend fun init() {
        withContext(Dispatchers.IO) {
            val dao = database.trackableEntityDao()
            insertDefaultTrackables()
            val enabledTrackables = dao.getEnabledTrackables()
            enabledTrackables.forEach { state[it] = false }
            val trackableEntities = dao.getTrackableEntities(midnight())
            trackableEntities.forEach { state[dao.getTrackableById(it.trackableId)] = it.executed }
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

    suspend fun getAllTrackables(): List<Trackable> {
        return database.trackableEntityDao().getAllTrackables()
    }

    suspend fun toggleTrackableEnabled(trackable: Trackable, enabled: Boolean) {
        val enabledTrackable = EnabledTrackable(trackable.id)
        val dao = database.trackableEntityDao()
        if (enabled) {
            dao.saveEnabledTrackable(enabledTrackable)
        } else {
            dao.deleteEnabledTrackable(enabledTrackable)
        }
    }

    suspend fun getAllEnabledTrackables(): List<EnabledTrackable> {
        return database.trackableEntityDao().getAllEnabledTrackables()
    }

    private fun midnight(): Date {
        val calendar: Calendar = GregorianCalendar()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.time
    }

    private suspend fun insertDefaultTrackables() {
       val entityDao = database.trackableEntityDao()
       val defaultTrackables = listOf(
           Trackable(1, "Coffee past 12"),
           Trackable(2, "Alcohol"),
           Trackable(3, "Weed"),
           Trackable(4, "Slept well"),
           Trackable(5, "Woke up rested"),
           Trackable(6, "Exercise"),
           Trackable(7, "Meditate"),
           Trackable(8, "Morning brush"),
           Trackable(9, "Evening brush"),
       )
        defaultTrackables.forEach {
            entityDao.saveTrackable(it)
        }
    }
}
