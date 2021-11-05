package com.alexsullivan.datacollor.database

import androidx.room.Entity
import java.util.*

@Entity(tableName = "trackable_entity_table", primaryKeys = ["trackableId", "date"])
data class TrackableEntity(
    val trackableId: String,
    val executed: Boolean,
    val date: Date
)
