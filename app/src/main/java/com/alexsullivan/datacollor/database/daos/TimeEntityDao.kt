package com.alexsullivan.datacollor.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alexsullivan.datacollor.database.entities.TimeTrackableEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TimeEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: TimeTrackableEntity)

    @Query("SELECT * FROM time_trackable_entity_table WHERE date = :date AND trackableId = :id LIMIT 1")
    suspend fun getEntity(date: LocalDate, id: String): TimeTrackableEntity

    @Query("SELECT * FROM time_trackable_entity_table")
    suspend fun getEntities(): List<TimeTrackableEntity>

    @Query("SELECT * FROM time_trackable_entity_table WHERE date = :date")
    suspend fun getEntities(date: LocalDate): List<TimeTrackableEntity>

    @Query("SELECT * FROM time_trackable_entity_table WHERE date = :date")
    fun getEntitiesFlow(date: LocalDate): Flow<List<TimeTrackableEntity>>

    @Query("DELETE FROM time_trackable_entity_table WHERE trackableId = :id")
    suspend fun deleteAllForTrackable(id: String)

    @Delete
    suspend fun delete(entity: TimeTrackableEntity)

    @Query("SELECT * FROM time_trackable_entity_table WHERE trackableId = :id")
    suspend fun getEntities(id: String): List<TimeTrackableEntity>
}
