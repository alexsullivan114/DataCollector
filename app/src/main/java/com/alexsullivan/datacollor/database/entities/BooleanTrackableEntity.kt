package com.alexsullivan.datacollor.database.entities

import androidx.room.Entity
import java.time.OffsetDateTime

// TODO: The date primary key doesn't make sense...but maybe who cares?
@Entity(tableName = "boolean_trackable_entity_table", primaryKeys = ["trackableId", "date"])
data class BooleanTrackableEntity(
    val trackableId: String,
    val executed: Boolean,
    val date: OffsetDateTime,
)
