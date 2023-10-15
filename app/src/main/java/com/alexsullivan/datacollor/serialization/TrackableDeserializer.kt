package com.alexsullivan.datacollor.serialization

import android.annotation.SuppressLint
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableType
import com.alexsullivan.datacollor.database.entities.BooleanTrackableEntity
import com.alexsullivan.datacollor.database.entities.NumberTrackableEntity
import com.alexsullivan.datacollor.database.entities.Rating
import com.alexsullivan.datacollor.database.entities.RatingTrackableEntity
import com.alexsullivan.datacollor.database.entities.TrackableEntity
import com.alexsullivan.datacollor.database.entities.WeatherEntity
import com.alexsullivan.datacollor.serialization.TrackableSerializer.DAILY_TEMP_TITLE
import com.alexsullivan.datacollor.serialization.TrackableSerializer.DAILY_WEATHER_DESCRIPTION_TITLE
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.util.UUID

object TrackableDeserializer {

    fun deserialize(contents: String): LifetimeData {
        val trackableTitleMap = mutableMapOf<String, List<TrackableEntity?>>()
        val weatherList = mutableListOf<WeatherEntity>()
        // Go through the data and parse out trackable title, entity value, and weather
        contents.trim().split("\n").map {
            val csvs = it.split(",")
            // First should be the date
            val date = parseDate(csvs[0])
            // Then pairs of Trackable -> some type
            val trackablePairStrings = csvs.subList(1, csvs.size)
            var i = 0
            while (i < trackablePairStrings.lastIndex) {
                // Check titles of current title/data pair and the next title/data pair
                if (isWeather(trackablePairStrings.getOrNull(i), trackablePairStrings.getOrNull(i+2))) {
                    // It's weather, so parse out the data from the current title/data pair and the
                    // data from the next title/data pair
                    val weather = parseWeather(
                        date,
                        trackablePairStrings.getOrNull(i + 1),
                        trackablePairStrings.getOrNull(i + 3)
                    )
                    weather?.let(weatherList::add)
                    i += 4
                    continue
                }
                val trackableTitle = trackablePairStrings[i]
                val data = trackablePairStrings[i + 1]
                val entity = constructEntity(data, date)
                val entities = trackableTitleMap[trackableTitle] ?: emptyList()
                val updatedEntities = entities + entity
                trackableTitleMap[trackableTitle] = updatedEntities
                i+=2
            }
        }
        // Create actual map of trackables to trackable entities. Note that the entities at this
        // point do not have a populated trackable ID field.
        val trackableMap = trackableTitleMap.mapKeys { (title, entries) ->
            val type = when (entries.firstNotNullOf { it }) {
                is TrackableEntity.Boolean -> TrackableType.BOOLEAN
                is TrackableEntity.Number -> TrackableType.NUMBER
                is TrackableEntity.Rating -> TrackableType.RATING
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
                }
            }
        }

        val trackables = populatedTrackableMap.keys.toList()
        val entitesByDate = populatedTrackableMap.values.flatten().groupBy { it.date }
        val days = entitesByDate.map { (date, entities) -> DayData(date, entities, null) }
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
        val rating = data.deserializeToRating()
        val booleanData = data.toBooleanStrictOrNull()
        return when {
            numberData != null -> TrackableEntity.Number(NumberTrackableEntity("", numberData, date))
            rating != null -> TrackableEntity.Rating(RatingTrackableEntity("", rating, date))
            booleanData != null -> TrackableEntity.Boolean(BooleanTrackableEntity("", booleanData, date))
            else -> null
        }
    }

    private fun String.deserializeToRating(): Rating? {
        val rating = Rating.values().firstOrNull { rating ->
            rating.name.lowercase() == this.lowercase()
        }
        return rating
    }
}
