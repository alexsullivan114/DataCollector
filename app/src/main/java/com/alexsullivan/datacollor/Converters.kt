package com.alexsullivan.datacollor

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
    fun fromEntityValue(value: Int?): Trackable? {
        return Trackable.values().firstOrNull { it.id == value }
    }

    @TypeConverter
    fun toEntityValue(trackable: Trackable?): Int? {
        return trackable?.id
    }
}