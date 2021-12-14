package com.alexsullivan.datacollor.database

import androidx.room.Entity
import java.util.*

@Entity(tableName = "boolean_trackable_entity_table", primaryKeys = ["trackableId", "date"])
data class BooleanTrackableEntity(
    val trackableId: String,
    val executed: Boolean,
    val date: Date
)
