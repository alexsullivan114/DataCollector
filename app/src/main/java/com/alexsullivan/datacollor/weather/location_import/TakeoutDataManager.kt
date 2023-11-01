package com.alexsullivan.datacollor.weather.location_import

import android.content.Context
import android.location.Location
import com.alexsullivan.datacollor.R
import com.alexsullivan.datacollor.database.GetTrackableEntitiesUseCase
import com.alexsullivan.datacollor.database.daos.WeatherDao
import com.alexsullivan.datacollor.weather.FetchHistoricalWeatherEntityUseCase
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

class TakeoutDataManager @Inject constructor(
    val fetchHistoricalWeatherEntityUseCase: FetchHistoricalWeatherEntityUseCase,
    val weatherDao: WeatherDao,
    val getTrackableEntitiesUseCase: GetTrackableEntitiesUseCase
) {
    private suspend fun importTakeoutData(semanticLocationHistoryMonthString: String) {
        val dateLocations = buildDateLocationsForMonth(semanticLocationHistoryMonthString)
        val trackableEntities = getTrackableEntitiesUseCase()
        dateLocations.forEach { (date, location) ->
            if (weatherDao.getWeather(date) == null && trackableEntities.any { it.date == date }) {
                val mappedLocation = Location("fake").apply {
                    latitude = location.latitudeE7
                    longitude = location.longitudeE7
                }
                val weather = fetchHistoricalWeatherEntityUseCase.invoke(mappedLocation, date)
                weatherDao.saveWeather(weather)
            }
        }
    }

    suspend fun triggerImport(context: Context) {
        R.raw::class.java.declaredFields.forEach {
            val stringData = context.resources.openRawResource(it.getInt(it)).bufferedReader().use { it.readText() }
            importTakeoutData(stringData)
        }
    }

    private fun buildDateLocationsForMonth(semanticLocationHistoryMonthString: String): List<DateLocation> {
        val moshi = Moshi.Builder().add(TimeAdapter()).build()
        val takeoutSemanticLocationHistoryMonth = try {
                moshi.adapter(TakeoutSemanticLocationHistoryMonth::class.java)
                    .fromJson(semanticLocationHistoryMonthString) ?: return emptyList()
        } catch (exception: JsonEncodingException) {
            null
        } ?: return emptyList()

        val locationInDegrees =
            takeoutSemanticLocationHistoryMonth.timelineObjects.map {
                if (it.placeVisit != null) {
                    val location = it.placeVisit.location
                    val updatedLocation = location.copy(
                        latitudeE7 = location.latitudeE7 / DEGREES_DIVISION_FACTOR,
                        longitudeE7 = location.longitudeE7 / DEGREES_DIVISION_FACTOR
                    )
                    return@map it.copy(placeVisit = it.placeVisit.copy(location = updatedLocation))
                } else if (it.activitySegment != null) {
                    val location = it.activitySegment.startLocation
                    val updatedLocation = location.copy(
                        latitudeE7 = location.latitudeE7 / DEGREES_DIVISION_FACTOR,
                        longitudeE7 = location.longitudeE7 / DEGREES_DIVISION_FACTOR
                    )
                    return@map it.copy(activitySegment = it.activitySegment.copy(startLocation = updatedLocation))
                } else {
                    return@map it
                }
            }

        val uniqueDateTimelineObjects = mutableMapOf<LocalDate, TimelineObject>()
        val dates = mutableListOf<LocalDate>()
        for (timelineObject in locationInDegrees) {
            val date = timelineObject.placeVisit?.duration?.startTimestamp
                ?: timelineObject.activitySegment?.duration?.endTimestamp ?: continue
            uniqueDateTimelineObjects.putIfAbsent(
                date.withZoneSameInstant(ZoneOffset.UTC).toLocalDate(), timelineObject
            )
            dates.add(date.toLocalDate())
        }
        return uniqueDateTimelineObjects.map { (date,timelineObject) ->
            val location = if (timelineObject.placeVisit != null) {
                timelineObject.placeVisit.location
            } else {
                timelineObject.activitySegment!!.startLocation
            }
            DateLocation(date, location)
        }
    }

    private data class DateLocation(val date: LocalDate, val location: TakeoutLocation)
}

private const val DEGREES_DIVISION_FACTOR = 10000000
