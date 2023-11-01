package com.alexsullivan.datacollor.weather.location_import

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.ZonedDateTime

class TimeAdapter {
    @ToJson
    fun toJson(zonedDateTime: ZonedDateTime): String {
        return zonedDateTime.toString()
    }

    @FromJson
    fun fromJson(zonedDateTime: String): ZonedDateTime {
        return ZonedDateTime.parse(zonedDateTime)
    }
}
