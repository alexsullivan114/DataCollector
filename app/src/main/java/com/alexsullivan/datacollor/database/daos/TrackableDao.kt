package com.alexsullivan.datacollor.database.daos

import androidx.room.*
import com.alexsullivan.datacollor.database.Trackable
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTrackable(trackable: Trackable)

    @Delete
    suspend fun deleteTrackable(trackable: Trackable)

    @Query("SELECT * FROM trackable_table")
    suspend fun getTrackables(): List<Trackable>

    @Query("SELECT * FROM trackable_table")
    fun getTrackablesFlow(): Flow<List<Trackable>>

    @Query("SELECT * FROM trackable_table WHERE id = :id")
    suspend fun getTrackableById(id: String): Trackable?

    @Query("SELECT * FROM trackable_table where enabled = 1")
    suspend fun getEnabled(): List<Trackable>
}
