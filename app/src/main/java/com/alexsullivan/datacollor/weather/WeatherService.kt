package com.alexsullivan.datacollor.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("onecall?exclude=hourly,minutely,current,alerts&appid=03b92df104c20209282f8cd31306418e&units=imperial")
    suspend fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): WeatherResponse

    @GET("onecall/timemachine?appid=03b92df104c20209282f8cd31306418e&units=imperial")
    suspend fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("dt") timestamp: Long
    ): HistoricalWeatherResponse
}
