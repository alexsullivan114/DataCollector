package com.alexsullivan.datacollor.database.entities

import androidx.room.Entity
import java.time.LocalDate

@Entity(tableName = "number_trackable_entity_table", primaryKeys = ["trackableId", "date"])
data class NumberTrackableEntity(
    val trackableId: String,
    val count: Int,
    val date: LocalDate
)
