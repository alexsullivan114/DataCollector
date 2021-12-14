package com.alexsullivan.datacollor

import android.annotation.SuppressLint
import com.alexsullivan.datacollor.database.*
import java.lang.NumberFormatException
import java.text.SimpleDateFormat
import java.util.*

object TrackableSerializer {
    @SuppressLint("SimpleDateFormat")
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    fun serialize(entities: List<TrackableEntity>, trackables: List<Trackable>): String {
        val groupedTrackables = entities.groupBy { it.date }.toSortedMap()
        val csvText = groupedTrackables.map { (date, entities) ->
            var entry = format.format(date)
            val pairedEntities = entities.map { entity ->
                val associatedTrackable = trackables.first { it.id == entity.trackableId }
                entity to associatedTrackable
            }
            val sortedEntities = pairedEntities.sortedBy { it.second.title }
            sortedEntities.forEach { (entity, trackable) ->
                val value = when (entity) {
                    is TrackableEntity.Boolean -> entity.booleanEntity.executed.toString()
                    is TrackableEntity.Number -> entity.numberEntity.count.toString()
                }
                entry += ",${trackable.title},${value}"
            }
            entry
        }.fold("") { acc, entity -> acc + entity + "\n" }
        return csvText
    }

    fun deserialize(contents: String): Map<Trackable, List<TrackableEntity>> {
        val trackableTitleMap = mutableMapOf<String, List<TrackableEntity>>()
        contents.trim().split("\n").map {
            val csvs = it.split(",")
            // First should be the date
            val date = format.parse(csvs[0])!!
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
            val type = when (entries[0]) {
                is TrackableEntity.Boolean -> TrackableType.BOOLEAN
                is TrackableEntity.Number -> TrackableType.NUMBER
            }
            Trackable(UUID.randomUUID().toString(), title, true, type)
        }

        val returnMap = trackableMap.mapValues { (trackable, entites) ->
            entites.map { entity ->
                when (entity) {
                    is TrackableEntity.Boolean -> TrackableEntity.Boolean(entity.booleanEntity.copy(trackableId = trackable.id))
                    is TrackableEntity.Number -> TrackableEntity.Number(entity.numberEntity.copy(trackableId = trackable.id))
                }
            }
        }
        return returnMap
    }

    private fun constructEntity(data: String, date: Date): TrackableEntity {
        return try {
            val numberData = data.toInt()
            return TrackableEntity.Number(NumberTrackableEntity("", numberData, date))
        } catch (e: NumberFormatException) {
            val booleanData = data.toBooleanStrict()
            return TrackableEntity.Boolean(BooleanTrackableEntity("", booleanData, date))
        }
    }
}
