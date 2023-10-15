package com.alexsullivan.datacollor.weather

import retrofit2.http.GET

interface WeatherService {
    @GET("onecall?lat=33.44&lon=-94.04&exclude=hourly,minutely,current,alerts&appid=03b92df104c20209282f8cd31306418e&units=imperial")
    suspend fun getWeather(): WeatherResponse
}
