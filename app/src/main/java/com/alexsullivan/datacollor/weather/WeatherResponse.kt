package com.alexsullivan.datacollor.weather

import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(val daily: List<DailyWeather>)

@Serializable
data class DailyWeather(val dt: Long, val temp: Temp, val weather: List<DailyWeatherDescription>)

@Serializable
data class Temp(
    val day: Float,
    val min: Float,
    val max: Float,
    val night: Float,
    val eve: Float,
    val morn: Float
)

@Serializable
data class DailyWeatherDescription(val main: String, val description: String)

@Serializable
data class HistoricalWeatherResponse(val data: List<HistoricalWeatherData>)
@Serializable
data class HistoricalWeatherData(val dt: Long, val temp: Float, val weather: List<DailyWeatherDescription>)
