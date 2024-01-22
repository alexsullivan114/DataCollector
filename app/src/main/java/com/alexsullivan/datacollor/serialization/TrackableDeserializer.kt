package com.alexsullivan.datacollor.serialization

import android.annotation.SuppressLint
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableType
import com.alexsullivan.datacollor.database.entities.BooleanTrackableEntity
import com.alexsullivan.datacollor.database.entities.NumberTrackableEntity
import com.alexsullivan.datacollor.database.entities.Rating
import com.alexsullivan.datacollor.database.entities.RatingTrackableEntity
import com.alexsullivan.datacollor.database.entities.TimeTrackableEntity
import com.alexsullivan.datacollor.database.entities.TrackableEntity
import com.alexsullivan.datacollor.database.entities.WeatherEntity
import com.alexsullivan.datacollor.serialization.TrackableSerializer.DAILY_TEMP_TITLE
import com.alexsullivan.datacollor.serialization.TrackableSerializer.DAILY_WEATHER_DESCRIPTION_TITLE
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.util.UUID

object TrackableDeserializer {

    fun deserialize(contents: String): LifetimeData {
        // Get all of the headers.
        val headers = mutableListOf<String>()
        contents.substringBefore("\n").split(",").forEach(headers::add)
        val trackableTitleMap = mutableMapOf<String, List<TrackableEntity?>>()
        val weatherList = mutableListOf<WeatherEntity>()
        // Go through the data and parse out trackable title, entity value, and weather
        contents.substringAfter("\n").trim().split("\n").map {
            val csvs = it.split(",")
            var processingDate: LocalDate? = null
            var dailyTemp: String? = null
            var weatherDescription: String? = null
            csvs.forEachIndexed { index, value ->
                // The date should always be our first item.
                when (index) {
                    0 -> processingDate = parseDate(value)
                    headers.indexOf(DAILY_TEMP_TITLE) -> dailyTemp = value
                    headers.indexOf(DAILY_WEATHER_DESCRIPTION_TITLE) -> weatherDescription = value
                    else -> constructEntity(value, processingDate!!)?.let { entity ->
                        val entities = trackableTitleMap[headers[index]] ?: emptyList()
                        val updatedEntities = entities + entity
                        trackableTitleMap[headers[index]] = updatedEntities
                    }
                }
            }
            parseWeather(processingDate!!, dailyTemp, weatherDescription)?.let(weatherList::add)
        }
        // Create actual map of trackables to trackable entities. Note that the entities at this
        // point do not have a populated trackable ID field.
        val trackableMap = trackableTitleMap.mapKeys { (title, entries) ->
            val type = when (entries.firstNotNullOf { it }) {
                is TrackableEntity.Boolean -> TrackableType.BOOLEAN
                is TrackableEntity.Number -> TrackableType.NUMBER
                is TrackableEntity.Rating -> TrackableType.RATING
                is TrackableEntity.Time -> TrackableType.TIME
            }
            Trackable(UUID.randomUUID().toString(), title, true, type)
        }

        // Go through all of the trackable entities and update them to have a correct, populated
        // trackable ID field.
        val populatedTrackableMap = trackableMap.mapValues { (trackable, entities) ->
            entities.filterNotNull().map { entity ->
                when (entity) {
                    is TrackableEntity.Boolean -> TrackableEntity.Boolean(entity.booleanEntity.copy(trackableId = trackable.id))
                    is TrackableEntity.Number -> TrackableEntity.Number(entity.numberEntity.copy(trackableId = trackable.id))
                    is TrackableEntity.Rating -> TrackableEntity.Rating(entity.ratingEntity.copy(trackableId = trackable.id))
                    is TrackableEntity.Time -> TrackableEntity.Time(entity.timeEntity.copy(trackableId = trackable.id))
                }
            }
        }

        val trackables = populatedTrackableMap.keys.toList()
        val entitesByDate = populatedTrackableMap.values.flatten().groupBy { it.date }
        val days = entitesByDate.map { (date, entities) ->
            DayData(date, entities, weatherList.firstOrNull { it.date == date })
        }
        return LifetimeData(trackables, days)
    }

    @SuppressLint("SimpleDateFormat")
    private fun parseDate(dateString: String): LocalDate {
        return try {
            OffsetDateTime.parse(dateString).toLocalDate()
        } catch (exception: DateTimeParseException) {
            LocalDate.parse(dateString)
        } catch (exception: DateTimeParseException) {
            // TODO: Will this ever happen now?
            // We're probably in old date land.
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = format.parse(dateString)!!
            OffsetDateTime.ofInstant(Instant.ofEpochMilli(date.time), ZoneId.systemDefault()).toLocalDate()
        }
    }

    private fun isWeather(dailyTempTitle: String?, weatherDescriptionTitle: String?): Boolean {
        return dailyTempTitle == DAILY_TEMP_TITLE && weatherDescriptionTitle == DAILY_WEATHER_DESCRIPTION_TITLE
    }

    private fun parseWeather(
        date: LocalDate,
        dailyTemp: String?,
        weatherDescription: String?
    ): WeatherEntity? {
        return if (dailyTemp?.toFloatOrNull() != null && weatherDescription != null) {
            WeatherEntity(date, dailyTemp.toFloat(), weatherDescription)
        } else {
            return null
        }
    }

    private fun constructEntity(data: String, date: LocalDate): TrackableEntity? {
        val numberData = data.toIntOrNull()
        val rating = data.deserializeToRatingOrNull()
        val booleanData = data.toBooleanStrictOrNull()
        val timeData = data.deserializeToTimeOrNull()
        return when {
            numberData != null -> TrackableEntity.Number(NumberTrackableEntity("", numberData, date))
            rating != null -> TrackableEntity.Rating(RatingTrackableEntity("", rating, date))
            booleanData != null -> TrackableEntity.Boolean(BooleanTrackableEntity("", booleanData, date))
            timeData != null -> TrackableEntity.Time(TimeTrackableEntity("", timeData.localTime, date))
            else -> null
        }
    }

    private fun String.deserializeToRatingOrNull(): Rating? {
        val rating = Rating.values().firstOrNull { rating ->
            rating.name.lowercase() == this.lowercase()
        }
        return rating
    }

    data class TimeContainer(val localTime: LocalTime?)

    private fun String.deserializeToTimeOrNull(): TimeContainer? {
        return try {
            val time = LocalTime.parse(this)
            TimeContainer(time)
        } catch (e: Exception) {
            if (this == TrackableSerializer.UNSET_TIME) {
                TimeContainer(null)
            } else {
                null
            }
        }
    }
}
