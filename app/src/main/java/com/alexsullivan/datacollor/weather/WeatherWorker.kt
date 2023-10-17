package com.alexsullivan.datacollor.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.google.android.gms.location.LocationServices
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltWorker
class WeatherWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val getWeatherUseCase: FetchWeatherEntityUseCase
) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val trackableEntityDatabase = TrackableEntityDatabase.getDatabase(context)
        val weatherDao = trackableEntityDatabase.weatherDao()
        val location = getLastLocation() ?: return Result.retry()
        val weather = getWeatherUseCase(location)
        weatherDao.saveWeather(weather)
        return Result.success()
    }

    private suspend fun getLastLocation(): Location? = suspendCoroutine { continuation ->
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { continuation.resume(it) }
        } else {
            continuation.resume(null)
        }
    }
}
