package com.alexsullivan.datacollor.weather

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WeatherWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val getWeatherUseCase: GetWeatherEntityUseCase
) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val trackableEntityDatabase = TrackableEntityDatabase.getDatabase(context)
        val weatherDao = trackableEntityDatabase.weatherDao()
        val weather = getWeatherUseCase()
        weatherDao.saveWeather(weather)
        return Result.success()
    }
}
