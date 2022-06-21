package com.alexsullivan.datacollor.database.daos

import androidx.room.*
import com.alexsullivan.datacollor.database.entities.NumberTrackableEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface NumberEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: NumberTrackableEntity)

    @Query("SELECT * FROM number_trackable_entity_table WHERE date = :date AND trackableId = :id LIMIT 1")
    suspend fun getEntity(date: LocalDate, id: String): NumberTrackableEntity

    @Query("SELECT * FROM number_trackable_entity_table")
    suspend fun getEntities(): List<NumberTrackableEntity>

    @Query("SELECT * FROM number_trackable_entity_table WHERE date = :date")
    suspend fun getEntities(date: LocalDate): List<NumberTrackableEntity>

    @Query("SELECT * FROM number_trackable_entity_table WHERE date = :date")
    fun getEntitiesFlow(date: LocalDate): Flow<List<NumberTrackableEntity>>

    @Query("DELETE FROM number_trackable_entity_table WHERE trackableId = :id")
    suspend fun deleteAllForTrackable(id: String)

    @Delete
    suspend fun delete(entity: NumberTrackableEntity)

    @Query("SELECT * FROM number_trackable_entity_table WHERE trackableId = :id")
    suspend fun getEntities(id: String): List<NumberTrackableEntity>
}
