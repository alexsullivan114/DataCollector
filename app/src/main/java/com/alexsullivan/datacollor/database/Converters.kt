package com.alexsullivan.datacollor.database

import androidx.room.TypeConverter
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
            else -> null
        }
    }

    @TypeConverter
    fun fromTrackableType(trackableType: TrackableType?): Int? {
       return when (trackableType) {
           TrackableType.BOOLEAN -> 0
           TrackableType.NUMBER -> 1
           null -> null
       }
    }
}
