package com.alexsullivan.datacollor.previousdays

import com.alexsullivan.datacollor.database.entities.Rating
import java.time.LocalTime

sealed class DisplayableTrackableEntity {
    abstract val title: String
    abstract val trackableId:String

    data class BooleanEntity(
        override val title: String,
        override val trackableId: String,
        val checked: Boolean
    ) :
        DisplayableTrackableEntity()

    data class NumberEntity(
        override val title: String,
        override val trackableId: String,
        val count: Int
    ) :
        DisplayableTrackableEntity()

    data class RatingEntity(
        override val title: String,
        override val trackableId: String,
        val rating: Rating
    ) :
        DisplayableTrackableEntity()

    data class TimeEntity(
        override val title: String,
        override val trackableId: String,
        val time: LocalTime?
    ) :
        DisplayableTrackableEntity()
}
