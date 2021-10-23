package com.alexsullivan.datacollor.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface TrackableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEntity(entity: TrackableEntity)

    @Query("SELECT * FROM trackable_entity_table")
    suspend fun getTrackableEntities(): List<TrackableEntity>

    @Query("SELECT * FROM trackable_entity_table WHERE date = :date")
    suspend fun getTrackableEntities(date: Date): List<TrackableEntity>

    @Query("DELETE FROM trackable_entity_table")
    suspend fun nuke()

    @Query("SELECT * FROM trackable_table where enabled = 1")
    suspend fun getEnabledTrackables(): List<Trackable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTrackable(trackable: Trackable)

    @Delete
    suspend fun deleteTrackable(trackable: Trackable)

    @Query("SELECT * FROM trackable_table")
    suspend fun getTrackables(): List<Trackable>

    @Query("SELECT * FROM trackable_table")
    fun getTrackablesFlow(): Flow<List<Trackable>>

    @Query("SELECT * FROM trackable_table WHERE id = :id")
    suspend fun getTrackableById(id: Int): Trackable
}
