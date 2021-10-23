package com.alexsullivan.datacollor.database

import androidx.room.*
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEnabledTrackable(enabledTrackable: EnabledTrackable)

    @Delete
    suspend fun deleteEnabledTrackable(enabledTrackable: EnabledTrackable)

    @Query("SELECT * FROM enabled_trackable_table INNER JOIN trackable_table ON trackable_table.id = enabled_trackable_table.id")
    suspend fun getEnabledTrackables(): List<Trackable>

    @Query("SELECT * FROM enabled_trackable_table")
    suspend fun getAllEnabledTrackables(): List<EnabledTrackable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTrackable(trackable: Trackable)

    @Delete
    suspend fun deleteTrackable(trackable: Trackable)

    @Query("SELECT * FROM trackable_table")
    suspend fun getAllTrackables(): List<Trackable>

    @Query("SELECT * FROM trackable_table WHERE id = :id")
    suspend fun getTrackableById(id: Int): Trackable
}
