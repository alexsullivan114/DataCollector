package com.alexsullivan.datacollor.database.daos

import androidx.room.*
import com.alexsullivan.datacollor.database.entities.RatingTrackableEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface RatingEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: RatingTrackableEntity)

    @Query("SELECT * FROM rating_trackable_entity_table WHERE date = :date AND trackableId = :id LIMIT 1")
    suspend fun getEntity(date: LocalDate, id: String): RatingTrackableEntity

    @Query("SELECT * FROM rating_trackable_entity_table")
    suspend fun getEntities(): List<RatingTrackableEntity>

    @Query("SELECT * FROM rating_trackable_entity_table WHERE date = :date")
    suspend fun getEntities(date: LocalDate): List<RatingTrackableEntity>

    @Query("SELECT * FROM rating_trackable_entity_table WHERE date = :date")
    fun getEntitiesFlow(date: LocalDate): Flow<List<RatingTrackableEntity>>

    @Query("DELETE FROM rating_trackable_entity_table WHERE trackableId = :id")
    suspend fun deleteAllForTrackable(id: String)

    @Delete
    suspend fun delete(entity: RatingTrackableEntity)

    @Query("SELECT * FROM rating_trackable_entity_table WHERE trackableId = :id")
    suspend fun getEntities(id: String): List<RatingTrackableEntity>
}
