package com.alexsullivan.datacollor.database

import androidx.room.TypeConverter
import com.alexsullivan.datacollor.database.entities.Rating
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toTrackableType(value: Int?): TrackableType? {
        return when (value) {
            0 -> TrackableType.BOOLEAN
            1 -> TrackableType.NUMBER
            2 -> TrackableType.RATING
            else -> null
        }
    }

    @TypeConverter
    fun fromTrackableType(trackableType: TrackableType?): Int? {
       return when (trackableType) {
           TrackableType.BOOLEAN -> 0
           TrackableType.NUMBER -> 1
           TrackableType.RATING -> 2
           null -> null
       }
    }

    @TypeConverter
    fun toRating(value: Int?): Rating? {
        return when (value) {
            0 -> Rating.TERRIBLE
            1 -> Rating.POOR
            2 -> Rating.MEDIOCRE
            3 -> Rating.GOOD
            4 -> Rating.GREAT
            else -> null
        }
    }

    @TypeConverter
    fun fromRating(rating: Rating?): Int? {
       return when (rating) {
           Rating.TERRIBLE -> 0
           Rating.POOR -> 1
           Rating.MEDIOCRE -> 2
           Rating.GOOD -> 3
           Rating.GREAT -> 4
           null -> null
       }
    }
}
