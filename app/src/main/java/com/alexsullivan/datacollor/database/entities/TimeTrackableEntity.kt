package com.alexsullivan.datacollor.database.entities

import androidx.room.Entity
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "time_trackable_entity_table", primaryKeys = ["trackableId", "date"])
data class TimeTrackableEntity(
    val trackableId: String,
    val time: LocalTime?,
    val date: LocalDate,
)
