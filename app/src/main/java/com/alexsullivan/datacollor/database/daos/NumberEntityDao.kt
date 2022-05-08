package com.alexsullivan.datacollor.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alexsullivan.datacollor.database.entities.NumberTrackableEntity
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

@Dao
interface NumberEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: NumberTrackableEntity)

    @Query("SELECT * FROM number_trackable_entity_table WHERE date = :date AND trackableId = :id LIMIT 1")
    suspend fun getEntity(date: OffsetDateTime, id: String): NumberTrackableEntity

    @Query("SELECT * FROM number_trackable_entity_table")
    suspend fun getEntities(): List<NumberTrackableEntity>

    @Query("SELECT * FROM number_trackable_entity_table WHERE date = :date")
    suspend fun getEntities(date: OffsetDateTime): List<NumberTrackableEntity>

    @Query("SELECT * FROM number_trackable_entity_table WHERE date = :date")
    fun getEntitiesFlow(date: OffsetDateTime): Flow<List<NumberTrackableEntity>>

    @Query("DELETE FROM number_trackable_entity_table WHERE trackableId = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM number_trackable_entity_table WHERE trackableId = :id")
    suspend fun getEntities(id: String): List<NumberTrackableEntity>
}
