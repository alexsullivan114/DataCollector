package com.alexsullivan.datacollor.weather

import com.alexsullivan.datacollor.database.daos.WeatherDao
import com.alexsullivan.datacollor.database.entities.WeatherEntity
import javax.inject.Inject

class GetAllWeatherEntitiesUseCase @Inject constructor(private val weatherDao: WeatherDao) {
    suspend operator fun invoke(): List<WeatherEntity> {
        return weatherDao.getAllWeather()
    }
}
