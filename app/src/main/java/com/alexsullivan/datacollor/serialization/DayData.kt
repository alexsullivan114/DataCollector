package com.alexsullivan.datacollor.serialization

import com.alexsullivan.datacollor.database.entities.TrackableEntity
import com.alexsullivan.datacollor.database.entities.WeatherEntity
import java.time.LocalDate

data class DayData(
    val date: LocalDate,
    val trackedEntities: List<TrackableEntity>,
    val weatherEntity: WeatherEntity?
)
