package com.alexsullivan.datacollor.database.entities

import androidx.room.Entity
import java.time.LocalDate

@Entity(tableName = "weather_table", primaryKeys = ["date"])
data class WeatherEntity(val date: LocalDate, val temp: Float, val description: String)
