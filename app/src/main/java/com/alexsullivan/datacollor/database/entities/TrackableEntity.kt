package com.alexsullivan.datacollor.database.entities

import java.util.*

sealed class TrackableEntity {
    val trackableId: String
        get() = when (this) {
            is Boolean -> booleanEntity.trackableId
            is Number -> numberEntity.trackableId
            is Rating -> ratingEntity.trackableId
        }
    val date: Date
        get() = when (this) {
            is Boolean -> booleanEntity.date
            is Number -> numberEntity.date
            is Rating -> ratingEntity.date
        }
    data class Number(val numberEntity: NumberTrackableEntity): TrackableEntity()
    data class Boolean(val booleanEntity: BooleanTrackableEntity): TrackableEntity()
    data class Rating(val ratingEntity: RatingTrackableEntity): TrackableEntity()
}
