package com.alexsullivan.datacollor.weather

data class WeatherResponse(val daily: List<DailyWeather>)

data class DailyWeather(val dt: Long, val temp: Temp, val weather: List<DailyWeatherDescription>)

data class Temp(
    val day: Float,
    val min: Float,
    val max: Float,
    val night: Float,
    val eve: Float,
    val morn: Float
)

data class DailyWeatherDescription(val main: String, val description: String)
