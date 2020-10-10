package com.alexsullivan.datacollor

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "trackable_entity_table", primaryKeys = ["trackable", "date"])
data class TrackableEntity(
    val trackable: Trackable,
    val executed: Boolean,
    val date: Date
)