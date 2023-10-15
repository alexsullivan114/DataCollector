package com.alexsullivan.datacollor.weather

import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(private val weatherService: WeatherService) {
    suspend operator fun invoke(): DailyWeather {
        return weatherService.getWeather().daily[0]
    }
}
