package com.alexsullivan.datacollor.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*

@Dao
interface NumberEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: NumberTrackableEntity)

    @Query("SELECT * FROM number_trackable_entity_table WHERE date = :date AND trackableId = :id LIMIT 1")
    suspend fun getEntity(date: Date, id: String): NumberTrackableEntity

    @Query("SELECT * FROM number_trackable_entity_table")
    suspend fun getEntities(): List<NumberTrackableEntity>

    @Query("SELECT * FROM number_trackable_entity_table WHERE date = :date")
    suspend fun getEntities(date: Date): List<NumberTrackableEntity>

    @Query("DELETE FROM number_trackable_entity_table WHERE trackableId = :id")
    suspend fun delete(id: String)
}
