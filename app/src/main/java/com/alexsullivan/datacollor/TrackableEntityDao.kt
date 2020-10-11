package com.alexsullivan.datacollor

import androidx.room.*
import java.util.*

@Dao
interface TrackableEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEntity(entity: TrackableEntity)

    @Query("SELECT * FROM trackable_entity_table")
    suspend fun getTrackableEntities(): List<TrackableEntity>

    @Query("SELECT * FROM trackable_entity_table WHERE date = :date")
    suspend fun getTrackableEntities(date: Date): List<TrackableEntity>

    @Query("DELETE FROM trackable_entity_table")
    suspend fun nuke()
}