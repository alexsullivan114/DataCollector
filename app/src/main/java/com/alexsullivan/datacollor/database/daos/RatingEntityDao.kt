package com.alexsullivan.datacollor.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alexsullivan.datacollor.database.entities.RatingTrackableEntity
import java.util.*

@Dao
interface RatingEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: RatingTrackableEntity)

    @Query("SELECT * FROM rating_trackable_entity_table WHERE date = :date AND trackableId = :id LIMIT 1")
    suspend fun getEntity(date: Date, id: String): RatingTrackableEntity

    @Query("SELECT * FROM rating_trackable_entity_table")
    suspend fun getEntities(): List<RatingTrackableEntity>

    @Query("SELECT * FROM rating_trackable_entity_table WHERE date = :date")
    suspend fun getEntities(date: Date): List<RatingTrackableEntity>

    @Query("DELETE FROM rating_trackable_entity_table WHERE trackableId = :id")
    suspend fun delete(id: String)
}
