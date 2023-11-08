package com.alexsullivan.datacollor.serialization

import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.entities.Rating
import com.alexsullivan.datacollor.database.entities.TrackableEntity
import com.alexsullivan.datacollor.database.entities.WeatherEntity
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TrackableSerializer {
    const val DAILY_TEMP_TITLE = "DailyTemp"
    const val DAILY_WEATHER_DESCRIPTION_TITLE = "WeatherDescription"
    const val UNSET_TIME = "null-time"

    fun serialize(data: LifetimeData): String {
        val sortedTrackables = data.trackables.sortedBy { it.title }
        val csvText = data.days.sortedBy { it.date }.map { (date, entities, weather) ->
            var entry = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            sortedTrackables.forEach { trackable ->
                val associatedEntity = entities.firstOrNull { it.trackableId == trackable.id }
                val dataString = serializeEntity(trackable, associatedEntity)
                entry += dataString
            }
            entry += serializeWeather(weather)
            entry
        }.fold("") { acc, entity -> acc + entity + "\n" }
        return csvText
    }


    private fun serializeEntity(trackable: Trackable, entity: TrackableEntity?): String {
        return if (entity != null) {
            val value = when (entity) {
                is TrackableEntity.Boolean -> entity.booleanEntity.executed.toString()
                is TrackableEntity.Number -> entity.numberEntity.count.toString()
                is TrackableEntity.Rating -> entity.ratingEntity.rating.serialized()
                is TrackableEntity.Time -> entity.timeEntity.time?.serialized()
            }
            ",${trackable.title},${value}"
        } else {
            ",${trackable.title},"
        }
    }

    private fun serializeWeather(weatherEntity: WeatherEntity?): String {
        var returnString = ",$DAILY_TEMP_TITLE,%s,$DAILY_WEATHER_DESCRIPTION_TITLE,%s"
        if (weatherEntity != null) {
            returnString =
                returnString.format(weatherEntity.temp.toString(), weatherEntity.description)
        } else {
            returnString = returnString.format(null, "", "")
        }
        return returnString
    }

    private fun Rating.serialized() = this.name.lowercase().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
        ) else it.toString()
    }

    private fun LocalTime?.serialized() = if (this != null) {
        this.format(DateTimeFormatter.ISO_TIME)
    } else {
        UNSET_TIME
    }
}
