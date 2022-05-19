package com.alexsullivan.datacollor.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alexsullivan.datacollor.database.entities.BooleanTrackableEntity
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

@Dao
interface BooleanEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: BooleanTrackableEntity)

    @Query("SELECT * FROM boolean_trackable_entity_table WHERE date = :date AND trackableId = :id LIMIT 1")
    suspend fun getEntity(date: OffsetDateTime, id: String): BooleanTrackableEntity

    @Query("SELECT * FROM boolean_trackable_entity_table")
    suspend fun getEntities(): List<BooleanTrackableEntity>

    @Query("SELECT * FROM boolean_trackable_entity_table WHERE date = :date")
    suspend fun getEntities(date: OffsetDateTime): List<BooleanTrackableEntity>

    @Query("SELECT * FROM boolean_trackable_entity_table WHERE date = :date")
    fun getEntitiesFlow(date: OffsetDateTime): Flow<List<BooleanTrackableEntity>>

    @Query("DELETE FROM boolean_trackable_entity_table WHERE trackableId = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM boolean_trackable_entity_table WHERE trackableId = :id")
    suspend fun getEntities(id: String): List<BooleanTrackableEntity>
}
