package com.alexsullivan.datacollor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class TrackableManager(private val database: TrackableEntityDatabase) {
    val state = mutableMapOf<Trackable, Boolean>().apply {
        putAll(Trackable.values().map { it to false })
    }
    private var operatingDate: Date = midnight()

    suspend fun init() {
        withContext(Dispatchers.IO) {
            val trackables = database.trackableEntityDao().getTrackableEntities(midnight())
            trackables.forEach { state[it.trackable] = it.executed }
        }
    }

    suspend fun update() {
        if (midnight() != operatingDate) {
            state.clear()
            state.putAll(Trackable.values().map { it to false })
        }
    }

    suspend fun toggle(trackable: Trackable) {
        state[trackable] = !state.getValue(trackable)
        withContext(Dispatchers.IO) {
            val trackableEntity = TrackableEntity(trackable, state.getValue(trackable), midnight())
            database.trackableEntityDao().saveEntity(trackableEntity)
        }
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