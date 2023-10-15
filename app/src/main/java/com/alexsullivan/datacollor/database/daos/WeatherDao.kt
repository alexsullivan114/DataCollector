package com.alexsullivan.datacollor.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alexsullivan.datacollor.database.entities.WeatherEntity
import java.time.LocalDate

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWeather(weatherEntity: WeatherEntity)

    @Query("SELECT * FROM WEATHER_TABLE")
    suspend fun getAllWeather(): List<WeatherEntity>

    @Query("SELECT * FROM WEATHER_TABLE WHERE date = :date")
    suspend fun getWeather(date: LocalDate): WeatherEntity?
}
