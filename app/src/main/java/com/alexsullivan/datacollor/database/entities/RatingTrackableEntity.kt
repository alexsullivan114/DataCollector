package com.alexsullivan.datacollor.database.entities

import androidx.room.Entity
import java.time.LocalDate

@Entity(tableName = "rating_trackable_entity_table", primaryKeys = ["trackableId", "date"])
data class RatingTrackableEntity(val trackableId: String, val rating: Rating, val date: LocalDate)
