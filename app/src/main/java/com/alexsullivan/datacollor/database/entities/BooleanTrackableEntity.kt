package com.alexsullivan.datacollor.database.entities

import androidx.room.Entity
import java.time.LocalDate

@Entity(tableName = "boolean_trackable_entity_table", primaryKeys = ["trackableId", "date"])
data class BooleanTrackableEntity(
    val trackableId: String,
    val executed: Boolean,
    val date: LocalDate,
)
