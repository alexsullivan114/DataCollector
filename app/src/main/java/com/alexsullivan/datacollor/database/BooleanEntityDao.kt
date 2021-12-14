package com.alexsullivan.datacollor.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*

@Dao
interface BooleanEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: BooleanTrackableEntity)

    @Query("SELECT * FROM boolean_trackable_entity_table WHERE date = :date AND trackableId = :id LIMIT 1")
    suspend fun getEntity(date: Date, id: String): BooleanTrackableEntity

    @Query("SELECT * FROM boolean_trackable_entity_table")
    suspend fun getEntities(): List<BooleanTrackableEntity>

    @Query("SELECT * FROM boolean_trackable_entity_table WHERE date = :date")
    suspend fun getEntities(date: Date): List<BooleanTrackableEntity>

    @Query("DELETE FROM boolean_trackable_entity_table WHERE trackableId = :id")
    suspend fun delete(id: String)
}
