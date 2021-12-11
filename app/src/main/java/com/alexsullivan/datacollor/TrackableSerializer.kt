package com.alexsullivan.datacollor

import android.annotation.SuppressLint
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableEntity
import java.text.SimpleDateFormat
import java.util.*

object TrackableSerializer {
    @SuppressLint("SimpleDateFormat")
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    fun serialize(trackableEntities: List<TrackableEntity>, trackables: List<Trackable>): String {
        val groupedTrackables = trackableEntities.groupBy { it.date }.toSortedMap()
        val csvText = groupedTrackables.map { (date, entities) ->
            var entry = format.format(date)
            entities.forEach { entity ->
                val trackable = trackables.first { it.id == entity.trackableId }
                entry += ",${trackable.title},${entity.executed}"
            }
            entry
        }.fold("") { acc, entity -> acc + entity + "\n" }
        return csvText
    }

    data class CsvLine(val date: Date, val trackables: List<Pair<String, Boolean>>)
    fun deserialize(contents: String): Map<Trackable, List<TrackableEntity>> {
        val csvLines = contents.trim().split("\n").map {
            val csvs = it.split(",")
            // First should be the date
            val date = format.parse(csvs[0])!!
            // Then pairs of Trackable -> boolean
            val trackablePairStrings = csvs.subList(1, csvs.size)
            val trackablePairs = mutableListOf<Pair<String, Boolean>>()
            for (i in 0 until trackablePairStrings.lastIndex step 2) {
                val trackableTitle = trackablePairStrings[i]
                val toggled = trackablePairStrings[i + 1].toBooleanStrict()
                trackablePairs.add(trackableTitle to toggled)
            }
            CsvLine(date, trackablePairs)
        }
        val trackableTitleMap = mutableMapOf<String, List<Pair<Date, Boolean>>>()
        csvLines.forEach { csvLine ->
            csvLine.trackables.forEach { (trackableTitle, enabled) ->
                val pairList = trackableTitleMap.getOrPut(trackableTitle, { emptyList() })
                val updatedList = pairList + (csvLine.date to enabled)
                trackableTitleMap[trackableTitle] = updatedList
            }
        }

        val trackableEntityMap: MutableMap<Trackable, List<TrackableEntity>> = mutableMapOf()
        trackableTitleMap.forEach { (trackableTitle, entityList) ->
            val trackable = Trackable(UUID.randomUUID().toString(), trackableTitle, true)
            val entities = entityList.map { (date, toggled) ->
                TrackableEntity(trackable.id, toggled, date)
            }
            trackableEntityMap[trackable] = entities
        }

        return trackableEntityMap
    }
}
