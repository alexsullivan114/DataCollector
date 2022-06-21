package com.alexsullivan.datacollor.database.daos

import androidx.room.*
import com.alexsullivan.datacollor.database.entities.BooleanTrackableEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface BooleanEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: BooleanTrackableEntity)

    @Query("SELECT * FROM boolean_trackable_entity_table WHERE date = :date AND trackableId = :id LIMIT 1")
    suspend fun getEntity(date: LocalDate, id: String): BooleanTrackableEntity

    @Query("SELECT * FROM boolean_trackable_entity_table")
    suspend fun getEntities(): List<BooleanTrackableEntity>

    @Query("SELECT * FROM boolean_trackable_entity_table WHERE date = :date")
    suspend fun getEntities(date: LocalDate): List<BooleanTrackableEntity>

    @Query("SELECT * FROM boolean_trackable_entity_table WHERE date = :date")
    fun getEntitiesFlow(date: LocalDate): Flow<List<BooleanTrackableEntity>>

    @Query("DELETE FROM boolean_trackable_entity_table WHERE trackableId = :id")
    suspend fun deleteAllForTrackable(id: String)

    @Delete
    suspend fun delete(entity: BooleanTrackableEntity)

    @Query("SELECT * FROM boolean_trackable_entity_table WHERE trackableId = :id")
    suspend fun getEntities(id: String): List<BooleanTrackableEntity>
}
