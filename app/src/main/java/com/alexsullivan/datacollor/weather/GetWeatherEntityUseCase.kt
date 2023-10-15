package com.alexsullivan.datacollor.weather

import com.alexsullivan.datacollor.database.entities.WeatherEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

class GetWeatherEntityUseCase @Inject constructor(private val weatherService: WeatherService) {
    suspend operator fun invoke(): WeatherEntity {
        val dailyWeather = weatherService.getWeather().daily[0]
        val date = LocalDate.ofInstant(Instant.ofEpochSecond(dailyWeather.dt), ZoneOffset.UTC)
        return WeatherEntity(
            date,
            dailyWeather.temp.day,
            dailyWeather.weather[0].main
        )
    }
}
