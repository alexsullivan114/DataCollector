package com.alexsullivan.datacollor.serialization

import com.alexsullivan.datacollor.database.GetTrackableEntitiesUseCase
import com.alexsullivan.datacollor.database.daos.TrackableDao
import com.alexsullivan.datacollor.database.daos.WeatherDao
import javax.inject.Inject

class GetLifetimeDataUseCase @Inject constructor(
    private val trackableDao: TrackableDao,
    private val getTrackableEntities: GetTrackableEntitiesUseCase,
    private val weatherDao: WeatherDao
) {
    suspend operator fun invoke(): LifetimeData {
        val trackableEntities = getTrackableEntities()
        val groupedTrackableEntities = trackableEntities.groupBy { it.date }
        val days = groupedTrackableEntities.map { (date, entities) ->
            DayData(date, entities, weatherDao.getWeather(date))
        }
        return LifetimeData(trackableDao.getEnabled(), days)
    }
}
