package com.alexsullivan.datacollor.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.alexsullivan.datacollor.database.entities.WeatherEntity

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWeather(weatherEntity: WeatherEntity)
}
