package com.alexsullivan.datacollor.database

import java.util.*

sealed class TrackableEntity {
    val trackableId: String
        get() = when (this) {
            is Boolean -> booleanEntity.trackableId
            is Number -> numberEntity.trackableId
        }
    val date: Date
        get() = when (this) {
            is Boolean -> booleanEntity.date
            is Number -> numberEntity.date
        }
    data class Number(val numberEntity: NumberTrackableEntity): TrackableEntity()
    data class Boolean(val booleanEntity: BooleanTrackableEntity): TrackableEntity()
}
