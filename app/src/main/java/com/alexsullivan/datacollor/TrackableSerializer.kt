package com.alexsullivan.datacollor

import android.annotation.SuppressLint
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableType
import com.alexsullivan.datacollor.database.entities.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

object TrackableSerializer {

    fun serialize(entities: List<TrackableEntity>, trackables: List<Trackable>): String {
        val sortedTrackables = trackables.sortedBy { it.title }
        // TODO: Check to see that this is correct
        val groupedTrackables = entities.groupBy { it.date }.toSortedMap()
        val csvText = groupedTrackables.map { (date, entities) ->
            var entry = date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            sortedTrackables.forEach { trackable ->
                val associatedEntity = entities.firstOrNull { it.trackableId == trackable.id }
                val dataString = serializeEntity(trackable, associatedEntity)
                entry += dataString
            }
            entry
        }.fold("") { acc, entity -> acc + entity + "\n" }
        return csvText
    }

    fun deserialize(contents: String): Map<Trackable, List<TrackableEntity>> {
        val trackableTitleMap = mutableMapOf<String, List<TrackableEntity?>>()
        contents.trim().split("\n").map {
            val csvs = it.split(",")
            // First should be the date
            val date = parseDate(csvs[0])
            // Then pairs of Trackable -> some type
            val trackablePairStrings = csvs.subList(1, csvs.size)
            for (i in 0 until trackablePairStrings.lastIndex step 2) {
                val trackableTitle = trackablePairStrings[i]
                val data = trackablePairStrings[i + 1]
                val entity = constructEntity(data, date)
                val entities = trackableTitleMap[trackableTitle] ?: emptyList()
                val updatedEntities = entities + entity
                trackableTitleMap[trackableTitle] = updatedEntities
            }
        }
        val trackableMap = trackableTitleMap.mapKeys { (title, entries) ->
            val type = when (entries.firstNotNullOf { it }) {
                is TrackableEntity.Boolean -> TrackableType.BOOLEAN
                is TrackableEntity.Number -> TrackableType.NUMBER
                is TrackableEntity.Rating -> TrackableType.RATING
            }
            Trackable(UUID.randomUUID().toString(), title, true, type)
        }

        val returnMap = trackableMap.mapValues { (trackable, entities) ->
            entities.filterNotNull().map { entity ->
                when (entity) {
                    is TrackableEntity.Boolean -> TrackableEntity.Boolean(entity.booleanEntity.copy(trackableId = trackable.id))
                    is TrackableEntity.Number -> TrackableEntity.Number(entity.numberEntity.copy(trackableId = trackable.id))
                    is TrackableEntity.Rating -> TrackableEntity.Rating(entity.ratingEntity.copy(trackableId = trackable.id))
                }
            }
        }
        return returnMap
    }

    private fun serializeEntity(trackable: Trackable, entity: TrackableEntity?): String {
        return if (entity != null) {
            val value = when (entity) {
                is TrackableEntity.Boolean -> entity.booleanEntity.executed.toString()
                is TrackableEntity.Number -> entity.numberEntity.count.toString()
                is TrackableEntity.Rating -> entity.ratingEntity.rating.serialized()
            }
            ",${trackable.title},${value}"
        } else {
            ",${trackable.title},"
        }
    }

    private fun constructEntity(data: String, date: OffsetDateTime): TrackableEntity? {
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

    private fun Rating.serialized() = this.name.lowercase().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
        ) else it.toString()
    }

    private fun String.deserializeToRating(): Rating? {
        val rating = Rating.values().firstOrNull { rating ->
            rating.name.lowercase() == this.lowercase()
        }
        return rating
    }

    @SuppressLint("SimpleDateFormat")
    private fun parseDate(dateString: String): OffsetDateTime {
        return try {
            OffsetDateTime.parse(dateString)
        } catch (exception: DateTimeParseException) {
            // We're probably in old date land.
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = format.parse(dateString)!!
            OffsetDateTime.ofInstant(Instant.ofEpochMilli(date.time), ZoneId.systemDefault())
        }
    }
}
