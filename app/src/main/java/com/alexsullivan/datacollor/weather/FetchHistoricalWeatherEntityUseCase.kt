package com.alexsullivan.datacollor.weather

import android.location.Location
import com.alexsullivan.datacollor.database.entities.WeatherEntity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject

class FetchHistoricalWeatherEntityUseCase @Inject constructor(private val weatherService: WeatherService) {
    suspend operator fun invoke(location: Location, date: LocalDate): WeatherEntity {
        val epochSeconds = date.toEpochSecond(LocalTime.NOON, ZoneOffset.UTC)
        val weatherResponse = weatherService.getWeather(
            location.latitude,
            location.longitude,
            epochSeconds
        )
        val weatherData = weatherResponse.data[0]
        val returnedDate = LocalDate.ofInstant(Instant.ofEpochSecond(weatherData.dt), ZoneOffset.UTC)
        return WeatherEntity(
            returnedDate,
            weatherData.temp,
            weatherData.weather[0].main
        )
    }
}
