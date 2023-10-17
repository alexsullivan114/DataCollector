package com.alexsullivan.datacollor.weather

import android.location.Location
import com.alexsullivan.datacollor.database.entities.WeatherEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

class FetchWeatherEntityUseCase @Inject constructor(private val weatherService: WeatherService) {
    suspend operator fun invoke(location: Location): WeatherEntity {
        val dailyWeather = weatherService.getWeather(location.latitude, location.longitude).daily[0]
        val date = LocalDate.ofInstant(Instant.ofEpochSecond(dailyWeather.dt), ZoneOffset.UTC)
        return WeatherEntity(
            date,
            dailyWeather.temp.day,
            dailyWeather.weather[0].main
        )
    }
}
